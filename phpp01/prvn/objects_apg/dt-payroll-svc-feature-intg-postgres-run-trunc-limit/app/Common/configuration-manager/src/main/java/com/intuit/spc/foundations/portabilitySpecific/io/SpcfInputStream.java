/**
 * 
 */
package com.intuit.spc.foundations.portabilitySpecific.io;

import java.io.*;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.io.*;

/**
 * Wraps an InputStream around an SpfcStream.
 */
public class SpcfInputStream extends InputStream
{
	/**
	 * The binary SpcfStream we are wrapping.
	 */
	protected SpcfStream mBinaryStream = null;
	
	
	/**
	 * Constructs a new SpcfInputStream to wrap the given SpcfStream
	 * @param binaryStream 
	 * @throws SpcfArgumentNullException if the binary stream is null 
	 * @throws SpcfIllegalArgumentException if the binary stream cannot be read.
	 */
	public SpcfInputStream(SpcfStream binaryStream)
	{
		// Make sure that the binary stream is not null:
		SpcfParamValidator.checkIsNotNull(binaryStream, "Binary Stream");

		// Make sure that the binary stream is writable:
		if (!binaryStream.canRead()) throw new SpcfIllegalArgumentException("Binary stream must be readable.");

		// Hang onto the binary stream:
		mBinaryStream = binaryStream;
	}
	
	
	/**
	 * This won't really throw IOException because the underlying SpcfStream will
	 * wrap java exceptions with SPCF exceptions. 
	 */
	@Override
	public int read() throws IOException
	{
		try
		{
			return mBinaryStream.readByte();
		}
		catch (SpcfEofException see)
		{
			return -1;
		}
	}
	

	/**
	 * This won't really throw IOException because the underlying SpcfStream will
	 * wrap java exceptions with SPCF exceptions. 
	 */
	@Override
	public void close() throws IOException
	{
		mBinaryStream.close();
		// Don't set to null. Let the binary stream throw IO exceptions automatically for us.
	}

	
	/**
	 * This won't really throw IOException because the underlying SpcfStream will
	 * wrap java exceptions with SPCF exceptions. 
	 */
	@Override
	public int read(byte[] buffer) throws IOException
	{
		return mBinaryStream.read(buffer, 0, (buffer != null ? buffer.length : 0));
	}
	
	
	/**
	 * This won't really throw IOException because the underlying SpcfStream will
	 * wrap java exceptions with SPCF exceptions. 
	 */
	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException
	{
		return mBinaryStream.read(buffer, offset, count);
	}
}
