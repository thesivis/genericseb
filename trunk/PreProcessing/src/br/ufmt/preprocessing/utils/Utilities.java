/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing.utils;

import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageDecoderImpl;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codecimpl.util.DataBufferFloat;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import sun.awt.image.SunWritableRaster;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import static br.ufmt.preprocessing.utils.Constants.*;

/**
 *
 * @author raphael
 */
public class Utilities {

    private static List<br.ufmt.genericlexerseb.Variable> variables = new ArrayList<>();

    static {
        variables.add(new br.ufmt.genericlexerseb.Variable("pi", pi));
        variables.add(new br.ufmt.genericlexerseb.Variable("deg2rad", deg2rad));
        variables.add(new br.ufmt.genericlexerseb.Variable("kel2deg", kel2deg));
        variables.add(new br.ufmt.genericlexerseb.Variable("k", k));
        variables.add(new br.ufmt.genericlexerseb.Variable("Sigma_SB", Sigma_SB));
        variables.add(new br.ufmt.genericlexerseb.Variable("T0", T0));
        variables.add(new br.ufmt.genericlexerseb.Variable("Rso", Rso));
        variables.add(new br.ufmt.genericlexerseb.Variable("g", g));
        variables.add(new br.ufmt.genericlexerseb.Variable("Rmax", Rmax));
        variables.add(new br.ufmt.genericlexerseb.Variable("Rmin", Rmin));
        variables.add(new br.ufmt.genericlexerseb.Variable("Rd", Rd));
        variables.add(new br.ufmt.genericlexerseb.Variable("Rv", Rv));
        variables.add(new br.ufmt.genericlexerseb.Variable("Cpw", Cpw));
        variables.add(new br.ufmt.genericlexerseb.Variable("Cpd", Cpd));
        variables.add(new br.ufmt.genericlexerseb.Variable("Cd", Cd));
        variables.add(new br.ufmt.genericlexerseb.Variable("Ct", Ct));
        variables.add(new br.ufmt.genericlexerseb.Variable("gammaConst", gammaConst));
        variables.add(new br.ufmt.genericlexerseb.Variable("Pr", Pr));
        variables.add(new br.ufmt.genericlexerseb.Variable("Pr_u", Pr_u));
        variables.add(new br.ufmt.genericlexerseb.Variable("Pr_s", Pr_s));
        variables.add(new br.ufmt.genericlexerseb.Variable("ri_i", ri_i));
        variables.add(new br.ufmt.genericlexerseb.Variable("L_e", L_e));
        variables.add(new br.ufmt.genericlexerseb.Variable("rho_w", rho_w));
        variables.add(new br.ufmt.genericlexerseb.Variable("PSI0", PSI0));
        variables.add(new br.ufmt.genericlexerseb.Variable("DT", DT));
        variables.add(new br.ufmt.genericlexerseb.Variable("MaxAllowedError", MaxAllowedError));
        variables.add(new br.ufmt.genericlexerseb.Variable("dimx", dimx));
        variables.add(new br.ufmt.genericlexerseb.Variable("dimy", dimy));
        variables.add(new br.ufmt.genericlexerseb.Variable("SelectedDevice", SelectedDevice));
        variables.add(new br.ufmt.genericlexerseb.Variable("nThreadsPerBlock", nThreadsPerBlock));
        variables.add(new br.ufmt.genericlexerseb.Variable("z200", z200));
        variables.add(new br.ufmt.genericlexerseb.Variable("z2", z2));
        variables.add(new br.ufmt.genericlexerseb.Variable("z1", z1));
        variables.add(new br.ufmt.genericlexerseb.Variable("p", p));
        variables.add(new br.ufmt.genericlexerseb.Variable("cp", cp));
    }

    public static float[] getData(List<DataStructure> datas, ParameterEnum parameter) {
        for (DataStructure dataStructure : datas) {
            if (dataStructure.getParameter().equals(parameter)) {
                return dataStructure.getDatas();
            }
        }
        return null;
    }

