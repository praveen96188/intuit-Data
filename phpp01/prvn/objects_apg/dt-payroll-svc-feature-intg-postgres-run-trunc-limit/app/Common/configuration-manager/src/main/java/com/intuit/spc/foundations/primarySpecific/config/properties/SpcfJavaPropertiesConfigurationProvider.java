package com.intuit.spc.foundations.primarySpecific.config.properties;

import org.apache.commons.lang.StringUtils;

import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfConfigurationProvider;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigModuleException;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.SpcfProviderInitException;
import com.intuit.spc.foundations.primary.config.SpcfProviderLoadException;
import com.intuit.spc.foundations.primary.config.SpcfProviderUpdateException;
import com.intuit.spc.foundations.primary.config.SpcfProviderUpdateUnsupportedException;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigurationProviderAttribute;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigFileUtils;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigProviderUtils;


/**
 * Java Property Configuration Provider.
 * @author yzhang [Created on Sep 8, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/properties/SpcfJavaPropertiesConfigurationProvider.java#1 $
 */
public class SpcfJavaPropertiesConfigurationProvider implements ISpcfConfigurationProvider
{    
	/**
	 * Configuration Source Name Variable
	 */
    private String mConfigurationSourceName = null;

    /**
	 * Use Class Path Flag Variable
	 */
    private boolean mUseClasspath = false;
    
    /**
	 * Return Copy Flag Variable
	 */
    private boolean mReturnCopy = true;

    /**
	 * Optional Flag Variable
	 */
    private boolean mOptional = false;

    /**
	 * Initialized Flag Variable
	 */
    protected boolean mInitialized = false;

    /**
	 * Delegate to the default properties handler
	 */
    private SpcfDefaulPropertiesHandler mDelegate = null;

    /**
     * Constructs an instance with no parameters.
     */
    public SpcfJavaPropertiesConfigurationProvider()
    {
    	//Constructor
    }

    /**
     * @inheritDoc
     */
    public void init(SpcfMap<String, Object> properties)
    {
        SpcfParamValidator.checkIsNotNull(properties, "properties");
        try
        {
            this.mUseClasspath = SpcfConfigProviderUtils.getOptionalBooleanValue(properties, SpcfConfigurationProviderAttribute.UseClasspath);
            this.mOptional = SpcfConfigProviderUtils.getOptionalBooleanValue(properties, SpcfConfigurationProviderAttribute.Optional);
            this.mReturnCopy = SpcfConfigProviderUtils.getOptionalBooleanValue(properties, SpcfConfigurationProviderAttribute.ReturnCopy, true);
            Object obj = properties.getItem(SpcfConfigurationProviderAttribute.File);
            if (obj == null)
            {
                throw new SpcfMetaConfigEntryMissingException("properties.file",
                        "Missing required 'file' attribute for <properties> configuration provider.");
            }
            this.mConfigurationSourceName = (String) obj;
            SpcfConfigFileUtils.validateConfigurationSourceName(this.getClass().getClassLoader(),
                    this.mConfigurationSourceName, this.mUseClasspath, this.mOptional);
            this.mDelegate = SpcfDefaulPropertiesHandler.getInstance(this.mConfigurationSourceName, this.mUseClasspath);
            this.mInitialized = true;
        }
        catch (SpcfConfigModuleException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            String errorMsg = constructErrorMessage("Unable to initialize SpcfJavaPropertiesConfigurationProvider", e);
            throw new SpcfProviderInitException(errorMsg, e);
        }
    }

    /**
     * To check the state. You may not call store/load before initializing
     */
    protected void checkState()
    {
        if (!this.mInitialized)
        {
            throw new SpcfIllegalStateException("Instance has not been properly initialized.");
        }
    }

    /**
     * To construct the error message
     * @param message Message
     * @return Error Message
     */
    protected String constructErrorMessage(String message)
    {
        return constructErrorMessage(message, null);
    }

    /**
     * To construct error message
     * @param message Message
     * @param cause Exception caused
     * @return Error message
     */
    protected String constructErrorMessage(String message, Exception cause)
    {
        StringBuffer sb = new StringBuffer(message);
        sb.append(". configurationSource='");
        sb.append(this.mConfigurationSourceName == null ? "<Unknown>" : this.mConfigurationSourceName);
        sb.append("', useClasspath=");
        sb.append(this.mUseClasspath);
        sb.append(", optional=");
        sb.append(this.mOptional);
        String detailedError = (cause == null) ? null : cause.getMessage();
        if (!StringUtils.isBlank(detailedError))
        {
            sb.append(", error=");
            sb.append(detailedError);
        }
        return sb.toString();
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
            config = this.mDelegate.load(configConstants, this.mOptional);
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
            this.mDelegate.store(config, this.mOptional);
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

    /**
     * @inheritDoc
     */
    public final boolean getReadonly()
    {
        return this.mUseClasspath;
    }

    /**
     * @inheritDoc
     */
    public boolean getReturnCopyRequired() {
        return mReturnCopy;
    }


    /**
     * @inheritDoc
     */
    public String getConfigurationSourceName() {
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }
}
