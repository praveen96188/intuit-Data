package com.intuit.sbd.payroll.psp.common.pgp.impl;

import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/8/13
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PgpUnencryptedWriter implements PgpWriter {

    private OutputStream mFileOutputStream;
    private OutputStreamWriter mOutputStreamWriter;

    public void open(String pFileName) throws Exception {
        mFileOutputStream = new BufferedOutputStream(new FileOutputStream(pFileName));
        mOutputStreamWriter = new OutputStreamWriter(mFileOutputStream);
    }

    public void write(String pString) throws IOException {
        try {
            mOutputStreamWriter.write(pString);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void write(char pChar) throws IOException {
        try {
            mOutputStreamWriter.write(pChar);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void flush() throws IOException {
        mOutputStreamWriter.flush();
    }

    public void close() throws IOException {
        try {
            if (mOutputStreamWriter != null)
                mOutputStreamWriter.close();
            if (mFileOutputStream != null)
                mFileOutputStream.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
