package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfStringBuilder;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.util.SpcfAggregateConfiguration;
import com.intuit.spc.foundations.primary.config.util.SpcfProviderMap;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

/**
 * The class that manages configurations for a single application. It is the entry point to the CMS portable APIs.
 * <p>
 * Here is a simple example of typical client usage:
 * <p>
 * String myMetaConfig = ...; SpcfConfigurationManager.setMetaConfiguration(myMetaConfig); //optional <br>
 * SpcfConfigurationManager configMgr = SpcfConfigurationManager.getInstance(); <br>
 * ISpcfConfiguration config = configMgr.getConfiguration("Logger"); <br>
 * config.setShort("ThreadCount", 10); <br>
 * ... <br>
 * configMgr.storeConfiguration("Logger", config); <br>
 */
public abstract class SpcfConfigurationManager
{
    /**
     * Hard coded overall size limit (1 MB) on any files read from the file system. 
     */
    public static final int OVERALL_FILE_SIZE_LIMIT = 1048576;
    
    /**
     * Configuration Manager Instance
     */
    private volatile static SpcfConfigurationManager sInstance = null;
    /**
     * Configuration Constants
     */
    private static ISpcfImmutableConfiguration sConstants = null;
    /**
     * Meta Configuration XML
     */
    private static String sMetaConfigurationXml = null;
    /**
     * Meta Configuration Constants
     */
    private static ISpcfImmutableConfiguration sMetaConfigurationConstants = null;
    /**
     * Provider Map
     */
    private static SpcfProviderMap sProviderMap = null;
    /**
     * Factory Instance
     */
    private static final SpcfFactory Factory = SpcfFactory.getInstance();
    /**
     * Configuration Cache Instance
     */
    private final SpcfMap<String, ISpcfConfiguration> ConfigurationCache = Factory
                    .<String, ISpcfConfiguration> createHashMap();
    /**
     * Synchronization Instance. has to use String (instead of Boolean or some other primitive types) in order to be
     * translatable
     */
    private static final String SyncObj = new String("SpcfConfigurationManager-Sync-Object");

    /**
     * Constructor
     */
    protected SpcfConfigurationManager()
    {
        // Constructor
    }

    /**
     * Sets the meta configuration which is an optional operation. Please see {@link #getInstance()}for default CMS
     * behavior if no meta configuration is explicitly set.
     * <p>
     * If this method is called, it must be called before {@link #getInstance()}is called. Also, this method is not
     * thread safe as initialization of the CMS should only be done once.
     * 
     * @param metaConfigXml The meta configuration in XML format. Note that this is NOT the location of the meta
     *            configuration file but the content of the meta configuration file in XML format. The portable APIs
     *            specifically do not take the path of the meta configuration file because file locations are most
     *            likely platform specific. Clients are expected to load the content of the meta configuration file in
     *            platform specific code and pass the content to portable code that uses the CMS.
     * @throws SpcfArgumentNullException if metaConfigXml is null
     * @throws SpcfIllegalStateException If method is called after {@link #getInstance()}has already been called at
     *             least once.
     */
    public static void setMetaConfiguration(String metaConfigXml)
    {
        SpcfParamValidator.checkIsNotNull(metaConfigXml, "metaConfigXml");
        if (sInstance != null)
        {
            throw new SpcfIllegalStateException(
                            "setMetaConfiguration can not be called after CMS instance has been initialized.");
        }
        sMetaConfigurationXml = metaConfigXml;
    }

    /**
     * Sets the meta configuration constants which is an optional operation. These constants will be used by CMS to
     * replace any constant references in the meta configuration xml content.
     * <p>
     * If this method is called, it must be called before {@link #getInstance()}is called. Also, this method is not
     * thread safe as initialization of the CMS should only be done once.
     * 
     * @param metaConfigConstants The meta configuration constants (Key, Value pair).
     * @throws SpcfArgumentNullException if metaConfigConstants is null
     * @throws SpcfIllegalStateException If method is called after {@link #getInstance()}has already been called at
     *             least once.
     */
    public static void setMetaConfigurationConstants(ISpcfImmutableConfiguration metaConfigConstants)
    {
        SpcfParamValidator.checkIsNotNull(metaConfigConstants, "metaConfigConstants");
        if (sInstance != null)
        {
            throw new SpcfIllegalStateException(
                            "setMetaConfigurationConstants can not be called after CMS instance has been initialized.");
        }
        sMetaConfigurationConstants = metaConfigConstants;
    }

