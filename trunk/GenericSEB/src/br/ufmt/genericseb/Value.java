/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

/**
 *
 * @author raphael
 */
public class Value {
    
    private String name;
    private double[] data;

    public Value() {
    }

    public Value(String name, double[] data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }
    
}
