package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception used by configuration providers to indicate that configurations 
 * could not be loaded.  
 * 
 * Configuration providers should provide more specific sub-classes
 * for errors specific to the provider. 
 */
public class SpcfProviderLoadException extends SpcfConfigProviderException
{       

	/**
	 * Serial Version ID
	 */
    private static final long serialVersionUID = 7040626316455784239L;

    /**
     * Constructs an instance with a detailed error message
     * @param message The detailed error message
     */
    public SpcfProviderLoadException(String message)
    {
        super(message);
    } 
    
    /**
     * Constructs an instance with a detailed error message and a cause
     * @param message The detailed error message
     * @param cause The exception that caused this exception
     */
    public SpcfProviderLoadException(String message, Throwable cause)
    {
        super(message, cause);
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
        return SpcfConfigErrorEnum.LoadConfigError;
    }
}