    /**
     * Gets the meta configuration xml if set; otherwise, returns null.
     * 
     * @return the meta configuration xml if set; otherwise, returns null.
     */
    public static String getMetaConfiguration()
    {
        return sMetaConfigurationXml;
    }

    /**
     * Gets the instance of the configuration manager. Before calling this method, you can use
     * {@link #setMetaConfiguration(String)}to set the meta configuration data. If
     * {@link #setMetaConfiguration(String)}is not called, the following default behavior will be used to determine
     * meta configuration:
     * <p>
     * Default behavior for Java: <br>
     * When no meta configuration is explicitly specified, the CMS will perform the following logic to locate the
     * default meta configuration file: <br>
     * 1.) The CMS will check to see if a system property named &quot;spcf.cms.meta.config&quot; is defined. If so, the
     * value of this system property is used to locate the meta configuration. If &quot;spcf.cms.meta.config&quot; is
     * defined, it must point to a valid meta configuration file; otherwise, the CMS throws
     * SpcfCannotLoadMetaConfigException. Also, the value of &quot;spcf.cms.meta.config&quot; can be either relative or
     * absolute file paths. When relative file location is used, the file will be relative to the directory specified by
     * the &quot;app.home&quot; system property. If &quot;app.home&quot; is not specified, the file will be relative to
     * the execution directory of the Java application which varies from application to application, i.e. equivalent to
     * the &quot;user.dir&quot; set by the JVM. System properties can be passed in to the Java application via the
     * &quot;-D&quot; command line option when the application is first started up. <br>
     * 2.) If &quot;spcf.cms.meta.config&quot; is not defined, the CMS will use the classloader that loaded the CMS
     * classes to search through the CLASSPATH to look for all resources matching the name
     * &quot;conf/spcf-meta-config.xml&quot; on the CLASSPATH. There must be at least one
     * &quot;conf/spcf-meta-config.xml&quot; on the CLASSPATH. If none was found, the CMS throws
     * SpcfCannotLoadMetaConfigException. If multiple resources were found, CMS loads and merges all of them. Since the
     * classloader that loaded the CMS classes is used, it works only if the &quot;conf/spcf-meta-config.xml&quot; can
     * be seen by this classloader. So when multiple classloaders are used such as in web and J2EE applications, care
     * must be taken to make sure that the CMS is loaded by the classloader that can see the meta config file. <br>
     * 3.) If both 1 and 2 fails, the CMS throws SpcfCannotLoadMetaConfigException indicating that no default meta
     * configuration file can be found.
     * <p>
     * Default behavior for .NET: <br>
     * For .NET applications, when no meta configuration is specified, both the meta and module configurations can be in
     * the .NET application configuration file using .NET specific format. For instance, the &quot;configSections&quot;
     * section is used to store CMS Meta configuration data, and the sections after the &quot;configSections&quot; can
     * be used to store configurations for each module. <br>
     * For details, please see <a
     * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Default_CMS_Behavior_for_.NET_Applications">Default
     * CMS Behavior for .NET Applications </a>
     * 
     * @return The SpcfConfigurationManager instance.
     * @throws SpcfCannotLoadMetaConfigException If the meta configuration xml can not be loaded. For instance, no
     *             explicit meta configuration is set, and no default meta configuration can be found. Or there is an
     *             error loading one of the default meta configuration files.
     * @throws SpcfMetaConfigEntryMissingException If one or more required entry is missing in the meta configuration.
     *             For instance, the file attribute for the xml configuration provider is missing.
     * @throws SpcfMetaConfigEntryInvalidException If one or more entry in the meta configuration is invalid. For
     *             instance, duplicate module IDs were found or the provider class specified does not implement
     *             {@link ISpcfConfigurationProvider}.
     * @throws SpcfConfigInvalidException if one or more configuration entry in the constants section is invalid.
     * @throws SpcfConfigModuleException If the SpcfConfigurationManager instance can not be instantiated. For instance,
     *             the specific configuration manager class can not be instantiated because the runtime does not have
     *             the correct dependencies.
     */
    public static SpcfConfigurationManager getInstance()
    {
        if (sInstance == null)
        {
            synchronized (SyncObj) // declare the method synchronized does not translate correctly
            {
                if (sInstance == null)
                {
                    sInstance = createInstance(true);
                }
            }
        }
        return sInstance;
    }

