package com.intuit.spc.foundations.primarySpecific.config.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.primary.config.ISpcfConfiguration;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfConfigInvalidException;
import com.intuit.spc.foundations.primary.config.SpcfInMemoryConfiguration;
import com.intuit.spc.foundations.primary.config.SpcfMetaConfigEntryInvalidException;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigFileUtils;
import com.intuit.spc.foundations.primarySpecific.config.SpcfConfigUtils;

/**
 * @author yzhang [Created on Jun 6, 2005]
 * @version $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/xml/SpcfNameValueXmlConfigImpl.java#1 $
 */
class SpcfNameValueXmlConfigImpl
{
    /**
     * 
     */
    private static final String XERCES_SECURITY_MANAGER_PROPERTY = Constants.XERCES_PROPERTY_PREFIX
                    + Constants.SECURITY_MANAGER_PROPERTY;
    
    private static final int ENTITY_EXPANSION_LIMIT = 64;
    /**
     * Configuration Source Name variable
     */
    private String mConfigurationSourceName = null;
    /**
     * Use Class path flag variable
     */
    private boolean mUseClasspath = false;
    /**
     * Transformer variable
     */
    private Transformer mTransformer = null;
    /**
     * Document Builder flag variable
     */
    private DocumentBuilder mDocumentBuilder = null;
    /**
     * TODO The xerces specific serializer offers more options than the JAXP transform API, but this ties the JAXP
     * implementation to xerces
     */
    private XMLSerializer mSingleElementSerializer = null;
    /**
     * Entry Start Tag Pattern
     */
    private static final Pattern EntryStartTagPattern = Pattern.compile("<entry[\\s]+key=\"[^\"]+\">[\\s]*",
                    Pattern.DOTALL);
    /**
     * Entry End Tag Pattern
     */
    private static final Pattern EntryEndTagPattern = Pattern.compile("[\\s]*</entry>[\\s]*$", Pattern.DOTALL);
    /**
     * no need to synchronize because instances is only updated during CMS initialization from a single thread.
     */
    private static final Map<String, Object> Instances = new HashMap<String, Object>();

    /**
     * Constructs an instance with no parameters
     * 
     * @param configurationSourceName Configuration Source Name
     * @param useClasspath Use Class Path Flag
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    private SpcfNameValueXmlConfigImpl(String configurationSourceName, boolean useClasspath)
                    throws TransformerConfigurationException, TransformerFactoryConfigurationError,
                    ParserConfigurationException
    {
        this.mConfigurationSourceName = configurationSourceName;
        this.mUseClasspath = useClasspath;
        reset();
    }

    /**
     * This method is only called during CMS initialization from a single thread so synchronization is not necessary.
     * 
     * @param configurationSourceName Configuration Source Name
     * @param useClasspath Use Class Path
     * @return an instance of SpcfNameValueXmlConfigImpl
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws IOException
     */
    static SpcfNameValueXmlConfigImpl getInstance(String configurationSourceName, boolean useClasspath)
                    throws TransformerConfigurationException, TransformerFactoryConfigurationError,
                    ParserConfigurationException, IOException
    {
        String uniqueConfigSourceName = configurationSourceName;
        if (!useClasspath)
        {
            File configFile = SpcfConfigFileUtils.getFile(configurationSourceName);
            // A canonical pathname is both absolute and unique.
            uniqueConfigSourceName = configFile.getCanonicalPath();
        }
        String key = uniqueConfigSourceName + useClasspath;
        Object instance = Instances.get(key);
        if (instance == null)
        {
            instance = new SpcfNameValueXmlConfigImpl(uniqueConfigSourceName, useClasspath);
            Instances.put(key, instance);
        }
        return (SpcfNameValueXmlConfigImpl) instance;
    }

