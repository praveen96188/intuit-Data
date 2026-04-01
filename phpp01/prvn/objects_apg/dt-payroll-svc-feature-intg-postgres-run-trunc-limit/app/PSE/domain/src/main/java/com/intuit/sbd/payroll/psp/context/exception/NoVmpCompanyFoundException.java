package com.intuit.sbd.payroll.psp.context.exception;

public class NoVmpCompanyFoundException extends RuntimeException{
    public NoVmpCompanyFoundException(String message) {
        super(message);
    }
}
