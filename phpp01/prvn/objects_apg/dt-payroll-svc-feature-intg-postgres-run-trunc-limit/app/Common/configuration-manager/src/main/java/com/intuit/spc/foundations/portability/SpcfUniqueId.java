package com.intuit.spc.foundations.portability;

import java.io.Serializable;

/**
 * SpcfUniqueId is the portable implementation of a globally unique 
 * identifier (GUID/UUID). GUID/UUIDs are 128-bit values with a string representation 
 * as a sequence of hexadecimal digits with the following standard format:
 * XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX.  There is no support for 
 * portable binary representation of the ids due to the difference in 
 * Guid (.NET) and UUID (Java) byte ordering.      
 */ 
@SuppressWarnings("unchecked")
public abstract class SpcfUniqueId implements Comparable, Serializable
{    
	/**
	 * needed for serialization
	 */  
	private static final long serialVersionUID = 6710464413643570130L;

	/**
    * string constant representing empty UUID/Guid 
    */
	public static final String EmptyGuid = "00000000-0000-0000-0000-000000000000";
     	
    /**
     * SpcfUniqueId instance that is used for static methods and is empty
     */
    private static SpcfUniqueId sEmptyUniqueId;  
    
    static
    {
    	sEmptyUniqueId = SpcfFactory.getInstance().createUniqueId(false); //instance for static methods
    } 
     
    /**
     * Create a new SpcfUniqueId instance 
     * @param initializeToRandomValue Initialize with a pseudo-random initial value if true
     * @return new instance of the SpcfUniqueId 
     */
    public static SpcfUniqueId createInstance(boolean initializeToRandomValue)
    {
    	return SpcfFactory.getInstance().createUniqueId(initializeToRandomValue);
    } 
    
    /** 
     * Constructs a new SpcfUniqueId instance with an empty value  
     * @return empty instance of the SpcfUniqueId
     */
    public static SpcfUniqueId createInstance()
    {
    	return SpcfFactory.getInstance().createUniqueId(false);
    }
    
    /**
     * Constructs a new SpcfUniqueId instance from the specified string.
     * The string must be a series of 32 hexadecimal digits in the standard
     * format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX or one of the acceptable
     * following formats: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX, 
     * {XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}, 
     * or (XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX). Any non-letter or non-digit 
     * character can be used as a separator, if consistent. 
     * @param s The string representation of the unique id value. 
     * @return new instance of the SpcfUniqueId containing the specified string value
     * @throws SpcfArgumentNullException - if the specified string is null
     * @throws SpcfIllegalArgumentException - if the specified string is empty or does 
     * not have the expected format
     */
    public static SpcfUniqueId createInstance(String s)
    {   
    	return SpcfFactory.getInstance().createUniqueId(s);
    }

    /**
     * Constructs a new SpcfUniqueId instance from the contents of the specified SpcfStringBuilder.
     * The buffer must be a series of 32 hexadecimal digits in the standard
     * format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX or one of the acceptable
     * following formats: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX, 
     * {XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}, 
     * or (XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX). Any non-hexadecimal 
     * digit character can be used as a separator, if consistent.
     * @param sb The buffer containing the string representation of the unique id value. 
     * @return new instance of the SpcfUniqueId containing the specified string value
     * @throws SpcfArgumentNullException - if the specified buffer is null
     * @throws SpcfIllegalArgumentException - if the specified buffer is empty or does 
     * not have the expected format 
     */
    public static SpcfUniqueId createInstance(SpcfStringBuilder sb)
    {  
    	return SpcfUniqueId.createInstance(sb.toString());
    }  
    
    /**
     * Indicates whether the specified object is equal to this <code>SpcfUniqueId</code>.  
     * @param o Object to compare with this <code>SpcfUniqueId</code>. 
     * @return <code>true</code> if the object is equal; <code>false</code> otherwise.
     */
    @Override
    public abstract boolean equals(Object o);
      
    /**
	 * Compares the current SpcfUniqueId to another, using the string in 
	 * standard format for comparison.
	 * @param o The object to compare against.
	 * @return 0 if the 2 items are equal, or an int indicating relative ordering.
	 * @throws SpcfArgumentNullException - if object o is null
     * @throws SpcfClassCastException If the object is not an SpcfUniqueId.
	 */ 
	public int compareTo(Object o) 
	{ 
		SpcfParamValidator.checkIsNotNull(o, "o");
		
		if (o instanceof SpcfUniqueId)
		{
			String s1 = getStandardFormatString();
			String s2 = ((SpcfUniqueId)o).getStandardFormatString();
			return s1.compareTo(s2);
		}
		else 
		{
			throw new SpcfClassCastException();
		} 
	} 
	
    /**
	 * Returns the hash code for the current SpcfUniqueId class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects. Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	@Override
    public abstract int hashCode();  
     
	/**
	 * Get the string representation of the unique id value.
	 * @return The string representation of the unique id value.
	 */
	@Override
    public abstract String toString();  
    
    /**
	 * Get the string representation of the unique id value 
	 * in standard format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX 
	 * @return The string representation of the unique id value 
	 * in standard format.
	 */
    public abstract String getStandardFormatString();  
   
    
    /**
     * Return an empty SpcfUniqueId instance that has value of:  
     * 00000000-0000-0000-0000-000000000000, where all bytes are set to zero.
     * @return empty instance of the SpcfUniqueId
     */
    public static SpcfUniqueId getEmptyUniqueId()
    { 
    	return sEmptyUniqueId; 
    }  
    
	/**
     * Create a new SpcfUniqueId instance with a pseudo-random initial value.   
     * @return new instance of the SpcfUniqueId
     */
    public static SpcfUniqueId generateRandomUniqueId()
    {
    	return SpcfFactory.getInstance().createUniqueId(true); 
    } 
     
    /**
	 * Generates the string representation of pseudo-random unique id.  
	 * @return string representation of the generated value with the
	 * format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX 
	 */
	public static String generateRandomUniqueIdString()
	{ 
    	return sEmptyUniqueId.doGenerateRandomUniqueIdString();
	} 
	
	/**
     * Forwarding virtual method for generateRandomUniqueIdString 
	 * @return string representation of the generated value with the
	 * format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX 
     */ 
	protected abstract String doGenerateRandomUniqueIdString();   
}
