package com.intuit.spc.foundations.portability.text;

/**
* An encapsulation of standard time formats.
*
*/
public class SpcfTimeEnum
{

	/**
	 * The standard short time format.
	 * <br>Example:
	 * <ul>
	 * <li>3:30 PM
	 * </ul>
	 */
    public static final SpcfTimeEnum 
    ShortFormat = new SpcfTimeEnum (1, "ShortFormat");

	/**
	 * The standard long time format.
	 * <br>Example: 
	 * <ul>
	 * <li>3:30:32 PM
	 * </ul>
	 */
    public static final SpcfTimeEnum 
    LongFormat = new SpcfTimeEnum (2, "LongFormat");       

    /**
     * Initializes a new instance of this class
     *
     * @param id Unique id
     * @param name Name
     */
    private SpcfTimeEnum(int id, String name)
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
    public static SpcfTimeEnum parse(String val)
    {
        String lowerCase = val.toLowerCase();
        if (lowerCase.equals("shortformat"))
        {
            return SpcfTimeEnum.ShortFormat;
        }
        else if (lowerCase.equals("longformat"))
        {
            return SpcfTimeEnum.LongFormat;
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