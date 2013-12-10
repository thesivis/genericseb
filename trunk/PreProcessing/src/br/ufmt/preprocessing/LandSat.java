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
            while (line != null) {
//                System.out.println(line);
                line = line.replaceAll("[ ]+", "");
                vet = line.split("=");
                variables.add(vet[0]);
                try {
                    ex.evaluateExpr(line, variables);
                    equations.put(ParameterEnum.valueOf(vet[0]), line);
                } catch (IllegalArgumentException e) {
//                    System.out.println("Equation is wrong: " + line);
                    System.out.println(e.getMessage());
                }
                line = bur.readLine();
            }
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

                        GenericLexerSEB lexer = new GenericLexerSEB();
                        Structure structure;
                        String equation;
                        List<Variable> variables = Utilities.getVariable();
                        variables.add(new Variable("julianDay", julianDay));
                        variables.add(new Variable("Z", Z));
                        variables.add(new Variable("reflectanciaAtmosfera", reflectanciaAtmosfera));
                        variables.add(new Variable("P", P));
                        variables.add(new Variable("UR", UR));
                        variables.add(new Variable("Ta", Ta));
                        variables.add(new Variable("Kt", Kt));
                        variables.add(new Variable("L", L));
                        variables.add(new Variable("K1", K1));
                        variables.add(new Variable("K2", K2));
                        variables.add(new Variable("S", s));
                        variables.add(new Variable("StefanBoltzman", StefanBoltzman));
                        variables.add(new Variable("latitude", latitude));
                        variables.add(new Variable("Rg_24h", Rg_24h));
                        variables.add(new Variable("Uref", Uref));

                        float dr;
                        structure = new Structure();
                        structure.setToken("dr");
                        equation = lexer.analyse(equations.get(ParameterEnum.dr), structure, null, LanguageType.PYTHON);
                        dr = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("dr", dr));


                        float cosZ;
                        structure = new Structure();
                        structure.setToken("cosZ");
                        equation = lexer.analyse(equations.get(ParameterEnum.cosZ), structure, null, LanguageType.PYTHON);
                        cosZ = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("cosZ", cosZ));


                        float declinacaoSolar;
                        structure = new Structure();
                        structure.setToken("declinacaoSolar");
                        equation = lexer.analyse(equations.get(ParameterEnum.declinacaoSolar), structure, null, LanguageType.PYTHON);
                        declinacaoSolar = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("declinacaoSolar", declinacaoSolar));

                        float anguloHorarioNascerSol;
                        structure = new Structure();
                        structure.setToken("anguloHorarioNascerSol");
                        equation = lexer.analyse(equations.get(ParameterEnum.anguloHorarioNascerSol), structure, null, LanguageType.PYTHON);
                        anguloHorarioNascerSol = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("anguloHorarioNascerSol", anguloHorarioNascerSol));


                        float rad_solar_toa;
                        structure = new Structure();
                        structure.setToken("rad_solar_toa");
                        equation = lexer.analyse(equations.get(ParameterEnum.rad_solar_toa), structure, null, LanguageType.PYTHON);
                        rad_solar_toa = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("rad_solar_toa", rad_solar_toa));

                        float Rg_24h_mj;
                        structure = new Structure();
                        structure.setToken("Rg_24h_mj");
                        equation = lexer.analyse(equations.get(ParameterEnum.Rg_24h_mj), structure, null, LanguageType.PYTHON);
                        Rg_24h_mj = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("Rg_24h_mj", Rg_24h_mj));

                        float transmissividade24h;
                        structure = new Structure();
                        structure.setToken("transmissividade24h");
                        equation = lexer.analyse(equations.get(ParameterEnum.transmissividade24h), structure, null, LanguageType.PYTHON);
                        transmissividade24h = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("transmissividade24h", transmissividade24h));

//                        float transmissividade = (float) (0.75f + 2 * Math.pow(10, -5) * altura);
                        float ea;
                        structure = new Structure();
                        structure.setToken("ea");
                        equation = lexer.analyse(equations.get(ParameterEnum.ea), structure, null, LanguageType.PYTHON);
                        ea = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("ea", ea));

                        float W;
                        structure = new Structure();
                        structure.setToken("W");
                        equation = lexer.analyse(equations.get(ParameterEnum.W), structure, null, LanguageType.PYTHON);
                        W = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("W", W));

                        float transmissividade;
                        structure = new Structure();
                        structure.setToken("transmissividade");
                        equation = lexer.analyse(equations.get(ParameterEnum.transmissividade), structure, null, LanguageType.PYTHON);
                        transmissividade = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("transmissividade", transmissividade));

                        float emissivityAtmosfera;
                        structure = new Structure();
                        structure.setToken("emissivityAtmosfera");
                        equation = lexer.analyse(equations.get(ParameterEnum.emissivityAtmosfera), structure, null, LanguageType.PYTHON);
                        emissivityAtmosfera = (float) lexer.getResult(equation, variables);
                        variables.add(new Variable("emissivityAtmosfera", emissivityAtmosfera));

