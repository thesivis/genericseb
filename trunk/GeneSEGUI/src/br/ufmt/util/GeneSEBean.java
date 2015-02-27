/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author raphael
 */
public class GeneSEBean {

    private List<Constant> constants = new ArrayList<Constant>();
    private List<String> forVariable = new ArrayList<String>();
    private List<String> forEachValue = new ArrayList<String>();

    public List<Constant> getConstants() {
        return constants;
    }

    public void setConstants(List<Constant> constants) {
        this.constants = constants;
    }

    public List<String> getForVariable() {
        return forVariable;
    }

    public void setForVariable(List<String> forVariable) {
        this.forVariable = forVariable;
    }

    public List<String> getForEachValue() {
        return forEachValue;
    }

    public void setForEachValue(List<String> forEachValue) {
        this.forEachValue = forEachValue;
    }

    public void addConstant(Constant constant) {
        this.constants.add(constant);
    }

    public void addForVariable(String forVariable) {
        this.forVariable.add(forVariable);
    }

    public void addForEachValue(String forEachValue) {
        this.forEachValue.add(forEachValue);
    }

}
