package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: ihannur
 * Date: 1/23/13
 * Time: 1:18 PM
 */
public class CompanyFilingInfoDTO {
    boolean finalFlag = false;
    boolean genAnnualFormInd = false;
    String lastTaxQuarter = null;
    SpcfCalendar finalPayrollDate = null;

    public boolean isFinalFlag() {
        return finalFlag;
    }

    public void setFinalFlag(boolean pFinalFlag) {
        finalFlag = pFinalFlag;
    }

    public boolean isGenAnnualFormInd() {
        return genAnnualFormInd;
    }

    public void setGenAnnualFormInd(boolean pGenAnnualFormInd) {
        genAnnualFormInd = pGenAnnualFormInd;
    }

    public String getLastTaxQuarter() {
        return lastTaxQuarter;
    }

    public void setLastTaxQuarter(String pLastTaxQuarter) {
        lastTaxQuarter = pLastTaxQuarter;
    }

    public SpcfCalendar getFinalPayrollDate() {
        return finalPayrollDate;
    }

    public void setFinalPayrollDate(SpcfCalendar pFinalPayrollDate) {
        finalPayrollDate = pFinalPayrollDate;
    }
}