    /**
     * Resets the internal state of this instance in case of exceptions so that the same instance can still be used to
     * service future calls.
     * 
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    private void reset() throws TransformerConfigurationException, TransformerFactoryConfigurationError,
                    ParserConfigurationException
    {
        this.mTransformer = TransformerFactory.newInstance().newTransformer();
        this.mTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        OutputFormat singleElementFormat = new OutputFormat();
        singleElementFormat.setOmitDocumentType(true);
        singleElementFormat.setOmitXMLDeclaration(true);
        singleElementFormat.setIndenting(false);
        singleElementFormat.setOmitComments(false);
        singleElementFormat.setPreserveEmptyAttributes(true);
        singleElementFormat.setPreserveSpace(true);
        this.mSingleElementSerializer = new XMLSerializer(singleElementFormat);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        SecurityManager securityManager = (SecurityManager) factory.getAttribute(XERCES_SECURITY_MANAGER_PROPERTY);
        if (securityManager == null)
        {
            securityManager = new SecurityManager();
            factory.setAttribute(XERCES_SECURITY_MANAGER_PROPERTY, securityManager);
        }
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        // Setting the following JAXP property (as per
        // http://java.sun.com/webservices/docs/1.5/jaxp/ReleaseNotes.html#JAXP_security) does not work on Xerces.
        // factory.setAttribute("http://apache.org/xml/properties/entity-expansion-limit", 2);
        this.mDocumentBuilder = factory.newDocumentBuilder();
    }

    /**
     * To load and get the configuration
     * 
     * @param nodeName Node Name
     * @param optional Optional flag
     * @param constants Constants
     * @return Configuration
     * @throws TransformerException
     * @throws IOException
     */
    synchronized ISpcfConfiguration load(String nodeName, boolean optional, ISpcfImmutableConfiguration constants)
                    throws TransformerException, IOException
    {
        ISpcfConfiguration config = null;
        Document document = loadDocument(optional);
        if (document == null)
        {
            config = new SpcfInMemoryConfiguration();
        }
        else
        {
            Element configParentElement = getConfigParentElement(document, nodeName);
            config = loadConfigurations(configParentElement, constants);
        }
        return config;
    }

    /**
     * To load the xml document
     * 
     * @param optional Optional flag
     * @return Document
     * @throws IOException
     */
    private Document loadDocument(boolean optional) throws IOException
    {
        Document document = null;
        try
        {
            if (this.mUseClasspath)
            {
                document = loadDocumentFromClasspath(optional);
            }
            else
            {
                document = loadDocumentFromFile(optional);
            }
        }
        catch (SAXException e)
        {
            StringBuffer sb = new StringBuffer(
                            "The configuration file contains malformed/invalid Xml.  configurationFile=");
            sb.append(this.mConfigurationSourceName);
            sb.append(", error=");
            sb.append(e.toString());
            throw new SpcfConfigInvalidException(sb.toString(), e);
        }
        return document;
    }

