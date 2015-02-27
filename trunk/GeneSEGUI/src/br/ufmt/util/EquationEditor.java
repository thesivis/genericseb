/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.util;

import br.ufmt.genericlexerseb.ExpressionParser;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author raphael
 */
public class EquationEditor extends DefaultCellEditor {

    private JTextField textField;

    public EquationEditor() {
        super(new JTextField());

    }

    @Override
    public boolean stopCellEditing() {
        textField = (JTextField) getComponent();
        String equation = textField.getText();
        boolean right = Util.isEquationValid(equation);
        if (!right) {
            JOptionPane.showMessageDialog(textField, Util.getMessage("error.equations"));
            return false;
        }

        return super.stopCellEditing(); //To change body of generated methods, choose Tools | Templates.
    }

}
