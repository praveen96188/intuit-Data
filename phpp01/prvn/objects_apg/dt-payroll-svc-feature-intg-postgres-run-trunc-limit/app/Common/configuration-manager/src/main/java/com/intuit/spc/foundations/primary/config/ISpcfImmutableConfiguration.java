package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;

/**
 * An interface that represents a set of immutable configurations. This interface does not allow null configuration
 * keys.
 * <p>
 * For more info go to:
 * </p>
 * <p>
 * <a href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0">SPCF Configuration
 * Management Users Guide </a>
 * </p>
 */
public strictfp interface ISpcfImmutableConfiguration
{        
    /**
     * Get a iterator of the keys contained in the configuration.
     * 
     * @return A iterator of the keys contained in the configuration.
     */
    ISpcfIterator<String> getKeys();
    
    /**
     * Gets an iterable map of the configuration entries contained in the configuration.
     * 
     * @return An iterable map of configuration entries contained in the configuration.
     */
    SpcfMap<String, Object> getConfigurationEntries();
    
    /**
     * Get the number of configuration entries in this configuration object.
     * 
     * @return the number of configuration entries in this configuration object.
     */
    int getCount();        

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
    ISpcfImmutableConfiguration subset(String prefix);

    /**
     * Check if the configuration is empty.
     * 
     * @return <code>true</code> if the configuration contains no entries, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Check if the configuration contains the specified key.
     * 
     * @param key the key whose presence in this configuration is to be tested
     * @return <code>true</code> if the configuration contains an entry for this key, <code>false</code> otherwise
     * @throws SpcfArgumentNullException if key is null
     */
    boolean containsKey(String key);

    /**
     * Get a boolean associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The boolean value associated with the key. The boolean returned represents the value true if the
     *         configuration value is equal, ignoring case, to the string "true". For all other string values, false is
     *         returned.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    boolean getBoolean(String key);

    /**
     * Get a boolean associated with the given configuration key. If the key doesn't map to an existing entry, the
     * default value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found.
     * @return The associated boolean if key is found, defaultValue otherwise.
     * @throws SpcfArgumentNullException if key is null
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get a double associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The double value associated with the key.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a double value.
     * @throws SpcfArgumentNullException if key is null
     */
    double getDouble(String key);

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
    double getDouble(String key, double defaultValue);

    /**
     * Get a float associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The float value associated with the key.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a float value.
     * @throws SpcfArgumentNullException if key is null
     */
    float getFloat(String key);

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
    float getFloat(String key, float defaultValue);

    /**
     * Get an int associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The int value associated with the key.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a int value.
     * @throws SpcfArgumentNullException if key is null
     */
    int getInteger(String key);

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
    int getInteger(String key, int defaultValue);

    /**
     * Get a long associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The long value associated with the key.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfConfigConversionException is thrown if the key maps to an entry that can not be converted to a long
     *             value.
     * @throws SpcfArgumentNullException if key is null
     */
    long getLong(String key);

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
    long getLong(String key, long defaultValue);

    /**
     * Get a short associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The short value associated with the key.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfConfigConversionException if the key maps to an entry that can not be converted to a short value.
     * @throws SpcfArgumentNullException if key is null
     */
    short getShort(String key);

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
    short getShort(String key, short defaultValue);

    /**
     * Get a string associated with the given configuration key.
     * 
     * @param key The configuration key.
     * @return The string associated with the key.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null
     */
    String getString(String key);

    /**
     * Get a string associated with the given configuration key. If the key doesn't map to an existing entry, the
     * default value is returned.
     * 
     * @param key The configuration key.
     * @param defaultValue The default value to return if key is not found, can be null.
     * @return The associated string if key is found, defaultValue otherwise.
     * @throws SpcfArgumentNullException if key is null
     */
    String getString(String key, String defaultValue);

    /**
     * Gets an entry associated with the key from the configuration.
     * 
     * @param key the key of the configuration entry to retrieve
     * @return the value to which this key maps.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null.
     */
    Object getEntry(String key);

    /**
     * Gets an entry associated with the key from the configuration. If the key doesn't map to an existing entry, the
     * default value is returned.
     * 
     * @param key the key of the configuration entry to retrieve
     * @param defaultValue The default value to return if key is not found, can be null.
     * @return The associated Object if key is found, defaultValue otherwise.
     * @throws SpcfConfigEntryMissingException if the key doesn't map to an existing entry.
     * @throws SpcfArgumentNullException if key is null.
     */
    Object getEntry(String key, Object defaultValue);
    
    /**
     * Gets the Module for which this configuration belongs to. Null will be returned
     * if this configuration does not belong to any module.
     * @return String representing Module ID
     */
    String getModuleID();
    
    /**
     * Sets the Module for which this configuration belongs to.
     * Note: Setting the module id for a configuration is automatically taken care
     * by CMS. So there is no need to use this API.
     * @param moduleID String representing Module ID
     */
    void setModuleID(String moduleID);
}