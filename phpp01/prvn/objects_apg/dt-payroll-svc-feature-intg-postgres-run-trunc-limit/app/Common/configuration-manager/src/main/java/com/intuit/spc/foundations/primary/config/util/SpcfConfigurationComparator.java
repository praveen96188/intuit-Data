package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;

/**
 * A class to compare configurations.
 * @author yzhang [Created on Aug 31, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primary/config/util/SpcfConfigurationComparator.java#1 $
 */
public class SpcfConfigurationComparator
{
	/**
	 * This is a utility class with all static methods, no need to instantiate
	 */
    private SpcfConfigurationComparator()
    {
    	//Constructor
    }

    /**
     * Compares oldConfig with newConfig and returns true if they are equal. Two configuration objects are considered
     * equal if they point to the same reference or both contain the same set of configuration keys and each key maps to
     * the same configuration value. When comparing configuration values, the equals method (Object.equals in java and
     * System.Object.Equals in C#) is used to determine whether or not the configuration values are equal.
     * 
     * @param oldConfig The old configuration object, can be null.
     * @param newConfig The new configuration object, can be null.
     * @return true if oldConfig is equal to newConfig, false otherwise.
     */
    public static boolean areEqual(ISpcfImmutableConfiguration oldConfig, ISpcfImmutableConfiguration newConfig)
    {
        if (oldConfig == newConfig)
        {
            return true;
        }
        if (oldConfig == null || newConfig == null)
        {
            return false;
        }
        if (oldConfig.getCount() != newConfig.getCount())
        {
            return false;
        }
        for (ISpcfIterator<String> it = oldConfig.getKeys(); it.hasNext();)
        {
            String key = it.next();
            if (!newConfig.containsKey(key))
            {
                return false;
            }
            Object oldValue = oldConfig.getEntry(key);
            Object newValue = newConfig.getEntry(key);
            if (oldValue != newValue && !oldValue.equals(newValue))
            {
                return false;
            }
        }
        return true;
    }    
}
