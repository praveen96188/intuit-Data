package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: ihannur
 * Date: 11/26/12
 * Time: 11:05 AM
 */
@SuppressWarnings("unused")
public class SAPLedgerItemDetailsCriterion {
    private String sourceSystemCd;
    private String companyId;
    private Date payrollDate;
    private String payrollRunId;
    private String voidId;
    private String templateCd;
    private String lawId;
    private boolean isQTD;
    private boolean isYTD;
    private boolean includeNotPostedPayments;

    public SAPLedgerItemDetailsCriterion() {
    }

    public SAPLedgerItemDetailsCriterion(String sourceSystemCd, String companyId, Date payrollDate, String payrollRunId, String voidId, String templateCd, String lawId, boolean isQTD, boolean isYTD, boolean includeNotPostedPayments) {
        this.sourceSystemCd = sourceSystemCd;
        this.companyId = companyId;
        this.payrollDate = payrollDate;
        this.payrollRunId = payrollRunId;
        this.voidId = voidId;
        this.templateCd = templateCd;
        this.lawId = lawId;
        this.isQTD = isQTD;
        this.isYTD = isYTD;
        this.includeNotPostedPayments = includeNotPostedPayments;
    }

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String pSourceSystemCd) {
        sourceSystemCd = pSourceSystemCd;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String pCompanyId) {
        companyId = pCompanyId;
    }

    public Date getPayrollDate() {
        return payrollDate;
    }

    public void setPayrollDate(Date payrollDate) {
        this.payrollDate = payrollDate;
    }

    public String getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(String payrollRunId) {
        this.payrollRunId = payrollRunId;
    }

    public String getVoidId() {
        return voidId;
    }

    public void setVoidId(String voidId) {
        this.voidId = voidId;
    }

    public String getTemplateCd() {
        return templateCd;
    }

    public void setTemplateCd(String templateCd) {
        this.templateCd = templateCd;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public boolean getIsQTD() {
        return isQTD;
    }

    public void setIsQTD(boolean isQTD) {
        this.isQTD = isQTD;
    }

    public boolean getIsYTD() {
        return isYTD;
    }

    public void setIsYTD(boolean isYTD) {
        this.isYTD = isYTD;
    }

    public boolean isIncludeNotPostedPayments() {
        return includeNotPostedPayments;
    }

    public void setIncludeNotPostedPayments(boolean includeNotPostedPayments) {
        this.includeNotPostedPayments = includeNotPostedPayments;
    }
}
