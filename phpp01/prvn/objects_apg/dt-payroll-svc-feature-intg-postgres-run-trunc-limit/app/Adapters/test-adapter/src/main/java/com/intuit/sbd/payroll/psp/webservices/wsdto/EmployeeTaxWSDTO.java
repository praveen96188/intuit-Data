package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * User: dweinberg
 * Date: Oct 22, 2010
 * Time: 4:05:51 PM
 */
public class EmployeeTaxWSDTO {
    public String taxLawVersion;
    public String w2Name;
    public String taxType;
    public boolean subjectTo;
    public BigDecimal extraWithholding;
    public Collection<String> taxTableMiscData;

    public String companyLaw; //guid
}
