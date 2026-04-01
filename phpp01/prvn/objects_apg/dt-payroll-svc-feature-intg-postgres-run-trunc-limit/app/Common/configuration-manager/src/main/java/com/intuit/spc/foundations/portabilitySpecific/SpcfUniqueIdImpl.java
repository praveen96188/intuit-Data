package com.intuit.spc.foundations.portabilitySpecific;
 
import java.util.UUID;

import com.intuit.spc.foundations.portability.*; 

/**
 * A platform specific implementation of SpcfUniqueId.   
 */
  
public class SpcfUniqueIdImpl extends SpcfUniqueId
{    
	/**
	 * needed for serialization
	 */  
    private static final long serialVersionUID = -5416092023493031186L; 
      
    /**
     * the byte array to hold the value of this UUID
     */
    private byte[] mBytes = null;  
    
    /** 
     * Constructs a new SpcfUniqueId instance with an empty value  
     */
 	public SpcfUniqueIdImpl()  
 	{ 
 		this(false);
 	}
 	
 	/**
     * Constructs a new SpcfUniqueId instance with either empty or random value.
     * @param initializeToRandomValue Initialize with a pseudo-random initial value if true
     */
 	public SpcfUniqueIdImpl(boolean initializeToRandomValue)  
 	{   
 		String uniqueId = null;
 		if (initializeToRandomValue)
 		{
 			uniqueId = doGenerateRandomUniqueIdString();
 		}
 		else
 		{
 			uniqueId = EmptyGuid;
 		} 
 		
 		mBytes = toByteArray(uniqueId);
 	} 
 	
 	/**
     * Constructs a new SpcfUniqueId instance from the specified string.
     * The string must be a series of 32 hexadecimal digits in the standard
     * format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX or one of the acceptable
     * following formats: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX, 
     * {XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}, 
     * or (XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX). Any non-hexadecimal 
     * digit character can be used as a separator, if consistent. 
     * @param s The string representation of the unique id value. 
     * @throws SpcfArgumentNullException - if the specified string is null
     * @throws SpcfIllegalArgumentException - if the specified string is empty or does 
     * not have the expected format
     */
 	public SpcfUniqueIdImpl(String s)
 	{ 
 		SpcfParamValidator.checkIsNotNull(s, "s"); 
 		
 		mBytes = toByteArray(validateFormat(s));
 	} 
 	
 	/**
     * Constructs a new SpcfUniqueId instance from the specified byte array.
     * The byte array must be 16 bytes long and match byte order for java.util.UUID
     * @param bytes The binary representation of the java.util.UUID. 
     * @throws SpcfArgumentNullException - if the specified byte array is null
     * @throws SpcfIllegalArgumentException - if the byte array is not the expected size.  
     */
 	public SpcfUniqueIdImpl(byte[] bytes)
 	{ 
 		SpcfParamValidator.checkIsNotNull(bytes, "bytes");
 		if( bytes.length != 16 )
 		{
 			throw new SpcfIllegalArgumentException( "bytes must be 16 bytes in length" );
 		}   
 		mBytes = bytes;
 	}  

 	/**
     * Constructs a new SpcfUniqueId instance from the specified java.util.UUID.
     * @param uid The java.util.UUID to encapsulate. 
     * @throws SpcfArgumentNullException - if the specified byte java.util.UUID is null
     */
    public SpcfUniqueIdImpl(java.util.UUID uid)
    {
        SpcfParamValidator.checkIsNotNull(uid, "uid");
        mBytes = toByteArray(uid.toString());
    } 
 
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfUniqueId#equals(Object)
	 */
    @Override
    public boolean equals(Object o)
    { 
		if (o == null)
		{
			return false;
		}

		if (o instanceof SpcfUniqueIdImpl)
		{ 
			return java.util.Arrays.equals(mBytes, ((SpcfUniqueIdImpl)o).toByteArray());
		}
		return false;
	}
      
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfUniqueId#hashCode()
	 */
    @Override
	public int hashCode()
	{
		int hash = 0;
        for (int i = 0; i < mBytes.length; i ++)
        	hash += mBytes[i] * 31 ^ mBytes.length - (i + 1); 
        return hash;
	}
	
    /** 
     * @see com.intuit.spc.foundations.portability.SpcfUniqueId#toString()
     */
    @Override
    public String toString()
    {
 		long mostSig = 0;
 		for (int i = 0; i < 8; i++) {
 			mostSig = (mostSig << 8) | (mBytes[i] & 0xff);
 		}

 		long leastSig = 0;
 		for (int i = 8; i < 16; i++) {
 			leastSig = (leastSig << 8) | (mBytes[i] & 0xff);
 		}
 		java.util.UUID uid = new UUID(mostSig, leastSig); 
 		return uid.toString();
    } 
    
    /** 
     * @see com.intuit.spc.foundations.portability.SpcfUniqueId#getStandardFormatString()
     */
    @Override
    public String getStandardFormatString()
    {   
    	return toString();
    }  
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfUniqueId#doGenerateRandomUniqueIdString()
     */
    @Override
	protected String doGenerateRandomUniqueIdString()
	{  
		java.util.UUID uid = java.util.UUID.randomUUID();
		return uid.toString(); 
	} 
	 
