package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.text.*;

/**
 * SpcfStringWriter takes a string builder (SpcfStringBuilder) and wraps a 
 * text writer around it.
 */
public class SpcfStringWriter extends SpcfWriter
{
	/**
	 * Internal string builder that this class reads from.
	 */
	protected SpcfStringBuilder mStringBuilder = null;
	

	/**
	 * Constructs a string writer whose underlying data store is a 
	 * string builder that the writer will create.
	 */
	public SpcfStringWriter()
	{
		super();
		
		// Create a new string builder:
		mStringBuilder = SpcfFactory.getInstance().createStringBuilder(); 
	} 
	 
	/**
	 * Constructs a string writer whose underlying data store is the 
	 * SpcfStringBuilder passed in.
	 * @param stringBuilder
	 * @throws SpcfArgumentNullException if string builder is null
	 */
	public SpcfStringWriter(SpcfStringBuilder stringBuilder)
	{
		super();
		
		// Make sure the string builder is not null:
		SpcfParamValidator.checkIsNotNull(stringBuilder, "String Builder");
		
		// Hang onto the string builder:
		mStringBuilder = stringBuilder; 
	} 
	
	/**
	 * Constructs a string writer whose underlying data store is a 
	 * string builder that the writer will create.
	 */
	public static SpcfStringWriter createInstance()
	{
		return new SpcfStringWriter();
	} 

	/**
	 * Constructs a string writer whose underlying data store is the 
	 * SpcfStringBuilder passed in. passed in.
	 * @param stringBuilder
	 * @throws SpcfArgumentNullException if string builder is null
	 */
	public static SpcfStringWriter createInstance(SpcfStringBuilder stringBuilder)
	{
		return new SpcfStringWriter(stringBuilder);
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
	 * Does nothing.
	 */
	@Override
	public void flush()
	{
	} 
	
	/**
	 * Returns the internal SpcfStringBuilder being read by the string writer.  
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
	 * Writes a character to the current string.
	 * @param c The character to be written
	 */
	@Override
	public void write(char c)
	{
		// If the string builder is disposed, throw an IO exception:
		if (mStringBuilder == null) throw new SpcfIOException();

		// Append the character:
		mStringBuilder.append(c);
	}

	/**
	 * Writes the given buffer to the current string.
	 * @param buffer The buffer to be written
	 * @param offset The offset into the buffer at which to begin reading
	 * @param count The number of characters to be read from the buffer.
	 */	
	@Override
	public void write(char[] buffer, int offset, int count)
	{
		// If the string builder is disposed, throw an IO exception:
		if (mStringBuilder == null) throw new SpcfIOException();

		// Append the buffer:
		mStringBuilder.append(buffer, offset, count);
	}

	/**
	 * Writes the given string to the current string.
	 * @param s The string to be written.
	 */
	@Override
	public void write(String s)
	{
		// If the string builder is disposed, throw an IO exception:
		if (mStringBuilder == null) throw new SpcfIOException();

		// Ensure that the passed in string is not null:
		SpcfParamValidator.checkIsNotNull(s, "s");
		
		// Append the string:
		mStringBuilder.append(s);
	}

	
	/**
	 * Writes the contents of this string writer to the specified text writer.
	 * @param writer to receive the contents of this string writer
	 */
	public void writeTo(SpcfWriter writer)
	{
		// If the string builder is disposed, throw an IO exception:
		if (mStringBuilder == null) throw new SpcfIOException();

		// Ensure that the writer passed in is not null:
		SpcfParamValidator.checkIsNotNull(writer, "writer");
		
		// Write the output of the string builder to the writer.
		writer.write(mStringBuilder.toString());
	}
}
