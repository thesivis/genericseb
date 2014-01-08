/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

/**
 *
 * @author raphael
 */
public class Equation {

    private String term;
    private String index;
    private String[] condition;
    private String form;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String[] getCondition() {
        return condition;
    }

    public void setCondition(String[] condition) {
        this.condition = condition;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }
}
