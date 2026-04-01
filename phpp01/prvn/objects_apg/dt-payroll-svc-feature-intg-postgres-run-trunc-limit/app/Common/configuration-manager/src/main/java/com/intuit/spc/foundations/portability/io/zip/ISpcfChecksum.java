package com.intuit.spc.foundations.portability.io.zip;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;

/**
 * An interface representing a data checksum. A data checksum can be updated by one byte or with a byte array.<p>
 * 
 * After each update the value of the current checksum can be returned by calling getValue(). The complete 
 * checksum object can also be reset so it can be used again with new data.
 */
public interface ISpcfChecksum 
{
	/**
	 * Returns the data checksum computed so far.
	 * @return the current checksum value
	 */
	 long getValue();
	 
	 /**
	  * Resets the data checksum as if no update was ever called.
	  *
	  */
	 void reset();
	 
	 /**
	  * Updates the data checksum with the bytes taken from the array.
	  * @param buffer the byte array to update the checksum with	
	  * @throws SpcfArgumentNullException if buffer is null
	  */
	 void update(byte[] buffer);
	 
	 /**
	  * Adds the byte array to the data checksum.
	  * @param buffer the byte array to update the checksum with
	  * @param off the start offset of the data
	  * @param len the number of bytes to use for the update
	  * @throws SpcfArgumentNullException if buffer is null
	  * @throws SpcfIndexOutOfBoundsException if off or len are incorrect
	  */
	 void update(byte[] buffer, int off, int len);
	 
	 /**
	  * Updates the current checksum with the specified byte. 
	  * @param bval the data value to add. The high byte of the int is ignored.
	  */
	 void update(int bval) ;
}
