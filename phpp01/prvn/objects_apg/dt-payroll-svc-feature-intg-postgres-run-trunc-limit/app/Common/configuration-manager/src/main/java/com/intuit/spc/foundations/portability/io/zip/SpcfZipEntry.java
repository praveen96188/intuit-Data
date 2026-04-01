package com.intuit.spc.foundations.portability.io.zip;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * This class represents an entry in a zip archive.  This can be a file or a directory.
 * SpcfZipReader will give you instances of this class as information about the members in an archive.  
 * SpcfZipWriter uses an instance of this class when creating an entry in a Zip file.
 */
abstract public class SpcfZipEntry 
{	
	/**
	 * Creates a zip entry with the given name. 
	 * 
	 * @param zipEntryName The name for this entry. Can include directory components. 
     * The convention for names is 'unix' style paths with no device names and path 
     * elements separated by '/' characters.
	 * @return zip entry
	 * @throws SpcfArgumentNullException if the entry name is null 
	 * @throws SpcfIllegalArgumentException if the entry name is longer than 0xFFFF (65535) bytes
	 */
	public static SpcfZipEntry createInstance(String zipEntryName)
	{	
		return SpcfFactory.getInstance().createZipEntry(zipEntryName);
	}
	
	/**
	 * Returns the entry name. The path components in the entry should always separated by slashes ('/').
	 * Dos device names like C: should also be removed.
	 * @return the name of the entry
	 */
	public abstract String getName();
	
	/**	
	 * Sets the modification time of the entry. 
	 * @param time the entry modification time. If null is passed, current time will be set. 
	 * Milliseconds are discarded from the time parameter.   
	 */
	public abstract void setTime(SpcfCalendar time);
	
	/**
	 * Returns the modification time of the entry. 
	 * @return the modification time of the entry. If the time was never explicitly set, 
     * the time when the entry was created is returned.
	 */
	public abstract SpcfCalendar getTime();
	
	/**
	 * Sets the uncompressed size of the entry data. 
	 * @param size the uncompressed size in bytes
	 * @throws SpcfIllegalArgumentException if the specified size is less than 0 or greater 
     * than 0xFFFFFFFF bytes
	 */
	public abstract void setSize(long size);
	
	/**
	 * Returns the uncompressed size of the entry data, or -1 if not known. 
	 * @return the uncompressed size of the entry data, or -1 if not known
	 */
	public abstract long getSize();
	
	/**
	 * Returns the size of the compressed entry data, or -1 if not known. In the case of a stored entry, 
     * the compressed size will be the same as the uncompressed size of the entry. 
	 * @return the size of the compressed entry data, or -1 if not known
	 */
	public abstract long getCompressedSize();
	
	/**
	 * Sets the size of the compressed entry data.
	 * @param csize the compressed size to set to
	 * @throws SpcfIllegalArgumentException Size is not in the range 0...0xFFFFFFFF
	 */
	public abstract void setCompressedSize(long csize);
	
	/**
	 * Sets the CRC-32 checksum of the uncompressed entry data.
	 * @param crc  the CRC-32 value
	 * @throws SpcfIllegalArgumentException  Crc is not in the range 0..0xFFFFFFFF
	 */
	public abstract void setCrc(long crc);
	
	/**
	 * Returns the CRC-32 checksum of the uncompressed entry data, or -1 if not known.
	 * @return the CRC-32 checksum of the uncompressed entry data, or -1 if not known
	 */
	public abstract long getCrc();
	
	/**
	 * Sets the compression method for the entry. 
	 * @param method the compression method, either Stored or Deflated
	 * @throws SpcfIllegalArgumentException  if the specified compression method is invalid 
	 */
	public abstract void setCompressionMethod(SpcfCompressionMethod method);
	
	/**
	 * Returns the compression method of the entry, or unknown if not specified. 
	 * @return the compression method of the entry, or unknown if not specified.
	 */
	public abstract SpcfCompressionMethod getCompressionMethod();
	
	/**
	 * Sets the optional comment string for the entry.
	 * @param comment the comment string
	 * @throws SpcfIllegalArgumentException if the length of the specified comment string is greater 
     * than 0xFFFF bytes
	 */
	public abstract void setComment(String comment);
	
	/**
	 * Returns the comment string for the entry, or null if none. 
	 * @return the comment string for the entry, or null if none
	 */
	public abstract String getComment();
	
	/**
	 * Returns true if this is a directory entry. A directory entry is defined to be one whose name 
     * ends with a '/'. 
	 * @return true if this is a directory entry
	 */
	public abstract boolean isDirectory();	
}
