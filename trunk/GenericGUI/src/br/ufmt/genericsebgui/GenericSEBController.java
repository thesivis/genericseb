/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebgui;

import br.ufmt.genericgui.GenericController;
import br.ufmt.genericgui.Main;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericseb.GenericSEB;
import br.ufmt.genericseb.Value;
import br.ufmt.preprocessing.utils.DataFile;
import br.ufmt.preprocessing.utils.Utilities;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import br.ufmt.utils.EditingCell;
import br.ufmt.utils.Image;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import sun.awt.image.SunWritableRaster;

/**
 * FXML Controller class
 *
 * @author raphael
 */
public class GenericSEBController extends GenericController {

    protected int MAX = 550000000;
    @FXML
    private TableView<Image> filesTable;
    @FXML
    private TableView<Constante> calibrationTable;

    public GenericSEBController() {
        extensions = new String[]{"*.tiff", "*.tif"};
    }

    @FXML
    protected void addFileAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("extension"), extensions));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(bundle.getString("file.chooser.title"));
        file = fileChooser.showOpenDialog(Main.screen);
        if (file != null) {
            SeekableStream s = null;
            int tam;
            try {
                boolean add = true;
                s = new FileSeekableStream(file);
                TIFFDecodeParam param = null;
                ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
                int bands;
                Raster raster = dec.decodeAsRaster(0);
                bands = raster.getNumBands();
                tam = raster.getWidth() * raster.getHeight();
                if (filesTable.getItems().size() > 0) {
                    Image image;
                    SeekableStream compare = null;
                    ImageDecoder decCompare;
                    Raster rasterCompare;
                    int tamCompare;
                    for (int i = 0; i < filesTable.getItems().size(); i++) {
                        image = filesTable.getItems().get(i);
                        compare = new FileSeekableStream(image.getFile());
                        decCompare = ImageCodec.createImageDecoder("tiff", compare, param);
                        rasterCompare = decCompare.decodeAsRaster(0);
                        tamCompare = rasterCompare.getWidth() * rasterCompare.getHeight();
                        if (tamCompare != tam) {
                            add = false;
                            break;
                        }
                    }
                }

                if (add) {
                    if (bands > 1) {
                        for (int i = 0; i < bands; i++) {
                            filesTable.getItems().add(new Image(file.getName(), "pixel" + (i + 1), file));
                        }
                    } else {
                        String vet[] = file.getName().split("\\.");
                        StringBuilder name = new StringBuilder(vet[0]);
                        for (int i = 1; i < vet.length - 1; i++) {
                            String string = vet[i];
                            name.append(vet[i]);
                        }
                        filesTable.getItems().add(new Image(file.getName(), name.toString(), file));
                    }
                } else {
                    new AlertDialog(Main.screen, bundle.getString("error.size") + " X:" + raster.getWidth() + " Y:" + raster.getHeight()).showAndWait();
                }

            } catch (IOException ex) {
                Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @FXML
    protected void removeFileAction(ActionEvent event) {
        removeSelecteds(filesTable);
    }

    @FXML
    protected void addCalibrationAction(ActionEvent event) {
        calibrationTable.getItems().add(new Constante("nome", 0.0f, 0.0f, 0.0f));
    }

    @FXML
    protected void removeCalibrationAction(ActionEvent event) {
        removeSelecteds(calibrationTable);
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
                if (filesTable.getItems().isEmpty()) {
                    new AlertDialog(Main.screen, bundle.getString("error.image")).showAndWait();
                    run = false;
                }
                if (bodyTable.getItems().isEmpty()) {
                    new AlertDialog(Main.screen, bundle.getString("error.equation")).showAndWait();
                    run = false;
                }

                if (run) {
                    updateMessage(bundle.getString("execution"));
                    StringBuilder header = new StringBuilder();
                    StringBuilder body = new StringBuilder();

                    for (Constante object : headerTable.getItems()) {
                        header.append(object.getNome()).append("\n");
                    }
                    for (Constante object : bodyTable.getItems()) {
                        body.append(object.getNome()).append("\n");
                    }
                    Map<String, Float> constants = new HashMap<>();
                    for (Constante object : constanteTable.getItems()) {
                        constants.put(object.getNome(), object.getValor());
                    }

                    try {

                        Image image = null;
                        TIFFDecodeParam param = null;
                        SeekableStream stream = null;
                        ImageDecoder decoder = null;
                        Raster raster = null;
                        int size = 0;
                        List<Value> parameters = new ArrayList<>();
                        Map<String, Integer> files = new HashMap<>();

                        float[] data;
                        float[] value = null;
                        int idx;
                        int l;

                        for (int i = 0; i < filesTable.getItems().size(); i++) {
                            image = filesTable.getItems().get(i);
                            if (!files.containsKey(image.getFile().getName())) {
                                files.put(image.getFile().getName(), 0);
                            }
                            stream = new FileSeekableStream(image.getFile());
                            decoder = ImageCodec.createImageDecoder("tiff", stream, param);
                            raster = decoder.decodeAsRaster(0);
                            if (size == 0) {
                                size = raster.getWidth() * raster.getHeight();
                            }
                            data = new float[size];
                            idx = 0;
                            l = files.get(image.getFile().getName());
                            for (int j = 0; j < raster.getWidth(); j++) {
                                for (int k = 0; k < raster.getHeight(); k++) {
                                    value = raster.getPixel(j, k, value);
                                    data[idx] = value[l];
                                    idx++;
                                }
                            }
                            files.put(image.getFile().getName(), l + 1);
                            parameters.add(new Value(image.getValor(), data));
                        }

                        float[][] calibration = new float[calibrationTable.getItems().size()][3];
                        Constante constante;
                        float sum = 0;
                        for (int i = 0; i < calibrationTable.getItems().size(); i++) {
                            constante = calibrationTable.getItems().get(i);
                            calibration[i][0] = constante.getValor();
                            calibration[i][1] = constante.getValor2();
                            calibration[i][2] = constante.getValor3();
                            if (calibration[i][2] != 1.0) {
                                sum += calibration[i][2];
                            }
                        }

                        Map<String, float[][]> constMatrix = new HashMap<>();
                        constMatrix.put("calibration", calibration);

                        float[] parameterAlbedo = new float[calibrationTable.getItems().size()];
                        for (int i = 0; i < parameterAlbedo.length; i++) {
                            if (calibration[i][2] != 1.0) {
                                parameterAlbedo[i] = calibration[i][2] / sum;
                            } else {
                                parameterAlbedo[i] = 0.0f;
                            }
                        }

                        Map<String, float[]> constVetor = new HashMap<>();
                        constVetor.put("parameterAlbedo", parameterAlbedo);

                        File tiff = image.getFile();
                        String parent = tiff.getParent() + "/OutputParameters/";
                        File dir = new File(parent);
                        dir.mkdirs();

                        //GETTING CONFIGURATION OF TIFF
                        int k = 0;
                        ColorModel model = decoder.decodeAsRenderedImage().getColorModel();
                        Iterator readersIterator = ImageIO.getImageReadersByFormatName("tif");
                        ImageReader imageReader = (ImageReader) readersIterator.next();
                        ImageInputStream imageInputStream = new FileImageInputStream(tiff);
                        imageReader.setInput(imageInputStream, false, true);
                        IIOMetadata imageMetaData = imageReader.getImageMetadata(k);
                        TIFFDirectory ifd = TIFFDirectory.createFromMetadata(imageMetaData);
                        /* Create a Array of TIFFField*/
                        TIFFField[] allTiffFields = ifd.getTIFFFields();

                        int bands = raster.getNumBands();
                        int width = raster.getWidth();
                        int height = raster.getHeight();

                        size = width * height;
                        int totalPixels = size * bands;
                        int total = totalPixels;

                        List<DataFile> ret = new ArrayList<>();
                        String[] lines = body.toString().split("\n");
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
                                        execute(tiff, raster, model, imageReader, allTiffFields, ret, header.toString(), without, exec, parameters, constants, constVetor, constMatrix);
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
                            execute(tiff, raster, model, imageReader, allTiffFields, ret, header.toString(), without, exec, parameters, constants, constVetor, constMatrix);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new AlertDialog(Main.screen, ex.getMessage()).showAndWait();
                    }

                }
                progressBar.setVisible(false);

                tabPane.setDisable(false);
                return null;
            }
        };
    }

    private void execute(File tiff, Raster raster, ColorModel model, ImageReader imageReader, TIFFField[] allTiffFields, List<DataFile> ret, String header, StringBuilder without, StringBuilder exec, List<Value> parameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) {
        try {
            System.out.println("Executing:" + exec.toString());
//            System.out.println("Whito:" + without.toString());
//            System.out.println();

            GenericSEB g = new GenericSEB(LanguageType.JAVA);
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
            Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void inicializated() {
        filesTable.getItems().clear();
        filesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        calibrationTable.getItems().clear();
        calibrationTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Callback<TableColumn, TableCell> cellFactoryString =
                new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell(bundle);
            }
        };

        Callback<TableColumn, TableCell> cellFactoryDouble =
                new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell(bundle, EditingCell.DOUBLE);
            }
        };

        TableColumn tc = (TableColumn) filesTable.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<Image, String>("valor"));
        tc.setCellFactory(cellFactoryString);

        tc = (TableColumn) filesTable.getColumns().get(1);
        tc.setCellValueFactory(new PropertyValueFactory<Image, String>("nome"));

        tc = (TableColumn) calibrationTable.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<Image, String>("valor"));
        tc.setCellFactory(cellFactoryDouble);

        for (int i = 1; i < calibrationTable.getColumns().size(); i++) {
            tc = (TableColumn) calibrationTable.getColumns().get(i);
            tc.setCellValueFactory(new PropertyValueFactory<Image, String>("valor" + (i + 1)));
            tc.setCellFactory(cellFactoryDouble);
        }

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

        calibrationTable.getItems().add(new Constante("nome", -1.52f, 193.0f, 1957.0f));
        calibrationTable.getItems().add(new Constante("nome", -2.84f, 365.0f, 1826.0f));
        calibrationTable.getItems().add(new Constante("nome", -1.17f, 264.0f, 1554.0f));
        calibrationTable.getItems().add(new Constante("nome", -1.51f, 221.0f, 1036.0f));
        calibrationTable.getItems().add(new Constante("nome", -0.37f, 30.2f, 215.0f));
        calibrationTable.getItems().add(new Constante("nome", 1.2378f, 15.303f, 1.0f));
        calibrationTable.getItems().add(new Constante("nome", -0.15f, 16.5f, 80.67f));

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
            Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void open() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
