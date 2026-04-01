package com.intuit.spc.foundations.portabilitySpecific.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.io.*;

import java.io.*;

/**
 * This class is a utility for copying a file.
 **/
class SpcfFileUtil 
{
	/**
     * Copies a file or directory.
     *
     * @param sourceFileName the name of the file to copy
     * @param destFileName the name of the new file to copy to
     * @param overwrite specify if the destFileName exists to override it
     *
     * @return true if the copy operation succeeded, false if the destFileName already exists
     * and override set to false or if the sourceFileName does not exist
     *
     * @throws SpcfSecurityException if no access granted to the file
     * @throws SpcfFileAlreadyExistsException if the destFileName already exists
     * @throws SpcfIOException if an I/O event caused failure to copy the file
    */
	public static boolean copy(String sourceFileName, String destFileName, boolean overwrite)
	{
		File sourceFile = new File(sourceFileName);  // Get File objects from Strings
	    File destinationFile = new File(destFileName);
	    
	    // Make sure the source exists:
	    if (!sourceFile.exists()) return false;
	    
	    // If the source is a directory, invoke the copyDirectory method:
	    if (sourceFile.isDirectory()) 
    	{
	    	try
	    	{
	    		return copyDirectory(sourceFile, destinationFile, overwrite);
	    	}
	    	catch (IOException ioe)
	    	{
	    		throw new SpcfIOException(ioe);
	    	}
    	}
	    
	    // At this point, ensure it's a readable file:
	    if (!sourceFile.isFile() || !sourceFile.canRead()) return false;
	     
	    //if the destination file exists, check that the destination file is valid
	    if (destinationFile.exists())
	    {
	    	// Make sure that destinationFile is a file:
	    	if (!destinationFile.isFile()) return false;

	    	// If we're not allowed to overwrite, then throw an exception:
	    	if (!overwrite) throw new SpcfFileAlreadyExistsException();

	    	// If we're here, we can overwrite, so check if the destinationFile is writable
	    	if (!destinationFile.canWrite()) return false;

	    	// At this point we know destinationFile is a valid writable file
	    }
	    else 
	    {  
	        // If the file doesn't exist, check if directory exists and is writeable.
	        // If getParent() returns null, then the directory is the current dir.
	        // so look up the user.dir system property to find out what that is.
	        String parent = destinationFile.getParent();  // Get the destination directory
	        if (parent == null) parent = System.getProperty("user.dir"); // or current working dir
	        File dir = new File(parent);          // Convert it to a file.
	        if (!dir.exists() || dir.isFile() || !dir.canWrite())
	        {
		    	throw new SpcfIOException();
	        }
	    }    
	    
	    // If we've gotten this far, then everything is okay.
	    // So we copy the file, a buffer of bytes at a time.
	    FileInputStream from = null;  // Stream to read from source
	    FileOutputStream to = null;   // Stream to write to destination
	    try 
	    {
			from = new FileInputStream(sourceFile);      // Create input stream
			to = new FileOutputStream(destinationFile);  // Create output stream
			byte[] buffer = new byte[4096];              // A buffer to hold file contents
			int bytesRead;                               // How many bytes in buffer
			// Read a chunk of bytes into the buffer, then write them out, 
			// looping until we reach the end of the file (when read() returns -1).
			// Note the combination of assignment and comparison in this while
			// loop.  This is a common I/O programming idiom.
			while((bytesRead = from.read(buffer)) != -1) // Read bytes until EOF
			{
				to.write(buffer, 0, bytesRead);            //   write bytes
			}
	    }
	    catch(FileNotFoundException fnfe)
		{
	    	//this shouldn't happen because we already checked, but we need to catch
	    	throw new SpcfIOException(fnfe);
	    }
	    catch(SecurityException se)
	    {
	    	throw new SpcfSecurityException(se);
	    }
	    catch(IOException ioe)
		{
	    	throw new SpcfIOException(ioe);
	    }
	    // Always close the streams, even if exceptions were thrown
	    finally 
	    {
	    	if (from != null) try { from.close(); } catch (IOException e) { throw new SpcfIOException(e); }
	    	if (to != null) try { to.close(); } catch (IOException e) { throw new SpcfIOException(e); }
	    }
	    return true;
	}
	
	
	/**
     * Copies a directory.
     *
     * @param dirSource the file/directory to copy
     * @param dirTarget the file/directory to copy to
     * @param overwrite specify if the destFileName exists to override it
     *
     * @return true if the copy operation succeeded, false if the dirTarget already exists
     * and override set to false or if the dirSource does not exist
     *
     * @throws SpcfSecurityException if no access granted to the file
     * @throws SpcfFileAlreadyExistsException if the dirTarget already exists
     * @throws SpcfIOException if an I/O event caused failure to copy the file
     * @throws IOException if an I/O event caused a failure
    */
    protected static boolean copyDirectory(File dirSource, File dirTarget, boolean overwrite) throws IOException
    {
    	// If the target directory does not exist, make it:
        if (!dirTarget.exists()) dirTarget.mkdir(); 

        // Iterate over the children:
        String[] children = dirSource.list();
        for (int i = 0; i < children.length; i++) 
        {
        	String sourceFileName = new File(dirSource, children[i]).getCanonicalPath();
        	String targetFileName = new File(dirTarget, children[i]).getCanonicalPath();
            if (!copy(sourceFileName, targetFileName, overwrite)) return false;
        }
        
        // If we are here, presume success:
        return true;
    }
    
    
	/**
	 * Does the specified file represent an existing directory? 
	 * @param file file/directory to check
	 * @return true if it is an existing directory, false otherwise
	 */
    public static boolean isDirectory(File file)
    {
		try
		{
			return file.isDirectory();
		} 
		catch (SecurityException se)
		{
			// throw new SpcfSecurityException(se);
			return false;
		}    	
    }

    
	/**
	 * Does the specified path represent an existing file?
	 * @param file file/directory to check
	 * @return true if it is an existing file, false otherwise
	 */
    public static boolean isFile(File file)
    {
		try
		{
			return file.isFile();
		} 
		catch (SecurityException se)
		{
			// throw new SpcfSecurityException(se);
			return false;
		}    	
    }
    

