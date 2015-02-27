/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.util;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author raphael
 */
public class VariableEditor extends DefaultCellEditor {

    private JTextField textField;

    public VariableEditor() {
        super(new JTextField());
    }

    @Override
    public boolean stopCellEditing() {
        textField = (JTextField) getComponent();
        String numero = textField.getText();

        if (!numero.matches("[aA-zZ_](\\w+)?")) {
            JOptionPane.showMessageDialog(textField, Util.getMessage("error.variable"));
            return false;
        }

        return super.stopCellEditing(); //To change body of generated methods, choose Tools | Templates.
    }

}
