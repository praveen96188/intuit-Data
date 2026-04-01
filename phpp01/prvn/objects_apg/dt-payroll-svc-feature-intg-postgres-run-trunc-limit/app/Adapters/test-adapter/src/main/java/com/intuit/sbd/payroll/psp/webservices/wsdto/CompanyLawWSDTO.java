package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 1:25:08 PM
 */
public class CompanyLawWSDTO {
    public String exemptionStatus;
    public String sourceDescription;
    public String sourceId;
    public String status;
    public String taxFormLine;

    public String lawId;
    public QBDTPayrollItemInfoWSDTO qbdtPayrollItemInfo;

    public Collection<CompanyLawRateWSDTO> lawRates;

}
