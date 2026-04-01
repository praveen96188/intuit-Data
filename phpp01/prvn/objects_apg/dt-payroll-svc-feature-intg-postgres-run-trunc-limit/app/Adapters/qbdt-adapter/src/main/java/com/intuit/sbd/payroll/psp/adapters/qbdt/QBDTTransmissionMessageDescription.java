package com.intuit.sbd.payroll.psp.adapters.qbdt;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jun 7, 2008
 * Time: 10:16:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class QBDTTransmissionMessageDescription {

    public static String getPaycheckVoidedDescriptor(int paycheckCreatedCnt) {
        return  paycheckCreatedCnt + " paychecks voided";
    }

    public static String getPaycheckAddedDescriptor(int paycheckCreatedCnt) {
        return  paycheckCreatedCnt + " paychecks added";
    }

    public static String getZeroPayrollDescriptor() {
        return "Zero Payroll";
    }

    public static String getCoInfoUpdateDescriptor() {
        return "Company info updated";
    }

    public static String getErrorDescriptor() {
        return "Error";
    }

    public static String getErrorBadPIN() {
        return "Invalid PIN";
    }

    public static String getDuplicatePaycheckDescriptor() {
        return "Duplicate payrolls - rejected dupliactes";
    }
    

}
