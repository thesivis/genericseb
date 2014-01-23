/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing;

import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericseb.GenericSEB;
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
public class ProcessorTiff {

    private LanguageType language = LanguageType.JAVA;

    public ProcessorTiff() {
    }

    public ProcessorTiff(LanguageType language) {
        this.language = language;
    }

    public List<DataFile> execute(String header, String body, String pathProcessorTiff, String[] nameParameters, Map<String, Double> constants, Map<String, double[]> constantsVetor, Map<String, double[][]> constantsMatrix) throws Exception {
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

                double[] valor = null;
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

                double[][] pixel = new double[bands][width * height];

                Map<String, double[]> parameters = new HashMap<>();
                for (int i = 1; i <= bands; i++) {
                    parameters.put(nameParameters[i - 1], pixel[i - 1]);
                }

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
                System.out.println("Executing");
                GenericSEB g = new GenericSEB(language);
                Map<String, double[]> datas = g.execute(header, body, parameters, constants, constantsVetor, constantsMatrix);

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
                System.out.println("Width:" + width);
                System.out.println("height:" + height);
                double[] vet;
                List<DataFile> ret = new ArrayList<>();
                for (String string : datas.keySet()) {
                    vet = datas.get(string);
                    if (!string.equals("coef")) {

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
                        ret.add(new DataFile(string, new File(pathTiff)));
                    }else{
                        pathTiff = parent + "A.dat";
                        PrintWriter pw = new PrintWriter(pathTiff);
                        pw.print(vet[0]);
                        pw.close();
                        ret.add(new DataFile("A", new File(pathTiff)));
                        
                        pathTiff = parent + "B.dat";
                        pw = new PrintWriter(pathTiff);
                        pw.print(vet[1]);
                        pw.close();
                        ret.add(new DataFile("B", new File(pathTiff)));
                    }
                }

                return ret;


            } catch (IOException ex) {
                Logger.getLogger(ProcessorTiff.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessorTiff.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
}
