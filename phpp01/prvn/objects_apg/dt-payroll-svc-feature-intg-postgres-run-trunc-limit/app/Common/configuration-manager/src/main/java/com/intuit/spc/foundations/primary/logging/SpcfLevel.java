package com.intuit.spc.foundations.primary.logging;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfPriority;

/**
 * Enumeration class providing a static set of <code>SpcfLevel</code> instances that
 * are associated with a level of the log message.
 * A SpcfLevel can be associated with a log message, a logger and
 * a logger repository.
 * <p>
 * The order of levels are such that the integer values of the levels have 
 * the following order:
 * </p>
 * <p>
 * Off &gt; Fatal &gt; Error &gt; Warn &gt; Info &gt; Debug &gt; Trace &gt; All
 * </p>
 */
public class SpcfLevel
{
	/**
	 * The integer value for this level
	 */
    private int mLevel;
    
    /**
     * The string value for this level
     */
    private String mLevelStr;

	/**
	 * Integer representation of the <code>Off</code> level.
	 */
	final static int OffInt = Integer.MAX_VALUE;
    /**
	 * Integer representation of the <code>Fatal</code> level.
	 */
    final static int FatalInt = 50000;
    /** 
       Integer representation of the <code>Error</code> level.
     */
    final static int ErrorInt = 40000;
    /**
       Integer representation of the <code>Warn</code> level.
     */
    final static int WarnInt  = 30000;
    /**
       Integer representation of the <code>Info</code> level.
     */
    final static int InfoInt  = 20000;

    /**
       Integer representation of the <code>Debug</code> level.
     */
    final static int DebugInt = 10000;

    /**
       Integer representation of the <code>Trace</code> level.
     */
    final static int TraceInt = 5000;

    /**
       Integer representation of the <code>All</code> level.
     */
	final static int AllInt = Integer.MIN_VALUE;

    /**
     * This <code>SpcfLevel</code> instance has the highest possible rank and is
     * intended to turn off logging.
     * <p>Integer value is Integer.MAX_VALUE in java and System.Int32.MaxValue in dotnet code.</p>
     * <p>String value is OFF</p>
	 */
	final static public SpcfLevel Off = new SpcfLevel(OffInt, "OFF");

    /**
     * This <code>SpcfLevel</code> instance is used for very severe error events 
     * that will presumably lead the application to abort.
     * <p>Integer value is 50000</p>
     * <p>String value is FATAL</p>
     */
    final static public SpcfLevel Fatal = new SpcfLevel(FatalInt, "FATAL");

    /**
     * This <code>SpcfLevel</code> instance is used for 
     * error events that might allow the application to continue running.  
     * <p>Integer value is 40000</p>
     * <p>String value is ERROR</p>
     */
    final static public SpcfLevel Error = new SpcfLevel(ErrorInt, "ERROR");
    
    /**
     * This <code>SpcfLevel</code> instance is used for 
     * potentially harmful situations.
     * <p>Integer value is 30000</p>
     * <p>String value is WARN</p>
     */
    final static public SpcfLevel Warn  = new SpcfLevel(WarnInt, "WARN");

    /**
     * This <code>SpcfLevel</code> instance is used for 
     * informational messages that highlight the progress of the
     * application at a coarse-grained level.
     * <p>Integer value is 20000</p>
     * <p>String value is INFO</p>
     */
    final static public SpcfLevel Info  = new SpcfLevel(InfoInt, "INFO");

    /**
     * This <code>SpcfLevel</code> instance is used for 
     * fine-grained informational events that are most useful for debugging.
     * <p>Integer value is 10000</p>
     * <p>String value is DEBUG</p>
     */
    final static public SpcfLevel Debug = new SpcfLevel(DebugInt, "DEBUG");

    /**
     * This <code>SpcfLevel</code> instance is used for 
     * finest-grained informational events that are most useful for tracing.
     * <p>Integer value is 5000</p>
     * <p>String value is TRACE</p>
     */
    final static public SpcfLevel Trace = new SpcfLevel(TraceInt, "TRACE");

    /**
     * This <code>SpcfLevel</code> instance has the lowest possible rank and 
     * is intended to turn on all logging.
     * <p>Integer value is Integer.MIN_VALUE in java and System.Int32.MinValue in dotnet code.</p>
     * <p>String value is ALL</p>
     */
    final static public SpcfLevel All = new SpcfLevel(AllInt, "ALL");

    /**
     * Private constructor
     * @param level the integer value defining this level
     * @param levelStr the string value defining this level
     */
    private SpcfLevel(int level, String levelStr) 
    {
        this.mLevel = level;
        this.mLevelStr = levelStr;
    }
    
    /**
     * The integer level value for this instance.
     * @return integer level value
     */
	public int getLevelInt()
	{
		return this.mLevel;
	}

	/**
	 * The string level value for this instance.
	 * @return string level value
	 */
	public String getLevelString()
	{
		return this.mLevelStr;
	}

	/**
	 * The string representation for this level.  This method will return string
	 * level value for this instance.
	 * @return string level value
	 */
	public String toString()
	{
		return this.mLevelStr;
	}

	/**
	 * A package protected method to convert the SpcfPriority to SpcfLevel
	 * @param priority the instance of SpcfPriority to convert
	 * @return instance of SpcfLevel
	 */
	static SpcfLevel convertPriority(SpcfPriority priority) 
	{
		if (priority == null) return null;
		if (priority == SpcfPriority.Error) return SpcfLevel.Error;
		if (priority == SpcfPriority.Fatal) return SpcfLevel.Fatal;
		if (priority == SpcfPriority.Info) return SpcfLevel.Info;
		if (priority == SpcfPriority.Warn) return SpcfLevel.Warn;
		throw new SpcfIllegalArgumentException();
	}
}
