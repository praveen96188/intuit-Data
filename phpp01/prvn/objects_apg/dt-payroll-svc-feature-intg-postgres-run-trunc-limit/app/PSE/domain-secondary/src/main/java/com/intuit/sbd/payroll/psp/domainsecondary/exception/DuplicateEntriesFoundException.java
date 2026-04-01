package com.intuit.sbd.payroll.psp.domainsecondary.exception;

import com.intuit.v4.Error;

public class DuplicateEntriesFoundException extends RuntimeException{
    public static final Error.ErrorTypeEnum ERROR_TYPE_ENUM = Error.ErrorTypeEnum.INVALID_REQUEST;

    public DuplicateEntriesFoundException(String message, Throwable e){super(message, e);}

    public DuplicateEntriesFoundException(String message){super(message);}
}
