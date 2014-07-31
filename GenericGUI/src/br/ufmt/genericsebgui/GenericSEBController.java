/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebgui;

import br.ufmt.genericgui.GenericController;
import br.ufmt.genericgui.Main;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericseb.GenericSEB;
import br.ufmt.genericseb.VariableValue;
import br.ufmt.preprocessing.utils.DataFile;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import br.ufmt.utils.EditingCell;
import br.ufmt.utils.Image;
import br.ufmt.utils.Names;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

/**
 * FXML Controller class
 *
 * @author raphael
 */
public class GenericSEBController extends GenericController {

    @FXML
    private TableView<Image> filesTable;
    @FXML
    private TableView<Constante> calibrationTable;
    private Driver driver;

    public GenericSEBController() {
        extensions = new String[]{"*.tiff", "*.tif"};
        extensionsConf = new String[]{"*.seb"};
        gdal.AllRegister();

        driver = gdal.GetDriverByName("GTiff");
        driver.Register();
    }

    @FXML
    protected void addFileAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("extension"), extensions));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(bundle.getString("file.chooser.title"));
        file = fileChooser.showOpenDialog(Main.screen);
        if (file != null) {
            int tam;

            boolean add = true;

            Dataset entrada = gdal.Open(file.getPath(), gdalconstConstants.GA_ReadOnly);

            int bands = entrada.GetRasterCount();
            int height = entrada.GetRasterYSize();
            int width = entrada.GetRasterXSize();

            tam = height * width;
            System.out.println("Add W:" + width + " H:" + height);
            if (filesTable.getItems().size() > 0) {
                Image image;
                int tamCompare;
                Dataset rasterCompare;
                for (int i = 0; i < filesTable.getItems().size(); i++) {
                    image = filesTable.getItems().get(i);
                    rasterCompare = gdal.Open(image.getFile().getPath(), gdalconstConstants.GA_ReadOnly);
                    tamCompare = rasterCompare.GetRasterYSize() * rasterCompare.GetRasterXSize();
                    System.out.println("I:" + (i + 1) + " W:" + rasterCompare.GetRasterXSize() + " H:" + rasterCompare.GetRasterYSize());
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
                        name.append(vet[i]);
                    }
                    filesTable.getItems().add(new Image(file.getName(), name.toString(), file));
                }
            } else {
                new AlertDialog(Main.screen, bundle.getString("error.size") + " X:" + width + " Y:" + height).showAndWait();
            }

        }

    }

    @FXML
    protected void removeFileAction(ActionEvent event) {
        removeSelecteds(filesTable);
    }

    @FXML
    protected void editTableFileAction(TableColumn.CellEditEvent<Image, String> t) {
        Image editado = ((Image) t.getTableView().getItems().get(t.getTablePosition().getRow()));
        editado.setValor(t.getNewValue());
    }

    @FXML
    protected void editCalibrationValue1Action(TableColumn.CellEditEvent<Constante, String> t) {
        Constante editado = ((Constante) t.getTableView().getItems().get(t.getTablePosition().getRow()));
        editado.setValor(Float.parseFloat(t.getNewValue()));
    }

    @FXML
    protected void editCalibrationValue2Action(TableColumn.CellEditEvent<Constante, String> t) {
        Constante editado = ((Constante) t.getTableView().getItems().get(t.getTablePosition().getRow()));
        editado.setValor2(Float.parseFloat(t.getNewValue()));
    }

    @FXML
    protected void editCalibrationValue3Action(TableColumn.CellEditEvent<Constante, String> t) {
        Constante editado = ((Constante) t.getTableView().getItems().get(t.getTablePosition().getRow()));
        editado.setValor3(Float.parseFloat(t.getNewValue()));
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
                        int size = 0;
                        List<VariableValue> parameters = new ArrayList<>();
                        Map<String, Integer> files = new HashMap<>();

                        float[] data;
                        float[] value = null;
                        int idx;
                        int l;

                        Dataset raster = null;

                        for (int i = 0; i < filesTable.getItems().size(); i++) {
                            image = filesTable.getItems().get(i);
                            if (!files.containsKey(image.getFile().getName())) {
                                files.put(image.getFile().getName(), 0);
                            }

                            raster = gdal.Open(image.getFile().getPath(), gdalconstConstants.GA_ReadOnly);
                            if (size == 0) {
                                size = raster.GetRasterYSize() * raster.GetRasterXSize();
                            }
                            data = new float[size];

                            idx = 0;
                            l = files.get(image.getFile().getName());
                            Band banda = raster.GetRasterBand((l + 1));
                            banda.ReadRaster(0, 0, raster.GetRasterXSize(), raster.GetRasterYSize(), data);
                            files.put(image.getFile().getName(), l + 1);
                            parameters.add(new VariableValue(image.getValor(), data));
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

                        int bands = filesTable.getItems().size();
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
                                        execute(tiff, raster, ret, header.toString(), without, exec, parameters, constants, constVetor, constMatrix);
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
                            execute(tiff, raster, ret, header.toString(), without, exec, parameters, constants, constVetor, constMatrix);
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

    private void execute(File tiff, Dataset raster, List<DataFile> ret, String header, StringBuilder without, StringBuilder exec, List<VariableValue> parameters, Map<String, Float> constants, Map<String, float[]> constantsVetor, Map<String, float[][]> constantsMatrix) {
        try {
            System.out.println("Executing:" + exec.toString());
//            System.out.println("Whito:" + without.toString());
//            System.out.println();

            GenericSEB g = new GenericSEB(LanguageType.JAVA);
            Map<String, float[]> datas = g.execute(header, exec.toString(), parameters, constants, constantsVetor, constantsMatrix);

            exec.delete(0, exec.length());
            exec.append(without.toString());

            System.out.println("Executed");

            String parent = tiff.getParent() + "/OutputParameters/";
            File dir = new File(parent);
            dir.mkdirs();
            String pathTiff;

            float[] dado;
            int x, y;
            int width = raster.GetRasterXSize();
            int height = raster.GetRasterYSize();
            String projecao = raster.GetProjection();
            System.out.println("Width:" + width);
            System.out.println("height:" + height);
            float[] vet;
            Dataset novo;
            Band bandaNovo;

            for (String resp : datas.keySet()) {
                vet = datas.get(resp);
                if (!resp.equals("coef")) {
                    pathTiff = parent + resp + ".tif";
                    novo = driver.Create(pathTiff, width, height, 1, gdalconstConstants.GDT_Float32);
                    novo.SetProjection(projecao);
                    bandaNovo = novo.GetRasterBand(1);
                    bandaNovo.WriteRaster(0, 0, width, height, vet);
                    bandaNovo.SetNoDataValue(0);
                    
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

        Callback<TableColumn, TableCell> cellFactoryString
                = new Callback<TableColumn, TableCell>() {
                    @Override
                    public TableCell call(TableColumn p) {
                        return new EditingCell(bundle);
                    }
                };

        Callback<TableColumn, TableCell> cellFactoryDouble
                = new Callback<TableColumn, TableCell>() {
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
            tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("valor" + (i + 1)));
            tc.setCellFactory(cellFactoryDouble);
        }

    }

    @Override
    public void open(File file) {
        try {
            BufferedReader bur = new BufferedReader(new FileReader(file));
            String line = bur.readLine();
            if (line != null && line.equals(Names.CONSTANT)) {
                String[] vet;
                line = bur.readLine();
                while (line != null && (!line.equals("<calibration>") && !line.equals(Names.FOR_VARIABLES) && !line.equals(Names.FOR_EACH_VALUE))) {
                    vet = line.split("=");
                    if (vet[1].matches("(-?)[0-9]+([\\.][0-9]+([E](-?)[0-9+])?)?")) {
                        constanteTable.getItems().add(new Constante(vet[0], Float.parseFloat(vet[1])));
                    }
                    line = bur.readLine();
                }
            }
            if (line != null && line.equals("<calibration>")) {
                String[] vet;
                line = bur.readLine();
                boolean right;
                while (line != null && (!line.equals(Names.FOR_VARIABLES) && !line.equals(Names.FOR_EACH_VALUE))) {
                    vet = line.split(";");
                    right = true;
                    for (String vet1 : vet) {
                        if (!vet1.matches("(-?)[0-9]+([\\.][0-9]+([E](-?)[0-9+])?)?")) {
                            right = false;
                            break;
                        }
                    }
                    if (right) {
                        calibrationTable.getItems().add(new Constante("name", Float.parseFloat(vet[0]), Float.parseFloat(vet[1]), Float.parseFloat(vet[2])));
                    }
                    line = bur.readLine();
                }
            }
            if (line != null && line.equals(Names.FOR_VARIABLES)) {
                line = bur.readLine();
                while (line != null && (!line.equals(Names.FOR_EACH_VALUE))) {
                    headerTable.getItems().add(new Constante(line, 0.0f));
                    line = bur.readLine();
                }
            }
            if (line != null && line.equals(Names.FOR_EACH_VALUE)) {
                line = bur.readLine();
                while (line != null) {
                    bodyTable.getItems().add(new Constante(line, 0.0f));
                    line = bur.readLine();
                }
            }
            bur.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void save(File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.println(Names.CONSTANT);
            for (Constante object : constanteTable.getItems()) {
                pw.println(object.getNome() + "=" + object.getValor());
            }
            pw.println("<calibration>");
            for (int i = 0; i < calibrationTable.getItems().size(); i++) {
                Constante constante = calibrationTable.getItems().get(i);
                pw.println(constante.getValor() + ";" + constante.getValor2() + ";" + constante.getValor3());
            }
            pw.println(Names.FOR_VARIABLES);
            for (Constante object : headerTable.getItems()) {
                pw.println(object.getNome());
            }
            pw.println(Names.FOR_EACH_VALUE);
            for (Constante object : bodyTable.getItems()) {
                pw.println(object.getNome());
            }
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void clear() {
        filesTable.getItems().clear();
        calibrationTable.getItems().clear();
    }
}
