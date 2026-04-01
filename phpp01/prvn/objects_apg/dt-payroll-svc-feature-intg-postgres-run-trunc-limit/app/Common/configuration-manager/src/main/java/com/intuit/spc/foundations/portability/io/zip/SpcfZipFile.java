package com.intuit.spc.foundations.portability.io.zip;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.io.*;
import com.intuit.spc.foundations.portability.SpcfSecurityException;

/**
 * This class represents a Zip archive.  You can ask for the contained entries, or get an input stream for a file entry.  
 * The entry is automatically decompressed.
 *
 */
abstract public class SpcfZipFile 
{
	/**
     * Mode flag to open a zip file for reading.
     */
    public static final int OpenRead = 0x1;
        
    /**
     * Mode flag to open a zip file and mark it for deletion.  The file will be
     * deleted some time between the moment that it is opened and the moment
     * that it is closed, but its contents will remain accessible via the
     * SpcfZipFile object until either the close method is invoked or the 
     * virtual machine exits.
     */
    public static final int OpenDelete = 0x4;
    
	/**
	 * Opens a ZIP file for reading given the specified SpcfFile object.
	 * @param file the ZIP file to be opened for reading
	 * @throws SpcfArgumentNullException if file is null
	 * @throws SpcfFileNotFoundException if file is not found
	 * @throws SpcfZipException if a ZIP error has occurred 
	 * @throws SpcfIOException  if an I/O error has occurred
	 * @throws SpcfSecurityException if reading on the file is restricted
	 */
	public static SpcfZipFile createInstance(SpcfFile file)
	{	
		return SpcfFactory.getInstance().createZipFile(file);
	}
	
	/**
	 * Opens a zip file for reading.
	 * @param name the name of the zip file
	 * @throws SpcfArgumentNullException if file is null
	 * @throws SpcfFileNotFoundException if file is not found
	 * @throws SpcfZipException if a ZIP error has occurred 
	 * @throws SpcfIOException  if an I/O error has occurred
	 * @throws SpcfSecurityException if reading on the file is restricted 
	 */
	public static SpcfZipFile createInstance(String name)
	{		
		return SpcfFactory.getInstance().createZipFile(name);
	}
	
	/**
	 * Opens a new ZipFile to read from the specified File object in the specified mode. The mode argument must be 
	 * either OpenRead or OpenRead | OpenDelete. 
	 * @param file the ZIP file to be opened for reading
	 * @param mode the mode in which the file is to be opened
	 * @throws SpcfArgumentNullException if file is null
	 * @throws SpcfFileNotFoundException if file is not found
	 * @throws SpcfIllegalArgumentException If the mode argument is invalid
	 * @throws SpcfZipException if a ZIP error has occurred 
	 * @throws SpcfIOException  if an I/O error has occurred 
	 * @throws SpcfSecurityException if reading on the file is restricted
	 */	
	public static SpcfZipFile createInstance(SpcfFile file, int mode)
	{		
		return SpcfFactory.getInstance().createZipFile(file, mode);
	}
	
	/**
	 * Returns the zip file entry for the specified name, or null if not found. 
	 * @param name the name of the entry
	 * @return the zip file entry, or null if not found
	 * @throws SpcfArgumentNullException if name is null
	 * @throws SpcfIllegalStateException  if the zip file has been closed
	 */
	public abstract SpcfZipEntry getEntry(String name);
	
	/**
	 * Returns an input stream for reading the contents of the specified zip file entry. <p>
	 * 
	 * Closing this ZIP file will, in turn, close all input streams that have been returned by invocations of this method.
	 * @param entry the zip file entry
	 * @return the input stream for reading the contents of the specified zip file entry.
	 * @throws SpcfArgumentNullException if entry is null
	 * @throws SpcfZipException if a ZIP error has occurred 
	 * @throws SpcfIOException  if an I/O error has occurred 
	 * @throws SpcfIllegalStateException  if the zip file has been closed
	 */
	public abstract SpcfStream getInputStream(SpcfZipEntry entry);

	/**
	 * Returns the path name of the ZIP file. 
	 * @return the path name of the ZIP file
	 */
	public abstract String getName();
	
	/**
	 * Returns an iterator of the ZIP file entries. 
	 * @return an iterator of the ZIP file entries 
	 * @throws SpcfIllegalStateException  if the zip file has been closed
	 */	
	public abstract ISpcfIterator<SpcfZipEntry> getIterator();
	
	/**
	 * Returns the number of entries in the ZIP file. 
	 * @return the number of entries in the ZIP file 
	 */
	public abstract int getZipEntriesCount();
	
	/**
	 * Closes the ZIP file. <p>
	 * 
	 * Closing this ZIP file will close all of the input streams previously returned by invocations of the getInputStream method.
	 * @throws SpcfIOException  if an I/O error has occurred
	 */
	public abstract void close();
}
