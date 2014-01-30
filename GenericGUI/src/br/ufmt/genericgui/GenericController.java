/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericgui;

import br.ufmt.utils.Constante;
import br.ufmt.utils.EditingCell;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 *
 * @author raphael
 */
public abstract class GenericController implements Initializable {

    protected ResourceBundle bundle;
    @FXML
    protected Label nomeArquivoLabel;
    @FXML
    protected TableView<Constante> constanteTable;
    @FXML
    protected TableView<Constante> headerTable;
    @FXML
    protected TableView<Constante> bodyTable;
    protected File file;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected TabPane tabPane;
    protected Task task;
    protected String[] extensions;

    @FXML
    protected void runButtonAction(ActionEvent event) {
        new Thread(task).start();
    }

    @FXML
    protected void uploadButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("extension"), extensions));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(bundle.getString("file.chooser.title"));
        file = fileChooser.showOpenDialog(Main.screen);
        if (file != null) {
            nomeArquivoLabel.setText(file.getPath());
            afterUpload();
        }
    }

    @FXML
    protected void addConstanteAction(ActionEvent event) {
        constanteTable.getItems().add(new Constante("nome", 0.0f));
    }

    @FXML
    protected void removeConstanteAction(ActionEvent event) {
        removeSelecteds(constanteTable);
    }

    @FXML
    protected void addHeaderAction(ActionEvent event) {
        headerTable.getItems().add(new Constante("equacao", 0.0f));
    }

    @FXML
    protected void removeHeaderAction(ActionEvent event) {
        removeSelecteds(headerTable);
    }

    @FXML
    protected void addBodyAction(ActionEvent event) {
        bodyTable.getItems().add(new Constante("equacao", 0.0f));
    }

    @FXML
    protected void removeBodyAction(ActionEvent event) {
        removeSelecteds(bodyTable);
    }

    @FXML
    protected void editTableAction(TableColumn.CellEditEvent<Constante, String> t) {
        Constante editado = ((Constante) t.getTableView().getItems().get(
                t.getTablePosition().getRow()));
        if (t.getNewValue().matches("[aA-zZ_](\\w+)?")) {
            editado.setNome(t.getNewValue());

        }
    }

    @FXML
    protected void editTableDoubleAction(TableColumn.CellEditEvent<Constante, Float> t) {
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
        tc.setCellValueFactory(new PropertyValueFactory<Constante, Float>("valor"));
        tc.setCellFactory(cellFactory);

        tc = (TableColumn) headerTable.getColumns().get(0);
        tc.setCellFactory(cellFactoryEquation);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));

        tc = (TableColumn) bodyTable.getColumns().get(0);
        tc.setCellFactory(cellFactoryEquation);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));
        
        inicializated();
        
        setTask();
    }

    protected void removeSelecteds(TableView table) {
        ObservableList<Integer> rem = table.getSelectionModel().getSelectedIndices();
        int idx;
        for (int i = rem.size() - 1; i >= 0; i--) {
            idx = (Integer) rem.get(i);
            table.getItems().remove(idx);
        }
        table.getSelectionModel().clearSelection();
    }

    protected abstract  void setTask();
    
    protected abstract void inicializated();
    
    protected void afterUpload(){
        
    }
}
