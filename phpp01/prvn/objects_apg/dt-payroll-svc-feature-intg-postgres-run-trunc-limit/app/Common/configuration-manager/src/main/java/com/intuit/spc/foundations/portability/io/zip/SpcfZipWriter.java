package com.intuit.spc.foundations.portability.io.zip;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.io.*;


/**
 * This class writes the files into a zip archive one after another.  It has a special method to start a new
 * zip entry.  The zip entries contain information about the file name size, compressed size, CRC, etc.<p>
 * 
 * It includes support for Stored and Deflated entries.
 */
abstract public class SpcfZipWriter extends SpcfStream
{
	/**
	 * Creates a new ZIP writer.
	 * @param zipOut binary file writer
	 * @return zip file writer.
	 * @throws SpcfArgumentNullException if zipOut is null
	 */
	public static SpcfZipWriter createInstance(SpcfStream zipOut)
	{
		return SpcfFactory.getInstance().createZipWriter(zipOut);	
	}
	
	/**
	 * Sets the ZIP file comment.
	 * @param comment the comment string
	 * @throws SpcfIllegalArgumentException  if the length of the specified ZIP file comment is greater than 
     * 0xFFFF bytes
	 */
	public abstract void setComment(String comment);
	
	/**
	 * Sets the default compression method for subsequent entries. This default will be used whenever the compression 
	 * method is not specified for an individual ZIP file entry, and is initially set to Deflated.
	 * @param method the default compression method 
	 * @throws SpcfIllegalArgumentException if the method is not either Deflated or Stored. 
	 */
	public abstract void setDefaultCompressionMethod(SpcfCompressionMethod method);
	
	/**
	 * Sets the compression level for subsequent entries which are Deflated. The new level will be activated immediately. 
	 * @param level the compression level (0-9). If -1 is passed, default compression level will be used.
	 * @throws SpcfIllegalArgumentException  if the compression level is invalid
	 */
	public abstract void setDefaultCompressionLevel(int level);
	
	/**
	 * Begins writing a new ZIP file entry and positions the stream to the start of the entry data. Closes the current entry 
	 * if still active. All entry elements bar name are optional, but must be correct if present. The default compression 
	 * method will be used if no compression method was specified for the entry, and the current time will be used if the 
	 * entry has no set modification time. <p>
	 * 
	 * If CRC, Size and CompressedSize properties are set for the input zipEntry, these values will be ignored.
	 * 
	 * @param zipEntry  the ZIP entry to be written
	 * @throws SpcfArgumentNullException if zipEntry is null 
	 * @throws SpcfZipException if a ZIP format error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred
	 */
	public abstract void putNextEntry(SpcfZipEntry zipEntry);
	
	/**
	 * Closes the current ZIP entry and positions the stream for writing the next entry.
	 * @throws SpcfZipException if a ZIP format error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred
	 */
	public abstract void closeEntry();
	
	/**
	 * Writes an array of bytes to the current ZIP entry data. This method will block until all the bytes are written. 
	 * @param buf the data to be written
	 * @param offset the start offset in the data
	 * @param length the number of bytes that are written
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfIndexOutOfBoundsException if off or len values are invalid
	 * @throws SpcfZipException if a ZIP format error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred
	 */
	public abstract void write(byte[] buf, int offset, int length);
		
	/**
	 * Finishes writing the contents of the ZIP output stream without closing the underlying stream. This will write 
	 * the central directory at the end of the zip file and flush the stream. This is automatically called when the 
	 * stream is closed.
	 * @throws SpcfZipException if a ZIP format error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred	 
	 */
	public abstract void finish();
	
	/**
	 * Closes the ZIP output stream as well as the underlying stream. 
	 * @throws SpcfZipException if a ZIP format error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred	 
	 */
	public abstract void close();	
}
