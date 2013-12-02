/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing.utils;


/**
 *
 * @author raphael
 */
public class DataStructure {

    private ParameterEnum parameter;
    private float[] datas;
    private int[] datasInt;

    public DataStructure() {
    }

    public DataStructure(ParameterEnum parameter, float[] datas) {
        this.parameter = parameter;
        this.datas = datas;
    }
    
    public DataStructure(ParameterEnum parameter, int[] datasInt) {
        this.parameter = parameter;
        this.datasInt = datasInt;
    }

    public float[] getDatas() {
        return datas;
    }

    public void setDatas(float[] datas) {
        this.datas = datas;
    }

    public ParameterEnum getParameter() {
        return parameter;
    }

    public void setParameter(ParameterEnum name) {
        this.parameter = name;
    }

    public int[] getDatasInt() {
        return datasInt;
    }

    public void setDatasInt(int[] datasInt) {
        this.datasInt = datasInt;
    }
}
