package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 3/8/13
 * Time: 9:43 AM
 */
public class SAPCompanyFilingAmountHistory extends SAPCompanyFilingAmount {
    private SAPQuarter effectiveQuarter;
    private Date invalidDate;
    private Date modifiedDate;
    private String modifiedBy;

    public SAPQuarter getEffectiveQuarter() {
        return effectiveQuarter;
    }

    public void setEffectiveQuarter(SAPQuarter pEffectiveQuarter) {
        effectiveQuarter = pEffectiveQuarter;
    }

    public Date getInvalidDate() {
        return invalidDate;
    }

    public void setInvalidDate(Date pInvalidDate) {
        invalidDate = pInvalidDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        modifiedDate = pModifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String pModifiedBy) {
        modifiedBy = pModifiedBy;
    }
}
