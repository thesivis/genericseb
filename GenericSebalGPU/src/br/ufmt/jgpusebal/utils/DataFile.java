/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.utils;


import java.io.File;

/**
 *
 * @author raphael
 */
public class DataFile {

    private ParameterEnum parameter;
    private File file;

    public DataFile() {
    }

    public DataFile(ParameterEnum parameter, File file) {
        this.parameter = parameter;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ParameterEnum getParameter() {
        return parameter;
    }

    public void setParameter(ParameterEnum parameter) {
        this.parameter = parameter;
    }
}
