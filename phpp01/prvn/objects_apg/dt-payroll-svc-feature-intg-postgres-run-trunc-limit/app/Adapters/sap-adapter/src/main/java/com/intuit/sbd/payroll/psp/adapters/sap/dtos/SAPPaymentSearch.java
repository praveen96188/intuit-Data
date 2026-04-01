package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 1/17/12
 * Time: 12:17 PM
 */
public class SAPPaymentSearch {
    private String searchType;
    private String status;
    private String agencyAbbrev;
    private String paymentTemplate;
    private String paymentMethod;
    private String companyIds;
    private Date settlementStartDate;
    private Date settlementEndDate;
    private Date initiationStartDate;
    private Date initiationEndDate;
    private SAPQuarter quarter;
    private boolean overduePaymentsOnly;

    public SAPPaymentSearch() {
    }

    public SAPPaymentSearch(String searchType, String status, String agency, String paymentTemplate, String paymentMethod, String companyIds, Date settlementStartDate, Date settlementEndDate, Date initiationStartDate, Date initiationEndDate, SAPQuarter quarter, boolean overduePaymentsOnly) {
        this.searchType = searchType;
        this.status = status;
        this.agencyAbbrev = agency;
        this.paymentTemplate = paymentTemplate;
        this.paymentMethod = paymentMethod;
        this.companyIds = companyIds;
        this.settlementStartDate = settlementStartDate;
        this.settlementEndDate = settlementEndDate;
        this.initiationStartDate=initiationStartDate;
        this.initiationEndDate=initiationEndDate;
        this.quarter = quarter;
        this.overduePaymentsOnly = overduePaymentsOnly;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgencyAbbrev() {
        return agencyAbbrev;
    }

    public void setAgencyAbbrev(String agency) {
        this.agencyAbbrev = agency;
    }

    public String getPaymentTemplate() {
        return paymentTemplate;
    }

    public void setPaymentTemplate(String paymentTemplate) {
        this.paymentTemplate = paymentTemplate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(String companyIds) {
        this.companyIds = companyIds;
    }

    public Date getSettlementStartDate() {
        return settlementStartDate;
    }

    public Date getInitiationStartDate() {
        return initiationStartDate;
    }

    public void setSettlementStartDate(Date settlementStartDate) {
        this.settlementStartDate = settlementStartDate;
    }

    public void setInitiationStartDate(Date initiationStartDate) {
        this.initiationStartDate = initiationStartDate;
    }

    public Date getSettlementEndDate() {
        return settlementEndDate;
    }

    public Date getInitiationEndDate() {
        return initiationEndDate;
    }

    public void setSettlementEndDate(Date settlementEndDate) {
        this.settlementEndDate = settlementEndDate;
    }

    public void setInitiationEndDate(Date initiationEndDate) {
        this.initiationEndDate = initiationEndDate;
    }

    public SAPQuarter getQuarter() {
        return quarter;
    }

    public void setQuarter(SAPQuarter quarter) {
        this.quarter = quarter;
    }

    public boolean getOverduePaymentsOnly() {
        return overduePaymentsOnly;
    }

    public void setOverduePaymentsOnly(boolean overduePaymentsOnly) {
        this.overduePaymentsOnly = overduePaymentsOnly;
    }
}
