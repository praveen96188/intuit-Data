/**
 * 
 */
package com.intuit.spc.foundations.portabilitySpecific.io;

import java.io.*;
import java.nio.channels.*;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.io.*;
import com.intuit.spc.foundations.portability.text.*;
import com.intuit.spc.foundations.portabilitySpecific.text.*;

/**
 *  SpcfReaderImpl wraps an SpcfReader around a binary stream.<br/><br/>
 *  This class can be constructed using an existing binary stream or it can be
 *  constructed from an SpcfFile reference.
 */
public class SpcfReaderImpl extends SpcfReader
{
	/**
	 * The internal buffered reader. 
	 */
	protected BufferedReader mBufferedReader = null;
	
	
	/**
	 * The text encoding that we use internally.
	 */
	protected SpcfEncoding mEncoding = null;
	
	
	/**
	 * The channel file lock that we are holding onto, if any.
	 */
	protected FileLock mFileLock = null;

	
	/**
	 * Creates a new reader that wraps the file passed. Uses the specified text encoding.
	 * @param file the SpcfFile to open and wrap
	 * @param encoding the text encoding to use
	 * @param fileLock specifies how the file should be locked or shared 
	 * @throws SpcfArgumentNullException if either file or encoding is null
	 * @throws SpcfFileNotFoundException if the file does not exist, is not a file, or cannot be opened for reading
	 * @throws SpcfSecurityException if a security exception is encountered while trying to open the file
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if the lock attempt fails
	 * @throws SpcfIOException if an IO exception occurs
	 */
	public SpcfReaderImpl(SpcfFileImpl file, SpcfEncoding encoding, SpcfFileLockEnum fileLock)
	{
		super();

		// Check params:
		SpcfParamValidator.checkIsNotNull(file, "File");
		SpcfParamValidator.checkIsNotNull(encoding, "Encoding");

		// Check that the file path is valid:
		String filePath = file.getPath();
		if (filePath == null || filePath == "" || SpcfFileSystemEntry.isDirectory(filePath)) throw new SpcfFileNotFoundException("Invalid file path.");

		// If exclusive locking was requested, throw an SpcfFileLockException:
		if (fileLock == SpcfFileLockEnum.Exclusive) throw new SpcfFileLockException("Cannot request exclusive locking when opening a file for reading.");

		// We will set this value if our reader is constructed correctly:
		boolean success = false;
		
		try
		{
			// Hang onto the encoding for the getEncoding() call:
			mEncoding = encoding;
			
			// Decorate a file input stream with an input stream reader.
			// Then decorate the input stream reader with a buffered reader.
			FileInputStream fis = new FileInputStream(file.toSpecific());
			InputStreamReader isr = new InputStreamReader(fis, 
					SpcfEncodingUtility.getEncodingName(encoding.getEncoding()));
			mBufferedReader = new BufferedReader(isr);

			// Set the locking options based on fileLock:
			if (fileLock != SpcfFileLockEnum.None) 
			{
				boolean doSharedLock = (fileLock == SpcfFileLockEnum.Shared);
				mFileLock = fis.getChannel().tryLock(0L, Long.MAX_VALUE, doSharedLock);
				if (mFileLock == null || !mFileLock.isValid()) throw new SpcfFileLockException();
			}
			
			// If we are here, then we were successful:
			success = true;
		}
		catch (FileNotFoundException fnfe)
		{
			// FileInputStream - if the file does not exist, is a directory rather than
			//		a regular file, or for some other reason cannot be opened for reading
			throw new SpcfFileNotFoundException(fnfe);
		}
		catch (SecurityException se)
		{
			// FileInputStream - if a security manager exists and its checkRead method 
			//		denies read access to the file.
			throw new SpcfSecurityException(se);
		}
		catch (UnsupportedEncodingException uee)
		{
			// InputStreamReader - if the named encoding is not supported
			throw new SpcfUnsupportedEncodingException(uee);
		}
		catch (IllegalArgumentException iae)
		{
			// TryLock - If the preconditions on the parameters do not hold 
			// Should not happen.
			throw new SpcfIOException(iae);
		}
		catch (ClosedChannelException  cce)
		{
			// TryLock - If this channel is closed.
			throw new SpcfIOException(cce);
		}
		catch (OverlappingFileLockException ofle)
		{
			// TryLock - If a lock that overlaps the requested region is already held by this Java 
			// 		virtual machine, or if another thread is already blocked in this method and  
			// 		is attempting to lock an overlapping region 
			throw new SpcfFileLockException(ofle);
		}
		catch (IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}		
		finally
		{
			if (!success) this.close();
		}
	}

	
	/**
	 * Creates a new reader that wraps the SpcfStream. Uses the specified text encoding.<br/><br/>
	 * If you will be using this class to read a file, use the SpcfReaderImpl constructor 
	 * that takes an SpcfFile.
	 * @param binaryStream the binary stream to wrap
	 * @param encoding the text encoding to use
	 * @throws SpcfArgumentNullException if either binaryStream or encoding is null
	 * @throws SpcfIllegalArgumentException if binaryStream is not readable
	 * @throws SpcfSecurityException if a security exception is encountered
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfIOException if an IO exception occurs
	 */
	public SpcfReaderImpl(SpcfStream binaryStream, SpcfEncoding encoding)
	{
		super();

		// Check params:
		SpcfParamValidator.checkIsNotNull(binaryStream, "Binary Stream");
		SpcfParamValidator.checkIsNotNull(encoding, "Encoding");

		// Make sure that the binary stream is readable:
		if (!binaryStream.canRead()) throw new SpcfIllegalArgumentException("Binary stream must be readable.");

		try
		{
			// Hang onto the encoding for the getEncoding() call:
			mEncoding = encoding;

			// Decorate the binary stream with an input stream.
			// Then decorate the input stream with an input stream reader.
			// Then decorate the input stream reader with a buffered reader.
			InputStream is = new SpcfInputStream(binaryStream);
			InputStreamReader isr = new InputStreamReader(is, 
					SpcfEncodingUtility.getEncodingName(encoding.getEncoding()));
			mBufferedReader = new BufferedReader(isr);
		}
		catch (SecurityException se)
		{
			this.close();
			throw new SpcfSecurityException(se);
		}
		catch (UnsupportedEncodingException uee)
		{
			this.close();
			throw new SpcfUnsupportedEncodingException(uee);
		}		
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public void close()
	{
		// Release the lock and let go of our pointer to it:
		try
		{
			if (mFileLock != null) mFileLock.release();
		}
		catch (ClosedChannelException cce)
		{
		}
		catch (IOException ioe)
		{
		}
		mFileLock = null;		
		
		// Release the buffered reader and let go of our pointer to it:
		try
		{
			if (mBufferedReader != null) mBufferedReader.close();
		}
		catch (IOException ioe)
		{
		}
		mBufferedReader = null;
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfEncoding getTextEncoding()
	{
		return mEncoding;
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public int read(char[] buffer, int offset, int count)
	{
		// If the buffered reader is disposed, throw an IO exception:
		if (mBufferedReader == null) throw new SpcfIOException();
		
		// Check the params:
		SpcfParamValidator.checkArrayParams(buffer, offset, count);
		
		// If we weren't asked to read anything, don't:
		if (count == 0) return 0;
		
		try
		{
			// Call the read method:
			return mBufferedReader.read(buffer, offset, count);
		}
		catch (IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public char readChar()
	{
		// If the buffered reader is disposed, throw an IO exception:
		if (mBufferedReader == null) throw new SpcfIOException();
		
		try
		{
			int n = mBufferedReader.read();
			if (n < 0 || n > 65535) throw new SpcfEofException();
			return (char)n;
		}
		catch (IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public String readLine()
	{
		// If the buffered reader is disposed, throw an IO exception:
		if (mBufferedReader == null) throw new SpcfIOException();
		
		try
		{
			return mBufferedReader.readLine();
		}
		catch (IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}
}
