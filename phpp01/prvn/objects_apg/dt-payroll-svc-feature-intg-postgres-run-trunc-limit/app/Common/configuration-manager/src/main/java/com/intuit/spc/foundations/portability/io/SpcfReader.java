package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.text.*;
import com.intuit.spc.foundations.portability.collections.*;

/**
 * SpcfReader provides methods for reading characters and strings
 * in a specific text encoding. No other primitive data types are
 * supported by the SpcfReader abstract class.
 */
public abstract class SpcfReader
{
	/**
	 * Creates a new SpcfReader object that wraps a binary stream, encoding text in the selected format.
	 * @param binaryStream binary stream that this text writer
	 * @param encoding text encoding to use
	 */
	public static SpcfReader createInstance(SpcfStream binaryStream, SpcfEncoding encoding)
	{
		return SpcfFactory.getInstance().createReader(binaryStream, encoding);
	}
		
	
	/**
	 * Creates a new SpcfReader object that wraps a binary stream, encoding text in UTF_8.
	 * @param binaryStream binary stream that this text writer
	 * @throws SpcfArgumentNullException if binaryStream is null
	 * @throws SpcfIllegalArgumentException if binaryStream is not readable
	 * @throws SpcfSecurityException if a security exception is encountered
	 */
	public static SpcfReader createInstance(SpcfStream binaryStream)
	{
		return SpcfFactory.getInstance().createReader(binaryStream, SpcfEncoding.Utf8);
	}
	
	
	/**
	 * Closes any underlying streams or resources held by the reader and releases them.
	 */
	public abstract void close();

	
	/**
	 * Gets the text encoding used for reading characters. Not all readers
	 * need a particular encoding. (For example if the underlying store is 
	 * a string buffer, not a byte stream, then no encoding needs to be done.) 
	 * @return the text encoding
	 */
	public abstract SpcfEncoding getTextEncoding();

	
	/**
	 * Reads an array of characters into the specified character array.
	 * @param buffer the character array in which to place the characters read
	 * @param offset the position in the array in which to begin copying read characters
	 * @param count the number of characters to copy into the array
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfIndexOutOfBoundsException if offset is less than 0, count is less than 0, or offset + count is greater than buffer's length 
	 * @throws SpcfIOException if an I/O exception occurs
	 * @return the number of characters that were read, -1 if the end of the stream has been reached
	 */
	public abstract int read(char[] buffer, int offset, int count);
	
	
	/**
	 * Reads the next character.
	 * @throws SpcfEofException when attempting to read beyond the end of the underlying data store
	 * @throws SpcfIOException if an I/O exception occurs
	 * @return character
	 */
	public abstract char readChar();

	
	/**
	 * Returns an array of characters. Attempts to read the number of characters specified 
	 * by the count parameter, but the actual number of characters read may be less than that. 
	 * Check the size of the return array.
	 * @param count the number of characters to read
	 * @throws SpcfIndexOutOfBoundsException if count is less than or equal to 0 
	 * @throws SpcfIOException if an I/O exception occurs
	 * @return character array, returns an empty array if EOF has been reached
	 */
	public char[] readChars(int count)
	{
		// Check params:
		SpcfParamValidator.checkIsPositive(count, "Count");

		// Allocate a buffer of count size:
		char[] buffer = new char[count];
		
		// Read the characters allowing exceptions to be thrown:
		int numberOfCharsRead = this.read(buffer, 0, count);

		// If nothing could be read, return an empty character array:
		if (numberOfCharsRead <= 0) return new char[0];

		// If the number of characters read is the same as what was 
		// requested, return the buffer as is. (The check here is >= 
		// but in reality, number of characters read should never exceed 
		// count.)
		if (numberOfCharsRead >= count) return buffer;
		
		// If we are here, the number of bytes read is less than count,
		// so resize the array and return that:
		return SpcfArraysUtil.resize(buffer, numberOfCharsRead);
	}
	
	
	/**
	 * Reads a line. A line ends with a carriage return followed by a line feed 
	 * or just a carriage return or just a line feed, whichever is greater.
	 * If the end of the stream is reached, it will return all the characters it 
	 * finds up to that point. If we are already past the end of the stream, a null
	 * will be returned.
	 * @throws SpcfIOException if an I/O exception occurs
	 * @return a string that does not include the end of line character(s)
	 */
	public abstract String readLine();
}
