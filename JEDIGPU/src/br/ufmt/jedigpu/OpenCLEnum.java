/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import static org.jocl.CL.CL_DEVICE_TYPE_CPU;
import static org.jocl.CL.CL_DEVICE_TYPE_GPU;

/**
 *
 * @author raphael
 */
public enum OpenCLEnum {

    GPU, CPU;

    public long getType() {
        switch (this) {
            case CPU:
                return CL_DEVICE_TYPE_CPU;
            default:
                return CL_DEVICE_TYPE_GPU;
        }
    }

}
