package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Represent the company's combined service status.
 *
 * In the business domain, a company can also be placed on-hold by the addition of on-hold reasons.  These on-hold
 * reasons 'override' the current company service status.
 *
 * The company on-hold reasons are not represented on the company status as a convenience to the UI.  Since the
 * on-hold reasons override the service sub-status, the on-hold reasons are 'pushed down' into the status/sub-status
 * values of the company service and the UI is shielded from this 'complexity'
 */
public class SAPCompanyStatus {
    private String sourceSystemCd;
    private String companyId;
    private boolean flaggedForFraud;

    // services that can be added to a company
    private ArrayList<String> availableServices;

    private ArrayList<SAPCompanyServiceStatus> serviceStatusCollection;

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

    public boolean isFlaggedForFraud() {
        return flaggedForFraud;
    }

    public void setFlaggedForFraud(boolean flaggedForFraud) {
        this.flaggedForFraud = flaggedForFraud;
    }

    public ArrayList<String> getAvailableServices() {
        return availableServices;
    }

    public void setAvailableServices(ArrayList<String> pAvailableServices) {
        availableServices = pAvailableServices;
    }

    public ArrayList<SAPCompanyServiceStatus> getServiceStatusCollection() {
        return serviceStatusCollection;
    }

    public void setServiceStatusCollection(ArrayList<SAPCompanyServiceStatus> serviceStatusCollection) {
        this.serviceStatusCollection = serviceStatusCollection;
    }
}
