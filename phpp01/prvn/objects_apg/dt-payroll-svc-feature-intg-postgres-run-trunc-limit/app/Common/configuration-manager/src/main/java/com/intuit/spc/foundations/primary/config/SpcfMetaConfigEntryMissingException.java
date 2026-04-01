package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception indicating that a required Meta configuration setting is missing. 
 */
public class SpcfMetaConfigEntryMissingException extends SpcfConfigEntryMissingException
{
	/**
	 * Serial Version ID
	 */
    private static final long serialVersionUID = -6446335558002755228L;

    /**
     * Constructs an instance with the id of the missign meta configuration entry.
     * @param missingId The id of the missing entry.
     */
    public SpcfMetaConfigEntryMissingException(String missingId)
    {
        super(missingId, "A required entry in the meta configuration is missing.");
    }
    
    /**
     * Constructs an instance with the id of the missign meta configuration entry.
     * @param missingId The id of the missing entry.
     * @param message The detailed error message
     */
    public SpcfMetaConfigEntryMissingException(String missingId, String message)
    {
        super(missingId, message);
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
        return SpcfConfigErrorEnum.MissingMetaConfig;
    }
}
