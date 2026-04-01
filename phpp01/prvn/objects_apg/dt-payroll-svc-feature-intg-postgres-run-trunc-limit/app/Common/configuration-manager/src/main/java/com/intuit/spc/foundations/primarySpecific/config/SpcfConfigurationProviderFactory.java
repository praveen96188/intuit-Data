package com.intuit.spc.foundations.primarySpecific.config;

import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfHashMapImpl;
import com.intuit.spc.foundations.primary.config.ISpcfConfigurationProvider;
import com.intuit.spc.foundations.primary.config.SpcfConfigFileTooBigException;
import com.intuit.spc.foundations.primary.config.SpcfConfigInitException;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryInvalidException;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigurationProviderAttribute;
import com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider;
import com.intuit.spc.foundations.primarySpecific.config.meta.Custom;
import com.intuit.spc.foundations.primarySpecific.config.meta.Properties;
import com.intuit.spc.foundations.primarySpecific.config.meta.Property;
import com.intuit.spc.foundations.primarySpecific.config.meta.Xml;
import com.intuit.spc.foundations.primarySpecific.config.properties.SpcfJavaPropertiesConfigurationProvider;
import com.intuit.spc.foundations.primarySpecific.config.xml.SpcfXmlConfigurationProvider;

/**
 * A factory class used internally by CMS for creating configuration provider.
 *  
 * @author yzhang [Created on Jul 12, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/SpcfConfigurationProviderFactory.java#1 $
 */
public class SpcfConfigurationProviderFactory
{
    /**
     * To get the Configuration Provider
     * @param configProvider Config Provider
     * @return The configuration provider instance Config Provider Instance
     * @throws SpcfMetaConfigEntryMissingException if any meta config entry is missing
     * @throws SpcfMetaConfigEntryInvalidException if any meta config entry is invalid
     * @throws SpcfConfigInitException if any problem in intialization
     * @throws UnsupportedOperationException if configProvider is not supported
     */
    static ISpcfConfigurationProvider getConfigurationProvider(ConfigProvider configProvider)
    {
        if (configProvider instanceof Xml)
        {
            return SpcfXmlConfigurationProviderFactory.getProvider((Xml) configProvider);
        }
        else if (configProvider instanceof Custom)
        {
            return SpcfCustomConfigurationProviderFactory.getProvider((Custom) configProvider);
        }
        else if (configProvider instanceof com.intuit.spc.foundations.primarySpecific.config.meta.Properties)
        {
            return SpcfPropertiesConfigurationProviderFactory.getProvider((Properties) configProvider);            
        }
        
        throw new UnsupportedOperationException("provider not supported yet: " + configProvider.toString());
    }

    /**
     * To get the properties
     * @param configProvider Config provider
     * @return Configuration Properties
     */
    private static HashMap<String, Object> getProperties(ConfigProvider configProvider)
    {
        int propertyCount = configProvider.getPropertyCount();
        HashMap<String, Object> propertyMap = new HashMap<String, Object>(propertyCount + 1);
        for (int i = 0; i < propertyCount; i++)
        {
            Property property = configProvider.getProperty(i);
            propertyMap.put(property.getName(), property.getValue());
        }
        propertyMap.put(SpcfConfigurationProviderAttribute.Optional, Boolean.valueOf(configProvider.getOptional()));
        return propertyMap;
    }

