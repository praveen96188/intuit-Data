package com.intuit.spc.foundations.primary.logging;
 
import com.intuit.spc.foundations.portability.SpcfSystem;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.threading.SpcfThread;
import com.intuit.spc.foundations.portability.threading.ISpcfThreadUncaughtExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * The SPC-F Logging component is comprised of a set of loggers referred to as 
 * a repository of loggers.  The SpcfLogManager class provides methods to 
 * configure the repository of loggers used by an application.
 * This class provides static methods to
 * set the configuration used by this repository of loggers,
 * set or get the threshold for this repository, 
 * get an instance of a specific logger, 
 * and set or get whether the internal logging is enabled for the 
 * SPC-F Logging component.
 * <p>
 * <b>Logger Repository Configuration:</b>  The SPC-F Logging component can be 
 * configured with the platform specific logger xml format configuration.
 * The following key/values can be set in the configuration, 
 * </p>
 * <p>- set the key "SPCFLoggingInternalDebugging" to a "true" or "false" value</p>
 * <p>- set the key "PlatformSpecificLoggerXml" to a valid xml format 
 * configuration string value for the platform specific logger.
 * </p>
 * <p>
 * Internal Debugging Output:  If internal SPC-F Logging component 
 * debugging is enabled, the output for informational messages will be output to 
 * standard out and any exceptions will be output to standard error.
 * No exception will be swallowed during internal debugging but it should be a rare
 * case when an exception is thrown during internal debugging. One known case when 
 * an exception is thrown during internal debugging is if one or more required 
 * jars/assemblies are missing.
 * </p>
 * <p>
 * Get Logger instance:  Get an instance of the specified logger.  
 * If a name is supplied, the named logger is returned.  
 * If no name or null is specified, an instance of the root logger is returned.
 * </p>
 * <p>
 * Logger Repository Threshold:  The logger repository threshold limits 
 * the messages that are logged across the whole repository, 
 * regardless of the logger that the message is logged to. 
 * The default value is ALL. 
 * </p>
 * <p></p>
 * For more information go to: 
 * <p>
 * <a href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Logging">SPC-F Portable Logger Users Guide</a>
 * </p>
 */
@Slf4j
public abstract class SpcfLogManager implements ISpcfThreadUncaughtExceptionHandler
{
	public static final String NOP_LOGGER_INVOKE_MESSAGE = "NOPLogger invoked";
	/**
	 * Singleton instance of SpcfLogManager
     * Do not access this variable directly.  Instead access through
     * the getLogMgrInstance method in java and Instance property in dotnet.
	 */
	private volatile static SpcfLogManager sLogMgr = null;

	/**
	 * To synchronize creation of singleton instance object
	 */
	private static final String SyncObj = "SpcfLogManager-Sync-Object";
	
	/**
	 * Flag to denote if internal SPC-F Logging component debug
	 * will be written to std out and std error.  
	 * The default value is false.
	 */
	private static boolean sInternalDebugging = false;
	
	/**
	 * Protected so SpcfLogManagerImpl can be derived from SpcfLogManager
	 * and live in a different package
	 */
	protected SpcfLogManager() 
	{
		// This method implementation is empty on purpose.
	}

	/**
	 * To Get singleton platform specific instance of this class. 
	 * Use this method rather than accessing the sLogMgr variable directly.
	 * @return a SpcfLogManager instance
	 */
	private static SpcfLogManager getLogMgrInstance() {
		if (sLogMgr == null) {
			synchronized (SyncObj) // declare the method synchronized does not tranlate correctly
			{
				if (sLogMgr == null) {
					sLogMgr = new com.intuit.spc.foundations.primarySpecific.logging.SpcfLogManagerImpl();

					//add instance as default handler for uncaught thread exceptions
					SpcfThread.initializeUncaughtExceptionHandler(sLogMgr);
				}
			}
		}
		return sLogMgr;
	}
    
    /**
     * This method allows SpcfLogManager to register itself as the default for 
     * handling uncaught exceptions within a thread.  It should not be called directly.
     * @param thread SpcfThread that threw the exception. 
	 * @param exception Throwable/Exception that was thrown.
     */
    public void handleException(SpcfThread thread, Throwable exception)
    {
    	SpcfLogger logger = getLogger();
    	if (logger != null)
    	{
		   logger.error(thread, exception);
    	}
    } 

    /**
     * To Set singleton platform specific instance of this class. 
     * Use this method rather than accessing the sLogMgr variable directly.
     * @param instance to be set
     */
    public static void setInstance(SpcfLogManager instance)
    {
    	synchronized (SyncObj)
    	{
    		sLogMgr = instance;
    	}
    }
    
	/**
	 * Get an instance of the root logger.  
	 * @return an instance of the SpcfLogger
	 */
	public static SpcfLogger getLogger() 
	{
		String s = null; 
		return getLogger(s);
	}

