/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing;

import br.ufmt.genericlexerseb.ExpressionParser;
import br.ufmt.genericlexerseb.GenericLexerSEB;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericlexerseb.Structure;
import br.ufmt.genericlexerseb.Variable;
import br.ufmt.preprocessing.exceptions.CalibrationException;
import br.ufmt.preprocessing.exceptions.TiffErrorBandsException;
import br.ufmt.preprocessing.exceptions.TiffNotFoundException;
import static br.ufmt.preprocessing.utils.Constants.*;
import br.ufmt.preprocessing.utils.DataFile;
import br.ufmt.preprocessing.utils.ParameterEnum;
import br.ufmt.preprocessing.utils.Utilities;
import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
import java.awt.Point;
import java.awt.image.ColorModel;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.BandedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import sun.awt.image.SunWritableRaster;

/**
 *
 * @author raphael
 */
public class LandSat {

    private static int QUANTITY = 40000000;
//    private static int QUANTITY_LINES = 1000;
    public static HashMap<ParameterEnum, String> equations = new HashMap<>();
    private static ExecuteEquation executeEquation;

    static {
        verifyEquations();
    }

    public static boolean verifyEquations() {
        BufferedReader bur = null;
        try {
            String path = System.getProperty("user.dir") + "/source/landsat.prop";
            bur = new BufferedReader(new FileReader(path));
            String line = bur.readLine();

            List<String> variables = getVariables();
            ExpressionParser ex = new ExpressionParser();
            String vet[];
            boolean codigo = false;

            StringBuilder source = new StringBuilder();
            source.append("import br.ufmt.preprocessing.ExecuteEquation;\n");
            source.append("import static br.ufmt.preprocessing.utils.Constants.*;\n\n");

            source.append("public class Equation extends ExecuteEquation {\n");

            source.append("    public void execute(double[] pixel, int idx){\n");
            source.append("        float sumBandas = 0.0f,albedo,NDVI,SAVI,IAF,emissividadeNB,emissivity,Ts,LWd,Rn;\n");
            source.append("        int k;\n");

            GenericLexerSEB lexer = new GenericLexerSEB();
            Structure structure;
            while (line != null) {
//                System.out.println(line);
                line = line.replaceAll("[ ]+", "");
                vet = line.split("=");
                if (!vet[0].equals("reflectancia")) {
                    variables.add(vet[0]);
                    try {
//                    System.out.println("line:"+line);
                        ex.evaluateExpr(line, variables);
                        equations.put(ParameterEnum.valueOf(vet[0]), line);


                        structure = new Structure();
                        structure.setToken(vet[0]);
                        String equation = lexer.analyse(equations.get(ParameterEnum.valueOf(vet[0])), structure, null, LanguageType.JAVA);
                        equation = equation.replace("coef_calib_a", "calibration[k][0]");
                        equation = equation.replace("coef_calib_b", "calibration[k][1]");
                        equation = equation.replace("pixel", "pixel[k]");
                        equation = equation.replace("irrad_espectral", "calibration[k][2]");


                        if (vet[0].equals("rad_espectral")) {
                            line = bur.readLine();
                            line = line.replaceAll("[ ]+", "");
                            String[] vet2 = line.split("=");
                            variables.add(vet2[0]);
                            ex.evaluateExpr(line, variables);
                            equations.put(ParameterEnum.valueOf(vet2[0]), line);

                            structure = new Structure();
                            structure.setToken(vet2[0]);
                            String equation2 = lexer.analyse(equations.get(ParameterEnum.valueOf(vet2[0])), structure, null, LanguageType.JAVA);
                            equation2 = equation2.replace("coef_calib_a", "calibration[k][0]");
                            equation2 = equation2.replace("coef_calib_b", "calibration[k][1]");
                            equation2 = equation2.replace("pixel", "pixel[k]");
                            equation2 = equation2.replace("irrad_espectral", "calibration[k][2]");

                            codigo = true;
                            for (int i = 1; i < 8; i++) {

                                source.append("        k = " + (i - 1) + ";\n");
                                source.append("        " + equation.replace("rad_espectral", "banda" + i) + ";\n");
                                if (i != 6) {
                                    source.append("        " + equation2.replace("rad_espectral", "banda" + i).replace("reflectancia", "bandaRefletida" + i) + ";\n");
                                    source.append("        sumBandas += sumBandas * bandaRefletida" + i + ";\n\n");
                                }
                            }
                        } else if (vet[0].equals("reflectancia")) {
                        } else if (codigo) {
                            source.append("        " + equation + ";\n");
                        }


                    } catch (IllegalArgumentException e) {
//                    System.out.println("Equation is wrong: " + line);
                        System.out.println(e.getMessage());
                    }
                }
                line = bur.readLine();
            }

            source.append("        this.albedo[idx] = albedo;\n");
            source.append("        this.NDVI[idx] = NDVI;\n");
            source.append("        this.SAVI[idx] = SAVI;\n");
            source.append("        this.IAF[idx] = IAF;\n");
            source.append("        this.emissividadeNB[idx] = IAF;\n");
            source.append("        this.emissivity[idx] = emissivity;\n");
            source.append("        this.Ts[idx] = Ts;\n");
            source.append("        this.LWd[idx] = LWd;\n");
            source.append("        this.Rn[idx] = Rn;\n");

            source.append("    }\n");

            source.append("}\n");

            executeEquation = (ExecuteEquation) Utilities.compile(source.toString(), "Equation");

//            System.out.println(source.toString());
            System.exit(1);
        } catch (Exception ex) {
            Logger.getLogger(LandSat.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bur.close();
            } catch (IOException ex) {
                Logger.getLogger(LandSat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public static List<String> getVariables() {
        List<String> variables = Utilities.getVariables();
        variables.add("julianDay");
        variables.add("Z");
        variables.add("reflectanciaAtmosfera");
        variables.add("P");
        variables.add("UR");
        variables.add("Ta");
        variables.add("Kt");
        variables.add("L");
        variables.add("K1");
        variables.add("K2");
        variables.add("S");
        variables.add("StefanBoltzman");
        variables.add("latitude");
        variables.add("Rg_24h");
        variables.add("Uref");
        variables.add("coef_calib_a");
        variables.add("coef_calib_b");
        variables.add("irrad_espectral");
        variables.add("pixel");
        variables.add("banda1");
        variables.add("banda2");
        variables.add("banda3");
        variables.add("banda4");
        variables.add("banda5");
        variables.add("banda6");
        variables.add("banda7");
        variables.add("bandaRefletida1");
        variables.add("bandaRefletida2");
        variables.add("bandaRefletida3");
        variables.add("bandaRefletida4");
        variables.add("bandaRefletida5");
        variables.add("bandaRefletida6");
        variables.add("bandaRefletida7");
        variables.add("sumBandas");
        return variables;
    }

    public List<DataFile> preprocessing(String pathToOriginalTiff, float[][] calibration, float[] parameterAlbedo, int julianDay, float Z, float reflectanciaAtmosfera, float P, float UR, float Ta, float Kt, float L, float K1, float K2, float S, float StefanBoltzman, float latitude, float Rg_24h, float Uref) {

        File tiff = new File(pathToOriginalTiff);
        if (tiff.exists() && tiff.getName().endsWith(".tif")) {
            if (calibration != null && calibration.length == 7 && calibration[0].length == 3) {
                SeekableStream s = null;
                try {
                    List<DataFile> ret = new ArrayList<>();
//                    System.out.println("Arq:" + tiff.getName());
                    s = new FileSeekableStream(tiff);
                    TIFFDecodeParam param = null;
                    ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
                    // Which of the multiple images in the TIFF file do we want to load
                    // 0 refers to the first, 1 to the second and so on.
                    int bands;
                    ColorModel model = dec.decodeAsRenderedImage().getColorModel();
                    Raster raster = dec.decodeAsRaster(0);
                    bands = raster.getNumBands();
                    int width = raster.getWidth();
                    int height = raster.getHeight();
                    int tam = width * height;
//                    System.out.println("W:" + raster.getWidth() + " H:" + raster.getHeight());
//                    System.out.println("Size:" + tam);

                    if (bands == 7) {

                        Map<String, Variable> variables = Utilities.getVariable();
                        variables.put("julianDay", new Variable("julianDay", julianDay));
                        variables.put("Z", new Variable("Z", Z));
                        variables.put("reflectanciaAtmosfera", new Variable("reflectanciaAtmosfera", reflectanciaAtmosfera));
                        variables.put("P", new Variable("P", P));
                        variables.put("UR", new Variable("UR", UR));
                        variables.put("Ta", new Variable("Ta", Ta));
                        variables.put("Kt", new Variable("Kt", Kt));
                        variables.put("L", new Variable("L", L));
                        variables.put("K1", new Variable("K1", K1));
                        variables.put("K2", new Variable("K2", K2));
                        variables.put("S", new Variable("S", S));
                        variables.put("StefanBoltzman", new Variable("StefanBoltzman", StefanBoltzman));
                        variables.put("latitude", new Variable("latitude", latitude));
                        variables.put("Rg_24h", new Variable("Rg_24h", Rg_24h));
                        variables.put("Uref", new Variable("Uref", Uref));

                        Utilities.executeMath(ParameterEnum.dr, equations, variables);

                        Utilities.executeMath(ParameterEnum.cosZ, equations, variables);

                        Utilities.executeMath(ParameterEnum.declinacaoSolar, equations, variables);

                        Utilities.executeMath(ParameterEnum.anguloHorarioNascerSol, equations, variables);

                        Utilities.executeMath(ParameterEnum.rad_solar_toa, equations, variables);

                        Utilities.executeMath(ParameterEnum.Rg_24h_mj, equations, variables);

                        Utilities.executeMath(ParameterEnum.transmissividade24h, equations, variables);

                        Utilities.executeMath(ParameterEnum.ea, equations, variables);

                        Utilities.executeMath(ParameterEnum.W, equations, variables);

                        Utilities.executeMath(ParameterEnum.transmissividade, equations, variables);

                        Utilities.executeMath(ParameterEnum.emissivityAtm, equations, variables);

                        Utilities.executeMath(ParameterEnum.SWd, equations, variables);

                        Utilities.executeMath(ParameterEnum.LWdAtm, equations, variables);

//                        System.out.println("transmissividade:" + transmissividade);
//                        System.out.println("w:" + W);
//                        System.out.println("ea:" + ea);
//                        System.out.println("cosZ:" + cosZ);
//                        System.out.println("dr:" + dr);
//                        System.out.println("P:" + P);

//                        System.exit(1);

                        float reflectancia;

                        int size = QUANTITY;
                        if (tam < size) {
                            size = tam;
                        }

                        float[] albedoVet = null;
                        float[] NDVIVet = null;
                        float[] RnVet = null;
                        float[] SAVIVet = null;
                        float[] TsVet = null;
                        float[] emissivityVet = null;
                        float[] IAFVet = null;
                        float[] emissividadeNBVet = null;
                        float[] LWdVet = null;

                        float albedo;
                        float somaBandas;
                        float NDVI;
                        float Rn;
                        float SAVI;
                        float Ts;
                        float emissivity;
                        float IAF;
                        float emissividadeNB;
                        float LWd;

                        String parent = tiff.getParent() + "/OutputParameters/";
                        File dir = new File(parent);
                        dir.mkdirs();

                        String name = "Datas.dat";
                        List<ParameterEnum> parameters = new ArrayList<>();
                        parameters.add(ParameterEnum.albedo);
                        parameters.add(ParameterEnum.NDVI);
                        parameters.add(ParameterEnum.SAVI);
                        parameters.add(ParameterEnum.Ts);
                        parameters.add(ParameterEnum.emissivity);
                        parameters.add(ParameterEnum.IAF);
                        parameters.add(ParameterEnum.emissividadeNB);
                        parameters.add(ParameterEnum.LWnet);
//                        parameters.add(ParameterEnum.SWnet);
                        parameters.add(ParameterEnum.Rn);
//                        parameters.add(ParameterEnum.Tao_24h);
                        parameters.add(ParameterEnum.Rg_24h);
                        parameters.add(ParameterEnum.Uref);

                        PrintWriter pw;

                        String pathTiff;
                        TIFFEncodeParam encParam = null;
                        ImageEncoder enc;
                        WritableRaster wraster;
                        BufferedReader bur;
                        String linha = null;
                        FileOutputStream fos = null;

                        boolean create = (tam <= (QUANTITY));

                        FileOutputStream fosAlbedo = null;
                        FileOutputStream fosNDVI = null;
                        FileOutputStream fosSAVI = null;
                        FileOutputStream fosTs = null;
                        FileOutputStream fosEmissivity = null;
                        FileOutputStream fosLAI = null;
                        FileOutputStream fosEmissividadeNB = null;
                        FileOutputStream fosLWd = null;
//                        FileOutputStream fosSWd = null;
                        FileOutputStream fosRn = null;
//                        FileOutputStream fosTao24 = null;
                        FileOutputStream fosRg24 = null;
                        FileOutputStream fosUref = null;

                        WritableRaster rasterAlbedo = null;
                        WritableRaster rasterNDVI = null;
                        WritableRaster rasterSAVI = null;
                        WritableRaster rasterTs = null;
                        WritableRaster rasterEmissivity = null;
                        WritableRaster rasterLAI = null;
                        WritableRaster rasterEmissividadeNB = null;
                        WritableRaster rasterLWd = null;
//                        WritableRaster rasterSWd = null;
                        WritableRaster rasterRn = null;
//                        WritableRaster rasterTao24 = null;
                        WritableRaster rasterRg24 = null;
                        WritableRaster rasterUref = null;

                        BandedSampleModel mppsm;
                        DataBufferFloat dataBuffer;

                        if (create) {
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterAlbedo = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterNDVI = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterSAVI = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterTs = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterEmissivity = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterLAI = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterEmissividadeNB = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterLWd = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
//                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
//                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
//                            rasterSWd = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterRn = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

//                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
//                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
//                            rasterTao24 = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterRg24 = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterUref = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            pathTiff = parent + ParameterEnum.albedo.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosAlbedo = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.NDVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosNDVI = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.SAVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosSAVI = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.Ts.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosTs = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.emissivity.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosEmissivity = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.IAF.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosLAI = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.emissividadeNB.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosEmissividadeNB = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.LWnet.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosLWd = new FileOutputStream(pathTiff);

//                            pathTiff = parent + ParameterEnum.SWnet.getFileName();
//                            pathTiff = pathTiff.replace(".dat", ".tif");
//                            fosSWd = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.Rn.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosRn = new FileOutputStream(pathTiff);

//                            pathTiff = parent + ParameterEnum.Tao_24h.getFileName();
//                            pathTiff = pathTiff.replace(".dat", ".tif");
//                            fosTao24 = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.Rg_24h.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosRg24 = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.Uref.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosUref = new FileOutputStream(pathTiff);

                        } else {
                            albedoVet = new float[size];
                            NDVIVet = new float[size];
                            RnVet = new float[size];
                            SAVIVet = new float[size];
                            TsVet = new float[size];
                            emissivityVet = new float[size];
                            IAFVet = new float[size];
                            emissividadeNBVet = new float[size];
                            LWdVet = new float[size];

                            pw = new PrintWriter((parent + name));
                            StringBuilder header = new StringBuilder();
                            for (int i = 0; i < parameters.size(); i++) {
                                ParameterEnum parameterEnum = parameters.get(i);
                                header.append(parameterEnum.toString() + ";");
                            }
                            pw.println(header.toString());
                            pw.close();
                        }

                        float banda4, banda3, banda1, banda2, banda5, banda6, banda7;
                        float bandaRefletida4, bandaRefletida3, bandaRefletida1, bandaRefletida2, bandaRefletida5, bandaRefletida7;

                        double[] valor = null;
                        int idx = 0;
                        int k = 0;

                        float[] dado;

                        int quant = tam / size;
                        int write = 0;


                        //GETTING CONFIGURATION OF TIFF
                        Iterator readersIterator = ImageIO.getImageReadersByFormatName("tif");
                        ImageReader imageReader = (ImageReader) readersIterator.next();
                        ImageInputStream imageInputStream = new FileImageInputStream(tiff);
                        imageReader.setInput(imageInputStream, false, true);
                        IIOMetadata imageMetaData = imageReader.getImageMetadata(k);
                        TIFFDirectory ifd = TIFFDirectory.createFromMetadata(imageMetaData);
                        /* Create a Array of TIFFField*/
                        TIFFField[] allTiffFields = ifd.getTIFFFields();

                        System.out.println("Calculating " + quant);

                        executeEquation.setParameters(albedoVet, NDVIVet, RnVet, SAVIVet, TsVet, emissivityVet, IAFVet, emissividadeNBVet, dado, calibration, Ts, Ts, reflectancia, MaxAllowedError, somaBandas, Rg_24h, emissividadeNB, Ta, S, emissividadeNB, emissivity, julianDay, Z, reflectanciaAtmosfera, P, UR, Ta, Kt, L, K1, K2, S, StefanBoltzman, latitude, Rg_24h, Uref, LWd, LWd);
                        
                        for (int i = 0; i < height; i++) {
                            System.out.println("I:" + i + " H:" + height);
                            for (int j = 0; j < width; j++) {
                                valor = raster.getPixel(j, i, valor);

//                                if (calcule(valor)) {
                                k = 0;
                                variables.put(ParameterEnum.irrad_espectral.getName(), new Variable(ParameterEnum.irrad_espectral.getName(), calibration[k][2]));
                                variables.put(ParameterEnum.coef_calib_b.getName(), new Variable(ParameterEnum.coef_calib_b.getName(), calibration[k][1]));
                                variables.put(ParameterEnum.coef_calib_a.getName(), new Variable(ParameterEnum.coef_calib_a.getName(), calibration[k][0]));
                                variables.put(ParameterEnum.pixel.getName(), new Variable(ParameterEnum.pixel.getName(), valor[k]));
                                banda1 = Utilities.executeMath(ParameterEnum.rad_espectral, equations, variables);
                                variables.put(ParameterEnum.banda1.getName(), new Variable(ParameterEnum.banda1.getName(), banda1));
                                reflectancia = Utilities.executeMath(ParameterEnum.reflectancia, equations, variables);
                                bandaRefletida1 = reflectancia;
                                somaBandas = parameterAlbedo[k] * reflectancia;
                                variables.put(ParameterEnum.bandaRefletida1.getName(), new Variable(ParameterEnum.bandaRefletida1.getName(), bandaRefletida1));
//
                                k = 1;
                                variables.put(ParameterEnum.irrad_espectral.getName(), new Variable(ParameterEnum.irrad_espectral.getName(), calibration[k][2]));
                                variables.put(ParameterEnum.coef_calib_b.getName(), new Variable(ParameterEnum.coef_calib_b.getName(), calibration[k][1]));
                                variables.put(ParameterEnum.coef_calib_a.getName(), new Variable(ParameterEnum.coef_calib_a.getName(), calibration[k][0]));
                                variables.put(ParameterEnum.pixel.getName(), new Variable(ParameterEnum.pixel.getName(), valor[k]));
                                banda2 = Utilities.executeMath(ParameterEnum.rad_espectral, equations, variables);
                                variables.put(ParameterEnum.banda2.getName(), new Variable(ParameterEnum.banda2.getName(), banda2));
                                reflectancia = Utilities.executeMath(ParameterEnum.reflectancia, equations, variables);
                                bandaRefletida2 = reflectancia;
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;
                                variables.put(ParameterEnum.bandaRefletida2.getName(), new Variable(ParameterEnum.bandaRefletida2.getName(), bandaRefletida2));
//
                                k = 2;
                                variables.put(ParameterEnum.irrad_espectral.getName(), new Variable(ParameterEnum.irrad_espectral.getName(), calibration[k][2]));
                                variables.put(ParameterEnum.coef_calib_b.getName(), new Variable(ParameterEnum.coef_calib_b.getName(), calibration[k][1]));
                                variables.put(ParameterEnum.coef_calib_a.getName(), new Variable(ParameterEnum.coef_calib_a.getName(), calibration[k][0]));
                                variables.put(ParameterEnum.pixel.getName(), new Variable(ParameterEnum.pixel.getName(), valor[k]));
                                banda3 = Utilities.executeMath(ParameterEnum.rad_espectral, equations, variables);
                                variables.put(ParameterEnum.banda3.getName(), new Variable(ParameterEnum.banda3.getName(), banda3));
                                reflectancia = Utilities.executeMath(ParameterEnum.reflectancia, equations, variables);
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;
                                bandaRefletida3 = reflectancia;
                                variables.put(ParameterEnum.bandaRefletida3.getName(), new Variable(ParameterEnum.bandaRefletida3.getName(), bandaRefletida3));

                                k = 3;
                                variables.put(ParameterEnum.irrad_espectral.getName(), new Variable(ParameterEnum.irrad_espectral.getName(), calibration[k][2]));
                                variables.put(ParameterEnum.coef_calib_b.getName(), new Variable(ParameterEnum.coef_calib_b.getName(), calibration[k][1]));
                                variables.put(ParameterEnum.coef_calib_a.getName(), new Variable(ParameterEnum.coef_calib_a.getName(), calibration[k][0]));
                                variables.put(ParameterEnum.pixel.getName(), new Variable(ParameterEnum.pixel.getName(), valor[k]));
                                banda4 = Utilities.executeMath(ParameterEnum.rad_espectral, equations, variables);
                                variables.put(ParameterEnum.banda4.getName(), new Variable(ParameterEnum.banda4.getName(), banda4));
                                reflectancia = Utilities.executeMath(ParameterEnum.reflectancia, equations, variables);
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;
                                bandaRefletida4 = reflectancia;
                                variables.put(ParameterEnum.bandaRefletida4.getName(), new Variable(ParameterEnum.bandaRefletida4.getName(), bandaRefletida4));

                                k = 4;
                                variables.put(ParameterEnum.irrad_espectral.getName(), new Variable(ParameterEnum.irrad_espectral.getName(), calibration[k][2]));
                                variables.put(ParameterEnum.coef_calib_b.getName(), new Variable(ParameterEnum.coef_calib_b.getName(), calibration[k][1]));
                                variables.put(ParameterEnum.coef_calib_a.getName(), new Variable(ParameterEnum.coef_calib_a.getName(), calibration[k][0]));
                                variables.put(ParameterEnum.pixel.getName(), new Variable(ParameterEnum.pixel.getName(), valor[k]));
                                banda5 = Utilities.executeMath(ParameterEnum.rad_espectral, equations, variables);
                                variables.put(ParameterEnum.banda5.getName(), new Variable(ParameterEnum.banda5.getName(), banda5));
                                reflectancia = Utilities.executeMath(ParameterEnum.reflectancia, equations, variables);
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;
                                bandaRefletida5 = reflectancia;
                                variables.put(ParameterEnum.bandaRefletida5.getName(), new Variable(ParameterEnum.bandaRefletida5.getName(), bandaRefletida5));

                                k = 6;
                                variables.put(ParameterEnum.irrad_espectral.getName(), new Variable(ParameterEnum.irrad_espectral.getName(), calibration[k][2]));
                                variables.put(ParameterEnum.coef_calib_b.getName(), new Variable(ParameterEnum.coef_calib_b.getName(), calibration[k][1]));
                                variables.put(ParameterEnum.coef_calib_a.getName(), new Variable(ParameterEnum.coef_calib_a.getName(), calibration[k][0]));
                                variables.put(ParameterEnum.pixel.getName(), new Variable(ParameterEnum.pixel.getName(), valor[k]));
                                banda7 = Utilities.executeMath(ParameterEnum.rad_espectral, equations, variables);
                                variables.put(ParameterEnum.banda7.getName(), new Variable(ParameterEnum.banda7.getName(), banda7));
                                reflectancia = Utilities.executeMath(ParameterEnum.reflectancia, equations, variables);
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;
                                bandaRefletida7 = reflectancia;
                                variables.put(ParameterEnum.bandaRefletida7.getName(), new Variable(ParameterEnum.bandaRefletida7.getName(), bandaRefletida7));

                                variables.put(ParameterEnum.sumBandas.getName(), new Variable(ParameterEnum.sumBandas.getName(), somaBandas));

                                albedo = Utilities.executeMath(ParameterEnum.albedo, equations, variables);

                                NDVI = Utilities.executeMath(ParameterEnum.NDVI, equations, variables);

                                SAVI = Utilities.executeMath(ParameterEnum.SAVI, equations, variables);

                                if (SAVI <= 0.1f) {
                                    IAF = 0.0f;
                                } else if (SAVI >= 0.687f) {
                                    IAF = 6.0f;
                                } else {
                                    IAF = Utilities.executeMath(ParameterEnum.IAF, equations, variables);
                                }
                                variables.put(ParameterEnum.IAF.getName(), new Variable(ParameterEnum.IAF.getName(), IAF));
//                                    System.out.println(variables.get(ParameterEnum.IAF.getName()));

                                if (IAF >= 3) {
                                    emissividadeNB = 0.98f;
                                    emissivity = 0.98f;
                                } else if (NDVI <= 0) {
                                    emissividadeNB = 0.99f;
                                    emissivity = 0.985f;
                                } else {
                                    emissividadeNB = Utilities.executeMath(ParameterEnum.emissividadeNB, equations, variables);
                                    emissivity = Utilities.executeMath(ParameterEnum.emissivity, equations, variables);
                                }
                                variables.put(ParameterEnum.emissividadeNB.getName(), new Variable(ParameterEnum.emissividadeNB.getName(), emissividadeNB));
                                variables.put(ParameterEnum.emissivity.getName(), new Variable(ParameterEnum.emissivity.getName(), emissivity));

                                k = 5;
                                variables.put(ParameterEnum.irrad_espectral.getName(), new Variable(ParameterEnum.irrad_espectral.getName(), calibration[k][2]));
                                variables.put(ParameterEnum.coef_calib_b.getName(), new Variable(ParameterEnum.coef_calib_b.getName(), calibration[k][1]));
                                variables.put(ParameterEnum.coef_calib_a.getName(), new Variable(ParameterEnum.coef_calib_a.getName(), calibration[k][0]));
                                variables.put(ParameterEnum.pixel.getName(), new Variable(ParameterEnum.pixel.getName(), valor[k]));
                                banda6 = Utilities.executeMath(ParameterEnum.rad_espectral, equations, variables);
                                variables.put(ParameterEnum.banda6.getName(), new Variable(ParameterEnum.banda6.getName(), banda6));

//                                    for (Variable variable : variables.values()) {
//                                        System.out.println("Var:"+variable.getName());
//                                    }

                                Ts = Utilities.executeMath(ParameterEnum.Ts, equations, variables);



                                LWd = Utilities.executeMath(ParameterEnum.LWd, equations, variables);
//                                    SWdVet[idx] = S * cosZ * dr * transmissividade;
//
////                                    LWdVet[idx] = 391.5f;
////                                    SWdVet[idx] = 736.6f;
////                                    if (idx == 653) {
////                                        System.out.println("albedoVet2:" + albedoVet[idx]);
////                                    }
////                                    albedoVet[idx] = 0.172f;
                                Rn = Utilities.executeMath(ParameterEnum.Rn, equations, variables);
////                                    if (idx == 653) {
////                                        System.out.println("LWdAtmosfera:" + LWdAtmosfera);
////                                        System.out.println("Sfetan:" + StefanBoltzman);
////                                        System.out.println("albedoVet:" + albedoVet[idx]);
////                                        System.out.println("NDVIVet:" + NDVIVet[idx]);
////                                        System.out.println("SAVIVet:" + SAVIVet[idx]);
////                                        System.out.println("IAFVet:" + IAFVet[idx]);
////                                        System.out.println("emissividadeNBVet:" + emissividadeNBVet[idx]);
////                                        System.out.println("emissivityVet:" + emissivityVet[idx]);
////                                        System.out.println("TsVet:" + TsVet[idx]);
////                                        System.out.println("LWdVet:" + LWdVet[idx]);
////                                        System.out.println("SWdVet:" + SWdVet[idx]);
////                                        System.out.println("RnVet:" + RnVet[idx]);
////                                        System.exit(1);
////                                    }

                                if (create) {
                                    dado = new float[]{albedo};
                                    rasterAlbedo.setPixel(j, i, dado);

                                    dado = new float[]{NDVI};
                                    rasterNDVI.setPixel(j, i, dado);

                                    dado = new float[]{SAVI};
                                    rasterSAVI.setPixel(j, i, dado);

                                    dado = new float[]{IAF};
                                    rasterLAI.setPixel(j, i, dado);

                                    dado = new float[]{emissivity};
                                    rasterEmissivity.setPixel(j, i, dado);

                                    dado = new float[]{emissividadeNB};
                                    rasterEmissividadeNB.setPixel(j, i, dado);

                                    dado = new float[]{Ts};
                                    rasterTs.setPixel(j, i, dado);

                                    dado = new float[]{LWd};
                                    rasterLWd.setPixel(j, i, dado);

//                                    dado = new float[]{SWd};
//                                    rasterSWd.setPixel(j, i, dado);

                                    dado = new float[]{Rn};
                                    rasterRn.setPixel(j, i, dado);

//                                    dado = new float[]{transmissividade24h};
//                                    rasterTao24.setPixel(j, i, dado);

                                    dado = new float[]{Rg_24h};
                                    rasterRg24.setPixel(j, i, dado);

                                    dado = new float[]{Uref};
                                    rasterUref.setPixel(j, i, dado);
                                } else {
////                                }
                                    albedoVet[idx] = albedo;
                                    NDVIVet[idx] = NDVI;
                                    RnVet[idx] = Rn;
                                    SAVIVet[idx] = SAVI;
                                    TsVet[idx] = Ts;
                                    emissivityVet[idx] = emissivity;
                                    IAFVet[idx] = IAF;
                                    emissividadeNBVet[idx] = emissividadeNB;
                                    LWdVet[idx] = LWd;

                                    idx++;

                                    if (size < tam && idx >= size) {
                                        write++;
                                        System.out.println("Writing " + write + " from " + quant);
                                        pw = new PrintWriter(new FileOutputStream(parent + name, true));
                                        StringBuilder line = new StringBuilder();
                                        int lines = 0;
                                        long tempo = System.currentTimeMillis();
                                        for (int l = 0; l < albedoVet.length; l++) {
                                            line = new StringBuilder();
                                            line.append(albedoVet[j] + ";");
                                            line.append(NDVIVet[j] + ";");
                                            line.append(SAVIVet[j] + ";");
                                            line.append(TsVet[j] + ";");
                                            line.append(emissivityVet[j] + ";");
                                            line.append(IAFVet[j] + ";");
                                            line.append(emissividadeNBVet[j] + ";");
                                            line.append(LWdVet[j] + ";");
//                                            line.append(SWd + ";");
                                            line.append(RnVet[j] + ";");
//                                            line.append(transmissividade24h + ";");
                                            line.append(Rg_24h + ";");
                                            line.append(Uref + ";");
//                                        line.append("\n");
//                                        lines++;
//                                        if (lines >= QUANTITY_LINES) {
                                            pw.println(line.toString());
//                                            line = new StringBuilder();
//                                            lines = 0;
//                                        }
                                        }
//                                    if (lines > 0) {
//                                        pw.println(line.toString());
//                                        line = new StringBuilder();
//                                        lines = 0;
//                                    }
                                        pw.close();

                                        System.out.println("Tempo:" + (System.currentTimeMillis() - tempo));
                                        idx = 0;
                                        albedoVet = new float[size];
                                        NDVIVet = new float[size];
                                        SAVIVet = new float[size];
                                        TsVet = new float[size];
                                        emissivityVet = new float[size];
                                        IAFVet = new float[size];
                                        emissividadeNBVet = new float[size];
                                        LWdVet = new float[size];
                                        RnVet = new float[size];
                                        System.out.println("End Writing " + write);
                                    }
                                }
                            }
                        }

                        if (idx != 0 && !create) {
                            write++;
                            System.out.println("Writing " + write + " from " + quant);
                            pw = new PrintWriter(new FileOutputStream(parent + name, true));
                            StringBuilder line = new StringBuilder();
                            int lines = 0;
                            for (int j = 0; j < idx; j++) {
                                line = new StringBuilder();
                                line.append(albedoVet[j] + ";");
                                line.append(NDVIVet[j] + ";");
                                line.append(SAVIVet[j] + ";");
                                line.append(TsVet[j] + ";");
                                line.append(emissivityVet[j] + ";");
                                line.append(IAFVet[j] + ";");
                                line.append(emissividadeNBVet[j] + ";");
                                line.append(LWdVet[j] + ";");
//                                line.append(SWd + ";");
                                line.append(RnVet[j] + ";");
//                                line.append(transmissividade24h + ";");
                                line.append(Rg_24h + ";");
                                line.append(Uref + ";");

//                                if (lines >= QUANTITY_LINES) {
                                pw.println(line.toString());
//                                    
//                                    lines = 0;
//                                }
                            }
//                            if (lines > 0) {
//                                pw.println(line.toString());
//                                line = new StringBuilder();
//                                lines = 0;
//                            }
                            pw.close();
                            System.out.println("End Writing");
                        }

                        albedoVet = null;
                        NDVIVet = null;
                        SAVIVet = null;
                        TsVet = null;
                        emissivityVet = null;
                        IAFVet = null;
                        emissividadeNBVet = null;
                        LWdVet = null;
                        RnVet = null;

                        System.out.println("Creating TIFFs");

                        if (create) {
                            enc = ImageCodec.createImageEncoder("tiff", fosAlbedo, encParam);
                            enc.encode(rasterAlbedo, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosNDVI, encParam);
                            enc.encode(rasterNDVI, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosSAVI, encParam);
                            enc.encode(rasterSAVI, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosTs, encParam);
//                            dado = new float[1];
//                            System.out.println("Raster:" + Arrays.toString(rasterTs.getPixel(653, 0, dado)));
                            enc.encode(rasterTs, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosEmissivity, encParam);
                            enc.encode(rasterEmissivity, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosLAI, encParam);
                            enc.encode(rasterLAI, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosEmissividadeNB, encParam);
                            enc.encode(rasterEmissividadeNB, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosLWd, encParam);
                            enc.encode(rasterLWd, model);
//                            enc = ImageCodec.createImageEncoder("tiff", fosSWd, encParam);
//                            enc.encode(rasterSWd, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosRn, encParam);
                            enc.encode(rasterRn, model);

//                            enc = ImageCodec.createImageEncoder("tiff", fosTao24, encParam);
//                            enc.encode(rasterTao24, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosRg24, encParam);
                            enc.encode(rasterRg24, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosUref, encParam);
                            enc.encode(rasterUref, model);

                            fosAlbedo.close();
                            fosNDVI.close();
                            fosSAVI.close();
                            fosTs.close();
                            fosEmissivity.close();
                            fosLAI.close();
                            fosEmissividadeNB.close();
                            fosLWd.close();
//                            fosSWd.close();
                            fosRn.close();
//                            fosTao24.close();
                            fosRg24.close();
                            fosUref.close();

                            pathTiff = parent + ParameterEnum.albedo.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterAlbedo);

                            pathTiff = parent + ParameterEnum.NDVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterNDVI);

                            pathTiff = parent + ParameterEnum.SAVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterSAVI);

                            pathTiff = parent + ParameterEnum.Ts.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterTs);

                            pathTiff = parent + ParameterEnum.emissivity.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterEmissivity);

                            pathTiff = parent + ParameterEnum.IAF.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterLAI);

                            pathTiff = parent + ParameterEnum.emissividadeNB.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterEmissividadeNB);

                            pathTiff = parent + ParameterEnum.LWnet.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterLWd);

//                            pathTiff = parent + ParameterEnum.SWnet.getFileName();
//                            pathTiff = pathTiff.replace(".dat", ".tif");
//                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterSWd);

                            pathTiff = parent + ParameterEnum.Rn.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterRn);

//                            pathTiff = parent + ParameterEnum.Tao_24h.getFileName();
//                            pathTiff = pathTiff.replace(".dat", ".tif");
//                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterTao24);

                            pathTiff = parent + ParameterEnum.Rg_24h.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterRg24);

