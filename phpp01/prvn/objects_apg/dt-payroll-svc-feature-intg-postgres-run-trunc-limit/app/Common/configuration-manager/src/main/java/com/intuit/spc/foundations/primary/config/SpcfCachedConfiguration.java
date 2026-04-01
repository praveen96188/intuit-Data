package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.util.SpcfConfiguration;

/**
 * This is a wrapper for the configuration and an internal class to be used by CMS. 
 * CMS loads the configuration into cache for better performance. CMS creates this wrapper 
 * around the configuration and loads the wrapper into the cache instead of the actual
 * configuration. This is useful as the wrapper may have other informations regarding the
 * configuration which is wrapped (like  Configuration Provider).
 * 
 */
strictfp class SpcfCachedConfiguration extends SpcfConfiguration
{    
	/**
	 * Configuration Instance
	 */
    ISpcfConfiguration mConfiguration = null;
 
    /**
     * Constructor
     * @param configuration Configuration
     */
    SpcfCachedConfiguration(ISpcfConfiguration configuration)
    {
        this.mConfiguration = configuration;
    }

    /**
     * To get the Configuration
     * @return Configuration
     */
    ISpcfConfiguration getConfiguration()
    {
        return this.mConfiguration;
    }
    
    /**
     * To set the Configuration
     * @param configuration Configuration
     */
    void setConfiguration(ISpcfConfiguration configuration)
    {
        this.mConfiguration = configuration;
    }    

    /**
     * To get the Configuration Provider
     * @return Configuration Provider
     */
    ISpcfConfigurationProvider getConfigurationProvider()
    {
        return mConfigurationProvider;
    }
        
    /**
     * To set Configuration Provider
     * @param configurationProvider Configuration Provider
     */
    void setConfigurationProvider(ISpcfConfigurationProvider configurationProvider)
    {
        this.mConfigurationProvider = configurationProvider;
    }
    
    /**
     * Provider for the configuration
     */
    private ISpcfConfigurationProvider mConfigurationProvider = null;
    
    /**
     * To convert to Immutable Configuration
     * @return Immutable Configuration
     */
    @Override
    public ISpcfImmutableConfiguration toImmutable() {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.toImmutable();
    }

    /**
     * To set the Configuration Entry
     * @param key Configuration Key
     * @param configValue Configuration Value
     */
    public void setEntry(String key, Object configValue) {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            mConfiguration.setEntry(key, configValue);
    }    
    
    /**
     * To get the Copy of the configuration
     * @return Configuration Copy
     */
    public ISpcfConfiguration getCopy() {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.getCopy();
    }

    /**
     * To get Configuration Keys
     * @return Iterator of Configuration Keys
     */
    public ISpcfIterator<String> getKeys() {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.getKeys();
    }
    
    /**
     * To get Configuration Entries
     * @return Map of Configuration Entries
     */
    public SpcfMap<String, Object> getConfigurationEntries() {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.getConfigurationEntries();
    }
    
    /**
     * To get the count of configuration entries in the configuration
     * @return configuration entry count
     */
    @Override
    public int getCount() {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.getCount(); 
    }

    /**
     * To get the subset of the configuration
     * @param prefix Prefix of the Configuration Entry Key to filter
     * @return Immutable Subset of Configuration
     */
    @Override
    public ISpcfImmutableConfiguration subset(String prefix) {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.subset(prefix);
    }

    /**
     * To find whether the configuration is empty or not
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.isEmpty();
    }
    
    /**
     * To find whether the given configuration entry key exists or not
     * @param key Configuration Entry Key
     * @return true if the configuration contains the given key, false otherwise
     */
    public boolean containsKey(String key) {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.containsKey(key);
    }       

    /**
     * To get the Configuration Entry.
     * @param key Configuration Entry Key
     * @return Configuration Entry Object
     */
    public Object getEntry(String key) {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.getEntry(key);
    }
    
    /**
     * To get the Configuration Entry.
     * @param key Configuration Entry Key
     * @param defaultValue Default value to be returned if the entry does not exist
     * @return Configuration Entry Object
     */
    @Override
    public Object getEntry(String key, Object defaultValue) {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.getEntry(key, defaultValue);
    }
    
    /**
     * To get module ID
     * @return module Id
     */
    @Override
    public String getModuleID() {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            return mConfiguration.getModuleID();
    }

    /**
     * To set module ID
     * @param moduleID module Id
     */
    @Override
    public void setModuleID(String moduleID) {
        if(this.mConfiguration == null)
            throw new SpcfInvalidOperationException("Configuration wrapped is null. Cannot perform the operation");
        else            
            mConfiguration.setModuleID(moduleID);
    }
}