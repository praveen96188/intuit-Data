package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: dweinberg
 * Date: Jul 10, 2009
 * Time: 3:55:09 PM
 */
public class SAPCompanySearchResult {
    private SAPCompanyKey key;
    private String legalName;
    private String fein;
    private String PSID;

    private ArrayList<SAPCompanyServiceStatus> services;

    private ArrayList<SAPEntitlementSearchResult> entitlements;

    public SAPCompanyKey getKey() {
        return key;
    }

    public void setKey(SAPCompanyKey key) {
        this.key = key;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getFein() {
        return fein;
    }

    public void setFein(String fein) {
        this.fein = fein;
    }

    public String getPSID() {
        return PSID;
    }

    public void setPSID(String PSID) {
        this.PSID = PSID;
    }

    public ArrayList<SAPCompanyServiceStatus> getServices() {
        return services;
    }

    public void setServices(ArrayList<SAPCompanyServiceStatus> pServices) {
        services = pServices;
    }

    public ArrayList<SAPEntitlementSearchResult> getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(ArrayList<SAPEntitlementSearchResult> entitlements) {
        this.entitlements = entitlements;
    }
}
