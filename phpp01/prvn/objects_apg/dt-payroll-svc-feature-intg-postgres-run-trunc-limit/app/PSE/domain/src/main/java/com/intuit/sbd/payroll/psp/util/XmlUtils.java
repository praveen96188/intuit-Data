package com.intuit.sbd.payroll.psp.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * @author Wiktor Kozlik
 */
public class XmlUtils {

    public static String xmlToString(Document pXmlDoc) {
        try {
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer xform = tfactory.newTransformer();
            Source src = new DOMSource(pXmlDoc);
            StringWriter writer = new StringWriter();
            StreamResult result = new javax.xml.transform.stream.StreamResult(writer);
            xform.transform(src, result);
            return writer.getBuffer().toString();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
