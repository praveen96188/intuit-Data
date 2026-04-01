package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2008
 * Time: 2:32:19 PM
 */
public class SAPQuickbooksInfo {
    private String licenseNumber;
    private String applicationVersion;
    private String taxTable;
    private String coaFeeAccountName;
    private String coaSalesTaxAccountName;
    private boolean processTransmissions;
    private boolean allowTransmissions;
    private String fileId;

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getTaxTable() {
        return taxTable;
    }

    public void setTaxTable(String taxTable) {
        this.taxTable = taxTable;
    }

    public String getCoaFeeAccountName() {
        return coaFeeAccountName;
    }

    public void setCoaFeeAccountName(String coaFeeAccountName) {
        this.coaFeeAccountName = coaFeeAccountName;
    }

    public String getCoaSalesTaxAccountName() {
        return coaSalesTaxAccountName;
    }

    public void setCoaSalesTaxAccountName(String coaSalesTaxAccountName) {
        this.coaSalesTaxAccountName = coaSalesTaxAccountName;
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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String pFileId) {
        fileId = pFileId;
    }
}