    public static int[] getDataInt(List<DataStructure> datas, ParameterEnum parameter) {
        for (DataStructure dataStructure : datas) {
            if (dataStructure.getParameter().equals(parameter)) {
                return dataStructure.getDatasInt();
            }
        }
        return null;
    }

    public static float[] ReadTIFF(String filePath) {
        try {
            File file = new File(filePath);
            SeekableStream s = new FileSeekableStream(file);

            TIFFDecodeParam param = null;

            ImageDecoderImpl dec = (ImageDecoderImpl) ImageCodec.createImageDecoder("tiff", s, param);

            // Which of the multiple images in the TIFF file do we want to load
            // 0 refers to the first, 1 to the second and so on.

            int k = 0;

            ArrayList<PrintWriter> arqs;
            Raster raster = dec.decodeAsRaster(k);

            float[] ret = new float[raster.getWidth() * raster.getHeight()];

            double[] valor = null;
            int idx = 0;
            for (int i = 0; i < raster.getHeight(); i++) {
                for (int j = 0; j < raster.getWidth(); j++) {
                    valor = raster.getPixel(j, i, valor);
                    ret[idx] = (float) valor[0];
                    idx++;
                }
            }
            return ret;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static float[] ReadTXT(String filePath) {
        BufferedReader bur = null;
        try {
            List<Float> num = new ArrayList<Float>();
            bur = new BufferedReader(new FileReader(filePath));
            String linha = bur.readLine();
//            int quant = 1000;
            while (linha != null) {
                num.add(Float.parseFloat(linha.replace(",", ".")));
                linha = bur.readLine();
//                if(quant == 0){
//                    break;
//                }
//                quant--;
            }
            float[] res = new float[num.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = num.get(i);
            }
            return res;
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bur.close();
            } catch (IOException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static float[] ReadNetCDF(String InVarName, String filePath) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filePath);
            Variable v = ncfile.findVariable(InVarName);
            Array var = v.read();
            List<Float> num = new ArrayList<Float>();
            for (int i = 0; i < var.getSize(); i++) {
                num.add(var.getFloat(i));
            }

            float[] res = new float[num.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = num.get(i);
            }
            return res;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return null;
    }

    public static int[] ReadNetCDF_int(String InVarName, String filePath) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filePath);
            Variable v = ncfile.findVariable(InVarName);
            Array var = v.read();
            List<Integer> num = new ArrayList<Integer>();
            for (int i = 0; i < var.getSize(); i++) {
                num.add(var.getInt(i));
            }
            int[] res = new int[num.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = num.get(i);
            }
            return res;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void WriteOutput(int[] inVar, String fileName) {
        try {
            PrintWriter pw = new PrintWriter(fileName);
            for (int i = 0; i < inVar.length; i++) {
                pw.printf("%d\n", inVar[i]);
            }
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void WriteOutput(float[] inVar, String fileName) {
        try {
            PrintWriter pw = new PrintWriter(fileName);
            for (int i = 0; i < inVar.length; i++) {
                pw.printf("%.6f\n", inVar[i]);
            }
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<File> exportTXT(String path, List<DataStructure> datas) {
        List<File> files = new ArrayList<File>();
        PrintWriter pw;
        float[] values;
        String name;
        File dir = new File(path);
        dir.mkdirs();
        for (int i = 0; i < datas.size(); i++) {
            values = datas.get(i).getDatas();
            try {
                name = path + "/" + datas.get(i).getParameter().getFileName();
                pw = new PrintWriter(name);
                for (int j = 0; j < values.length; j++) {
                    pw.printf("%.6f\n", values[j]);
                }
                pw.close();
                files.add(new File(name));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return files;
    }

    public static List<File> exportTIFF(String path, String pathTiff, List<DataStructure> datas) {
        try {
            List<File> files = new ArrayList<File>();
            float[] values;
            String name;

            SeekableStream s = null;
            s = new FileSeekableStream(pathTiff);
            TIFFDecodeParam param = null;
            ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
            // Which of the multiple images in the TIFF file do we want to load
            // 0 refers to the first, 1 to the second and so on.

            ColorModel model = dec.decodeAsRenderedImage().getColorModel();
            Raster raster = dec.decodeAsRaster(0);
            int width = raster.getWidth();

            TIFFEncodeParam encParam = null;
            ImageEncoder enc;
            WritableRaster wraster;
            FileOutputStream fos = null;

            BandedSampleModel mppsm;
            DataBufferFloat dataBuffer;


            //GETTING CONFIGURATION OF TIFF
            Iterator readersIterator = ImageIO.getImageReadersByFormatName("tif");
            ImageReader imageReader = (ImageReader) readersIterator.next();
            ImageInputStream imageInputStream = new FileImageInputStream(new File(pathTiff));
            imageReader.setInput(imageInputStream, false, true);
            int k = 0;
            IIOMetadata imageMetaData = imageReader.getImageMetadata(k);
            TIFFDirectory ifd = TIFFDirectory.createFromMetadata(imageMetaData);
            /* Create a Array of TIFFField*/
            TIFFField[] allTiffFields = ifd.getTIFFFields();

            int x;
            int y;

            float[] dado;
            File dir = new File(path);
            dir.mkdirs();
            for (int i = 0; i < datas.size(); i++) {
                values = datas.get(i).getDatas();
                try {

                    name = path + "/" + datas.get(i).getParameter().getFileName();
                    name = name.replace(".dat", ".tif");
                    fos = new FileOutputStream(name);

                    mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                    dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());

                    wraster = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));

                    x = 0;
                    y = 0;

                    for (int j = 0; j < values.length; j++) {
                        if ((j != 0) && (j % (width)) == 0) {
                            y++;
                            x = 0;
                        }
                        dado = new float[]{values[j]};
                        wraster.setPixel(x, y, dado);
                        x++;
                    }
//                    W:1688 H:1279

                    enc = ImageCodec.createImageEncoder("tiff", fos, encParam);
                    enc.encode(wraster, model);
                    fos.close();

                    Utilities.saveTiff(name, imageReader, allTiffFields, wraster);

                    files.add(new File(name));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            return files;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void saveTiff(String pathTiff, ImageReader imageReader, TIFFField[] allTiffFields, WritableRaster wraster) {
        try {

            ImageInputStream imageInputStream;

            System.out.println("Lendo:" + pathTiff);
            imageInputStream = new FileImageInputStream(new File(pathTiff));
            imageReader.setInput(imageInputStream, false, true);

            IIOMetadata imageMetaData = imageReader.getImageMetadata(0);

            TIFFDirectory ifd = TIFFDirectory.createFromMetadata(imageMetaData);
//            ifd.removeTIFFFields();

            for (int i = 0; i < allTiffFields.length; i++) {
                if (allTiffFields[i].getTag().getNumber() != 339) {
//                    System.out.println("Add:"+allTiffFields[i].getTag().getName());
                    ifd.addTIFFField(allTiffFields[i]);
                }
            }

            imageMetaData = ifd.getAsMetadata();

            ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);

            ColorModel colorModel = new ComponentColorModel(
                    colorSpace, false, false, Transparency.OPAQUE,
                    DataBuffer.TYPE_FLOAT);

            BufferedImage newImage = new BufferedImage(colorModel, wraster, false, null);

            Iterator writers = ImageIO.getImageWritersByFormatName("TIFF");
            ImageWriter writer = (ImageWriter) writers.next();

            FileImageOutputStream fios = new FileImageOutputStream(new File(pathTiff));
            writer.setOutput(fios);
            writer.prepareWriteSequence(imageMetaData);
            ImageWriteParam par = writer.getDefaultWriteParam();
            writer.writeToSequence(new IIOImage(newImage, null, imageMetaData), par);
            writer.endWriteSequence();
            writer.dispose();
            writer = null;
            fios.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static float arredondar(float num, int casas) {
//        System.out.println("Dentro");
        float ret = num;
//        System.out.println(ret);
        float dez = (float) Math.pow(10, casas);
//        System.out.println(dez);
        ret = ret * dez;
//        System.out.println(ret);
        ret = (Math.round(ret) / dez);
//        System.out.println(ret);
        return ret;
    }

    public static void calculaAB(float[] coeficientes, float Rn_hot, float G_hot, float Uref, float SAVI_hot, float Ts_hot, float Ts_cold) {

        float z0m = (float) Math.exp(-5.809f + 5.62f * SAVI_hot);

        float U_star = (float) (Constants.k * Uref / Math.log(Constants.z200 / z0m));

        float r_ah = (float) (Math.log(Constants.z2 / Constants.z1) / (U_star * Constants.k));

        float H_hot = Rn_hot - G_hot;

        float a = 0.0f;
        float b = 0.0f;

        float L;

        float tm_200;
        float th_2;
        float th_0_1;

        float errorH = 10.0f;
        int step = 1;
        float r_ah_anterior;
        float H = H_hot;

        while (errorH > Constants.MaxAllowedError && step < 100) {

            a = ((H) * r_ah) / (Constants.p * Constants.cp * (Ts_hot - Ts_cold));
            b = -a * (Ts_cold - Constants.T0);


            H = Constants.p * Constants.cp * (b + a * (Ts_hot - Constants.T0)) / r_ah;

            L = (float) (-(Constants.p * Constants.cp * U_star * U_star * U_star * (Ts_hot)) / (Constants.k * Constants.g * H));

            tm_200 = Psim(L);
            th_2 = Psih(Constants.z2, L);
            th_0_1 = Psih(Constants.z1, L);

            U_star = (float) (Constants.k * Uref / (Math.log(Constants.z200 / z0m) - tm_200));
            r_ah_anterior = r_ah;
            r_ah = (float) ((Math.log(Constants.z2 / Constants.z1) - th_2 + th_0_1) / (U_star * Constants.k));

            errorH = Math.abs(((r_ah - r_ah_anterior) * 100) / r_ah);

            step++;
        }

//        System.out.println("Total de Interações:" + step);
        coeficientes[0] = a;
        coeficientes[1] = b;

    }

    protected static float X(float Zref_m, float L) {
        return (float) (Math.sqrt(Math.sqrt((1.0f - 16.0f * Zref_m / L))));
    }

    protected static float Psim(float L) {
        if (L < 0.0f) {
            /* unstable */
            float x200 = X(200, L);
            return (float) (2.0f * Math.log((1.0f + x200) / 2.0f) + Math.log((1.0f + x200 * x200) / (2.0f)) - 2.0f * Math.atan(x200) + 0.5f * Math.PI);
        } else if (L > 0.0f) {
            /* stable */
            return (-5 * (2 / L));
        } else {
            return (0);
        }
    }

    protected static float Psih(float Zref_h, float L) {
        if (L < 0.0f) {
            /* unstable */
            float x = X(Zref_h, L);
            return (float) (2.0f * Math.log((1.0f + x * x) / 2.0f));
        } else if (L > 0.0f) {
            /* stable */
            return (-5 * (2 / L));
        } else {
            return (0);
        }
    }

    public static List<String> getVariables() {
        List<String> variables = new ArrayList<>();

        variables.add("pi");
        variables.add("deg2rad");
        variables.add("kel2deg");
        variables.add("k");
        variables.add("Sigma_SB");
        variables.add("T0");
        variables.add("Rso");
        variables.add("g");
        variables.add("Rmax");
        variables.add("Rmin");
        variables.add("Rd");
        variables.add("Rv");
        variables.add("Cpw");
        variables.add("Cpd");
        variables.add("Cd");
        variables.add("Ct");
        variables.add("gammaConst");
        variables.add("Pr");
        variables.add("Pr_u");
        variables.add("Pr_s");
        variables.add("ri_i");
        variables.add("L_e");
        variables.add("rho_w");
        variables.add("PSI0");
        variables.add("DT");
        variables.add("MaxAllowedError");
        variables.add("dimx");
        variables.add("dimy");
        variables.add("SelectedDevice");
        variables.add("nThreadsPerBlock");
        variables.add("z200");
        variables.add("z2");
        variables.add("z1");
        variables.add("p");
        variables.add("cp");

        return variables;
    }

    public static List<br.ufmt.genericlexerseb.Variable> getVariable() {
        return variables;
    }
}
