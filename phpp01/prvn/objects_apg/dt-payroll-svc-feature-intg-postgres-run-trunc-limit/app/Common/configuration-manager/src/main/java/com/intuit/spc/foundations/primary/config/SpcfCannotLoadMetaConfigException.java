package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * This exception indicates that the meta configuration XML can not be loaded. 
 * @author yzhang [Created on Aug 9, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primary/config/SpcfCannotLoadMetaConfigException.java#1 $ 
 */
public class SpcfCannotLoadMetaConfigException extends SpcfConfigInitException
{   
    /**
     * 
     */
    private static final long serialVersionUID = 2996596242845698679L;

    /**
     * @param message
     * @param rootCause
     */
    public SpcfCannotLoadMetaConfigException(String message, Exception rootCause)
    {
        super(message, rootCause);
    }

    /**
     * @param message
     */
    public SpcfCannotLoadMetaConfigException(String message)
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
        return SpcfConfigErrorEnum.CannotLoadConfigError;
    }
}