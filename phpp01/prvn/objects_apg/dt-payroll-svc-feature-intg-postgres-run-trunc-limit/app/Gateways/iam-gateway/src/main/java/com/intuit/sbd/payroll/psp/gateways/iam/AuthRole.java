package com.intuit.sbd.payroll.psp.gateways.iam;

public enum AuthRole {
    CompanyAdmin("Intuit.ems.CompanyAdmin"),
    CustomerSupport("Intuit.ems.CustomerSupport"),
    ProviderAdmin("Intuit.ems.ProviderAdmin"),
    Employee("Intuit.ems.Employee"),
    Contractor("Intuit.ems.Contractor");

    private String iamRole;

    AuthRole(String iamRole) {
        this.iamRole = iamRole;
    }

    public String getIamRole() {
        return iamRole;
    }

    @Override
    public String toString() {
        return iamRole;
    }
}