    /**
     * Tries to get the instance of the configuration manager. Before calling this method, you may use
     * {@link #setMetaConfiguration(String)}to set the meta configuration data.
     * <p>
     * Please see {@link #getInstance()} for details on the default behavior of the CMS to determine meta configuration
     * if {@link #setMetaConfiguration(String)} is not called.<br>
     * But please NOTE: No exceptions will be thrown in case of any failures while getting the instance. Null instance
     * will be returned instead of exceptions being thrown. <br>
     * In order to get exceptions while getting an instance, please use {@link #getInstance()} instead.<br>
     * <p>
     * 
     * @return The SpcfConfigurationManager instance.
     */
    public static SpcfConfigurationManager tryGetInstance()
    {
        if (sInstance == null)
        {
            synchronized (SyncObj) // declare the method synchronized does not translate correctly
            {
                if (sInstance == null)
                {
                    sInstance = createInstance(false);
                }
            }
        }
        return sInstance;
    }

    /**
     * To create an instance of the configuration manager
     * 
     * @param reThrowExceptions flag to re-throw exceptions
     * @return Configuration Manager Instance
     */
    private static SpcfConfigurationManager createInstance(boolean reThrowExceptions)
    {
        SpcfConfigurationManager instance = null;
        try
        {
            instance = new com.intuit.spc.foundations.primarySpecific.config.SpcfConfigurationManagerImpl();
            instance.init(sMetaConfigurationXml, sMetaConfigurationConstants);
            sConstants = instance.getConfigurationConstants();
            sProviderMap = instance.getProviders();
            System.out.println("SpcfConfigurationManager createInstance, status=success");
        }
        catch (SpcfConfigModuleException exp)
        {
            System.out.println("SpcfConfigModuleException in createInstance, status=failed, reason=\n" + getStackTrace(exp));
            if (reThrowExceptions)
                throw exp;
            else
                return null;
        }
        catch (Exception e)
        {
            System.out.println("Exception in createInstance, status=failed, reason=\n" + getStackTrace(e));
            if (reThrowExceptions)
                throw new SpcfConfigInitException("Unable to create and initialize a configuration manager instance: "
                                + e.toString(), e);
            else
                return null;
        }
        return instance;
    }

    /**
     * Returns true if the moduleId is configured in meta configuration; false otherwise. Use this method before calling
     * the get and store configuration methods to avoid SpcfMetaConfigEntryMissingException when the moduleId is not
     * found in meta configuration.
     * 
     * @param moduleId moduleId A string representing the id of the module, cannot be null.
     * @return true if the moduleId is configured in meta configuration; false otherwise.
     * @throws SpcfArgumentNullException If moduleId is null
     */
    public boolean hasModuleId(String moduleId)
    {
        SpcfParamValidator.checkIsNotNull(moduleId, "moduleId");
        return sProviderMap.hasProvider(moduleId);
    }

