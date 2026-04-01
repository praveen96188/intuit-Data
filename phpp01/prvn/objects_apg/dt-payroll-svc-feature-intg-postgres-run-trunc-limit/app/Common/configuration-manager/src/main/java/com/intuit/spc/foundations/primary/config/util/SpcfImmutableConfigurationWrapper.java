package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigEntryMissingException;

/**
 * A class that converts an ISpcfConfiguration instance into an immutable instance.
 */
public final strictfp class SpcfImmutableConfigurationWrapper extends SpcfImmutableConfiguration implements ISpcfImmutableConfiguration    
{
	/**
	 * Factory Instance
	 */	
    private static SpcfFactory sFactory = SpcfFactory.getInstance();

    /**
     * Configuration Cache Instance
     */
    private SpcfMap<String, Object> mConfigCache = null;                   

    /**
     * Used to create an empty immutable configuration object
     */
    SpcfImmutableConfigurationWrapper()
    {        
        mConfigCache = sFactory.<String, Object>createHashMap();
    }

    /**
     * A copy constructor that constructs an instance using a configuration instance. The resulting object is an
     * immutable configuration object with all entries in config. Note that the clone is only one level deep.
     * Configuration keys and values are not cloned.
     * <p>
     * 
     * @param config An immutable instance.
     * @throws SpcfArgumentNullException if config is null
     */
    public SpcfImmutableConfigurationWrapper(ISpcfConfiguration config)
    {
        this();
        SpcfParamValidator.checkIsNotNull(config, "config");
        for (ISpcfIterator<String> it = config.getKeys(); it.hasNext();)
        {        	
            String key = it.next();
            this.mConfigCache.add(key, config.getEntry(key));
        }
    }

    /**
     * Check if the configuration is empty.
     * 
     * @return <code>true</code> if the configuration contains no entries, <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return this.mConfigCache.getSize() == 0;
    }

    /**
     * Gets an entry associated with the key from the configuration.
     * 
     * @param configKey the key of the configuration entry to retrieve
     * @return the value to which this key maps.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null.
     */
    public Object getEntry(String configKey)
    {
        SpcfParamValidator.checkIsNotNull(configKey, "configKey");
        Object obj = this.mConfigCache.getItem(configKey);
        if (obj == null)
        {
            throw new SpcfConfigEntryMissingException(configKey);
        }
        return obj;
    }

    /**
     * Check if the configuration contains the specified key.
     * 
     * @param configKey the key whose presence in this configuration is to be tested
     * @return <code>true</code> if the configuration contains an entry for this key, <code>false</code> otherwise
     * @throws SpcfArgumentNullException if key is null
     */
    public boolean containsKey(String configKey)
    {
        SpcfParamValidator.checkIsNotNull(configKey, "configKey");
        return this.mConfigCache.containsKey(configKey);
    }

    /**
     * Get a iterator of the keys contained in the configuration.
     * 
     * @return A iterator of the keys contained in the configuration.
     */
    public ISpcfIterator<String> getKeys()
    {
        return this.mConfigCache.getKeyList().getIterator();
    }
    
    /**
     * Gets an iterable map of the configuration entries contained in the configuration.
     * 
     * @return An iterable map of configuration entries contained in the configuration.
     */
    public SpcfMap<String, Object> getConfigurationEntries()
    {
        return this.mConfigCache;
    }
}
