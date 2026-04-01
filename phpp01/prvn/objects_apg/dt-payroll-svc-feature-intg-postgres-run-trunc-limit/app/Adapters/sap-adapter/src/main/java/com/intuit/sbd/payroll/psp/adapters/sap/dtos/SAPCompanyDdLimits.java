package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 31, 2009
 * Time: 1:49:58 PM
 */
public class SAPCompanyDdLimits {
    private double perPayrollLimit;
    private double perEmployeeLimit;

    private double defaultEmployeeLimit;
    private double defaultPayrollLimit;

    public double getPerPayrollLimit() {
        return perPayrollLimit;
    }

    public void setPerPayrollLimit(double pPerPayrollLimit) {
        perPayrollLimit = pPerPayrollLimit;
    }

    public double getPerEmployeeLimit() {
        return perEmployeeLimit;
    }

    public void setPerEmployeeLimit(double pPerEmployeeLimit) {
        perEmployeeLimit = pPerEmployeeLimit;
    }

    public double getDefaultEmployeeLimit() {
        return defaultEmployeeLimit;
    }

    public void setDefaultEmployeeLimit(double pDefaultEmployeeLimit) {
        defaultEmployeeLimit = pDefaultEmployeeLimit;
    }

    public double getDefaultPayrollLimit() {
        return defaultPayrollLimit;
    }

    public void setDefaultPayrollLimit(double pDefaultPayrollLimit) {
        defaultPayrollLimit = pDefaultPayrollLimit;
    }
}
