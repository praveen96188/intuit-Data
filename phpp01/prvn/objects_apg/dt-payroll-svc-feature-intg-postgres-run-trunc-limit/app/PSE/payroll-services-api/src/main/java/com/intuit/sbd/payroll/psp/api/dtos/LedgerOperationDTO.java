package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

/**
 * User: dweinberg
 * Date: 11/8/12
 * Time: 10:21 AM
 */
public class LedgerOperationDTO {
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private SpcfMoney amount;
    private DateDTO checkDate;
    private String lawId;
    private String memo;
    private String originalLegalName;
    private SpcfMoney taxableWages;

    // Specific to RateUpdate
    private double rate;
    private boolean pushToQuickBooks;

    // Specific to AdditionalFilingAmountUpdate
    private String additionalAmountName;

    // Specific to DepositFrequencyUpdate
    private DepositFrequencyCode depositFrequencyCode;

    public SourceSystemCode getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(SourceSystemCode pSourceSystemCd) {
        sourceSystemCd = pSourceSystemCd;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String pSourceCompanyId) {
        sourceCompanyId = pSourceCompanyId;
    }

    public SpcfMoney getAmount() {
        return amount;
    }

    public void setAmount(SpcfMoney pAmount) {
        amount = pAmount;
    }

    public DateDTO getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(DateDTO pCheckDate) {
        checkDate = pCheckDate;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String pLawId) {
        lawId = pLawId;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String pMemo) {
        memo = pMemo;
    }

    public String getOriginalLegalName() {
        return originalLegalName;
    }

    public void setOriginalLegalName(String pOriginalLegalName) {
        originalLegalName = pOriginalLegalName;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double pRate) {
        rate = pRate;
    }

    public String getAdditionalAmountName() {
        return additionalAmountName;
    }

    public void setAdditionalAmountName(String pAdditionalAmountName) {
        additionalAmountName = pAdditionalAmountName;
    }

    public boolean isPushToQuickBooks() {
        return pushToQuickBooks;
    }

    public void setPushToQuickBooks(boolean pPushToQuickBooks) {
        pushToQuickBooks = pPushToQuickBooks;
    }

    public DepositFrequencyCode getDepositFrequencyCode() {
        return depositFrequencyCode;
    }

    public void setDepositFrequencyCode(DepositFrequencyCode pDepositFrequencyCode) {
        depositFrequencyCode = pDepositFrequencyCode;
    }

    public SpcfMoney getTaxableWages() {
        return taxableWages;
    }

    public void setTaxableWages(SpcfMoney pTaxableWages) {
        taxableWages = pTaxableWages;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (sourceSystemCd == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "SourceSystemCode");
        }

        if (StringUtils.isEmpty(sourceCompanyId)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "SourceCompanyId");
        }

        if (checkDate == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, sourceCompanyId, "CheckDate");
        }

        validationResult.merge(checkDate.validate());

        if (StringUtils.isEmpty(lawId)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, sourceCompanyId, "LawId");
        }

        if (StringUtils.isEmpty(memo)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, sourceCompanyId, "Memo");
        }

        return validationResult;
    }
}