    /**
     * To load document from the class path
     * 
     * @param optional Optional flag
     * @return document
     * @throws IOException
     * @throws SAXException
     */
    private Document loadDocumentFromClasspath(boolean optional) throws IOException, SAXException
    {
        InputStream is = null;
        Document document = null;
        try
        {
            is = this.getClass().getClassLoader().getResourceAsStream(this.mConfigurationSourceName);
            if (is != null)
            {
                document = this.mDocumentBuilder.parse(is);
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        if (!optional && document == null)
        {
            String errorMsg = "The configuration resource could not be found on the CLASSPATH: ";
            throw new SpcfMetaConfigEntryInvalidException(errorMsg + this.mConfigurationSourceName);
        }
        return document;
    }

    /**
     * To load document from file
     * 
     * @param optional Optional flag
     * @return Document
     * @throws IOException
     * @throws SAXException
     */
    private Document loadDocumentFromFile(boolean optional) throws IOException, SAXException
    {
        FileInputStream fis = null;
        Document document = null;
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
                    // FileLock fileLock = fileChannel.tryLock(0, Long.MAX_VALUE, true);
                    // if (fileLock == null)
                    // {
                    // throw new SpcfProviderLoadException("Unable to obtain shared lock on "
                    // + this.configurationSourceName);
                    // }
                }
                document = this.mDocumentBuilder.parse(fis);
            }
        }
        finally
        {
            if (fis != null)
            {
                fis.close(); // This also closes the associated channel and file lock.
            }
        }
        if (!optional && document == null)
        {
            throw new SpcfMetaConfigEntryInvalidException("The configuration file does not exist or can not be read: "
                            + this.mConfigurationSourceName);
        }
        return document;
    }

    /**
     * To get Configuration parent element
     * 
     * @param doc Document
     * @param nodeName Node Name
     * @return Parent Element
     * @throws IOException
     */
    private Element getConfigParentElement(Document doc, String nodeName) throws IOException
    {
        Element configRoot = doc.getDocumentElement();
        if (!StringUtils.isBlank(nodeName))
        {
            NodeList nodes = doc.getElementsByTagName(nodeName);
            int size = nodes.getLength();
            if (size == 0)
            {
                StringBuffer sb = new StringBuffer("Unable to find the specified configuration parent node <");
                sb.append(nodeName);
                sb.append("> in the configuration file '");
                sb.append(this.mConfigurationSourceName);
                sb.append("'");
                throw new SpcfConfigInvalidException(sb.toString());
            }
            else if (size > 1)
            {
                StringBuffer sb = new StringBuffer("The specified configuration parent node <");
                sb.append(nodeName);
                sb.append("> appears more than once in the configuration file '");
                sb.append(this.mConfigurationSourceName);
                sb.append("'");
                throw new SpcfConfigInvalidException(sb.toString());
            }
            configRoot = (Element) nodes.item(0);
        }
        return configRoot;
    }

    /**
     * To load configurations
     * 
     * @param configRoot Configuration Root
     * @param constants Constants
     * @return Configuration
     * @throws IOException
     */
    private ISpcfConfiguration loadConfigurations(Element configRoot, ISpcfImmutableConfiguration constants)
                    throws IOException
    {
        NodeList children = configRoot.getChildNodes();
        int size = children.getLength();
        SpcfInMemoryConfiguration config = new SpcfInMemoryConfiguration();
        for (int i = 0; i < size; i++)
        {
            Node node = children.item(i);
            if (node instanceof Element)
            {
                processConfigEntry(configRoot.getNodeName(), config, (Element) node, constants);
            } // skip non Element nodes
        }
        return config;
    }

    /**
     * To process configuration entry
     * 
     * @param parentNodeName Name of the Parent node
     * @param config Configuration
     * @param entryNode Entry Node
     * @param constants Constants
     * @throws IOException
     */
    private void processConfigEntry(String parentNodeName, final SpcfInMemoryConfiguration config, Element entryNode,
                    ISpcfImmutableConfiguration constants) throws IOException
    {
        if (!entryNode.getNodeName().equals("entry"))
        {
            StringBuffer sb = new StringBuffer("Invalid configuration entry: unrecognized node <");
            sb.append(entryNode.getNodeName());
            sb.append("> under <");
            sb.append(parentNodeName);
            sb.append("> in configuration file '");
            sb.append(this.mConfigurationSourceName);
            sb.append("'");
            throw new SpcfConfigInvalidException(sb.toString());
        }
        String key = entryNode.getAttribute("key");
        if (StringUtils.isBlank(key))
        {
            StringBuffer sb = new StringBuffer(
                            "Invalid configuration entry: missing required 'key' attribute in one of the <entry> nodes under <");
            sb.append(parentNodeName);
            sb.append("> in configuration file '");
            sb.append(this.mConfigurationSourceName);
            sb.append("'");
            throw new SpcfConfigInvalidException(sb.toString());
        }
        String value = this.getElementChildrenAsString(entryNode);
        if (constants != null)
        {
            String expandedName = SpcfConfigUtils.expandConstant(constants, key);
            String expandedValue = SpcfConfigUtils.expandConstant(constants, value);
            if (expandedName == null || expandedValue == null)
            {
                throw new SpcfConfigInvalidException(key, value,
                                "The configuration key or value contains an invalid constant reference.");
            }
            key = expandedName;
            value = expandedValue;
        }
        // Expanding system properties if any.
        try
        {
            String expandedName = SpcfConfigUtils.expandSystemProperties(key);
            String expandedValue = SpcfConfigUtils.expandSystemProperties(value);
            key = expandedName;
            value = expandedValue;
        }
        catch (Exception exception)
        {
            throw new SpcfConfigInvalidException(key, value,
                            "The configuration key or value contains a system property which cannot be processed.");
        }
        config.setString(key, value);
    }

    /**
     * To get element's children as string
     * 
     * @param entry Configuration Entry
     * @return Children
     * @throws IOException
     */
    private String getElementChildrenAsString(Element entry) throws IOException
    {
        NodeList children = entry.getChildNodes();
        int size = children.getLength();
        String configValue = "";
        if (size == 1 && children.item(0) instanceof Text)
        {
            configValue = children.item(0).getNodeValue().trim();
        }
        else if (size != 0)
        {
            StringWriter stringWriter = new StringWriter();
            this.mSingleElementSerializer.setOutputCharStream(stringWriter);
            // serialize at the <entry> element level so that we don't have to make
            // multiple calls to serialize in case there are multiple children under
            // <entry>
            this.mSingleElementSerializer.serialize(entry);
            configValue = stringWriter.toString();
            // get rid of the <entry> start and end tags
            configValue = EntryStartTagPattern.matcher(configValue).replaceFirst("");
            configValue = EntryEndTagPattern.matcher(configValue).replaceFirst("");
        }
        return configValue;
    }

    /**
     * To store the configuration
     * 
     * @param nodeName Name of the node
     * @param config Configuration to store
     * @param optional Optional flag
     * @throws TransformerException
     * @throws IOException
     */
    synchronized void store(String nodeName, ISpcfConfiguration config, boolean optional) throws TransformerException,
                    IOException
    {
        // in case someone else udpated the same file outside of the app, always load
        // before store
        Document document = loadDocument(optional);
        Element configParentElement = null;
        if (document == null)
        {
            document = this.mDocumentBuilder.newDocument();
            String configRootName = StringUtils.isBlank(nodeName) ? "configuration" : nodeName;
            configParentElement = document.createElement(configRootName);
            document.appendChild(configParentElement);
        }
        else
        {
            configParentElement = getConfigParentElement(document, nodeName);
        }
        this.updateDocument(document, config, configParentElement);
        storeDocumentToFile(document);
    }

    /**
     * To store document to file
     * 
     * @param document document
     * @throws IOException
     * @throws TransformerException
     */
    private void storeDocumentToFile(Document document) throws IOException, TransformerException
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
                // FileLock fileLock = fileChannel.tryLock();
                // if (fileLock == null)
                // {
                // throw new SpcfProviderUpdateException("Unable to obtain exclusive lock on "
                // + this.configurationSourceName);
                // }
            }
            Source source = new DOMSource(document);
            Result result = new StreamResult(fos);
            this.mTransformer.transform(source, result);
            fos.flush();
        }
        catch (FileNotFoundException e)
        {
            StringBuffer sb = new StringBuffer(
                            "The configuration file specified in the meta configuration is invalid.  configurationFile=");
            sb.append(this.mConfigurationSourceName);
            sb.append(", error=");
            sb.append(e.getMessage());
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
     * To update document
     * 
     * @param document Document
     * @param config Configuration to update
     * @param configParentElement Configuration parent element
     * @throws TransformerException
     */
    private void updateDocument(Document document, ISpcfConfiguration config, Element configParentElement)
                    throws TransformerException
    {
        SpcfConfigXmlUtils.removeAllChildren(configParentElement);
        for (ISpcfIterator<String> it = config.getKeys(); it.hasNext();)
        {
            String configKey = it.next();
            String configValue = config.getString(configKey);
            Node entryNode = getConfigEntryAsDomNode(document, configKey, configValue);
            configParentElement.appendChild(entryNode);
        }
    }

    /**
     * To get configuration entry as DOM node
     * 
     * @param document document
     * @param configKey configuration entry key
     * @param configValue configuration value
     * @return Configuration entry
     * @throws TransformerException
     */
    private Node getConfigEntryAsDomNode(Document document, String configKey, String configValue)
                    throws TransformerException
    {
        Node entryNode = null;
        try
        {
            // okay let's assume it's XML, but even if it's not, the same logic works too.
            // Transformation may be expensive so if we know for sure it's not xml,
            // we don't want to call transform.
            if (configValue.trim().startsWith("<"))
            {
                // It seems that the transform method only works on single node, so if
                // we directly transform child nodes of <entry>, we'd have to call
                // transform multiple times if <entry> has more than one child.
                // This is why we transform the entire <entry> node.
                String configEntryXml = getConfigEntryAsXmlString(configKey, configValue);
                StringReader stringReader = new StringReader(configEntryXml);
                Source streamSource = new StreamSource(stringReader);
                entryNode = document.createDocumentFragment();
                Result domResult = new DOMResult(entryNode);
                this.mTransformer.transform(streamSource, domResult);
            }
            else
            // else it's just text
            {
                entryNode = document.createElement("entry");
                ((Element) entryNode).setAttribute("key", configKey);
                Node text = document.createTextNode(configValue);
                entryNode.appendChild(text);
            }
        }
        catch (TransformerException e)
        {
            if (e.getCause() instanceof SAXException)
            {
                StringBuffer sb = new StringBuffer(
                                "Can not convert configuration to Xml due to the following inavlid configuration entry: configKey=");
                sb.append(configKey);
                sb.append(", configValue=");
                sb.append(configValue);
                String errMsg = e.getCause().getMessage();
                if (errMsg == null)
                {
                    errMsg = e.getCause().toString();
                }
                sb.append(", error=");
                sb.append(e.getCause().getMessage());
                throw new SpcfConfigInvalidException(sb.toString());
            }
            throw e;
        }
        return entryNode;
    }

    /**
     * To get configuration entry as XML string
     * 
     * @param configKey Configuration entry key
     * @param configValue Configuration value
     * @return Configuration entry
     */
    private static String getConfigEntryAsXmlString(String configKey, String configValue)
    {
        StringBuffer sb = new StringBuffer(configKey.length() + configValue.length() + 22);
        sb.append("<entry key=\"");
        sb.append(configKey);
        sb.append("\">");
        sb.append(configValue);
        sb.append("</entry>");
        return sb.toString();
    }
}
