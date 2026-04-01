package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: Jul 9, 2009
 * Time: 5:17:34 PM
 */
public class SAPTaxCompanyServiceInfo extends SAPAbstractCompanyServiceInfo {
    private String lastTaxQuarter;
    private boolean fileAnnualReturns;
    private boolean mIsfinal;
    private Date lastPayrollDate;

    public String getLastTaxQuarter() {
        return lastTaxQuarter;
    }

    public void setLastTaxQuarter(String lastTaxQuarter) {
        this.lastTaxQuarter = lastTaxQuarter;
    }

    public boolean getFileAnnualReturns() {
        return fileAnnualReturns;
    }

    public void setFileAnnualReturns(boolean fileAnnualReturns) {
        this.fileAnnualReturns = fileAnnualReturns;
    }

    public boolean getIsFinal() {
        return mIsfinal;
    }

    public void setIsFinal(boolean mfinal) {
        this.mIsfinal = mfinal;
    }

    public Date getLastPayrollDate() {
        return lastPayrollDate;
    }

    public void setLastPayrollDate(Date lastPayrollDate) {
        this.lastPayrollDate = lastPayrollDate;
    }
}
