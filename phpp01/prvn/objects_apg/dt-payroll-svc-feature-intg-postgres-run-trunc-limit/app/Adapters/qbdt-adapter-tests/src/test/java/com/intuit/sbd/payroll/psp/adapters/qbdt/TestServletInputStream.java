package com.intuit.sbd.payroll.psp.adapters.qbdt;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 20, 2010
 * Time: 1:11:21 PM
 */
public class TestServletInputStream extends ServletInputStream {

	private InputStream mInputStream;

	public TestServletInputStream(InputStream pInputStream) {
		this.mInputStream = pInputStream;
	}

	@Override
	public int read() throws IOException {
		return mInputStream.read();
	}

	@Override
	public int available() throws IOException {
		return mInputStream.available();
	}

	@Override
	public void close() throws IOException {
		mInputStream.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		mInputStream.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return mInputStream.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return mInputStream.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return mInputStream.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		mInputStream.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return mInputStream.skip(n);
	}

	public InputStream getInputStream() {
		return mInputStream;
	}
}
