/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericlexerseb;

/**
 *
 * @author raphael
 */
public class Variable implements Comparable<Variable>{
    
    private String name;
    private Object value;

    public Variable() {
    }

    public Variable(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    public Variable(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int compareTo(Variable o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Variable){
            Variable o = (Variable) obj;
            return this.name.equals(o.name);
        }
        return false; //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
