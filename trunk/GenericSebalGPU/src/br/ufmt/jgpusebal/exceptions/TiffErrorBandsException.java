/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.exceptions;


/**
 *
 * @author raphael
 */
public class TiffErrorBandsException extends RuntimeException {

    /**
     * Constructs an instance of <code>TiffErrorBandsException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TiffErrorBandsException() {
        super("Quantidade de bandas insuficientes!");
    }
}