package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.collections.SpcfArraysUtil;

/**
 * SpcfMemoryStream is a binary stream whose underyling data store is an array of bytes.
 */
public class SpcfMemoryStream extends SpcfStream 
{
	/**
	 * Default buffer size if not elsewhere specified.
	 */
	private static final int DefaultBufferSize = 32;
	
	
	/**
	 * Maximum size of the internal buffer.
	 * (Equivalent to C#'s Int32.MaxValue or Java's Integer.Max_Value)
	 */
	private static final int MaximumBufferSize = 2147483647;
	
	
	/**
	 * The byte array used internally.
	 */
	protected byte[] mBuffer = null;
	
	
	/**
	 * Our current position within the buffer.
	 */
	protected int mCurrentPosition = 0; 
	
	
	/**
	 * True length of the contents.
	 */
	protected int mCount = 0; 
	
	
	/**
	 * Is the memory stream open for reading/writing?
	 */
	protected boolean mIsOpen = false;

	
	/**
	 * Constructs a new memory stream that will create an its own empty internal buffer.
	 */
	public SpcfMemoryStream()
	{
		// Default this to an empty buffer of default size:
		this(DefaultBufferSize);
	}
	
	
	/**
	 * Constructs a new memory stream that will create its own empty internal buffer.
	 */
	public SpcfMemoryStream(int size)
	{
		// Call the super:
		super();
		
		// Make sure that the size is non-zero: 
		int actualsize = (size > 0 ? size : DefaultBufferSize);
		
		// Build the internal structure:
		mBuffer = new byte[actualsize];
		mCount = 0;
		mIsOpen = true;
		mCurrentPosition = 0;
	}
	
	
	/**
	 * Creates a memory stream to wrap the buffer. The contents of the buffer will
	 * be copied. The buffer passed in is assumed to be right-sized to its contents.
	 * @param buffer
	 * @throws SpcfArgumentNullException if buffer is null
	 */
	public SpcfMemoryStream(byte[] buffer)
	{
		// Set the internal buffer to what is passed in. We will hand off
		// to another constructor that will throw SpcfArgumentNullException 
		// for us if buffer is null. Even so, we still have to check if buffer 
		// is null so that buffer.length doesn't throw an exception.
		this(buffer, 0, (buffer != null ? buffer.length : 0));

		// Assume the passed-in buffer is right-sized:
		mCount = (buffer != null ? buffer.length : 0);
	}

	
	/**
	 * Creates a memory stream to wrap a subset of the passed in buffer.
	 * @param buffer The buffer to wrap.
	 * @param offset The buffer offset to use.
	 * @param count The index at which to start copying.
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if offset is less than 0 or count is less than 0  
	 * @throws SpcfIndexOutOfBoundsException if offset + count is greater than the buffer's length 
	 */
	public SpcfMemoryStream(byte[] buffer, int offset, int count)
	{
		// Initialize the internal array to the count passed in:
		this(count);

		// Check parameters:
		SpcfParamValidator.checkArrayParams(buffer, offset, count);

		// Copy the contents of the passed-in buffer into our internal buffer:
		SpcfArraysUtil.copy(buffer, offset, mBuffer, 0, count);
		
		// Set the size of the contents:
		mCount = count;
	}

	
	/**
	 * Creates a new SpcfMemoryStream object that will create its own internal buffer.
	 */
	public static SpcfMemoryStream createInstance()
	{
		return new SpcfMemoryStream();
	}

	
	/**
	 * Constructs a new memory stream that will create its own empty internal buffer.
	 * @param size The initial size of the stream to create.
	 */
	public static SpcfMemoryStream createInstance(int size)
	{
		return new SpcfMemoryStream(size);
	}

	
	/**
	 * Creates a new SpcfMemoryStream object that will be initialized to the buffer passed in.
	 * @param buffer initial byte array to use as contents of memory stream
	 * @throws SpcfArgumentNullException if buffer is null
	 */
	public static SpcfMemoryStream createInstance(byte[] buffer)
	{
		return new SpcfMemoryStream(buffer);
	}
	

