/**
 * 
 */
package com.intuit.spc.foundations.portabilitySpecific.io;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.*;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.io.*;


/**
 * This is a stream that is created when a file is opened up. This essentially proxies
 * for a random access file.
 */
public class SpcfFileStream extends SpcfStream
{
	/**
	 * The random access file that sits at the core of this object.
	 */
	protected RandomAccessFile mRandomAccessFile = null;
	
	
	/**
	 * The channel file lock that we are holding onto, if any.
	 */
	protected FileLock mFileLock = null;
	
	
	/**
	 * Can we read from this stream?
	 */
	protected boolean mCanRead = false;

	
	/**
	 * Can we seek within this stream?
	 */
	protected boolean mCanSeek = false;
	
	
	/**
	 * Can we write to this stream?
	 */
	protected boolean mCanWrite = false;
	
    /**
     * The file access for the current stream
     */
    protected SpcfFileAccessEnum mFileAccess;
	
	/**
	 * Creates a new file stream with the specified file access and sharing.
	 * @param file the SpcfFile to open and wrap
	 * @param fileAccess how the file will be accessed
	 * @param fileLock specifies how the file should be locked or shared 
	 * @throws SpcfArgumentNullException if file is null
	 * @throws SpcfFileNotFoundException if the file does not exist and cannot be created, exists but is not a file, or exists but cannot be opened for writing
	 * @throws SpcfSecurityException if a security exception is encountered while trying to open the file
	 * @throws SpcfFileLockException if the lock attempt fails
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public SpcfFileStream(SpcfFileImpl file, SpcfFileAccessEnum fileAccess, SpcfFileLockEnum fileLock)
	{
		super();

		// Check params:
		SpcfParamValidator.checkIsNotNull(file, "File");
		
		// Check that the file path is valid:
		String filePath = file.getPath();
		if (filePath == null || filePath == "" || SpcfFileSystemEntry.isDirectory(filePath)) throw new SpcfFileNotFoundException("Invalid file path.");

		// We will set this value if our stream is constructed correctly:
		boolean success = false;
		
		// Attempt to open the file, wrapping any exceptions that we encounter:
		try
		{
			// If we are opening for append....
			if (fileAccess == SpcfFileAccessEnum.Append)
			{
				// If shared locking was requested, throw an SpcfFileLockException:
				if (fileLock == SpcfFileLockEnum.Shared) throw new SpcfFileLockException("Cannot request shared locking when opening a file for writing.");
				
				// Create the file if it does not exist:
				if (!file.exists()) file.createFile();

				// Open the file:
				mRandomAccessFile = new RandomAccessFile(file.toSpecific(), "rw");

				// Seek to the end so we are ready for appending:
				mRandomAccessFile.seek(mRandomAccessFile.length());

				// Allow writing only:
				mCanWrite = true;
			}
			
			// If we are opening for read...
			else if (fileAccess == SpcfFileAccessEnum.Read)
			{
				// If exclusive locking was requested, throw an SpcfFileLockException:
				if (fileLock == SpcfFileLockEnum.Exclusive) throw new SpcfFileLockException("Cannot request exclusive locking when opening a file for reading.");
				
				// If the file does not exist, throw an exception
				if (!file.exists()) throw new SpcfFileNotFoundException();

				// Open the file:
				mRandomAccessFile = new RandomAccessFile(file.toSpecific(), "r");

				// Allow reading only:
				mCanRead = true;	
			}
			
			// If we are opening for write....
			if (fileAccess == SpcfFileAccessEnum.Write)
			{
				// If shared locking was requested, throw an SpcfFileLockException:
				if (fileLock == SpcfFileLockEnum.Shared) throw new SpcfFileLockException("Cannot request shared locking when opening a file for writing.");

				// If the file exists, delete it:
				if (file.exists()) file.delete();
				
				// Create the file if it does not exist:
				if (!file.exists()) file.createFile();

				// Open the file:
				mRandomAccessFile = new RandomAccessFile(file.toSpecific(), "rw");

				// Allow writing only:
				mCanWrite = true;
			}
			
			// If we are opening for random access...
			else if (fileAccess == SpcfFileAccessEnum.RandomAccess)
			{
				// If shared locking was requested, throw an SpcfFileLockException:
				if (fileLock == SpcfFileLockEnum.Shared) throw new SpcfFileLockException("Cannot request shared locking when opening a file for writing.");

				// Create the file if it does not exist:
				if (!file.exists()) file.createFile();

				// Open the file:
				mRandomAccessFile = new RandomAccessFile(file.toSpecific(), "rw");

				// Allow all operations:
				mCanRead = true;
				mCanWrite = true;
			}
			
            mCanSeek = true;
            
            mFileAccess = fileAccess;
            
			// Set the locking options based on fileLock:
			if (fileLock != SpcfFileLockEnum.None) 
			{
				boolean doSharedLock = (fileLock == SpcfFileLockEnum.Shared);
				mFileLock = mRandomAccessFile.getChannel().tryLock(0L, Long.MAX_VALUE, doSharedLock);
				if (mFileLock == null || !mFileLock.isValid()) throw new SpcfFileLockException();
			}
			
			// We were successful if we are here:
			success = true;
		}
		catch (FileNotFoundException fnfe)
		{
			throw new SpcfFileNotFoundException(fnfe);
		}
		catch (IllegalArgumentException iae)
		{
			// This exception can only occur if we mistyped "r", "w", or "rw".
			// Therefore, don't throw an illegal argument exception. Make it an IO exception.
			throw new SpcfIOException(iae);
		}
		catch (SecurityException se)
		{
			throw new SpcfSecurityException(se);
		}
		catch (NonWritableChannelException nrce)
		{
			// TryLock - The channel is nonwritable
			// This will happen if we attempt to open for read access with exclusive locking.
			throw new SpcfFileLockException(nrce);
		}
		catch (ClosedChannelException cce)
		{
			// TryLock - If this channel is closed 
			throw new SpcfIOException(cce);
		}
		catch (OverlappingFileLockException ofle)
		{
			// TryLock - If a lock that overlaps the requested region is already held by this 
			//		Java virtual machine, or if another thread is already blocked in this 
			//		method and is attempting to lock an overlapping region 
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
	 * Retrieves the platform-specific implementation behind this class.
	 * @return the random access file used
	 */
	public RandomAccessFile toSpecific()
	{
		return mRandomAccessFile;
	}
	
	
	/**
	 * @inheritDoc
	 */
	@Override
	public boolean canRead()
	{
		return (mRandomAccessFile != null && mCanRead);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public boolean canWrite()
	{
		return (mRandomAccessFile != null && mCanWrite);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public boolean canSeek()
	{
		return (mRandomAccessFile != null && mCanSeek);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public void close()
	{
		// Release the lock and let go of our pointer to it:
		if (mFileLock != null)
		{
			try
			{
				mFileLock.release();
			}
			catch (ClosedChannelException cce)
			{
			}
			catch (IOException ioe)
			{
			}
			
			mFileLock = null;
		}

		// Close the file and let go of our pointer to it:
		if (mRandomAccessFile != null)
		{
			try
			{
				mRandomAccessFile.getChannel().close();
			}
			catch (IOException ioe)
			{
			}

			try
			{
				mRandomAccessFile.close();
			}
			catch (IOException ioe)
			{
			}
			
			mRandomAccessFile = null;
		}
		
		// We can no longer read, write, or seek:
		mCanRead = false;
		mCanSeek = false;
		mCanWrite = false;
	}
	

	/**
	 * @inheritDoc
	 */
	@Override
	public void flush()
	{
		// Binary access is always written directly, so there really is no flushing to do.
		// However, if we are disposed, throw an IO exception.
		if (mRandomAccessFile == null) throw new SpcfIOException();
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public long getLength()
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		try
		{
			// Return the length:
			return mRandomAccessFile.length();
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
	public long getPosition()
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		try
		{
			return mRandomAccessFile.getFilePointer();
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
	public byte peekByte()
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		// If reading are not allowed, throw an exception:
		if (!mCanRead) throw new SpcfInvalidOperationException("Peeking is not supported.");

		try
		{
			// Get the current position:
			long currentPosition = mRandomAccessFile.getFilePointer();
			byte b = mRandomAccessFile.readByte();
			mRandomAccessFile.seek(currentPosition);
			return b;
		}
		catch (EOFException eof)
		{
			throw new SpcfEofException(eof);
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
	public int read(byte[] buffer, int offset, int count)
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		// If reading is not allowed, throw an exception:
		if (!mCanRead) throw new SpcfInvalidOperationException("Reading is not supported.");
		
		// If we weren't asked to read anything, don't:
		if (count == 0) return 0;
		
		// Check params:
		SpcfParamValidator.checkArrayParams(buffer, offset, count);
		
		try
		{
			// Read the bytes into the buffer and return the number of bytes read:
			return mRandomAccessFile.read(buffer, offset, count);
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
	public byte readByte()
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		// If reading is not allowed, throw an exception:
		if (!mCanRead) throw new SpcfInvalidOperationException("Reading is not supported.");
		
		try
		{
			// Read and return the byte:
			return mRandomAccessFile.readByte();
		}
		catch (EOFException eof)
		{
			throw new SpcfEofException(eof);
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
	public long seek(long position, SpcfSeekOriginEnum origin)
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		// If seeking is not allowed, throw an exception:
		if (!mCanSeek) throw new SpcfInvalidOperationException("Seeking is not supported.");
		
		try
		{
			// Calculate the new position:
			long newPosition = 0;
			if (origin == SpcfSeekOriginEnum.FromBeginning)
			{
				newPosition = position;
			}
			else if (origin == SpcfSeekOriginEnum.FromCurrentPosition)
			{
				newPosition = mRandomAccessFile.getFilePointer() + position;
			}
			else if (origin == SpcfSeekOriginEnum.FromEnd)
			{
				newPosition = mRandomAccessFile.length() + position;
			}
			
			// Validate that the new position is greater than or equal to 0:
			SpcfParamValidator.checkIsNonNegative(newPosition, "New seek position");
			
            // If in append mode, check that the stream is not asked to back up (to match C#).
            if ((newPosition < this.getPosition()) && (mFileAccess == SpcfFileAccessEnum.Append))
            {
                throw new SpcfIOException("Backwards seek not available in append mode.");
            }
            
			// Seek to the new position:
			mRandomAccessFile.seek(newPosition);
			
			// Return the new position from the beginning of the file:
			return newPosition;
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
	public void write(byte b)
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		// If writing is not allowed, throw an exception:
		if (!mCanWrite) throw new SpcfInvalidOperationException("Writing is not supported.");
		
		try
		{
			mRandomAccessFile.writeByte(b);
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
	public void write(byte[] buffer, int offset, int count)
	{
		// If the file is closed, throw an IO exception:
		if (mRandomAccessFile == null) throw new SpcfIOException();
		
		// If writing is not allowed, throw an exception:
		if (!mCanWrite) throw new SpcfInvalidOperationException("Writing is not supported.");

		try
		{
			mRandomAccessFile.write(buffer, offset, count);
		}
		catch (IOException ioe)
		{
			throw new SpcfIOException(ioe);
		}
	}
}
