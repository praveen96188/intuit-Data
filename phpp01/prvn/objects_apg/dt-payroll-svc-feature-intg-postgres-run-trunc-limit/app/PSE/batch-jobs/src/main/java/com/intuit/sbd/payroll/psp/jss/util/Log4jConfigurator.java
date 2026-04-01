package com.intuit.sbd.payroll.psp.jss.util;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.intuit.sbd.payroll.psp.Application;
import org.apache.commons.lang3.shaded.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;

/**
 * Utility class to configure the logging system
 *
 * @author kmuthurangam
 *
 */
public class Log4jConfigurator {

	/**
	 * Initialize the Log4j system with the configuration from spcf-logger-conf.xml
	 */
	public static void configure() {
		System.out.println("start Log4jConfigurator configure");

		String metaCfgFile = ConfigurationManager.getMetaCfgFileWithPath();
		System.out.println("ConfigFile initiated, metaCfgFile=" + metaCfgFile);
		Node loggerNode = evaluteXPathExpression(metaCfgFile, "/meta-config/module[@id='SpcfLogger-2']/xml/@file");
		String loggerPath = loggerNode.getNodeValue();

		System.out.println("Spcf Logger configuration file is located at " + loggerPath);
		try {
			if (!StringUtils.isBlank(loggerPath)) {
				Application.initializeLogger(loggerPath);
			} else {
				throw new IllegalStateException("log4jConfiguration value=empty");
			}
		} catch (Exception e) {
			System.err.println("Unable to load log4jConfiguration. " + e);
		}
	}

	private static Node evaluteXPathExpression(String fileName, String xpathExpression) {
		Node loggerPath = null;
		try {
			File inputFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;

			dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			XPath xPath = XPathFactory.newInstance().newXPath();

			loggerPath = (Node) xPath.compile(xpathExpression).evaluate(doc, XPathConstants.NODE);
		} catch (Exception e) {
			System.out.println("[Log4jConfigurator] Error evaluating xpathExpression");
		}
		return loggerPath;
	}
}
