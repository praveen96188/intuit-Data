package com.intuit.spc.foundations.portabilitySpecific.io.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfSecurityException;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.io.SpcfEofException;
import com.intuit.spc.foundations.portability.io.SpcfFile;
import com.intuit.spc.foundations.portability.io.SpcfFileNotFoundException;
import com.intuit.spc.foundations.portability.io.SpcfIOException;
import com.intuit.spc.foundations.portability.io.SpcfSeekOriginEnum;
import com.intuit.spc.foundations.portability.io.SpcfStream;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipException;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipFile;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfIteratorImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfFileImpl;

/**
 * Implementation class of portable abstract class- SpcfZipFile.
 */
public class SpcfZipFileImpl extends SpcfZipFile
{	
	private ZipFile mPlatformSpecific;
	
    /**
     * Opens a ZIP file for reading given the specified SpcfFile object.
     * @param file the ZIP file to be opened for reading
     * @exception SpcfArgumentNullException if file is null
     * @exception SpcfFileNotFoundException if file is not found
     * @exception SpcfZipException if a ZIP error has occurred
     * @exception SpcfIOException if an I/O error has occurred
     * @exception SpcfSecurityException if reading on the file is restricted
     */
	public SpcfZipFileImpl(SpcfFile file)
	{
		this(file, OpenRead);		
	}
	
    /**
     * Opens a zip file for reading.
     * @param name the name of the zip file
     * @exception SpcfArgumentNullException if file is null
     * @exception SpcfFileNotFoundException if file is not found
     * @exception SpcfZipException if a ZIP error has occurred
     * @exception SpcfIOException if an I/O error has occurred
     * @exception SpcfSecurityException if reading on the file is restricted
     */
	public SpcfZipFileImpl(String name)
	{		
		this(SpcfFactory.getInstance().createFile(name), OpenRead);		
	}
	
