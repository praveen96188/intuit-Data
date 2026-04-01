package com.intuit.spc.foundations.primary.logging;

import com.intuit.spc.foundations.portability.*;

/**
 * The portable wrapper class for a platform specific logger. 
 * All logging methods should be accessed 
 * via the methods of this class. Accessing logging methods through the 
 * SpcfLogger class is recommended because the
 * underlying implementation might be changed in future to use another
 * logging package.
 * <p>
 * The SPC-F Logging Component provides API's to set
 * configuration through the {@link SpcfLogManager} class.  
 * See the Users Guide for more information on configuring the 
 * logger component.
 * </p>
 * <p>
 * The SPC-F Logging Component configuration determines how an object and 
 * throwable/exception are logged.  The appender and its layout will
 * define the output of a logging request.  Any object renderers defined will
 * dictate how an object is rendered into the log output.  The default object
 * renderer calls the toString() method on the object.
 * </p>
 * <p>
 * Usage examples for instantiating a logger and logging a message.
 * </p>
 * <p>
 * Java code sample:
 * </p>
 * <pre>
 *   SpcfLogger logger = SpcfLogManager.getLogger("myNamespace.MyClassName");
 *
 *   // log simple message at Debug level
 *   logger.debug("simple debug message");
 * 
 *   // log simple message at Info level
 *   logger.info("simple info message");
 * 
 *   // log simple message at Info level
 *   logger.log(SpcfLevel.Info, "simple info message");
 * </pre>
 * <p>
 * C# code sample:
 * </p>
 * <pre>
 *   SpcfLogger logger = SpcfLogManager.GetLogger("MyNamespace.MyClassName");
 *
 *   // example log simple message at Debug level
 *   logger.Debug("simple debug message");
 * 
 *   // log simple message at Info level
 *   logger.Info("simple info message");
 * 
 *   // log simple message at Info level
 *   logger.Log(SpcfLevel.Info, "simple info message");
 * </pre>
 * <p>
 * For more information go to: <a href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Logging_2.0">SPC-F Portable Logging 2.0 User Guide</a>
 * </p>
*/
public abstract class SpcfLogger 
{
	/**
	 * Key used to get the event ID from the context:
	 */
	public static final String EventIdContextKeyString = "EventID";
	

	/**
	 * protected constructor
	 */
	protected SpcfLogger() 
	{
	}


	/**
	 * Log the object and throwable/exception at the specified level.
	 * If level is null, then nothing is logged.
	 * @param level the level to log the object at
	 * @param obj the object to log
	 * @param t the throwable/exception to log
	 */
	public abstract void log(SpcfLevel level, Object obj, Throwable t);


	/**
	 * Log the object and throwable/exception at the specified level.
	 * Associate the logging event with an event ID that will appear in the Microsoft Windows Event Log.
	 * If level is null, then nothing is logged.
	 * @param eventId the event ID that should appear in the Microsoft Windows Event Log
	 * @param level the level to log the object at
	 * @param obj the object to log
	 * @param t the throwable/exception to log
	 */
	public void log(SpcfLevel level, Object obj, Throwable t, String eventId)
	{
		// While the following looks like it might need synchronization, it turns
		// out that the setContext/putContext interacts with thread-local maps,
		// thusly avoiding the need for synchronization...
		
		// Get the old event ID from the context so that we can restore its value
		// when we are done logging. Otherwise, everything after this call will have
		// the same event ID associated with it:
		String oldEventId = SpcfLogManager.getContext(EventIdContextKeyString);
		
		// Put the new event ID in the context:
		SpcfLogManager.putContext(EventIdContextKeyString, eventId);
		
		// Log as normal. If configured, the pattern layout will pull the event ID 
		// from the context and add it to the appropriate locations:
		log(level, obj, t);
		
		// If the old event ID was null, delete it from the context;
		// otherwise, put it back in the context:
    	if (oldEventId == null) 
   		{
    		SpcfLogManager.removeContext(EventIdContextKeyString);
   		}
    	else
    	{
    		SpcfLogManager.putContext(EventIdContextKeyString, oldEventId);	
    	}
	}

	
	/**
	 * Log the object at the specified level.
	 * If level is null, then nothing is logged.
	 * @param level the level to log the object at
	 * @param obj the object to log
	 */
	public void log(SpcfLevel level, Object obj)
	{
		log(level, obj, null);
	}

	
	/**
	 * Log the specified incident.  The incident variable contains message to 
	 * log and the SpcfPriority which dictates the level to log at.
	 * If incident parameter is null, nothing is logged.
	 * @param incident the incident to log
	 */
	public void log(ISpcfIncident incident)
	{
		try
		{
    		if (incident == null)
    		{
        		SpcfLogManager.internalLogError("Null incident parameter not allowed in SpcfLogger.log.", null);
        		return;
    		}
			log(SpcfLevel.convertPriority(incident.getPriority()), incident.toString());
		} 
		catch (Exception e) 
		{
    		SpcfLogManager.internalLogError("Exception thrown logging incident: ", e);
		}
	}
	
	
	/**
	 * Check whether this logger is enabled for the specified Level.
	 * @param level logging level to determine if enabled
	 * @return true if this logger is enabled for the specified level , 
	 * false if this logger is not enabled for the specified level or if there
	 * was an error determining if enabled at the specified level 
	 * or if level parameter is null.
	 */
	protected abstract boolean isEnabledFor(SpcfLevel level);

	
	/**
	 * Check whether this logger is enabled for the Debug Level.
	 * @return true if this logger is enabled for level Debug, 
	 * false if this logger is not enabled for level Debug or if there was an
	 * error determining if enabled at Debug level.
	 */
	public boolean isDebugEnabled()
	{
		return isEnabledFor(SpcfLevel.Debug);
	}

	
	/**
	 * Check whether this logger is enabled for the Info Level.
	 * @return true if this logger is enabled for level Info, 
	 * false if this logger is not enabled for level Info or if there was an
	 * error determining if enabled at Info level.
	 */
	public boolean isInfoEnabled()
	{
		return isEnabledFor(SpcfLevel.Info);
	}

	
	/**
	 * Check whether this logger is enabled for the Trace level.
	 * @return true if this logger is enabled for level Trace, 
	 * false if this logger is not enabled for level Trace or if there was an
	 * error determining if enabled at Trace level.
	 */
	public boolean isTraceEnabled()
	{
		return isEnabledFor(SpcfLevel.Trace);
	}

