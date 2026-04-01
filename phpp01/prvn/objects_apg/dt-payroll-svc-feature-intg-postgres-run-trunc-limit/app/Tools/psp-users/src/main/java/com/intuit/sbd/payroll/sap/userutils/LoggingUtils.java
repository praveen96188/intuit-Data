package com.intuit.sbd.payroll.sap.userutils;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Dec 22, 2008
 * Time: 4:29:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoggingUtils {
    public static void log(String output) {
        System.out.println(output);
    }

    public static void display(String output) {
        System.out.println(output);
    }

    public static void logException(Exception e) {
        log(e.getMessage());
        e.printStackTrace();
    }
}
