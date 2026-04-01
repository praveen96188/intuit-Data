package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User: dweinberg
 * Date: 11/15/12
 * Time: 9:58 AM
 */
public class LedgerOperationWSDTO {
    public String sourceSystemCode;
    public String sourceCompanyId;
    public BigDecimal amount;
    public String memo;
    public Date checkDate;
    public String originalLegalName;
    public String status;
    public String messages;
    public int originalIndex;
    public String lawId;

    public String jobStatus;
    public Date startTime;
    public Date finishTime;
    public String jobType;

}
