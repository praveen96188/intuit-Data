package com.intuit.spc.foundations.portability.io.zip;


/**
 * The kind of compression used for an entry in an archive.
 */
public enum SpcfCompressionMethod 
{
	/**
	 * A direct copy of the file contents is held in the archive.
	 */
	Stored,
	
	/**
	 * Common Zip compression method using a sliding dictionary of up to 32KB and secondary compression 
	 * from Huffman/Shannon-Fano trees.
	 */
	Deflated,
	
	/**
	 * Unknown zip compression.
	 */
	Unknown
}
