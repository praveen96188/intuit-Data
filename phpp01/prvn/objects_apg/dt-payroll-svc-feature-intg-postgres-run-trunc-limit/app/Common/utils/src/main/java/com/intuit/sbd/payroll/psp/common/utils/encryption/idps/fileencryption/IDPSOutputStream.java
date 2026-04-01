package com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption;

import com.intuit.idps.service.StreamingCryptoService;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IdpsSdkConstants;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import com.intuit.idps.domain.item.Key;

/**
 * @author snasim
 * FilterOutputStream wrapper for IDPS
 */
public class IDPSOutputStream extends FilterOutputStream {

    private static final int chunkSize = IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE;

    // Internal buffer to maintain data until the buffer is full
    private byte[] buffer;

    // Length of the buffer
    private int count;

    // Indicates if the final bytes are written to the underlying stream
    private boolean isEnd;

    // Indicates if the streams are closed
    private boolean isClosed;

    // Internal streams
    private PipedOutputStream pos;
    private PipedInputStream pis;

    // IDPS stream for encryption/decryption
    private StreamingCryptoService idpsStream;

    Key key;

    public IDPSOutputStream(OutputStream os, Key key) {
        super(os);
        this.key = key;
        init();
    }

    private void init() {
        buffer = new byte[chunkSize];
        try {
            pos = new PipedOutputStream();
            pis = new PipedInputStream(pos, chunkSize);
            this.idpsStream = StreamingCryptoService.Factory.streamEncryptInit(key, IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE, Channels.newChannel(pis));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void flushBuffer(boolean isFinal) {
        try {
            if(isFinal || count == chunkSize) {
                if (count > 0) {
                    pos.write(buffer, 0, count);
                    if (isFinal && pos != null) {
                        pos.close();
                    }
                    encryptAndWrite();
                }
                count = 0;
                isEnd = isFinal;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void encryptAndWrite() {
        try {
            int nBytes = idpsStream.streamEncryptNext();
            if (nBytes != -1) {
                ReadableByteChannel streamOut = idpsStream.getOutputChannel();
                byte[] cipherbufBackingArray = new byte[IdpsSdkConstants.STREAMING_ENCRYPT_CHUNK_SIZE * 2];
                ByteBuffer readBuffer = ByteBuffer.wrap(cipherbufBackingArray, 0, nBytes);
                streamOut.read(readBuffer);
                byte[] cipherbuf = readBuffer.array();
                super.out.write(cipherbuf, 0, nBytes);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void writeInternal(final byte[] b, int off, int len) throws IOException {
        if (isEnd || isClosed) {
            throw new IOException("Cannot write to the stream anymore.");
        }

        flushBuffer(false);
        // Fill rest of the buffer first
        if (count < chunkSize && !(count == 0 && len > chunkSize)) {
            int left = chunkSize - count; // Space left in the buffer
            int v = left >= len ? len : left;
            System.arraycopy(b, off, buffer, count, v);
            off += v;
            len -= v;
            count += v;
            flushBuffer(false);
        }

        // Write directly to underlying stream
        int times = len / chunkSize;
        for (int i = 0; i < times; i++) {
            pos.write(b, off, chunkSize);
            encryptAndWrite();
            off += chunkSize;
            len -= chunkSize;
        }

        if (len > 0) {
            System.arraycopy(b, off, buffer, count, len);
            count = len;
        }
    }

    public void endWriting() {
        flushBuffer(true);
    }

    @Override
    public void write(int b) throws IOException {
        writeInternal(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        writeInternal(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeInternal(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (!isClosed) {
            endWriting();
            if (idpsStream != null) idpsStream.streamClose();
            if (pis != null) pis.close();
            if (pos!= null) pos.close();
            super.close();
            isClosed = true;
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    public int getBufferCount() {
        return count;
    }
}