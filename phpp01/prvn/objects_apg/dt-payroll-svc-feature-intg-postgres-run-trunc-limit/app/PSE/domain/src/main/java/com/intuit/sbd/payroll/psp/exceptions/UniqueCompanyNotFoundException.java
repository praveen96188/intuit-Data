package com.intuit.sbd.payroll.psp.exceptions;

public class UniqueCompanyNotFoundException extends RuntimeException{

    public UniqueCompanyNotFoundException(String message, Throwable e){super(message, e);}

    public UniqueCompanyNotFoundException(String message){super(message);}
}
