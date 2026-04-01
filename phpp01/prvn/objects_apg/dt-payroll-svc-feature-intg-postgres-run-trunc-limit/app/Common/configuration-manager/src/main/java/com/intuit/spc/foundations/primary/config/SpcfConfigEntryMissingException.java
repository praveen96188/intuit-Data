package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception indicating that a required configuration entry is missing.  
 */
public class SpcfConfigEntryMissingException extends SpcfConfigModuleException
{
    /**
     * Serial Version ID
     */
    private static final long serialVersionUID = -3800926994024085138L;
    
    /**
     * The id of the missing entry
     */
    protected String mMissingId;        
    
    /**
     * Constructs an instance with the id of the missing entry
     * @param missingId The id of the missing entry
     */
    public SpcfConfigEntryMissingException(String missingId)
    {
        this(missingId, "Missing configuration entry: id="+missingId);        
    }
    
    /**
     * Constructs an instance with the id of the missing entry and a detailed error message.
     * @param missingId The id of the missing entry
     * @param message detailed error message.
     */
    public SpcfConfigEntryMissingException(String missingId, String message)
    {
        super(message + " id="+missingId);
        this.mMissingId = missingId;
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
        return SpcfConfigErrorEnum.MissingConfig;
    }
        
    /**
     * @return Returns the missing configKey that caused this exception.
     */
    public String getId()
    {
        return this.mMissingId;
    }   
}
