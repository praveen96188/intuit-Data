package com.intuit.sbd.payroll.psp.adapters.qbdtws.ui;

/**
 * User: rnorian
 * Date: Mar 25, 2010
 * Time: 10:24:09 PM
 */
public class ValidationMessage {
    private String status;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
