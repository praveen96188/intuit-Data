package com.intuit.spc.foundations.primary.config;

/**
 * This interface has to be implemented by configurations which are used for 
 * lazy load and save. This has to be implemented in addition to ISpcfConfiguration.
 * 
 * For more info go to:
 * <a
 * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Implementing_Lazy_Load.2FSave">SPCF
 * Configuration Management Lazy Load/Save Users Guide </a>
 * 
 */
public strictfp interface ISpcfLazyConfiguration
{
        
    /**
     * Gets the Configuration Provider which produced this configuration. Null will be returned
     * if this configuration is not created by any providers.
     * @return Configuration Provider
     */
    ISpcfConfigurationProvider getConfigurationProvider();
    
    /**
     * Sets the Configuration Provider which produced this configuration. Configuration
     * Manager will take care of setting the provider for a configuration while 
     * loading the configuration from the Configuration Provider. So, may not be used 
     * any where else.
     * @param configurationProvider Configuration Provider
     */
    void setConfigurationProvider(ISpcfConfigurationProvider configurationProvider);            
}