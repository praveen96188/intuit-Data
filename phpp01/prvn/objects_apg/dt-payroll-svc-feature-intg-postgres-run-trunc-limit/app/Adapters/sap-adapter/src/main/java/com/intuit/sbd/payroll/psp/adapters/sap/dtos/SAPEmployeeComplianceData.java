package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

public class SAPEmployeeComplianceData {

    private String state;
    private String wagePlanDomain;
    private String name;
    private String wagePlanValue;
    private String description;
    private String rulesVersion;
    private String id;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWagePlanDomain() {
        return wagePlanDomain;
    }

    public void setWagePlanDomain(String domain) {
        this.wagePlanDomain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWagePlanValue() {
        return wagePlanValue;
    }

    public void setWagePlanValue(String wagePlanValue) {
        this.wagePlanValue = wagePlanValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(String rulesVersion) {
        this.rulesVersion = rulesVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
