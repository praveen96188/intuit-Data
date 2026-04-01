package com.intuit.sbd.payroll.psp.configuration;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2010
 * Time: 11:43:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationProxy implements ISpcfImmutableConfiguration {
    public static final Logger logger = Logger.getLogger(IDPSManager.class.getName());
    private static final Pattern sfmIDPSPattern = Pattern.compile("^IDPS\\((.+)\\)$", Pattern.CASE_INSENSITIVE);
    private ISpcfImmutableConfiguration mConfiguration;


    public ConfigurationProxy(ISpcfImmutableConfiguration pDelegate) {
        mConfiguration = pDelegate;
    }

    public ISpcfIterator<String> getKeys() {
        return mConfiguration.getKeys();
    }

    /**
     * Note that retrieving all properties via this method will require any encrypted properties to be manually
     * decrypted.
     * @return
     */
    public SpcfMap<String, Object> getConfigurationEntries() {
        return mConfiguration.getConfigurationEntries();
    }

    public int getCount() {
        return mConfiguration.getCount();
    }

    public ISpcfImmutableConfiguration subset(String prefix) {
        return mConfiguration.subset(prefix);
    }

    public boolean isEmpty() {
        return mConfiguration.isEmpty();
    }

    public boolean containsKey(String key) {
        return mConfiguration.containsKey(key);
    }

    public boolean getBoolean(String key) {
        return mConfiguration.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mConfiguration.getBoolean(key, defaultValue);
    }

    public double getDouble(String key) {
        return mConfiguration.getDouble(key);
    }

    public double getDouble(String key, double defaultValue) {
        return mConfiguration.getDouble(key, defaultValue);
    }

    public float getFloat(String key) {
        return mConfiguration.getFloat(key);
    }

    public float getFloat(String key, float defaultValue) {
        return mConfiguration.getFloat(key, defaultValue);
    }

    public int getInteger(String key) {
        return mConfiguration.getInteger(key);
    }

    public int getInteger(String key, int defaultValue) {
        return mConfiguration.getInteger(key, defaultValue);
    }

    public long getLong(String key) {
        return mConfiguration.getLong(key);
    }

    public long getLong(String key, long defaultValue) {
        return mConfiguration.getLong(key, defaultValue);
    }

    public short getShort(String key) {
        return mConfiguration.getShort(key);
    }

    public short getShort(String key, short defaultValue) {
        return mConfiguration.getShort(key, defaultValue);
    }

    /**
     * This method will retrieve the given string property and decrypt it if it is encrypted.<br>
     * Encrypted string properties are case-insensitively tagged by the pattern:  ENC(encrypted-property)
     * @param key
     * @return
     */
    public String getString(String key) {
        return decodeProperty(mConfiguration.getString(key));
    }

    /**
     * This method will retrieve the given string property and decrypt it if it is encrypted.<br>
     * Encrypted string properties are case-insensitively tagged by the pattern:  ENC(encrypted-property)
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        return decodeProperty(mConfiguration.getString(key, defaultValue));
    }

    /**
     * This method will retrieve the given string property and decrypt it if it is encrypted.<br>
     * Encrypted string properties are case-insensitively tagged by the pattern:  ENC(encrypted-property)
     * @param key
     * @return
     */
    public Object getEntry(String key) {
        return getEntry(key, null);
    }

    /**
     * This method will retrieve the given string property and decrypt it if it is encrypted.<br>
     * Encrypted string properties are case-insensitively tagged by the pattern:  ENC(encrypted-property)
     * @param key
     * @param defaultValue
     * @return
     */
    public Object getEntry(String key, Object defaultValue) {
        Object obj = mConfiguration.getEntry(key, defaultValue);
        return (obj instanceof String) ? decodeProperty((String)obj) : obj;
    }

    public String getModuleID() {
        return mConfiguration.getModuleID();
    }

    public void setModuleID(String moduleID) {
        mConfiguration.setModuleID(moduleID);
    }

    /**
     * If the passed in property is encrypted (i.e. matches the pattern: ENC(...)), the property will be decrypted.
     * If the passed in property is not encrypted, the unaltered value is returned.
     * If mPropertyDecrypter is uninitialized (i.e. if the private key file is unavailable), the passed in value
     * will be returned unaltered.
     * @param value The (possibly) encrypted property
     * @return The decrypted property if encrypted (and mPropertyDecrypter is valid), else the unaltered property.
     */
    public static String decodeProperty(String value) {
        if ((value != null) && (value.length() > 0)) {
            try {
                Matcher matcher = sfmIDPSPattern.matcher(value);
                if(matcher.matches()) {
                    String prefix =  "PSPSecrets/secrets/";
                    String env = ConfigurationManager.getSettingValue(DatabaseConfigManager.MonolithDbToken, "dataAccess.env");
                    if(env != null) {
                        prefix = prefix + env + "/";
                        value = prefix + matcher.group(1);
                        value = IDPSManager.getSecret(value);
                    } else {
                        throw new RuntimeException("Null environment variable found. Cannot read property value from IDPS.");
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException("Error decrypting property value.", t);
            }
        }

        return value;
    }
}
