/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing.utils;


import java.io.File;

/**
 *
 * @author raphael
 */
public class DataFile {

    private String parameter;
    private File file;

    public DataFile() {
    }

    public DataFile(String parameter, File file) {
        this.parameter = parameter;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
