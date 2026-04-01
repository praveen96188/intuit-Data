package com.intuit.ems.payroll.psp.gateways.ers;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 13, 2010
 * Time: 2:25:26 PM
 */
public class ERSConnectionException extends Exception {
    public ERSConnectionException() {
        super();
    }

    public ERSConnectionException(String message) {
        super(message);
    }

    public ERSConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ERSConnectionException(Throwable cause) {
        super(cause);
    }
}
