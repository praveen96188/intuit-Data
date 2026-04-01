package com.intuit.sbd.payroll.psp.common.pgp;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/8/13
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PgpWriter {

    public void open(String pFileName) throws Exception;

    public void write(String pString) throws IOException;

    public void write(char pChar) throws IOException;

    public void flush() throws IOException;

    public void close() throws IOException;

}
