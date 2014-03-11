/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

/**
 *
 * @author raphael
 */
public class VariableValue {
    
    private String name;
    private float[] data;

    public VariableValue() {
    }

    public VariableValue(String name, float[] data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float[] getData() {
        return data;
    }

    public void setData(float[] data) {
        this.data = data;
    }
    
}
