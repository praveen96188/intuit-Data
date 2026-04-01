package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.collections.SpcfMap;

/**
 * An interface that all configuration providers must implement. A configuration provider is a class (or set of classes)
 * that knows how to read and update a set of configuration entries in a particular storage and format.
 * <p>
 * The CMS provides a set of default configuration providers that are generic for all clients. If a client needs more
 * customized configuration provider, such as a specific format, the client can provide its own configuration provider
 * and plug it in to the CMS framework.
 * <p>
 * All configuration providers implementing this interface must provide a public default constructor. Here is the life
 * cycle of a configuration provider:<br>
 * 1. The configuration provider is first instantiated using the default constructor.<br>
 * 2. The {@link #init(SpcfMap&lt;String, Object&gt;)} method is called on the configuration instance.<br>
 * 3. After {@link #init(SpcfMap&lt;String, Object&gt;)} returns successfully, the configuration provider is considered to be ready for
 * service.<br>
 * 4. At this point, any methods other than {@link #init(SpcfMap&lt;String, Object&gt;)}, such as {@link #getReadonly()},
 * {@link #load(ISpcfImmutableConfiguration)}or {@link #store(ISpcfConfiguration)}may be called in any order and by
 * multiple threads.
 */
public interface ISpcfConfigurationProvider
{
    /**
     * This method is called by the configuration manager after the provider has been instantiated and before any other
     * methods is called. This method gives the provider a chance to initialize itself with properties specified in the
     * meta configuration.
     * 
     * @param properties The name-value pair properties specified in the meta configuration for this provider.
     * @throws SpcfArgumentNullException if properties is null.
     * @throws SpcfMetaConfigEntryMissingException if one or more meta configuration entries are missing
     * @throws SpcfMetaConfigEntryInvalidException if one or more meta configuration entries are invalid
     * @throws SpcfConfigProviderException if the instance can not be properly initialized.
     */
    void init(SpcfMap<String, Object> properties);

    /**
     * Loads configuration from the storage repository that this provider supports. {@link #init(SpcfMap&lt;String, Object&gt;)} is guranteed
     * to have been called once and only once before this method is called. <br>
     * The implementation class must make sure that load and store can be called in any order by multiple threads.
     * <p>
     * Some configuration providers allow configuration entries to reference constants using a specific syntax. For
     * instance, suppose configConstants contains the following entries:
     * 
     * <pre>
     * 
     *      smtpHost=mail.ma.intuit.com 
     *      database=dev
     *  
     * </pre>
     * 
     * The configuration entry:
     * 
     * <pre>
     * 
     *   spc-f.autotest.smtpHost=$$smtpHost$$
     *  
     * </pre>
     * 
     * May be expanded to:
     * 
     * <pre>
     * 
     *   spc-f.autotest.smtpHost=mail.ma.intuit.com
     *  
     * </pre>
     * 
     * The syntax used above is only an example. The exact syntax used to reference constants may differ from
     * configuration provider to configuration provider. Also, not every configuration provider is required to support
     * constants expansion. For details, please refer to documentation on the specific configuration provider.
     * <p>
     * Please note that when this configuration provider is used to load constants, configConstants will be null.
     *  
     * @param configConstants An immutable configuration object representing a set of constants or null when this
     *            configuration provider is used to load constants. Ignored if this configuration provider does not
     *            support constant expansion.
     * @return The configuration from storage. If the configuration provider supports constant expansion, all constants
     *         referenced in the configuration source must be expanded to the values defined in configConstants.
     * @throws SpcfConfigInvalidException if one or more configuration entries are invalid
     * @throws SpcfConfigProviderException if the configuration can not be loaded successfully.
     */
    ISpcfConfiguration load(ISpcfImmutableConfiguration configConstants);

    /**
     * Stores the configuration to the storage repository that this provider supports. {@link #init(SpcfMap&lt;String, Object&gt;)} is
     * guranteed to have been called once and only once before this method is called.
     * <p>
     * The implementation class must make sure that load and store can be called in any order by multiple threads.
     * 
     * @param config The configuration to store
     * @throws SpcfArgumentNullException if config is null.
     * @throws SpcfConfigInvalidException if one or more configuration entries are invalid
     * @throws SpcfProviderUpdateUnsupportedException if store is not supported by this configuration provider.
     * @throws SpcfConfigProviderException if the configuration can not be stored successfully.
     */
    void store(ISpcfConfiguration config);
    
    /**
     * Returns true if this configuration provider only supports load not store. This can happen for the following two
     * reasons:<br>
     * The configuration provider has been explicitly configured to be read only in the meta configuration<br>
     * the configuration storage associated with this configuration provider is read only.<br>
     * 
     * @return true if this configuration provider only supports load not store, false otherwise.
     */
    boolean getReadonly();
    
    /**
     * This is to enable the CMS to return different copies of the configuration at every call to 
     * {@link SpcfConfigurationManager#getConfiguration(String)} 
     *  
     * @return True if differnt copies have to be created at every call to {@link SpcfConfigurationManager#getConfiguration(String)} , 
     * False if same instance to be returned at different call to {@link SpcfConfigurationManager#getConfiguration(String)}.
     */
    boolean getReturnCopyRequired();

    /**
     * This is to enable configuration manager to return source filename
     */
    String getConfigurationSourceName();
}