	/**
	 * Get an instance of the logger associated with the specified name.  
	 * If loggerName is null, the root logger will be returned.
	 * @param loggerName the name of the logger to use when logging
	 * @return an instance of the SpcfLogger
	 */
	public static SpcfLogger getLogger(String loggerName) 
	{
		SpcfLogger returnLogger = null;
		try 
		{
			returnLogger = getLogMgrInstance().doGetLogger(loggerName);
		} 
		catch (Exception e) 
		{
    		internalLogError("Exception thrown getting platform specific instance of SpcfLogger: ", e);
		}

		try
		{
			if (returnLogger == null)
			{
				System.out.println(NOP_LOGGER_INVOKE_MESSAGE);
				returnLogger = getNOPLogger();
			}
		}
		catch (Exception e)
		{
    		internalLogError("Exception thrown getting null version of SpcfLogger: ", e);
		}

		return returnLogger;
	}

	private static SpcfLogger getNOPLogger() {
		return getLogMgrInstance().getNOPLoggerImpl();
	}

	/**
	 * Get an instance of the logger associated with the name of the specified class.  
	 * If loggerName is null, the root logger will be returned.
	 * @param loggerClass the class of the logger whose name will be used to when logging
	 * @return an instance of the SpcfLogger
	 */
    @SuppressWarnings("unchecked")
	public static SpcfLogger getLogger(Class loggerClass) 
	{
		// If no logger class was passed in, then request the root logger:
		if (loggerClass == null) return getLogger();
		
		// Get the full name of the class:
		String name = SpcfClass.getFullName(loggerClass);
		
        // On Java and .Net, to keep packages and inner classes disambiguated,
        // Java separates class names with '$' and .Net uses '+'. We have to
        // correct this or an inner class with no specified logger will log
        // to its parent's parent's logger rather than it's parent's logger.
        // This would be an unexpected result.
        if (SpcfSystem.isJavaPlatform())
        {
            name = name.replace('$', '.');
        }
        else
        {
            name = name.replace('+', '.');
        }
		
		// Using the string name of the class, return the logger:
		return getLogger(name);
	}

	/**
	 * Instance implementation called from getLogger(String). 
	 * @param loggerName the name of the logger to use when logging
	 * @return an instance of the SpcfLogger
	 */
	protected abstract SpcfLogger doGetLogger(String loggerName);

	/**
	 * Write string to std error if log debug flag enabled.
	 * 
	 * This method will not swallow any exception but that should be rather a rare case when this 
	 * method throws an exception. One known case is - if one or more jars/assemblies 
	 * (e.g. portabilitySpecific.jar/PortabilitySpecific.dll) required by Logger at runtime are missing.
	 * @param logErrorStr string to write to standard error
	 * @param e an exception caught in the Logging component
	 */
	public static void internalLogError(String logErrorStr, Throwable e) {
		if (sInternalDebugging) {
			log.error(logErrorStr, e);
		}
	}

	/**
     * Gets the context for the specified key.
     *
     * @param key Key for which to get the context. The key cannot be null.
     *
     * @return Context specified by the key, or <code>null</code> if none
     * exists or an error occurs.
     */
    public static String getContext(String key)
    {
    	try
		{
    		if (key == null)
    		{
        		SpcfLogManager.internalLogError("Null key parameter not allowed in SpcfLoggerContext.getContext.", null);
        		return null;
    		}
    		return MDC.get(key);
		}
    	catch (Exception e)
		{
    		SpcfLogManager.internalLogError("Exception thrown in SpcfLoggerContext.getContext ", e);
    		return null;
		}
    }
    
   /** 
     * Puts a specified context value into the current thread's context
     * map, using the specified key to identify it. If the current thread
     * does not have a context map, this method creates one as a side
     * effect.  If a value is already defined for the key specified then the value 
     * will be replaced. 
     *
     * @param key Key with which to identify the context value in the
     * current thread's context map. The key cannot be null.
     *
     * @param val Context value to put into the current thread's context map.
     * The val cannot be null.
     */
    public static void putContext(String key, String val) 
    {
    	try
		{
    		if ((key == null) || (val == null))
    		{
        		SpcfLogManager.internalLogError("Null key or val parameter not allowed in SpcfLoggerContext.putContext.", null);
        		return;
    		}
			MDC.put(key, val);
		}
    	catch (Exception e)
		{
    		SpcfLogManager.internalLogError("Exception thrown in SpcfLoggerContext.put ", e);
		}
    }

    /**
     * Removes the context identified by a specified key from the current
     * thread's context map. 
     *
     *  @param key Key for the context to be removed which cannot be null.
     */
    public static void removeContext(String key) 
    {
    	try
		{
    		if (key == null)
    		{
        		SpcfLogManager.internalLogError("Null key parameter not allowed in SpcfLoggerContext.removeContext.", null);
        		return;
    		}
			MDC.remove(key);
		}
    	catch (Exception e)
		{
    		SpcfLogManager.internalLogError("Exception thrown in SpcfLoggerContext.remove ", e);
		}
    }

	protected abstract SpcfLogger getNOPLoggerImpl();
}