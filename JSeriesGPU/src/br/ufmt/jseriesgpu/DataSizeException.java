/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jseriesgpu;

/**
 *
 * @author raphael
 */
public class DataSizeException extends RuntimeException {

    public DataSizeException() {
        super("Data Size is too big for this GPU!");
    }
}
