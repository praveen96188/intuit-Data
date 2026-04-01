package com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain;

public enum SoxDataManager {
    DATA_MANAGER_APP("PSP"),
    DATA_MANAGER_DB_MONOLITH("PSP_MONOLITH"),
    DATA_MANAGER_DB_AUDIT("PSP_AUDIT")
    ;

    private final String value;

    SoxDataManager(String dmName) {
        this.value=dmName;
    }

    public String value(){
        return value;
    }
}
