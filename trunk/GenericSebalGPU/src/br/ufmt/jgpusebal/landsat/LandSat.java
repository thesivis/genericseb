/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.landsat;



import br.ufmt.jgpusebal.exceptions.TiffErrorBandsException;
import br.ufmt.jgpusebal.exceptions.TiffNotFoundException;
import br.ufmt.jgpusebal.exceptions.CalibrationException;
import br.ufmt.jgpusebal.utils.Constants;
import br.ufmt.jgpusebal.utils.DataFile;
import br.ufmt.jgpusebal.utils.ParameterEnum;
import br.ufmt.jgpusebal.utils.Utilities;
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

    public List<DataFile> preprocessing(String pathToOriginalTiff, float[][] calibration, float[] parameterAlbedo, int julianDay, float Z, float reflectancaAtmosfera, float P, float UR, float Ta, float Kt, float L, float K1, float K2, float S, float StefanBoltzman, float latitude, float Rg_24h, float Uref) {

        File tiff = new File(pathToOriginalTiff);
        if (tiff.exists() && tiff.getName().endsWith(".tif")) {
            if (calibration != null && calibration.length == 7 && calibration[0].length == 3) {
                SeekableStream s = null;
                try {
                    List<DataFile> ret = new ArrayList<>();
                    System.out.println("Arq:" + tiff.getName());
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
                    System.out.println("W:" + raster.getWidth() + " H:" + raster.getHeight());
                    System.out.println("Size:" + tam);

                    if (bands == 7) {

                        float dr = (float) (1.0f + 0.033f * Math.cos(julianDay * 2 * Math.PI / 365.0f));
                        float cosZ = (float) Math.cos(((90.0f - Z) * Math.PI) / 180.0f);

                        float declinacaoSolar = (float) Math.toRadians(23.45f * Math.sin(Math.toRadians(360.0f * (julianDay - 80) / 365.f)));
                        float anguloHorarioNascerSol = (float) Math.acos(-Math.tan(Math.PI * latitude / 180.0f) * Math.tan(declinacaoSolar));
                        float rad_solar_toa = (float) (24.0f * 60.0f * 0.082f * dr * (anguloHorarioNascerSol * Math.sin(Math.PI * latitude / 180.0f) * Math.sin(declinacaoSolar) + Math.cos(Math.PI * latitude / 180.0f) * Math.cos(declinacaoSolar) * Math.sin(anguloHorarioNascerSol)) / Math.PI);
                        float Rg_24h_mj = 0.0864f * Rg_24h;
                        float transmissividade24h = Rg_24h_mj / rad_solar_toa;

//                        float transmissividade = (float) (0.75f + 2 * Math.pow(10, -5) * altura);
                        float ea = (float) ((0.61078f * Math.exp(17.269f * Ta / (237.3f + Ta))) * UR / 100.f);
                        float W = 0.14f * ea * P + 2.1f;
                        float transmissividade = (float) (0.35f + 0.627f * Math.exp((-0.00146f * P / (Kt * cosZ)) - 0.075f * Math.pow((W / cosZ), 0.4f)));
                        float emissivityAtmosfera = (float) (0.625f * Math.pow((1000.0f * ea / (Ta + Constants.T0)), 0.131f));
//                        EXP((-0,00146*P/(KT*COSZ)-0,075*(W/COSZ)^0,4))

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
                        float SWdVet = (S * cosZ * cosZ) / (1.085f * cosZ + 10.0f * ea * (2.7f + cosZ) * 0.001f + 0.2f);

                        float albedo;
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
                        parameters.add(ParameterEnum.LWd);
                        parameters.add(ParameterEnum.SWd);
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

                            pathTiff = parent + ParameterEnum.LWd.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            fosLWd = new FileOutputStream(pathTiff);

                            pathTiff = parent + ParameterEnum.SWd.getFileName();
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

                        float banda4, banda3;

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
                        float LWdAtmosfera = (float) (emissivityAtmosfera * StefanBoltzman * (Math.pow(Ta + Constants.T0, 4)));

                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < width; j++) {
                                valor = raster.getPixel(j, i, valor);

//                                if (calcule(valor)) {
                                k = 0;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                albedo = parameterAlbedo[k] * reflectancia;

                                k = 1;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                albedo = albedo + parameterAlbedo[k] * reflectancia;

                                k = 2;

                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                albedo = albedo + parameterAlbedo[k] * reflectancia;

                                banda3 = reflectancia;

                                k = 3;

                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                albedo = albedo + parameterAlbedo[k] * reflectancia;

                                banda4 = reflectancia;

                                k = 4;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                albedo = albedo + parameterAlbedo[k] * reflectancia;

                                k = 6;
                                calibracao = (float) (calibration[k][0] + ((calibration[k][1] - calibration[k][0]) / 255.f) * valor[k]);
                                reflectancia = (float) ((Math.PI * calibracao) / (calibration[k][2] * cosZ * dr));
                                albedo = albedo + parameterAlbedo[k] * reflectancia;

                                albedo = (albedo - reflectancaAtmosfera) / (transmissividade * transmissividade);

                                NDVI = (banda4 - banda3) / (banda4 + banda3);

                                SAVI = ((1.0f + L) * (banda4 - banda3)) / (L + banda4 + banda3);

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

                            pathTiff = parent + ParameterEnum.LWd.getFileName();
                            pathTiff = pathTiff.replace(".dat", ".tif");
                            Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterLWd);

                            pathTiff = parent + ParameterEnum.SWd.getFileName();
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
