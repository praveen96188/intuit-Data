package com.intuit.sbd.payroll.psp.exceptions;

public class CompensationCollectionNotFoundException extends RuntimeException{

    public CompensationCollectionNotFoundException(String message, Throwable e){super(message, e);}

    public CompensationCollectionNotFoundException(String message){super(message);}
}
