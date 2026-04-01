package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.primary.config.util.SpcfConfigErrorEnum;

/**
 * @author yzhang [Created on Aug 12, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primary/config/SpcfProviderUpdateUnsupportedException.java#1 $ 
 */
public class SpcfProviderUpdateUnsupportedException extends SpcfProviderUpdateException
{
	/**
	 * Serial Version ID
	 */
    private static final long serialVersionUID = 5078882322863945793L;

    /**
     * @param message
     */
    public SpcfProviderUpdateUnsupportedException(String message)
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
        return SpcfConfigErrorEnum.UpdateUnsupportedError;
    }
}
