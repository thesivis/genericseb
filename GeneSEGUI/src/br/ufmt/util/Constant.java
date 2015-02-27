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
public class Constant extends Name {

    private Float value;

    public Constant() {
    }

    public Constant(String name, Float value) {
        super(name);
        this.value = value;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

}