	/**
	 * Log the object at the Trace level.
	 * @param obj the object to log
	 */
	public void trace(Object obj) 
	{
		log(SpcfLevel.Trace, obj, null);
	}
	

	/**
	 * Log the object and throwable/exception at the Trace level
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void trace(Object obj, Throwable t) 
	{
		log(SpcfLevel.Trace, obj, t);
	}

	
	/**
	 * Log the object and throwable/exception at the Trace level
	 * @param eventId event ID to use in the Microsoft Windows Event Log
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void trace(Object obj, Throwable t, String eventId) 
	{
		log(SpcfLevel.Trace, obj, t, eventId);
	}

	
	/**
	 * Log the object at the Debug level.
	 * @param obj the object to log
	 */
	public void debug(Object obj) 
	{
		log(SpcfLevel.Debug, obj, null);
	}

	
	/**
	 * Log the object and throwable/exception at the Debug level
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void debug(Object obj, Throwable t) 
	{
		log(SpcfLevel.Debug, obj, t);
	}

	
	/**
	 * Log the object and throwable/exception at the Debug level
	 * @param eventId event ID to use in the Microsoft Windows Event Log
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void debug(Object obj, Throwable t, String eventId) 
	{
		log(SpcfLevel.Debug, obj, t, eventId);
	}

	
	/**
	 * Log the object at the Info level.
	 * @param obj the object to log
	 */
	public void info(Object obj) 
	{
		log(SpcfLevel.Info, obj, null);
	}

	
	/**
	 * Log the object and throwable/exception at the Info level
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void info(Object obj, Throwable t) 
	{
		log(SpcfLevel.Info, obj, t);
	}

	
	/**
	 * Log the object and throwable/exception at the Info level
	 * @param eventId event ID to use in the Microsoft Windows Event Log
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void info(Object obj, Throwable t, String eventId) 
	{
		log(SpcfLevel.Info, obj, t, eventId);
	}

	
	/**
	 * Log the object at the Warn level.
	 * @param obj the object to log
	 */
	public void warn(Object obj) 
	{
		log(SpcfLevel.Warn, obj, null);
	}

	
	/**
	 * Log the object and throwable/exception at the Warn level
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void warn(Object obj, Throwable t) 
	{
		log(SpcfLevel.Warn, obj, t);
	}

	
	/**
	 * Log the object and throwable/exception at the Warn level
	 * @param eventId event ID to use in the Microsoft Windows Event Log
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void warn(Object obj, Throwable t, String eventId) 
	{
		log(SpcfLevel.Warn, obj, t, eventId);
	}

	
	/**
	 * Log the object at the Error level.
	 * @param obj the object to log
	 */
	public void error(Object obj) 
	{
		log(SpcfLevel.Error, obj, null);
	}
	

	/**
	 * Log the object and throwable/exception at the Error level
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void error(Object obj, Throwable t) 
	{
		log(SpcfLevel.Error, obj, t);
	}

	
	/**
	 * Log the object and throwable/exception at the Error level
	 * @param eventId event ID to use in the Microsoft Windows Event Log
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void error(Object obj, Throwable t, String eventId) 
	{
		log(SpcfLevel.Error, obj, t, eventId);
	}
	
	
	/**
	 * Log the object at the Fatal level.
	 * @param obj the object to log
	 */
	public void fatal(Object obj) 
	{
		log(SpcfLevel.Fatal, obj, null);
	}

	
	/**
	 * Log the object and throwable/exception at the Fatal level.
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void fatal(Object obj, Throwable t) 
	{
		log(SpcfLevel.Fatal, obj, t);
	}
	
	
	/**
	 * Log the object and throwable/exception at the Fatal level.
	 * @param eventId event ID to use in the Microsoft Windows Event Log
	 * @param obj the object to log
	 * @param t the exception to log
	 */
	public void fatal(Object obj, Throwable t, String eventId) 
	{
		log(SpcfLevel.Fatal, obj, t, eventId);
	}
}

