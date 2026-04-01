/**
 * 
 */
package com.intuit.spc.foundations.portabilitySpecific.io;

import java.io.*;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.io.*;
import com.intuit.spc.foundations.portability.text.*;

/**
 * Provides the implementation details to connect the portable SpcfFile to the
 * actual Java platform.
 */
public class SpcfFileImpl extends SpcfFile
{
	/**
	 * Platform specific instance of the file
	 */
	protected File mFile;

	
	/**
	 * Instantiates with the path pointed to nowhere specific. In Java, this is
	 * called the "empty abstract pathname".
	 */
	public SpcfFileImpl()
	{
		mFile = new File("");
	}

	
	/**
	 * Instantiates with the path pointed to the specified location. If a null
	 * path is passed in, this will point to nowhere specific.
	 * 
	 * @param path
	 */
	public SpcfFileImpl(String path)
	{
		mFile = new File(path == null ? "" : path);
	}

	
	/**
	 * Instantiates with the file passed in.
	 * @param file
	 */
	public SpcfFileImpl(File file)
	{
		mFile = file;
	}

	
	/**
	 * Retrieves the platform-specific implementation.
	 * 
	 * @return File object.
	 */
	public File toSpecific()
	{
		return mFile;
	}
	

	/**
	 * @inheritDoc
	 * @see com.intuit.spc.foundations.portability.io.SpcfFile#canRead()
	 */
	@Override
	public boolean canRead()
	{
		try
		{
			if (SpcfFileUtil.isFile(mFile)) return mFile.canRead();
			return false;
		} 
		catch (SecurityException se)
		{
			throw new SpcfSecurityException(se);
		}
	}
	

	/**
	 * @inheritDoc
	 * @see com.intuit.spc.foundations.portability.io.SpcfFile#canWrite()
	 */
	@Override
	public boolean canWrite()
	{
		try
		{
			if (SpcfFileUtil.isFile(mFile)) return mFile.canWrite();
			return false;
		} 
		catch (SecurityException se)
		{
			throw new SpcfSecurityException(se);
		}
	}
	

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean copy(String targetPath)
	{
		if (SpcfFileUtil.isFile(mFile)) return SpcfFileUtil.copy(mFile.getPath(), targetPath, false);
		return false;
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public void createFile()
	{
		try
		{
			mFile.createNewFile();
		} 
		catch (SecurityException se)
		{
			throw new SpcfSecurityException(se);
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
	public boolean delete()
	{
		try
		{
			if (SpcfFileUtil.isFile(mFile)) return mFile.delete();
			return false;
		} 
		catch (SecurityException se)
		{
			throw new SpcfSecurityException(se);
		}
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public boolean exists()
	{
		//return SpcfFileUtil.isFile(mFile) && mFile.exists();
		return SpcfFileUtil.isFile(mFile);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public long getLength()
	{
		try
		{
			if (SpcfFileUtil.isFile(mFile)) return mFile.length();
			return 0L;
		} 
		catch (SecurityException se)
		{
			throw new SpcfSecurityException(se);
		}
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public String getPath()
	{
		return SpcfFileUtil.getPath(mFile);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isDirectory()
	{
		return SpcfFileUtil.isDirectory(mFile);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isFile()
	{
		return SpcfFileUtil.isFile(mFile);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public boolean move(String newPath)
	{
		// Must make sure that the source file exists:
		if (!SpcfFileUtil.isFile(mFile)) return false;
		
		// Move the file:
		if (SpcfFileUtil.move(mFile, newPath))
		{
			// Point to the new path:
			mFile = new File(newPath);
			
			// Return success:
			return true;
		}
		
		// We were unsuccessful:
		return false;
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfStream openForBinaryWriting(SpcfFileLockEnum fileLock)
	{
		return new SpcfFileStream(this, SpcfFileAccessEnum.Write, fileLock);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfStream openForBinaryRandomAccess(SpcfFileLockEnum fileLock)
	{
		return new SpcfFileStream(this, SpcfFileAccessEnum.RandomAccess, fileLock);
	}
	

	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfStream openForBinaryReading(SpcfFileLockEnum fileLock)
	{
		return new SpcfFileStream(this, SpcfFileAccessEnum.Read, fileLock);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfWriter openForTextAppending(SpcfFileLockEnum fileLock, SpcfEncoding encoding)
	{
		return new SpcfWriterImpl(this, encoding, fileLock, true);
	}
	
	
	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfReader openForTextReading(SpcfFileLockEnum fileLock, SpcfEncoding encoding)
	{
		return new SpcfReaderImpl(this, encoding, fileLock);
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfWriter openForTextWriting(SpcfFileLockEnum fileLock, SpcfEncoding encoding)
	{
		return new SpcfWriterImpl(this, encoding, fileLock, false);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public String toString()
	{
		return mFile.toString();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getParentPath()
	{
		return mFile.getParent();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getName()
	{
		return mFile.getName();
	}

}
