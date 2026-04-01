package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 3, 2008
 * Time: 8:09:43 AM
 */
public class SAPCompanyBankAccountHistory extends SAPCompanyBankAccount {
    private Date statusEffectiveDate;
    private ArrayList<SAPPropertyAudit> propertyAudit;

    public ArrayList<SAPPropertyAudit> getPropertyAudit() {
        return propertyAudit;
    }

    public void setPropertyAudit(ArrayList<SAPPropertyAudit> propertyAudit) {
        this.propertyAudit = propertyAudit;
    }

    public Date getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(Date statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }
}
