/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.util;

/**
 *
 * @author raphael
 */
public class Equation extends Name {

    private String value;

    public Equation() {
    }

    public Equation(String value, String name) {
        super(name);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value; //To change body of generated methods, choose Tools | Templates.
    }

}