	/**
	 * Creates a memory stream to wrap a subset of the passed in buffer.
	 * @param buffer The buffer to wrap.
	 * @param offset The buffer offset to use.
	 * @param count The index at which to start copying.
	 * @throws SpcfArgumentNullException if buffer is null
	 * @throws SpcfArgumentOutOfRangeException if offset is less than 0 or count is less than or equal to 0  
	 * @throws SpcfIndexOutOfBoundsException if offset + count is greater than the buffer's length 
	 */
	public static SpcfMemoryStream createInstance(byte[] buffer, int offset, int count)
	{
		return new SpcfMemoryStream(buffer, offset, count);
	}

	
	/**
	 * Gets the byte array currently being used by the memory stream. The internal 
	 * byte array will dynamically grow or shrink as necessary, so repeated calls
	 * to getBuffer() are not guaranteed to return the same array. Also, the size of
	 * the byte array may be much larger than the actual length of the stream, so use
	 * getLength() to determine the actual endpoint of the data. If you prefer a
	 * trimmed array, use toArray().
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#getLength()
	 * @see com.intuit.spc.foundations.portability.io.SpcfMemoryStream#toArray()
	 * @return the byte array currently used by the memory stream 
	 */
	public byte[] getBuffer()
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();
		
		return mBuffer;		
	}
	
	
	/**
	 * Returns a trimmed copy of the byte array used internally by the memory stream.
	 * To get the actual byte array currently being used by the memory stream, use
	 * getBuffer().
	 * @return byte array sized to the exact contents of the memory stream
	 * @see com.intuit.spc.foundations.portability.io.SpcfMemoryStream#getBuffer()
	 */
	public byte[] toArray()
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		// Create a right-sized array:
		byte[] temp = new byte[mCount];

		// Copy the contents of the our buffer into the return array:
		SpcfArraysUtil.copy(mBuffer, 0, temp, 0, mCount);

		// Return the array:
		return temp;
	}
	
	
	/**
	 * Writes the contents of this memory stream to the selected binary stream.
	 * Make sure that the memory stream and the target stream share the same 
	 * byte ordering or the results may appear scrambled.
	 * @param binaryStream binary stream that will receive the contents of the memory stream 
	 * @throws SpcfIOException if the memory stream is not open or if an I/O exception occurs while attempting to write to the binary stream
	 * @throws SpcfArgumentNullException if binaryStream is null
	 * @throws SpcfInvalidOperationException if writing is not permitted on the binary stream
	 */
	public void writeTo(SpcfStream binaryStream)
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		// Make sure the binary stream is not null:
		SpcfParamValidator.checkIsNotNull(binaryStream, "Binary Stream");
		
		// Write the contents of our buffer to the binary stream
		binaryStream.write(mBuffer, 0, mCount);
	}
	
	
	/**
	 * This will prevent further reading/writing of the memory stream and 
	 * release the pointer to the internal byte array so that it can be garbage
	 * collected.
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#close()
	 */
	@Override
	public void close()
	{
		mBuffer = null;
		mIsOpen = false;
		mCurrentPosition = 0;
		mCount = 0;
	}
	

	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#flush()
	 */
	@Override
	public void flush()
	{
		// Flushing doesn't do anything, but if we are disposed, throw an IO exception.
		if (!mIsOpen) throw new SpcfIOException();
	}
	

	/**
	 * By default, if open, memory streams are readable.
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#canRead()
	 */
	@Override
	public boolean canRead()
	{
		return mIsOpen;
	}
	
	
	/**
	 * By default, if open, memory streams are seekable.
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#canSeek()
	 */
	@Override
	public boolean canSeek()
	{
		return mIsOpen;
	}
	
	
	/**
	 * By default, if open, memory streams are writable.
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#canWrite()
	 */
	@Override
	public boolean canWrite()
	{
		return mIsOpen;
	}	

	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#getLength()
	 */
	@Override
	public long getLength()
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		return mCount;
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#getLength()
	 */
	@Override
	public long getPosition()
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		return mCurrentPosition;
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#peekByte()
	 */
	@Override
	public byte peekByte()
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		if (mCurrentPosition >= mCount || mCurrentPosition < 0) throw new SpcfEofException();
		return mBuffer[mCurrentPosition];
	}

	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] buffer, int offset, int count)
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		// Check params:
		SpcfParamValidator.checkArrayParams(buffer, offset, count);

		// If we weren't asked to read anything, don't:
		if (count == 0) return 0;

		// If we are past the end, then return -1:
		if (mCurrentPosition >= mCount) return -1;
		
		// Retrieve data:
		int remaining = mCount - mCurrentPosition;
		int actual = (count < remaining ? count : remaining);
		if (actual <= 0) return 0;
		for (int n = 0; n < actual; n++)
		{
			buffer[offset + n] = mBuffer[mCurrentPosition + n];
		}
		mCurrentPosition += actual;
		return actual;
	}

	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#readByte()
	 */
	@Override
	public byte readByte()
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		// Make sure that we are not reading past the EOF:
		if (mCurrentPosition >= mCount) throw new SpcfEofException();
		
		// Read the next byte:
		byte b = mBuffer[mCurrentPosition];
		mCurrentPosition++;
		return b;
	}

		
	/**
	 * Resizes the internal buffer to make room for the new bytes.
	 * @throws SpcfIOException if the new size of the internal buffer will exceed the maximum allowed 
	 */
	protected void resize(int additional)
	{
		// The size of our buffer must be at least the length of the contents (mCount) plus the additional space: 
		long requiredNewSize = (long)mCount + (long)additional;
		
		// The current buffer length as a long:
		long currentBufferLength = (long)mBuffer.length;
		
		// We only need to resize the buffer if the required new size is greater than the current buffer length:
		if (requiredNewSize > currentBufferLength)
		{
			// The new size will be the minimum of (a) twice the current buffer length or (b) the required size:
			long newSize = currentBufferLength * 2L;
			if (newSize < requiredNewSize) newSize = requiredNewSize;
			
			// If the new size exceeds the maximum buffer size allowed, throw an IO exception:
			if (newSize >= (long)MaximumBufferSize) throw new SpcfIOException("Memory stream exceeds maximum size allowed.");
			
			// Otherwise, resize the buffer using SpcfArraysUtil.
			mBuffer = SpcfArraysUtil.resize(mBuffer, (int)newSize);
		}
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#seek(long, SpcfSeekOriginEnum)
	 */
	@Override
	public long seek(long position, SpcfSeekOriginEnum origin)
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		long newPosition = 0;
		if (origin == SpcfSeekOriginEnum.FromBeginning)
		{
			newPosition = position;
		}
		else if (origin == SpcfSeekOriginEnum.FromCurrentPosition)
		{
			newPosition = mCurrentPosition + position;
		}
		else if (origin == SpcfSeekOriginEnum.FromEnd)
		{
			newPosition = mCount + position;
		}
		
		// If the new position exceeds the maximum possible size of the data, then throw an exception:
		if (newPosition > MaximumBufferSize) throw new SpcfIOException("Cannot seek past the MaximumBufferSize of the memory stream buffer.");
		if (newPosition < 0) throw new SpcfIOException("Cannot seek past the beginning of an input stream.");
		
		// At this point, we know newPosition is within the realm of an integer:
		mCurrentPosition = (int)newPosition;
		
		// Return the new position:
		return mCurrentPosition;
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#write(byte)
	 */
	@Override
	public void write(byte b)
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();
		
		// Make sure there is enough room for the new data:
		resize(mCurrentPosition - mCount + 1);
		
		// Write the byte:
		mBuffer[mCurrentPosition] = b;
		mCurrentPosition++;
		if (mCurrentPosition > mCount) mCount = mCurrentPosition;
	}

	
	/**
	 * @see com.intuit.spc.foundations.portability.io.SpcfStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] buffer, int offset, int count)
	{
		// Make sure the memory stream is open:
		if (!mIsOpen) throw new SpcfIOException();

		// Check parameters:
		SpcfParamValidator.checkArrayParams(buffer, offset, count);

		// Make sure there is enough room for the new data:
		resize(mCurrentPosition - mCount + count - offset);
		
		// Write the bytes:
		for (int n = 0; n < count; n++)
		{
			mBuffer[mCurrentPosition + n] = buffer[offset + n];
		}
		mCurrentPosition += count;
		if (mCurrentPosition > mCount) mCount = mCurrentPosition;
	}
}
