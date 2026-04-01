package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.text.*;
import com.intuit.spc.foundations.portability.*;

/**
 * SpcfStringReader takes a string builder (SpcfStringBuilder) and wraps a 
 * text reader around it.
 */
public class SpcfStringReader extends SpcfReader
{
	/**
	 * Internal string builder that this class reads from.
	 */
	protected SpcfStringBuilder mStringBuilder = null;
	
	
	/**
	 * The current position in the string builder we are reading.
	 */
	protected int mCurrentPosition = 0;
	

	/**
	 * Constructs a string reader that will be initialized to the string passed in.
	 * @param s string to wrap this reader around
	 */
	public SpcfStringReader(String s)
	{
		super();
		
		// Make sure that the input string used to create the reader is not null:
		SpcfParamValidator.checkIsNotNull(s, "Input String");
		
		// Create an SpcfStringBuilder to wrap the input string:
		mStringBuilder = SpcfFactory.getInstance().createStringBuilder(s); 
	} 
	 
	/**
	 * Constructs a string reader whose underlying data store is the SpcfStringBuilder passed in.
	 * @param stringBuilder
	 */
	public SpcfStringReader(SpcfStringBuilder stringBuilder)
	{
		super();
		
		// Make sure that the input string builder used to create the reader is not null:
		SpcfParamValidator.checkIsNotNull(stringBuilder, "Input String Builder");
		
		// Hang onto the SpcfStringBuilder:
		mStringBuilder = stringBuilder; 
	}
	
	
	/**
	 * Constructs a string reader that will be initialized to the string passed in.
	 * @param s string to wrap this reader around
	 */
	public static SpcfStringReader createInstance(String s)
	{
		return new SpcfStringReader(s);
	} 
	
	/**
	 * Constructs a string reader whose underlying data store is the SpcfStringBuilder passed in.
	 * @param stringBuilder
	 */
	public static SpcfStringReader createInstance(SpcfStringBuilder stringBuilder)
	{
		return new SpcfStringReader(stringBuilder);
	}


	/**
	 * Throws away its reference to its internal string builder.
	 */
	@Override
	public void close()
	{
		mStringBuilder = null;
	} 
	 
	/**
	 * Returns the internal SpcfStringBuilder being read by the string reader.  
	 * Replacement for StringBuilder property
	 * @return internal SpcfStringBuilder 
	 */ 
	public SpcfStringBuilder getStringBuilder()
	{
		return mStringBuilder;
	}
	
	
	/**
	 * Always returns SpcfEncoding.UTF_8, which is the default SPCF string encoding.
	 */
	@Override
	public SpcfEncoding getTextEncoding()
	{
		return SpcfEncoding.Utf8;
	}

	
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
	@Override
	public int read(char[] buffer, int offset, int count)
	{
		// If the string builder is disposed, throw an IO exception:
		if (mStringBuilder == null) throw new SpcfIOException();
		
		// Check parameters:
		SpcfParamValidator.checkArrayParams(buffer, offset, count);

		// If we weren't asked to read anything, don't:
		if (count == 0) return 0;

		// Get the length of the string builder:
		int length = mStringBuilder.getLength();
		
		// If we are past the end, then return -1:
		if (mCurrentPosition >= length) return -1;

		// Retrieve data:
		int remaining = length - mCurrentPosition;
		int actual = (count < remaining ? count : remaining);
		//mStringBuilder.getChars(mCurrentPosition, mCurrentPosition + actual, buffer, offset);
		SpcfStringUtil.getChars(mStringBuilder.toString(), mCurrentPosition, mCurrentPosition + actual, buffer, offset);
		mCurrentPosition += actual;
		return actual;
	}

	
	/**
	 * Reads the next character.
	 * @throws SpcfEofException when attempting to read beyond the end of the underlying data store
	 * @throws SpcfIOException if an I/O exception occurs
	 * @return character
	 */
	@Override
	public char readChar()
	{
		// If the string builder is disposed, throw an IO exception:
		if (mStringBuilder == null) throw new SpcfIOException();

		// Read the next character, if we can: 
		int remaining = mStringBuilder.getLength() - mCurrentPosition;
		if (remaining <= 0) throw new SpcfEofException();
		char c = mStringBuilder.charAt(mCurrentPosition);
		mCurrentPosition++;
		return c;
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
	@Override
	public String readLine()
	{
		// If the string builder is disposed, throw an IO exception:
		if (mStringBuilder == null) throw new SpcfIOException();

		// Get the length of the internal string builder so that we don't go past EOF:
		int length = mStringBuilder.getLength();
		
		// If the current position is at or beyond the length of the string, return null:
		if (mCurrentPosition >= length) return null;
		
		// Create a string builder that we will append characters to:
		SpcfStringBuilder stringBuilder = SpcfStringBuilder.createInstance();
		
		// Keep retrieving characters until we hit EOL or EOF:
		boolean foundEOL = false;
		while (!foundEOL && mCurrentPosition < length)
		{
			// Get the next character and advance the position:
			char c = mStringBuilder.charAt(mCurrentPosition);
			mCurrentPosition++;
			
			// If the next character is a carriage return ('\r'), 
			// check if it is followed by a linefeed ('\n'):
			if (c == '\r')
			{
				foundEOL = true;
				if (mCurrentPosition < length)
				{
					// Peek at the next character and only eat it if it's a linefeed:
					char c2 = mStringBuilder.charAt(mCurrentPosition);
					if (c2 == '\n') mCurrentPosition++;
				}
			}
			// Check if the next character is a linefeed: 
			else if (c == '\n')
			{
				foundEOL = true;
			}
			// Otherwise, append it to the return string:
			else
			{
				stringBuilder.append(c);
			}
		}
		
		// Return the string without any end of line characters:
		return stringBuilder.toString();
	}
}
