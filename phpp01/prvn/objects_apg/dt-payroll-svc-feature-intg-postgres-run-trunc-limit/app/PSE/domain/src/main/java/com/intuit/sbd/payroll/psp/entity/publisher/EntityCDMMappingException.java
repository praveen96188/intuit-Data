package com.intuit.sbd.payroll.psp.entity.publisher;

public class EntityCDMMappingException extends RuntimeException{
    public EntityCDMMappingException(String message) {
        super(message);
    }

    public EntityCDMMappingException(Throwable cause) {
        super(cause);
    }

    public EntityCDMMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
