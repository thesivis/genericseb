/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.utils;

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

/**
 *
 * @author raphael
 */
public class Utilities {

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
}
