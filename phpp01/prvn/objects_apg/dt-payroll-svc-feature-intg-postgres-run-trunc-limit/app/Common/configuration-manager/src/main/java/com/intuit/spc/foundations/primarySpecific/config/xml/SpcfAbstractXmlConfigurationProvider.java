package com.intuit.spc.foundations.primarySpecific.config.xml;

import org.apache.commons.lang.StringUtils;
import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.primary.config.ISpcfConfigurationProvider;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryMissingException;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigurationProviderAttribute;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigFileUtils;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigProviderUtils;

/**
 * Abstract class for Providing Configuration from XML source.
 * @author yzhang [Created on Jun 6, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/xml/SpcfAbstractXmlConfigurationProvider.java#1 $
 */
public abstract class SpcfAbstractXmlConfigurationProvider implements ISpcfConfigurationProvider
{    
	/**
	 * Configuration Source Name variable
	 */
    private String mConfigurationSourceName = null;

    /**
	 * Node name variable
	 */
    private String mNodeName = null;

    /**
	 * Use Class Path flag variable
	 */
    private boolean mUseClasspath = false;
    
    /**
     * Return copy flag variable
     */
    private boolean mReturnCopy = true;

    /**
     * Optional flag variable
     */
    private boolean mOptional = false;

    /**
     * Initialization Flag variable
     */
    protected boolean mInitialized = false;

    /**
     * Constructor
     *
     */
    protected SpcfAbstractXmlConfigurationProvider()
    {
    	//Constructor
    }

    /**
     * To get the configuration source name
     * @return Configuraiton Source Name
     */
    public final String getConfigurationSourceName()
    {
        return this.mConfigurationSourceName;
    }

    /**
     * Gets the name of the node.
     * @return the name of the node.
     */
    public final String getNodeName()
    {
        return this.mNodeName;
    }

    /**
     * @return true if useClasspath is true; false otherwise.
     */
    protected final boolean isUsingClasspath()
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
    public final boolean getReadonly()
    {
        return this.mUseClasspath;
    }

    /**
     * To get whether the file is optional or not
     * @return true if optional, false otherwise
     */
    protected final boolean isOptional()
    {
        return this.mOptional;
    }

    /**
     * @inheritDoc
     */
    public void init(SpcfMap<String, Object> properties)
    {
        SpcfParamValidator.checkIsNotNull(properties, "properties");
        Object obj = properties.getItem(SpcfConfigurationProviderAttribute.Node);
        if (obj != null)
        {
            this.mNodeName = (String) obj;
        }
        this.mUseClasspath = SpcfConfigProviderUtils.getOptionalBooleanValue(properties, SpcfConfigurationProviderAttribute.UseClasspath);
        this.mOptional = SpcfConfigProviderUtils.getOptionalBooleanValue(properties, SpcfConfigurationProviderAttribute.Optional);
        this.mReturnCopy = SpcfConfigProviderUtils.getOptionalBooleanValue(properties, SpcfConfigurationProviderAttribute.ReturnCopy, true);
                
        obj = properties.getItem(SpcfConfigurationProviderAttribute.File);
        if (obj == null)
        {
            throw new SpcfMetaConfigEntryMissingException("xml.file",
                    "Missing required 'file' attribute for XML configuration provider.");
        }
        this.mConfigurationSourceName = (String) obj;
        
        SpcfConfigFileUtils.validateConfigurationSourceName(this.getClass().getClassLoader(),
                this.mConfigurationSourceName, this.mUseClasspath, this.mOptional);
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
        sb.append(". configurationFile='");
        sb.append(getConfigurationSourceName() == null ? "<Unknown>" : getConfigurationSourceName());
        sb.append("' configurationParentNode='");
        sb.append(getNodeName() == null ? "<root>" : getNodeName());
        sb.append("'");
        String detailedError = (cause == null) ? "" : cause.getMessage();
        if (!StringUtils.isBlank(detailedError))
        {
            sb.append(", error=");
            sb.append(detailedError);
        }
        return sb.toString();
    }
    
}
