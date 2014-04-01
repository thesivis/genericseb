/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericcsv;

import br.ufmt.genericgui.GenericController;
import br.ufmt.genericgui.Main;
import br.ufmt.genericlexerseb.LanguageType;
import br.ufmt.genericseb.GenericSEB;
import br.ufmt.genericseb.VariableValue;
import br.ufmt.utils.AlertDialog;
import br.ufmt.utils.Constante;
import br.ufmt.utils.EditingCell;
import br.ufmt.utils.Names;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author raphael
 */
public class GenericCSVController extends GenericController {

    @FXML
    protected Label nomeArquivoLabel;
    @FXML
    protected TableView<Constante> columnsTable;
    private BufferedReader bur;
    private String line;
    private String delimiter = ";";
    @FXML
    private AnchorPane anchorPane;

    public GenericCSVController() {
        extensions = new String[]{"*.csv"};
        extensionsConf = new String[]{"*.propcsv"};
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
                    Map<String, Float> constants = new HashMap<>();
                    for (Constante object : constanteTable.getItems()) {
                        constants.put(object.getNome(), object.getValor());
                    }

                    File csv = file;
                    if (csv.exists() && csv.getName().endsWith(".csv")) {
                        try {
                            ArrayList<List<Float>> datas = new ArrayList<>();
                            int tam = columnsTable.getItems().size();
                            int size = 0;
                            for (int i = 0; i < tam; i++) {
                                datas.add(new ArrayList<Float>());
                            }
                            line = bur.readLine();
                            String[] vet;
                            while (line != null) {
                                vet = line.split(delimiter);
//                            System.out.println("Line:"+line);
                                for (int i = 0; i < vet.length; i++) {
                                    datas.get(i).add(Float.parseFloat(vet[i]));
                                }
                                line = bur.readLine();
                            }
                            bur.close();

                            List<VariableValue> parameters = new ArrayList<>();

                            float[] d;
                            for (int i = 0; i < tam; i++) {
                                d = new float[datas.get(i).size()];
                                size = datas.get(i).size();
                                for (int j = 0; j < d.length; j++) {
                                    d[j] = datas.get(i).get(j);
                                }
                                parameters.add(new VariableValue(columnsTable.getItems().get(i).getNome(), d));
                            }

                            GenericSEB g = new GenericSEB(LanguageType.JAVA);
                            Map<String, float[]> datum = g.execute(header.toString(), body.toString(), parameters, constants);

                            PrintWriter pw = new PrintWriter(file.getParent() + "/" + file.getName().substring(0, file.getName().length() - 3) + "Resp.csv");

                            Set<String> keys = datum.keySet();
                            StringBuilder newLine;
                            newLine = new StringBuilder();
                            for (String string : keys) {
                                newLine.append(string).append(";");
                            }
                            pw.println(newLine.toString().substring(0, newLine.toString().length() - 1));
                            for (int i = 0; i < size; i++) {
                                newLine = new StringBuilder();
                                for (String string : keys) {
                                    newLine.append(datum.get(string)[i]).append(";");
                                }
                                pw.println(newLine.toString().substring(0, newLine.toString().length() - 1));
                            }

                            pw.close();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            new AlertDialog(Main.screen, ex.getMessage()).showAndWait();
                        }
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
        Callback<TableColumn, TableCell> cellFactoryString = new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell(bundle);
            }
        };
        TableColumn tc = (TableColumn) columnsTable.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<Constante, String>("nome"));
        tc.setCellFactory(cellFactoryString);
    }

    @Override
    protected void afterUpload() {
        try {
            nomeArquivoLabel.setText(file.getPath());
            bur = new BufferedReader(new FileReader(file));
            line = bur.readLine();
            line = line.replaceAll("[ ]+", "");
            line = line.replace("\"", "");
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
                variable = vet[i].replaceAll("[^\\w]", "");
                columnsTable.getItems().add(new Constante(variable, 0.0f));
            }

        } catch (IOException ex) {
            Logger.getLogger(GenericCSVController.class.getName()).log(Level.SEVERE, null, ex);
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
                while (line != null && (!line.equals(Names.FOR_VARIABLES) && !line.equals(Names.FOR_EACH_VALUE))) {
                    vet = line.split("=");
                    try {
                        constanteTable.getItems().add(new Constante(vet[0], Float.parseFloat(vet[1])));
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
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
            Logger.getLogger(GenericCSVController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GenericCSVController.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(GenericCSVController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void clear() {
        columnsTable.getItems().clear();
        line = null;
        bur = null;
        delimiter = ";";
        nomeArquivoLabel.setText("");
    }
}
