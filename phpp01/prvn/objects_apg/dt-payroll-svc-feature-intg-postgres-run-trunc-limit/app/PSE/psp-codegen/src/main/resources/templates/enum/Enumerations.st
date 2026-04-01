package com.intuit.sbd.payroll.psp.domain;

import java.util.HashMap;

/**
 * Custom generated code for PSP.
 */
public class Enumerations {

    public static final HashMap<Class<?>, Class<?>> enumToDataObjectMap = new HashMap<Class<?>, Class<?>>();

    static {
        $enumsToDataObjects.keys:{k| enumToDataObjectMap.put($k$.class, $enumsToDataObjects.(k)$.class);}; separator="\n"$
    }
}