    /**
     * To create Provider.
     * @param providerClass Property Class
     * @param propertiesMap Property Map
     * @return configuration provider
     * @throws SpcfMetaConfigEntryMissingException if any meta config entry is missing
     * @throws SpcfMetaConfigEntryInvalidException if any meta config entry is invalid
     * @throws SpcfConfigFileTooBigException if any configuration file is over the allowed size limit.
     * @throws SpcfConfigInitException if any problem in intialization
     */
    private static ISpcfConfigurationProvider createProvider(String providerClass, HashMap<String, Object> propertiesMap)
    {
        ISpcfConfigurationProvider provider = null;
        try
        {
            Class theClass = Class.forName(providerClass);
            provider = (ISpcfConfigurationProvider) theClass.newInstance();
            provider.init(new SpcfHashMapImpl<String, Object>(propertiesMap));
        }
        catch (SpcfConfigFileTooBigException e)
        {
            throw e;
        }
        catch (SpcfMetaConfigEntryMissingException e)
        {
            throw e;
        }
        catch (SpcfMetaConfigEntryInvalidException e)
        {
            throw e;
        }
        catch (ClassNotFoundException e)
        {
            StringBuffer sb = new StringBuffer("The provider class can not be found: providerClass=");
            sb.append(providerClass);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfMetaConfigEntryInvalidException(sb.toString(), e);
        }
        catch (InstantiationException e)
        {
            StringBuffer sb = new StringBuffer("The provider class can not be instantiated: providerClass=");
            sb.append(providerClass);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfMetaConfigEntryInvalidException(sb.toString(), e);
        }
        catch (IllegalAccessException e)
        {
            StringBuffer sb = new StringBuffer("The provider's default constructor is not accessible: providerClass=");
            sb.append(providerClass);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfMetaConfigEntryInvalidException(sb.toString(), e);
        }
        catch (ClassCastException e)
        {
            StringBuffer sb = new StringBuffer("The provider class can not be casted to ");
            sb.append(ISpcfConfigurationProvider.class.getName());
            sb.append(". providerClass=");
            sb.append(providerClass);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfMetaConfigEntryInvalidException(sb.toString(), e);            
        }
        catch (Exception e)
        {
            StringBuffer sb = new StringBuffer("Unable to create/initialize configuration provider. providerClass=");
            sb.append(providerClass);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfConfigInitException(sb.toString(), e);
        }
        return provider;
    }

    /**
     * Spcf Xml Configuration Provider Factory
     * @author barunachalam
     *
     */
    private static class SpcfXmlConfigurationProviderFactory
    {
        /**
         * To get provider
         * @param xml Xml 
         * @return Provider
         */
    	private static ISpcfConfigurationProvider getProvider(Xml xml)
        {
            HashMap<String, Object> propertyMap = getProperties(xml);
 
            propertyMap.put(SpcfConfigurationProviderAttribute.File, xml.getFile());
            propertyMap.put(SpcfConfigurationProviderAttribute.UseClasspath, 
                    Boolean.valueOf(xml.getUseClasspath()));
            propertyMap.put(SpcfConfigurationProviderAttribute.ReturnCopy, 
                    Boolean.valueOf(xml.getReturnCopy()));
            String nodeName = xml.getNode();
            if (!StringUtils.isBlank(nodeName))
            {
                propertyMap.put(SpcfConfigurationProviderAttribute.Node, nodeName);
            }            
            String providerClass = xml.getProviderClass();
            if (StringUtils.isBlank(providerClass))
            {
                providerClass = SpcfXmlConfigurationProvider.class.getName();
            }            
    
            return createProvider(providerClass, propertyMap);
        }
    }
    
    /**
     * Properities configuration Provider Factory
     * @author barunachalam
     *
     */
    private static class SpcfPropertiesConfigurationProviderFactory
    {
    	/**
    	 * To get provider
    	 * @param properties Properties
    	 * @return Provider
    	 */
        private static ISpcfConfigurationProvider getProvider(Properties properties)
        {
            HashMap<String, Object> propertyMap = getProperties(properties);
 
            propertyMap.put(SpcfConfigurationProviderAttribute.File, properties.getFile());
            propertyMap.put(SpcfConfigurationProviderAttribute.UseClasspath, 
                    Boolean.valueOf(properties.getUseClasspath()));
            propertyMap.put(SpcfConfigurationProviderAttribute.ReturnCopy, 
                    Boolean.valueOf(properties.getReturnCopy()));        
            String providerClass = properties.getProviderClass();
            if (StringUtils.isBlank(providerClass))
            {
                providerClass = SpcfJavaPropertiesConfigurationProvider.class.getName();
            }            
    
            return createProvider(providerClass, propertyMap);
        }
    }

    /**
     * Custom Configuration Provider Factory
     * @author barunachalam
     *
     */
    private static class SpcfCustomConfigurationProviderFactory
    {
    	/**
    	 * To get provider
    	 * @param custom Custom
    	 * @return Provider
    	 */
        private static ISpcfConfigurationProvider getProvider(Custom custom)
        {
            HashMap<String, Object> propertyMap = getProperties(custom);            
            propertyMap.put(SpcfConfigurationProviderAttribute.ReturnCopy, 
                    Boolean.valueOf(custom.getReturnCopy()));
            String providerClass = custom.getProviderClass();
            if (StringUtils.isBlank(providerClass))
            {
                throw new SpcfMetaConfigEntryMissingException(SpcfConfigurationProviderAttribute.ProviderClass,
                        "You must provide the fully qualified class name of the custom configuration provider.");
            }
            return createProvider(providerClass, propertyMap);
        }
    }
}
