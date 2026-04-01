package com.intuit.spc.foundations.portabilitySpecific.io.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.io.SpcfIOException;
import com.intuit.spc.foundations.portability.io.SpcfSeekOriginEnum;
import com.intuit.spc.foundations.portability.io.SpcfStream;
import com.intuit.spc.foundations.portability.io.zip.SpcfCompressionMethod;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipException;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipWriter;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfOutputStream;

/**
 * Implementation class of portable abstract class- SpcfZipWriter.
 */
public class SpcfZipWriterImpl extends SpcfZipWriter
{
	private ZipOutputStream mPlatformSpecific;		
	
    /**
     * Creates a new Zip output stream, writing a zip archive.
     * @param zipOut The output stream to which the archive contents are written.
     * @exception SpcfArgumentNullException if zipOut is null.
     */ 
	public SpcfZipWriterImpl(SpcfStream zipOut)
	{
		SpcfParamValidator.checkIsNotNull(zipOut, "zipOut");
		mPlatformSpecific = new ZipOutputStream(new SpcfOutputStream(zipOut));
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void setComment(String comment) 
	{	
		try
		{
			mPlatformSpecific.setComment(comment);
		}
		catch(IllegalArgumentException ex)
		{
			throw new SpcfIllegalArgumentException (ex);
		}
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void setDefaultCompressionMethod(SpcfCompressionMethod method) 
	{								
		if(method == SpcfCompressionMethod.Stored)
		{
			mPlatformSpecific.setMethod(ZipEntry.STORED);				
		}
		else if(method == SpcfCompressionMethod.Deflated)
		{
			mPlatformSpecific.setMethod(ZipEntry.DEFLATED);
		}	
		else
		{
			throw new SpcfIllegalArgumentException("invalid compression method");
		}
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void setDefaultCompressionLevel(int level) 
	{
		try
		{		
			if(level == -1)
			{
				mPlatformSpecific.setLevel(Deflater.DEFAULT_COMPRESSION);
			}
			else
			{
				mPlatformSpecific.setLevel(level);
			}
		}
		catch(IllegalArgumentException ex)
		{
			throw new SpcfIllegalArgumentException (ex);
		}		
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void putNextEntry(SpcfZipEntry zipEntry) 
	{
		SpcfParamValidator.checkIsNotNull(zipEntry, "zipEntry");
		
		try
		{
			ZipEntry ze = ((SpcfZipEntryImpl)zipEntry).toSpecific();
			ZipEntry zeToWrite = ze;
			
			if(zipEntry.getCompressionMethod() != SpcfCompressionMethod.Stored)
			{
				// Ignore size, csize and crc value
				zeToWrite = new ZipEntry(ze.getName());
				zeToWrite.setComment(ze.getComment());
				zeToWrite.setExtra(ze.getExtra());
				zeToWrite.setTime(ze.getTime());
				if(ze.getMethod() == ZipEntry.DEFLATED || ze.getMethod() == ZipEntry.STORED)
				{
					zeToWrite.setMethod(ze.getMethod());
				}	
			}

			mPlatformSpecific.putNextEntry(zeToWrite);
		}
		catch(ZipException ex)
		{
			throw new SpcfZipException (ex);
		}		
		catch(IOException ex)
		{
			throw new SpcfIOException  (ex);
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
		catch(ZipException ex)
		{
			throw new SpcfZipException (ex);
		}		
		catch(IOException ex)
		{
			throw new SpcfIOException  (ex);
		}		
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void write(byte[] buffer, int off, int len) 
	{	
		SpcfParamValidator.checkIsNotNull(buffer, "buffer");
		
		try
		{
			mPlatformSpecific.write(buffer, off, len);
		}
		catch(ZipException ex)
		{
			throw new SpcfZipException (ex);
		}		
		catch(IOException ex)
		{
			throw new SpcfIOException  (ex);
		}
		catch(IndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void finish()
	{
		try
		{
			mPlatformSpecific.finish();
		}
		catch(ZipException ex)
		{
			throw new SpcfZipException (ex);
		}		
		catch(IOException ex)
		{
			throw new SpcfIOException  (ex);
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
		catch(IOException ex)
		{
			//if (!ex.getMessage().contains("ZIP file must have at least one entry")) 
			//{							
				throw new SpcfIOException (ex);
			//}
		}	
	}

	public OutputStream toOutputStream() 
	{		
		return mPlatformSpecific;
	}

	/**
	 * @inheritDoc 
	 */
	@Override
	public void flush() 
	{
		try
		{
			mPlatformSpecific.flush();
		}			
		catch(IOException ex)
		{
			throw new SpcfIOException (ex);
		}	
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public boolean canRead()
	{
		return false;
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
		return true;
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
	public int read(byte[] buffer, int offset, int count)
	{
		throw new SpcfInvalidOperationException();
	}
	
	/**
	 * @inheritDoc 
	 */
	@Override
	public byte readByte()
	{
		throw new SpcfInvalidOperationException();
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
		try
		{
			mPlatformSpecific.write(b);
		}
		catch(ZipException ex)
		{
			throw new SpcfZipException (ex);
		}		
		catch(IOException ex)
		{
			throw new SpcfIOException  (ex);
		}
		catch(IndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}
}