	/**
	 * Moves a file/directory to the new location. 
	 * @param file file/directory to move
	 * @param newPath new location or name 
	 * @return true if the move succeeded, false otherwise
	 * @throws SpcfFileAlreadyExistsException if the target file/directory already exists
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public static boolean move(File file, String newPath)
	{
		// Must make sure that the source file exists:
		if (!file.exists()) return false;

		// Make sure the destination file does not already exist or throw an exception:
		File targetFile = new File(newPath);
		if (targetFile.exists()) throw new SpcfFileAlreadyExistsException();
		
		// Attempt the rename and return the success of that operation:
		try
		{
			// If the source and target are the same, return false:
			if (file.getCanonicalPath() == targetFile.getCanonicalPath()) return false;

			// Attempt to rename the file, returning the status of the move:
			return file.renameTo(targetFile); 
		} 
		catch (NullPointerException npe)
		{
			return false;
		}
		catch (SecurityException se)
		{
			throw new SpcfSecurityException(se);
		}
		catch (IOException ioe)
		{
			// This may occur if the file system queries necessary to resolve the
			// canonical path name fails. Assume it's a security exception.
			throw new SpcfSecurityException(ioe);
		}
	}
	
	
	/**
	 * Gets the path to the file/directory object, resolved to a canonical path.
	 * If a canonical path cannot be determined, this will attempt to resolve to 
	 * an absolute path. If an absolute path cannot be determined, this will return 
	 * the original path that was used to construct this object.
	 * @param file object of type File to get the full path of
	 * @return fully resolved path to the file/directory
	 */
	public static String getPath(File file)
	{
		String path = "";
		
		// Try to get the unique, fully resolved canonical path:
		try
		{
			path = file.getCanonicalPath();
		}
		catch (Exception eCanonical)
		{
			// The attempt to retrieve the canonical path failed, 
			// so try to get the absolute path:
			try
			{
				path = file.getAbsolutePath();
			}
			catch (Exception eAbsolute)
			{
				// The attempt to retrieve the absolute path failed, 
				// so just return the path that we were constructed with:
				path = file.getPath();
			}
		}
		
		path = removeTrailingFileSeparator(path);
		
		// Return the path:
		return path;
	}
	
	/**
	 * remove trailing separators but not on a drive root
	 * @param path the pathname to trim the trailing file separator
	 * @return the path name with trailing separators removed if not root
	 */
	public static String removeTrailingFileSeparator(String path)
	{
		String tmpPath = path;
		if (!isRoot(tmpPath))
		{
			// If our path ends with the separator, chop it:
			String separator = SpcfSystem.getFileSeparator();
			while (tmpPath.endsWith(separator)) 
			{
				tmpPath = tmpPath.substring(0,tmpPath.length()-1);
			}
		}
		return tmpPath;
	}

	
	/**
	 * determine if a given path is a root.
	 * @param path the path to determine if it is a root
	 * @return true if the path parameter is a root, false if the path is not a root
	 */
	public static boolean isRoot(String path)
	{
		if (path == null)
			return false;
		String[] roots = getRootsArray();
		if (roots == null)
			return false;
		for (int i = 0; i < roots.length; i++)
		{
			if (path.equalsIgnoreCase(roots[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Utility method to return the string array of roots for the computer
	 * @return an array of strings with one for each path of each drive on the computer
	 */
	public static String[] getRootsArray()
	{
		File[] roots = File.listRoots();
		String[] rootsArray = new String[roots.length];
		
		for (int i = 0; i < roots.length; i++)
		{
			rootsArray[i] = roots[i].getAbsolutePath();
		}
		return rootsArray;
	}

}
