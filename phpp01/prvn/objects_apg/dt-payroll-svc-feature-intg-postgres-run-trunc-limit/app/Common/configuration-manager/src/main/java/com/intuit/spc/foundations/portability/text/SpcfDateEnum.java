package com.intuit.spc.foundations.portability.text;

/**
* An encapsulation of standard date formats. The exact result of
* these standard formats depends on the geographical and cultural region. 
*/
public class SpcfDateEnum
{	
	/**
	 * The standard short date format.<br>
	 * Examples:<br>
	 * <ul>
	 * <li>12/13/1952
	 * <li>1/3/2002
	 * </ul>	
	 */
    public static final SpcfDateEnum 
    ShortFormat = new SpcfDateEnum (1, "ShortFormat");

	/**
	 * The standard long date format.<br>	
	 * Example:<br>
	 * <ul>
	 * <li>Saturday, December 13, 1952
	 * </ul>
	 */	
    public static final SpcfDateEnum 
    LongFormat = new SpcfDateEnum (2, "LongFormat");       

    /**
     * Initializes a new instance of this class
     *
     * @param id Unique id
     * @param name Name
     */
    private SpcfDateEnum(int id, String name)
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
    public static SpcfDateEnum parse(String val)
    {
        String lowerCase = val.toLowerCase();
        if (lowerCase.equals("shortformat"))
        {
            return SpcfDateEnum.ShortFormat;
        }
        else if (lowerCase.equals("longformat"))
        {
            return SpcfDateEnum.LongFormat;
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