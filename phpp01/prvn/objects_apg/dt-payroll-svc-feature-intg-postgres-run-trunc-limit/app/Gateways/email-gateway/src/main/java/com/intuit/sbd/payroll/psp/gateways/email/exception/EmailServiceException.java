package com.intuit.sbd.payroll.psp.gateways.email.exception;

/**
 * This is a non-recoverable error and indicates that processing should cease.
 * User: kpaul
 * Date: Aug 1, 2008
 * Time: 12:02:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailServiceException extends RuntimeException {
    public EmailServiceException(String pMessage) {
        super(pMessage);
    }

    public EmailServiceException(String pMessage, Throwable pThrowable) {
        super(pMessage, pThrowable);
    }
}
