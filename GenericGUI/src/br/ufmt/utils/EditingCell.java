/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.utils;

import br.ufmt.genericgui.Main;
import br.ufmt.genericlexerseb.ExpressionParser;
import com.sun.prism.impl.Disposer;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author raphael
 */
public class EditingCell extends TableCell<Disposer.Record, Object> {

    private TextField textField;
    private int type;
    public static final int DOUBLE = 0;
    public static final int VARIABLE = 1;
    public static final int EQUATION = 2;
    private ResourceBundle bundle;

    public EditingCell(ResourceBundle bundle) {
        this(bundle, VARIABLE);
    }

    public EditingCell(ResourceBundle bundle, int type) {
        this.bundle = bundle;
        this.type = type;
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }

        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(String.valueOf(getItem()));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }

                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {

                    switch (type) {
                        case DOUBLE:
                            if (textField.getText().matches("(-?)[0-9]+([\\.][0-9]+)?")) {
                                commitEdit(Float.parseFloat(textField.getText()));
                            } else {
                                new AlertDialog(Main.screen, bundle.getString("error.number")).showAndWait();
                            }
                            break;
                        case EQUATION:
                            boolean right = false;
                            ExpressionParser exp = new ExpressionParser();
                            String equation = textField.getText();
                            equation = equation.replaceAll("[ ]+", "");
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
                                    new AlertDialog(Main.screen, e.getMessage()).showAndWait();
                                    right = false;
                                }
                            }else{
                                right = true;
                            }
                            if (right) {
                                equation = vet[0] + "=" + vet[1];
                                try {
                                    right = exp.evaluateExpr(equation);
                                } catch (IllegalArgumentException e) {
                                    new AlertDialog(Main.screen, e.getMessage()).showAndWait();
                                    right = false;
                                }
                            }

                            if (right) {
                                commitEdit(textField.getText());
                            } else {
                                new AlertDialog(Main.screen, bundle.getString("error.equations")).showAndWait();
                            }
                            break;
                        default:
                            if (textField.getText().matches("[aA-zZ_](\\w+)?")) {
                                commitEdit(textField.getText());
                            } else {
                                new AlertDialog(Main.screen, bundle.getString("error.variable")).showAndWait();
                            }
                            break;
                    }


                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
