package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.collections.*;

/**
 * SpcfStream describes the interface for binary streams.<br/><br/>
 * 
 * It provides methods for reading and writing primitive data types.<br/><br/>
 * 
 * Bytes are always read/written in big endian format. (However, encoded wrappers
 * may force a different byte ordering.)<br/><br/>
 * 
 * If you want to read/write strings in a specific text encoding, use 
 * SpcfReader and SpcfWriter instead.<br/><br/>
 * 
 * @see com.intuit.spc.foundations.portability.io.SpcfReader 
 * @see com.intuit.spc.foundations.portability.io.SpcfWriter 
 */
public abstract strictfp class SpcfStream 
{
	/**
	 * By default, we will use a big endian bit converter.
	 */
	protected SpcfBitConverter mBitConverter = SpcfBitConverter.BigEndianBitConverter;
	
	
	/**
	 * Can data be read from the stream?
	 * @return true if data can be read from the stream, false otherwise
	 */
	public abstract boolean canRead();
	

	/**
	 * Can data be written to the stream?
	 * @return true if data can be written to the stream, false otherwise
	 */
	public abstract boolean canWrite();
	
	
	/**
	 * Can the current position be changed in the stream? Can we perform 
	 * random-access within the stream?
	 * @return true if seeking is permitted within the stream, false otherwise
	 */
	public abstract boolean canSeek();
	
	
	/**
	 * Close the stream and releases any system resources held by the stream. 
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract void close();

		
	/**
	 * Flush any internal content buffers.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract void flush();

	
	/**
	 * Retrieves the byte order for the binary stream.
	 * @return SpcfByteOrderEnum - either big endian or little endian
	 */
	public SpcfByteOrderEnum getByteOrder()
	{
		return mBitConverter.getByteOrder();
	}

	
	/**
	 * Sets the byte order to use on this stream. 
	 * By default, the byte ordering is big endian.
	 * @param byteOrder
	 */
	public void setByteOrder(SpcfByteOrderEnum byteOrder)
	{
		if (byteOrder == SpcfByteOrderEnum.LittleEndian)
		{
			mBitConverter = SpcfBitConverter.LittleEndianBitConverter;
		}
		else
		{
			mBitConverter = SpcfBitConverter.BigEndianBitConverter;
		}
	}
	
	
	/**
	 * Gets the length in bytes of the stream.
	 * @return total length of the stream; can be -1 if the stream doesn't end; default is 0
	 * @throws SpcfIOException if an IO exception occurs while trying to get the length
	 */
	public abstract long getLength();
	
	
	/**
	 * Gets the current position in the stream. Units are in bytes. 
	 * @return position from the beginning of the stream; can be -1 if the stream doesn't really begin; default is 0
	 * @throws SpcfIOException if an IO exception occurs while trying to get the current position
	 */
	public abstract long getPosition();
	
	
	/**
	 * Reads the next byte from the stream and then immediately rewinds the stream one byte. 
	 * Requires that the stream be seekable and readable.
	 * @return the next byte from the input stream
	 * @throws SpcfInvalidOperationException if seeking and reading are not permitted on the current stream
	 * @throws SpcfEofException if attempting to peek beyond the end of the stream.
	 * @throws SpcfIOException if an IO exception occurs. 
	 */
	public abstract byte peekByte();
	
	
	/**
	 * Reads an array of bytes from the stream. 
	 * Requires that the stream be readable.
	 * @param buffer the byte array in which to place the bytes read
	 * @param offset the position in the array in which to begin copying read bytes
	 * @param count the number of bytes to copy into the array
	 * @return the number of bytes that were read, -1 if none were read because we are past the end of the stream.
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if offset is less than 0 or count is less than 0  
	 * @throws SpcfIndexOutOfBoundsException if offset + count is greater than the buffer's length 
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract int read(byte[] buffer, int offset, int count);

	
	/**
	 * Reads the next byte from the stream and returns whether it is 
	 * zero (false) or nonzero (true). 
	 * Requires that the stream be readable.
	 * @return boolean 
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public boolean readBoolean()
	{
		byte[] buffer = readBytes(1);
		if (buffer.length < 1) throw new SpcfEofException();
		return mBitConverter.toBoolean(buffer);
	}

	
	/**
	 * Reads the next byte from the stream. 
	 * Requires that the stream be readable.
	 * @return byte
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract byte readByte();
	
	
	/**
	 * Reads an array of bytes from the stream. 
	 * Requires that the stream be readable.
	 * @param count the number of bytes to read.
	 * @return byte array, returns an empty array if EOF has been reached
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfArgumentOutOfRangeException if count is less than or equal to 0
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public byte[] readBytes(int count)
	{
		// Check params:
		SpcfParamValidator.checkIsPositive(count, "Count");

		// Allocate a buffer of count size:
		byte[] buffer = new byte[count];
		
		// Read the bytes allowing exceptions to be thrown:
		int numberOfBytesRead = this.read(buffer, 0, count);

		// If nothing could be read, return an empty byte array:
		if (numberOfBytesRead <= 0) return new byte[0];

		// If the number of bytes read is the same as what was 
		// requested, return the buffer as is. (The check here is >= 
		// but in reality, number of bytes read should never exceed 
		// count.)
		if (numberOfBytesRead >= count) return buffer;
		
		// If we are here, the number of bytes read is less than count,
		// so resize the array and return that:
		return SpcfArraysUtil.resize(buffer, numberOfBytesRead);		
	}


	/**
	 * Reads the next two bytes from the stream and converts them to a unicode character.
	 * Requires that the stream be readable.
	 * @return unicode character
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public char readChar()
	{
		byte[] buffer = readBytes(2);
		if (buffer.length < 2) throw new SpcfEofException();
		return mBitConverter.toChar(buffer);
	}
	
	
	/**
	 * Reads the next eight bytes from the stream and converts them to a double.  
	 * Requires that the stream be readable.
	 * @return double
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public double readDouble()
	{
		byte[] buffer = readBytes(8);
		if (buffer.length < 8) throw new SpcfEofException();
		return mBitConverter.toDouble(buffer);
	}
	
	
	/**
	 * Reads the next four bytes from the stream and converts them to a float.
	 * @return float
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public float readFloat()
	{
		byte[] buffer = readBytes(4);
		if (buffer.length < 4) throw new SpcfEofException();
		return mBitConverter.toFloat(buffer);
	}
	
	
	/**
	 * Reads the next four bytes from the stream and converts them to an integer.  
	 * Requires that the stream be readable.
	 * @return integer
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public int readInt()
	{
		byte[] buffer = readBytes(4);
		if (buffer.length < 4) throw new SpcfEofException();
		return mBitConverter.toInt(buffer);
	}
	
	
	/**
	 * Reads the next eight bytes from the stream and converts them to a double.  
	 * Requires that the stream be readable.
	 * @return double
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public long readLong()
	{
		byte[] buffer = readBytes(8);
		if (buffer.length < 8) throw new SpcfEofException();
		return mBitConverter.toLong(buffer);
	}
	

	/**
	 * Reads the next two bytes from the stream and converts them to a short.  
	 * Requires that the stream be readable.
	 * @return short
	 * @throws SpcfInvalidOperationException if reading is not permitted on the current stream
	 * @throws SpcfEofException if attempting to read beyond the end of the stream.
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public short readShort()
	{
		byte[] buffer = readBytes(2);
		if (buffer.length < 2) throw new SpcfEofException();
		return mBitConverter.toShort(buffer);
	}
	

	/**
	 * Changes the current position in the stream to the specified position relative 
	 * to the specified origin.
	 * @param position location in bytes
	 * @param origin whether to move relative to the current position or from the beginning or the end of the stream 
	 * @return new position within the stream
	 * @throws SpcfInvalidOperationException if seeking is not allowed for the stream
	 * @throws SpcfArgumentOutOfRangeException if the resulting absolute position is negative
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract long seek(long position, SpcfSeekOriginEnum origin);

	
	/**
	 * Writes a boolean to the stream. If the boolean value to be written is true, 
	 * this will write (byte)1; if false, this will write (byte)0.
	 * Requires that the stream be writable.
	 * @param b boolean to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(boolean b)
	{
		write(mBitConverter.fromBoolean(b), 0, 1);
	}
	

	/**
	 * Writes a byte to the stream.
	 * Requires that the stream be writable.
	 * @param b byte to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract void write(byte b);
	
	
	/**
	 * Writes an array of bytes to the stream.
	 * Requires that the stream be writable.
	 * @param buffer byte array to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if the buffer's length is 0  
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(byte[] buffer)
	{
		write(buffer, 0, (buffer != null ? buffer.length : 0));
	}

	
	/**
	 * Writes a subset of the contents of an array of bytes to the stream.
	 * Requires that the stream be writable.
	 * @param buffer byte array
	 * @param offset the position in the byte array to begin copying from
	 * @param count the number of bytes to write to the stream
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if offset is less than 0 or count is less than or equal to 0  
	 * @throws SpcfIndexOutOfBoundsException if offset + count is greater than the buffer's length 
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract void write(byte[] buffer, int offset, int count);

	
	/**
	 * Writes a unicode character to the stream as a series of two bytes. 
	 * Requires that the stream be writable.
	 * @param c char to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(char c)
	{
		write(mBitConverter.fromChar(c), 0 , 2);
	}
	
	
	/**
	 * Writes an integer to the stream as a series of four bytes. 
	 * Requires that the stream be writable.
	 * @param i int to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(int i)
	{
		write(mBitConverter.fromInt(i), 0, 4);
	}

	
	/**
	 * Writes a double to the stream as a series of eight bytes. 
	 * Requires that the stream be writable.
	 * @param d double to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(double d)
	{
		write(mBitConverter.fromDouble(d), 0, 8);
	}
	
	
	/**
	 * Writes a float to the stream as a series of four bytes.
	 * Requires that the stream be writable.
	 * @param f float to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(float f)
	{
		write(mBitConverter.fromFloat(f), 0, 4);
	}
	
	
	/**
	 * Writes a long integer to the stream as a series of eight bytes. 
	 * Requires that the stream be writable.
	 * @param l long to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(long l)
	{
		write(mBitConverter.fromLong(l), 0, 8);		
	}
	
	
	/**
	 * Writes a short integer to the stream as a series of two bytes. 
	 * Requires that the stream be writable.
	 * @param s short to be written
	 * @throws SpcfInvalidOperationException if writing is not permitted on the current stream
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public void write(short s)
	{
		write(mBitConverter.fromShort(s), 0, 2);
	}
}
