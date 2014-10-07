/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author raphael
 */
public class GPU {

    protected boolean manual = false;
    protected boolean print = false;
    protected boolean ExceptionsEnabled = true;
    protected boolean measure = false;
    protected HashMap<EnumMeasure, MeasureTimeGPU> measures = new HashMap<EnumMeasure, MeasureTimeGPU>();
    protected MeasureTimeGPU time;
    protected MeasureTimeGPU allTimes;
    public static Cores[] cores;

    static {
        cores = new Cores[]{
            new Cores(0x10, 8),
            new Cores(0x11, 8), // Tesla Generation (SM 1.1) G8x class
            new Cores(0x12, 8), // Tesla Generation (SM 1.2) G9x class
            new Cores(0x13, 8), // Tesla Generation (SM 1.3) GT200 class
            new Cores(0x20, 32), // Fermi Generation (SM 2.0) GF100 class
            new Cores(0x21, 48), // Fermi Generation (SM 2.1) GF10x class
            new Cores(0x30, 192), // Kepler Generation (SM 3.0) GK10x class
            new Cores(0x32, 192), // Kepler Generation (SM 3.2) GK10x class
            new Cores(0x35, 192), // Kepler Generation (SM 3.5) GK11x class
            new Cores(0x50, 128), // Maxwell Generation (SM 5.0) GM10x class
            new Cores(-1, -1)
        };
    }

    public boolean isExceptionsEnabled() {
        return ExceptionsEnabled;
    }

    public void setExceptionsEnabled(boolean ExceptionsEnabled) {
        this.ExceptionsEnabled = ExceptionsEnabled;
    }

    public boolean isPrint() {
        return print;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public boolean isMeasure() {
        return measure;
    }

    public void setMeasure(boolean measure) {
        this.measure = measure;
    }

    public HashMap<EnumMeasure, MeasureTimeGPU> getMeasures() {
        return measures;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public static String createString(byte bytes[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            char c = (char) bytes[i];
            if (c == 0) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // Beginning of GPU Architecture definitions
    public static int convertSMVer2Cores(int major, int minor) {
        // Defines for GPU Architecture types (using the SM version to determine the # of cores per SM
        int index = 0;
        while (cores[index].SM != -1) {
            if (cores[index].SM == ((major << 4) + minor)) {
                return cores[index].cores;
            }

            index++;
        }
        return cores[7].cores;
    }
}
