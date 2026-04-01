package com.intuit.spc.foundations.portability.io.zip;

import com.intuit.spc.foundations.portability.SpcfFactory;

/**
 * A class that can be used to compute the CRC-32 of a data stream. 
 */
abstract public class SpcfCRC32 implements ISpcfChecksum
{		
	/***
	 * Creates a new object of SpcfCRC32.
	 * @return SpcfCRC32 object
	 */	
	public static SpcfCRC32 createInstance()
	{
		return SpcfFactory.getInstance().createCRC32();
	}
}
