package com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption;

import java.io.*;

import com.intuit.idps.domain.item.Key;


/**
 * @author snasim
 * File Writer wrapper for IDPS
 */
public class IDPSFileWriter extends OutputStreamWriter {

    public IDPSFileWriter( String fileName, Key key) throws IOException {
        super(new IDPSFileOutputStream(fileName,key));
    }

    public IDPSFileWriter( String fileName, boolean append, Key key) throws IOException {
        super(new IDPSFileOutputStream(fileName, append,key));
    }

    public IDPSFileWriter( File file, Key key) throws IOException {
        super(new IDPSFileOutputStream( file,key));
    }

    public IDPSFileWriter( File file, boolean append, Key key) throws IOException {
        super(new IDPSFileOutputStream( file, append,key));
    }

    public IDPSFileWriter(FileDescriptor fd, Key key) {
        super(new IDPSFileOutputStream(fd,key));
    }
    public IDPSFileWriter(File file, Key key, String charsetNam)throws FileNotFoundException,UnsupportedEncodingException{
        super(new IDPSFileOutputStream(file,key),charsetNam);
    }
}