    /**
     * Opens a new ZipFile to read from the specified File object in the specified mode.<p>
     * 
     * The mode argument must be  either OpenRead or OpenRead | OpenDelete.
     * @param file the ZIP file to be opened for reading
     * @param mode the mode in which the file is to be opened
     * @exception SpcfArgumentNullException if file is null
     * @exception SpcfFileNotFoundException if file is not found
     * @exception SpcfIllegalArgumentException If the mode argument is invalid
     * @exception SpcfZipException if a ZIP error has occurred
     * @exception SpcfIOException if an I/O error has occurred
     * @exception SpcfSecurityException if reading on the file is restricted
     */
	public SpcfZipFileImpl(SpcfFile file, int mode)
	{	
		SpcfParamValidator.checkIsNotNull(file, "file");
		if(!file.exists())
		{
			throw new SpcfFileNotFoundException();
		}
		
		if(mode == SpcfZipFile.OpenRead ||  mode == (SpcfZipFile.OpenRead | SpcfZipFile.OpenDelete))
		{
			try
			{
				mPlatformSpecific = new ZipFile(((SpcfFileImpl)file).toSpecific());
			}
			catch(ZipException ex)
			{
				throw new SpcfZipException(ex);
			} 
			catch (IOException ex) 
			{
				throw new SpcfIOException(ex);
			}
			catch (SecurityException ex) 
			{
				throw new SpcfSecurityException(ex);
			}	
		}
		else
		{
			throw new SpcfIllegalArgumentException("mode value is not correct!");
		}
	}
	
    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipFile#getEntry(String)
     */
	public SpcfZipEntry getEntry(String name) 
	{	
		SpcfParamValidator.checkIsNotNull(name, "name");		
		SpcfZipEntry retEntry = null;	
		ZipEntry entry = null;
		try
		{
			entry = mPlatformSpecific.getEntry(name);
		}
		catch(IllegalStateException ex)
		{
			throw new SpcfIllegalStateException(ex);
		}
		
		//
		if(entry != null)
		{
			retEntry = new SpcfZipEntryImpl(entry);
		}
		return retEntry;
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipFile#getInputStream(SpcfZipEntry)
     */
	public SpcfStream getInputStream(SpcfZipEntry entry) 
	{	
		SpcfParamValidator.checkIsNotNull(entry, "entry");
		
		InputStream iStream = null;
		try 
		{
			iStream = mPlatformSpecific.getInputStream(((SpcfZipEntryImpl)entry).toSpecific());
			return new SpcfZipEntryReader(iStream);
		}
		catch(ZipException ex)
		{
			throw new SpcfZipException(ex);
		} 
		catch (IOException ex) 
		{
			throw new SpcfIOException(ex);
		}		
		catch (IllegalStateException ex) 
		{
			throw new SpcfIllegalStateException(ex);
		}		
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipFile#getName()
     */
	public String getName() 
	{
		return mPlatformSpecific.getName();
	}
	
    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipFile#getIterator()
     */
	public ISpcfIterator<SpcfZipEntry> getIterator() 
	{	
		try
		{			
			SpcfZipEntryIterator it = new SpcfZipEntryIterator(mPlatformSpecific.entries());
			return new SpcfIteratorImpl<SpcfZipEntry>(it);
		}
		catch (IllegalStateException ex) 
		{
			throw new SpcfIllegalStateException(ex);
		}
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipFile#getZipEntriesCount()
     */
	public int getZipEntriesCount() 
	{
		try
		{
			return mPlatformSpecific.size();
		}
		catch (IllegalArgumentException ex) 
		{
			throw new SpcfIllegalArgumentException(ex);
		}
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.SpcfZipFile#close()
     */
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
     * Returns platform specific implementation instance of the zip file.
     * @return platform specific instance
     */
	public ZipFile toSpecific()
	{			
		return mPlatformSpecific;		
	}
}

class SpcfZipEntryIterator implements Iterator<SpcfZipEntry>
{
	private Enumeration mEnumeration;
    
	SpcfZipEntryIterator(Enumeration en)
	{
		mEnumeration = en;
	}
	
	public boolean hasNext() 
	{
		return mEnumeration.hasMoreElements();
	}

	public SpcfZipEntry next() 
	{
		return new SpcfZipEntryImpl((ZipEntry)mEnumeration.nextElement());
	}

	public void remove() 
	{	
		throw new SpcfUnsupportedOperationException();
	}		
}

class SpcfZipEntryReader extends SpcfStream
{
	private InputStream mStream;
	private boolean mClosed = false;
	
	SpcfZipEntryReader(InputStream stream)
	{	
		mStream = stream;
	}

	@Override
	public boolean canRead()
	{
		return ! mClosed;
	}
	
	@Override
	public boolean canSeek()
	{
		return false;
	}
	
	@Override
	public boolean canWrite()
	{
		return false;
	}

	@Override
	public byte readByte()
	{
		int b = 0;
		
		try
		{
			b = mStream.read();
		}
		catch(IOException ex)
		{
			throw new SpcfIOException(ex);
		}

		if (b == -1) throw new SpcfEofException();
		
		return (byte)b;
	}

	@Override
	public int read(byte[] buf, int offset, int length) 
	{	
		// Make sure input is available:
		ensureOpen();

		// Check parameters:
		SpcfParamValidator.checkArrayParams(buf, offset, length);
		
		// If we weren't asked to read anything, don't:
		if (length == 0) return 0;
		
		try
		{
			return mStream.read(buf, offset, length);
			
		}
		catch(IOException ex)
		{
			throw new SpcfIOException(ex);
		}
		catch(IndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}

	@Override
	public void close() 
	{
		try
		{
			mStream.close();
			mClosed = true; 
		}
		catch(IOException ex)
		{
			throw new SpcfIOException(ex);
		}
	}	
	
	@Override
	public long getPosition()
	{
		return 0;
	}
	
	@Override
	public long getLength()
	{
		return 0;
	}

	@Override
	public byte peekByte()
	{
		throw new SpcfInvalidOperationException();
	}
	
	private void ensureOpen()
    {
        if (mClosed)
        {
            throw new SpcfIOException("Stream closed");
        }
    }
	
	@Override
	public void flush()
	{
	}
	
	@Override
	public long seek(long position, SpcfSeekOriginEnum origin)
	{
		throw new SpcfInvalidOperationException();
	}
	
	@Override
	public void write(byte b)
	{
		throw new SpcfInvalidOperationException();
	}

	@Override
	public void write(byte[] buffer, int offset, int count)
	{
		throw new SpcfInvalidOperationException();
	}
}
