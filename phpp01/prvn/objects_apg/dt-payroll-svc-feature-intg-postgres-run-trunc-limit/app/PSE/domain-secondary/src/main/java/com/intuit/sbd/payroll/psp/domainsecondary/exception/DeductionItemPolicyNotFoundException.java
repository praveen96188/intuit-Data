package com.intuit.sbd.payroll.psp.domainsecondary.exception;

import com.intuit.v4.Error;

public class DeductionItemPolicyNotFoundException extends RuntimeException{

    public static final Error.ErrorTypeEnum ERROR_TYPE_ENUM = Error.ErrorTypeEnum.NOT_FOUND_ERROR;

    public DeductionItemPolicyNotFoundException(String message, Throwable e){super(message, e);}

    public DeductionItemPolicyNotFoundException(String message){super(message);}
}