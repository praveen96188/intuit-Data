package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.collections.*;

/**
 * SpcfDirectory represents a directory on the file system.
 */
public abstract class SpcfDirectory extends SpcfFileSystemEntry
{
	/**
	 * Creates an SpcfDirectory object that points to the specified path.
	 * @param path location of the directory
	 * @return SpcfDirectory object created through the factory
	 */
	public static SpcfDirectory createInstance(String path)
	{
		return SpcfFactory.getInstance().createDirectory(path);
	}
	
	
	/**
	 * Creates an SpcfDirectory object that points to the directory specified 
	 * by name under the specified path.
	 * @param path location of the directory
	 * @param name directory under the path
	 * @return SpcfDirectory object created through the factory
	 */
	public static SpcfDirectory createInstance(String path, String name)
	{
		// Construct a total path from the path and name: 
		String separator = SpcfSystem.getFileSeparator();
		String totalPath = path;
		if (!totalPath.endsWith(separator)) totalPath += separator;
		if (name.contains(separator)) name = name.substring(name.lastIndexOf(separator)); // Name starts with last separator
		totalPath += name;
		
		// Return the directory object:
		return SpcfFactory.getInstance().createDirectory(totalPath);
	}


	/**
	 * Copies the source directory to the target location.
	 * @param sourcePath directory to copy
	 * @param targetPath location to copy the directory to
	 * @return true if the copy succeeded, false otherwise
	 * @throws SpcfSecurityException if a security exception occurs.
	 * @throws SpcfIOException if any other I/O exception occurs.
	 */
	public static boolean copy(String sourcePath, String targetPath)
	{
		return createInstance(sourcePath).copy(targetPath);
	}


	/**
	 * Creates a directory with default access permissions defined. This will also
	 * create any necessary, but nonexistent parent directories. Note that if this
	 * operation fails, it may have succeeded in creating some of the necessary
	 * parent directories.
	 * Note: Do not confuse this with createInstance().
	 * @param path directory to create
	 * @return SpcfDirectory that corresponds to the newly created directory, if it could be created.
	 * @throws SpcfSecurityException if the directory cannot be created
	 * @throws SpcfIOException if an I/O error occurred
	 */
	public static SpcfDirectory createDirectory(String path)
	{
		SpcfDirectory dir = createInstance(path); 
		dir.createDirectory();
		return dir;
	}

	
	/**
	 * Creates a directory with default access permissions defined. This will also
	 * create any necessary, but nonexistent parent directories. Note that if this
	 * operation fails, it may have succeeded in creating some of the necessary
	 * parent directories.
	 * Note: Do not confuse this with createInstance().
	 * @throws SpcfSecurityException if the directory cannot be created
	 * @throws SpcfIOException if an I/O error occurred
	 */
	public abstract void createDirectory();
	

	/**
	 * Deletes the directory.
	 * An exception is not thrown if the specified directory does not exist. 
	 * @param path directory to delete
	 * @return true if the directory exists, could be deleted, and was deleted; false otherwise
	 * @throws SpcfSecurityException if a security exception occurs
	 * @throws SpcfIOException if an I/O error occurred
	 */
	public static boolean delete(String path)
	{
		return createInstance(path).delete();
	}
	

	/**
	 * Does the directory exist?
	 * @param path directory to check
	 * @return true if it exists, false otherwise
	 */
	public static boolean exists(String path)
	{
		return createInstance(path).exists();
	}
	

	/**
	 * If this is a directory that exists, retrieves a list of files/directories that are in the directory. 
	 * If the directory is empty, an empty string array is returned (not null).
	 * However, if this is not a valid directory or an I/O exception occurs, then this will return null.
	 * @return String[] of file/directory names without the full path; null if not a valid, existing directory.
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public abstract String[] list();

	
	/**
	 * If the path parameter is a directory that exists, retrieves a list of files/directories 
	 * that are in the directory. 
	 * @param path directory to list the contents of
	 * @return String[] of file/directory names without the full path
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public static String[] list(String path)
	{
		return createInstance(path).list();
	}
	

	/**
	 * If this is a directory that exists, retrieves a list of files/directories that are in the directory. 
	 * @return SpcfFileSystemEntry[] of files/directories; null if it is not a valid, existing directory.
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public SpcfFileSystemEntry[] listEntries()
	{
		// Get the list of files/directories as a string[]:
		String[] arrFileNames = this.list();
		
		// If the file names are empty, return null:
		if (arrFileNames == null) return null;
		
		// Get the path to this directory:
		String path = this.getPath();
		
		// Make an array of file system entries out of the list of file names:
		SpcfFileSystemEntry[] arrFSE = new SpcfFileSystemEntry[arrFileNames.length * 2];
		int nPosition = 0;
		for (int n = 0; n < arrFileNames.length; n++)
		{
			SpcfFile file = SpcfFile.createInstance(path, arrFileNames[n]);
			SpcfDirectory dir = SpcfDirectory.createInstance(path, arrFileNames[n]); 
			if (file.exists()) 
			{
				arrFSE[nPosition] = file;
				nPosition++;
			}
			if (dir.exists())
			{
				arrFSE[nPosition] = dir;
				nPosition++;
			}
		}
		
		// Return the array of file system entries:
		return SpcfArraysUtil.resize(arrFSE, nPosition);
	}

	
	/**
	 * If the path parameter is a directory that exists, retrieves a list of files/directories 
	 * that are in the directory. 
	 * @param path directory to list the contents of
	 * @return SpcfFileSystemEntry[] of files/directories
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public static SpcfFileSystemEntry[] listEntries(String path)
	{
		return createInstance(path).listEntries();
	}


	/**
	 * Moves/renames the directory from oldPath to newPath. 
	 * @param oldPath old path of directory
	 * @param newPath new path of directory 
	 * @return true if the move succeeded, false otherwise
	 * @throws SpcfFileAlreadyExistsException if the target directory already exists
	 * @throws SpcfSecurityException if a security exception is encountered
	 */
	public static boolean move(String oldPath, String newPath)
	{
		return createInstance(oldPath).move(newPath);
	}
}
