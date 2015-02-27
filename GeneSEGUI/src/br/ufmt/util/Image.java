/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.util;

import java.io.File;

/**
 *
 * @author raphael
 */
public class Image extends Name {

    private File file;
    private String value;

    public Image(String name, String value, File file) {
        super(name);
        this.file = file;
        this.value = value;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
