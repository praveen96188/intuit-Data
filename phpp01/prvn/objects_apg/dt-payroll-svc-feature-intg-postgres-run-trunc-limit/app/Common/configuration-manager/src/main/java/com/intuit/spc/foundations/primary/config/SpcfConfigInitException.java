package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception used to indicate errors during CMS initialization.
 */
public class SpcfConfigInitException extends SpcfConfigModuleException
{    
	/**
	 * Serial Version ID
	 */
    private static final long serialVersionUID = 3659894102652672903L;

    /**
     * constructs a SpcfConfigInitException instance with detailed error message and root
     * cause
     * 
     * @param message The detailed error message
     * @param rootCause The root cause of the initializtion exception
     */
    public SpcfConfigInitException(String message, Exception rootCause)
    {
        super(message, rootCause);
    }

    /**
     * constructs a SpcfConfigInitException instance with detailed error message
     * 
     * @param message The detailed error message
     */
    public SpcfConfigInitException(String message)
    {
        super(message);
    }

    /**
     * Your SpcfXXXException must add a unique entry to the ConfigErrorEnum and return it
     * when it overrides this method. This is how we ensure our exceptions are using ids
     * unique to the module in a type safe manner.
     * @return Configuration Error Enum
     */
    @Override
    SpcfConfigErrorEnum getSpcfConfigError()
    {
        return SpcfConfigErrorEnum.Initialization;
    }
}
