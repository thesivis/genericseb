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
public class Image extends Equation {

    private File file;

    public Image(String name, String value, File file) {
        super(value, name);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return getName(); //To change body of generated methods, choose Tools | Templates.
    }

}
