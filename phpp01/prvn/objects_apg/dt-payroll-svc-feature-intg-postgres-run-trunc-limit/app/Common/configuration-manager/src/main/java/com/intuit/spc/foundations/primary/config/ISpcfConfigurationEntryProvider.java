package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;

/**
 * <p>
 * An interface for supporting lazy load/save of configurations. This will be useful for creating 
 * thin proxy configurations, a configuration which does not have any data of its own but just 
 * acts as a proxy to re-direct any calls to Get/Set Configuration Entry to this configuration provider.
 * </p>
 * 
 * <p>
 * <b>Lazy Load:</b>
 * A lazy configuration is a thin configuration which has no data in it but always goes to
 * the Configuration Entry Provider to get or set a configuration entry. This could be really
 * useful in some situations like a huge configuration sitting in a database. You may not want
 * to load/store the whole configuration in a single shot rather you may want to do it one at a time.
 * </p> 
 * 
 * <p>
 * Any configuration provider which has to support this lazy load/save shall implement this interface in addition
 * to <b>ISpcfConfigurationProvider</b>
 * 
 * For more info go to:
 * <a
 * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Implementing_Lazy_Load.2FSave">SPCF
 * Configuration Management Lazy Load/Save Users Guide </a>
 *  
 * </p>
 * 
 * @see com.intuit.spc.foundations.primary.config.util.SpcfThinProxyConfiguration
 * @see ISpcfConfigurationProvider
 */
public interface ISpcfConfigurationEntryProvider
{    
    /**
     * Sets an entry in the configuration for the key
     * @param key Configuration Entry Key
     * @param configValue Configuration Entry Value
     */
    void setEntry(String key, Object configValue);
    
    /**
     * Gets an entry from the configuration for the key
     * @param key Configuration Entry Key
     * @return Configuration Entry Value
     */
    Object getEntry(String key);
 
    /**
     * Gets whether the configuration is empty or not
     * @return True if empty, false otherwise
     */
    boolean isEmpty();
    
    /**
     * Finds whether the specified key exists in the configuration or not
     * @param configKey Configuration Entry Key
     * @return True if found, false otherwise
     */
    boolean containsKey(String configKey);
    
    /**
     * Gets the keys in the configuration
     * @return Keys Configuration Entry Keys
     */
    ISpcfIterator<String> getKeys();
    
    /**
     * Gets an iterable map of configuration entries contained in the configuration.
     * 
     * @return An iterable map of configuration entries contained in the configuration.
     */
    SpcfMap<String, Object> getConfigurationEntries();
    
    /** 
     * Gets a copy of the configuration. Changing the copy does not affect the real configuration
     * object.  
     * @return Copy of the configuration
     */
    ISpcfConfiguration getCopy();
 }
