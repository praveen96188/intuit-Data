package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;

/**
 * An interface that represents a set of mutable configurations. Neither configuration key nor configuration value is
 * allowed to be null.
 * <p>
 * For more info go to:
 * </p>
 * <p>
 * <a href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0">SPCF Configuration
 * Management Users Guide </a>
 * </p>
 */
public strictfp interface ISpcfConfiguration extends ISpcfImmutableConfiguration
{
    /**
     * Clones this configuration instance to an immutable configuration instance. The immutable instance will not be
     * affected by changes done to this instance.
     *
     * @return An immutable instance containing all configuration settings of this configuration instance; however,
     *         changes done to this configuration instance will not be propagated to the immutable instance after the
     *         method returns.
     */
    ISpcfImmutableConfiguration toImmutable();

    /**
     * Set the configuration entry associated with the key to the specified configValue if key exists; otherwise, a new
     * configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    void setEntry(String key, Object configValue);

    /**
     * Set the configuration entry associated with the key to the specified boolean value if key exists; otherwise, a
     * new configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key is null.
     */
    void setBoolean(String key, boolean configValue);

    /**
     * Set the configuration entry associated with the key to the specified double configValue if key exists; otherwise,
     * a new configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key is null.
     */
    void setDouble(String key, double configValue);

    /**
     * Set the configuration entry associated with the key to the specified float configValue if key exists; otherwise,
     * a new configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key is null.
     */
    void setFloat(String key, float configValue);

    /**
     * Set the configuration entry associated with the key to the specified int configValue if key exists; otherwise, a
     * new configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key is null.
     */
    void setInteger(String key, int configValue);

    /**
     * Set the configuration entry associated with the key to the specified long configValue if key exists; otherwise, a
     * new configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key is null.
     */
    void setLong(String key, long configValue);

    /**
     * Set the configuration entry associated with the key to the specified short configValue if key exists; otherwise,
     * a new configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key is null.
     */
    void setShort(String key, short configValue);

    /**
     * Set the configuration entry associated with the key to the specified String configValue if key exists; otherwise,
     * a new configuration entry is added with the specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    void setString(String key, String configValue);
    
    /** 
     * Gets a copy of the configuration. Changing the copy does not affect the real configuration
     * object.  
     * @return Copy of the configuration
     */    
    ISpcfConfiguration getCopy();     
}