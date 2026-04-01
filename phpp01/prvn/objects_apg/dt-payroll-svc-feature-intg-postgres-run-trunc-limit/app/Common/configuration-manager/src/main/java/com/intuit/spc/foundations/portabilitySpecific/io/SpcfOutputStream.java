/**
 * 
 */
package com.intuit.spc.foundations.portabilitySpecific.io;

import java.io.*;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.io.*;


/**
 * Wraps an OutputStream around an SpfcStream.
 */
public class SpcfOutputStream extends OutputStream
{
	/**
	 * The binary SpcfStream we are wrapping.
	 */
	protected SpcfStream mBinaryStream = null;
	
	
	/**
	 * Constructs a new SpcfOutputStream to wrap the given SpcfStream
	 * @param binaryStream
	 * @throws SpcfArgumentNullException if the binary stream is null 
	 * @throws SpcfIllegalArgumentException if the binary stream is not writable
	 */
	public SpcfOutputStream(SpcfStream binaryStream)
	{
		// Make sure that the binary stream is not null:
		SpcfParamValidator.checkIsNotNull(binaryStream, "Binary Stream");

		// Make sure that the binary stream is writable:
		if (!binaryStream.canWrite()) throw new SpcfIllegalArgumentException("Binary stream must be writable.");

		// Hang onto the binary stream:
		mBinaryStream = binaryStream;
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public void write(int b) throws IOException
	{
		mBinaryStream.write((byte)b);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public void close() throws IOException
	{
		mBinaryStream.close();
		// Don't set to null. Let the binary stream throw IO exceptions automatically for us.
	}
	
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void flush() throws IOException
	{
		mBinaryStream.flush();
	}
	
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void write(byte[] buffer) throws IOException
	{
		mBinaryStream.write(buffer);
	}
	
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException
	{
		mBinaryStream.write(buffer, offset, count);
	}
}
