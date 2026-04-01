package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

/**
 * @author Jeff Jones
 */
public class RSStateWithholding {
    private String type;
    private String state;
    private String filingStatus;
    private String allowances;
    private String additionalWithHolding;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFilingStatus() {
        return filingStatus;
    }

    public void setFilingStatus(String filingStatus) {
        this.filingStatus = filingStatus;
    }

    public String getAllowances() {
        return allowances;
    }

    public void setAllowances(String allowances) {
        this.allowances = allowances;
    }

    public String getAdditionalWithHolding() {
        return additionalWithHolding;
    }

    public void setAdditionalWithHolding(String additionalWithHolding) {
        this.additionalWithHolding = additionalWithHolding;
    }
}
