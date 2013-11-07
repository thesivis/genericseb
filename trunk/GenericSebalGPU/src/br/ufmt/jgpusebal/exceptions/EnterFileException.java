/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.exceptions;
/**
 *
 * @author raphael
 */
public class EnterFileException extends RuntimeException {

    /**
     * Creates a new instance of <code>EntradaException</code> without detail message.
     */
    public EnterFileException() {
        super("Quantidade de arquivos de entrada é insuficiente!");
    }

}

