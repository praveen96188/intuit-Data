package com.intuit.spc.foundations.portabilitySpecific.io.zip;

import java.util.zip.ZipEntry;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.io.zip.SpcfCompressionMethod;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Implementation class of portable abstract class- SpcfZipEntry.
 */
public class SpcfZipEntryImpl extends SpcfZipEntry
{
	private ZipEntry mPlatformSpecific;
	
    /**
     * Creates a zip entry with the given name.
     * @param zipEntryName The name for this entry. Can include directory components. The convention for names is
     * 'unix' style paths  with no device names and path elements separated by '/' characters.
     * @exception SpcfArgumentNullException if the entry name is null
     * @exception SpcfIllegalArgumentException if the entry name is longer than 0xFFFF (65535) bytes
     */
	public SpcfZipEntryImpl(String zipEntryName)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(zipEntryName, "zipEntryName");
		
		try
		{
			mPlatformSpecific = new ZipEntry(zipEntryName);
			mPlatformSpecific.setTime(SpcfCalendar.getNow().getTimeInMilliseconds());
		}
		catch(IllegalArgumentException ex)
		{
			throw new SpcfIllegalArgumentException(ex);
		}
	}
	
    /**
     * Constructs zip entry from a portable zip-entry.
     * @param zipEntry portable zip-entry
     * @exception SpcfArgumentNullException if zipEntry is null
     */
	public SpcfZipEntryImpl(SpcfZipEntry zipEntry)
	{
		SpcfParamValidator.checkIsNotNull(zipEntry, "zipEntry");
		mPlatformSpecific = new ZipEntry(((SpcfZipEntryImpl)zipEntry).toSpecific());
	}
	
    /**
     * Constructs zip entry from platform specific zip-entry.
     * @param zipEntry platform specific zip-entry
     * @exception SpcfArgumentNullException if zipEntry is null
     */
	SpcfZipEntryImpl(ZipEntry zipEntry)
	{
		SpcfParamValidator.checkIsNotNull(zipEntry, "zipEntry");		
		mPlatformSpecific = zipEntry;
		if(mPlatformSpecific.getTime() == -1)
		{
			mPlatformSpecific.setTime(SpcfCalendar.getNow().getTimeInMilliseconds());
		}
	}
	
    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#getName()
     */
	public String getName() 
	{		
		return mPlatformSpecific.getName();
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#setTime(SpcfCalendar)
     */
	public void setTime(SpcfCalendar time) 
	{			
		if(time == null)
		{	
			time = SpcfCalendar.getNow();
		}		
		mPlatformSpecific.setTime(time.getTimeInMilliseconds());		
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#getTime()
     */
	public SpcfCalendar getTime() 
	{		
		SpcfCalendar cal = null;
		long time = mPlatformSpecific.getTime();
		if(time != -1)
		{
			cal = SpcfFactory.getInstance().createCalendar(time);			
		}
		return cal;
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#setSize(long)
     */
	public void setSize(long size) 
	{
		try
		{
			mPlatformSpecific.setSize(size);
		}
		catch(IllegalArgumentException ex)
		{
			throw new SpcfIllegalArgumentException(ex);			
		}
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#getSize()
     */
	public long getSize()
	{	
		long size = mPlatformSpecific.getSize();
		return size == 0 ? -1 : size;
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#getCompressedSize()
     */
	public long getCompressedSize() 
	{	
		long size = mPlatformSpecific.getCompressedSize();
		return size == 0 ? -1 : size;
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#setCompressedSize(long)
     */
	public void setCompressedSize(long csize) 
	{
		if (((long)csize & 0xffffffff00000000L) != 0) 
		{
			throw new SpcfIllegalArgumentException();
		}		
		
		mPlatformSpecific.setCompressedSize(csize);		
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#setCrc(long)
     */
	public void setCrc(long crc) 
	{
		try
		{
			mPlatformSpecific.setCrc(crc);
		}
		catch(IllegalArgumentException ex)
		{
			throw new SpcfIllegalArgumentException(ex);			
		}
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#getCrc()
     */
	public long getCrc() 
	{	
		long crc = mPlatformSpecific.getCrc();
		return crc == 0 ? -1 : crc;
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#setCompressionMethod(SpcfCompressionMethod)
     */
	public void setCompressionMethod(SpcfCompressionMethod method) 
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
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#getCompressionMethod()
     */
	public SpcfCompressionMethod getCompressionMethod() 
	{	
		SpcfCompressionMethod ret = SpcfCompressionMethod.Unknown;
		int method = mPlatformSpecific.getMethod();
		if (method == ZipEntry.STORED)            
        {
            return SpcfCompressionMethod.Stored;
        }
		else if (method == ZipEntry.DEFLATED)            
        {
            return SpcfCompressionMethod.Deflated;
        }
		
		return ret;
	}	

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#setComment(String)
     */
	public void setComment(String comment)
	{
		try
		{
			mPlatformSpecific.setComment(comment);
		}
		catch(IllegalArgumentException ex)
		{
			throw new SpcfIllegalArgumentException(ex);			
		}			
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#getComment()
     */
	public String getComment() 
	{	
		return mPlatformSpecific.getComment();
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry#isDirectory()
     */
	public boolean isDirectory() 
	{	
		return mPlatformSpecific.isDirectory();
	}

   /**
    * Returns a string representation of the ZIP entry.
    */
	@Override
	public String toString() 
	{	
		return mPlatformSpecific.toString();		
	}
	
	/**
     * Returns the hash code value for this entry.
	 */
	@Override
	public int hashCode() 
	{	
		return mPlatformSpecific.hashCode();
	}
	
	/**
     * Returns platform specific implementation used by the zip-entry.
     * @return platform specific iinstance
	 */
    public ZipEntry toSpecific()
	{
		return this.mPlatformSpecific;
	}
}
