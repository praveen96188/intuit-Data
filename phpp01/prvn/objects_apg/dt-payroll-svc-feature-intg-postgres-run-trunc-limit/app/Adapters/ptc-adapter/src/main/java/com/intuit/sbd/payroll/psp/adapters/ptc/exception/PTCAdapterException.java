package com.intuit.sbd.payroll.psp.adapters.ptc.exception;

public class PTCAdapterException extends RuntimeException {

    public PTCAdapterException(String message) {
        super(message);
    }

    public PTCAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
