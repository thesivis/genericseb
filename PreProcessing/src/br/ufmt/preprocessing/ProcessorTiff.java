/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing;

import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericseb.GenericSEB;
import br.ufmt.genericseb.VariableValue;
import br.ufmt.preprocessing.utils.DataFile;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

/**
 *
 * @author raphael
 */
public class ProcessorTiff {

    public static final int MAX = 550000000;
    private LanguageType language = LanguageType.JAVA;
    private Driver driver;

    public ProcessorTiff() {
        this(LanguageType.JAVA);
    }

    public ProcessorTiff(LanguageType language) {
        this.language = language;
        gdal.AllRegister();

        driver = gdal.GetDriverByName("GTiff");
        driver.Register();
    }

    public List<DataFile> execute(String header, String body, String pathProcessorTiff, String[] nameParameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) throws Exception {
        File tiff = new File(pathProcessorTiff);
        if (tiff.exists() && tiff.getName().endsWith(".tif")) {

            Dataset entrada = gdal.Open(pathProcessorTiff, gdalconstConstants.GA_ReadOnly);

            int bands = entrada.GetRasterCount();
            int height = entrada.GetRasterYSize();
            int width = entrada.GetRasterXSize();

            System.out.println("Calculating ");

            int size = height * width;
            int totalPixels = size * bands;
            int total = totalPixels;

            System.out.println("Total:" + totalPixels);
            System.out.println("Size:" + size);
            short[][] pixel = new short[bands][height * width];
            System.out.println("Alocado");

            List<VariableValue> parameters = new ArrayList<>();
            for (int i = 1; i <= bands; i++) {
                parameters.add(new VariableValue(nameParameters[i - 1], pixel[i - 1]));
            }

            System.out.println("Creating datas");

            for (int i = 0; i < bands; i++) {
                Band banda = entrada.GetRasterBand((i + 1));
                banda.ReadRaster(0, 0, width, height, pixel[i]);
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
                                } else {
                                    break;
                                }
                            }
                            total = totalPixels;
                            execute(tiff, entrada, ret, header, without, exec, parameters, constants, constantsVetor, constantsMatrix);
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
                execute(tiff, entrada, ret, header, without, exec, parameters, constants, constantsVetor, constantsMatrix);
            }

            return ret;

        }
        return null;
    }

    private void execute(File tiff, Dataset entrada, List<DataFile> ret, String header, StringBuilder without, StringBuilder exec, List<VariableValue> parameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) {
        try {
//            System.out.println("Executing:" + exec.toString());
            System.out.println("Executing");
//            System.out.println("Whito:" + without.toString());
//            System.out.println();

            GenericSEB g = new GenericSEB(language);
            Map<String, float[]> datas = g.execute(header, exec.toString(), parameters, constants, constantsVetor, constantsMatrix);

            exec.delete(0, exec.length());
            exec.append(without.toString());

            System.out.println("Executed");

            String parent = tiff.getParent() + "/OutputParameters/";
            File dir = new File(parent);
            dir.mkdirs();
            String pathTiff;

            int height = entrada.GetRasterYSize();
            int width = entrada.GetRasterXSize();
            System.out.println("height:" + height);
            System.out.println("width:" + width);
            String projecao = entrada.GetProjection();
            float[] vet;
            Dataset novo;
            Band bandaNovo;

            for (String resp : datas.keySet()) {
//                System.out.println("Resp:"+resp);
                vet = datas.get(resp);
                if (!resp.equals("coef") && !resp.equals("TC") && !resp.equals("TH")) {
                    pathTiff = parent + resp + ".tif";
                    novo = driver.Create(pathTiff, width, height, 1, gdalconstConstants.GDT_Float32);
                    novo.SetProjection(projecao);
                    bandaNovo = novo.GetRasterBand(1);
                    bandaNovo.WriteRaster(0, 0, width, height, vet);
                    bandaNovo.SetNoDataValue(0);
                    bandaNovo = null;
                    novo = null;

                    ret.add(new DataFile(resp, new File(pathTiff)));
//                    System.out.println("salvo");
                } else {
                    System.out.println(resp + "=" + Arrays.toString(vet));
//                    pathTiff = parent + "A.dat";
//                    PrintWriter pw = new PrintWriter(pathTiff);
//                    pw.print(vet[0]);
//                    pw.close();
//                    ret.add(new DataFile("A", new File(pathTiff)));
//                    constants.put("a", vet[0]);
//
//                    pathTiff = parent + "B.dat";
//                    pw = new PrintWriter(pathTiff);
//                    pw.print(vet[1]);
//                    constants.put("b", vet[1]);
//                    pw.close();
//                    ret.add(new DataFile("B", new File(pathTiff)));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ProcessorTiff.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
