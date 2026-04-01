package com.intuit.sbd.payroll.psp.gateways.salestax.offlineticket;

/**
 * This enum is duplicated to avoid cyclic dependency utils -> payroll-services-api -> domain -> salestax-gateway -> utils. This enum is not to be used
 * by other modules. Please use the original ConfigType enum present under Common utils.
 */
public enum ConfigType {
    EFE("psp_efe"),
    PSP("psp");

    private String value;

    private ConfigType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
