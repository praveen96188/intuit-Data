package com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption;

import com.intuit.idps.service.StreamingCryptoService;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IdpsSdkConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import com.intuit.idps.domain.item.Key;

import com.intuit.idps.service.IdpsException;

/**
 * @author snasim
 * FilterInputStream wrapper for IDPS
 */
public class IDPSInputStream extends FilterInputStream {

    private StreamingCryptoService idpsStream;
    private ByteArrayInputStream buffer;
    private ByteArrayOutputStream out;
    private ReadableByteChannel streamOut;
    private byte[] cipherbufBackingArray;
    private boolean isEncrypt;
    private int totalSize;
    // Indicates if the streams are closed
    private boolean isClosed;
    // Lazily inititialize IDPS stream
    private boolean isIDPSStreamInitialized;
    // Close/Keep open InputStream passed by the client
    private boolean isCloseInputStream;

    Key key;

    public IDPSInputStream(InputStream in, Key key)throws IOException, IdpsException {
        this(in, false,key);
    }

    public IDPSInputStream(InputStream in, boolean isEncrypt, Key key)throws IOException, IdpsException {
        super(in);
        this.isEncrypt = isEncrypt;
        this.key = key;
        initializeIDPSStream();
    }

    private void initializeIDPSStream()throws IOException, IdpsException {
        if (!isIDPSStreamInitialized) {
            if (isEncrypt) {
                this.idpsStream = StreamingCryptoService.Factory.streamEncryptInit(key, IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE, Channels.newChannel(super.in));
            } else {
                this.idpsStream = StreamingCryptoService.Factory.streamDecryptInit(key,  Channels.newChannel(super.in));
            }
            out = new ByteArrayOutputStream();
            // set up the channel to read the results from
            streamOut = idpsStream.getOutputChannel();
            // read buffer - should be larger than chunkSize for encryption
            cipherbufBackingArray = new byte[IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE * 2];
            isIDPSStreamInitialized = true;
        }
    }

    @Override
    public int read() throws IOException {
        readData(1);
        return buffer.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        readData(b.length);
        return buffer.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        readData(len);
        return buffer.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (!isClosed) {
            if (buffer != null) {
                buffer.close();
            }
            if (idpsStream != null) {
                idpsStream.streamClose();
            }
            if (out != null) {
                out.close();
            }
            if (isCloseInputStream) {
                super.close();
            }
            isClosed = true;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IOException("mark/reset/skip not supported");
    }

    @Override
    public int available() throws IOException {
        // TODO: Find IDPS encrypted/decrypted length
        return super.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new RuntimeException("mark/reset/skip not supported");
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset/skip not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Returns size of encrypted/decrypted bytes.
     *
     * NOTE:  Correct size can only be obtained once the stream is fully read (after  read methods return -1)
     * @return
     */
    public int getTotalSize() {
        return totalSize;
    }

    /**
     * If set to true, the InputStream passed by the client will be closed on close().
     *
     * By default, the input stream will not be closed and the client will have to close it explicitly.
     * @param isCloseInputStream
     */
    public void setCloseInputStream(boolean isCloseInputStream) {
        this.isCloseInputStream = isCloseInputStream;
    }

    /**
     * Encrypts/Decrypts data read from underlying input stream/channel
     *
     * @param required
     * @throws IOException
     */
    private void readData(int required) throws IOException {
        try {

            int availableInBuffer = 0;
            if (buffer != null) {
                availableInBuffer = buffer.available();
                if (availableInBuffer >= required) {
                    return;
                }
                if (availableInBuffer > 0) {
                    byte[] b = new byte[availableInBuffer];
                    buffer.read(b);
                    out.write(b);
                }
                buffer.close();
                buffer = null;
            }

            // Read until enough bytes are read
            int readBytes;
            while ((readBytes = isEncrypt ? idpsStream.streamEncryptNext() : idpsStream.streamDecryptNext()) != -1) {
                totalSize += readBytes;
                // read results and write to out file
                ByteBuffer readBuffer = ByteBuffer.wrap(cipherbufBackingArray, 0, readBytes);
                streamOut.read(readBuffer);
                byte[] cipherbuf = readBuffer.array();
                out.write(cipherbuf, 0, readBytes);
                availableInBuffer += readBytes;
                if (availableInBuffer >= required) {
                    break;
                }
            }

            if (buffer == null) {
                buffer = new ByteArrayInputStream(out.toByteArray());
                out.reset();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}