package com.intuit.spc.foundations.primarySpecific.config.xml;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigModuleException;
import com.intuit.spc.foundations.primary.config.SpcfProviderInitException;
import com.intuit.spc.foundations.primary.config.SpcfProviderLoadException;
import com.intuit.spc.foundations.primary.config.SpcfProviderUpdateException;
import com.intuit.spc.foundations.primary.config.SpcfProviderUpdateUnsupportedException;

/**
 * Xml Configuration Provider.
 * @author yzhang [Created on Jun 6, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/xml/SpcfXmlConfigurationProvider.java#1 $
 */
public final class SpcfXmlConfigurationProvider extends SpcfAbstractXmlConfigurationProvider
{
    /**
     * This is used so that the same instance is used to handle the same configuration file. In other words, even though
     * the same configuration file can be used for multiple modules, only one SpcfNameValueXmlConfigImpl instance will
     * be created per configuration file.
     */
    private SpcfNameValueXmlConfigImpl mDelegate = null;

    /**
     * constructs an instance with no parameters
     */
    public SpcfXmlConfigurationProvider()
    {
    	//Constructor
    }

    /**
     * To initialize the configuration provider
     * @param properties Properties
     */
    @Override
    public void init(SpcfMap<String, Object> properties)
    {
        try
        {
            super.init(properties);                        
            this.mDelegate = SpcfNameValueXmlConfigImpl.getInstance(getConfigurationSourceName(), isUsingClasspath());            
            this.mInitialized = true;
        }
        catch (SpcfArgumentNullException e)
        {
            throw e;
        }
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            String errorMsg = constructErrorMessage("Unable to initialize SpcfXmlConfigurationProvider", e);
            throw new SpcfProviderInitException(errorMsg, e);
        }
    }

    /**
     * @inheritDoc
     */
    public ISpcfConfiguration load(ISpcfImmutableConfiguration configConstants)
    {
        checkState();
        ISpcfConfiguration config = null;
        try
        {
            String nodeName = getNodeName();
            config = this.mDelegate.load(nodeName, isOptional(), configConstants);
        }        
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }        
        catch (Exception e)
        {
            String errorMsg = constructErrorMessage("Error loading configurations", e);
            throw new SpcfProviderLoadException(errorMsg, e);
        }
        return config;
    }

    /**
     * @inheritDoc
     */
    public void store(ISpcfConfiguration config)
    {
        SpcfParamValidator.checkIsNotNull(config, "config");
        checkState();
        
        if (getReadonly())
        {
            String errorMsg = constructErrorMessage("Can not store configuration because I've been configured to be read only in the meta configuration.");
            throw new SpcfProviderUpdateUnsupportedException(errorMsg);
        }
        
        try
        {
            String nodeName = getNodeName();
            this.mDelegate.store(nodeName, config, isOptional());
        }  
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }        
        catch (Exception e)
        {
            String errorMsg = constructErrorMessage("Error storing configuration", e);
            throw new SpcfProviderUpdateException(errorMsg, e);
        }
    }
}
