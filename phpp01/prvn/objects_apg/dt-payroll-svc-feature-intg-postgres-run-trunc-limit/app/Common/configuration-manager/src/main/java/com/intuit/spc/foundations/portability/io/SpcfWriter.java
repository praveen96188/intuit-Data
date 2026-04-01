package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.text.*;

/**
 * SpcfWriter provides methods for writing characters and strings
 * in a specific text encoding. No other primitive data types are
 * supported by the SpcfWriter abstract class.
 */
public abstract class SpcfWriter
{
	/**
	 * Creates a new SpcfWriter object that wraps a binary stream, encoding text in the selected format.
	 * @param binaryStream binary stream that this text writer
	 * @param encoding text encoding to use
	 * @throws SpcfArgumentNullException if either binaryStream or encoding is null
	 * @throws SpcfIllegalArgumentException if binaryStream is not writable
	 * @throws SpcfSecurityException if a security exception is encountered while trying to open the file
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 */
	public static SpcfWriter createInstance(SpcfStream binaryStream, SpcfEncoding encoding)
	{
		return SpcfFactory.getInstance().createWriter(binaryStream, encoding);
	}

	
	/**
	 * Creates a new SpcfWriter object that wraps a binary stream, encoding text in UTF_8.
	 * @param binaryStream binary stream that this text writer
	 * @throws SpcfArgumentNullException if binaryStream is null
	 * @throws SpcfIllegalArgumentException if binaryStream is not writable
	 * @throws SpcfSecurityException if a security exception is encountered while trying to open the file
	 */
	public static SpcfWriter createInstance(SpcfStream binaryStream)
	{
		return SpcfFactory.getInstance().createWriter(binaryStream, SpcfEncoding.Utf8);
	}
	
	
	/**
	 * Closes any underlying streams or resources held by the writer and releases them.
	 */
	public abstract void close();

	
	/**
	 * Flushes any internal content buffers.
	 * @throws SpcfIOException if an I/O error occurs.
	 */
	public abstract void flush();

	
	/**
	 * Gets the text encoding used for writing characters. Not all writers
	 * need a particular encoding. (For example if the underlying store is 
	 * a string buffer, not a byte stream, then no encoding needs to be done.) 
	 * @return the text encoding
	 */
	public abstract SpcfEncoding getTextEncoding();

	
	/**
	 * Writes a character.
	 * @param c character
	 * @throws SpcfIOException if an I/O exception occurs.
	 */
	public abstract void write(char c);

	
	/**
	 * Writes an array of characters.
	 * @param buffer character array
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if buffer's length is 0  
	 * @throws SpcfIOException if an I/O exception occurs.
	 */
	public void write(char[] buffer)
	{
		write(buffer, 0, (buffer != null ? buffer.length : 0));		
	}

	
	/**
	 * Writes a subset of the specified character array.
	 * @param buffer character array
	 * @param offset position in the character array to begin copying from
	 * @param count number of characters to write
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if offset is less than 0 or count is less than 0  
	 * @throws SpcfIndexOutOfBoundsException if offset + count is greater than the buffer's length 
	 * @throws SpcfIOException if an I/O exception occurs.
	 */
	public abstract void write(char[] buffer, int offset, int count);
	
	
	/**
	 * Writes a string. 
	 * @param s string
	 * @throws SpcfIOException if an I/O exception occurs.
	 * @throws SpcfArgumentNullException if s is null.
	 */
	public abstract void write(String s);


	/**
	 * Writes a carriage return and line feed.
	 * @throws SpcfIOException if an I/O exception occurs.
	 */
	public void writeLine()
	{
		write(SpcfSystem.getNewLine());
	}
	
	
	/**
	 * Writes a string followed by a carriage return and line feed.
	 * @param s string
	 * @throws SpcfIOException if an I/O exception occurs.
	 * @throws SpcfArgumentNullException if s is null.
	 */
	public void writeLine(String s)
	{
		write(s);
		write(SpcfSystem.getNewLine());
	}
}
