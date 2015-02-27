/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.util;

import br.ufmt.genericlexerseb.ExpressionParser;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author raphael
 */
public class Util {

    public static String getMessage(String key) {
        return java.util.ResourceBundle.getBundle("br/ufmt/bundles/messages").getString(key);
    }

    public static GeneSEBean open(File file) {
        GeneSEBean ret = new GeneSEBean();
        if (file != null) {
            try {
                BufferedReader bur = new BufferedReader(new FileReader(file));
                String line = bur.readLine();
                if (line != null && line.equals(Names.CONSTANT)) {
                    String[] vet;
                    line = bur.readLine();
                    while (line != null && (!line.equals(Names.FOR_VARIABLES) && !line.equals(Names.FOR_EACH_VALUE))) {
                        vet = line.split("=");
                        if (vet[1].matches("(-?)[0-9]+([\\.][0-9]+([E](-?)[0-9+])?)?")) {
                            ret.addConstant(new Constant(vet[0], Float.parseFloat(vet[1])));
                        }
                        line = bur.readLine();
                    }
                }
                if (line != null && line.equals(Names.FOR_VARIABLES)) {
                    line = bur.readLine();
                    while (line != null && (!line.equals(Names.FOR_EACH_VALUE))) {
                        if (isEquationValid(line)) {
                            ret.addForVariable(line);
                        }
                        line = bur.readLine();
                    }
                }
                if (line != null && line.equals(Names.FOR_EACH_VALUE)) {
                    line = bur.readLine();
                    while (line != null) {
                        if (isEquationValid(line)) {
                            ret.addForEachValue(line);
                        }
                        line = bur.readLine();
                    }
                }
                bur.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }

    public static void save(File file, GeneSEBean bean) {
        if (file != null) {
            try {
                PrintWriter pw = new PrintWriter(file);
                pw.println(Names.CONSTANT);
                for (Constant object : bean.getConstants()) {
                    pw.println(object.getName() + "=" + object.getValue());
                }
                pw.println(Names.FOR_VARIABLES);
                for (String object : bean.getForVariable()) {
                    pw.println(object);
                }
                pw.println(Names.FOR_EACH_VALUE);
                for (String object : bean.getForEachValue()) {
                    pw.println(object);
                }
                pw.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static File uploadFile(final String[] extensions, Component parent) {
        StringBuilder aux = new StringBuilder("");
        for (int i = 0; i < extensions.length; i++) {
            String string = extensions[i];
            aux.append("*.").append(string);
            if (i < extensions.length - 1) {
                aux.append(",");
            }
        }
        final String description = aux.toString();
        JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setDialogTitle(getMessage("file.chooser.title"));
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String[] vet = f.getName().split("\\.");
                String extensionFile = vet[vet.length - 1];
                for (String extension : extensions) {
                    if (extension.equals(extensionFile)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return description;
            }
        });
        int ret = chooser.showOpenDialog(parent);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    public static void removeTable(JTable table) {
        DefaultTableModel dfm = (DefaultTableModel) table.getModel();
        int[] lines = table.getSelectedRows();
        for (int i = lines.length - 1; i >= 0; i--) {
            dfm.removeRow(lines[i]);
        }
    }

    public static void addLineTable(JTable table, int numCols) {
        DefaultTableModel dfm = (DefaultTableModel) table.getModel();

        Object[] line = new Object[numCols];
        for (int i = 0; i < line.length; i++) {
            line[i] = "";
        }
        dfm.addRow(line);
    }

    public static void setBean(GeneSEBean bean, JTable constantjTable, JTable forVariablejTable, JTable forEachValuejTable) {
        List<Constant> constants = bean.getConstants();
        DefaultTableModel dfm;
        if (constantjTable != null) {
            clearTable(constantjTable);
            dfm = (DefaultTableModel) constantjTable.getModel();
            for (Constant constant : constants) {
                dfm.addRow(new Object[]{constant.getName(), constant.getValue()});
            }
        }

        if (forVariablejTable != null) {
            clearTable(forVariablejTable);
            List<String> forVariable = bean.getForVariable();
            dfm = (DefaultTableModel) forVariablejTable.getModel();
            for (String string : forVariable) {
                dfm.addRow(new Object[]{string});
            }
        }

        if (forEachValuejTable != null) {
            clearTable(forEachValuejTable);
            List<String> forEachValue = bean.getForEachValue();
            dfm = (DefaultTableModel) forEachValuejTable.getModel();
            for (String string : forEachValue) {
                dfm.addRow(new Object[]{string});
            }
        }
    }

    public static GeneSEBean getBean(JTable constantjTable, JTable forVariablejTable, JTable forEachValuejTable) {
        GeneSEBean bean = new GeneSEBean();

        if (constantjTable != null) {
            Constant constant;
            for (int i = 0; i < constantjTable.getRowCount(); i++) {
                constant = new Constant((String) constantjTable.getValueAt(i, 0), (Float) constantjTable.getValueAt(i, 1));
                bean.addConstant(constant);
            }
        }

        if (forVariablejTable != null) {
            for (int i = 0; i < forVariablejTable.getRowCount(); i++) {
                bean.addForVariable((String) forVariablejTable.getValueAt(i, 0));
            }
        }

        if (forEachValuejTable != null) {
            for (int i = 0; i < forEachValuejTable.getRowCount(); i++) {
                bean.addForEachValue((String) forEachValuejTable.getValueAt(i, 0));
            }
        }
        return bean;
    }

    public static void clearTable(JTable table) {
        DefaultTableModel dfm = (DefaultTableModel) table.getModel();
        int lines = dfm.getRowCount();
        for (int i = 0; i < lines; i++) {
            dfm.removeRow(0);
        }
    }

    public static boolean isEquationValid(String equation) {
        boolean right = false;
        ExpressionParser exp = new ExpressionParser();
        equation = equation.replaceAll("[ ]+", "");

        if (equation.matches("[aA-zZ_][aA-zZ_0-9]*[_(]?[aA-zZ_0-9+-/()\\.\\*=><!]*[)]?[=][aA-zZ_0-9+-/()\\.\\*]+")) {
            String[] vet;
            if (equation.contains(")=")) {
                vet = equation.split("[)]=");
                vet[0] += ")";
            } else {
                vet = equation.split("=");
            }

            if (vet[0].startsWith("O_")) {
                vet[0] = vet[0].substring(2);
            }
            if (vet[0].contains("_(")) {

                String ifTest = vet[0].substring(vet[0].indexOf("_(") + 2, vet[0].length() - 1);
                vet[0] = vet[0].substring(0, vet[0].indexOf("_("));
                try {
                    right = exp.evaluateExprIf(ifTest);

                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    right = false;
                }
            } else {
                right = true;
            }
            if (vet.length == 2) {
                if (right) {
                    equation = vet[0] + "=" + vet[1];
                    try {
                        right = exp.evaluateExpr(equation);
                    } catch (IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                        right = false;
                    }
                }
            } else {
                right = false;
            }
        } else {
            right = false;
        }
        return right;
    }

    public static void moveTableUp(JTable table) {
        int[] lines = table.getSelectedRows();
        if (lines.length > 0 && lines[0] > 0) {
            DefaultTableModel dfm = (DefaultTableModel) table.getModel();
            table.getSelectionModel().clearSelection();
            for (int i = 0; i < lines.length; i++) {
                int j = lines[i];
                if (j > 0) {
                    int newLine = j - 1;
                    dfm.moveRow(j, j, newLine);
                    table.getSelectionModel().addSelectionInterval(newLine, newLine);
                } else {
                    break;
                }
            }
        }
    }

    public static void moveTableDown(JTable table) {
        int[] lines = table.getSelectedRows();
        if (lines.length > 0 && lines[lines.length - 1] < table.getRowCount() - 1) {
            DefaultTableModel dfm = (DefaultTableModel) table.getModel();
            table.getSelectionModel().clearSelection();
            for (int i = lines.length - 1; i >= 0; i--) {
                int j = lines[i];
                if (j < table.getRowCount() - 1) {
                    int newLine = j + 1;
                    dfm.moveRow(j, j, newLine);
                    table.getSelectionModel().addSelectionInterval(newLine, newLine);
                } else {
                    break;
                }
            }
        }
    }

}
