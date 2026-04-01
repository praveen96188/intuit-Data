package com.intuit.sbd.payroll.psp.gateways.email.exception;

/**
 * This is a recoverable error and indicates that processing should continue.
 * User: kpaul
 * Date: Aug 1, 2008
 * Time: 12:01:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailProcessingException extends RuntimeException {
    public EmailProcessingException(String pMessage) {
        super(pMessage);
    }

    public EmailProcessingException(String pMessage, Throwable pThrowable) {
        super(pMessage, pThrowable);
    }
}
