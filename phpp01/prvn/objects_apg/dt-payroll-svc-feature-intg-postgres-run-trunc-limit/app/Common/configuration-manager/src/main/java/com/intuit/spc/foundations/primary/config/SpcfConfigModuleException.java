package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfArchitectureException;
import com.intuit.spc.foundations.portability.SpcfArchitectureModuleEnum;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * The top level exception class for CMS.
 */

public abstract class SpcfConfigModuleException extends
        SpcfArchitectureException
{    
	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = 5485612187754436427L;

	/**
     * Constructs an exception with a message.
     * @param message The detailed error message
     */
    public SpcfConfigModuleException(String message)
    {
        super(message);
    }

    /**
     * Constructs an exception with a message and chained exception.
     * @param message The detailed error message
     * @param cause The exception that caused this exception
     */
    public SpcfConfigModuleException(String message, Throwable cause)
    {
        super(message, cause);
    }   

    /**
     * Your SpcfXXXException must add a unique entry to the ConfigErrorEnum and return it
     * when it overrides this method. This is how we ensure our exceptions are using ids
     * unique to the module in a type safe manner.
     * @return Configuration Error Enum
     */
    abstract SpcfConfigErrorEnum getSpcfConfigError();

    /**
     * Override the generic, error prone int, to call the typesafe LoggingError property
     * we're going to make everyone implement
     * 
     * @return The error id
     */
    @Override
    protected int getErrorIdForDefiningType()
    {
        return getSpcfConfigError().getId();
    }

    /**
     * We have to implement this method in order to uniquely identify this module within
     * the architecture in a type safe manner
     * @return the enum that represents the CMS.
     */
    @Override
    protected SpcfArchitectureModuleEnum getSpcfArchitectureModule()
    {
        return SpcfArchitectureModuleEnum.Config;
    }
}