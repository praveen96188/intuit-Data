package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Mar 25, 2009
 * Time: 2:49:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyAgencyWSDTO {
    public String agencyId;

    public String agencyTaxPayerId;
    public String companyAgencyStatusCode;
    public Date finalPayrollDate;
    public String firstFilingsQuarter;                                    
    public boolean generateAnnualForm;
    public Date intuitResponsibilityStartDate;
    public Date intuitResponsibilityEndDate;
    public boolean isFinalReturn;
    public String lastFilingsQuarter;

    public Collection<CompanyLawWSDTO> companyLaws;
    public Collection<CompanyAgencyPaymentTemplateWSDTO> companyAgencyPaymentTemplates;
    public Collection<CompanyAgencyOnHoldReasonWSDTO> companyAgencyOnHoldReasons;
}
