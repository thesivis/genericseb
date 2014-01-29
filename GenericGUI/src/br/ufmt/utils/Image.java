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
public class Image {
    private SimpleStringProperty nome;
    private SimpleStringProperty valor;
    private File file;

    public Image(String nome, String valor, File file) {
        this.nome = new SimpleStringProperty(nome);
        this.valor = new SimpleStringProperty(valor);
        this.file = file;
    }

    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public String getValor() {
        return valor.get();
    }

    public void setValor(String valor) {
        this.valor.set(valor);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
