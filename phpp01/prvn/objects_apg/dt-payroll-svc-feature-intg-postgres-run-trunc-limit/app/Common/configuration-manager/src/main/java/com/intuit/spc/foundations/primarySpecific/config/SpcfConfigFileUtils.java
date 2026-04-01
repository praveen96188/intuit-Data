package com.intuit.spc.foundations.primarySpecific.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;

import org.exolab.castor.xml.Unmarshaller;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfCannotLoadMetaConfigException;
import com.intuit.spc.foundations.primary.config.SpcfConfigFileTooBigException;
import com.intuit.spc.foundations.primary.config.SpcfConfigurationManager;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryInvalidException;
import com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider;
import com.intuit.spc.foundations.primarySpecific.config.meta.Constants;
import com.intuit.spc.foundations.primarySpecific.config.meta.MetaConfig;
import com.intuit.spc.foundations.primarySpecific.config.meta.Module;

/**
 * An internal class used by CMS.
 * 
 * @author yzhang [Created on Jul 13, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/SpcfConfigFileUtils.java#1 $
 */
public class SpcfConfigFileUtils
{
    /**
     * Meta file Path on the class path
     */
    static final String MetaFilePathOnClasspath = "conf/spcf-meta-config.xml";
    /**
     * System property used to specify the meta configuration file
     */
    public static final String MetaFileSysProp = "spcf.cms.meta.config";
    /**
     * System property used to specify the application home directory.
     */
    public static final String AppHomeSysProp = "app.home";
    /**
     * User Directory System Property
     */
    static final String UserDirSysProp = "user.dir";
    /**
     * Configuraion File Parent Directory
     */
    private static File sConfigFileParentDir = null;
    /**
     * boolean indicating whether or not file lock should be enabled
     */
    private static final boolean NeedFileLock = Boolean.valueOf(System.getProperty("spcf.cms.enable.filelock"))
                    .booleanValue();

    /**
     * Method to return whether or not file lock should be enabled for file based configuration providers.
     * 
     * @return a boolean indicating whether or not file lock should be enabled for file based configuration providers.
     */
    public static boolean needFileLock()
    {
        return NeedFileLock;
    }

    /**
     * Gets the File object based on the location, which can be either relative or absolute. When it's relative, the
     * file will be relative to the directory returned by {@link #getConfigurationRootDir()}
     * 
     * @param fileName The location of the file, can be either relative or absolute.
     * @return a File object representing the path.
     */
    public static File getFile(String fileName)
    {
        File file = new File(fileName);
        if (!file.isAbsolute())
        {
            File parent = getConfigurationRootDir();
            file = new File(parent, fileName);
        }
        return file;
    }

    /**
     * Gets the configuration root directory using the following logic: <br>
     * 1. If system property "app.home" is set, this value is returned <br>
     * 2. Otherwise, system property "user.dir" is returned, which is the application execution directory
     * 
     * @return the configuration root directory, used to construct the full paths of relative configuration file
     *         locations
     */
    static File getConfigurationRootDir()
    {
        if (sConfigFileParentDir == null)
        {
            String parentDir = System.getProperty(AppHomeSysProp);
            if (parentDir == null)
            {
                parentDir = System.getProperty(UserDirSysProp);
            }
            sConfigFileParentDir = new File(parentDir);
            if (!sConfigFileParentDir.exists())
            {
                // TODO throw exception??
            }
            if (!sConfigFileParentDir.isDirectory())
            {
                // TODO throw exception??
            }
        }
        return sConfigFileParentDir;
    }

    /**
     * This method is for testing only!!!!
     */
    static void testOnlyResetConfigurationRootDir()
    {
        sConfigFileParentDir = null;
    }

