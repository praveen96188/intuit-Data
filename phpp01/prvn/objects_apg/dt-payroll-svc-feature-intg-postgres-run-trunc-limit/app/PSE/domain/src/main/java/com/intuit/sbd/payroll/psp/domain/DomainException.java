package com.intuit.sbd.payroll.psp.domain;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Mar 25, 2009
 * Time: 1:15:01 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException() {
    }

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    protected DomainException(Throwable cause) {
        super(cause);
    }
}
