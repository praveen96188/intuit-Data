package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.collections.SpcfSet;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.SpcfInMemoryConfiguration;

/**
 * CMS allows a module to have configurations coming from more than one configuration source but provides a unique interface to access
 * them, encapsulating how and where the configurations are loaded from/stored to. SpcfAggregateConfiguration is a configuration used 
 * by the CMS to represent the collection of configurations from different sources. This class stores configuration objects in a list
 * and chooses the appropriate configuration for Get or Set of a configuration Entry.
 * 
 * <p>
 * This aggregate configuration class needs to contain at least one configuration
 * </p>
 *  
 * <p>
 * The way GetXXX/SetXXX Method works is defined as follows:
 * 
 * GetXXX(key): This collector class will lookup the specified key in all the configurations from Last to First. 
 * The first found configuration which has the key wins and it will be chosen as the candidate for getting the value. 
 * If the key does not exist in any of the configurations, SpcfConfigEntryMissingException will be thrown.<br><br>
 * 
 * 
 * SetXXX(key, value): This collector class will lookup the key only in the last configuration. If the last configuration
 * has the key, it will modify the current value with the given value. If the last configuration does not have
 * the key, it will add a new configuration entry with the key and value pair into the last configuration.<br>
 * 
 * </p> 
 */
public strictfp class SpcfAggregateConfiguration implements ISpcfConfiguration
{
    /**
     * Factory instance
     */
	private static final SpcfFactory Factory = SpcfFactory.getInstance();

	/**
	 * Arraylist to contain the configurations
	 */
    private SpcfList<ISpcfConfiguration> mConfigList = Factory.<ISpcfConfiguration>createArrayList();

    /**
     * Constructor
     */
    public SpcfAggregateConfiguration()
    {
       //Constructor
    }

    /**
     * Adds a configuration object to the collection. For simplicity and to prevent cyclic references, this method currently 
     * does not support adding SpcfAggregateConfiguration instances.
     * 
     * @param config The configuration object to be added, cannot be null.
     * @throws com.intuit.spc.foundations.portability.SpcfArgumentNullException if config is null
     * @throws SpcfIllegalArgumentException if config is an instance of SpcfAggregateConfiguration.
     */
    public void addConfiguration(ISpcfConfiguration config)
    {
        SpcfParamValidator.checkIsNotNull(config, "config");
        if (config instanceof SpcfAggregateConfiguration)
        {
            throw new SpcfIllegalArgumentException("Cannot add SpcfAggregateConfiguration instances.");
        }       
        mConfigList.add(config);
    }    

    /**
     * To get the list of configurations contained in this aggregate configuration.
     * @return List of Configurations
     */
    public SpcfList<ISpcfConfiguration> getConfigurationList()
    {
        return mConfigList;
    }
    
    /**
     * Gets the last configuration in the collection.
     * @return Last Configuration
     * @throws SpcfInvalidOperationException if there is no configurations in the collection
     */
    private ISpcfConfiguration getLastConfiguration()
    {
         int size = mConfigList.getSize();
         if(size < 1)
             throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
         
            return mConfigList.getItem(size - 1);
    }
  
    /**
     * Gets the configuration for getting the configuration entries.
     * @param key Config Entry Key
     * @return Configuration
     * @throws SpcfInvalidOperationException if there is no configurations in the collection
     */
    private ISpcfConfiguration getConfigurationForGet(String key)
    {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
                
        for (int i = size - 1; i >= 0; i--)
        {
            ISpcfConfiguration config = mConfigList.getItem(i);
            if (config.containsKey(key))
            {
                return config;
            }
        }
        throw new SpcfConfigEntryMissingException(key);
    }
        
    /**
     * Retrieves the list of keys from all the configurations in the collection (Union of keys)
     * @return Union of keys in all the configurations in the collection.
     * @throws SpcfInvalidOperationException if there is no configurations in the collection     
     */
    public ISpcfIterator<String> getKeys()
    {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
        
        SpcfSet<String> keys = SpcfFactory.getInstance().<String>createTreeSet();
        for (int i = 0; i < size; i++)
        {
            ISpcfConfiguration config = mConfigList.getItem(i);
            for (ISpcfIterator<String> it = config.getKeys(); it.hasNext();)
            {
                keys.add(it.next().toString());
            }
        }
        return keys.getIterator();
    }
    
   
    /**
     * Retrieves the list of configuration entries in all the configurations in the collection.
     * @return Union of Configuration Entries in all configurations in the collection.
     * @throws SpcfInvalidOperationException if there is no configurations in the collection     
     */
    public SpcfMap<String, Object> getConfigurationEntries()
    {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
          
        SpcfMap<String, Object> entries = SpcfFactory.getInstance().<String, Object>createHashMap();
        for (int i = size - 1; i >= 0; i--)
        {
            ISpcfConfiguration config = mConfigList.getItem(i);
            for (SpcfPair<String, Object> it : config.getConfigurationEntries())
            {   
                if(!entries.containsKey(it.getKeyItem()))
                    entries.add(it.getKeyItem().toString(), it.getValueItem());              
            }
        }
        return entries;
    }
    
    /**
     * Returns whether the configuration is empty or not. 
     * If at least one of the configurations in the collection is non-empty, then it will return false.
     * @return True if all configurations in the collection are empty and False even if one configuration or more 
     * is not empty.
     * @throws SpcfInvalidOperationException if there is no configurations in the collection       
     */
    public boolean isEmpty()
    {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
                
        for (int i = 0; i < size; i++)
        {
            ISpcfConfiguration config = mConfigList.getItem(i);
            if (!config.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the key exists in any of the configurations in the collection.
     * @param key Config Entry Key
     * @return True if the key exists in at least one of the configurations and False if the key does not exist in any configurations
     * @throws SpcfInvalidOperationException if there is no configurations in the collection        
     */
    public boolean containsKey(String key)
    {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
                
        for (int i = 0; i < size; i++)
        {
            ISpcfConfiguration config = mConfigList.getItem(i);
            if (config.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }   
    
    /**
     * Gets a copy of the configuration. Modifying the copy does not affect 
     * the actual configuration.
     * @return Copy of the configuration
     * @throws SpcfInvalidOperationException if there is no configurations in the collection 
     */
    public ISpcfConfiguration getCopy()
    {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
        
        ISpcfConfiguration inMemoryCopy = new SpcfInMemoryConfiguration(this.toImmutable());
        return inMemoryCopy;
    }

    /**
     * Returns an Immutable configuration created out of the actual configuration. This will create an
     * immutable configuration (a configuration which wraps around hash map) by calling GetEntry for each
     * key in all the contained configurations from First to Last.
     * @return Immutable configuration
     * @throws SpcfInvalidOperationException if there is no configurations in the collection 
     */
    public ISpcfImmutableConfiguration toImmutable() 
    {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
        
        if (this.isEmpty())
        {
            return SpcfImmutableConfiguration.Empty;
        }
        return new SpcfImmutableConfigurationWrapper(this);
    }

    /**
     * Sets/Adds a boolean value for the specified key.
     * Sets a boolean value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setBoolean(String key, boolean configValue) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setBoolean(key, configValue);    
    }

    /**
     * Sets/Adds a double value for the specified key.
     * Sets a double value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setDouble(String key, double configValue) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setDouble(key, configValue);        
    }

    /**
     * Sets/Adds a float value for the specified key.
     * Sets a float value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setFloat(String key, float configValue) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setFloat(key, configValue);
    }

    /**
     * Sets/Adds an integer value for the specified key.
     * Sets a integer value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setInteger(String key, int configValue) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setInteger(key, configValue);
    }

    /**
     * Sets/Adds a long value for the specified key.
     * Sets a long value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setLong(String key, long configValue) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setLong(key, configValue);
    }

    /**
     * Sets/Adds a short value for the specified key.
     * Sets a short value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setShort(String key, short configValue) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setShort(key, configValue);
        
    }

    /**
     * Sets/Adds a string value for the specified key.
     * Sets a string value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setString(String key, String configValue) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setString(key, configValue);        
    }

    /**
     * Sets/Adds an entry value for the specified key.
     * Sets an entry value for the specified key in the last configuration. If the key
     * does not exist in the last configuration, then it will add this key with the value as a 
     * new configuration entry into the last configuration.
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null. 
     */
    public void setEntry(String key, Object configValue)
    {
        SpcfParamValidator.checkIsNotNull(key, "key");
        SpcfParamValidator.checkIsNotNull(configValue, "configValue");
        getLastConfiguration().setEntry(key, configValue);         
    }
    
    
    /**
     * Gets the cumulative count of entries in all the configuration in this aggregate configuration
     * @return Count of entries
     * @throws SpcfInvalidOperationException if there is no configurations in the collection        
     */
    public int getCount() {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
        
        int count = 0;        
        for (int i = 0; i < size; i++)
        {
            ISpcfConfiguration config = mConfigList.getItem(i);
            count = count + config.getCount();            
        }        
        return count;
    }

    /**
     * Gets a subset configuration which has only the entries with keys starting with a specific prefix. Modifying
     * any values in this subset configuration will not affect the actual configuration. This is a handy API to get
     * a bunch of configuration entries with same prefix in the keys.
     * 
     * <p>
     * Note: It's preferable to arrange the key names as namespace like "Button.Color, Button.Size, TextBox.Color, 
     * TextBox.Size etc". By following this pattern, it will be easy to get a group of related configuration. For instance,
     * you may get a subset configuration which has only entries pertaining to "Button" by calling 
     * this API with a prefix "Button".
     * </p>
     *  
     * @param prefix of the key
     * @return Subset Configuration
     * @throws SpcfInvalidOperationException if there is no configurations in the collection        
     */
    public ISpcfImmutableConfiguration subset(String prefix) {
        int size = mConfigList.getSize();
        if(size < 1)
            throw new SpcfInvalidOperationException("SpcfAggregateConfiguration needs to have at least one configuration");
        
        return SpcfSubsetConfiguration.getSubset(this, prefix);
    }

    /**
     * Gets a boolean value for the specified key.
     * Gets a boolean value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public boolean getBoolean(String key) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getBoolean(key);
    }

    /**
     * Gets a boolean value for the specified key.
     * Gets a boolean value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getBoolean(key);
    }

    /**
     * Gets a double value for the specified key.
     * Gets a double value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public double getDouble(String key) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getDouble(key);
    }

    /**
     * Gets a double value for the specified key.
     * Gets a double value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public double getDouble(String key, double defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getDouble(key);
    }

    /**
     * Gets a float value for the specified key.
     * Gets a float value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public float getFloat(String key) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getFloat(key);
    }

    /**
     * Gets a float value for the specified key.
     * Gets a float value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public float getFloat(String key, float defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getFloat(key);
    }

    /**
     * Gets a integer value for the specified key.
     * Gets a integer value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public int getInteger(String key) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getInteger(key);
    }

    /**
     * Gets an integer value for the specified key.
     * Gets an integer value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public int getInteger(String key, int defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getInteger(key);
    }

    /**
     * Gets a long value for the specified key.
     * Gets a long value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public long getLong(String key) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getLong(key);
    }

    /**
     * Gets a long value for the specified key.
     * Gets a long value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public long getLong(String key, long defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getLong(key);
    }

    /**
     * Gets a short value for the specified key.
     * Gets a short value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public short getShort(String key) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getShort(key);
    }

    /**
     * Gets a short value for the specified key.
     * Gets a short value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public short getShort(String key, short defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getShort(key);
    }

    /**
     * Gets a string value for the specified key.
     * Gets a string value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public String getString(String key) {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getString(key);
    }

    /**
     * Gets a string value for the specified key.
     * Gets a string value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public String getString(String key, String defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getString(key);
    }

    /**
     * Gets an entry for the specified key.
     * Gets entry value from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, then SpcfConfigEntryMissingException will be
     * thrown.
     * @param key Config Key
     * @return Config Value
     * @throws SpcfConfigEntryMissingException if the key does not exists
     */
    public Object getEntry(String key)
    {
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getEntry(key);
    }

    /**
     * Gets an entry for the specified key.
     * Gets an entry from the last found configuration in which the key exists. if the key does not
     * exist in any of the configuration in this aggregate configuration, the default value will be returned.
     * @param key Config Key
     * @param defaultValue to be returned if the key does not exists
     * @return Config Value
     */
    public Object getEntry(String key, Object defaultValue) {
        if(!this.containsKey(key))
        {
            return defaultValue;
        }
        SpcfParamValidator.checkIsNotNull(key, "key");
        ISpcfConfiguration config = getConfigurationForGet(key);
        return config.getEntry(key);
    }

    /**
     * Gets the id of the module for which the last configuration belongs to. 
     * 
     * @return Module ID
     * @throws SpcfInvalidOperationException if there is no configurations in the collection      
     */
    public String getModuleID() {
        ISpcfConfiguration defaultConfiguration = getLastConfiguration();
        if(defaultConfiguration != null)
            return defaultConfiguration.getModuleID();
        else
            return null;
    }

    /**
     * Calling this API does nothing as CMS will take care of setting the appropriate module ID
     * for the individual configurations.
     * 
     * @param moduleID Module ID
     */
    public void setModuleID(String moduleID) {
        //Do nothing    
    }  
}
