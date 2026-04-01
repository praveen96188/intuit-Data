package com.intuit.sbd.payroll.psp.adapters.sap;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 28, 2010
 * Time: 6:41:22 PM
 */
public class SAPException extends Exception {
    public SAPException() {
        super();
    }

    public SAPException(String message) {
        super(message);
    }

    public SAPException(String message, Throwable cause) {
        super(message, cause);
    }

    public SAPException(Throwable cause) {
        super(cause);
    }
}
