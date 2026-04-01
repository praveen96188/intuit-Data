package com.intuit.sbd.payroll.psp.common.pgp.impl;

import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileSourceCode;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/11/13
 * Time: 11:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class PgpUnencryptedReader implements PgpReader {

    FileInputStream mFileInputStream;
    InputStreamReader mInputStreamReader;
    BufferedReader mBufferedReader;

    public void open(String pFileName) throws Exception {
        open(new File(pFileName));
    }

    public void open(String pFileName, PgpFileSourceCode pPgpFileSourceCode) throws Exception {
        open(new File(pFileName));
    }

    public void open(File pFile) throws Exception {
        mFileInputStream = new FileInputStream(pFile);
        mInputStreamReader = new InputStreamReader(mFileInputStream);
        mBufferedReader = new BufferedReader(mInputStreamReader);
    }

    public boolean ready() throws IOException {
        return mBufferedReader.ready();
    }

    public String readLine() throws IOException {
        return mBufferedReader.readLine();
    }

    public void close() throws IOException {
        if (mBufferedReader != null)
            mBufferedReader.close();
        if (mInputStreamReader != null)
            mInputStreamReader.close();
        if (mFileInputStream != null)
            mFileInputStream.close();
    }
}
