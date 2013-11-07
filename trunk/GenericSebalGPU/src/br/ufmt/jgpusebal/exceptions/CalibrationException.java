/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jgpusebal.exceptions;


/**
 *
 * @author raphael
 */
public class CalibrationException extends RuntimeException {

    /**
     * Constructs an instance of <code>CalibrationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CalibrationException() {
        super("Table of calibration error!");
    }
}