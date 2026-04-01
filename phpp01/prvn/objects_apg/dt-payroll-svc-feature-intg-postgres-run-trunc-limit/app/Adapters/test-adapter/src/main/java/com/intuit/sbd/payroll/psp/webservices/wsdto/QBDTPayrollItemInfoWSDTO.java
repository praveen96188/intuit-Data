package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 11:05:50 AM
 */
public class QBDTPayrollItemInfoWSDTO {
    public boolean adjustGross;
    public String agencyId;
    public boolean basedOnQuantity;
    public BigDecimal defaultLimit;
    public double defaultRate;
    public String defaultRateType;
    public boolean earningsTable;
    public String expenseAccount;
    public boolean expenseByJob;
    public boolean isDeleted;
    public boolean isEmployeePaid;
    public String liabilityAccount;
    public String liabilityAgency;
    public boolean onService;
    public String payType;
    public String specialType;
}
