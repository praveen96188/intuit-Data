package com.intuit.sbd.payroll.psp.common.pgp;

import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileSourceCode;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/11/13
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PgpReader {

    public void open(String pFileName) throws Exception;

    public void open(String pFileName, PgpFileSourceCode pPgpFileSourceCode) throws Exception;

    public void open(File pFile) throws Exception;

    public boolean ready() throws IOException;

    public String readLine() throws IOException;

    public void close() throws IOException;

}
