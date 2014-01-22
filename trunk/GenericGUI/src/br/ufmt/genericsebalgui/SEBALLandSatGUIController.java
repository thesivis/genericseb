/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgui;

import br.ufmt.genericgui.Main;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import br.ufmt.utils.EditingCell;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
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
    private void runButtonAction(ActionEvent event) {
        boolean run = true;
        if(image == null){
            new AlertDialog(Main.screen, bundle.getString("error.image")).showAndWait();
            run = false;
        }
        if(bodyTable.getItems().isEmpty()){
            new AlertDialog(Main.screen, bundle.getString("error.equation")).showAndWait();
            run = false;
        }
        
        if(run){
            
        }
    }

    @FXML
    private void uploadButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
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


        TableColumn tc = (TableColumn) constanteTable.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));
        tc.setCellFactory(cellFactoryString);

        tc = (TableColumn) constanteTable.getColumns().get(1);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, Double>("valor"));
        tc.setCellFactory(cellFactory);

        tc = (TableColumn) headerTable.getColumns().get(0);
        tc.setCellFactory(cellFactoryString);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));

        tc = (TableColumn) bodyTable.getColumns().get(0);
        tc.setCellFactory(cellFactoryString);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));

        constanteTable.getItems().add(new Constante("julianDay", 248.0));
        constanteTable.getItems().add(new Constante("Z", 50.24));
        constanteTable.getItems().add(new Constante("latitude", -16.56));
        constanteTable.getItems().add(new Constante("Rg_24h", 243.949997));
        constanteTable.getItems().add(new Constante("Uref", 0.92071358));
        constanteTable.getItems().add(new Constante("P", 299.3));
        constanteTable.getItems().add(new Constante("UR", 36.46));
        constanteTable.getItems().add(new Constante("Ta", 32.74));

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
}
