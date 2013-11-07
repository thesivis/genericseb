/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jseriesgpu;

import java.util.Date;

/**
 *
 * @author raphael
 */
public class MeasureTimeGPU {

    private Date begin;
    private Date end;
    private long beginLong;
    private long endLong;
    private float time;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public long getDiferenceNano() {
        return (endLong - beginLong);
    }

    public Date getDiference() {
        return new Date(end.getTime() - begin.getTime());
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public long getEndLong() {
        return endLong;
    }

    public void setEndLong(long endLong) {
        this.endLong = endLong;
    }

    public long getBeginLong() {
        return beginLong;
    }

    public void setBeginLong(long beginLong) {
        this.beginLong = beginLong;
    }
}
