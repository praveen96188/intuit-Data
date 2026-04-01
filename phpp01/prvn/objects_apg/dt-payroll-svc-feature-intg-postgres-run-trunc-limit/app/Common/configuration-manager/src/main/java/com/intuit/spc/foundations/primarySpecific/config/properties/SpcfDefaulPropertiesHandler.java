package com.intuit.spc.foundations.primarySpecific.config.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigInvalidException;
import com.intuit.spc.foundations.primary.config.SpcfInMemoryConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryInvalidException;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigFileUtils;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigUtils;

/**
 * @author yzhang [Created on Sep 8, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/properties/SpcfDefaulPropertiesHandler.java#1 $
 */
class SpcfDefaulPropertiesHandler
{
    /**
     * Configuration Source Name
     */
	private String mConfigurationSourceName = null;

	/**
	 * Use Class Path Variable
	 */
    private boolean mUseClasspath = false;
        
    /**
     * no need to synchronize because instances is only updated during CMS initialization from a single thread.
     */
    private static final Map<String, Object> Instances = new HashMap<String, Object>();

    /**
     * constructs an instance with no parameters
     * @param configurationSourceName Configuration Source Name
     * @param useClasspath Use Class Path flag
     */
    private SpcfDefaulPropertiesHandler(String configurationSourceName, boolean useClasspath)
    {
        this.mConfigurationSourceName = configurationSourceName;
        this.mUseClasspath = useClasspath;
    }

    /**
     * This method is only called during CMS initialization from a single thread so synchronization is not necessary.
     * 
     * @param configurationSourceName Configuration Source Name
     * @param useClasspath Use Class Path Flag
     * @return an instance of SpcfDefaulPropertiesHandler
     * @throws IOException
     */
    static SpcfDefaulPropertiesHandler getInstance(String configurationSourceName, boolean useClasspath)
            throws IOException
    {
        String uniqueConfigSourceName = configurationSourceName;
        if (!useClasspath)
        {
            File configFile = SpcfConfigFileUtils.getFile(configurationSourceName);
            //A canonical pathname is both absolute and unique.
            uniqueConfigSourceName = configFile.getCanonicalPath();
        }
        String key = uniqueConfigSourceName + useClasspath;
        Object instance = Instances.get(key);
        if (instance == null)
        {
            instance = new SpcfDefaulPropertiesHandler(uniqueConfigSourceName, useClasspath);
            Instances.put(key, instance);
        }
        return (SpcfDefaulPropertiesHandler) instance;
    }

    /**
     * To load the configuration.
     * @param constants Constants
     * @param optional Optional Flag
     * @return Configuration
     * @throws TransformerException
     * @throws IOException
     * @inheritDoc
     */
    synchronized ISpcfConfiguration load(ISpcfImmutableConfiguration constants, boolean optional)
            throws TransformerException, IOException
    {
        ISpcfConfiguration config = null;
        Properties properties = loadProperties(optional);
        if (properties == null)
        {
            config = new SpcfInMemoryConfiguration();
        }
        else
        {
            config = convertPropertiesToConfiguration(constants, properties);
        }
        return config;
    }

    /**
     * To load properties
     * @param optional Optiona Flag
     * @return Properties
     * @throws IOException
     */
    private Properties loadProperties(boolean optional) throws IOException
    {
        Properties properties = null;
        try
        {
            if (this.mUseClasspath)
            {
                properties = loadPropertiesFromClasspath(optional);
            }
            else
            {
                properties = loadPropertiesFromFile(optional);
            }
        }
        catch (IllegalArgumentException e)
        {
            StringBuffer sb = new StringBuffer(
                    "The properties file contains a malformed Unicode escape sequence.  configurationFile=");
            sb.append(this.mConfigurationSourceName);
            if (e.getMessage() != null)
            {
                sb.append(", error=");
                sb.append(e.getMessage());
            }
            throw new SpcfConfigInvalidException(sb.toString(), e);
        }
        return properties;
    }

