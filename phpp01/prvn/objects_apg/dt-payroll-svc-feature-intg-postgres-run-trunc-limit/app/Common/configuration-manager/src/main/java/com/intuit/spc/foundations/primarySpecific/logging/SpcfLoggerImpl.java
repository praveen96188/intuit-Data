package com.intuit.spc.foundations.primarySpecific.logging;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.lang.reflect.Array;

/**
 * The java platform specific implementation of SpcfLogger.
 */
public class SpcfLoggerImpl extends SpcfLogger 
{
	/**
	 * Hold the platform specific instance of the logger.
	 */
	private Logger mSpecificLogger;

	
	/**
	 * The private default constructor.  
	 * This default constructor will encapsulate the root logger.
	 */
	private SpcfLoggerImpl() 
	{
		this(null);
	}

	/**
	 * The protected constructor receiving the logger to encapsulate
	 * @param logger the platform specific logger to encapsulate.
	 */
	protected SpcfLoggerImpl(Logger logger)
	{
		mSpecificLogger = logger;
	}

	/**
	 * @see com.intuit.spc.foundations.primary.logging.SpcfLogger#log(SpcfLevel, Object, Throwable)
	 */
	public void log(SpcfLevel level, Object obj, Throwable t) {
		try {

			if (!isEnabledFor(level)) {
				return;
			}

			if (obj == null) {
				if (t != null) {
					mSpecificLogger.error(StringUtils.EMPTY, t);
				}

			} else if (level == SpcfLevel.Info) {
				mSpecificLogger.info(obj.toString(), t);

			} else if (level == SpcfLevel.Warn) {
				mSpecificLogger.warn(obj.toString(), t);

			} else if (level == SpcfLevel.Debug) {
				mSpecificLogger.debug(obj.toString(), t);

			} else if (level == SpcfLevel.Error) {
				mSpecificLogger.error(obj.toString(), t);

			} else if (level == SpcfLevel.Fatal) {
				mSpecificLogger.error(obj.toString(), t);

			} else if (level == SpcfLevel.Trace) {
				mSpecificLogger.trace(obj.toString(), t);

			} else if (level == null) {
				SpcfLogManagerImpl.internalLogError("Null level parameter not allowed in SpcfLogger.log.", t);

			} else {
				SpcfLogManagerImpl.internalLogError("Unknown LogLevel=" + level, t);

			}
		} catch (Exception e) {
			SpcfLogManagerImpl.internalLogError("Exception thrown logging: ", e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.primary.logging.SpcfLogger#isEnabledFor(SpcfLevel)
	 */
	protected boolean isEnabledFor(SpcfLevel level) {
		try {
			if (level == null) {
				SpcfLogManagerImpl.internalLogError("Null level parameter not allowed in SpcfLogger.isEnabledFor.", null);
				return false;
			}
			if (level == SpcfLevel.Info) {
				return mSpecificLogger.isInfoEnabled();

			} else if (level == SpcfLevel.Warn) {
				return mSpecificLogger.isWarnEnabled();

			} else if (level == SpcfLevel.Debug) {
				return mSpecificLogger.isDebugEnabled();

			} else if (level == SpcfLevel.Error) {
				return mSpecificLogger.isErrorEnabled();

			} else if (level == SpcfLevel.Fatal) {
				return mSpecificLogger.isErrorEnabled();

			} else if (level == SpcfLevel.Trace) {
				return mSpecificLogger.isTraceEnabled();

			} else {
				return false;
			}
		} catch (Exception e) {
			SpcfLogManagerImpl.internalLogError("Exception thrown determining if enabled for level: " + level.toString(), e);
			return false;
		}
	}
}

