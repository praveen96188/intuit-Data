package com.intuit.sbd.payroll.psp;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * This class has been added to configure specific classes for which boolean will be converted to org.hibernate.type.NumericBooleanType
 */
public class NumericBooleanTypeConfig {

    private static  List<String> numericBooleanTyAllowedList;

    static
    {
        numericBooleanTyAllowedList = new ArrayList<>(
                Arrays.asList("CompanyNote", "PstubPayItem", "SqlExecutionLogEntry"));
    }

    public static List<String> getNumericBooleanTyAllowedList(){
        return numericBooleanTyAllowedList;
    }
}
