package com.intuit.spc.foundations.primarySpecific.logging;

import com.intuit.spc.foundations.primary.config.SpcfConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.collections.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.slf4j.helpers.NOPLogger;

/**
 * The java platform specific implementation of SpcfLogManager.
 */
public class SpcfLogManagerImpl extends SpcfLogManager
{
	
    /**
     * The module ID used in the SPC-F Configuration Manager
     * config file.
     */
    private static final String ModuleId = "SpcfLogger-2";

    /**
     * To store loggers which are part of configuration
     */
    private static SpcfHashMap<String, SpcfLogger> sLoggerHash = SpcfFactory.getInstance().createHashMap();
    
    /**
     * To store a cached copy of the root logger.
     */
    private static SpcfLogger sRootLogger = null;

    /**
     * Default constructor.
     */
	public SpcfLogManagerImpl() {
		// no-op
	}

    /**
     * Retrieves a logger by name. 
     *
     * @param name Name of the logger. If null, the root logger is returned.
     *
     * @return an SpcfLogger instance.
     */
    @Override
	protected SpcfLogger doGetLogger(String name) {
		// If the name is null, retrieve, cache, and return the root logger:
		if (name == null) {
			if (sRootLogger == null) {
				sRootLogger = new SpcfLoggerImpl(LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME));
				sRootLogger.info("sRootLogger instantiated");
			}
			return sRootLogger;
		}

		// If we have it in our logger cache, return it:
		SpcfLogger logger = sLoggerHash.getItem(name);
		if (logger != null) return logger;
    	
		// Retrieve, cache, and return the logger:
		logger = new SpcfLoggerImpl(LoggerFactory.getLogger(name));
		sLoggerHash.add(name, logger);
		return logger;
    }

	@Override
	protected SpcfLogger getNOPLoggerImpl() {
		return new SpcfLoggerImpl(NOPLogger.NOP_LOGGER);
	}

	/**
	 * Configure the SPC-F Logging component using the instance of SpcfConfigurationManager.
	 *
	 * @param configManager a SpcfConfigurationManager instance
	 */
	public static void setConfiguration(SpcfConfigurationManager configManager) {

		String log = "Logger Configuration File, ModuleId=" + ModuleId + ", status=";
		try {
			System.out.println(log + "start");
			String sourceFileName = configManager.getSourceFilename(ModuleId);
			if (!StringUtils.isEmpty(sourceFileName)) {
				org.apache.logging.log4j.core.config.Configurator.initialize(null, sourceFileName);
				System.out.println(log + "complete, fileName=" + sourceFileName);
			} else {
				throw new IllegalStateException(log + "error, fileName=EMPTY");
			}
		} catch (Exception e) {
			throw new IllegalStateException(log + "error, exception=" + e.getMessage(), e);
		}
	}

	/**
     * @see com.intuit.spc.foundations.primary.logging.SpcfLogManager#internalLogError(String, Throwable)
	 * @param logErrorStr string to write to standard error
	 * @param e an exception caught in the Logging component
	 */
    public static void internalLogError(String logErrorStr, Throwable e)
    {
    	// call the method on the base class
    	SpcfLogManager.internalLogError(logErrorStr, e);
    }
}
