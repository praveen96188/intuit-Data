package com.intuit.sbd.payroll.psp.context.exception;

public class DifferentCompanyAlreadyPresentException extends RuntimeException{
    public DifferentCompanyAlreadyPresentException(String message){
        super(message);
    }
}
