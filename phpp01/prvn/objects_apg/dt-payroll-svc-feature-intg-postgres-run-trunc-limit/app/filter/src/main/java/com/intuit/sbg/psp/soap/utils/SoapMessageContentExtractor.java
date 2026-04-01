package com.intuit.sbg.psp.soap.utils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import java.util.Objects;

public class SoapMessageContentExtractor {

    public static String extractContent(SOAPBody body, String elementName) {
        NodeList nodeList = body.getElementsByTagName(elementName);
        if (nodeList == null) {
            return null;
        }
        Node elementNode = nodeList.item(0);
        if (elementNode == null) {
            return null;
        }
        return elementNode.getNodeValue() == null ? elementNode.getTextContent() : elementNode.getNodeValue();
    }

    public static String extractContentIfOnlySingleElementPresent(SOAPBody body, String elementName) {
        NodeList nodeList = body.getElementsByTagName(elementName);
        if (nodeList == null || nodeList.getLength() > 1) {
            return null;
        }
        Node elementNode = nodeList.item(0);
        if (elementNode == null) {
            return null;
        }
        return elementNode.getNodeValue() == null ? elementNode.getTextContent() : elementNode.getNodeValue();
    }

    public static String extractAttributeForElement(SOAPBody soapBody, String elementName, String attributeName) {
        NodeList nodeList = soapBody.getElementsByTagName(elementName);
        Node nNode = nodeList.item(0);

        if(nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) nNode;
            String userValue = element.getAttribute(attributeName);
            return userValue;
        }
        return null;
    }
}
