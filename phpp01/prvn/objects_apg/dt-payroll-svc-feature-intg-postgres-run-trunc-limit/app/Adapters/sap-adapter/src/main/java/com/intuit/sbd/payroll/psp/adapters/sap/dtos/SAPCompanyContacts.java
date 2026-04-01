package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: rnorian
 * Date: Jun 3, 2009
 * Time: 3:34:10 PM
 */
public class SAPCompanyContacts {
    private String sourceSystemCd;
    private String companyId;

    private boolean hasSecondaryContact = false;
    private ArrayList<SAPContact> contacts = new ArrayList<SAPContact>();

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String sourceSystemCd) {
        this.sourceSystemCd = sourceSystemCd;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public ArrayList<SAPContact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<SAPContact> contacts) {
        this.contacts = contacts;
    }
}
