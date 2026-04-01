<?xml version="1.0" encoding="UTF-8" ?>
<stylesheet version="1.0" xmlns="http://www.w3.org/1999/XSL/Transform">
<output method="text"/>

<template match="FormsRules">// Generated from xml, do not edit
<text>
package com.intuit.payroll.agency.dao.generated;

//import com.intuit.payroll.core.enumtype.FilingFrequency;

import java.util.LinkedList;
import java.util.Collection;

/** 
  * Generated from xml, do not edit.
  * Information about relationships between taxes and forms.
  * Temporary, until content is hopefully incorporated into AgencyRules.xml
  */
public class FormsRules {

    static public final Collection&lt;Form> FORMS = new LinkedList&lt;Form>();
    
    public static class Form {
        public String id;
        public boolean employeeWagesReportedPerQtr;
        public boolean companyWagesReportedPerQtr;
        public Collection&lt;String> taxes = new LinkedList&lt;String>();
    }
    
    static 
    {
        Form form;
</text>    
<apply-templates select="FormTemplate"/>
    }
}
</template>

<template match="FormTemplate">
        form = new Form();
        form.id = "<value-of select="FormID"/>";
        form.employeeWagesReportedPerQtr = <value-of select="EmployeeWagesReportedPerQuarter"/>;
        form.companyWagesReportedPerQtr = <value-of select="CompanyWagesReportedPerQuarter"/>;
<apply-templates select="Tax"/>        FORMS.add(form);
</template>

<template match="Tax">        form.taxes.add ("<value-of select="."/>");
</template>


</stylesheet>
