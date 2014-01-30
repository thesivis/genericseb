/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.utils;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author raphael
 */
public class Constante {

    private SimpleStringProperty nome;
    private SimpleFloatProperty valor;
    private SimpleFloatProperty valor2;
    private SimpleFloatProperty valor3;

    public Constante(String nome, Float valor) {
        this.nome = new SimpleStringProperty(nome);
        this.valor = new SimpleFloatProperty(valor);
    }

    public Constante(String nome, Float valor, Float valor2, Float valor3) {
        this(nome, valor);
        this.valor2 = new SimpleFloatProperty(valor2);
        this.valor3 = new SimpleFloatProperty(valor3);
    }

    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public Float getValor() {
        return valor.get();
    }

    public void setValor(Float valor) {
        this.valor.set(valor);
    }

    public Float getValor2() {
        return valor2.get();
    }

    public void setValor2(Float valor) {
        this.valor2.set(valor);
    }

    public Float getValor3() {
        return valor3.get();
    }

    public void setValor3(Float valor) {
        this.valor3.set(valor);
    }
}
