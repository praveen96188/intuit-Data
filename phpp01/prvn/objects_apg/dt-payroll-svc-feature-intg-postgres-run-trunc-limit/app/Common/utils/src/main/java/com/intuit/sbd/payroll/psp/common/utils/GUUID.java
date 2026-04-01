package com.intuit.sbd.payroll.psp.common.utils;

import java.util.UUID;

public class GUUID {

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
