package com.intuit.sbd.payroll.psp.gateways.amo;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 15, 2010
 * Time: 8:38:18 AM
 */
public class TransactionAttribute {
    // transaction attribute values
    public static String UP_TO_3 = "Up to 3";
    public static String UNLIMITED = "Unlimited";
    public static String ENHANCED = "Enhanced";
    public static String BASIC = "Basic";
    public static String ENHANCED_ACCOUNTANT = "Enhanced Accountant";
    public static String STANDARD = "Standard";

    public TransactionAttribute(String pName, String pValue) {
        name = pName;
        value = pValue;
    }

    public String name;
    public String value;
}
