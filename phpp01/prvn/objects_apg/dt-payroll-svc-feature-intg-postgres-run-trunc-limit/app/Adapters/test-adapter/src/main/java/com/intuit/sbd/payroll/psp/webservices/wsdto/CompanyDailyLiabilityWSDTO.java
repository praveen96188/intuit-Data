package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Dawn
 * Date: 5/3/11
 * Time: 4:30:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyDailyLiabilityWSDTO {
    public String id; // GUID
    public String sourceSystemCD;
    public String sourceCompanyID;
    public Date liabilityDate;
    public BigDecimal taxAmount;
    public BigDecimal taxableWagesAmount;
    public String lawId;
}
