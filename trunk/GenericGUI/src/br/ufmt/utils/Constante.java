/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.utils;

import java.io.File;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author raphael
 */
public class Constante {

    private SimpleStringProperty nome;
    private SimpleDoubleProperty valor;
    private SimpleDoubleProperty valor2;
    private SimpleDoubleProperty valor3;

    public Constante(String nome, Double valor) {
        this.nome = new SimpleStringProperty(nome);
        this.valor = new SimpleDoubleProperty(valor);
    }

    public Constante(String nome, Double valor, Double valor2, Double valor3) {
        this(nome, valor);
        this.valor2 = new SimpleDoubleProperty(valor2);
        this.valor3 = new SimpleDoubleProperty(valor3);
    }

    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public Double getValor() {
        return valor.get();
    }

    public void setValor(Double valor) {
        this.valor.set(valor);
    }

    public Double getValor2() {
        return valor2.get();
    }

    public void setValor2(Double valor) {
        this.valor2.set(valor);
    }

    public Double getValor3() {
        return valor3.get();
    }

    public void setValor3(Double valor) {
        this.valor3.set(valor);
    }
}
