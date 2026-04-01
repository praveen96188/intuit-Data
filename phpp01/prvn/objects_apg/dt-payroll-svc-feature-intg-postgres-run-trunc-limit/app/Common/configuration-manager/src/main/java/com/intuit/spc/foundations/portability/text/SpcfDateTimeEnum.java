package com.intuit.spc.foundations.portability.text;

/**
* An encapsulation of standard date/time formats.
* 
*/
public class SpcfDateTimeEnum
{	
	/**
	 * The date/time pattern defined in RFC 1123.
	 * <br>Example:
	 * <ul>
	 * <li>Thu, 03 Jan 2002 00:00:00 GMT.
	 * </ul>
	 */	
    public static final SpcfDateTimeEnum 
    Rfc1123 = new SpcfDateTimeEnum (1, "Rfc1123");

	/**
	 * The sortable date/time pattern defined in ISO8601. This format is also
	 * used for the XML (XSD) dateTime datatype.<br>	
	 * Though a valid ISO8601 formated string can take many variations but SPC-F allows only a limited set of variations. 
	 * For a IS08601 formatted date to be compliant with SPC-F, follwoing rules must be followed: <br>
	 * <ul>
	 * <li>The text must follow the format "yyyy-MM-ddTHH:mm:ss.SZ". 
	 * <li>If time is present, it must be seperated from date using character 'T'. 
	 * <li>Date parts must be seperated using character '-'.
	 * <li>Time parts must be seperated using character ':' and milliseconds must be seperated by character '.'.
	 * <li>Time information is optional but date is mandatory.
	 * <li>The string must always end with 'Z'.  
	 * </ul>  	 
	 * <br>Examples:
	 * <ul>  
	 * <li>2002-01-03T00:00:00.0Z -> full date-time format including milliseconds.
	 * <li>2002-01-03T00:00:00Z -> date-time format, excluding milliseconds. 
	 * <li>2002-01-03T00:00Z -> date-time format, excluding seconds and milliseconds.
	 * <li>2002-01-03T00Z -> date-time format, excluding minutes, seconds and milliseconds.
	 * <li>2002-01-03Z -> date-time format, excluding time.
	 * </ul>
	 */
    public static final SpcfDateTimeEnum 
    Iso8601 = new SpcfDateTimeEnum (2, "Iso8601");       

    /**
     * Initializes a new instance of this class
     *
     * @param id Unique id
     * @param name Name
     */
    private SpcfDateTimeEnum(int id, String name)
    {
        setId(id);
        setName(name);
    }
	
	/**
     * The Id associated with this class
     */
    private int mId;

    /**
     * The Id associated with this class
     */
    public int getId()
    {
        return mId;
    }
    
    /**
     * The Id associated with this class
     */
    private void setId(int val)
    {
        mId = val;
    }
    
    /**
     * The Name associated with this class
     */
    private String mName;

    /**
     * The Name associated with this class
     */
    public String getName()
    {
        return mName;
    }
    
    /**
     * The Name associated with this class
     */
    private void setName(String name)
    {
        mName = name;
    }
    
    /**
     * converts text to instance
     * @param val
     * @return
     */
    public static SpcfDateTimeEnum parse(String val)
    {
        String lowerCase = val.toLowerCase();
        if (lowerCase.equals("rfc1123"))
        {
            return SpcfDateTimeEnum.Rfc1123;
        }
        else if (lowerCase.equals("iso8601"))
        {
            return SpcfDateTimeEnum.Iso8601;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * To convert into a string.
     */
    public String toString()
    {
    	return this.getName();
    }
}
