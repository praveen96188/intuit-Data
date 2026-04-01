package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception indicating that a configuration entry is invalid.
 */
public class SpcfConfigInvalidException extends SpcfConfigModuleException
{
    /**
     * Serial Version ID
     */
    private static final long serialVersionUID = -8711595295793464937L;

    /**
     * ID
     */
    protected String mId = "<NA>";

    /**
     * Configuration Value
     */
    protected String mConfigValue = "<NA>";

    /**
     * Constructs an instance with the id of the invalid configuration entry.
     * 
     * @param id The id of the invalid configuration entry.
     * @param configValue The value of the invalid configuration entry.
     */
    public SpcfConfigInvalidException(String id, String configValue)
    {
        this(id, configValue, "The configuration entry is invalid: id=" + id
                + ", value=" + configValue);
    }

    /**
     * Constructs an instance with the id of the invalid configuration entry and a
     * detailed error message.
     * 
     * @param id The id of the invalid configuration entry.
     * @param configValue The value of the invalid configuration entry.
     * @param message The detailed error message.
     */
    public SpcfConfigInvalidException(String id, String configValue,
            String message)
    {
        super(message + " id=" + id + ", value=" + configValue);
        this.mId = id;
        this.mConfigValue = configValue;
    }

    /**
     * Constructs an instance with the id of the invalid configuration entry.
     * 
     * @param id The id of the invalid configuration entry.
     * @param configValue The value of the invalid configuration entry.
     * @param cause The exception that caused this exception.
     */
    public SpcfConfigInvalidException(String id, String configValue,
            Throwable cause)
    {
        super("The configuration entry is invalid: id=" + id + ", value="
                + configValue, cause);
        this.mId = id;
        this.mConfigValue = configValue;
    }

    /**
     * Constructs an instance with a detailed error message and a cause
     * 
     * @param message detailed error message
     * @param cause The exception that caused this exception.
     */
    public SpcfConfigInvalidException(String message, Throwable cause)
    {
        super(message, cause);        
    }
    
    /**
     * Constructs an instance with a detailed error message
     * 
     * @param message detailed error message
     */
    public SpcfConfigInvalidException(String message)
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
        return SpcfConfigErrorEnum.InvalidConfig;
    }

    /**
     * @return Returns the id of the invalid configuration entry.
     */
    public String getId()
    {
        return this.mId;
    }

    /**
     * @return the value of the invalid configuration entry.
     */
    public String getConfigValue()
    {
        return this.mConfigValue;
    }
}