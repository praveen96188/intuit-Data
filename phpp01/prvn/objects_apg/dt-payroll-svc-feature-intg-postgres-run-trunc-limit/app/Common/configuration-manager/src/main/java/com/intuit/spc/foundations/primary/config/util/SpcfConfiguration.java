package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;

/**
 * An abstract class that implements some methods defined on the
 * {@link com.intuit.spc.foundations.primary.config.ISpcfConfiguration} interface. It
 * provides default implementations for basic data conversion operations such as setShort,
 * setLong, etc.
 * 
 * For more info go to:
 * <p>
 * <a
 * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0">SPCF
 * Configuration Management Users Guide </a>
 */
public abstract strictfp class SpcfConfiguration
    extends SpcfImmutableConfiguration
    implements ISpcfConfiguration
{
    /**
     * Clones this configuration instance to an immutable configuration instance. The immutable instance will not be
     * affected by changes done to this instance.
     *
     * @return An immutable instance containing all configuration settings of this configuration instance; however,
     *         changes done to this configuration instance will not be propagated to the immutable instance after the
     *         method returns.
     */
    public ISpcfImmutableConfiguration toImmutable()
    {
        if (this.isEmpty())
        {
            return SpcfImmutableConfiguration.Empty;
        }
        
        return new SpcfImmutableConfigurationWrapper(this);
    }

    /**
     * Set the configuration entry associated with the key to the specified boolean value
     * if key exists; otherwise, a new configuration entry is added with the specified key
     * and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setBoolean(String key, boolean configValue)
    {
        setEntry(key, Boolean.valueOf(configValue));
    }

    /**
     * Set the configuration entry associated with the key to the specified double
     * configValue if key exists; otherwise, a new configuration entry is added with the
     * specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setDouble(String key, double configValue)
    {
        setEntry(key, new Double(configValue));
    }

    /**
     * Set the configuration entry associated with the key to the specified float
     * configValue if key exists; otherwise, a new configuration entry is added with the
     * specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setFloat(String key, float configValue)
    {
        setEntry(key, new Float(configValue));
    }

    /**
     * Set the configuration entry associated with the key to the specified int
     * configValue if key exists; otherwise, a new configuration entry is added with the
     * specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setInteger(String key, int configValue)
    {
        setEntry(key, new Integer(configValue));
    }

    /**
     * Set the configuration entry associated with the key to the specified long
     * configValue if key exists; otherwise, a new configuration entry is added with the
     * specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setLong(String key, long configValue)
    {
        setEntry(key, new Long(configValue));
    }

    /**
     * Set the configuration entry associated with the key to the specified short
     * configValue if key exists; otherwise, a new configuration entry is added with the
     * specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setShort(String key, short configValue)
    {
        setEntry(key, new Short(configValue));
    }

    /**
     * Set the configuration entry associated with the key to the specified String
     * configValue if key exists; otherwise, a new configuration entry is added with the
     * specified key and value.
     *
     * @param key The key of the configuration entry to set or add.
     * @param configValue The new configValue.
     * @throws SpcfArgumentNullException if key or configValue is null.
     */
    public void setString(String key, String configValue)
    {
        setEntry(key, configValue);
    }              
}