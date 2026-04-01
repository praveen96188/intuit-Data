package com.intuit.sbd.payroll.psp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class has been added to configure specific classes for which Clob will be converted to org.hibernate.type.TextType
 */
public class ClobToStringConfig {

    private static  List<String> clobToStringAllowedList;

    static
    {
        clobToStringAllowedList = new ArrayList<>(
                Arrays.asList("SqlExecutionLogEntry"));
    }

    public static List<String> getClobToStringAllowedList(){
        return clobToStringAllowedList;
    }
}
