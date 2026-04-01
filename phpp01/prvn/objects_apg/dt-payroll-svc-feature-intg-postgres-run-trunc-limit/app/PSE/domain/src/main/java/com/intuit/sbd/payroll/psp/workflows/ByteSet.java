package com.intuit.sbd.payroll.psp.workflows;

/**
 * Data Structure to store byte values to represent the workflow values
 */
public class ByteSet {

    public static int DEFAULT_BYTE_SIZE = 16;

    private int[] bytes;

    public ByteSet() {
        this(DEFAULT_BYTE_SIZE);
    }

    public ByteSet(int size) {
        this.bytes = new int[size];
    }


    // We are expecting the possible WorkflowState values will not exceed more than 9
    public void checkDataValidity(int value) {
        if ( value < 0 || value > 9){
            throw new IllegalArgumentException("Value should lie between 0 and 9");
        }

    }

    public void set(int index, int value) {

        this.checkDataValidity(value);
        this.bytes[index] = value;
    }

    public int get(int index) {

        return this.bytes[index];
    }

    public void remove(int index) {

        this.bytes[index] = 0;
    }

    public String getAllBytes() {
        final StringBuffer sb = new StringBuffer();
        for (int bytez : bytes) {
            sb.append(bytez);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ByteSet{");
        sb.append("bytes=");
        if (bytes == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < bytes.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(bytes[i]);
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }
}
