package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;

/**
 * SpcfBitConverter is a generic class that exposes methods for converting
 * bits/bytes to higher data types such as characters, integers, floats, etc.
 * A class that needs to do the conversion, such as SpcfStream, would call into
 * the class factory to get the appropriate bit converter for the selected
 * byte ordering.
 */
public abstract strictfp class SpcfBitConverter
{
	/**
	 * Big Endian Bit Converter
	 */
	public static final SpcfBitConverter BigEndianBitConverter = 
		SpcfFactory.getInstance().createBitConverter(SpcfByteOrderEnum.BigEndian);

	
	/**
	 * Little Endian Bit Converter
	 */
	public static final SpcfBitConverter LittleEndianBitConverter = 
		SpcfFactory.getInstance().createBitConverter(SpcfByteOrderEnum.LittleEndian);

	/**
	 * Static factory create method wrapper
	 * @return An SpcfBitConverter
	 */
	public static SpcfBitConverter createInstance(SpcfByteOrderEnum byteOrder)
	{
		return SpcfFactory.getInstance().createBitConverter(byteOrder);
	}
	
	/**
	 * Retrieves the byte order that is used in this instance of the bit converter.
	 * @return the byte order: big endian or little endian 
	 */
	public abstract SpcfByteOrderEnum getByteOrder();
	
	
	/**
	 * Converts the next byte in buffer to a boolean
	 * @param buffer the byte array to read from
	 */
	public abstract boolean toBoolean(byte[] buffer);
	
	
	/**
	 * Converts the next two bytes in the buffer to a unicode character.
	 * @param buffer the byte array to read from
	 */
	public abstract char toChar(byte[] buffer);
	
	
	/**
	 * Converts the next eight bytes in the buffer to a double.
	 * @param buffer the byte array to read from
	 */
	public abstract double toDouble(byte[] buffer);
	
	
	/**
	 * Converts the next four bytes in the buffer to a float.
	 * @param buffer the byte array to read from
	 */
	public abstract float toFloat(byte[] buffer);
	
	
	/**
	 * Converts the next four bytes in the buffer to an integer.
	 * @param buffer the byte array to read from
	 */
	public abstract int toInt(byte[] buffer);
	
	
	/**
	 * Converts the next eight bytes in the buffer to a long.
	 * @param buffer the byte array to read from
	 */
	public abstract long toLong(byte[] buffer);
	
	
	/**
	 * Converts the next two bytes in the buffer to a short.
	 * @param buffer the byte array to read from
	 */
	public abstract short toShort(byte[] buffer);
	
	
	/**
	 * Converts the boolean to an array of one byte.
	 * @param b boolean to convert
	 */
	public abstract byte[] fromBoolean(boolean b);

	
	/**
	 * Converts the unicode character to an array of two bytes.
	 * @param c character to convert
	 */
	public abstract byte[] fromChar(char c);
	
	
	/**
	 * Converts the double to an array of eight bytes.
	 * @param d double to convert
	 */
	public abstract byte[] fromDouble(double d);
	
	
	/**
	 * Converts the float to an array of four bytes.
	 * @param f float to convert
	 */
	public abstract byte[] fromFloat(float f);
	
	
	/**
	 * Converts the integer to an array of four bytes.
	 * @param i int to convert
	 */
	public abstract byte[] fromInt(int i);
	
	
	/**
	 * Converts the long to an array of eight bytes.
	 * @param l long to convert
	 */
	public abstract byte[] fromLong(long l);
	

	/**
	 * Converts the short to an array of two bytes.
	 * @param s short to convert
	 */
	public abstract byte[] fromShort(short s);
}