    /**
     * Gets the configuration settings for the given module Id. This method caches configurations for a given moduleId
     * so that subsequent calls are faster. This configuration cache will be refreshed each time configurations are
     * reloaded or stored for the module. That is, a call to {@link #reload()},{@link #reload(String)}, or
     * {@link #storeConfiguration(String, ISpcfConfiguration)}will automatically refresh this internal configuration
     * cache.
     * <p>
     * The configuration object returned is a collection configuration {@link SpcfAggregateConfiguration} which would
     * consist of configurations in a list from different configuration providers (as per configured in the meta
     * config). Depending on the returnCopy option set in the Meta Configuration, Each configuration object in the list
     * could be either a copy (which is obtained using {@link ISpcfConfiguration#getCopy()}) or the same configuration
     * object in the cache. If returnCopy is set true, changes made to the contained configuration object will not
     * affect the internal cache. If returnCopy is set false, changes made to the contained configuration will affect
     * the internal cache directly.
     * <p>
     * Please click <a
     * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Configuration_returned_from_CMS_-_Aggregate_Configuration">here</a>
     * to know more on the configuration object being returned by the Configuration Manager.<br>
     * Also, the configuration object returned is not thread safe.
     * 
     * @param moduleId A string representing the id of the module, cannot be null.
     * @return the configuration object for the module, not thread safe.
     * @throws SpcfArgumentNullException If moduleId is null
     * @throws SpcfMetaConfigEntryMissingException If the moduleId does not exist in the meta configuration file.
     * @throws SpcfConfigInvalidException If one or more configuration entries are invalid.
     * @throws SpcfConfigModuleException If the configurations can not be loaded from persistent storage for other
     *             reasons, such as IO errors for file based configurations, connection errors for database based
     *             configurations, etc.
     */
    public ISpcfConfiguration getConfiguration(String moduleId)
    {
        checkModuleIdExists(moduleId);
        ISpcfConfiguration config = null;
        try
        {
            ISpcfConfiguration cachedConfig = ConfigurationCache.getItem(moduleId);
            if (cachedConfig == null)
            {
                cachedConfig = loadAndCache(moduleId);
            }
            config = createAggregateConfiguration(cachedConfig);
        }
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            SpcfStringBuilder sb = Factory
                            .createStringBuilder("An unexpected error occurred while trying to get configuration for moduleId=");
            sb.append(moduleId);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfProviderLoadException(sb.toString(), e);
        }
        return config;
    }

    /*******************************************************************************************************************
     * Returns a new aggregate configuration from the cache. The creation process will take care checking the returnCopy
     * of each of the configuration provider in order whether to add the same configuration from the cache or a copy of
     * it into the aggregate configuration.
     * 
     * @param cachedConfig
     * @return Aggregate configuration
     */
    private ISpcfConfiguration createAggregateConfiguration(ISpcfConfiguration cachedConfig)
    {
        ISpcfConfiguration config = new SpcfAggregateConfiguration();
        SpcfList<ISpcfConfiguration> configList = ((SpcfAggregateConfiguration) cachedConfig).getConfigurationList();
        int size = configList.getSize();
        for (int i = 0; i < size; i++)
        {
            SpcfCachedConfiguration individualCachedConfig = (SpcfCachedConfiguration) configList.getItem(i);
            ISpcfConfiguration individualConfig = individualCachedConfig.getConfiguration();
            if (individualCachedConfig.getConfigurationProvider().getReturnCopyRequired()
                            && !(individualConfig instanceof ISpcfLazyConfiguration))
            {
                // Create a copy of the configuration and add it to the aggregate configuration
                ISpcfConfiguration copy = individualConfig.getCopy();
                if (copy instanceof ISpcfLazyConfiguration)
                {
                    ((ISpcfLazyConfiguration) copy).setConfigurationProvider(individualCachedConfig
                                    .getConfigurationProvider());
                }
                copy.setModuleID(individualConfig.getModuleID());
                ((SpcfAggregateConfiguration) config).addConfiguration(copy);
            }
            else
                // Add the same instance itself.
                ((SpcfAggregateConfiguration) config).addConfiguration(individualConfig);
        }
        return config;
    }

    /**
     * Stores the configuration for the module id. If store is successful, configuration change listeners for moduleId
     * will be notified. Please make sure that config is not being modified concurrently by another thread while this
     * method is called; otherwise, the actual values stored can not be predicated.
     * <p>
     * This API <b>expects</b> {@link SpcfAggregateConfiguration} (which is returned at {@link #reload(String)}, or
     * {@link #getConfiguration(String)}) to be passed as the config to be stored. CMS will take care of saving the
     * last configuration contained in the {@link SpcfAggregateConfiguration} to the last configuration provider. If
     * configuration instance other than {@link SpcfAggregateConfiguration} is passed, CMS will still try to store the
     * passed configuration instance through the last configuration provider.
     * </p>
     * <p>
     * This method also refreshes the internal configuration cache for the moudleId if and only if the store is
     * successful. Please see {@link #getConfiguration(String)}for details on CMS caching strategy.
     * 
     * @param moduleId A string representing the id of the module, cannot be null.
     * @param config The configuration to store
     * @throws SpcfArgumentNullException if moduleId or config is null
     * @throws SpcfMetaConfigEntryMissingException If the moduleId does not exist in the meta configuration file or is
     *             not associated with a valid configuration provider.
     * @throws SpcfMetaConfigEntryInvalidException If the last configuration provider configured in the meta
     *             configuration for moduleId does not support writable configuration.
     * @throws SpcfConfigInvalidException If one or more configuration entries are invalid.
     * @throws SpcfConfigChangeNotificationException if at least one configuration change listener throws an unexpected
     *             exception. In this case, the listener will be removed from the CMS.
     * @throws SpcfConfigModuleException if the configurations can not be stored successfully. For instance, the
     *             configuration storage is read only or locked by another process.
     */
    public void storeConfiguration(String moduleId, ISpcfConfiguration config)
    {
        SpcfParamValidator.checkIsNotNull(config, "config");
        checkModuleIdExists(moduleId);
        try
        {
            storeLastConfiguration(moduleId, config);
            ISpcfImmutableConfiguration oldConfig = getCachedConfiguration(moduleId);
            ISpcfImmutableConfiguration newConfig = config;
            loadAndCache(moduleId);
            SpcfConfigurationChangeNotifier.notifyConfigurationListeners(moduleId, oldConfig, newConfig);
        }
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            SpcfStringBuilder sb = Factory
                            .createStringBuilder("An unexpected error occurred while trying to store configuration for moduleId=");
            sb.append(moduleId);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfProviderUpdateException(sb.toString(), e);
        }
    }

    /**
     * To get cached configuration
     * 
     * @param moduleId Module Id
     * @return cached configuration
     */
    private ISpcfConfiguration getCachedConfiguration(String moduleId)
    {
        ISpcfConfiguration cachedConfig = ConfigurationCache.getItem(moduleId);
        if (cachedConfig == null)
            return null;
        ISpcfConfiguration config = new SpcfAggregateConfiguration();
        SpcfList<ISpcfConfiguration> configList = ((SpcfAggregateConfiguration) cachedConfig).getConfigurationList();
        int size = configList.getSize();
        for (int i = 0; i < size; i++)
        {
            SpcfCachedConfiguration individualCachedConfig = (SpcfCachedConfiguration) configList.getItem(i);
            ISpcfConfiguration individualConfig = individualCachedConfig.getConfiguration();
            ((SpcfAggregateConfiguration) config).addConfiguration(individualConfig);
        }
        return config;
    }

    /*******************************************************************************************************************
     * To store the last configuration through its configuration provider.
     * 
     * @param moduleID Module ID
     * @param config Configuration
     */
    private void storeLastConfiguration(String moduleID, ISpcfConfiguration config)
    {
        if (config instanceof SpcfAggregateConfiguration)
        {
            SpcfList<ISpcfConfiguration> configList = ((SpcfAggregateConfiguration) config).getConfigurationList();
            int size = configList.getSize();
            ISpcfConfiguration individualConfig = configList.getItem(size - 1);
            ISpcfConfigurationProvider provider = sProviderMap.getProviderForUpdate(moduleID);
            provider.store(individualConfig);
        }
        else
        {
            ISpcfConfigurationProvider provider = sProviderMap.getProviderForUpdate(moduleID);
            provider.store(config);
        }
    }

    /**
     * Reloads configuration settings for the given moduleId from permanent storages and returns a non-thread safe
     * configuration object. The in memory configuration settings for moduleId that are not commited to persistent
     * storage will be lost. Notifies all configuration change listeners for the moduleId if a configuration change
     * happened for the module.
     * <p>
     * This method refreshes the internal configuration cache for the moduleId. Please see
     * {@link #getConfiguration(String)}for details on CMS caching strategy.
     * </p>
     * Please click <a
     * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Configuration_returned_from_CMS_-_Aggregate_Configuration">here</a>
     * to know more on the configuration object being returned by the Configuration Manager.<br>
     * 
     * @param moduleId A string representing the id of the module, cannot be null.
     * @return the configuration object for the module, not thread safe.
     * @throws SpcfArgumentNullException if moduleId is null
     * @throws SpcfMetaConfigEntryMissingException If the moduleId does not exist in the meta configuration file.
     * @throws SpcfConfigInvalidException If one or more configuration entries are invalid.
     * @throws SpcfConfigChangeNotificationException if at least one configuration change listener throws an unexpected
     *             exception. In this case, the listener will be removed from the CMS.
     * @throws SpcfConfigModuleException if the configurations can not be loaded successfully.
     */
    public ISpcfConfiguration reload(String moduleId)
    {
        checkModuleIdExists(moduleId);
        ISpcfConfiguration returnConfig = null;
        try
        {
            // Getting the old config from the cache
            ISpcfImmutableConfiguration oldConfig = getCachedConfiguration(moduleId);
            // Getting the new config from by loading and caching it
            ISpcfConfiguration newConfig = loadAndCache(moduleId);
            // Notifying the listeners of this module
            SpcfConfigurationChangeNotifier.notifyConfigurationListeners(moduleId, oldConfig, newConfig);
            // Create an aggregate configuration and return it back.
            returnConfig = createAggregateConfiguration(newConfig);
        }
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            SpcfStringBuilder sb = Factory
                            .createStringBuilder("An unexpected error occurred while trying to load configuration for moduleId=");
            sb.append(moduleId);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfProviderLoadException(sb.toString(), e);
        }
        return returnConfig;
    }

    /**
     * Reloads all configuration settings from permanent storages. All in memory configuration settings that are not
     * commited to persistent storage will be lost. Also notifies configuration change listeners for all modules where a
     * configuration change occurred.
     * <p>
     * This method refreshes the internal configuration cache for all modules. Please see
     * {@link #getConfiguration(String)}for details on CMS caching strategy.
     * </p>
     * Please click <a
     * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Configuration_returned_from_CMS_-_Aggregate_Configuration">here</a>
     * to know more on the configuration object being returned by the Configuration Manager.<br>
     * 
     * @throws SpcfConfigInvalidException If one or more configuration entries are invalid.
     * @throws SpcfConfigChangeNotificationException if at least one configuration change listener throws an unexpected
     *             exception. In this case, the listener will be removed from the CMS.
     * @throws SpcfConfigModuleException If the configurations can not be loaded successfully.
     */
    public void reload()
    {
        try
        {
            SpcfCollection<String> moduleIds = sProviderMap.getModuleIds();
            for (ISpcfIterator<String> it = moduleIds.getIterator(); it.hasNext();)
            {
                String moduleId = it.next();
                reload(moduleId);
            }
        }
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SpcfProviderLoadException(
                            "An unexpected error occurred while trying to load configuration for all modules: "
                                            + e.toString(), e);
        }
    }

    /*******************************************************************************************************************
     * Loads the configuration from the Configuration Providers and caches it.
     * 
     * @param moduleId Module Id
     * @return configuration
     */
    private ISpcfConfiguration loadAndCache(String moduleId)
    {
        // Get all the config providers for the module
        SpcfList<ISpcfConfigurationProvider> providers = sProviderMap.getProviders(moduleId);
        int size = providers.getSize();
        SpcfAggregateConfiguration config = new SpcfAggregateConfiguration();
        // Get configuration out from each and every configuration provider
        for (int i = 0; i < size; i++)
        {
            ISpcfConfigurationProvider provider = providers.getItem(i);
            ISpcfConfiguration singleConfig = provider.load(sConstants);
            if (singleConfig == null)
            {
                throw new SpcfConfigInvalidException("Null configuration returned from the configuration provider for"
                                + " the module " + moduleId);
            }
            else
            {
                singleConfig.setModuleID(moduleId);
                checkForLazyLoad(provider, singleConfig, moduleId);
                // Create a wrapper configuration to be stored in the cache
                SpcfCachedConfiguration cachedConfig = new SpcfCachedConfiguration(singleConfig);
                // Setting the config provider, module id information into the configuration.
                cachedConfig.setConfigurationProvider(provider);
                // Add the config into the collection
                config.addConfiguration(cachedConfig);
            }
        }
        // Add the aggregate configuration into the cache
        ConfigurationCache.add(moduleId, config);
        return config;
    }

    /**
     * To check for lazy load
     * 
     * @param provider Provider
     * @param singleConfig Configuration
     * @param moduleId Module Id
     * @throws SpcfConfigInvalidException If the configuration or configuration provider is invalid for lazy load
     */
    protected void checkForLazyLoad(ISpcfConfigurationProvider provider, ISpcfConfiguration singleConfig,
                    String moduleId)
    {
        if ((provider instanceof ISpcfConfigurationEntryProvider && !(singleConfig instanceof ISpcfLazyConfiguration)))
        {
            throw new SpcfConfigInvalidException(
                            "Configuration Provider implementing "
                                            + "ISpcfConfigurationEntryProvider must return a Configuration which implements ISpcfLazyConfiguration in addition to ISpcfConfiguration."
                                            + " Module " + moduleId);
        }
        else if (singleConfig instanceof ISpcfLazyConfiguration
                        && !(provider instanceof ISpcfConfigurationEntryProvider))
        {
            throw new SpcfConfigInvalidException("Configuration implementing ISpcfLazyConfiguration in addition to "
                            + "ISpcfConfiguration must be returned only by a Configuration Provider which implements "
                            + "ISpcfConfigurationEntryProvider." + " Module " + moduleId);
        }
        // Set the config provider only if the config provider supports lazy loading
        if (provider instanceof ISpcfConfigurationEntryProvider && singleConfig instanceof ISpcfLazyConfiguration)
        {
            ((ISpcfLazyConfiguration) singleConfig).setConfigurationProvider(provider);
        }
    }

    /**
     * To check whether the module exists or not
     * 
     * @param moduleId Module Id
     */
    private void checkModuleIdExists(String moduleId)
    {
        if (!hasModuleId(moduleId))
        {
            throw new SpcfMetaConfigEntryMissingException(moduleId, "Missing provider for module '" + moduleId + "'.");
        }
    }

    /**
     * To initialize the configuration manager.
     * 
     * @param metaConfigXml Meta Configuration xml
     * @param constants Meta Configuration Constants
     * @throws SpcfConfigModuleException If any exceptions happens during initialization
     */
    protected abstract void init(String metaConfigXml, ISpcfImmutableConfiguration constants)
                    throws SpcfConfigModuleException;

    /**
     * To get the providers
     * 
     * @return Map of Providers
     * @throws SpcfConfigModuleException If any exceptions happen while getting the providers
     */
    protected abstract SpcfProviderMap getProviders() throws SpcfConfigModuleException;

    /**
     * To get the Configuration Constants
     * 
     * @return Immutable Configuration containing Configuration Constants
     * @throws SpcfConfigModuleException If any exceptions happen while getting the constants.
     */
    protected abstract ISpcfImmutableConfiguration getConfigurationConstants() throws SpcfConfigModuleException;

    /**
     * Get source config filename
     *
     * @return source config filename
     * @throws SpcfConfigModuleException If any exceptions happen while getting the filename.
     */
    public abstract String getSourceFilename(String moduleId) throws SpcfConfigModuleException;

    /**
     * WARNING: This method is for testing only!!!! Do NOT call!
     */
    static void testOnlyResetInstance()
    {
        synchronized (SyncObj)
        {
            sInstance = null;
            sMetaConfigurationXml = null;
            sMetaConfigurationConstants = null;
        }
    }
}