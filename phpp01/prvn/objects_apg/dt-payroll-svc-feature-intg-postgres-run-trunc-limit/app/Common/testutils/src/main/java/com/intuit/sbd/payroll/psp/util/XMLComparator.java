package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.XMLComparisonFailureException;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;

public class XMLComparator {

    /**
     * Compare two xml strings
     * @param expected expectedXml
     * @param actual actualXml
     */
    public static DetailedDiff compareXML(String expected, String actual) {

        try {
            XMLUnit.setIgnoreWhitespace(false);
            XMLUnit.setIgnoreAttributeOrder(false);
            return new DetailedDiff(XMLUnit.compareXML(expected, actual));
        } catch (Exception e) {
            throw new XMLComparisonFailureException(e.getMessage());
        }

    }
}
