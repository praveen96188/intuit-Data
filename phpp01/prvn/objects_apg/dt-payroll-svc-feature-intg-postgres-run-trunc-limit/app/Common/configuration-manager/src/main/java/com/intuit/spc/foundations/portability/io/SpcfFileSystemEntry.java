package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;

/**
 * The SpcfFileSystemInfo class contains methods that are common to file and
 * directory manipulation. An SpcfFileSystemInfo object can represent either a
 * file or a directory, thus serving as the base class for SpcfFile and SpcfDirectory
 * objects. Common use for this base class is when parsing/iterating through
 * files and directories.
 * 
 * @see SpcfFile
 * @see SpcfDirectory
 */
public abstract class SpcfFileSystemEntry
{
	/**
	 * Copies this file system entry to the target location.
	 * @param targetPath location to copy the file/directory to.
	 * @return true if the copy succeeded, false otherwise
	 * @throws SpcfSecurityException if a security exception occurs.
	 * @throws SpcfIOException if any other I/O exception occurs.
	 */
	public abstract boolean copy(String targetPath);

	
	/**
	 * Deletes the file system entry.
	 * An exception is not thrown if the specified file/directory does not exist.
	 * @return true if the file/directory exists, could be deleted, and was deleted; false otherwise
	 * @throws SpcfSecurityException if a security exception occurs
	 * @throws SpcfIOException if an I/O error occurred
	 */
	public abstract boolean delete();
	

	/**
	 * Does this file system entry actually exist on the file system?
	 * @return true if it exists, false otherwise
	 */
	public abstract boolean exists();


	/**
	 * Gets the name of the file/directory stripping out the drive, path, etc.
	 * @return name of the file/directory or empty string if this pathname's name sequence is empty 
	 */
	public abstract String getName();

	/**
	 * Gets the parent directory represented as an SpcfDirectory object or null if 
	 * this pathname does not name a parent directory.
	 * @return SpcfDirectory object representing the parent directory or null if there is no
	 * parent directory.
	 */
	public SpcfDirectory getParentDirectory()
	{
		String parentPath = this.getParentPath();
		if (parentPath == null)
			return null;
		return SpcfDirectory.createInstance(this.getParentPath());
	}
	

	/**
	 * Gets the parent directory's name, stripping out the drive, path, etc. 
	 * @return parent directory's name, empty string if not applicable
	 */
	public String getParentName()
	{
		String parentName = "";
		SpcfDirectory parentDir = this.getParentDirectory();
		if (parentDir != null)
		{
			parentName = parentDir.getName();
		}
		return parentName; 
	}
	
	
	/**
	 * Gets the full path to the parent directory in which this file/directory resides.
	 * @return absolute path to the parent directory if applicable, null if not applicable
	 */
	public abstract String getParentPath();
	
	/**
	 * Gets the path to the file/directory object, resolved to a canonical path.
	 * If a canonical path cannot be determined, this will attempt to resolve to 
	 * an absolute path. If an absolute path cannot be determined, this will return 
	 * the original path that was used to construct this object.
	 * @return fully resolved path to the file/directory
	 */
	public abstract String getPath();


	/**
	 * Does this object represent a directory? 
	 * This will return true if this object represents a directory even if the directory does not yet exist. 
	 * @return true if it is a directory, false otherwise
	 */
	public abstract boolean isDirectory();


	/**
	 * Does the specified path represent an existing directory? 
	 * Static method SpcfFileSystemEntry.isDirectory(path) is equivalent to static method SpcfDirectory.exists(path). 
	 * @param path file/directory to check
	 * @return true if it is an existing directory, false otherwise
	 */
	public static boolean isDirectory(String path)
	{
		return SpcfDirectory.exists(path);
	}
	
	
	/**
	 * Does this object represent a file? 
	 * This will return true if this object represents a file even if the file does not yet exist. 
	 * @return true if it is a file, false otherwise
	 */
	public abstract boolean isFile();
	

	/**
	 * Does the specified path represent an existing file?
	 * Static method SpcfFileSystemEntry.isFile(path) is equivalent to static method SpcfFile.exists(path). 
	 * @param path file/directory to check
	 * @return true if it is an existing file, false otherwise
	 */
	public static boolean isFile(String path)
	{
		return SpcfFile.exists(path);
	}
	

	/**
	 * Moves/renames the file/directory.
	 * @param newPath new location or name 
	 * @return true if the move succeeded, false otherwise
	 * @throws SpcfFileAlreadyExistsException if the target file/directory already exists
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public abstract boolean move(String newPath);
}
