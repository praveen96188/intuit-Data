package com.intuit.sbd.payroll.psp.common;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class OFXManager {

    /**
     * JAXBContexts are expensive, so cache them
     *
     * @throws JAXBException
     * @className
     */
    public static JAXBContext getJAXBContext(Class c) throws JAXBException {
        JAXBContext jaxbContext = jaxbContextMap.get(c);
        if (jaxbContext == null) {
            JAXBContext newJAXBContext = JAXBContext.newInstance(c);
            jaxbContext = jaxbContextMap.putIfAbsent(c, newJAXBContext);
            if (jaxbContext == null) {
                // put succeeded, use new value
                jaxbContext = newJAXBContext;
            }
        }
        return jaxbContext;
    }

    private static final ConcurrentHashMap<String, Schema> schemaMap = new ConcurrentHashMap<String, Schema>();

    public static Schema getSchema(String xsdFilenameAndPath) throws IOException, SAXException {
        Schema schema = schemaMap.get(xsdFilenameAndPath);
        if(schema == null) {
            URL xsdURL = OFXManager.class.getResource(xsdFilenameAndPath);
            InputStream xsdStream = xsdURL.openStream();
            StreamSource ss = new StreamSource(xsdStream);
            String sl = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(sl);
            Schema newSchema= factory.newSchema(ss);
            schema = schemaMap.putIfAbsent(xsdFilenameAndPath, newSchema);
            if(schema == null) {
                schema = newSchema;
            }
        }
        return schema;
    }

    private static final ConcurrentHashMap<Class, JAXBContext> jaxbContextMap = new ConcurrentHashMap<Class, JAXBContext>();

    /**
     * The passed in OFX String is transformed into Java class representing the Intuit OFX
     * standard for a request message.
     *
     * @param ofxStr
     * @return
     * @throws MalformedOFXException - The OFX passed in is not well formed.
     * @throws OFXToJavaMappingError - The OFX contains an unexpected, missing or unexpected content in one or more elements.
     */
    public static com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxRequestToJava(String ofxStr) throws MalformedOFXException, OFXToJavaMappingError {
        return ofxRequestToJava(ofxStr,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    /**
     *
     * @param ofxStr
     * @param ofxConversionOption
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxRequestToJava(String ofxStr, OfxConversionOptions ofxConversionOption) throws MalformedOFXException, OFXToJavaMappingError {
        String xsdFilename = "/resources/ofx_request.xsd";
        return (com.intuit.sbd.payroll.psp.common.ofx.request.OFX) ofxToJava(ofxStr, xsdFilename, com.intuit.sbd.payroll.psp.common.ofx.request.OFX.class,ofxConversionOption);
    }

    /**
     * The passed in response OFX string is transformed into Java class representing the Intuit OFX
     * standard for a request message.  Any piece of the request can be specified and returned.
     *
     * @param ofxStr
     * @return
     * @throws MalformedOFXException - The OFX passed in is not well formed.
     * @throws OFXToJavaMappingError - The OFX contains an unexpected, missing or unexpected content in one or more elements.
     */
    public static Object ofxRequestToJavaGeneric(String ofxStr,Class requestOFXClass) throws MalformedOFXException, OFXToJavaMappingError {
        return ofxRequestToJavaGeneric(ofxStr,requestOFXClass,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    /**
     *
     * @param ofxStr
     * @param requestOFXClass
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static Object ofxRequestToJavaGeneric(String ofxStr,Class requestOFXClass,OfxConversionOptions ofxConversionOption) throws MalformedOFXException, OFXToJavaMappingError {
        String xsdFilename = "/resources/ofx_request.xsd";
        return ofxToJava(ofxStr, xsdFilename, requestOFXClass, ofxConversionOption);
    }
    /**
     * The passed in OFX String is transformed into Java class representing the Intuit OFX
     * standard for a response message.
     *
     * @param ofxStr
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseToJava(String ofxStr) throws MalformedOFXException, OFXToJavaMappingError {
        return ofxResponseToJava(ofxStr,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    /**
     *
     * @param ofxStr
     * @param ofxConversionOption
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseToJava(String ofxStr,OfxConversionOptions ofxConversionOption) throws MalformedOFXException, OFXToJavaMappingError {
        String xsdFilename = "/resources/ofx_response.xsd";
        return (com.intuit.sbd.payroll.psp.common.ofx.response.OFX) ofxToJava(ofxStr, xsdFilename, com.intuit.sbd.payroll.psp.common.ofx.response.OFX.class,ofxConversionOption);
    }

    /**
     *
     * @param ofxStr
     * @param pEventHandler
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseToJava(String ofxStr, ValidationEventHandler pEventHandler) throws MalformedOFXException, OFXToJavaMappingError {
        return ofxResponseToJava(ofxStr, pEventHandler,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    /**
     *
     * @param ofxStr
     * @param pEventHandler
     * @param ofxConversionOption
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseToJava(String ofxStr, ValidationEventHandler pEventHandler,OfxConversionOptions ofxConversionOption) throws MalformedOFXException, OFXToJavaMappingError {
        String xsdFilename = "/resources/ofx_response.xsd";
        return (com.intuit.sbd.payroll.psp.common.ofx.response.OFX) ofxToJava(ofxStr, xsdFilename,
                com.intuit.sbd.payroll.psp.common.ofx.response.OFX.class, pEventHandler,ofxConversionOption);
    }

    /**
     * The passed in response OFX string is transformed into Java class representing the Intuit OFX
     * standard for a response message.  Any piece of the request can be specified and returned.
     *
     * @param ofxStr
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static Object ofxResponseToJavaGeneric(String ofxStr,Class responseOFXClass) throws MalformedOFXException, OFXToJavaMappingError {
        return ofxResponseToJavaGeneric(ofxStr,responseOFXClass,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    /**
     *
     * @param ofxStr
     * @param responseOFXClass
     * @param ofxConversionOption
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static Object ofxResponseToJavaGeneric(String ofxStr,Class responseOFXClass,OfxConversionOptions ofxConversionOption) throws MalformedOFXException, OFXToJavaMappingError {
        String xsdFilename = "/resources/ofx_response.xsd";
        return ofxToJava(ofxStr, xsdFilename, responseOFXClass,ofxConversionOption);
    }

    /**
     * Converts the ofxStr to the request or response OFX JAXB class type in
     * the ofxClass parameter.
     *
     * @param ofxStr
     * @param xsdFilenameAndPath
     * @param ofxClass
     * @return
     * @throws MalformedOFXException
     * @throws OFXToJavaMappingError
     */
    public static Object ofxToJava(String ofxStr, String xsdFilenameAndPath, Class ofxClass) throws MalformedOFXException, OFXToJavaMappingError {
        return ofxToJava(ofxStr, xsdFilenameAndPath, ofxClass,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    public static Object ofxToJava(String ofxStr, String xsdFilenameAndPath, Class ofxClass,OfxConversionOptions ofxConversionOption) throws MalformedOFXException, OFXToJavaMappingError {
        return ofxToJava(ofxStr, xsdFilenameAndPath, ofxClass, (ValidationEventHandler)null,ofxConversionOption);
    }

    public static Object ofxToJava(String ofxStr, String xsdFilenameAndPath, Class ofxClass, ValidationEventHandler pEventHandler,OfxConversionOptions ofxConversionOption)
            throws MalformedOFXException, OFXToJavaMappingError {
        Object ofxObj = null;
        String ofxXML = null;
        ofxXML = OFXToXML.convert(ofxStr,ofxConversionOption);
        try {
            verifyXML(ofxXML);
        }
        catch (Exception e) {
            throw new MalformedOFXException("OFX malformed: " + e.toString());
        }
        try {
            ByteArrayInputStream byteArrStream = new ByteArrayInputStream(ofxXML.getBytes("UTF-8"));

            JAXBContext jcRequest = getJAXBContext(ofxClass);
            Schema schema = getSchema(xsdFilenameAndPath);
            Unmarshaller unmarshaller = jcRequest.createUnmarshaller();
            unmarshaller.setSchema(schema);

            // Execute only if there is no event handler
            if (pEventHandler == null) {
                Validator v = schema.newValidator();
                v.validate(new StreamSource(byteArrStream));
            } else {
                unmarshaller.setEventHandler(pEventHandler);
            }
            byteArrStream.reset();
            ofxObj = unmarshaller.unmarshal(byteArrStream);
        }
        catch (Exception e) {
            throw new OFXToJavaMappingError("OFX could not be mapped to Java: " + e.toString(), e);
        }
        return ofxObj;
    }

    /**
     * Validates that the XML string passed in is valid XML.
     *
     * @param xmlStr
     * @return - True if well formed XML, false if not.
     */
    private static void verifyXML(String xmlStr) throws SAXException, IOException {
        DocumentBuilder builder = null;
        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            builder = dbfactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException("Unable to create JAXP DocumentBuilder");
        }
        InputSource inputSourceOfxStr = new InputSource(new StringReader(xmlStr));
        builder.parse(inputSourceOfxStr);
    }

    /**
     * Turns the request ofxObj object into an OFX String.
     *
     * @param ofxObj
     * @return
     */
    public static String javaRequestToOFX(com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj) {
        return javaRequestToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    public static String javaRequestToOFX(com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj,OfxConversionOptions ofxConversionOption) {
        return javaToOFX(ofxObj,ofxConversionOption);
    }

    /**
     * Turns the response ofxObj object into an OFX String.
     *
     * @param ofxObj
     * @return
     */
    public static String javaResponseToOFX(com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxObj) {
        return javaResponseToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    public static String javaResponseToOFX(com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxObj,OfxConversionOptions ofxConversionOption) {
        return javaToOFX(ofxObj,ofxConversionOption);
    }

    public static String javaToOFX(Object ofxObj) {
        return javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_CRIS_RULES);
    }

    /**
     * The passed in Java class representing the Intuit OFX standard OFX String is transformed into OFX.
     *
     * @param ofxObj
     * @return String representing Intuit OFX of Java object.
     */
    public static String javaToOFX(Object ofxObj,OfxConversionOptions ofxConversionOption) {
        String responseOFX = "";

        try {
            JAXBContext jcResponse = getJAXBContext(ofxObj.getClass());
            StringWriter sw = new StringWriter();
            Marshaller marshaller = jcResponse.createMarshaller();
            marshaller.marshal(ofxObj, sw);

            String xmlStr = sw.getBuffer().toString();

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlStr)));

            responseOFX = XMLToOFX.convert(document,ofxConversionOption);
        }
        catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return responseOFX;
    }

}