    /**
     * Validates that the configSourceName is valid based on the criteria passed in.
     * 
     * @param classLoader Class Loader
     * @param configSourceName Configuration Source Name
     * @param useClasspath Use Class Path
     * @param optional Optional
     * @throws SpcfMetaConfigEntryInvalidException if problem finding configuration resource
     * @throws SpcfConfigFileTooBigException if a resource on the file system exceeds the hard coded limit.
     */
    public static void validateConfigurationSourceName(ClassLoader classLoader, String configSourceName,
                    boolean useClasspath, boolean optional)
    {
        if (useClasspath)
        {
            URL url = classLoader.getResource(configSourceName);
            checkResourceSize(url);
            if (url == null && !optional)
            {
                throw new SpcfMetaConfigEntryInvalidException(
                                "The configuration resource could not be found on the CLASSPATH: " + configSourceName);
            }
        }
        else
        {
            File file = SpcfConfigFileUtils.getFile(configSourceName);
            checkFileSize(file);
            if (!file.exists() && !optional)
            {
                throw new SpcfMetaConfigEntryInvalidException("The configuration file does not exist: "
                                + configSourceName);
            }
            if (!file.canRead() && !optional)
            {
                throw new SpcfMetaConfigEntryInvalidException("The configuration file can not be read: "
                                + configSourceName);
            }
        }
    }

    /**
     * Gets the default meta configuration xml using the following logic:
     * <p>
     * 1. This method will check to see if a system property named “spcf.cms.meta.config” is defined. If so, the value
     * of this system property is used to locate the meta configuration. If “spcf.cms.meta.config” is defined, it must
     * point to a valid meta configuration file; otherwise, the method throws an SpcfCannotLoadMetaConfigException. The
     * system property can be passed in to the Java application via the “-D” command line option when the application is
     * first started up.
     * <p>
     * 2. If “spcf.cms.meta.config” is not defined, the method will use the classloader passed in to search through the
     * CLASSPATH to look for /conf/spcf-meta-config.xml. There must be at most one /conf/spcf-meta-config.xml on the
     * CLASSPATH. If multiple files with the same name and location are found on the CLASSPATH, the method throws a
     * SpcfCannotLoadMetaConfigException.
     * <p>
     * 3. If both 1 and 2 fails, SpcfCannotLoadMetaConfigException is thrown to indicate that no default meta
     * configuration file can be found.
     * <p>
     * 
     * @param classLoader
     * @return the meta configuration XML
     * @throws SpcfCannotLoadMetaConfigException
     */
    static String getDefaultMetaConfigXml(ClassLoader classLoader) throws SpcfCannotLoadMetaConfigException
    {
        String metaConfigXml = getMetaConfigXmlFromSystemProperty();
        if (metaConfigXml == null)
        {
            metaConfigXml = getMetaConfigXmlFromClassloader(classLoader);
        }
        return metaConfigXml;
    }

    /**
     * Creates a MetaConfig object using the following logic:
     * <p>
     * 1. This method will check to see if a system property named “spcf.cms.meta.config” is defined. If so, the value
     * of this system property is used to locate the meta configuration. If “spcf.cms.meta.config” is defined, it must
     * point to a valid meta configuration file; otherwise, the method throws an SpcfCannotLoadMetaConfigException. The
     * system property can be passed in to the Java application via the “-D” command line option when the application is
     * first started up.
     * <p>
     * 2. If “spcf.cms.meta.config” is not defined, the method will use the classloader passed in to search through the
     * CLASSPATH to look for /conf/spcf-meta-config.xml. If multiple files with the same name and location are found on
     * the CLASSPATH, they will be loaded an merged into a single MetaConfig object. Note that, in such a situation, it
     * is not permissible to have duplicate modules across meta-config files.
     * <p>
     * 3. If both 1 and 2 fails, SpcfCannotLoadMetaConfigException is thrown to indicate that no default meta
     * configuration file can be found.
     * <p>
     * 
     * @param metaConfigConstants
     * @param classLoader
     * @return a MetaConfig object
     * @throws SpcfCannotLoadMetaConfigException
     */
    static MetaConfig getDefaultMetaConfig(ISpcfImmutableConfiguration metaConfigConstants, ClassLoader classLoader)
    {
        MetaConfig metaConfig = null;
        // if its set in the sys prop, then use that one only
        String metaConfigXml = getMetaConfigXmlFromSystemProperty();
        if (metaConfigXml != null)
        {
            metaConfig = getMetaConfigFromXml(metaConfigConstants, metaConfigXml);
        }
        else
        {
            SpcfParamValidator.checkIsNotNull(classLoader, "classLoader");
            try
            {
                Enumeration<URL> resources = classLoader.getResources(MetaFilePathOnClasspath);
                // make sure there's at least one meta-config in the classpath
                if (!resources.hasMoreElements())
                {
                    throw new SpcfCannotLoadMetaConfigException(
                                    "No default meta configuration file specified or found on the CLASSPATH.");
                }
                // grab the first meta-config in the classpath
                URL url = (URL) resources.nextElement();
                checkResourceSize(url);
                // there's just one, so make it into a MetaConfig object
                InputStream metaInputStream = url.openStream();
                metaConfigXml = getContentAsString(metaInputStream);
                metaConfig = getMetaConfigFromXml(metaConfigConstants, metaConfigXml);
                // more than one, so merge them
                while (resources.hasMoreElements())
                {
                    URL nextUrl = (URL) resources.nextElement();
                    checkResourceSize(nextUrl);
                    InputStream nextMetaInputStream = nextUrl.openStream();
                    metaConfigXml = getContentAsString(nextMetaInputStream);
                    MetaConfig nextMetaConfig = getMetaConfigFromXml(metaConfigConstants, metaConfigXml);
                    metaConfig = mergeMetaConfigs(metaConfig, nextMetaConfig);
                }
            }
            catch (IOException e)
            {
                throw new SpcfCannotLoadMetaConfigException("Unable to read meta configuration from the CLASSPATH: "
                                + e.getMessage(), e);
            }
        }
        return metaConfig;
    }

