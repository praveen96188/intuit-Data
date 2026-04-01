package com.intuit.sbd.payroll.psp.domainsecondary.exception;

import com.intuit.v4.Error;

public class PolicyDescriptionNotFoundException extends RuntimeException{

    public static final Error.ErrorTypeEnum ERROR_TYPE_ENUM = Error.ErrorTypeEnum.NOT_FOUND_ERROR;

    public PolicyDescriptionNotFoundException(String message, Throwable e){super(message, e);}

    public PolicyDescriptionNotFoundException(String message){super(message);}
}
