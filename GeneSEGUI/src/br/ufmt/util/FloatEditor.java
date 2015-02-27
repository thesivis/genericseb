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
public class FloatEditor extends DefaultCellEditor {

    private JTextField textField;

    public FloatEditor() {
        super(new JTextField());
    }

    @Override
    public Object getCellEditorValue() {
        Object ret = super.getCellEditorValue();
        if (ret instanceof String) {
            return Float.parseFloat((String) ret);
        }
        return ret; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean stopCellEditing() {
        textField = (JTextField) getComponent();
        String numero = textField.getText();

        if (!numero.matches("(-?)[0-9]+([\\.][0-9]+([E](-?)[0-9+])?)?")) {
            JOptionPane.showMessageDialog(textField, Util.getMessage("error.number"));
            return false;
        }

        return super.stopCellEditing(); //To change body of generated methods, choose Tools | Templates.
    }

}