    /**
     * To merge meta configurations
     * 
     * @param firstMetaConfig First Meta configuration
     * @param secondMetaConfig Second Meta configuration
     * @return merged meta configuration
     */
    private static MetaConfig mergeMetaConfigs(MetaConfig firstMetaConfig, MetaConfig secondMetaConfig)
    {
        MetaConfig metaConfig = firstMetaConfig;
        // merge the modules
        Enumeration newModules = secondMetaConfig.enumerateModule();
        while (newModules.hasMoreElements())
        {
            metaConfig.addModule((Module) newModules.nextElement());
        }
        // merge the constants
        Constants firstConstants = firstMetaConfig.getConstants();
        Constants newConstants = secondMetaConfig.getConstants();
        if ((firstConstants != null) && (newConstants != null))
        {
            Enumeration newConfigProviders = newConstants.enumerateConfigProvider();
            while (newConfigProviders.hasMoreElements())
            {
                firstConstants.addConfigProvider((ConfigProvider) newConfigProviders.nextElement());
            }
            metaConfig.setConstants(firstConstants);
        }
        else if ((firstConstants == null) && (newConstants != null))
        {
            metaConfig.setConstants(newConstants);
        }
        return metaConfig;
    }

    /**
     * To get meta configuration from XML
     * 
     * @param metaConfigConstants Meta Configuration Constants
     * @param xml Xml
     * @return Meta Configuration Object
     */
    private static MetaConfig getMetaConfigFromXml(ISpcfImmutableConfiguration metaConfigConstants, String xml)
    {
        String metaConfigXml = xml.trim();
        Unmarshaller metaConfigUnmarshaller = new Unmarshaller(MetaConfig.class);
        metaConfigUnmarshaller.setValidation(true);
        if (metaConfigConstants != null)
        {
            metaConfigXml = SpcfConfigUtils.expandConstant(metaConfigConstants, metaConfigXml);
            if (metaConfigXml == null)
            {
                throw new SpcfMetaConfigEntryInvalidException("Meta Configuration has invalid constant reference(s)");
            }
        }
        // Expanding system properties if any.
        try
        {
            metaConfigXml = SpcfConfigUtils.expandSystemProperties(metaConfigXml);
        }
        catch (Exception ex)
        {
            throw new SpcfMetaConfigEntryInvalidException("Meta configuration "
                            + "contains a system property reference which cannot be processed.", ex);
        }
        try
        {
            StringReader sr = new StringReader(metaConfigXml);
            Object obj = metaConfigUnmarshaller.unmarshal(sr);
            return (MetaConfig) obj;
        }
        catch (Exception e)
        {
            throw new SpcfMetaConfigEntryInvalidException("Invalid meta configuration: " + e.getMessage(), e);
        }
    }

