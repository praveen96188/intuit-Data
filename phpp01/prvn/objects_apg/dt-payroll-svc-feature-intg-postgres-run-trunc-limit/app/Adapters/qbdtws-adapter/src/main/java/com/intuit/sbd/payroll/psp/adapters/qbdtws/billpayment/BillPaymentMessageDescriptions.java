package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 4, 2010
 * Time: 12:08:56 PM
 */
public class BillPaymentMessageDescriptions {
    public static String getPaymentVoided(int paymentsVoided) {
        return  paymentsVoided + " vendor payment" + (paymentsVoided > 1 ? "s" : "") + " voided";
    }

    public static String getPaymentAdded(int paymentsCreated) {
        return  paymentsCreated + " vendor payments added";
    }

    public static String getErrorDescriptor() {
        return "Error";
    }    

    public static String getQuery() {
        return "Payment query";
    }

    public static String getQueryFailure() {
        return "Payment query failure";
    }

    public static String getSubmitFailure() {
        return "Payment submit failure";
    }

    public static String getVoidFailure() {
        return "Payment void failure";
    }
}
