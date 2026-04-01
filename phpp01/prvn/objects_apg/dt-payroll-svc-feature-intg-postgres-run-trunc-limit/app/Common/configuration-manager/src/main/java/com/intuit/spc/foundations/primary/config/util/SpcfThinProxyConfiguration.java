package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfConfigurationEntryProvider;
import com.intuit.spc.foundations.primary.config.ISpcfConfigurationProvider;
import com.intuit.spc.foundations.primary.config.ISpcfLazyConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.SpcfConfigurationChangeNotifier;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryInvalidException;

/**
 * This is a simple configuration which could be used in lazy load scenarios. 
 * <p>
 * Lazy Load:
 * A lazy configuration is a thin configuration which has no data in it but always goes to
 * the Configuration Entry Provider to get or set a configuration entry. This could be really
 * useful in some situations like a huge configuration sitting in a database. You may not want
 * to load/store the whole configuration in a single shot rather you may want to do it one at a time.
 * This configuration acts as a proxy and every call to Get/Set is re-directed to the
 * configuration provider which created it.
 * </p>
 * 
 * <b>
 * Make sure to implement {@link ISpcfConfigurationEntryProvider} in any of the configuration
 * provider which you want to support lazy load.
 * </b>
 * 
 * For more info go to:
 * <a
 * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Implementing_Lazy_Load.2FSave">SPCF
 * Configuration Management Lazy Load/Save Users Guide </a>
 *  
 * @see ISpcfConfigurationEntryProvider
 * @see ISpcfConfigurationProvider
 */
public final class SpcfThinProxyConfiguration extends SpcfConfiguration implements ISpcfLazyConfiguration
{   
    /**
     * Constructor
     */
    public SpcfThinProxyConfiguration()
    {        
    	//Constructor   
    }

    /**
     * Check if the configuration is empty.
     * 
     * @return <code>true</code> if the configuration contains no entries, <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return this.getConfigurationEntryProvider().isEmpty();
    }

    /**
     * Set the configuration entry associated with the key to the specified configValue if key exists; otherwise, a new
     * configuration entry is added with the specified key and value.
     *
     * @param configKey The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     * @throws SpcfMetaConfigEntryInvalidException If the configuration provider of this configuration is read-only
     */    
    public void setEntry(String configKey, Object configValue)
    {        
        if(this.getConfigurationProvider().getReadonly())
            throw new SpcfMetaConfigEntryInvalidException("The last configuration provider for module '" + this.getModuleID()
                    + "' is readonly.");
        
        SpcfParamValidator.checkIsNotNull(configKey, "configKey");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        if(this.getConfigurationEntryProvider() != null)
        {
            this.getConfigurationEntryProvider().setEntry(configKey, configValue);
            SpcfConfigurationChangeNotifier.notifyConfigurationListeners(this.getModuleID(),
                configKey, configValue);
        }
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
        Object obj = null;
        if(this.getConfigurationEntryProvider() != null)
        {
            obj = this.getConfigurationEntryProvider().getEntry(configKey);
            if (obj == null)
            {
                throw new SpcfConfigEntryMissingException(configKey);
            }
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
        if(this.getConfigurationEntryProvider() != null)
        {
            return this.getConfigurationEntryProvider().containsKey(configKey);
        }
        else
            return false;
    }

    /**
     * Get a iterator of the keys contained in the configuration.
     * 
     * @return A iterator of the keys contained in the configuration.
     */
    public ISpcfIterator<String> getKeys()
    {
        if(this.getConfigurationEntryProvider() != null)
        {
            return this.getConfigurationEntryProvider().getKeys();
        }
        else
            return null;
    }
    
    /**
     * Gets an iterable map of the configuration entries contained in the configuration.
     * 
     * @return An iterable map of configuration entries contained in the configuration.
     */
    public SpcfMap<String, Object> getConfigurationEntries()
    {
        if(this.getConfigurationEntryProvider() != null)
        {
            return this.getConfigurationEntryProvider().getConfigurationEntries();            
        }
        else
            return null;
    }
        
    /** 
     * Gets a copy of the configuration. Changing the copy does not affect the real configuration
     * object.  
     * @return Copy of the configuration
     */ 
    public ISpcfConfiguration getCopy()
    {
        if(this.getConfigurationEntryProvider() != null)
        {
            return this.getConfigurationEntryProvider().getCopy();
        }
        else
            return null;
    }
    
    /**
     * Gets the Configuration Provider which produced this configuration. Null will be returned
     * if this configuration is not created by any providers.
     * @return Configuration Provider
     */
     public ISpcfConfigurationProvider getConfigurationProvider()
     {
         return mConfigurationProvider;
     }
     
     /**
      * Sets the Configuration Provider which produced this configuration. Configuration
      * Manager will take care of setting the provider for a configuration while 
      * loading the configuration from the Configuration Provider. So, may not be used 
      * any where else.
      * @param configurationProvider Configuration Provider
      */
     public void setConfigurationProvider(ISpcfConfigurationProvider configurationProvider)
     {
         this.mConfigurationProvider = configurationProvider;
     }
     
     /**
      * Provider for the configuration 
      */
     private ISpcfConfigurationProvider mConfigurationProvider = null;
     
     /**
      *  Gets the Configuration Entry Provider. Null will be returned
      *  if this configuration is not created by any providers.
      *  @return Configuration Entry Provider
      */
     ISpcfConfigurationEntryProvider getConfigurationEntryProvider()
     {
         return (ISpcfConfigurationEntryProvider) mConfigurationProvider;
     }         
}
