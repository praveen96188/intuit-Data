package com.intuit.sbd.payroll.psp.batchjobs.soxReport.models;

import lombok.Getter;

@Getter
public enum SoxResultSet {
    USERNAME(0),
    ACCESS(1),
    ACCOUNT_STATUS(2),
    PROFILE(3),
    CREATED(4);

    private int index;

    SoxResultSet(int index) {
        this.index = index;
    }

}

