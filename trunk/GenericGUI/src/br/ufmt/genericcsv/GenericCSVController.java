/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericcsv;

import br.ufmt.genericgui.GenericController;
import br.ufmt.genericgui.Main;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author raphael
 */
public class GenericCSVController extends GenericController {

    @FXML
    protected TableView<Constante> columnsTable;
    private BufferedReader bur;
    private String line;

    public GenericCSVController() {
        extensions = new String[]{"*.csv"};
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
                    new AlertDialog(Main.screen, bundle.getString("error.csv")).showAndWait();
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
                    Map<String, Double> variables = new HashMap<>();
                    for (Constante object : constanteTable.getItems()) {
                        variables.put(object.getNome(), object.getValor());
                    }


                    File csv = file;
                    if (csv.exists() && csv.getName().endsWith(".csv")) {
                    } else {
                        throw new Exception(bundle.getString("error.csv"));
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
        columnsTable.getItems().clear();
        TableColumn tc = (TableColumn) columnsTable.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));
    }

    @Override
    protected void afterUpload() {
        try {
            bur = new BufferedReader(new FileReader(file));
            line = bur.readLine();
            line = line.replaceAll("[ ]+", "");
            line = line.replace("\"", "");
            String delimiter = ";";
            if (!line.contains(delimiter)) {
                if (line.contains(",")) {
                    delimiter = ",";
                }
            } else {
                line = line.replace(",", ".");
            }

            String[] vet = line.split(delimiter);
            String variable;
            for (int i = 0; i < vet.length; i++) {
                variable = vet[i].replaceAll("[^\\w]","");
                columnsTable.getItems().add(new Constante(variable, 0.0));
            }

        } catch (IOException ex) {
            Logger.getLogger(GenericCSVController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
