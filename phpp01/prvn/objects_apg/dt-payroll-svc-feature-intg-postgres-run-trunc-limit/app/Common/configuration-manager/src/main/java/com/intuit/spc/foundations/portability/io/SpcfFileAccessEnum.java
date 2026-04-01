package com.intuit.spc.foundations.portability.io;

/**
 * Enumeration of how a file should be accessed.
 */
public enum SpcfFileAccessEnum 
{
	/**
	 * Append to an existing file. 
	 * Automatically create the file if it does not exist.
	 */
    Append,
    
    /**
     * Open a file for random access (reading, writing, and seeking).
     * Automatically create the file if it does not exist.
     */
    RandomAccess,
    
    /**
     * Open an existing file for reading from the beginning. 
     * If the file does not exist, throw an SpcfFileNotFoundException.
     */
    Read,
    
    /**
     * Open an existing file for writing. 
	 * Automatically create the file if it does not exist.
	 * Automatically truncate the file if it already exists.
     */
    Write
}
