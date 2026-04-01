package com.intuit.spc.foundations.portability;

import java.io.Serializable;

/**
 * Enumeration class providing a static set of <code>SpcfPriority</code> instances that
 * are associated with an <code>ISpcfException</code> instance and used to
 * indicate the severity of the error.
 */
public final class SpcfPriority implements Comparable, Serializable
{
    // needed for serialization - (some misc number)
    private static final long serialVersionUID = -5516092054498031989L;

    // constants used for generating hashCode
    private static final int Hash1 = 17;
    private static final int Hash2 = 37;

    //private static final int DEBUG_INT = 1000;
    private static final int InfoInt = 2000;
    private static final int WarnInt = 3000;
    private static final int ErrorInt = 4000;
    private static final int FatalInt = 5000;

    //private static final String DEBUG_STRING = "DEBUG";
    private static final String InfoString = "INFO";
    private static final String WarnString = "WARN";
    private static final String ErrorString = "ERROR"; 
    private static final String FatalString = "FATAL";

    /**
     * Information only; this priority is associated with the normal
     * lifecycle of an application.
     */
    public static final SpcfPriority Info  = new SpcfPriority( InfoInt,
                                                       InfoString);

    /**
     * Warning indicating a minor problem caused by factors external to the
     * application, or indicating that resources are getting lower than
     * desired. No immediate action needs to be taken. 
     */
    public static final SpcfPriority Warn  = new SpcfPriority( WarnInt,
                                                       WarnString);

    /**
     * Unexpected error that must be investigated; however, the
     * application can continue to service subsequent requests.
     */
    public static final SpcfPriority Error = new SpcfPriority( ErrorInt,
                                                       ErrorString);

    /**
     * Severe error that needs to be investigated immediately; implies an
     * imminent crash of the application or subcomponent.
     */
    public static final SpcfPriority Fatal = new SpcfPriority( FatalInt,
                                                       FatalString);
   
    /**
     * @return The maximum, highest priority
     */
    public static SpcfPriority getMaxValue()
    {
    	return Fatal;
    }
    
    /**
     * @return The minimum, lowest priority
     */
    public static SpcfPriority getMinValue()
    {
    	return Info;
    }
    
    private int mLevel;
    private String mMsg;

    /**
	 * Parse the string into an SpcfPriority
	 *
	 * @param source textual representation of a priority
	 * @return an instance of a priority
	 */
	public static SpcfPriority parse(String source)
	{
		if (source == null)
		{
			return null;
		}
		String s = source.toLowerCase();
		
		if (s.equals("info") == true)
		{
			return SpcfPriority.Info;
		}
		else if (s.equals("warn") == true || s.equals("warning") == true)
		{
			return SpcfPriority.Warn;
		}
		else if (s.equals("err")==true || s.equals("error")==true)
		{	
			return SpcfPriority.Error;
		}
		else if(s.equals("fatal") == true)
		{
			return SpcfPriority.Fatal;
		}
		else
		{
			return null;
		}
	}
	
    private SpcfPriority(int level, String msg) {
        this.mLevel = level;
        this.mMsg = msg;
    }

    /**
     * Compares the current <code>SpcfPriority</code> to a specified
     * <code>SpcfPriority</code>.
     * 
     * @param o SpcfPriority to which to compare the current <code>SpcfPriority</code>.
     *
     * @return One of the following:
     * <ul>
     * <li> 0 if the priorities are equal
     * <li> A negative number if the current priority is less than the
     * passed-in priority
     * <li> A positive number if the current priority is greater than the
     * passed-in priority
     * </ul>
     */
    public int compareTo(Object o) {
        
    	SpcfParamValidator.checkIsNotNull(o, "object");

        SpcfPriority p = (SpcfPriority) o;
        
        if (mLevel < p.mLevel) {
            return -1;
        } else if (mLevel > p.mLevel) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Indicates whether the specified object is equal to this
     * <code>SpcfPriority</code>.
     *
     * @param obj Object to compare with this <code>SpcfPriority</code>.
     *
     * @return <code>true</code> if the object is equal; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof SpcfPriority) {
        	SpcfPriority p = (SpcfPriority) obj;
            
            if (p.mLevel == this.mLevel) {
                return true;
            }
        }
            
        return false;
    }

    /**
     *  Returns a hash code value for this <code>SpcfPriority</code>.
     *
     *  @return Hash code value.
     */
    public int hashCode() {
        // It's *REQUIRED* when overriding equals method
        // to override hashCode method as well.
        // This method implements a basic algorithm
        // as suggested by:
        // Effective Java Programming Guide by Guy Steele
        // pp 33-41
        int result = Hash1;
        result = Hash2 * result + mLevel;
        result = Hash2 * result + mLevel;

        return result;
    }

    /**
     *  Returns the message set in this <code>SpcfPriority</code>.
     *
     *  @return message string value.
     */
    public String toString() {
        return mMsg;
    }
}