    /**
     * Load Properties from Class path
     * @param optional Optional Flag
     * @return Properties
     * @throws IOException
     */
    private Properties loadPropertiesFromClasspath(boolean optional) throws IOException
    {
        InputStream is = null;
        Properties properties = null;
        try
        {
            is = this.getClass().getClassLoader().getResourceAsStream(this.mConfigurationSourceName);
            if (is != null)
            {
                properties = new Properties();
                properties.load(is);
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        if (!optional && properties == null)
        {
            String errorMsg = "The configuration resource could not be found on the CLASSPATH: ";
            throw new SpcfMetaConfigEntryInvalidException(errorMsg + this.mConfigurationSourceName);
        }
        return properties;
    }

    /**
     * To load properties from file
     * @param optional Optional flag
     * @return Properties
     * @throws IOException
     */
    private Properties loadPropertiesFromFile(boolean optional) throws IOException
    {
        FileInputStream fis = null;
        Properties properties = null;
        try
        {
            File file = new File(this.mConfigurationSourceName);
            if (file.exists() && file.canRead())
            {
                fis = new FileInputStream(file);
                if (SpcfConfigFileUtils.needFileLock())
                {
                    FileChannel fileChannel = fis.getChannel();
                    // use lock to be consistent with store
                    fileChannel.lock(0, Long.MAX_VALUE, true);
                    //                    FileLock fileLock = fileChannel.tryLock(0, Long.MAX_VALUE, true);
                    //                    if (fileLock == null)
                    //                    {
                    //                        throw new SpcfProviderLoadException("Unable to obtain shared file lock on "
                    //                                + this.configurationSourceName);
                    //                    }                    
                }
                properties = new Properties();
                properties.load(fis);
            }
        }
        finally
        {
            if (fis != null)
            {
                fis.close(); // This also closes the associated channel and file lock.
            }
        }
        if (!optional && properties == null)
        {
            throw new SpcfMetaConfigEntryInvalidException("The properties file does not exist or can not be read: "
                    + this.mConfigurationSourceName);
        }
        return properties;
    }

    /**
     * To convert properties to configuration
     * @param constants Constants
     * @param properties Properties
     * @return Configuration
     */
    private static ISpcfConfiguration convertPropertiesToConfiguration(ISpcfImmutableConfiguration constants,
            Properties properties)
    {
        ISpcfConfiguration config = new SpcfInMemoryConfiguration();
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements())
        {
            String name = (String) propertyNames.nextElement();
            String value = properties.getProperty(name);
            assert (value != null);
            if (constants != null)
            {
                String expandedName = SpcfConfigUtils.expandConstant(constants, name);
                String expandedValue = SpcfConfigUtils.expandConstant(constants, value);
                if (expandedName == null || expandedValue == null)
                {
                    throw new SpcfConfigInvalidException(name, value,
                            "The configuration key or value contains an invalid constant reference.");
                }
                name = expandedName;
                value = expandedValue;
            }
            
            //Expanding system properties if any.
            try
            {
                String expandedName = SpcfConfigUtils.expandSystemProperties(name);
                String expandedValue = SpcfConfigUtils.expandSystemProperties(value);
                name = expandedName;
                value = expandedValue;
            }
            catch(Exception exception)
            {
            	throw new SpcfConfigInvalidException(name, value,
                "The configuration key or value contains a system property which cannot be processed.");
            }
            
            config.setString(name, value);
        }
        return config;
    }    
        
    /**
     * To convert configuration to properties
     * @param config Configuration
     * @return Properties
     */
    private static Properties convertConfigurationToProperties(ISpcfConfiguration config)
    {
        Properties properties = new Properties();
        for (ISpcfIterator<String> it = config.getKeys(); it.hasNext();)
        {
            String key = it.next();
            String value = config.getString(key);
            properties.setProperty(key, value);
        }
        return properties;
    }

    /**
     * To store the configuration
     * @param config configuration
     * @param optional optional flag
     * @throws IOException
     * @inheritDoc
     */
    synchronized void store(ISpcfConfiguration config, boolean optional) throws IOException
    {    	
        Properties properties = convertConfigurationToProperties(config);
        storePropertiesToFile(properties);
    }

    /**
     * To store properties to file
     * @param properties properties
     * @throws IOException
     */
    private void storePropertiesToFile(Properties properties) throws IOException
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(this.mConfigurationSourceName);
            if (SpcfConfigFileUtils.needFileLock())
            {
                FileChannel fileChannel = fos.getChannel();
                // we don't use tryLock because once the FileOutputStream is created with append = false,
                // the file is already overwritten even if we don't get the lock.
                fileChannel.lock();
                //                FileLock fileLock = fileChannel.tryLock();
                //                if (fileLock == null)
                //                {
                //                    throw new SpcfProviderUpdateException("Unable to obtain exclusive lock on "
                //                            + this.configurationSourceName);
                //                }
            }
            properties.store(fos, "Properties stored by the SPCF CMS.");
            fos.flush();
        }
        catch (FileNotFoundException e)
        {
            StringBuffer sb = new StringBuffer(
                    "The configuration file specified in the meta configuration is invalid.  configurationFile=");
            sb.append(this.mConfigurationSourceName);
            if (e.getMessage() != null)
            {
                sb.append(", error=");
                sb.append(e.getMessage());
            }
            throw new SpcfMetaConfigEntryInvalidException(sb.toString(), e);
        }
        finally
        {
            if (fos != null)
            {
                fos.close(); // This also closes the associated channel and file lock.
            }
        }
    }
    
    /**
     * Returns debug information about this instance.
     * @return a String containing debug information.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("configurationSource=");
        sb.append(this.mConfigurationSourceName == null ? "" : this.mConfigurationSourceName);
        sb.append(", useClasspath=");
        sb.append(this.mUseClasspath);
        sb.append(", optional=");

        return sb.toString();
    }
}