//                        EXP((-0,00146*P/(KT*COSZ)-0,075*(W/COSZ)^0,4))

                        float SWdVet = (S * cosZ * cosZ) / (1.085f * cosZ + 10.0f * ea * (2.7f + cosZ) * 0.001f + 0.2f);
                        float LWdAtmosfera = (float) (emissivityAtmosfera * StefanBoltzman * (Math.pow(Ta + T0, 4)));

                        System.exit(1);
//                        System.out.println("transmissividade:" + transmissividade);
//                        System.out.println("w:" + W);
//                        System.out.println("ea:" + ea);
//                        System.out.println("cosZ:" + cosZ);
//                        System.out.println("dr:" + dr);
//                        System.out.println("P:" + P);

//                        System.exit(1);

                        float calibracao;
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
                        parameters.add(ParameterEnum.Albedo);
                        parameters.add(ParameterEnum.NDVI);
                        parameters.add(ParameterEnum.SAVI);
                        parameters.add(ParameterEnum.LST_K);
                        parameters.add(ParameterEnum.Emissivity);
                        parameters.add(ParameterEnum.LAI);
                        parameters.add(ParameterEnum.EmissividadeNB);
                        parameters.add(ParameterEnum.LWnet);
                        parameters.add(ParameterEnum.SWnet);
                        parameters.add(ParameterEnum.Rn);
                        parameters.add(ParameterEnum.Tao_24h);
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
                        FileOutputStream fosSWd = null;
                        FileOutputStream fosRn = null;
                        FileOutputStream fosTao24 = null;
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
                        WritableRaster rasterSWd = null;
                        WritableRaster rasterRn = null;
                        WritableRaster rasterTao24 = null;
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
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterSWd = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterRn = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterTao24 = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterRg24 = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterUref = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                            pathTiff = parent + ParameterEnum.Albedo.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosAlbedo = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.NDVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosNDVI = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.SAVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosSAVI = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.LST_K.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosTs = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.Emissivity.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosEmissivity = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.LAI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosLAI = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.EmissividadeNB.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosEmissividadeNB = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.LWnet.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosLWd = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.SWnet.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosSWd = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.Rn.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosRn = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.Tao_24h.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosTao24 = new FileOutputStream(pathTiff);

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

                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < width; j++) {
                                valor = raster.getPixel(j, i, valor);

//                                if (calcule(valor)) {
                                k = 0;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                bandaRefletida1 = reflectancia;
                                banda1 = calibracao;
                                somaBandas = parameterAlbedo[k] * reflectancia;

                                k = 1;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                bandaRefletida2 = reflectancia;
                                banda2 = calibracao;
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;

                                k = 2;

                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;
                                bandaRefletida3 = reflectancia;
                                banda3 = calibracao;

                                k = 3;

                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;
                                bandaRefletida4 = reflectancia;
                                banda4 = calibracao;

                                k = 4;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                bandaRefletida5 = reflectancia;
                                banda5 = calibracao;
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;

                                k = 6;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                bandaRefletida7 = reflectancia;
                                banda7 = calibracao;
                                somaBandas = somaBandas + parameterAlbedo[k] * reflectancia;

                                albedo = (somaBandas - reflectanciaAtmosfera) / (transmissividade * transmissividade);

                                NDVI = (bandaRefletida4 - bandaRefletida3) / (bandaRefletida4 + bandaRefletida3);

                                SAVI = ((1.0f + L) * (bandaRefletida4 - bandaRefletida3)) / (L + bandaRefletida4 + bandaRefletida3);

                                if (SAVI <= 0.1f) {
                                    IAF = 0.0f;
                                } else if (SAVI >= 0.687f) {
                                    IAF = 6.0f;
                                } else {
                                    IAF = (float) (-Math.log((0.69f - SAVI) / 0.59f) / 0.91f);
                                }

                                if (IAF >= 3) {
                                    emissividadeNB = 0.98f;
                                    emissivity = 0.98f;
                                } else if (NDVI <= 0) {
                                    emissividadeNB = 0.99f;
                                    emissivity = 0.985f;
                                } else {
                                    emissividadeNB = 0.97f + 0.0033f * IAF;
                                    emissivity = 0.95f + 0.01f * IAF;
                                }

                                k = 5;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.0f) * valor[k]);
                                banda6 = calibracao;
                                Ts = (float) (K2 / (Math.log((emissividadeNB * K1 / calibracao) + 1.0f)));

                                LWd = (float) (emissivity * StefanBoltzman * (Math.pow(Ts, 4)));
