/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

/**
 *
 * @author raphael
 */
public class Device {

    private String name;
    private int cores;
    private Object device;
    private Object plataform;

    public Device() {
    }

    public Device(String name, int cores, Object device) {
        this.name = name;
        this.cores = cores;
        this.device = device;
    }

    public Device(String name, int cores, Object device, Object plataform) {
        this.name = name;
        this.cores = cores;
        this.device = device;
        this.plataform = plataform;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public Object getDevice() {
        return device;
    }

    public void setDevice(Object device) {
        this.device = device;
    }

    public Object getPlataform() {
        return plataform;
    }

    public void setPlataform(Object plataform) {
        this.plataform = plataform;
    }
}
