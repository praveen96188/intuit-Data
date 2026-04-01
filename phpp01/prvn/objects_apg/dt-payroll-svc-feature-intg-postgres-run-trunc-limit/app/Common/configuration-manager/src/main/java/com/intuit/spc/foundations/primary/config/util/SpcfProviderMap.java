package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.ISpcfConfigurationProvider;
import com.intuit.spc.foundations.primary.config.SpcfConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.SpcfProviderUpdateException;

/**
 * An internal class used by CMS. The class must be public because the specific layers are in different
 * packages/assemblies.
 * 
 * @author yzhang [Created on Jun 5, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primary/config/util/SpcfProviderMap.java#1 $
 */
public final class SpcfProviderMap
{
    /**
     * Factory Instance
     */
	private static final SpcfFactory Factory = SpcfFactory.getInstance();

    /**
     * Once created, it is immutable so no need to syncrhonize
     */
    private SpcfMap<String, SpcfList<ISpcfConfigurationProvider>> mMetaConfigMap = Factory.<String, SpcfList<ISpcfConfigurationProvider>>createHashMap();

    /**
     * Constructs an instance
     */
    public SpcfProviderMap()
    {
        //TODO how can we make this immutable after creation?
    }

    /**
     * Adds a provider for the module id.
     * 
     * @param moduleId
     * @param provider
     */
    public void addProvider(String moduleId, ISpcfConfigurationProvider provider)
    {
        SpcfList<ISpcfConfigurationProvider> providers = this.mMetaConfigMap.getItem(moduleId);
        if (providers == null)
        {
            providers = Factory.<ISpcfConfigurationProvider>createArrayList();
            this.mMetaConfigMap.add(moduleId, providers);
        }
        providers.add(provider);
    }

    /**
     * Returns true if the module id has at least one provider configured
     * 
     * @param moduleId The id of the module
     * @return true if the module id has at least one provider configured
     */
    public boolean hasProvider(String moduleId)
    {
        // we'll never have empty lists so as long as the key is found we are okay.
        return this.mMetaConfigMap.containsKey(moduleId);
    }

    /**
     * Gets a list of providers for the module id.
     * 
     * @param moduleId the id of the module.
     * @return a list of providers for the module id.
     * @throws SpcfMetaConfigEntryMissingException if the module has no providers configured in the meta configuration and
     *             exceptionIfMissing is true.
     */
    public SpcfList<ISpcfConfigurationProvider> getProviders(String moduleId)
    {
        SpcfList<ISpcfConfigurationProvider> obj = this.mMetaConfigMap.getItem(moduleId);
        if (obj != null && obj.getSize() > 0)
        {
            return obj;
        }
        throw new SpcfConfigEntryMissingException(moduleId, "Missing provider for module '" + moduleId + "'.");
    }

    /**
     * Gets the provider used to update configurations in the persistent storage for the module id.
     * 
     * @param moduleId The id of the module
     * @return The provider used to update configuration in the persistent storage for the module id.
     */
    public ISpcfConfigurationProvider getProviderForUpdate(String moduleId)
    {
        SpcfList<ISpcfConfigurationProvider> providers = this.getProviders(moduleId);
        // when multiple providers are available, for this first release the last one
        // will always be used for updates.
        int size = providers.getSize();
        ISpcfConfigurationProvider provider = providers.getItem(size - 1);
        if (provider.getReadonly())
        {
            throw new SpcfProviderUpdateException("The last configuration provider for module '" + moduleId
                    + "' is readonly.");
        }
        return provider;
    }
    
    /**
     * Gets a list of module ids
     * 
     * @return a list of String objects reprensenting module ids.
     */
    public SpcfCollection<String> getModuleIds()
    {
        return this.mMetaConfigMap.getKeyList();
    }
}
