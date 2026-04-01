package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: Aug 17, 2010
 * Time: 1:47:33 PM
 */
public class SAPAssetInfo {
    private boolean primary;
    private boolean assisted;
    private String assistedSubType;
    private String assetCode;

    public boolean getPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean getAssisted() {
        return assisted;
    }

    public void setAssisted(boolean assisted) {
        this.assisted = assisted;
    }

    public String getAssistedSubType() { return assistedSubType;}

    public void setAssistedSubType(String assistedSubType) {
        this.assistedSubType = assistedSubType;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }
}
