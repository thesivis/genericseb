/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgui;

import br.ufmt.genericgui.GenericController;
import br.ufmt.genericgui.Main;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.preprocessing.LandSat;
import br.ufmt.preprocessing.ProcessorTiff;
import br.ufmt.preprocessing.exceptions.TiffErrorBandsException;
import br.ufmt.preprocessing.exceptions.TiffNotFoundException;
import br.ufmt.preprocessing.utils.DataFile;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author raphael
 */
public class SEBALLandSatGUIController extends GenericController {

    public SEBALLandSatGUIController() {
        extensions = new String[]{"*.tiff", "*.tif"};
    }

    @Override
    protected void setTask() {
        task = new Task() {
            @Override
            protected Object call() throws Exception {

                progressBar.setVisible(true);
                progressBar.setProgress(-1);
                tabPane.setDisable(true);
                boolean run = true;
                if (file == null) {
                    new AlertDialog(Main.screen, bundle.getString("error.image")).showAndWait();
                    run = false;
                }
                if (bodyTable.getItems().isEmpty()) {
                    new AlertDialog(Main.screen, bundle.getString("error.equation")).showAndWait();
                    run = false;
                }

                if (run) {
                    updateMessage(bundle.getString("execution"));
                    String path = file.getPath();
                    StringBuilder header = new StringBuilder();
                    StringBuilder body = new StringBuilder();

                    for (Constante object : headerTable.getItems()) {
                        header.append(object.getNome()).append("\n");
                    }
                    for (Constante object : bodyTable.getItems()) {
                        body.append(object.getNome()).append("\n");
                    }
                    Map<String, Float> variables = new HashMap<>();
                    for (Constante object : constanteTable.getItems()) {
                        variables.put(object.getNome(), object.getValor());
                    }


                    File tiff = file;
                    if (tiff.exists() && tiff.getName().endsWith(".tif")) {

//                SeekableStream s = null;
                        try {
                            List<DataFile> ret = new ArrayList<>();
//                    System.out.println("Arq:" + tiff.getName());
                            SeekableStream s = new FileSeekableStream(tiff);
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

                            if (bands == 7) {

                                float[][] calibration = new float[][]{
                                    {-1.52f, 193.0f, 1957.0f},
                                    {-2.84f, 365.0f, 1826.0f},
                                    {-1.17f, 264.0f, 1554.0f},
                                    {-1.51f, 221.0f, 1036.0f},
                                    {-0.37f, 30.2f, 215.0f},
                                    {1.2378f, 15.303f, 1.0f},
                                    {-0.15f, 16.5f, 80.67f}};

//                        float[] parameterAlbedo = new float[]{0.293f, 0.274f, 0.233f, 0.157f, 0.033f, 0.0f, 0.011f};
                                float[] parameterAlbedo = new float[7];

                                float sum = 0;
                                for (int i = 0; i < calibration.length; i++) {
                                    sum += calibration[i][2];
                                }
                                for (int i = 0; i < parameterAlbedo.length; i++) {
                                    parameterAlbedo[i] = calibration[i][2] / sum;
                                }
                                parameterAlbedo[5] = 0;

                                String[] nameParameters = new String[]{"pixel1", "pixel2", "pixel3", "pixel4", "pixel5", "pixel6", "pixel7"};

                                Map<String, float[][]> constMatrix = new HashMap<>();
                                constMatrix.put("calibration", calibration);

                                Map<String, float[]> constVetor = new HashMap<>();
                                constVetor.put("parameterAlbedo", parameterAlbedo);

                                try {
                                    ProcessorTiff processorTiff = new ProcessorTiff(LanguageType.JAVA);
                                    ret = processorTiff.execute(header.toString(), body.toString(), path, nameParameters, variables, constVetor, constMatrix);
                                } catch (Exception ex) {
                                    new AlertDialog(Main.screen, ex.getMessage()).showAndWait();
                                }

                                System.out.println("End");
                            } else {
                                throw new TiffErrorBandsException();
                            }

                            s.close();

                            updateMessage(bundle.getString("execution.sucess"));

                        } catch (IOException ex) {
                            Logger.getLogger(LandSat.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        throw new TiffNotFoundException();
                    }
                }
                progressBar.setVisible(false);

                tabPane.setDisable(false);
                return null;
            }
        };
    }

    @Override
    protected void inicializated() {
        constanteTable.getItems().add(new Constante("julianDay", 248.0f));
        constanteTable.getItems().add(new Constante("Z", 50.24f));
        constanteTable.getItems().add(new Constante("latitude", -16.56f));
        constanteTable.getItems().add(new Constante("Rg_24h", 243.949997f));
        constanteTable.getItems().add(new Constante("Uref", 0.92071358f));
        constanteTable.getItems().add(new Constante("P", 299.3f));
        constanteTable.getItems().add(new Constante("UR", 36.46f));
        constanteTable.getItems().add(new Constante("Ta", 32.74f));
        constanteTable.getItems().add(new Constante("reflectanciaAtmosfera", 0.03f));
        constanteTable.getItems().add(new Constante("Kt", 1.0f));
        constanteTable.getItems().add(new Constante("L", 0.1f));
        constanteTable.getItems().add(new Constante("K1", 607.76f));
        constanteTable.getItems().add(new Constante("K2", 1260.56f));
        constanteTable.getItems().add(new Constante("S", 1367.0f));
        constanteTable.getItems().add(new Constante("StefanBoltzman", (float) (5.67 * Math.pow(10, -8))));
        constanteTable.getItems().add(new Constante("Tao_24h", 0.63f));


        try {
            BufferedReader bur = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/source/landsat.prop"));
            String linha = bur.readLine();

            if (linha.equals("<header>")) {
                linha = bur.readLine();
                while (!linha.equals("<body>")) {
                    headerTable.getItems().add(new Constante(linha, 0.0f));
                    linha = bur.readLine();
                }
            }

            linha = bur.readLine();
            while (linha != null) {
                bodyTable.getItems().add(new Constante(linha, 0.0f));
                linha = bur.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(SEBALLandSatGUIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void open(File file) {
        try {
            BufferedReader bur = new BufferedReader(new FileReader(file));
            String line = bur.readLine();
            if (line != null && line.equals("<constant>")) {
                String[] vet;
                line = bur.readLine();
                while (line != null && (!line.equals("<header>") && !line.equals("<body>"))) {
                    vet = line.split("=");
                    if (vet[1].matches("(-?)[0-9]+([\\.][0-9]+)?")) {
                        constanteTable.getItems().add(new Constante(vet[0], Float.parseFloat(vet[1])));
                    }
                    line = bur.readLine();
                }
            }
            if (line != null && line.equals("<header>")) {
                line = bur.readLine();
                while (line != null && (!line.equals("<body>"))) {
                    headerTable.getItems().add(new Constante(line, 0.0f));
                    line = bur.readLine();
                }
            }
            if (line != null && line.equals("<body>")) {
                line = bur.readLine();
                while (line != null) {
                    bodyTable.getItems().add(new Constante(line, 0.0f));
                    line = bur.readLine();
                }
            }
            bur.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SEBALLandSatGUIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SEBALLandSatGUIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void save(File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.println("<constant>");
            for (Constante object : constanteTable.getItems()) {
                pw.println(object.getNome() + "=" + object.getValor());
            }
            pw.println("<header>");
            for (Constante object : headerTable.getItems()) {
                pw.println(object.getNome());
            }
            pw.println("<body>");
            for (Constante object : bodyTable.getItems()) {
                pw.println(object.getNome());
            }
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SEBALLandSatGUIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
