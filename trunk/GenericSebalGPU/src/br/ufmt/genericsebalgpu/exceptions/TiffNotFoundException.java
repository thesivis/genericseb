/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.exceptions;
/**
 *
 * @author raphael
 */
public class TiffNotFoundException extends RuntimeException {

    /**
     * Constructs an instance of <code>TiffNotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TiffNotFoundException() {
        super("TIFF not found!");
    }
}
