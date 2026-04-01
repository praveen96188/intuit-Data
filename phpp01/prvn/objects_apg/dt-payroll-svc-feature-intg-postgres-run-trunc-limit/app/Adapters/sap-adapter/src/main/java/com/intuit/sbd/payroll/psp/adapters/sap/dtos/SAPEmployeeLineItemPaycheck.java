package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 4:37 PM
 */
public class SAPEmployeeLineItemPaycheck extends SAPEmployeeLineItemGroup {
    private boolean isPaycheckVoid;
    private Date paycheckDate;
    private String sourcePayrollRunId;
    private boolean isELA;

    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date paycheckDate) {
        this.paycheckDate = paycheckDate;
    }

    public boolean getIsPaycheckVoid() {
        return isPaycheckVoid;
    }

    public void setIsPaycheckVoid(boolean paycheckVoid) {
        isPaycheckVoid = paycheckVoid;
    }

    public String getSourcePayrollRunId() {
        return sourcePayrollRunId;
    }

    public void setSourcePayrollRunId(String sourcePayrollRunId) {
        this.sourcePayrollRunId = sourcePayrollRunId;
    }

    public boolean getIsELA() {
        return isELA;
    }

    public void setIsELA(boolean pELA) {
        isELA = pELA;
    }

}
