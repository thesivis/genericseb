/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.utils;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author raphael
 */
public class Constante {

    private SimpleStringProperty nome;
    private SimpleDoubleProperty valor;

    public Constante(String nome, Double valor) {
        this.nome = new SimpleStringProperty(nome);
        this.valor = new SimpleDoubleProperty(valor);
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
}