    /**
     * To get meta configuration Xml From Class Loaded
     * 
     * @param classLoader Class Loader
     * @return meta configuration xml
     */
    private static String getMetaConfigXmlFromClassloader(ClassLoader classLoader)
    {
        SpcfParamValidator.checkIsNotNull(classLoader, "classLoader");
        InputStream metaInputStream = null;
        String metaConfigXml = null;
        try
        {
            Enumeration resources = classLoader.getResources(MetaFilePathOnClasspath);
            // must have at least one
            if (!resources.hasMoreElements())
            {
                throw new SpcfCannotLoadMetaConfigException(
                                "No default meta configuration file specified or found on the CLASSPATH.");
            }
            URL url = (URL) resources.nextElement();
            checkResourceSize(url);
            // can not have more than one
            if (resources.hasMoreElements())
            {
                throw new SpcfCannotLoadMetaConfigException("Multiple " + MetaFilePathOnClasspath
                                + " found on CLASSPATH.");
            }
            metaInputStream = url.openStream();
            metaConfigXml = getContentAsString(metaInputStream);
        }
        catch (IOException e)
        {
            throw new SpcfCannotLoadMetaConfigException("Unable to read meta configuration from the CLASSPATH: "
                            + e.getMessage(), e);
        }
        return metaConfigXml;
    }

    /**
     * To get meta configuration Xml From System Property
     * 
     * @return Meta Configuration Xml
     * @throws SpcfCannotLoadMetaConfigException
     */
    private static String getMetaConfigXmlFromSystemProperty() throws SpcfCannotLoadMetaConfigException
    {
        String metaConfigXml = null;
        String metaConfigFileLocation = System.getProperty(MetaFileSysProp);
        try
        {
            if (metaConfigFileLocation != null)
            {
                File file = getFile(metaConfigFileLocation);
                checkFileSize(file);
                FileInputStream fis = new FileInputStream(file);
                metaConfigXml = getContentAsString(fis);
            }
        }
        catch (FileNotFoundException e)
        {
            StringBuffer sb = new StringBuffer(70);
            sb.append("The meta config file specified by '");
            sb.append(MetaFileSysProp);
            sb.append("' could not be found: ");
            sb.append(e.getMessage());
            throw new SpcfCannotLoadMetaConfigException(sb.toString(), e);
        }
        catch (IOException e)
        {
            StringBuffer sb = new StringBuffer(50);
            sb.append("Error reading content out of '");
            sb.append(metaConfigFileLocation);
            sb.append("': ");
            sb.append(e.getMessage());
            throw new SpcfCannotLoadMetaConfigException(sb.toString(), e);
        }
        return metaConfigXml;
    }

    /**
     * This method is made package level so that unit test can access it directly
     * 
     * @param file the file to check
     */
    static void checkFileSize(File file)
    {
        if (file != null && file.length() > SpcfConfigurationManager.OVERALL_FILE_SIZE_LIMIT)
        {
            throw new SpcfConfigFileTooBigException(file.getAbsolutePath());
        }
    }

    /**
     * This method is made package level so that unit test can access it directly
     * 
     * @param file the file to check
     */
    static void checkResourceSize(URL url)
    {
        if (url != null)
        {
            checkFileSize(new File(url.getFile()));
        }
    }

    /**
     * This method reads the input stream and returns its entire content as a String using the default character
     * encoding.
     * <p>
     * 
     * @param inputStream The input stream to be read. inputStream will be closed after the call.
     * @return the entire content of the file as a byte array.
     * @throws IOException if an I/O error occurred.
     */
    static String getContentAsString(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream data = null;
        try
        {
            int size = inputStream.available();
            if (size <= 0)
            {
                size = 1020;
            }
            byte[] buffer = new byte[size];
            data = new ByteArrayOutputStream();
            while (true)
            {
                int i = inputStream.read(buffer);
                if (i == -1)
                {
                    break;
                }
                data.write(buffer, 0, i);
            }
        }
        finally
        {
            if (inputStream != null)
            {
                inputStream.close();
            }
        }
        return data.toString();
    }
}