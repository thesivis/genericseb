/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

/**
 *
 * @author raphael
 */
public class ParameterGPU {

    private double[] dataDouble;
    private float[] dataFloat;
    private int[] dataInt;
    private long[] dataLong;
    private short[] dataShort;
    private char[] dataChar;
    private boolean read = false;
    private boolean write = false;
    private boolean divide = false;
    private boolean defineThreads = false;

    public ParameterGPU() {
    }

    public ParameterGPU(double[] dataDouble, boolean read) {
        this(read, false, false);
        this.dataDouble = dataDouble;
    }

    public ParameterGPU(double[] dataDouble, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads);
        this.dataDouble = dataDouble;
    }

    public ParameterGPU(float[] dataFloat, boolean read) {
        this(read, false, false);
        this.dataFloat = dataFloat;
    }

    public ParameterGPU(float[] dataFloat, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads);
        this.dataFloat = dataFloat;
    }

    public ParameterGPU(int[] dataInt, boolean read) {
        this(read, false, false);
        this.dataInt = dataInt;
    }

    public ParameterGPU(int[] dataInt, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads);
        this.dataInt = dataInt;
    }

    public ParameterGPU(long[] dataLong, boolean read) {
        this(read, false, false);
        this.dataLong = dataLong;
    }

    public ParameterGPU(long[] dataLong, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads);
        this.dataLong = dataLong;
    }

    public ParameterGPU(short[] dataShort, boolean read) {
        this(read, false, false);
        this.dataShort = dataShort;
    }

    public ParameterGPU(short[] dataShort, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads);
        this.dataShort = dataShort;
    }

    public ParameterGPU(char[] dataChar, boolean read) {
        this(read, false, false);
        this.dataChar = dataChar;
    }

    public ParameterGPU(char[] dataChar, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads);
        this.dataChar = dataChar;
    }

    private ParameterGPU(boolean read, boolean write, boolean defineThreads) {
        this.read = read;
        this.write = write;
        this.defineThreads = defineThreads;
    }

    public ParameterGPU(double[] dataDouble, boolean read, boolean write) {
        this(read, true, false);
        this.dataDouble = dataDouble;
    }

    public ParameterGPU(float[] dataFloat, boolean read, boolean write) {
        this(read, true, false);
        this.dataFloat = dataFloat;
    }

    public ParameterGPU(int[] dataInt, boolean read, boolean write) {
        this(read, true, false);
        this.dataInt = dataInt;
    }

    public ParameterGPU(long[] dataLong, boolean read, boolean write) {
        this(read, true, false);
        this.dataLong = dataLong;
    }

    public ParameterGPU(short[] dataShort, boolean read, boolean write) {
        this(read, true, false);
        this.dataShort = dataShort;
    }

    public ParameterGPU(char[] dataChar, boolean read, boolean write) {
        this(read, true, false);
        this.dataChar = dataChar;
    }

    public double[] getDataDouble() {
        return dataDouble;
    }

    public void setDataDouble(double[] dados) {
        this.dataDouble = dados;
    }

    public boolean isDivide() {
        return divide;
    }

    public void setDivide(boolean divide) {
        this.divide = divide;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public boolean isDefineThreads() {
        return defineThreads;
    }

    public void setDefineThreads(boolean defineThreads) {
        this.defineThreads = defineThreads;
    }

    public float[] getDataFloat() {
        return dataFloat;
    }

    public void setDataFloat(float[] dadosFloat) {
        this.dataFloat = dadosFloat;
    }

    public int[] getDataInt() {
        return dataInt;
    }

    public void setDataInt(int[] dadosInt) {
        this.dataInt = dadosInt;
    }

    public long[] getDataLong() {
        return dataLong;
    }

    public void setDataLong(long[] dadosLong) {
        this.dataLong = dadosLong;
    }

    public long getSize() {
        if (dataDouble != null) {
            return dataDouble.length;
        } else if (dataFloat != null) {
            return dataFloat.length;
        } else if (dataInt != null) {
            return dataInt.length;
        } else if (dataLong != null) {
            return dataLong.length;
        } else if (dataChar != null) {
            return dataChar.length;
        } else if (dataShort != null) {
            return dataShort.length;
        }
        return 0;
    }

    public short[] getDataShort() {
        return dataShort;
    }

    public void setDataShort(short[] datasShort) {
        this.dataShort = datasShort;
    }

    public char[] getDataChar() {
        return dataChar;
    }

    public void setDataChar(char[] datasChar) {
        this.dataChar = datasChar;
    }
}
