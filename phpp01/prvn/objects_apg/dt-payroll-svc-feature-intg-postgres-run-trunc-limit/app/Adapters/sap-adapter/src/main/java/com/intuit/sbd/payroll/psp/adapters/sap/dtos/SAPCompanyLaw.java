package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

public class SAPCompanyLaw {
    private String sourceId = null;
    private String description = null;
    private String lawType = null;
    private String status = null;
    private String taxFormLine = null;
    private String lawId = null;
    private String latestId = null;
    private String agencyId;
    private String deleteStatus = null;
    private String token;
    private String coaExpense = null;
    private String coaLiability = null;
    private String pendingPush = null;
    private Boolean iisemp = null;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String pSourceId) {
        sourceId = pSourceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public String getLawType() {
        return lawType;
    }

    public void setLawType(String pLawType) {
        lawType = pLawType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public String getTaxFormLine() {
        return taxFormLine;
    }

    public void setTaxFormLine(String pTaxFormLine) {
        taxFormLine = pTaxFormLine;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String pLawId) {
        lawId = pLawId;
    }

    public String getLatestId() {
        return latestId;
    }

    public void setLatestId(String pLatestId) {
        latestId = pLatestId;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String pAgencyId) {
        agencyId = pAgencyId;
    }

    public String getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(String pDeleteStatus) {
        deleteStatus = pDeleteStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String pToken) {
        token = pToken;
    }

    public String getCoaExpense() {
        return coaExpense;
    }

    public void setCoaExpense(String pCoaExpense) {
        coaExpense = pCoaExpense;
    }

    public String getCoaLiability() {
        return coaLiability;
    }

    public void setCoaLiability(String pCoaLiability) {
        coaLiability = pCoaLiability;
    }

    public String getPendingPush() {
        return pendingPush;
    }

    public void setPendingPush(String pPendingPush) {
        pendingPush = pPendingPush;
    }

    public Boolean getIisemp() {
        return iisemp;
    }

    public void setIisemp(Boolean pIisemp) {
        iisemp = pIisemp;
    }
}
