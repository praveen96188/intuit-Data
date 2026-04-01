package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: rnorian
 * Date: Apr 28, 2010
 * Time: 4:42:26 PM
 */
public class SAPSystemParameter {
    private String code;
    private String value;
    private String description;
    private String org;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }
}
