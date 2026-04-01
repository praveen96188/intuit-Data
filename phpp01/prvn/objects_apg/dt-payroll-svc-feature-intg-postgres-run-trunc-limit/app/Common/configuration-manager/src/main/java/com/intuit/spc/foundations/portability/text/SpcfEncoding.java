package com.intuit.spc.foundations.portability.text;

/**
* The SpcfEncoding class defines the supported character encoding types
* 
*/
public class SpcfEncoding
{
	private String mEncoding;
    private SpcfEncoding(String encoding)
    {
    	mEncoding = encoding;
    }
    
	/**
     * 8-bit UTF encoding
     */
    public static final SpcfEncoding Utf8 = new SpcfEncoding("Utf8"); 

    
    /**
     * 16-bit UTF encoding, Big-Endian byte order, Unmarked
     */
    public static final SpcfEncoding Utf16BigEndianUnmarked = new SpcfEncoding("Utf16BigEndianUnmarked");
    
    /**
     * 16-bit UTF encoding, Little-Endian byte order, Unmarked
     */
    public static final SpcfEncoding Utf16LittleEndianUnmarked = new SpcfEncoding("Utf16LittleEndianUnmarked");
    
 
    /**
     * 16-bit UTF encoding, Big-Endian byte order, with Byte Order Mark
     */
    public static final SpcfEncoding Utf16BigEndianByteOrderMarked = new SpcfEncoding("Utf16BigEndianByteOrderMarked");
    
    /**
     * 16-bit UTF encoding, Little-Endian byte order, with Byte Order Mark
     */
    public static final SpcfEncoding Utf16LittleEndianByteOrderMarked = new SpcfEncoding("Utf16LittleEndianByteOrderMarked");
      
    
    /**
     * American Standard Code for Information Interchange encoding
     */
    public static final SpcfEncoding UsAscii = new SpcfEncoding("UsAscii");
    
 
    /**
     * Latin Alphabet Number 1 encoding, ISO-8859-1, Western European
     */ 
    public static final SpcfEncoding Latin1 = new SpcfEncoding("Latin1");

    /**
     * Windows Latin-1 encoding, codepage 1252, this is currently the default for windows machines
     */
    public static final SpcfEncoding WindowsLatin1 = new SpcfEncoding("WindowsLatin1");

    /**
     * Returns the encoding as a string.
     * @return The associated encoding.
     */
    public String getEncoding()
    {
    	return mEncoding;
    }
}
