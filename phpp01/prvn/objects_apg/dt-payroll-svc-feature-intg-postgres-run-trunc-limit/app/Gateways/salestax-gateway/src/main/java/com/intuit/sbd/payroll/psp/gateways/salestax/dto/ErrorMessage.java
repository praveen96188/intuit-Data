package com.intuit.sbd.payroll.psp.gateways.salestax.dto;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 7, 2008
 * Time: 11:36:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class ErrorMessage {
    private String errorCode;
    private String errorDescription;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
