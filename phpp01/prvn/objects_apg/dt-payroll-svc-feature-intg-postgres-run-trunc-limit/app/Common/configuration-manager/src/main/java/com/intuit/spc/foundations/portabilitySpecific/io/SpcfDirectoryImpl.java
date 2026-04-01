package com.intuit.spc.foundations.portabilitySpecific.io;

import java.io.File;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.io.*;

/**
 * Platform specific implementation of the SpcfDirectory
 */
public class SpcfDirectoryImpl extends SpcfDirectory
{
	/**
	 * Platform specific instance of the file
	 */
	protected File mFile;

	
	/**
	 * Instantiates with the path pointed to nowhere specific. In Java, this is
	 * called the "empty abstract pathname".
	 */
	public SpcfDirectoryImpl()
	{
		mFile = new File("");
	}

	
	/**
	 * Instantiates with the path pointed to the specified location. If a null
	 * path is passed in, this will point to nowhere specific.
	 * @param path
	 */
	public SpcfDirectoryImpl(String path)
	{
		mFile = new File(path == null ? "" : path);
	}
	

	/**
	 * Instantiates with the file passed in.
	 * @param file
	 */
	public SpcfDirectoryImpl(File file)
	{
		mFile = file;
	}

	
	/**
	 * Retrieves the platform-specific implementation.
	 * @return File object.
	 */
	public File toSpecific()
	{
		return mFile;
	}
	
	
	/**
	 * @inheritDoc
	 */
	@Override
	public String[] list()
	{
		try
		{
			if (SpcfFileUtil.isDirectory(mFile)) 
			{
				String[] fileList = mFile.list();
				return fileList;
			}
			return null;
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
		if (SpcfFileUtil.isDirectory(mFile)) return SpcfFileUtil.copy(mFile.getPath(), targetPath, false);
		return false;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public void createDirectory()
	{
		try
		{
			// This call will automatically create the parent directories,
			// if they do not already exist:
			mFile.mkdirs();
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
	public boolean delete()
	{
		try
		{
			if (SpcfFileUtil.isDirectory(mFile)) return mFile.delete();
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
		try
		{
			return SpcfFileUtil.isDirectory(mFile);
		} 
		catch (SecurityException se)
		{
			// throw new SpcfSecurityException(se);
			return false;
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
		// Must make sure that the source directory exists:
		if (!SpcfFileUtil.isDirectory(mFile)) return false;
		
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

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString()
	{
		return mFile.toString();
	}

}
