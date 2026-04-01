package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception indicating that a Meta configuration setting is invalid. 
 */
public class SpcfMetaConfigEntryInvalidException extends SpcfConfigInvalidException
{
	/**
	 * Serial Version ID
	 */
    private static final long serialVersionUID = 21818691765440854L;

    /**
     * Constructs an instance with the id of the invalid configuration entry.
     * @param message A detailed error message.
     */
    public SpcfMetaConfigEntryInvalidException(String message)
    {
        super(message);        
    }

    /**
     * Constructs an instance with the id of the invalid configuration entry.
     * @param message A detailed error message.
     * @param cause The exception that caused this exception
     */
    public SpcfMetaConfigEntryInvalidException(String message, Throwable cause)
    {
        super(message, cause);        
    }
    
    /**
     * Constructs an instance with the id of the invalid configuration entry.
     * @param id    The id of the invalid configuration entry.
     * @param configValue   The value of the invalid configuration entry.
     */
    public SpcfMetaConfigEntryInvalidException(String id, String configValue)
    {
        super(id, configValue, "A meta configuration entry is invalid.");        
    }
    
    /**
     * Constructs an instance with the id of the invalid configuration entry and a 
     * detailed error message.
     * @param id    The id of the invalid configuration entry.
     * @param configValue   The value of the invalid configuration entry.
     * @param message The detailed error message.
     */
    public SpcfMetaConfigEntryInvalidException(String id, String configValue, String message)
    {
        super(id, configValue, message);        
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
        return SpcfConfigErrorEnum.InvalidMetaConfig;
    }
}
