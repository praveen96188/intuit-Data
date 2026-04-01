package com.intuit.sbd.payroll.psp.configuration;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigurationManager;
import com.intuit.spc.foundations.primarySpecific.logging.SpcfLogManagerImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * @author achaves
 *         Date: Feb 3, 2008
 *         Time: 10:23:20 PM
 */
public class ConfigurationManager {
    private static SpcfConfigurationManager configManager = null;

    /**
     * Relative path to the local directory that contains SPCF configuration files
     */
    private static final String CONF_DIR_LOCAL = "target/conf/";

    /**
     * Relative path to the jboss directory that contains SPCF configuration files
     */
    private static final String CONF_DIR_WEB_APP = "psp/conf/";

    /**
     * Relative path to meta-config file.
     */
    private static final String MetaCfgFile = "spcf-meta-conf.xml";

    /**
     * Relative path to component config file.
     */
    private static final String ComponentCfgFile = "object-service-component-factory-conf.xml";

    private static void initialize(String metadataConfigFile) {
        // Configure Configuration Manager.
        System.setProperty("spcf.cms.meta.config", metadataConfigFile);

        configManager = SpcfConfigurationManager.getInstance();

        // Configure Log Manager.
        SpcfLogManagerImpl.setConfiguration(configManager);
    }

    public static SpcfConfigurationManager getConfigurationManager() {
        if (configManager == null) {
            initialize(getMetaCfgFileWithPath());
        }

        return configManager;
    }

    public static String getSettingValue(ConfigurationModule pModule, String pSettingName) {
       return getConfiguration(pModule.moduleId).getString(pSettingName);
    }

    public static String getSettingValue(ConfigurationModule pModule, String pSettingName, String pDefaultValue) {
       return getConfiguration(pModule.moduleId).getString(pSettingName, pDefaultValue);
    }

    public static String getSettingValue(final String pModuleId, String pSettingName) {
       return ConfigurationManager.getConfiguration(pModuleId).getString(pSettingName);
    }

    public static String getSettingValue(final String pModuleId, String pSettingName, String pDefaultValue) {
       return ConfigurationManager.getConfiguration(pModuleId).getString(pSettingName, pDefaultValue);
    }


    public static ISpcfImmutableConfiguration getSettings(ConfigurationModule pModule) {
       return getConfiguration(pModule.moduleId);
    }

    public static ISpcfImmutableConfiguration getConfiguration(ConfigurationModule pModule) {
        return getConfiguration(pModule.moduleId);
    }

    /**
     * Gets the proxied configuration for the given module - the proxy manages encrypted properties
     * (encrypted properties will be returned as unencrypted strings)
     * <p/>
     *
     * @param pModuleId
     * @return
     */
    public static ISpcfImmutableConfiguration getConfiguration(final String pModuleId) {
        return new ConfigurationProxy(getConfigurationManager().getConfiguration(pModuleId).toImmutable());
    }

    public static ISpcfImmutableConfiguration getNonProxiedConfiguration(ConfigurationModule pModule) {
        return getNonProxiedConfiguration(pModule.moduleId);
    }

    /**
     * Gets the non-proxied configuration for the given module
     * (encrypted properties will be returned as unaltered encrypted strings)
     * <p/>
     *
     * @param pModuleId
     * @return
     */
    public static ISpcfImmutableConfiguration getNonProxiedConfiguration(final String pModuleId) {
        return getConfigurationManager().getConfiguration(pModuleId).toImmutable();
    }

    public static Properties getConfigurationProperties(ConfigurationModule pModule) {
        return getConfigurationProperties(pModule.moduleId);
    }

    /**
     * Retrieves all configuration properties, returning them in a Properties object.  This method will also decrypt
     * any encrypted properties from the properties file.
     * @param pModuleId
     * @return
     */
    public static Properties getConfigurationProperties(final String pModuleId) {
        Properties props = new Properties();
        ISpcfImmutableConfiguration config = getConfiguration(pModuleId);
        ISpcfIterator<String> iter = config.getKeys();
        String key;

        while (iter.hasNext()) {
            key = iter.next();
            props.setProperty(key, config.getString(key)); // decrypts property if necessary
        }

        return props;
    }

    /**
     * Returns relative path to the configuration file.
     * Different paths are used depending if an app is run locally or in jBoss.
     * If file is not found a runtime exception is thrown.
     *
     * @param fileName
     * @return
     */
    public static final String getConfigFilePath(String fileName) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ResourceLoader resourceLoader = resolver.getResourceLoader();
        String configFilePath = null;

        // First, let's check if the config file is in the root of the working directory
        Resource resource = resourceLoader.getResource("file:" + fileName);
        if (resource.exists()) {
            try {
                configFilePath = resource.getFile().getAbsolutePath();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else {
            // If the config file doesn't exist in the root directory
            // check if it exists in the predefined DEV or JBOSS config directories

            String jBossLibUrl = System.getProperty("jboss.server.lib.url");
            System.out.println("jboss.server.lib.url=" + jBossLibUrl);

            if (jBossLibUrl != null && jBossLibUrl.length() > 0) {
                String fileUrl = jBossLibUrl + CONF_DIR_WEB_APP + fileName;
                resource = resourceLoader.getResource(fileUrl);
            }
            else {
                /*
                *  For Local Mac, externalized conf dir to env variable -> conf.dir.local
                *  So, absolute path can be parameterized with env variable
                *  example: -Dconf.dir.local="~/psp/PSE/configuration/src/main/resources/local/jboss-conf/"
                */
                String confLocalPath = System.getProperty("conf.dir.local");
                confLocalPath = StringUtils.isEmpty(confLocalPath) ? CONF_DIR_LOCAL : confLocalPath;
                String relativeConfigFileName = confLocalPath + fileName;
                resource = resourceLoader.getResource("file:" + relativeConfigFileName);
            }

            if (resource.exists()) {
                try {
                    configFilePath = resource.getFile().getAbsolutePath();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }

        if (configFilePath == null) {
            System.out.println("File not found: " + fileName);
            throw new RuntimeException("File not found: " + fileName);
        }

        System.out.println("Config file path: "+configFilePath);
        return configFilePath;
    }

    public static String getMetaCfgFileWithPath() {
        return ConfigurationManager.getConfigFilePath(MetaCfgFile);
    }

    public static String getComponentCfgFileWithPath() {
        return ConfigurationManager.getConfigFilePath(ComponentCfgFile);
    }

    public static String getEnvironmentIdentifier() {
        String env = ConfigurationManager.getSettingValue(DatabaseConfigManager.MonolithDbToken, "dataAccess.env");
        System.out.println("EnvironmentIdentifier=" + env);
        if (!StringUtils.isEmpty(env)) {
            return env.toLowerCase();
        }
        return "local";
    }

    public static boolean containsKey(String pModuleId, String key) {
        return getConfiguration(pModuleId).containsKey(key);
    }

    public static void ensureInitialization() {
        getConfigurationManager();
    }
}
