/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import java.util.Arrays;

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
    private boolean divide = true;
    private boolean defineThreads = false;

    public ParameterGPU() {
    }

    public ParameterGPU(double[] dataDouble, boolean read) {
        this(read, false, false, true);
        this.dataDouble = dataDouble;
    }

    public ParameterGPU(double[] dataDouble, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads, true);
        this.dataDouble = dataDouble;
    }

    public ParameterGPU(double[] dataDouble, boolean read, boolean write, boolean defineThreads, boolean divide) {
        this(read, write, defineThreads, divide);
        this.dataDouble = dataDouble;
    }

    public ParameterGPU(float[] dataFloat, boolean read) {
        this(read, false, false, true);
        this.dataFloat = dataFloat;
    }

    public ParameterGPU(float[] dataFloat, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads, true);
        this.dataFloat = dataFloat;
    }

    public ParameterGPU(float[] dataFloat, boolean read, boolean write, boolean defineThreads, boolean divide) {
        this(read, write, defineThreads, divide);
        this.dataFloat = dataFloat;
    }

    public ParameterGPU(int[] dataInt, boolean read) {
        this(read, false, false, true);
        this.dataInt = dataInt;
    }

    public ParameterGPU(int[] dataInt, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads, true);
        this.dataInt = dataInt;
    }

    public ParameterGPU(int[] dataInt, boolean read, boolean write, boolean defineThreads, boolean divide) {
        this(read, write, defineThreads, divide);
        this.dataInt = dataInt;
    }

    public ParameterGPU(long[] dataLong, boolean read) {
        this(read, false, false, true);
        this.dataLong = dataLong;
    }

    public ParameterGPU(long[] dataLong, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads, true);
        this.dataLong = dataLong;
    }

    public ParameterGPU(long[] dataLong, boolean read, boolean write, boolean defineThreads, boolean divide) {
        this(read, write, defineThreads, divide);
        this.dataLong = dataLong;
    }

    public ParameterGPU(short[] dataShort, boolean read) {
        this(read, false, false, true);
        this.dataShort = dataShort;
    }

    public ParameterGPU(short[] dataShort, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads, true);
        this.dataShort = dataShort;
    }

    public ParameterGPU(short[] dataShort, boolean read, boolean write, boolean defineThreads, boolean divide) {
        this(read, write, defineThreads, divide);
        this.dataShort = dataShort;
    }

    public ParameterGPU(char[] dataChar, boolean read) {
        this(read, false, false, true);
        this.dataChar = dataChar;
    }

    public ParameterGPU(char[] dataChar, boolean read, boolean write, boolean defineThreads) {
        this(read, write, defineThreads, true);
        this.dataChar = dataChar;
    }

    public ParameterGPU(char[] dataChar, boolean read, boolean write, boolean defineThreads, boolean divide) {
        this(read, write, defineThreads, divide);
        this.dataChar = dataChar;
    }

    private ParameterGPU(boolean read, boolean write, boolean defineThreads, boolean divide) {
        this.read = read;
        this.write = write;
        this.defineThreads = defineThreads;
        this.divide = divide;
    }

    public ParameterGPU(double[] dataDouble, boolean read, boolean write) {
        this(read, true, false, true);
        this.dataDouble = dataDouble;
    }

    public ParameterGPU(float[] dataFloat, boolean read, boolean write) {
        this(read, true, false, true);
        this.dataFloat = dataFloat;
    }

    public ParameterGPU(int[] dataInt, boolean read, boolean write) {
        this(read, true, false, true);
        this.dataInt = dataInt;
    }

    public ParameterGPU(long[] dataLong, boolean read, boolean write) {
        this(read, true, false, true);
        this.dataLong = dataLong;
    }

    public ParameterGPU(short[] dataShort, boolean read, boolean write) {
        this(read, true, false, true);
        this.dataShort = dataShort;
    }

    public ParameterGPU(char[] dataChar, boolean read, boolean write) {
        this(read, true, false, true);
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

    public ParameterGPU cloneIndex(int begin, int end) {
        ParameterGPU clone = new ParameterGPU();

        clone.defineThreads = this.defineThreads;
        clone.divide = this.divide;
        clone.read = this.read;
        clone.write = this.write;
//        System.out.println(begin+" "+end);

        if (this.dataFloat != null) {
            clone.dataFloat = Arrays.copyOfRange(this.dataFloat, begin, end);
        } else if (this.dataInt != null) {
            clone.dataInt = Arrays.copyOfRange(this.dataInt, begin, end);
        } else if (this.dataDouble != null) {
            clone.dataDouble = Arrays.copyOfRange(this.dataDouble, begin, end);
        } else if (this.dataLong != null) {
            clone.dataLong = Arrays.copyOfRange(this.dataLong, begin, end);
        } else if (this.dataShort != null) {
            clone.dataShort = Arrays.copyOfRange(this.dataShort, begin, end);
        } else if (this.dataChar != null) {
            clone.dataChar = Arrays.copyOfRange(this.dataChar, begin, end);
        }

        return clone;
    }

    public void copyFrom(ParameterGPU parametro, int begin, int end) {
        if (this.dataFloat != null) {
            System.arraycopy(parametro.dataFloat, 0, this.dataFloat, begin, (int) parametro.getSize());
        } else if (this.dataInt != null) {
            System.arraycopy(parametro.dataInt, 0, this.dataInt, begin, (int) parametro.getSize());
        } else if (this.dataDouble != null) {
            System.arraycopy(parametro.dataDouble, 0, this.dataDouble, begin, (int) parametro.getSize());
        } else if (this.dataLong != null) {
            System.arraycopy(parametro.dataLong, 0, this.dataLong, begin, (int) parametro.getSize());
        } else if (this.dataShort != null) {
            System.arraycopy(parametro.dataShort, 0, this.dataShort, begin, (int) parametro.getSize());
        } else if (this.dataChar != null) {
            System.arraycopy(parametro.dataChar, 0, this.dataChar, begin, (int) parametro.getSize());
        }
    }
}
