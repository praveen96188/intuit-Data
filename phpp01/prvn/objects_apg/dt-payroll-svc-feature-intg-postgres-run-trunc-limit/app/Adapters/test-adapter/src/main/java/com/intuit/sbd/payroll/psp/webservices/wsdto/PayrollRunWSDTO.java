package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 18, 2008
 * Time: 4:46:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class PayrollRunWSDTO {
    public String id; // GUID
    public String sourceBatchId;
    public Date paycheckDepositDate;
    public Date payrollRunDate;
    public String status; // Payroll run status
    public BigDecimal netAmount;
    public int paycheckCount; // count paychecks in this payroll run
    public int txnCount; // count total number of transactions for this payroll run
    public int offloadExecutedCount; // count txns with “Executed” status
    public String payrollType;

}
