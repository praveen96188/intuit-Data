package com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import com.intuit.idps.domain.item.Key;

/**
 * @author snasim
 * File Output Stream wrapper for IDPS
 */
public class IDPSFileOutputStream extends FileOutputStream {

    private OutputStream secureStream;

    // File type.  Indicates the use-case or business case.  Used for turning on/off decryption.


    public IDPSFileOutputStream(String name, Key key) throws FileNotFoundException {
        super(name);
        this.secureStream = new IDPSOutputStream(new FileOutputStream(name),key);

    }

    public IDPSFileOutputStream(FileDescriptor fdObj, Key key) {
        super(fdObj);
        this.secureStream = new IDPSOutputStream(new FileOutputStream(fdObj),key);

    }

    public IDPSFileOutputStream(File file, boolean append, Key key) throws FileNotFoundException {
        super(file, append);
        this.secureStream = new IDPSOutputStream(new FileOutputStream(file, append),key);

    }

    public IDPSFileOutputStream( File file, Key key) throws FileNotFoundException {
        super(file);
        this.secureStream = new IDPSOutputStream(new FileOutputStream(file),key);

    }

    public IDPSFileOutputStream( String name, boolean append, Key key) throws FileNotFoundException {
        super(name, append);
        this.secureStream = new IDPSOutputStream(new FileOutputStream(name, append),key);

    }

    public void endWriting() {
        ((IDPSOutputStream) secureStream).endWriting();
    }

    @Override
    public void write(int b) throws IOException {
        secureStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        secureStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        secureStream.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (secureStream != null) {
            secureStream.close();
        }
    }

    @Override
    public FileChannel getChannel() {
        return super.getChannel();
    }

    @Override
    protected void finalize() throws IOException {
        super.finalize();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    public int getBufferCount() {
        if (secureStream != null) {
            return ((IDPSOutputStream) secureStream).getBufferCount();
        }
        return 0;
    }
}