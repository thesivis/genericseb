/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

/**
 *
 * @author raphael
 */
public class VariableValue<T> {
    
    private String name;
    private T data;
    
    public VariableValue() {
    }

    public VariableValue(String name, T data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
    
}
