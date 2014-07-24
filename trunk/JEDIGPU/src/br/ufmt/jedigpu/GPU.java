/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import java.util.ArrayList;

/**
 *
 * @author raphael
 */
public class GPU {

    protected boolean manual = false;
    protected boolean print = false;
    protected boolean ExceptionsEnabled = true;
    protected boolean measure = false;
    protected ArrayList<MeasureTimeGPU> measures = new ArrayList<MeasureTimeGPU>();
    protected MeasureTimeGPU time;
    protected MeasureTimeGPU allTimes;
    
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

    public ArrayList<MeasureTimeGPU> getMeasures() {
        return measures;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }
}
