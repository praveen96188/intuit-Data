package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Apr 11, 2008
 * Time: 9:36:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class QuickbooksInfoDTO {
    private String applicationVersion;
    private String applicationId;
    private String taxTableId;
    private String licenseNumber;
    private String CoaFeeAccountName;
    private String CoaSalesTaxAccountName;
    private long payrollCount;
    private String fileId;
    private boolean processTransmissions;
    private String mIAMRealmId;
    private boolean allowTransmissions;
    private SpcfCalendar watermarkDate;
    private String quickbooksSku;

    public String getTaxTableId() {
        return taxTableId;
    }

    public void setTaxTableId(String taxTableId) {
        this.taxTableId = taxTableId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getCoaFeeAccountName() {
        return CoaFeeAccountName;
    }

    public void setCoaFeeAccountName(String coaFeeAccountName) {
        CoaFeeAccountName = coaFeeAccountName;
    }

    public String getCoaSalesTaxAccountName() {
        return CoaSalesTaxAccountName;
    }

    public void setCoaSalesTaxAccountName(String coaSalesTaxAccountName) {
        CoaSalesTaxAccountName = coaSalesTaxAccountName;
    }

    public long getPayrollCount() {
        return payrollCount;
    }

    public void setPayrollCount(long payrollCount) {
        this.payrollCount = payrollCount;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String pFileId) {
        fileId = pFileId;
    }

    public boolean isProcessTransmissions() {
        return processTransmissions;
    }

    public void setProcessTransmissions(boolean pProcessTransmissions) {
        processTransmissions = pProcessTransmissions;
    }

    public boolean isAllowTransmissions() {
        return allowTransmissions;
    }

    public void setAllowTransmissions(boolean pAllowTransmissions) {
        allowTransmissions = pAllowTransmissions;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        String qbInfoId = applicationVersion+applicationId;

        if (!Validator.isValidLength(applicationVersion, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.QuickbooksInfo, qbInfoId, "ApplicationVersion");
        }

        if (!Validator.isValidLength(applicationId, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.QuickbooksInfo, qbInfoId, "ApplicationId");
        }

        if (!Validator.isValidLength(licenseNumber, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.QuickbooksInfo, qbInfoId, "LicenseNumber");
        }

        if (!Validator.isValidLength(taxTableId, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.QuickbooksInfo, qbInfoId, "TaxTableId");
        }

        if (!Validator.isValidLength(CoaFeeAccountName, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.QuickbooksInfo, qbInfoId, "CoaFeeAccountName");
        }

        if (!Validator.isValidLength(CoaSalesTaxAccountName, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.QuickbooksInfo, qbInfoId, "CoaSalesTaxAccountName");
        }

        if (!Validator.isValidLength(fileId, 0, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.QuickbooksInfo, qbInfoId, "FileId");
        }

        return validationResult;
    }

    public String getIAMRealmId() {
        return mIAMRealmId;
    }

    public void setIAMRealmId(String pIAMRealmId) {
        mIAMRealmId = pIAMRealmId;
    }

    public SpcfCalendar getWatermarkDate() {
        return watermarkDate;
    }

    public void setWatermarkDate(SpcfCalendar pWatermarkDate) {
        watermarkDate = pWatermarkDate;
    }

    public String getQuickbooksSku() {
        return quickbooksSku;
    }

    public void setQuickbooksSku(String pQuickbooksSku) {
        quickbooksSku = pQuickbooksSku;
    }
}
