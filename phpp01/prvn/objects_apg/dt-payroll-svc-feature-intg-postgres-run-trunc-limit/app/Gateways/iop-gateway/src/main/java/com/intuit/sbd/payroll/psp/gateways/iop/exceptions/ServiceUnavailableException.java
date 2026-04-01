package com.intuit.sbd.payroll.psp.gateways.iop.exceptions;

/**
 * @author Jeff Jones
 */
public class ServiceUnavailableException extends Exception {
    public ServiceUnavailableException(String pMessage) {
        super(pMessage);
    }

    public ServiceUnavailableException(String pMessage, Throwable pThrowable) {
        super(pMessage, pThrowable);
    }
}
