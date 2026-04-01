package com.intuit.spc.foundations.portability.threading;

import com.intuit.spc.foundations.portability.SpcfParamValidator;

/***
 * 
 * Used to specify a SpcfThread priority. The runtime schedules threads to execute
 * based on priority and other factors.
 * @see SpcfThread#setPriority(SpcfThreadPriority)
 * @see SpcfThread#getPriority()
 *
 */
public class SpcfThreadPriority {
	
	/***
	 * Lowest priority a thread can run at.
	 */
    public static final SpcfThreadPriority 
    Minimum = new SpcfThreadPriority (1, "Minimum");

	/***
	 * The default priority which is typical for most threads.
	 */
    public static final SpcfThreadPriority 
    Normal = new SpcfThreadPriority (2, "Normal");
    
	/***
	 * The highest priority a thread can run at.
	 */
    public static final SpcfThreadPriority 
    Maximum = new SpcfThreadPriority (3, "Maximum");

    /**
     * Initializes a new instance of this class
     *
     * @param id Unique id
     * @param name Name
     */
    private SpcfThreadPriority(int id, String name)
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
    public static SpcfThreadPriority parse(String val)
    {
    	SpcfParamValidator.checkIsNotNull(val, "val");
        String lowerCase = val.toLowerCase();
        if (lowerCase.equals("minimum"))
        {
            return SpcfThreadPriority.Minimum;
        }
        else if (lowerCase.equals("normal"))
        {
            return SpcfThreadPriority.Normal;
        }  
        if (lowerCase.equals("maximum"))
        {
            return SpcfThreadPriority.Maximum;
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
