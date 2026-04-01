package com.intuit.spc.foundations.portability.threading;

import com.intuit.spc.foundations.portability.SpcfParamValidator;


/***
 *
 * Used by SpcfThread to indicate the current state of the runtime time.
 * @see SpcfThread#getThreadState()
 *
 */
public class SpcfThreadState 
{
	/***
	 * The thread was created, but not started.
	 */
    public static final SpcfThreadState 
    Initialized = new SpcfThreadState (1, "Initialized");

	/***
	 * The thread was created and start was called.
	 */
    public static final SpcfThreadState 
    Running = new SpcfThreadState (2, "Running");
    
	/***
	 * The thread completed execution. This occurs when the the thread's entry point 
	 * method has returned.	
	 */
    public static final SpcfThreadState 
    Terminated = new SpcfThreadState (3, "Terminated");

	/***
	 * The thread is in a blocking state. This can occur when a thread sleeps or is joined.
	 */
    public static final SpcfThreadState 
    Wait = new SpcfThreadState (4, "Wait");

    /**
     * Initializes a new instance of this class
     *
     * @param id Unique id
     * @param name Name
     */
    private SpcfThreadState(int id, String name)
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
    public static SpcfThreadState parse(String val)
    {
    	SpcfParamValidator.checkIsNotNull(val, "val");
        String lowerCase = val.toLowerCase();
        if (lowerCase.equals("initialized"))
        {
            return SpcfThreadState.Initialized;
        }
        else if (lowerCase.equals("running"))
        {
            return SpcfThreadState.Running;
        }  
        if (lowerCase.equals("terminated"))
        {
            return SpcfThreadState.Terminated;
        }
        else if (lowerCase.equals("wait"))
        {
            return SpcfThreadState.Wait;
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