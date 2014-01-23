/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgui;

import br.ufmt.genericgui.Main;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.preprocessing.LandSat;
import br.ufmt.preprocessing.ProcessorTiff;
import br.ufmt.preprocessing.exceptions.TiffErrorBandsException;
import br.ufmt.preprocessing.exceptions.TiffNotFoundException;
import br.ufmt.preprocessing.utils.DataFile;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import br.ufmt.utils.EditingCell;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 *
 * @author raphael
 */
public class SEBALLandSatGUIController implements Initializable {

    private ResourceBundle bundle;
    @FXML
    private Label nomeArquivoLabel;
    @FXML
    private Button uploadButton;
    @FXML
    private Button runButton;
    @FXML
    private TableView<Constante> constanteTable;
    @FXML
    private TableView<Constante> headerTable;
    @FXML
    private TableView<Constante> bodyTable;
    private File image;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TabPane tabPane;
    private Task task;

    @FXML
    private void runButtonAction(ActionEvent event) {
        new Thread(task).start();
    }

    @FXML
    private void uploadButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("extension"), "*.tiff", "*.tif"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(bundle.getString("file.chooser.title"));
        image = fileChooser.showOpenDialog(Main.screen);
        if (image != null) {
            nomeArquivoLabel.setText(image.getPath());
        }
    }

    @FXML
    private void addConstanteAction(ActionEvent event) {
        constanteTable.getItems().add(new Constante("nome", 0.0));
    }

    @FXML
    private void removeConstanteAction(ActionEvent event) {
        removeSelecteds(constanteTable);
    }

    @FXML
    private void addHeaderAction(ActionEvent event) {
        headerTable.getItems().add(new Constante("equacao", 0.0));
    }

    @FXML
    private void removeHeaderAction(ActionEvent event) {
        removeSelecteds(headerTable);
    }

    @FXML
    private void addBodyAction(ActionEvent event) {
        bodyTable.getItems().add(new Constante("equacao", 0.0));
    }

    @FXML
    private void removeBodyAction(ActionEvent event) {
        removeSelecteds(bodyTable);
    }

    @FXML
    private void editTableAction(CellEditEvent<Constante, String> t) {
        Constante editado = ((Constante) t.getTableView().getItems().get(
                t.getTablePosition().getRow()));
        if (t.getNewValue().matches("[aA-zZ_](\\w+)?")) {
            editado.setNome(t.getNewValue());

        }
    }

    @FXML
    private void editTableDoubleAction(CellEditEvent<Constante, Double> t) {
        Constante editado = ((Constante) t.getTableView().getItems().get(
                t.getTablePosition().getRow()));

        if (t.getTablePosition().getColumn() == 1) {
            editado.setValor(t.getNewValue());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;
        constanteTable.getItems().clear();
        constanteTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        headerTable.getItems().clear();
        headerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        bodyTable.getItems().clear();
        bodyTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Callback<TableColumn, TableCell> cellFactory =
                new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell(bundle, EditingCell.DOUBLE);
            }
        };

        Callback<TableColumn, TableCell> cellFactoryString =
                new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell(bundle);
            }
        };

        Callback<TableColumn, TableCell> cellFactoryEquation =
                new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell(bundle, EditingCell.EQUATION);
            }
        };


        TableColumn tc = (TableColumn) constanteTable.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));
        tc.setCellFactory(cellFactoryString);

        tc = (TableColumn) constanteTable.getColumns().get(1);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, Double>("valor"));
        tc.setCellFactory(cellFactory);

        tc = (TableColumn) headerTable.getColumns().get(0);
        tc.setCellFactory(cellFactoryEquation);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));

        tc = (TableColumn) bodyTable.getColumns().get(0);
        tc.setCellFactory(cellFactoryEquation);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));

        constanteTable.getItems().add(new Constante("julianDay", 248.0));
        constanteTable.getItems().add(new Constante("Z", 50.24));
        constanteTable.getItems().add(new Constante("latitude", -16.56));
        constanteTable.getItems().add(new Constante("Rg_24h", 243.949997));
        constanteTable.getItems().add(new Constante("Uref", 0.92071358));
        constanteTable.getItems().add(new Constante("P", 299.3));
        constanteTable.getItems().add(new Constante("UR", 36.46));
        constanteTable.getItems().add(new Constante("Ta", 32.74));
        constanteTable.getItems().add(new Constante("reflectanciaAtmosfera", 0.03));
        constanteTable.getItems().add(new Constante("Kt", 1.0));
        constanteTable.getItems().add(new Constante("L", 0.1));
        constanteTable.getItems().add(new Constante("K1", 607.76));
        constanteTable.getItems().add(new Constante("K2", 1260.56));
        constanteTable.getItems().add(new Constante("S", 1367.0));
        constanteTable.getItems().add(new Constante("StefanBoltzman", (5.67 * Math.pow(10, -8))));
        constanteTable.getItems().add(new Constante("Tao_24h", 0.63));


        try {
            BufferedReader bur = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/source/landsat.prop"));
            String linha = bur.readLine();

            if (linha.equals("<header>")) {
                linha = bur.readLine();
                while (!linha.equals("<body>")) {
                    headerTable.getItems().add(new Constante(linha, 0.0));
                    linha = bur.readLine();
                }
            }

            linha = bur.readLine();
            while (linha != null) {
                bodyTable.getItems().add(new Constante(linha, 0.0));
                linha = bur.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(SEBALLandSatGUIController.class.getName()).log(Level.SEVERE, null, ex);
        }

        setTask();
    }

    private void removeSelecteds(TableView table) {
        ObservableList<Integer> rem = table.getSelectionModel().getSelectedIndices();
        int idx;
        for (int i = rem.size() - 1; i >= 0; i--) {
            idx = (Integer) rem.get(i);
            table.getItems().remove(idx);
        }
        table.getSelectionModel().clearSelection();
    }

    private void setTask() {
        task = new Task() {
            @Override
            protected Object call() throws Exception {

                progressBar.setVisible(true);
                progressBar.setProgress(-1);
                tabPane.setDisable(true);
                boolean run = true;
                if (image == null) {
                    new AlertDialog(Main.screen, bundle.getString("error.image")).showAndWait();
                    run = false;
                }
                if (bodyTable.getItems().isEmpty()) {
                    new AlertDialog(Main.screen, bundle.getString("error.equation")).showAndWait();
                    run = false;
                }

                if (run) {
                    updateMessage(bundle.getString("execution"));
                    String path = image.getPath();
                    StringBuilder header = new StringBuilder();
                    StringBuilder body = new StringBuilder();

                    for (Constante object : headerTable.getItems()) {
                        header.append(object.getNome()).append("\n");
                    }
                    for (Constante object : bodyTable.getItems()) {
                        body.append(object.getNome()).append("\n");
                    }
                    Map<String, Double> variables = new HashMap<>();
                    for (Constante object : constanteTable.getItems()) {
                        variables.put(object.getNome(), object.getValor());
                    }


                    File tiff = image;
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

                                double[][] calibration = new double[][]{
                                    {-1.52f, 193.0f, 1957.0f},
                                    {-2.84f, 365.0f, 1826.0f},
                                    {-1.17f, 264.0f, 1554.0f},
                                    {-1.51f, 221.0f, 1036.0f},
                                    {-0.37f, 30.2f, 215.0f},
                                    {1.2378f, 15.303f, 1.0f},
                                    {-0.15f, 16.5f, 80.67f}};

//                        double[] parameterAlbedo = new double[]{0.293f, 0.274f, 0.233f, 0.157f, 0.033f, 0.0f, 0.011f};
                                double[] parameterAlbedo = new double[7];

                                double sum = 0;
                                for (int i = 0; i < calibration.length; i++) {
                                    sum += calibration[i][2];
                                }
                                for (int i = 0; i < parameterAlbedo.length; i++) {
                                    parameterAlbedo[i] = calibration[i][2] / sum;
                                }
                                parameterAlbedo[5] = 0;

                                String[] nameParameters = new String[]{"pixel1", "pixel2", "pixel3", "pixel4", "pixel5", "pixel6", "pixel7"};

                                Map<String, double[][]> constMatrix = new HashMap<>();
                                constMatrix.put("calibration", calibration);

                                Map<String, double[]> constVetor = new HashMap<>();
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
}
