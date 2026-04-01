package com.intuit.sbd.payroll.psp.common.utils.offlineticket;

public enum ConfigType {
            EFE("psp_efe"),
            PSP("psp");

            private String value;

            ConfigType(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
 }


