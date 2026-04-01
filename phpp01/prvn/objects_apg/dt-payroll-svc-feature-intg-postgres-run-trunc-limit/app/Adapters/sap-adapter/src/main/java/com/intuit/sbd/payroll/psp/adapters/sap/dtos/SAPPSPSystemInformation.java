package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * "About box" information to display within SAP and aide in
 * debugging, etc.
 *
 * Double prefix "SAP", "PSP" is weird bu consistent.  All SAP DTOs are prefixed with 'SAP' and the returned information
 * is PSP system information.
 *
 * Better names may exist but do not rename without updating the mapped association in the corresponding
 * Flex ActionScript class.
 */
public class SAPPSPSystemInformation {
    private String buildNumber;
    private String pspDate;

    private String schemaVersion;


    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getPspDate() {
        return pspDate;
    }

    public void setPspDate(String pspDate) {
        this.pspDate = pspDate;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

}
