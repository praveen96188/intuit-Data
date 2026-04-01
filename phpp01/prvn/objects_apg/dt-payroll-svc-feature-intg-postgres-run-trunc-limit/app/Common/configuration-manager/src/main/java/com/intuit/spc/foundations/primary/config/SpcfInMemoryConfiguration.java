package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.util.SpcfConfiguration;

/**
 * A configuration instance that stores its configurations in memory. This class is not thread safe.
 */
public final class SpcfInMemoryConfiguration extends SpcfConfiguration
{
	/**
	 * Factory Instance
	 */
    private static final SpcfFactory Factory = SpcfFactory.getInstance();
    
    /**
     * Configuration Cache Instance
     */
    private SpcfMap<String, Object> mConfigCache = null;

    /**
     * Constructs an instance using a unsynchronized map as the in memory storage.
     */
    public SpcfInMemoryConfiguration()
    {        
        mConfigCache = Factory.<String, Object>createHashMap();
    }

    /**
     * A copy constructor that constructs an instance using an immutable configuration instance. The resulting object is
     * a configuration object with all entries in the the immutable configuration object. Note that the clone is only
     * one level deep. Configuration keys and values are not cloned.
     * <p>
     * This constructor also uses a unsynchronized map to store the configurations.
     * 
     * @param config An immutable instance.
     * @throws SpcfArgumentNullException if config is null
     */
    public SpcfInMemoryConfiguration(ISpcfImmutableConfiguration config)
    {
        this();
        SpcfParamValidator.checkIsNotNull(config, "config");
        for (ISpcfIterator<String> it = config.getKeys(); it.hasNext();)
        {
            String key = it.next();
            this.setEntry(key, config.getEntry(key));
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
     * Set the configuration entry associated with the key to the specified configValue if key exists; otherwise, a new
     * configuration entry is added with the specified key and value.
     *
     * @param configKey The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setEntry(String configKey, Object configValue)
    {
        SpcfParamValidator.checkIsNotNull(configKey, "configKey");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        this.mConfigCache.add(configKey, configValue);
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
    
    /** 
     * Gets a copy of the configuration. Changing the copy does not affect the real configuration
     * object.  
     * @return Copy of the configuration
     */  
    public ISpcfConfiguration getCopy()
    {
        ISpcfConfiguration inMemoryCopy = new SpcfInMemoryConfiguration(this.toImmutable());
        return inMemoryCopy;
    }   
}
