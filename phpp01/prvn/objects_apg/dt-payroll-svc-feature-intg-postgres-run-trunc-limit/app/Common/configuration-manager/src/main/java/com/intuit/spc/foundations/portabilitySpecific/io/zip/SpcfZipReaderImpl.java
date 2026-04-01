package com.intuit.spc.foundations.portabilitySpecific.io.zip;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.io.SpcfEofException;
import com.intuit.spc.foundations.portability.io.SpcfIOException;
import com.intuit.spc.foundations.portability.io.SpcfSeekOriginEnum;
import com.intuit.spc.foundations.portability.io.SpcfStream;
import com.intuit.spc.foundations.portability.io.zip.SpcfCompressionMethod;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipException;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipReader;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfInputStream;

/**
 * Implementation class of portable abstract class- SpcfZipReader.
 */
public class SpcfZipReaderImpl extends SpcfZipReader
{
	private ZipInputStream mPlatformSpecific;	
    
    /**
     * Creates a new ZIP reader.
     * @param zipIn binary file reader.
     * @exception SpcfArgumentNullException if zipIn is null
     */
	public SpcfZipReaderImpl(SpcfStream zipIn)
	{	
		SpcfParamValidator.checkIsNotNull(zipIn, "zipIn");
		mPlatformSpecific = new ZipInputStream(new SpcfInputStream(zipIn));
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public SpcfZipEntry getNextEntry() 
	{		
		try 
		{			
			SpcfZipEntry returnEntry = null;
			ZipEntry entry = mPlatformSpecific.getNextEntry();
			if(entry != null)
			{
				if(entry.getMethod() != ZipEntry.STORED)
				{
					returnEntry = new SpcfZipEntryImpl(entry.getName());
					returnEntry.setComment(entry.getComment());
					//
					if(entry.getMethod() == ZipEntry.STORED)
					{
						returnEntry.setCompressionMethod(SpcfCompressionMethod.Stored);				
					}
					else if(entry.getMethod() == ZipEntry.DEFLATED)
					{
						returnEntry.setCompressionMethod(SpcfCompressionMethod.Deflated);				
					}
					//
					if(entry.getTime() != -1)
					{
						returnEntry.setTime(SpcfFactory.getInstance().createCalendar(entry.getTime()));
					}	
				}
				else
				{
					returnEntry = new SpcfZipEntryImpl(entry);					
				}
			}
			return returnEntry;
		}
		catch (ZipException ex) 
		{		
			throw new SpcfZipException(ex);
		}
		catch (IOException ex) 
		{		
			throw new SpcfIOException(ex);
		}
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void closeEntry() 
	{
		try 
		{			
			mPlatformSpecific.closeEntry();
		}
		catch (ZipException ex) 
		{		
			throw new SpcfZipException(ex);
		}
		catch (IOException ex) 
		{		
			throw new SpcfIOException(ex);
		}			
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public int available() 
	{	
		try 
		{			
			return mPlatformSpecific.available();
		}
		catch (IOException ex) 
		{		
			throw new SpcfIOException(ex);
		}	
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public int read(byte[] destination, int index, int count) 
	{
		// Check parameters:
		SpcfParamValidator.checkArrayParams(destination, index, count);
		
		// If we weren't asked to read anything, don't:
		if (count == 0) return 0;
		
		try 
		{			
			return mPlatformSpecific.read(destination, index, count);
		}
		catch (ZipException ex) 
		{		
			throw new SpcfZipException(ex);
		}	
		catch (IOException ex) 
		{		
			throw new SpcfIOException(ex);
		}			
		catch (IndexOutOfBoundsException ex) 
		{		
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public long skip(long n) 
	{	
		try 
		{
			if (n <= 0) 
			{
	            throw new SpcfIllegalArgumentException("invalid skip length");
	        }	
			
			return mPlatformSpecific.skip(n);
		}		
		catch (ZipException ex) 
		{		
			throw new SpcfZipException(ex);
		}	
		catch (IOException ex) 
		{		
			throw new SpcfIOException(ex);
		}
		catch (IllegalArgumentException ex) 
		{		
			throw new SpcfIllegalArgumentException(ex);
		}
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void close() 
	{
		try 
		{			
			mPlatformSpecific.close();
		}		
		catch (IOException ex) 
		{		
			throw new SpcfIOException(ex);
		}		
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public boolean canRead()
	{
		return true;
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public boolean canSeek()
	{
		return false;
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public boolean canWrite()
	{
		return false;
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public void flush()
	{
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public long getLength()
	{
		return 0;
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public long getPosition()
	{
		return 0;
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public byte peekByte()
	{
		throw new SpcfInvalidOperationException();
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public byte readByte()
	{
		int b = 0;
		
		try
		{
			b = mPlatformSpecific.read();
		}
		catch (IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
		
		if (b == -1) throw new SpcfEofException();
		
		return (byte)b;
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public long seek(long position, SpcfSeekOriginEnum origin)
	{
		throw new SpcfInvalidOperationException();
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public void write(byte b)
	{
		throw new SpcfInvalidOperationException();
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public void write(byte[] buffer, int offset, int count)
	{
		throw new SpcfInvalidOperationException();
	}
}

