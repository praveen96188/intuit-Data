package com.intuit.spc.foundations.portability.io;

/**
 * An enumeration describing where to initiate a seek operation.
 */
public enum SpcfSeekOriginEnum
{
	/**
	 * Seek from the beginning of the file/stream.
	 */
	FromBeginning,
	
	/**
	 * Seek from the current position in the file/stream.
	 */
	FromCurrentPosition,
	
	/**
	 * Seek from the end of the file/stream.
	 */
	FromEnd
}
