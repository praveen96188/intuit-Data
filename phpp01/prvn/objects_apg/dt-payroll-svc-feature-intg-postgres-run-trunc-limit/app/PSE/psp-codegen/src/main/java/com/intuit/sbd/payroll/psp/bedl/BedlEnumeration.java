package com.intuit.sbd.payroll.psp.bedl;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;

import java.util.ArrayList;

/**
 * A wrapper for a DataObject DOM node from the generated bedl file
 */
public class BedlEnumeration {
    public BedlEnumeration(Element pEnumerationElement) {
        enumerationElement = pEnumerationElement;
    }

    public String getEnumerationName() {
       return enumerationElement.getAttribute("Name").toString(); 
    }

    public ArrayList<String> getEnumerationValues() {
        if (enumerationValues == null) {
           enumerationValues = new ArrayList<String>();

           NodeList enumerationValueList = enumerationElement.getElementsByTagName("Field");
            for (int i = 0; i < enumerationValueList.getLength(); i++) {
                enumerationValues.add(((Element)enumerationValueList.item(i)).getAttribute("Name"));
            }
        }

        return enumerationValues;
    }


    /*
    <Enumeration Name="ACHReturnCode">
      <Fields>
        <Field Name="R01" />
      </Fields>
    </Enumeration>

     */
    private Element enumerationElement;
    private ArrayList<String> enumerationValues = null;

}