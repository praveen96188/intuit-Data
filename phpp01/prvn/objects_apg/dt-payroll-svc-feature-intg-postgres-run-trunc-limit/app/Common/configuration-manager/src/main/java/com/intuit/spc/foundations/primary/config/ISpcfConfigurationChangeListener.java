package com.intuit.spc.foundations.primary.config;

/**
 * An interface provided for clients who are interested in being notified of configuration
 * changes. Clients should implement this interface and call the
 * {@link com.intuit.spc.foundations.primary.config.SpcfConfigurationChangeNotifier#addListener(String, ISpcfConfigurationChangeListener)}
 * method to register the listener.
 * <p>
 * Note that it is not the responsibility of the CMS to actively  monitor 
 * persistent configuration storages and determine when configurations  have changed.
 * The Configuration Change Notifier detects a configuration change only if one of the {@link com.intuit.spc.foundations.primary.config.SpcfConfigurationManager#reload(String)}
 * and {@link com.intuit.spc.foundations.primary.config.SpcfConfigurationManager#storeConfiguration(String, ISpcfConfiguration)} of CMS are called, either by the client application or  some
 * other external component. 
 * </p>
 * <p>
 * Configuration Change Notifier may also get notified explicitly outside of Configuration Manager. This explicit
 * notification is useful when a custom configuration wants to notify the listeners of any
 * changes in the individual configuration entries.
 * For more info go to:
 * <a
 * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Implementing_Lazy_Load.2FSave">SPCF
 * Configuration Management Lazy Load/Save Users Guide </a>
 * </p>
 * 
 */
public interface ISpcfConfigurationChangeListener
{
    /**
     * This method is called when the CMS detects a configuration change for the moduleId
     * that the listener is interested in.
     * 
     * @param moduleId A string reprenting the id of the module whose configuration changed.
     * @param newConfig The new configuration after the change
     */
    void onChange(String moduleId, ISpcfImmutableConfiguration newConfig);    
    
    /**
     * This method is called only when SpcfConfigurationChangeNotifier gets explicit notification of
     * changes in the Configuration Entry. CMS will not automatically notify changes in individual
     * configuration entries. 
     * 
     * <p>
     * CMS supports lazy load, i.e) you can create a custom proxy configuration which would 
     * direct the call for get/set to the Configuration Entry Provider ( a Configuration
     * Provider which implements ISpcfConfigurationEntryProvider in addition to ISpcfConfigurationProvider)
     * and make the proxy configuration to notify the SpcfConfigurationChangeNotifier of the configuration 
     * changes.
     * 
     * For more info go to:
     * <a
     * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Implementing_Lazy_Load.2FSave">SPCF
     * Configuration Management Lazy Load/Save Users Guide </a>
     * 
     * </p>
     * 
     * @param moduleId A string reprenting the id of the module in which a configuration is changed.
     * @param configurationKey Key of the configuration value which is changed
     * @param newValue The new value of the configuration which is changed 
     */
    void onChange(String moduleId, String configurationKey, Object newValue);
}


