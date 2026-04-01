package com.intuit.spc.foundations.primarySpecific.config;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Objects;

import com.intuit.spc.foundations.portability.collections.SpcfList;
import org.apache.commons.lang.StringUtils;
import org.exolab.castor.xml.Unmarshaller;

import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfConfigurationProvider;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigInitException;
import com.intuit.spc.foundations.primary.config.SpcfConfigModuleException;
import com.intuit.spc.foundations.primary.config.SpcfConfigurationManager;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryInvalidException;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.util.SpcfAggregateConfiguration;
import com.intuit.spc.foundations.primary.config.util.SpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.util.SpcfProviderMap;
import com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider;
import com.intuit.spc.foundations.primarySpecific.config.meta.Constants;
import com.intuit.spc.foundations.primarySpecific.config.meta.MetaConfig;
import com.intuit.spc.foundations.primarySpecific.config.meta.Module;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Implementation for SpcfConfigurationManager.
 * 
 * @see com.intuit.spc.foundations.primary.config.SpcfConfigurationManager
 * @author yzhang [Created on Jun 6, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/SpcfConfigurationManagerImpl.java#1 $
 */
public class SpcfConfigurationManagerImpl extends SpcfConfigurationManager
{
    /**
     * Meta configuration Unmarshaller
     */
    private Unmarshaller mMetaConfigUnmarshaller = null;
    /**
     * Meta Configuration
     */
    private MetaConfig mMetaConfig = null;

    /**
     * Constructs an instance using the default constructor
     */
    public SpcfConfigurationManagerImpl()
    {
        mMetaConfigUnmarshaller = new Unmarshaller(MetaConfig.class);
        mMetaConfigUnmarshaller.setValidation(true);
    }

    /**
     * To get meta configuration from xml
     * 
     * @param xml Xml
     * @return Meta configuration
     */
    protected MetaConfig getMetaConfigFromXml(String xml)
    {
        try
        {
            Object obj = mMetaConfigUnmarshaller.unmarshal(new StringReader(xml.trim()));
            return (MetaConfig) obj;
        }
        catch (Exception e)
        {
            throw new SpcfMetaConfigEntryInvalidException("Invalid meta configuration: " + e.getMessage(), e);
        }
    }

    /**
     * To initialize the manager
     * 
     * @param metaConfigXml Meta configuration Xml
     * @param metaConfigConstants Meta configuration Constants
     * @throws SpcfMetaConfigEntryMissingException
     * @throws SpcfMetaConfigEntryInvalidException
     * @throws SpcfConfigModuleException
     */
    @Override
    protected void init(String metaConfigXml, ISpcfImmutableConfiguration metaConfigConstants)
                    throws SpcfMetaConfigEntryMissingException, SpcfMetaConfigEntryInvalidException,
                    SpcfConfigModuleException
    {
        if (StringUtils.isBlank(metaConfigXml))
        {
            // load default meta configuration
            this.mMetaConfig = SpcfConfigFileUtils.getDefaultMetaConfig(metaConfigConstants, this.getClass()
                            .getClassLoader());
        }
        else
        {
            if (metaConfigConstants != null)
            {
                metaConfigXml = SpcfConfigUtils.expandConstant(metaConfigConstants, metaConfigXml);
                if (metaConfigXml == null)
                {
                    throw new SpcfMetaConfigEntryInvalidException(
                                    "Meta Configuration has invalid constant reference(s)");
                }
            }
            // Expanding system properties if any.
            try
            {
                metaConfigXml = SpcfConfigUtils.expandSystemProperties(metaConfigXml);
            }
            catch (Exception ex)
            {
                throw new SpcfMetaConfigEntryInvalidException(
                                "Meta configuration contains a system property reference which cannot be processed.",
                                ex);
            }
            this.mMetaConfig = getMetaConfigFromXml(metaConfigXml);
        }
        checkForDuplicateModuleIds(mMetaConfig);
    }

    /**
     * To get configuration constants
     * 
     * @return immutable configuration constants
     * @throws SpcfConfigModuleException
     */
    @Override
    protected ISpcfImmutableConfiguration getConfigurationConstants() throws SpcfConfigModuleException
    {
        Constants constants = this.mMetaConfig.getConstants();
        if (constants == null)
        {
            return SpcfImmutableConfiguration.Empty;
        }
        int providerCount = constants.getConfigProviderCount();
        SpcfAggregateConfiguration config = new SpcfAggregateConfiguration();
        for (int i = 0; i <= providerCount - 1; i++)
        {
            ConfigProvider configProvider = constants.getConfigProvider(i);
            ISpcfConfigurationProvider provider = SpcfConfigurationProviderFactory
                            .getConfigurationProvider(configProvider);
            ISpcfConfiguration constantConfig = provider.load(null);
            checkForLazyLoad(provider, constantConfig, "Constants");
            config.addConfiguration(constantConfig);
        }
        return config.toImmutable();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected SpcfProviderMap getProviders() throws SpcfMetaConfigEntryMissingException,
                    SpcfMetaConfigEntryInvalidException, SpcfConfigInitException
    {
        SpcfProviderMap providerMap = new SpcfProviderMap();
        // castor makes sure that at least one configuration provider is present in the xml
        int moduleCount = mMetaConfig.getModuleCount();
        for (int i = 0; i < moduleCount; i++)
        {
            Module module = mMetaConfig.getModule(i);
            createConfigProvdersForModule(module, providerMap);
        }
        return providerMap;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSourceFilename(String moduleId) throws SpcfConfigModuleException {
        SpcfProviderMap providerMap = getProviders();

        if (Objects.isNull(providerMap) || !providerMap.hasProvider(moduleId)) {
            return null;
        }

        SpcfList<ISpcfConfigurationProvider> providers = providerMap.getProviders(moduleId);
        if (providers.isEmpty()) {
            return null;
        }

        ISpcfConfigurationProvider provider = providers.getItem(0);
        if (provider != null && isNotBlank(provider.getConfigurationSourceName())) {
            return provider.getConfigurationSourceName();
        }
        return null;
    }

    /**
     * To check for duplicate module ids
     * 
     * @param metaConfig Meta configuration
     */
    private static void checkForDuplicateModuleIds(MetaConfig metaConfig)
    {
        HashSet<String> moduleIdChecker = new HashSet<>();
        int moduleCount = metaConfig.getModuleCount();
        for (int i = 0; i < moduleCount; i++)
        {
            Module module = metaConfig.getModule(i);
            String moduleId = module.getId();
            if (moduleIdChecker.contains(moduleId))
            {
                throw new SpcfMetaConfigEntryInvalidException("Duplicate module id found in meta configuration: "
                                + moduleId);
            }
            moduleIdChecker.add(moduleId);
        }
    }

    /**
     * To Create Configuration Provider
     * 
     * @param module Module
     * @param providerMap Provider Map
     */
    private static void createConfigProvdersForModule(Module module, SpcfProviderMap providerMap)
    {
        String moduleId = module.getId();
        int providerCount = module.getConfigProviderCount();
        if (providerCount == 0)
        {
            throw new SpcfMetaConfigEntryMissingException(module.getId(),
                            "At least one configuration provider is required for module '" + module.getId() + "'.");
        }
        for (int i = 0; i < providerCount; i++)
        {
            ConfigProvider configProvider = module.getConfigProvider(i);
            ISpcfConfigurationProvider provider = SpcfConfigurationProviderFactory
                            .getConfigurationProvider(configProvider);
            providerMap.addProvider(moduleId, provider);
        }
    }
}