package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfInMemoryConfiguration;

/**
 * This class is used to get a subset of any configuration. i.e) This class is used to 
 * get a subset of configuration entries which has the key starting with specified prefix.
 */
public class SpcfSubsetConfiguration
{
    /**
     * Creates an immutable configuration representing a subset of the original configuration.
     * 
     * @param parent The original configuration to use
     * @param prefix The prefix used to create the subset.
     * @return an immutable configuration representing a subset of the original configuration. If prefix is an empty
     *         string, parent is returned.
     * @throws SpcfArgumentNullException if parent or prefix is null
     */
    public static ISpcfImmutableConfiguration getSubset(ISpcfImmutableConfiguration parent, String prefix)
    {
        SpcfParamValidator.checkIsNotNull(parent, "parent");
        SpcfParamValidator.checkIsNotNull(prefix, "prefix");
        if (parent.isEmpty())
        {
            return SpcfImmutableConfiguration.Empty;
        }
        if (prefix.length() == 0)
        {
            return parent;
        }
        if (!prefix.endsWith("."))
        {
            prefix = prefix + ".";
        }
        int prefixLength = prefix.length();
        String prefixWithNoEndingDot = prefix.substring(0, prefixLength - 1);
        SpcfInMemoryConfiguration config = null;
        for (ISpcfIterator<String> it = parent.getKeys(); it.hasNext();)
        {
            String key = it.next();
            String subKey = null;
            if (key.equals(prefixWithNoEndingDot))
            {
                subKey = "";
            }
            else if (key.startsWith(prefix))
            {
                subKey = key.substring(prefixLength);
            }
            if (subKey != null)
            {
                if (config == null)
                {
                    //since we are readonly, no need to use a syncrhonized map.
                    config = new SpcfInMemoryConfiguration();
                }
                config.setEntry(subKey, parent.getEntry(key));
            }
        }
        if (config == null)
        {
            return SpcfImmutableConfiguration.Empty;
        }
        // Just making sure people can't cast back to ISpcfConfiguration
        return config.toImmutable();
    }
}
