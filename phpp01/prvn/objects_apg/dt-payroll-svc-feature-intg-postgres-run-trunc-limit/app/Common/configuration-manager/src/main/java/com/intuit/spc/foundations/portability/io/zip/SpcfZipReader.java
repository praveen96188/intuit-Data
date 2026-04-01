package com.intuit.spc.foundations.portability.io.zip;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.io.*;


/**
 * This class reads the files from a zip archive one after another.  It has a special method to get the zip entry of
 * the next file.  The zip entry contains information about the file name size, compressed size, Crc, etc. It includes 
 * support for Stored and Deflated entries. <p>
 * 
 * Following code sample shows how to read a zip file:<p>
 * 
 * <pre>
 * public static void readZipStream(String zipInputFilePathAndName, string outDir)
 * {
 *	SpcfFactory factory = SpcfFactory.getInstance();
 *	SpcfBinaryFileReader fin = factory.createBinaryFileReader(zipInputFilePathAndName);
 *	SpcfZipReader zin = SpcfZipReader.create(fin);
 *	//
 *	try
 *	{	
 *		SpcfZipEntry theEntry;
 *		while ((theEntry = zin.getNextEntry()) != null) 
 *		{
 *			SpcfBinaryFileWriter fout = factory.createBinaryFileWriter(outDir + theEntry.getName());
 *			int size = 2048;
 *			byte[] data = new byte[2048];
 *			while (true) 
 *			{
 *				size = zin.read(data, 0, data.length);
 *				if (size > 0) 
 *				{	
 *					fout.write(data, 0, size);
 *				}
 *				else
 *				{
 *					break;
 *				}
 *			}
 *			fout.close();
 *		}
 *	}
 *	finally
 *	{
 *		zin.close();
 *	}
 * }
 * </pre>
 */
abstract public class SpcfZipReader extends SpcfStream
{
	/**
	 * Creates a new ZIP reader. 
	 * @param zipIn binary file reader.
	 * @return zip file reader.
	 * @throws SpcfArgumentNullException if zipIn is null
	 */
	public static SpcfZipReader createInstance(SpcfStream zipIn)
	{		
		return SpcfFactory.getInstance().createZipReader(zipIn);
	}
	
	/**
	 * Advances to the next entry in the archive. <p>
	 *  
	 * User shall not rely on getCrc(), getSize() and getCompressedSize() methods to get crc, size and compressed size of
	 * the zip entry returned by this method. These methods will always return -1 for compressed zip entries. 
	 * @return The next entry in the archive or null if there are no more entries. If the previous entry is still open
	 * closeEntry() is called.
	 * @throws SpcfZipException if a ZIP file error has occurred
	 * @throws SpcfIOException if an I/O error has occurred
	 */
	public abstract SpcfZipEntry getNextEntry();
	
	/**
	 * Closes the current ZIP entry and positions the stream for reading the next entry. 
	 * @throws SpcfZipException if a ZIP file error has occurred
	 * @throws SpcfIOException if an I/O error has occurred
	 */
	public abstract void closeEntry();
	
	/**
	 * Returns 0 after EOF has reached for the current entry data, otherwise always return 1. 
	 * Programs should not count on this method to return the actual number of bytes that could be read without blocking.
	 * @return 1 before EOF and 0 after EOF has reached for current entry.
	 * @throws SpcfIOException if an I/O error has occurred
	 */
	public abstract int available();
	
	/**
	 * Reads from the current ZIP entry into an array of bytes. Blocks until some input is available. 
	 * @param buf the buffer into which the data is read
	 * @param offset the start offset of the data
	 * @param length The number of bytes to attempt to read.
	 * @return the actual number of bytes read, or -1 if the end of the entry is reached.
	 * @throws SpcfIndexOutOfBoundsException if offset or length values are not correct
	 * @throws SpcfZipException if a ZIP file error has occurred
	 * @throws SpcfIOException if an I/O error has occurred
	 */
	public abstract int read(byte[] buf, int offset, int length);	
	
	/**
	 * Reads a byte from the current zip entry.
	 * @return The byte or -1 if end of stream is reached.
	 * @throws SpcfZipException if a ZIP file error has occurred
	 * @throws SpcfIOException if an I/O error has occurred
	 */
	public int read()
	{
		byte[] b = new byte[1];
		if (read(b, 0, 1) <= 0) {
			return -1;
		}
		return b[0] & 0xff;
	}
	
	/**
	 * Skips specified number of bytes in the current ZIP entry.
	 * @param n the number of bytes to skip
	 * @return the actual number of bytes skipped	
	 * @throws SpcfZipException if a ZIP file error has occurred
	 * @throws SpcfIOException if an I/O error has occurred
	 * @throws SpcfIllegalArgumentException if n is less than 0.
	 */
	public abstract long skip(long n);	
}
