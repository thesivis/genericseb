/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebgui;

import br.ufmt.genericgui.GenericController;
import br.ufmt.genericgui.Main;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericseb.GenericSEB;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import br.ufmt.utils.EditingCell;
import br.ufmt.utils.Image;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author raphael
 */
public class GenericSEBController extends GenericController {

    @FXML
    protected TableView<Image> filesTable;

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
            try {
                s = new FileSeekableStream(file);
                TIFFDecodeParam param = null;
                ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
                int bands;
                Raster raster = dec.decodeAsRaster(0);
                bands = raster.getNumBands();

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

            } catch (IOException ex) {
                Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @FXML
    protected void removeFileAction(ActionEvent event) {
        removeSelecteds(filesTable);
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
                    Map<String, Double> constants = new HashMap<>();
                    for (Constante object : constanteTable.getItems()) {
                        constants.put(object.getNome(), object.getValor());
                    }

                    try {

                        Map<String, double[]> parameters = new HashMap<>();


                        GenericSEB g = new GenericSEB(LanguageType.JAVA);
                        Map<String, double[]> datum = g.execute(header.toString(), body.toString(), parameters, constants);



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

    @Override
    protected void inicializated() {
        filesTable.getItems().clear();
        Callback<TableColumn, TableCell> cellFactoryString =
                new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell(bundle);
            }
        };
        TableColumn tc = (TableColumn) filesTable.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<Image, String>("valor"));
        tc.setCellFactory(cellFactoryString);

        tc = (TableColumn) filesTable.getColumns().get(1);
        tc.setCellValueFactory(new PropertyValueFactory<Image, String>("nome"));

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
            Logger.getLogger(GenericSEBController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
