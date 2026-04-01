package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 17, 2008
 * Time: 12:20:23 PM
 */
public class SAPBankReturnExtendedInfo {
    private double payrollBalanceDue;
    private Date expectedResolutionDate;

    public double getPayrollBalanceDue() {
        return payrollBalanceDue;
    }

    public void setPayrollBalanceDue(double payrollBalanceDue) {
        this.payrollBalanceDue = payrollBalanceDue;
    }

    public Date getExpectedResolutionDate() {
        return expectedResolutionDate;
    }

    public void setExpectedResolutionDate(Date expectedResolutionDate) {
        this.expectedResolutionDate = expectedResolutionDate;
    }   
}
