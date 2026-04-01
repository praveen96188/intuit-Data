/**
 * 
 */
package com.intuit.spc.foundations.portabilitySpecific.io;

import java.io.*;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.io.SpcfStream;
import com.intuit.spc.foundations.portability.io.SpcfSeekOriginEnum;
import com.intuit.spc.foundations.portability.io.SpcfIOException;

/**
 *
 */
public class SpcfStreamImpl extends SpcfStream 
{

	/*
	 * inputStream exists only if initialized with an InputStream
	 */
	private InputStream mInputStream;
	
	/*
	 * outputStream exists only if initialized with an OutputStream
	 */
	private OutputStream mOutputStream;
	
	/*
	 * Defines wether the stream is an input stream or an outpu stream
	 */
	private boolean mIsInputStream = true;
	
	/*
	 * Defines if we can read from the stream
	 */
	private boolean mCanRead = true;
	
	/*
	 * Defines if we can seek to a certain position in the stream
	 */
	private boolean mCanSeek = false;
	
	/*
	 * Defines if we can write to the stream
	 */
	private boolean mCanWrite = false;
	
	/**
	 * Creates a Stream from an InputStream and sets different private members accordingly
	 */
	public SpcfStreamImpl(InputStream inputStream)
	{
		SpcfParamValidator.checkIsNotNull(inputStream, "inputStream");
		this.mInputStream = inputStream;
	}
	
	public SpcfStreamImpl(OutputStream outputStream)
	{
		SpcfParamValidator.checkIsNotNull(outputStream, "outputStream");
		this.mOutputStream = outputStream;
		mIsInputStream = false;
		mCanWrite = true;
		mCanRead = false;
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#canRead()
	 */
	@Override
	public boolean canRead() 
	{
		return mCanRead;
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#canSeek()
	 */
	@Override
	public boolean canSeek() 
	{
		return mCanSeek;
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#canWrite()
	 */
	@Override
	public boolean canWrite() 
	{
		// TODO Auto-generated method stub
		return mCanWrite;
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#close()
	 */
	@Override
	public void close() 
	{
		mCanRead = false;
		mCanWrite = false;
		
		try
		{
			if (mIsInputStream)
			{
				mInputStream.close();
			}
			else
			{
				mOutputStream.close();
			}
		}
		catch(IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#flush()
	 */
	@Override
	public void flush() 
	{
		if (mIsInputStream)
		{
			throw new SpcfIOException();
		}
		
		try
		{
			mOutputStream.flush();
		}
		catch(IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#getLength()
	 */
	@Override
	public long getLength() 
	{
		return -1;
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#getPosition()
	 */
	@Override
	public long getPosition() 
	{
		return -1;
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#peekByte()
	 */
	@Override
	public byte peekByte() 
	{
		throw new SpcfInvalidOperationException();
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] buffer, int offset, int count) 
	{
		if (!mIsInputStream || !mCanRead)
		{
			throw new SpcfInvalidOperationException();
		}

		SpcfParamValidator.checkArrayParams(buffer, offset, count);

		try
		{
			return mInputStream.read(buffer, offset, count);
		}
		catch(IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#readByte()
	 */
	@Override
	public byte readByte() 
	{
		if (!mIsInputStream || !mCanRead)
		{
			throw new SpcfInvalidOperationException();
		}

		try
		{
			int i = mInputStream.read();

			if (i == -1)
			{
				throw new SpcfIOException();
			}
			
			return (byte)i;
		}
		catch(IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#seek(long, com.intuit.spc.foundations.portability.io.SpcfSeekOriginEnum)
	 */
	@Override
	public long seek(long position, SpcfSeekOriginEnum origin) 
	{
		throw new SpcfInvalidOperationException();
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#write(byte)
	 */
	@Override
	public void write(byte b) 
	{
		if (mIsInputStream || !mCanWrite)
		{
			throw new SpcfInvalidOperationException();
		}

		try
		{
			mOutputStream.write(b);
		}
		catch(IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] buffer, int offset, int count) 
	{
		if (mIsInputStream || !mCanWrite)
		{
			throw new SpcfInvalidOperationException();
		}

		SpcfParamValidator.checkArrayParams(buffer, offset, count);
		
		try
		{
			mOutputStream.write(buffer, offset, count);
		}
		catch(IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}
}
