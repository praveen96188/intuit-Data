package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception used by configuration providers to indicate that configurations 
 * could not be stored/updated.  
 * 
 * Configuration providers should provide more specific sub-classes
 * for errors specific to the provider. 
 */
public class SpcfProviderUpdateException extends SpcfConfigProviderException
{   
	/**
	 * Serial Verion ID
	 */
    private static final long serialVersionUID = -4574377450227410930L;

    /**
     * Constructs an instance with a detailed error message
     * @param message The detailed error message
     */
    public SpcfProviderUpdateException(String message)
    {
        super(message);
    }  
    
    /**
     * Constructs an instance with a detailed error message and a cause
     * @param message The detailed error message
     * @param cause The exception that caused this exception
     */
    public SpcfProviderUpdateException(String message, Throwable cause)
    {
        super(message, cause);
    }  
    
    /**
     * Your SpcfXXXException must add a unique entry to the ConfigErrorEnum and return it
     * when it overrides this method. This is how we ensure our exceptions are using ids
     * unique to the module in a type safe manner.
     * @return Configuraiton Error Enum
     */
    @Override
    SpcfConfigErrorEnum getSpcfConfigError()
    {
        return SpcfConfigErrorEnum.UpdateConfigError;
    }
}
