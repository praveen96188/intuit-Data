package com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.intuit.idps.service.IdpsException;

import com.intuit.idps.domain.item.Key;

/**
 * @author snasim
 * File Input Stream wrapper for IDPS
 */
public class IDPSFileInputStream extends FileInputStream {

    private InputStream secureStream;

    // File type.  Indicates the use-case or business case.  Used for turning on/off decryption.

    public IDPSFileInputStream(String name, Key key) throws FileNotFoundException,IOException, IdpsException {
        super(name);
        IDPSInputStream sis = new IDPSInputStream(new FileInputStream(name),key);
        sis.setCloseInputStream(true);
        this.secureStream = sis;

    }

    public IDPSFileInputStream(File file, Key key) throws FileNotFoundException,IOException, IdpsException {
        super(file);
        IDPSInputStream sis = new IDPSInputStream(new FileInputStream(file),key);
        sis.setCloseInputStream(true);
        this.secureStream = sis;

    }

    public IDPSFileInputStream( FileDescriptor fdObj, Key key)throws IOException, IdpsException {
        super(fdObj);
        IDPSInputStream sis = new IDPSInputStream(new FileInputStream(fdObj),key);
        sis.setCloseInputStream(true);
        this.secureStream = sis;
    }

    @Override
    public int read() throws IOException {
        return secureStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return secureStream.read(b);

    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return secureStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (secureStream != null) {
            secureStream.close();
        }
        super.close();
    }
}