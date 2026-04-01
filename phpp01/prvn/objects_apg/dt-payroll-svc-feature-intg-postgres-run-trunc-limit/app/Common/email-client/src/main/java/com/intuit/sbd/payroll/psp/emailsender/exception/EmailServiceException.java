package com.intuit.sbd.payroll.psp.emailsender.exception;

/**
 * Custom exception for Authentication Failures.
 *
 * @author vishalb849
 */
public class EmailServiceException extends RuntimeException {

    public EmailServiceException(String pMessage) {
        super(pMessage);
    }

    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}