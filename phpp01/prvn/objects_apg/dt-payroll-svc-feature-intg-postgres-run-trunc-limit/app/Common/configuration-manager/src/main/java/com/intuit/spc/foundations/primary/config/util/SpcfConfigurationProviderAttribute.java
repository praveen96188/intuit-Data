package com.intuit.spc.foundations.primary.config.util;

/**
 * This class is used by CMS internally.  Contains attributes for all configuration providers.
 * @author yzhang [Created on Sep 12, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primary/config/util/SpcfConfigurationProviderAttribute.java#1 $
 */
public class SpcfConfigurationProviderAttribute
{     
    /**
     * providerClass attribute, applies to all config providers
     */
    public static final String ProviderClass = "providerClass";
    
    /**
     * optional attribute, applies to all config providers.
     */
    public static final String Optional = "optional";
    
    /**
     * file attribute, applies to all file based config providers
     */
    public static final String File = "file";

    /**
     * node attribute, only applies to Xml config providers
     */
    public static final String Node = "node";

    /**
     * useClasspath attribute, applies to Java file based config providers
     */
    public static final String UseClasspath = "useClasspath";    
    
    /**
     * embedded attribute, applies to .Net file based config providers
     */
    public static final String Embedded = "embedded";  
    
    /**
     * embeddedAssembly attribute, applies to .Net file based config providers
     */
    public static final String EmbeddedAssembly = "embeddedAssembly";  
    
    /**
     * readOnly attribute, applies to In-Memory config providers
     */
    public static final String WriteProtected = "writeProtected";  
    
    /**
     * Return Copy Requirement attribute, applies to all Config providers
     */
    public static final String ReturnCopy = "returnCopy";  
    
    /**
     * String Resource Name attribute, applies to config provider supporting embedded .resx files (.Net only) 
     */
    public static final String ResourceName = "resourceName";

    /**
     * String Resource Assembly attribute, applies to config provider supporting embedded .resx files (.Net only)
     */
    public static final String ResourceAssembly = "resourceAssembly";

    /**
     * Current UI Culture, applies to config provider supporting embedded .resx files (.Net only)
     */
    public static final String Culture = "culture";
    
    /**
     * Default Constructor
     */
    private SpcfConfigurationProviderAttribute()
    {
    	//Default Constructor
    }
}