//                                    SWdVet[idx] = S * cosZ * dr * transmissividade;

//                                    LWdVet[idx] = 391.5f;
//                                    SWdVet[idx] = 736.6f;
//                                    if (idx == 653) {
//                                        System.out.println("albedoVet2:" + albedoVet[idx]);
//                                    }
//                                    albedoVet[idx] = 0.172f;

                                Rn = (float) (((1.0f - albedo) * SWdVet) + (emissivity * (LWdAtmosfera) - LWd));

//                                    if (idx == 653) {
//                                        System.out.println("LWdAtmosfera:" + LWdAtmosfera);
//                                        System.out.println("Sfetan:" + StefanBoltzman);
//                                        System.out.println("albedoVet:" + albedoVet[idx]);
//                                        System.out.println("NDVIVet:" + NDVIVet[idx]);
//                                        System.out.println("SAVIVet:" + SAVIVet[idx]);
//                                        System.out.println("IAFVet:" + IAFVet[idx]);
//                                        System.out.println("emissividadeNBVet:" + emissividadeNBVet[idx]);
//                                        System.out.println("emissivityVet:" + emissivityVet[idx]);
//                                        System.out.println("TsVet:" + TsVet[idx]);
//                                        System.out.println("LWdVet:" + LWdVet[idx]);
//                                        System.out.println("SWdVet:" + SWdVet[idx]);
//                                        System.out.println("RnVet:" + RnVet[idx]);
//                                        System.exit(1);
//                                    }

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

                                    dado = new float[]{SWdVet};
                                    rasterSWd.setPixel(j, i, dado);

                                    dado = new float[]{Rn};
                                    rasterRn.setPixel(j, i, dado);

                                    dado = new float[]{transmissividade24h};
                                    rasterTao24.setPixel(j, i, dado);

                                    dado = new float[]{Rg_24h};
                                    rasterRg24.setPixel(j, i, dado);

                                    dado = new float[]{Uref};
                                    rasterUref.setPixel(j, i, dado);
                                } else {
//                                }
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
                                            line.append(SWdVet + ";");
                                            line.append(RnVet[j] + ";");
                                            line.append(transmissividade24h + ";");
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
                                line.append(SWdVet + ";");
                                line.append(RnVet[j] + ";");
                                line.append(transmissividade24h + ";");
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
                            enc = ImageCodec.createImageEncoder("tiff", fosSWd, encParam);
                            enc.encode(rasterSWd, model);
                            enc = ImageCodec.createImageEncoder("tiff", fosRn, encParam);
                            enc.encode(rasterRn, model);

                            enc = ImageCodec.createImageEncoder("tiff", fosTao24, encParam);
                            enc.encode(rasterTao24, model);
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
                            fosSWd.close();
                            fosRn.close();
                            fosTao24.close();
                            fosRg24.close();
                            fosUref.close();

                            pathTiff = parent + ParameterEnum.Albedo.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterAlbedo);

                            pathTiff = parent + ParameterEnum.NDVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterNDVI);

                            pathTiff = parent + ParameterEnum.SAVI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterSAVI);

                            pathTiff = parent + ParameterEnum.LST_K.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterTs);

                            pathTiff = parent + ParameterEnum.Emissivity.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterEmissivity);

                            pathTiff = parent + ParameterEnum.LAI.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterLAI);

                            pathTiff = parent + ParameterEnum.EmissividadeNB.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterEmissividadeNB);

                            pathTiff = parent + ParameterEnum.LWnet.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterLWd);

                            pathTiff = parent + ParameterEnum.SWnet.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterSWd);

                            pathTiff = parent + ParameterEnum.Rn.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterRn);

                            pathTiff = parent + ParameterEnum.Tao_24h.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterTao24);

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
