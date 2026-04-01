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
 * SpcfWriterImpl wraps an SpcfWriter around a binary stream.<br/><br/>
 * This class can be constructed using an existing binary stream or it can be
 * constructed from an SpcfFile reference.
 */
public class SpcfWriterImpl extends SpcfWriter
{
	/**
	 * The internal buffered reader. 
	 */
	protected BufferedWriter mBufferedWriter = null;
	
	
	/**
	 * The text encoding that we use internally.
	 */
	protected SpcfEncoding mEncoding = null;
	
	
	/**
	 * The channel file lock that we are holding onto, if any.
	 */
	protected FileLock mFileLock = null;

	
	/**
	 * Creates a new SpcfWriter that wraps a file. Uses the specified text encoding.
	 * @param file the SpcfFile to open and wrap
	 * @param encoding the text encoding to use
	 * @param fileLock specifies how the file should be locked or shared 
	 * @throws SpcfArgumentNullException if either file, encoding, or lock is null
	 * @throws SpcfFileNotFoundException if the file does not exist and cannot be created, exists but is not a file, or exists but cannot be opened for writing
	 * @throws SpcfSecurityException if a security exception is encountered while trying to open the file
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if the lock attempt fails
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public SpcfWriterImpl(SpcfFileImpl file, SpcfEncoding encoding, SpcfFileLockEnum fileLock, boolean append)
	{
		super();
		
		// Check params:
		SpcfParamValidator.checkIsNotNull(file, "File");
		SpcfParamValidator.checkIsNotNull(encoding, "Encoding");
		SpcfParamValidator.checkIsNotNull(fileLock, "File Lock");

		// Check that the file path is valid:
		String filePath = file.getPath();
		if (filePath == null || filePath == "" || SpcfFileSystemEntry.isDirectory(filePath)) throw new SpcfFileNotFoundException("Invalid file path.");

		// If shared file locking is requested, throw an exception:
		if (fileLock == SpcfFileLockEnum.Shared) throw new SpcfFileLockException("Cannot request shared locking when opening a file for writing.");
		
		// We will set this value if our writer is constructed correctly:
		boolean success = false;

		try
		{
			// Hang onto the encoding for the getEncoding() call:
			mEncoding = encoding;
			
			// Decorate a file output stream with an output stream writer.
			// Then decorate the output stream writer with a buffered writer.
			FileOutputStream fos = new FileOutputStream(file.toSpecific(), append);
			OutputStreamWriter osr = new OutputStreamWriter(fos, 
					SpcfEncodingUtility.getEncodingName(encoding.getEncoding()));
			mBufferedWriter = new BufferedWriter(osr);
			
			// Set the locking options based on fileLock:
			if (fileLock != SpcfFileLockEnum.None) 
			{
				boolean doSharedLock = (fileLock == SpcfFileLockEnum.Shared);
				mFileLock = fos.getChannel().tryLock(0L, Long.MAX_VALUE, doSharedLock);
				if (mFileLock == null || !mFileLock.isValid()) throw new SpcfFileLockException();
			}	
			
			// If we are here, then we were successful:
			success = true;
  		}
		catch (FileNotFoundException fnfe)
		{
			// FileOutputStream - if the file exists but is a directory rather than a regular file, 
			// 		does not exist but cannot be created, or cannot be opened for any other reason 
			throw new SpcfFileNotFoundException(fnfe);
		}
		catch (SecurityException se)
		{
			// FileOutputStream - if a security manager exists and its checkWrite method denies 
			//		write access to the file.
			throw new SpcfSecurityException(se);
		}
		catch (UnsupportedEncodingException uee)
		{
			// OutputStreamWriter - if the named encoding is not supported
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
		catch (NonReadableChannelException nrce)
		{
			// TryLock - If a shared lock is being opened on a write, then this error will be thrown.
			throw new SpcfFileLockException(nrce);
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
	 * Creates a new SpcfWriter that wraps the binary stream. Uses the specified text encoding.<br/><br/>
	 * If you will be using this class to write to a file, use the SpcfWriterImpl constructor 
	 * that takes an SpcfFile.
	 * @param binaryStream the binary stream to wrap
	 * @param encoding the text encoding to use
	 * @throws SpcfArgumentNullException if either binaryStream or encoding is null
	 * @throws SpcfIllegalArgumentException if binaryStream is not writable
	 * @throws SpcfSecurityException if a security exception is encountered while trying to open the file
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 */
	public SpcfWriterImpl(SpcfStream binaryStream, SpcfEncoding encoding)
	{
		super();

		// Check params:
		SpcfParamValidator.checkIsNotNull(binaryStream, "Binary Stream");
		SpcfParamValidator.checkIsNotNull(encoding, "Encoding");

		// Make sure that the binary stream is writable:
		if (!binaryStream.canWrite()) throw new SpcfIllegalArgumentException("Binary stream must be writable.");
		
		try
		{
			// Hang onto the encoding for the getEncoding() call:
			mEncoding = encoding;
			
			// Decorate the binary stream with an output stream.
			// Then decorate the output stream with an output stream writer.
			// Then decorate the output stream writer with a buffered writer.
			OutputStream os = new SpcfOutputStream(binaryStream);
			OutputStreamWriter osr = new OutputStreamWriter(os, 
					SpcfEncodingUtility.getEncodingName(encoding.getEncoding()));
			mBufferedWriter = new BufferedWriter(osr);
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

		// Release the buffered writer and let go of our pointer to it:
		try
		{
			if (mBufferedWriter != null) mBufferedWriter.close();
		}
		catch (IOException ioe)
		{
		}
		mBufferedWriter = null;
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public void flush()
	{
		// If the buffered writer is disposed, throw an IO exception:
		if (mBufferedWriter == null) throw new SpcfIOException();
		
		try
		{
			mBufferedWriter.flush();
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
	public SpcfEncoding getTextEncoding()
	{
		return mEncoding;
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public void write(char c)
	{
		// If the buffered writer is disposed, throw an IO exception:
		if (mBufferedWriter == null) throw new SpcfIOException();
		
		try
		{
			mBufferedWriter.write(c);
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
	public void write(char[] buffer, int offset, int count)
	{
		// If the buffered writer is disposed, throw an IO exception:
		if (mBufferedWriter == null) throw new SpcfIOException();
		
		// Check the params:
		SpcfParamValidator.checkArrayParams(buffer, offset, count);
		
		try
		{
			// Call the write method:
			mBufferedWriter.write(buffer, offset, count);
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
	public void write(String s)
	{
		// If the buffered writer is disposed, throw an IO exception:
		if (mBufferedWriter == null) throw new SpcfIOException();
		
		// Make sure string is not null
		SpcfParamValidator.checkIsNotNull(s, "s");
		
		try
		{
			mBufferedWriter.write(s);
		}
		catch (IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}
}
