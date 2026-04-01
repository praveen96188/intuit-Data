package com.intuit.spc.foundations.primarySpecific.config;

import com.intuit.spc.foundations.portability.collections.SpcfMap;

/**
 * An Internal Class used by CMS. 
 * 
 * @author yzhang [Created on Sep 8, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/SpcfConfigProviderUtils.java#1 $ 
 */
public class SpcfConfigProviderUtils
{
    /**
     * Gets the boolean value of an optional property. 
     * @param properties Properties
     * @param key Key
     * @return the boolean value that key maps to; or defaultValue if key does not exist.
     */
    public static boolean getOptionalBooleanValue(SpcfMap<String, Object> properties, String key)
    {
        return getOptionalBooleanValue(properties, key, false);
    }
    
    /**
     * Gets the boolean value of an optional property. 
     * @param properties Properties
     * @param key Key
     * @param defaultValue the default value to return if key does not exist.
     * @return the boolean value that key maps to; or defaultValue if key does not exist.
     */
    public static boolean getOptionalBooleanValue(SpcfMap<String, Object> properties, String key, boolean defaultValue)
    {
        boolean returnVal = defaultValue;
        Object obj = properties.getItem(key);
        if (obj != null)
        {            
            returnVal = Boolean.valueOf(obj.toString()).booleanValue();            
        }
        return returnVal;
    }
}
