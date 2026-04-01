package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 11:02:48 AM
 */
public class CompanyPayrollItemWSDTO {
    public String sourceDescription;
    public String sourcePayrollItemId;
    public String status;
    public String taxFormLine;

    public String payrollItemCode;
    public String payrollItemDescription;
    public String payrollItemType;    

    public QBDTPayrollItemInfoWSDTO qbdtPayrollItemInfo;
    public Collection<String> payrollItemTaxableToLawIds;
}
