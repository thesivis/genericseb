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
import br.ufmt.genericseb.GenericSEB;
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

    public List<DataFile> preprocessing(String pathToOriginalTiff, double[][] calibration, double[] parameterAlbedo, int julianDay, float Z, float reflectanciaAtmosfera, float P, float UR, float Ta, float Kt, float L, float K1, float K2, float S, float StefanBoltzman, float latitude, float Rg_24h, float Uref) {

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

                        Map<String, Double> variables = new HashMap<>();
                        variables.put("julianDay", (double) julianDay);
                        variables.put("Z", (double) Z);
                        variables.put("reflectanciaAtmosfera", (double) reflectanciaAtmosfera);
                        variables.put("P", (double) P);
                        variables.put("UR", (double) UR);
                        variables.put("Ta", (double) Ta);
                        variables.put("Kt", (double) Kt);
                        variables.put("L", (double) L);
                        variables.put("K1", (double) K1);
                        variables.put("K2", (double) K2);
                        variables.put("S", (double) S);
                        variables.put("StefanBoltzman", (double) StefanBoltzman);
                        variables.put("latitude", (double) latitude);
                        variables.put("Rg_24h", (double) Rg_24h);
                        variables.put("Uref", (double) Uref);



//                        System.out.println("transmissividade:" + transmissividade);
//                        System.out.println("w:" + W);
//                        System.out.println("ea:" + ea);
//                        System.out.println("cosZ:" + cosZ);
//                        System.out.println("dr:" + dr);
//                        System.out.println("P:" + P);

//                        System.exit(1);

                        int size = tam;


                        double[] valor = null;
                        int idx = 0;
                        int k = 0;



                        //GETTING CONFIGURATION OF TIFF
                        Iterator readersIterator = ImageIO.getImageReadersByFormatName("tif");
                        ImageReader imageReader = (ImageReader) readersIterator.next();
                        ImageInputStream imageInputStream = new FileImageInputStream(tiff);
                        imageReader.setInput(imageInputStream, false, true);
                        IIOMetadata imageMetaData = imageReader.getImageMetadata(k);
                        TIFFDirectory ifd = TIFFDirectory.createFromMetadata(imageMetaData);
                        /* Create a Array of TIFFField*/
                        TIFFField[] allTiffFields = ifd.getTIFFFields();

                        System.out.println("Calculating ");


                        double[] pixel1 = new double[width * height];
                        double[] pixel2 = new double[width * height];
                        double[] pixel3 = new double[width * height];
                        double[] pixel4 = new double[width * height];
                        double[] pixel5 = new double[width * height];
                        double[] pixel6 = new double[width * height];
                        double[] pixel7 = new double[width * height];

                        Map<String, double[]> parameters = new HashMap<>();
                        parameters.put("pixel1", pixel1);
                        parameters.put("pixel2", pixel2);
                        parameters.put("pixel3", pixel3);
                        parameters.put("pixel4", pixel4);
                        parameters.put("pixel5", pixel5);
                        parameters.put("pixel6", pixel6);
                        parameters.put("pixel7", pixel7);

                        Map<String, double[][]> constMatrix = new HashMap<>();
                        constMatrix.put("calibration", calibration);

                        Map<String, double[]> constVetor = new HashMap<>();
                        constVetor.put("parameterAlbedo", parameterAlbedo);

                        String header = "dr = 1.0 + 0.033 * cos(julianDay * 2 * pi / 365)\n"
                                + "cosZ = cos(((90.0 - Z) * pi) / 180.0)\n"
                                + "declinacaoSolar = radians(23.45 * sin(radians(360.0 * (julianDay - 80) / 365)))\n"
                                + "anguloHorarioNascerSol = acos(-tan(pi * latitude / 180.0) * tan(declinacaoSolar))\n"
                                + "rad_solar_toa = 24.0 * 60.0 * 0.082 * dr * (anguloHorarioNascerSol * sin(pi * latitude / 180.0) * sin(declinacaoSolar) + cos(pi * latitude / 180.0) * cos(declinacaoSolar) * sin(anguloHorarioNascerSol)) / pi\n"
                                + "Rg_24h_mj = 0.0864 * Rg_24h\n"
                                + "transmissividade24h = Rg_24h_mj / rad_solar_toa\n"
                                + "ea = (0.61078 * exp(17.269 * Ta / (237.3 + Ta))) * UR / 100\n"
                                + "W = 0.14 * ea * P + 2.1\n"
                                + "transmissividade = 0.35 + 0.627 * exp((-0.00146 * P / (Kt * cosZ)) - 0.075 * pow((W / cosZ), 0.4))\n"
                                + "emissivityAtm = 0.625 * pow((1000.0 * ea / (Ta + T0)), 0.131)\n"
                                + "SWd = (S * cosZ * cosZ) / (1.085 * cosZ + 10.0 * ea * (2.7 + cosZ) * 0.001 + 0.2)\n"
                                + "LWdAtm = emissivityAtm * StefanBoltzman * (pow(Ta + T0, 4))";

                        String body = "rad_espectral = coef_calib_a + ((coef_calib_b - coef_calib_a) / 255.0) * pixel\n"
                                + "reflectancia = (pi * rad_espectral) / (irrad_espectral * cosZ * dr)\n"
                                + "O_albedo = (sumBandas - reflectanciaAtmosfera) / (transmissividade * transmissividade)\n"
                                + "O_NDVI = (bandaRefletida4 - bandaRefletida3) / (bandaRefletida4 + bandaRefletida3)\n"
                                + "O_SAVI = ((1.0 + L) * (bandaRefletida4 - bandaRefletida3)) / (L + bandaRefletida4 + bandaRefletida3)\n"
                                + "O_IAF = (-ln((0.69 - SAVI) / 0.59) / 0.91)\n"
                                + "O_emissividadeNB = 0.97 + 0.0033 * IAF\n"
                                + "O_emissivity = 0.95 + 0.01 * IAF\n"
                                + "O_Ts = K2/ln(((emissividadeNB * K1) / banda6) + 1.0)\n"
                                + "O_LWd = emissivity * StefanBoltzman * (pow(Ts, 4))\n"
                                + "O_Rn = ((1.0 - albedo) * SWd) + (emissivity * (LWdAtm) - LWd)";

                        idx = 0;
                        for (int j = 0; j < width; j++) {
                            for (int i = 0; i < height; i++) {
//                                System.out.println("I:" + i + " H:" + height);
                                valor = raster.getPixel(j, i, valor);
                                pixel1[idx] = valor[0];
                                pixel2[idx] = valor[1];
                                pixel3[idx] = valor[2];
                                pixel4[idx] = valor[3];
                                pixel5[idx] = valor[4];
                                pixel6[idx] = valor[5];
                                pixel7[idx] = valor[6];
                                idx++;
                            }
                        }
                        GenericSEB g = new GenericSEB();
                        Map<String, double[]> datas = g.execute(header, body, parameters, variables, constVetor, constMatrix);

                        FileOutputStream fos = null;
                        WritableRaster rasterResp = null;

                        BandedSampleModel mppsm;
                        DataBufferFloat dataBuffer;
                        TIFFEncodeParam encParam = null;
                        ImageEncoder enc;

                        String parent = tiff.getParent() + "/OutputParameters/";
                        File dir = new File(parent);
                        dir.mkdirs();
                        String pathTiff;

                        float[] dado = new float[]{};
                        int x, y;
                        System.out.println("Width:" + width);
                        System.out.println("height:" + height);
                        double[] vet = datas.get("albedo");
                        for (String string : datas.keySet()) {
                            vet = datas.get(string);

                            pathTiff = parent + string + ".tif";
                            mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                            dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                            rasterResp = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                            fos = new FileOutputStream(pathTiff);

                            for (int i = 0; i < vet.length; i++) {
                                dado = new float[]{(float) vet[i]};
                                x = i % width;
                                y = i / width;
                                try {
                                    rasterResp.setPixel(x, y, dado);
                                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                                    System.out.println("i:" + i + " X:" + x + " Y: " + y);
                                    System.exit(1);
                                }
                            }
                            
                            enc = ImageCodec.createImageEncoder("tiff", fos, encParam);
                            enc.encode(rasterResp, model);
                            fos.close();
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterResp);
                            ret.add(new DataFile(ParameterEnum.valueOf(string), new File(pathTiff)));
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

        double[][] calibration = new double[][]{
            {-1.52f, 193.0f, 1957.0f},
            {-2.84f, 365.0f, 1826.0f},
            {-1.17f, 264.0f, 1554.0f},
            {-1.51f, 221.0f, 1036.0f},
            {-0.37f, 30.2f, 215.0f},
            {1.2378f, 15.303f, 1.0f},
            {-0.15f, 16.5f, 80.67f}};

        double[] parameterAlbedo = new double[]{0.293f, 0.274f, 0.233f, 0.157f, 0.033f, 0.0f, 0.011f};
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
