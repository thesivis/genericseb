/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

/**
 *
 * @author raphael
 */
public class Occupancy implements Comparable<Occupancy> {

    private float ratioOccupancy;
    private int threadsPerBlock;

    public Occupancy(float ratioOccupancy, int threadsPerBlock) {
        this.ratioOccupancy = ratioOccupancy;
        this.threadsPerBlock = threadsPerBlock;
    }

    public float getRatioOccupancy() {
        return ratioOccupancy;
    }

    public void setRatioOccupancy(float ratioOccupancy) {
        this.ratioOccupancy = ratioOccupancy;
    }

    public int getThreadsPerBlock() {
        return threadsPerBlock;
    }

    public void setThreadsPerBlock(int threadsPerBlock) {
        this.threadsPerBlock = threadsPerBlock;
    }

    @Override
    public int compareTo(Occupancy o) {
        if (this.ratioOccupancy == o.ratioOccupancy) {
            if (this.threadsPerBlock > o.threadsPerBlock) {
                return 1;
            }
            return -1;
        } else if (this.ratioOccupancy > o.ratioOccupancy) {
            return 1;
        } else {
            return -1;
        }
    }
}
