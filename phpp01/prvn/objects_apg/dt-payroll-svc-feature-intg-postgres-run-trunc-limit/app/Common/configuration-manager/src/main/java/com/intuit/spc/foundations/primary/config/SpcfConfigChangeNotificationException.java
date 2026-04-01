package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfStringBuilder;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * This exception represents one or more runtime exceptions thrown from the configuration change listeners.
 */
public class SpcfConfigChangeNotificationException extends SpcfConfigModuleException
{
	/**
	 * Serial Version ID
	 */
    private static final long serialVersionUID = 6570596071885819469L;

    /**
     * Listner Class Name
     */
    protected String mListenerClassName = "<N/A>";

    /**
     * Module ID
     */
    protected String mModuleId = "<N/A>";

    /**
     * Constructs an instance with a detailed error message and the cause
     * 
     * @param listenerClassName The name of the listener class that failed
     * @param moduleId The id of the configuration module that this listener is responsible for.
     * @param errorMsg the detailed error message
     * @param cause The error that caused this exception
     */
    public SpcfConfigChangeNotificationException(String listenerClassName, String moduleId, String errorMsg, Throwable cause)
    {
        super(errorMsg, cause);
        this.mListenerClassName = listenerClassName;
        this.mModuleId = moduleId;
    }

    /**
     * @return the name of the listener class that failed
     */
    public String getListenerClassName()
    {
        return mListenerClassName;
    }

    /**
     * @return Returns the id of the configuration module that the listener was responsible for.
     */
    public String getConfigurationModuleId()
    {
        return this.mModuleId;
    }

    /**
     * To get the exception message
     * @return The detailed message string of this exception.
     */
    @Override
    public String getMessage()
    {
        SpcfStringBuilder sb = SpcfFactory.getInstance().createStringBuilder(super.getMessage());
        sb.append(":  moduleId=");
        sb.append(this.mModuleId);
        sb.append(", listener=");        
        sb.append(this.mListenerClassName);
        if (super.getCause() != null && super.getCause().getMessage() != null)
        {
            sb.append(", cause=");
            sb.append(super.getCause().getMessage());
        }
        return sb.toString();
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
        return SpcfConfigErrorEnum.ListenerFailedError;
    }
}
