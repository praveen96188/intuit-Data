package com.intuit.spc.foundations.portabilitySpecific.io.zip;

import java.util.zip.CRC32;

import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.io.zip.SpcfCRC32;

/**
 * Implementation class of portable abstract class- SpcfCRC32.
 */
public class SpcfCRC32Impl extends SpcfCRC32
{
	private CRC32 mPlatformSpecific;
	
    /**
     * Default constructor.
     */
	public SpcfCRC32Impl()
	{	
		mPlatformSpecific = new CRC32();
	}
	
    /**
     * @see com.intuit.spc.foundations.portability.io.zip.ISpcfChecksum#getValue()
     */
	public long getValue() 
	{		
		return mPlatformSpecific.getValue();
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.ISpcfChecksum#reset()
     */
	public void reset() 
	{
		mPlatformSpecific.reset();		
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.ISpcfChecksum#update(byte[])
     */
	public void update(byte[] buffer) 
	{
		SpcfParamValidator.checkIsNotNull(buffer, "buffer");
		mPlatformSpecific.update(buffer);		
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.ISpcfChecksum#update(byte[], int, int)
     */
	public void update(byte[] buffer, int off, int len) 
	{
		SpcfParamValidator.checkIsNotNull(buffer, "buffer");
		try
		{
			mPlatformSpecific.update(buffer, off, len);
		}
		catch(ArrayIndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}

    /**
     * @see com.intuit.spc.foundations.portability.io.zip.ISpcfChecksum#update(int)
     */
	public void update(int bval) 
	{
		mPlatformSpecific.update(bval);			
	}
}