                            pathTiff = parent + ParameterEnum.Uref.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterUref);

                        } else {

                            ParameterEnum parameterEnum;

                            for (int m = 0; m < parameters.size(); m++) {
                                parameterEnum = parameters.get(m);
                                System.out.println("Creating TIFF: " + parameterEnum.toString());

                                pathTiff = parent + parameterEnum.getFileName();
                                pathTiff = pathTiff.replace(".dat", ".tif");
                                fos = new FileOutputStream(pathTiff);

                                mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                                dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());

                                wraster = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                                bur = new BufferedReader(new FileReader(parent + name));
                                linha = bur.readLine();
                                linha = bur.readLine();
                                String[] vet = linha.split(";", -2);

                                for (int i = 0; i < height; i++) {
                                    for (int j = 0; j < width; j++) {
                                        dado = new float[]{Float.parseFloat(vet[m])};
                                        wraster.setPixel(j, i, dado);
                                        linha = bur.readLine();
                                        if (linha != null) {
                                            vet = linha.split(";", -2);
                                        }
                                    }
                                }

                                enc = ImageCodec.createImageEncoder("tiff", fos, encParam);
                                enc.encode(wraster, model);
                                fos.close();
                                bur.close();


                                Utilities.saveTiff(pathTiff, imageReader, allTiffFields, wraster);
                            }
                        }


                        ParameterEnum parameterEnum;
                        for (int m = 0; m < parameters.size(); m++) {
                            parameterEnum = parameters.get(m);
                            pathTiff = parent + parameterEnum.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            ret.add(new DataFile(parameterEnum, new File(pathTiff)));
                        }

                        System.out.println("End");
                    } else {
                        throw new TiffErrorBandsException();
                    }

                    return ret;
                } catch (IOException ex) {
                    Logger.getLogger(LandSat.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        s.close();
                    } catch (IOException ex) {
                        Logger.getLogger(LandSat.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return null;
            } else {
                throw new CalibrationException();
            }

        } else {
            throw new TiffNotFoundException();
        }
    }

    public List<DataFile> lookPixelHotColdLandSat5(String pathToOriginalTiff, int julianDay, float Z, float P, float UR, float Ta, float Uref) {
        float[][] calibration = new float[][]{{-1.52f, 193.0f, 1957.0f},
            {-2.84f, 365.0f, 1826.0f},
            {-1.17f, 264.0f, 1554.0f},
            {-1.51f, 221.0f, 1036.0f},
            {-0.37f, 30.2f, 215.0f},
            {1.2378f, 15.303f, 1.0f},
            {-0.15f, 16.5f, 80.67f}};

        float[] parameterAlbedo = new float[]{0.293f, 0.274f, 0.233f, 0.157f, 0.033f, 0.0f, 0.011f};
        float reflectancaAtmosfera = 0.03f;
        float Kt = 1.0f;
        float L = 0.1f;
        float K1 = 607.76f;
        float K2 = 1260.56f;
        float S = 1367.0f;
        float StefanBoltzman = (float) (5.67 * Math.pow(10, -8));

        List<DataFile> ret = lookPixelHotCold(pathToOriginalTiff, calibration, parameterAlbedo, julianDay, Z, reflectancaAtmosfera, P, UR, Ta, Kt, L, K1, K2, S, StefanBoltzman, Uref);
        return ret;
    }

    public List<DataFile> lookPixelHotCold(String pathToOriginalTiff, float[][] calibration, float[] parameterAlbedo, int julianDay, float Z, float reflectancaAtmosfera, float P, float UR, float Ta, float Kt, float L, float K1, float K2, float S, float StefanBoltzman, float Uref) {
        File tiff = new File(pathToOriginalTiff);
        if (tiff.exists() && tiff.getName().endsWith(".tif")) {
            if (calibration != null && calibration.length == 7 && calibration[0].length == 3) {
                SeekableStream s = null;
                try {

                    List<DataFile> ret = new ArrayList<>();
                    System.out.println("Looking Pixel Hot and Cold:" + tiff.getName());
                    s = new FileSeekableStream(tiff);
                    TIFFDecodeParam param = null;
                    ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
                    // Which of the multiple images in the TIFF file do we want to load
                    // 0 refers to the first, 1 to the second and so on.
                    int bands;

                    Raster raster = dec.decodeAsRaster(0);
                    bands = raster.getNumBands();
                    int width = raster.getWidth();
                    int height = raster.getHeight();
                    int tam = width * height;
                    System.out.println("W:" + raster.getWidth() + " H:" + raster.getHeight());
                    System.out.println("Size:" + tam);

                    if (bands == 7) {

                        String parent = tiff.getParent() + "/OutputParameters/";
                        File dir = new File(parent);
                        dir.mkdirs();

                        float dr = (float) (1.0f + 0.033f * Math.cos(julianDay * 2 * Math.PI / 365.0f));

                        float cosZ = (float) Math.cos(((90.0f - Z) * Math.PI) / 180.0f);
//                        float transmissividade = (float) (0.75f + 2 * Math.pow(10, -5) * altura);
                        float ea = (float) ((0.61078f * Math.exp(17.269f * Ta / (237.3f + Ta))) * UR / 100.f);
                        float W = 0.14f * ea * P + 2.1f;
                        float transmissividade = (float) (0.35f + 0.627f * Math.exp((-0.00146f * P / (Kt * cosZ)) - 0.075f * Math.pow((W / cosZ), 0.4f)));
                        float emissivityAtmosfera = (float) (0.625f * Math.pow((1000.0f * ea / (Ta + T0)), 0.131f));
//                        EXP((-0,00146*P/(KT*COSZ)-0,075*(W/COSZ)^0,4))

                        float calibracao;
                        float reflectancia;

                        float albedoVet = 0.0f;
                        float NDVIVet = 0.0f;
                        float RnVet = 0.0f;
                        float SAVIVet = 0.0f;
                        float TsVet = 0.0f;
                        float emissivityVet = 0.0f;
                        float IAFVet = 0.0f;
                        float emissividadeNBVet = 0.0f;
                        float LWdVet = 0.0f;
                        float SWdVet = 0.0f;

                        float banda4, banda3;

                        double[] valor = null;
                        int idx = 0;
                        int k = 0;

                        System.out.println("Looking ");
                        float LWdAtmosfera = (float) (emissivityAtmosfera * StefanBoltzman * (Math.pow(Ta + T0, 4)));

                        int xHot = 0, yHot = 0, xCold = 0, yCold = 0;
                        float tMax, tMin;
                        float mSaviMax, mSaviMin;
                        float RnHot = 0, GHot = 0;
                        float SAVI_hot = 0;

                        float tmax = 0;
                        tMax = 0;
                        mSaviMax = 0;

                        mSaviMin = Float.MAX_VALUE;
                        tMin = Float.MAX_VALUE;

                        float mSAVI;

                        SWdVet = (S * cosZ * cosZ) / (1.085f * cosZ + 10.0f * ea * (2.7f + cosZ) * 0.001f + 0.2f);

                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < width; j++) {
                                valor = raster.getPixel(j, i, valor);

                                if (calcule(valor)) {
                                    k = 0;
                                    calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                    reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                    albedoVet = parameterAlbedo[k] * reflectancia;

                                    k = 1;
                                    calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                    reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                    albedoVet = albedoVet + parameterAlbedo[k] * reflectancia;

                                    k = 2;

                                    calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                    reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                    albedoVet = albedoVet + parameterAlbedo[k] * reflectancia;

                                    banda3 = reflectancia;

                                    k = 3;

                                    calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                    reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                    albedoVet = albedoVet + parameterAlbedo[k] * reflectancia;

                                    banda4 = reflectancia;

                                    k = 4;
                                    calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                    reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                    albedoVet = albedoVet + parameterAlbedo[k] * reflectancia;

                                    k = 6;
                                    calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                    reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                    albedoVet = albedoVet + parameterAlbedo[k] * reflectancia;

                                    albedoVet = (albedoVet - reflectancaAtmosfera) / (transmissividade * transmissividade);

                                    NDVIVet = (banda4 - banda3) / (banda4 + banda3);

                                    SAVIVet = ((1.0f + L) * (banda4 - banda3)) / (L + banda4 + banda3);

                                    if (SAVIVet <= 0.1f) {
                                        IAFVet = 0.0f;
                                    } else if (SAVIVet >= 0.687f) {
                                        IAFVet = 6.0f;
                                    } else {
                                        IAFVet = (float) (-Math.log((0.69f - SAVIVet) / 0.59f) / 0.91f);
                                    }

                                    if (IAFVet >= 3) {
                                        emissividadeNBVet = 0.98f;
                                        emissivityVet = 0.98f;
                                    } else if (NDVIVet <= 0) {
                                        emissividadeNBVet = 0.99f;
                                        emissivityVet = 0.985f;
                                    } else {
                                        emissividadeNBVet = 0.97f + 0.0033f * IAFVet;
                                        emissivityVet = 0.95f + 0.011f * IAFVet;
                                    }

                                    k = 5;
                                    calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.0f) * valor[k]);
                                    TsVet = (float) (K2 / (Math.log((emissividadeNBVet * K1 / calibracao) + 1.0f)));

                                    LWdVet = (float) (emissivityVet * StefanBoltzman * (Math.pow(TsVet, 4)));
//                                    SWdVet = S * cosZ * dr * transmissividade;

                                    RnVet = (float) (((1.0f - albedoVet) * SWdVet) + (emissivityVet * (LWdAtmosfera) - LWdVet));
                                    mSAVI = (float) ((0.5f) * ((2.0f * banda4 + 1) - Math.sqrt((Math.pow((2 * banda4 + 1), 2) - 8 * (banda4 - banda3)))));

//                                    if (j == 4059) {
//                                        if (i == 4668) {
//                                            System.out.println("SAVIVet:" + SAVIVet);
//                                            System.out.println("NDVIVet:" + NDVIVet);
//                                            System.out.println("TsVet:" + TsVet);
//                                            System.out.println("IAFVet:" + IAFVet);
//                                            System.out.println("albedoVet:" + albedoVet);
//                                            System.out.println("emissivityVet:" + emissivityVet);
//                                            System.out.println("RnVet:" + RnVet);
//                                            System.out.println("emissividadeNBVet:" + emissividadeNBVet);
//                                            System.out.println("mSAVI:" + mSAVI);
//                                        }
//                                    }
//
//                                    if (j == 5823) {
//                                        if (i == 10658) {
//
//                                            System.out.println("SAVIVet2:" + SAVIVet);
//                                            System.out.println("NDVIVet2:" + NDVIVet);
//                                            System.out.println("TsVet2:" + TsVet);
//                                            System.out.println("IAFVet2:" + IAFVet);
//                                            System.out.println("albedoVet2:" + albedoVet);
//                                            System.out.println("emissivityVet2:" + emissivityVet);
//                                            System.out.println("RnVet2:" + RnVet);
//                                            System.out.println("emissividadeNBVet2:" + emissividadeNBVet);
//                                            System.out.println("mSAVI2:" + mSAVI);
//                                        }
//                                    }

                                    if (tmax < TsVet) {
                                        tmax = TsVet;
                                    }

                                    if (mSAVI > mSaviMax) {
                                        if (TsVet < tMin) {
                                            if (!hasDiferenceTsAround(raster, j, i, calibration, cosZ, dr, L, K1, K2, TsVet)) {
                                                if (!hasDiferenceMSAVIAround(raster, j, i, calibration, cosZ, dr, mSAVI)) {
                                                    xCold = j;
                                                    yCold = i;
                                                    tMin = TsVet;
                                                    mSaviMax = mSAVI;
                                                }
                                            }
                                        }
                                    } else if (mSAVI < mSaviMin) {
                                        if (TsVet > tMax) {
                                            if (!hasDiferenceTsAround(raster, j, i, calibration, cosZ, dr, L, K1, K2, TsVet)) {
                                                if (!hasDiferenceMSAVIAround(raster, j, i, calibration, cosZ, dr, mSAVI)) {
                                                    xHot = j;
                                                    yHot = i;
                                                    tMax = TsVet;
                                                    mSaviMin = mSAVI;
                                                    RnHot = RnVet;
                                                    SAVI_hot = SAVIVet;
//                                            System.out.println("LWdVet:" + LWdVet);
                                                    GHot = (float) (RnHot * (((TsVet - T0) / albedoVet) * (0.0038f * albedoVet + 0.0074 * albedoVet * albedoVet) * (1.0f - 0.98f * NDVIVet * NDVIVet * NDVIVet * NDVIVet)));
                                                }
                                            }
                                        }
                                    }

                                }

                                idx++;

                            }
                        }

//                        System.out.println("SWdVet:" + SWdVet);
//                        System.out.println("LWdAtmosfera:" + LWdAtmosfera);
//                        System.out.println("XHot:" + xHot);
//                        System.out.println("YHot:" + yHot);
//                        System.out.println("XCold:" + xCold);
//                        System.out.println("YCold:" + yCold);
//                        System.out.println("Rn:" + RnHot);
//                        System.out.println("G:" + GHot);
////                        System.out.println("Savi:" + SAVI_hot);
////                        System.out.println("tmax:" + tmax);
//                        System.out.println("THot:" + tMax);
//                        System.out.println("TCold:" + tMin);
////                        System.out.println("mSaviMin:" + mSaviMin);

                        float[] coef = new float[2];

                        Utilities.calculaAB(coef, RnHot, GHot, Uref, SAVI_hot, tMax, tMin);

                        System.out.println("A:" + coef[0]);
                        System.out.println("B:" + coef[1]);

                        String name;
                        PrintWriter pw;

                        name = parent + ParameterEnum.A.getFileName();
                        pw = new PrintWriter(name);
                        pw.println(coef[0]);
                        pw.close();
                        ret.add(new DataFile(ParameterEnum.A, new File(name)));

                        name = parent + ParameterEnum.B.getFileName();
                        pw = new PrintWriter(name);
                        pw.println(coef[1]);
                        pw.close();
                        ret.add(new DataFile(ParameterEnum.B, new File(name)));

                        name = parent + ParameterEnum.COORDENATES.getFileName();
                        pw = new PrintWriter(name);
                        pw.println("X Hot:" + xHot);
                        pw.println("Y Hot:" + yHot);
                        pw.println("X Cold:" + xCold);
                        pw.println("Y Cold:" + yCold);
                        pw.close();
                        ret.add(new DataFile(ParameterEnum.COORDENATES, new File(name)));

                        return ret;
                    } else {
                        throw new TiffErrorBandsException();
                    }

                } catch (IOException ex) {
                    Logger.getLogger(LandSat.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        s.close();
                    } catch (IOException ex) {
                        Logger.getLogger(LandSat.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } else {
                throw new CalibrationException();
            }

        } else {
            throw new TiffNotFoundException();
        }
        return null;
    }

    private boolean hasDiferenceTsAround(Raster raster, int i, int j, float calibration[][], float cosZ, float dr, float L, float K1, float K2, float Ts) {
        double[] dado = null;
        if (j > 0 && i > 0 && j < (raster.getHeight() - 1) && i < (raster.getWidth() - 1)) {
            dado = raster.getPixel(i, j - 1, dado);
            if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                dado = raster.getPixel(i, j + 1, dado);
                if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                    dado = raster.getPixel(i - 1, j - 1, dado);
                    if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                        dado = raster.getPixel(i - 1, j + 1, dado);
                        if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                            dado = raster.getPixel(i + 1, j - 1, dado);
                            if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                                dado = raster.getPixel(i + 1, j + 1, dado);
                                if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                                    dado = raster.getPixel(i - 1, j, dado);
                                    if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                                        dado = raster.getPixel(i + 1, j, dado);
                                        if (!hasDiferenceTs(calibration, cosZ, dr, L, K1, K2, dado, Ts)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean hasDiferenceTs(float calibration[][], float cosZ, float dr, float L, float K1, float K2, double[] pixels, float Ts) {

        float calibracao;
        float reflectancia;

        float SAVIVet = 0.0f;
        float TsVet = 0.0f;
        float IAFVet = 0.0f;
        float emissividadeNBVet = 0.0f;
        float banda3 = 0.0f;
        float banda4 = 0.0f;

        int k = 0;

        k = 2;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda3 = reflectancia;

        k = 3;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda4 = reflectancia;

        SAVIVet = ((1.0f + L) * (banda4 - banda3)) / (L + banda4 + banda3);

        if (SAVIVet <= 0.02f) {
            IAFVet = 0.0f;
        } else if (SAVIVet >= 0.687f) {
            IAFVet = 6.0f;
        } else {
            IAFVet = (float) (-Math.log((0.69f - SAVIVet) / 0.59f) / 0.91f);
        }

        if (IAFVet > 3) {
            emissividadeNBVet = 0.98f;
        } else if (IAFVet <= 0) {
            emissividadeNBVet = 0.99f;
        } else {
            emissividadeNBVet = 0.97f + 0.0033f * IAFVet;
        }

        k = 5;
        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.0f) * pixels[k]);
        TsVet = (float) (K2 / (Math.log((emissividadeNBVet * K1 / calibracao) + 1.0f)));

        float dif = Math.abs(Ts - TsVet);

        if ((dif / Ts) <= 0.05f) {
            return false;
        }

        return true;
    }

    private boolean hasDiferenceMSAVIAround(Raster raster, int i, int j, float calibration[][], float cosZ, float dr, float mSAVI) {
        double[] dado = null;
        dado = raster.getPixel(i, j - 1, dado);
        if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
            dado = raster.getPixel(i, j + 1, dado);
            if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
                dado = raster.getPixel(i - 1, j - 1, dado);
                if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
                    dado = raster.getPixel(i - 1, j + 1, dado);
                    if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
                        dado = raster.getPixel(i + 1, j - 1, dado);
                        if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
                            dado = raster.getPixel(i + 1, j + 1, dado);
                            if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
                                dado = raster.getPixel(i - 1, j, dado);
                                if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
                                    dado = raster.getPixel(i + 1, j, dado);
                                    if (!hasDiferenceMSAVI(calibration, cosZ, dr, dado, mSAVI)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean hasDiferenceMSAVI(float calibration[][], float cosZ, float dr, double[] pixels, float mSAVI) {

        int k = 2;

        float calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        float reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        float banda3 = reflectancia;

        k = 3;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        float banda4 = reflectancia;

        float mSAVIVet = (float) ((0.5f) * ((2.0f * banda4 + 1) - Math.sqrt((Math.pow((2 * banda4 + 1), 2) - 8 * (banda4 - banda3)))));

        float dif = Math.abs(mSAVI - mSAVIVet);

        if ((dif / mSAVI) <= 0.1f) {
            return false;
        }

        return true;
    }

    private boolean hasDiferenceSAVIAround(Raster raster, int i, int j, float calibration[][], float cosZ, float dr, float L, float SAVI) {
        double[] dado = null;
        dado = raster.getPixel(i, j - 1, dado);
        if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
            dado = raster.getPixel(i, j + 1, dado);
            if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
                dado = raster.getPixel(i - 1, j - 1, dado);
                if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
                    dado = raster.getPixel(i - 1, j + 1, dado);
                    if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
                        dado = raster.getPixel(i + 1, j - 1, dado);
                        if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
                            dado = raster.getPixel(i + 1, j + 1, dado);
                            if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
                                dado = raster.getPixel(i - 1, j, dado);
                                if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
                                    dado = raster.getPixel(i + 1, j, dado);
                                    if (!hasDiferenceSAVI(calibration, cosZ, dr, L, dado, SAVI)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean hasDiferenceSAVI(float calibration[][], float cosZ, float dr, float L, double[] pixels, float SAVI) {

        float calibracao;
        float reflectancia;

        float SAVIVet = 0.0f;
        float banda3 = 0.0f;
        float banda4 = 0.0f;

        int k = 0;

        k = 2;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda3 = reflectancia;

        k = 3;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda4 = reflectancia;

        SAVIVet = ((1.0f + L) * (banda4 - banda3)) / (L + banda4 + banda3);

        float dif = Math.abs(SAVI - SAVIVet);

        if ((dif / SAVI) <= 0.05f) {
            return false;
        }

        return true;
    }

    private boolean hasDiferenceNVDIAround(Raster raster, int i, int j, float calibration[][], float cosZ, float dr, float NVDI) {
        double[] dado = null;
        dado = raster.getPixel(i, j - 1, dado);
        if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
            dado = raster.getPixel(i, j + 1, dado);
            if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
                dado = raster.getPixel(i - 1, j - 1, dado);
                if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
                    dado = raster.getPixel(i - 1, j + 1, dado);
                    if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
                        dado = raster.getPixel(i + 1, j - 1, dado);
                        if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
                            dado = raster.getPixel(i + 1, j + 1, dado);
                            if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
                                dado = raster.getPixel(i - 1, j, dado);
                                if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
                                    dado = raster.getPixel(i + 1, j, dado);
                                    if (!hasDiferenceNVDI(calibration, cosZ, dr, dado, NVDI)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean hasDiferenceNVDI(float calibration[][], float cosZ, float dr, double[] pixels, float NDVI) {

        float calibracao;
        float reflectancia;

        float NDVIVet = 0.0f;
        float banda3 = 0.0f;
        float banda4 = 0.0f;

        int k = 0;

        k = 2;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda3 = reflectancia;

        k = 3;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda4 = reflectancia;

        NDVIVet = (banda4 - banda3) / (banda4 + banda3);

        float dif = Math.abs(NDVI - NDVIVet);

        if ((dif / NDVI) <= 0.05f) {
            return false;
        }

        return true;
    }

    private boolean hasDiferenceIAFAround(Raster raster, int i, int j, float calibration[][], float cosZ, float dr, float L, float IAF) {
        double[] dado = null;
        dado = raster.getPixel(i, j - 1, dado);
        if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
            dado = raster.getPixel(i, j + 1, dado);
            if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
                dado = raster.getPixel(i - 1, j - 1, dado);
                if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
                    dado = raster.getPixel(i - 1, j + 1, dado);
                    if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
                        dado = raster.getPixel(i + 1, j - 1, dado);
                        if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
                            dado = raster.getPixel(i + 1, j + 1, dado);
                            if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
                                dado = raster.getPixel(i - 1, j, dado);
                                if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
                                    dado = raster.getPixel(i + 1, j, dado);
                                    if (!hasDiferenceIAF(calibration, cosZ, dr, L, dado, IAF)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean hasDiferenceIAF(float calibration[][], float cosZ, float dr, float L, double[] pixels, float IAF) {

        float calibracao;
        float reflectancia;

        float SAVIVet = 0.0f;
        float IAFVet = 0.0f;
        float banda3 = 0.0f;
        float banda4 = 0.0f;

        int k = 0;

        k = 2;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda3 = reflectancia;

        k = 3;

        calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * pixels[k]);
        reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));

        banda4 = reflectancia;

        SAVIVet = ((1.0f + L) * (banda4 - banda3)) / (L + banda4 + banda3);

        if (SAVIVet <= 0.02f) {
            IAFVet = 0.0f;
        } else if (SAVIVet >= 0.687f) {
            IAFVet = 6.0f;
        } else {
            IAFVet = (float) (-Math.log((0.69f - SAVIVet) / 0.59f) / 0.91f);
        }

        float dif = Math.abs(IAF - IAFVet);

        if ((dif / IAF) <= 0.05f) {
            return false;
        }

        return true;
    }

    public List<DataFile> preprocessingLandSat5(String path, int julianDay, float Z, float P, float UR, float Ta, float latitude, float Rg_24h, float Uref) {

        float[][] calibration = new float[][]{{-1.52f, 193.0f, 1957.0f},
            {-2.84f, 365.0f, 1826.0f},
            {-1.17f, 264.0f, 1554.0f},
            {-1.51f, 221.0f, 1036.0f},
            {-0.37f, 30.2f, 215.0f},
            {1.2378f, 15.303f, 1.0f},
            {-0.15f, 16.5f, 80.67f}};

        float[] parameterAlbedo = new float[]{0.293f, 0.274f, 0.233f, 0.157f, 0.033f, 0.0f, 0.011f};
        float reflectancaAtmosfera = 0.03f;
        float Kt = 1.0f;
        float L = 0.1f;
        float K1 = 607.76f;
        float K2 = 1260.56f;
        float S = 1367.0f;
        float StefanBoltzman = (float) (5.67 * Math.pow(10, -8));

        List<DataFile> ret = preprocessing(path, calibration, parameterAlbedo, julianDay, Z, reflectancaAtmosfera, P, UR, Ta, Kt, L, K1, K2, S, StefanBoltzman, latitude, Rg_24h, Uref);

        return ret;
    }

    private boolean calcule(double[] valor) {
        for (int i = 0; i < valor.length; i++) {
            if (valor[i] == 0.0f) {
                return false;
            }
        }
        return true;
    }
}
