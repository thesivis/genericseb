/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing;

import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericseb.GenericSEB;
import br.ufmt.genericseb.Value;
import br.ufmt.preprocessing.utils.DataFile;
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
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class ProcessorTiff {

    private int MAX = 600000000;
    private LanguageType language = LanguageType.JAVA;

    public ProcessorTiff() {
    }

    public ProcessorTiff(LanguageType language) {
        this.language = language;
    }

    public List<DataFile> execute(String header, String body, String pathProcessorTiff, String[] nameParameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {
        File tiff = new File(pathProcessorTiff);
        if (tiff.exists() && tiff.getName().endsWith(".tif")) {
            SeekableStream s = null;
            try {
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

                float[] valor = null;
                int idx;
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

                int size = width * height;
                int totalPixels = size * bands;
                int total = totalPixels;

                System.out.println(totalPixels);
                System.out.println(size);
                float[][] pixel = new float[bands][width * height];

                List<Value> parameters = new ArrayList<>();
                for (int i = 1; i <= bands; i++) {
                    parameters.add(new Value(nameParameters[i - 1], pixel[i - 1]));
                }

                System.out.println("Creating datas");
                idx = 0;
                for (int j = 0; j < width; j++) {
                    for (int i = 0; i < height; i++) {
                        valor = raster.getPixel(j, i, valor);
                        for (int l = 0; l < valor.length; l++) {
                            pixel[l][idx] = valor[l];
                        }
                        idx++;
                    }
                }
                System.out.println("Configuring Execution");

                List<DataFile> ret = new ArrayList<>();
                String[] lines = body.split("\n");
                Set<String> outputs = new HashSet<>();
                StringBuilder without = new StringBuilder();
                StringBuilder exec = new StringBuilder();
                boolean executed = false;
                for (int i = 0; i < lines.length; i++) {
                    String string = lines[i];
                    executed = false;
//                    System.out.println("String:"+string);
                    if (string.startsWith("O_")) {
                        without.append(string.substring(2));
                        String var = string.substring(2);
                        if (var.contains("_(")) {
                            var = var.substring(0, var.indexOf("_("));
                        } else {
                            var = var.substring(0, var.indexOf("="));
                        }
                        var = var.replaceAll("[ ]+", "");
                        if (outputs.add(var)) {
//                            System.out.println("var:" + var + ":");
                            exec.append(string);
                            if (total + 2 * size < MAX) {
                                total = total + size;
                            } else {
                                for (int j = i + 1; j < lines.length; j++) {
                                    String string2 = lines[j];
                                    
                                    var = string2.substring(2);
                                    if (var.contains("_(")) {
                                        var = var.substring(0, var.indexOf("_("));
                                    } else {
                                        var = var.substring(0, var.indexOf("="));
                                    }
                                    var = var.replaceAll("[ ]+", "");
                                    if (outputs.contains(var)) {
                                        without.append("\n").append(string2.substring(2));
                                        exec.append("\n").append(string2);
                                        i = j;
                                    }else{
                                        break;
                                    }
                                }
                                total = totalPixels;
                                execute(tiff, raster, model, imageReader, allTiffFields, ret, header, without, exec, parameters, constants, constantsVetor, constantsMatrix);
                                executed = true;
                            }
                        } else {
                            exec.append(string);
                        }
                    } else {
                        if (!string.startsWith("index")) {
                            without.append(string);
                        }
                        exec.append(string);
                    }
                    if (!string.startsWith("index")) {
                        without.append("\n");
                    }
                    exec.append("\n");
                }

                if (!executed) {
                    execute(tiff, raster, model, imageReader, allTiffFields, ret, header, without, exec, parameters, constants, constantsVetor, constantsMatrix);
                }

                s.close();
                return ret;


            } catch (IOException ex) {
                Logger.getLogger(ProcessorTiff.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private void execute(File tiff, Raster raster, ColorModel model, ImageReader imageReader, TIFFField[] allTiffFields, List<DataFile> ret, String header, StringBuilder without, StringBuilder exec, List<Value> parameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) {
        try {
            System.out.println("Executing:" + exec.toString());
//            System.out.println("Whito:" + without.toString());
//            System.out.println();

            GenericSEB g = new GenericSEB(language);
            Map<String, float[]> datas = g.execute(header, exec.toString(), parameters, constants, constantsVetor, constantsMatrix);

            exec.delete(0, exec.length());
            exec.append(without.toString());

            System.out.println("Executed");
            FileOutputStream fos;
            WritableRaster rasterResp;

            BandedSampleModel mppsm;
            DataBufferFloat dataBuffer;
            TIFFEncodeParam encParam = null;
            ImageEncoder enc;

            String parent = tiff.getParent() + "/OutputParameters/";
            File dir = new File(parent);
            dir.mkdirs();
            String pathTiff;

            float[] dado;
            int x, y;
            int width = raster.getWidth();
            int height = raster.getHeight();
            System.out.println("Width:" + width);
            System.out.println("height:" + height);
            float[] vet;

            for (String resp : datas.keySet()) {
                vet = datas.get(resp);
                if (!resp.equals("coef")) {

                    pathTiff = parent + resp + ".tif";
                    mppsm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), 1);
                    dataBuffer = new DataBufferFloat(raster.getWidth() * raster.getHeight());
                    rasterResp = new SunWritableRaster(mppsm, dataBuffer, new Point(0, 0));
                    fos = new FileOutputStream(pathTiff);

                    for (int j = 0; j < vet.length; j++) {
                        dado = new float[]{(float) vet[j]};
                        x = j % width;
                        y = j / width;
                        try {
                            rasterResp.setPixel(x, y, dado);
                        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                            System.out.println("i:" + j + " X:" + x + " Y: " + y);
                            System.exit(1);
                        }
                    }

                    enc = ImageCodec.createImageEncoder("tiff", fos, encParam);
                    enc.encode(rasterResp, model);
                    fos.close();
                    Utilities.saveTiff(pathTiff, imageReader, allTiffFields, rasterResp);
                    ret.add(new DataFile(resp, new File(pathTiff)));
                } else {
                    pathTiff = parent + "A.dat";
                    PrintWriter pw = new PrintWriter(pathTiff);
                    pw.print(vet[0]);
                    pw.close();
                    ret.add(new DataFile("A", new File(pathTiff)));
                    constants.put("a", vet[0]);

                    pathTiff = parent + "B.dat";
                    pw = new PrintWriter(pathTiff);
                    pw.print(vet[1]);
                    constants.put("b", vet[1]);
                    pw.close();
                    ret.add(new DataFile("B", new File(pathTiff)));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ProcessorTiff.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
