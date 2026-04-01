package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigConversionException;
import com.intuit.spc.foundations.primary.config.SpcfConfigEntryMissingException;

/**
 * An abstract class that implements some methods defined on the
 * {@link com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration} interface. It provides default
 * implementations for basic data conversion operations such as getShort, getLong, etc.
 */
public abstract strictfp class SpcfImmutableConfiguration 
    implements ISpcfImmutableConfiguration
{    
    /**
     * An immutable configuration object that represents an empty set of configurations.
     */       
    public static final ISpcfImmutableConfiguration Empty = new SpcfImmutableConfigurationWrapper();
    
    /**
     * Get the number of configuration entries in this configuration object.
     * 
     * @return the number of configuration entries in this configuration object.
     */
    public int getCount()
    {
        int count = 0;
        for (ISpcfIterator<String> it = this.getKeys(); it.hasNext(); count++, it.next())
        {
        	//Just Counting
        }
        return count;
    }
    
    /**
     * Get a boolean associated with the given configuration key.
     * 
     * @param configKey The configuration key.
     * @return The boolean value associated with the key. The boolean returned represents the value true if the
     *         configuration value is equal, ignoring case, to the string "true". For all other string values, false is
     *         returned.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    public boolean getBoolean(String configKey)
    {
        String stringValue = this.getString(configKey);
        boolean retVal = false;
        
        // Do not use Boolean.valueOf(stringValue).booleanValue() because we need to be portable            
        if (stringValue.toLowerCase().equals("true"))            
        {               
            retVal = true;           
        }            
        
        return retVal;
    }

    /**
     * Get a boolean associated with the given configuration key. If the key doesn't map to an existing entry, the
     * default value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found.
     * @return The associated boolean if key is found, defaultValue otherwise.
     * @throws SpcfArgumentNullException if key is null
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;            
        }
        
        return this.getBoolean(key);         
    }

    /**
     * Get a double associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The double value associated with the key.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a double value.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    public double getDouble(String key)
    {
        String stringValue = this.getString(key);
               
        try
        {
            return Double.parseDouble(stringValue);            
        }
        catch (Exception e)
        {
            throw new SpcfConfigConversionException(key, stringValue, "double", e);
        }               
    }

    /**
     * Get a double associated with the given configuration key. If the key doesn't map to an existing entry, the
     * default value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found.
     * @return The associated double value if key is found, defaultValue otherwise.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a double value.
     * @throws SpcfArgumentNullException if key is null
     */
    public double getDouble(String key, double defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;
        }
        
        return this.getDouble(key);
    }

    /**
     * Get a float associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The float value associated with the key.     
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a float value.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    public float getFloat(String key)
    {
        String configValue = this.getString(key);
        
        try
        {
            return Float.parseFloat(configValue);
        }
        catch (Exception e)
        {
            throw new SpcfConfigConversionException(key, configValue, "float", e);
        } 
    }

    /**
     * Get a float associated with the given configuration key. If the key doesn't map to an existing entry, the default
     * value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found.
     * @return The associated float value if key is found, defaultValue otherwise.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a float value.
     * @throws SpcfArgumentNullException if key is null
     */
    public float getFloat(String key, float defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;
        }
        
        return this.getFloat(key);
    }

    /**
     * Get an int associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The int value associated with the key.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a int value.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    public int getInteger(String key)
    {
        String configValue = this.getString(key);
        
        try
        {
            return Integer.parseInt(configValue);
        }
        catch (Exception e)
        {
            throw new SpcfConfigConversionException(key, configValue, "integer", e);
        } 
    }

    /**
     * Get an int associated with the given configuration key. If the key doesn't map to an existing entry, the default
     * value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found.
     * @return The associated int value if key is found, defaultValue otherwise.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a int value.
     * @throws SpcfArgumentNullException if key is null
     */
    public int getInteger(String key, int defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;
        }
        
        return this.getInteger(key);
    }

    /**
     * Get a long associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The long value associated with the key.
     * @throws SpcfConfigConversionException is thrown if the key maps to an entry that can not be converted to a long
     *             value.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null             
     */
    public long getLong(String key)
    {
        String configValue = this.getString(key);
        
        try
        {
            return Long.parseLong(configValue);
        }
        catch (Exception e)
        {
            throw new SpcfConfigConversionException(key, configValue, "long", e);
        }         
    }

    /**
     * Get a long associated with the given configuration key. If the key doesn't map to an existing entry, the default
     * value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found.
     * @return The associated long value if key is found, defaultValue otherwise.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a long value.
     * @throws SpcfArgumentNullException if key is null
     */
    public long getLong(String key, long defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;
        }
        
        return this.getLong(key);
    }

    /**
     * Get a short associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The short value associated with the key.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a short value.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    public short getShort(String key)
    {
        String configValue = this.getString(key);
        
        try
        {
            return Short.parseShort(configValue);
        }
        catch (Exception e)
        {
            throw new SpcfConfigConversionException(key, configValue, "short", e);
        }        
    }

    /**
     * Get a short associated with the given configuration key. If the key doesn't map to an existing entry, the default
     * value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found.
     * @return The associated short value if key is found, defaultValue otherwise.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a short value.
     * @throws SpcfArgumentNullException if key is null
     */
    public short getShort(String key, short defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;
        }
        
        return this.getShort(key);
    }

    /**
     * Get a string associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The string associated with the key.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    public String getString(String key)
    {
        Object obj = this.getEntry(key);
        if (obj instanceof String)
        {
            return (String) obj;
        }
        
        return obj.toString();        
    }

    /**
     * Get a string associated with the given configuration key. If the key doesn't map to an existing entry, the
     * default value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found, can be null.
     * @return The associated string if key is found, defaultValue otherwise.
     * @throws SpcfArgumentNullException if key is null
     */
    public String getString(String key, String defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;
        }
        
        return this.getString(key);
    }  
    
    /**
     * Gets an entry associated with the key from the configuration.
     * 
     * @param key the key of the configuration entry to retrieve
     * @param defaultValue the default value
     * @return the value to which this key maps.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    public Object getEntry(String key, Object defaultValue)
    {
        if (!this.containsKey(key))
        {
            return defaultValue;
        }
        
        return this.getEntry(key);        
    }
    
    /**
     * Returns a ISpcfImmutableConfiguration containing every key from this ISpcfImmutableConfiguration instance that
     * starts with the specified prefix. The keys are expected to be seperated by "." (dot). The prefix is removed from
     * the keys in the subset. For example, if the configuration contains the following properties:
     * 
     * <pre>
     *         prefix.number = 1
     *         prefix.string.key = Apache
     *         prefixed.foo = bar
     *         prefix = Jakarta
     * </pre>
     * 
     * the ISpcfImmutableConfiguration returned by <code>subset("prefix")</code> will contain the properties:
     * 
     * <pre>
     *         number = 1
     *         string.key = Apache
     *          = Jakarta
     * </pre>
     * 
     * (The keys are seperated by ".". The key for the value "Jakarta" is an empty string)
     * <p>
     * 
     * @param prefix The prefix used to select the subset, cannot be null.  
     * @return a ISpcfImmutableConfiguration containing every key from this ISpcfImmutableConfiguration instance that
     *         starts with the specified prefix. The prefix is removed from the keys in the subset returned.
     * @throws SpcfArgumentNullException if prefix is null
     */
    public ISpcfImmutableConfiguration subset(String prefix)
    {        
        return SpcfSubsetConfiguration.getSubset(this, prefix);
    }     
    
    /**
     * Gets the Module for which this configuration belongs to. Null will be returned
     * if this configuration does not belong to any module.
     * @return String representing Module ID
     */
    public String getModuleID()
    {
        return mModuleId;
    }
    
    /**
     * Sets the Module for which this configuration belongs to.
     * Note: Setting the module id for a configuration is automatically taken care
     * by CMS. So there is no need to use this API.
     * @param moduleID String representing Module ID
     */
    public void setModuleID(String moduleID)
    {
        this.mModuleId = moduleID;
    }
    
    /**
     * Instance to hold module ID
     */
    private String mModuleId = null;
    
}
