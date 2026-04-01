package com.intuit.sbd.payroll.psp.gateways.iam;

public enum PSPRealmType {

    INTUIT_FIXED_REALM("psp_intuitfixed_realmid"),
    CUSTOMER_WIDE_REALM("psp_customerwide_realmid");

    private String value;

    PSPRealmType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