	 /**
     * Get the byte array in standard network byte order.
     * @param standardizedString the UUID string in standarized order
     * @return the UUID in a byte array in standard network byte order
     */ 
   private byte[] toByteArray(String standardizedString)
   {
   		byte[] byteArray = new byte[16];
   		String id = standardizedString;
   		for (int i = 0, j = 0; i < 36; ++j)  
   		{  
   			// Need to bypass hyphens:  
   			switch (i)  
   			{  
   			case 8 :  
   			case 13 :  
   			case 18 :  
   			case 23 :  
   				++i;  
   			}  
   			char c = id.charAt(i);  

   			if (c >= '0' && c <= '9')  
   			{  
   				byteArray[j] = (byte) ((c - '0') << 4);  
   			}  
   			else if (c >= 'a' && c <= 'f')  
   			{  
   				byteArray[j] = (byte) ((c - 'a' + 10) << 4);  
   			}  
   			c = id.charAt(++i);  

   			if (c >= '0' && c <= '9')  
   			{  
   				byteArray[j] |= (byte) (c - '0');  
   			}  
   			else if (c >= 'a' && c <= 'f')  
   			{  
   				byteArray[j] |= (byte) (c - 'a' + 10);  
   			}   
   			++i;  
   		} 
   		return byteArray; 
   }

	 /**
      * Get the byte array in standard network byte order.
      * @return the UUID in a byte array in standard network byte order
      */ 
    public byte[] toByteArray()
    {
    	return mBytes; 
    }

    /**
     *  Get the equivalent platform specific java.util.UUID
     *  @return the java.util.UUID representation of this value
     */ 
    public java.util.UUID getSpecific()
    {
    	//return java.util.UUID.fromString(getStandardizedString()); 
    	return java.util.UUID.fromString(toString()); 
    } 
    
    /**
     * Validates a string to see if its in accepted format.  Returns standard 
     * format if it is, else throws SpcfIllegalArgumentException. 
     * The string must be a series of 32 hexadecimal digits in the standard
     * format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX or one of the acceptable
     * following formats: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX, 
     * {XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}, 
     * or (XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX). Any non-hexadecimal 
     * digit character can be used as a separator, if consistent. 
     * @param s The string representation of the unique id value.  
     * @return the UUID string in standard format
     * @throws SpcfIllegalArgumentException - if the specified string is empty or does 
     * not have the expected format
     */
    protected String validateFormat(String s)
    {  
    	// valid lengths are 32, 36, 34, 38
    	// valid brackets are {} and () 
    	
    	int original_length = s.length(); 
    	String tempId = s.toLowerCase();  
    	
    	if (original_length == 0)
    	{ 
    		throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "cannot be zero length.");
        }
    	
    	if (original_length > 38 || original_length < 32 )
    	{ 
    		throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "is not the correct size");
        } 
    	
    	if (s.charAt(0) == '{')
    	{
    		if (s.charAt(original_length - 1) != '}')	
    		{ 
    			throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "does not have the correct format.");
			}  
    		tempId = tempId.substring(1, original_length - 1);
    	}
    	
    	if (s.charAt(0) == '(')
    	{
    		if (s.charAt(original_length - 1) != ')')	
    		{ 
    			throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "does not have the correct format.");
			} 
    		tempId = tempId.substring(1, original_length - 1);
    	}
    	
    	  
    	// at this point, length must be either 32 (no separators) or 36 to be valid 
    	if (tempId.length() == 36)
    	{
    		 //remove separators and check consistency 
    		char separator;
    		separator = tempId.charAt(8);
    		boolean separatorsNotConsistent = false; 
    			 
			if (java.lang.Character.isLetterOrDigit(separator))
			{  
				//separator cannot be a hex digit
				throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "does not have the correct format.");
    		}  
			// make sure separator is consistent
			if (tempId.charAt(13) != separator) { separatorsNotConsistent = true;}
			if (tempId.charAt(18) != separator) { separatorsNotConsistent = true;}
			if (tempId.charAt(23) != separator) { separatorsNotConsistent = true;}
			
			if (separatorsNotConsistent)
			{ 
				throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "does not have the correct format.");
			}   
			
			StringBuilder sb = new StringBuilder(); 
	    	sb.append(tempId.substring(0, 8));  
			sb.append(tempId.substring(9, 13));  
			sb.append(tempId.substring(14, 18));  
			sb.append(tempId.substring(19, 23));  
			sb.append(tempId.substring(24));    
			 
			//remove separators and check consistency  
			tempId = sb.toString();
    	} 
		
    	// at this point separators are 
    	if (tempId.length() != 32) 
    	{ 
    		throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "does not have the correct format.");
    	}   
    	 
    	//validate hex digits
    	String hexDigits = "0123456789abcdef";  
    	char c;
    	for (int i = 0; i < tempId.length(); i++) 
 		{ 
    		c = tempId.charAt(i); 
			if (hexDigits.indexOf(c) < 0) 
			{   
				throw new SpcfIllegalArgumentException("Unique ID: " + s + " " + "has invalid hexadecimal digits.");
			}  
 		} 
    	 
    	// add standard separators
    	StringBuilder sbFinalId = new StringBuilder();  
    	sbFinalId.append(tempId.substring(0, 8)); 
    	sbFinalId.append("-");
    	sbFinalId.append(tempId.substring(8, 12)); 
    	sbFinalId.append("-");
    	sbFinalId.append(tempId.substring(12, 16)); 
    	sbFinalId.append("-");
    	sbFinalId.append(tempId.substring(16, 20)); 
    	sbFinalId.append("-");
    	sbFinalId.append(tempId.substring(20));    
		return sbFinalId.toString();
    }    
    
} 
