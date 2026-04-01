package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfStringBuilder;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * An exception thrown to indicate that the configuration entry can not be converted to
 * the type requested.
 */
public class SpcfConfigConversionException extends SpcfConfigInvalidException
{

	/**
	 * Serial Version ID
	 */
    private static final long serialVersionUID = 2625059719708205783L;
    
    /**
     * Target Type
     */
    protected String mTargetType = null;

    /**
     * Constructs a SpcfConfigConversionException instance to indicate that the
     * configuration entry can not be converted to the target type requested.
     * 
     * @param configKey The key of the configuration entry that caused this exception
     * @param configValue The value of the configuration entry that caused this exception
     * @param targetType The type requested, which is incompatible with the actual
     *            configValue.
     */
    public SpcfConfigConversionException(String configKey, String configValue,
            String targetType)
    {
        this(configKey, configValue, targetType, null);
    }

    /**
     * Constructs a SpcfConfigConversionException instance to indicate that the
     * configuration entry can not be converted to the target type requested.
     * 
     * @param configKey The key of the configuration entry that caused this exception
     * @param configValue The value of the configuration entry that caused this exception
     * @param targetType The type requested.
     * @param cause The cause of the exception.
     */
    public SpcfConfigConversionException(String configKey, String configValue,
            String targetType, Throwable cause)
    {
        super(configKey, configValue, cause);
        this.mTargetType = targetType;
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
        return SpcfConfigErrorEnum.InvalidConversion;
    }

    /**
     * To get the exception message
     * @return The detailed message string of this exception.
     */
    @Override
    public String getMessage()
    {
        SpcfStringBuilder message = SpcfFactory.getInstance().createStringBuilder();
        message.append("Unable to convert configuration value to ");
        message.append(this.mTargetType);
        message.append(": configKey=");
        message.append(super.getId());
        message.append(", configValue=");
        message.append(super.getConfigValue());
        if (super.getCause() != null && super.getCause().getMessage() != null)
        {
            message.append(", error=");
            message.append(super.getCause().getMessage());
        }
        return message.toString();
    }

    /**
     * To convert to string
     * @return a String representation of this exception instance.
     */
    @Override
    public String toString()
    {
        return getMessage();
    }

    /**
     * @return The targetType requested, which was incompatible with the actual
     *         configValue
     */
    public String getTargetType()
    {
        return this.mTargetType;
    }
}