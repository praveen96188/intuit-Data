package com.intuit.sbd.payroll.psp.batchjobs.util;

public class NonBankSimSFTPException extends RuntimeException {

    public NonBankSimSFTPException() {
        super();
    }

    public NonBankSimSFTPException(String message) {
        super(message);
    }

    public NonBankSimSFTPException(Throwable cause) {
        super(cause);
    }

    public NonBankSimSFTPException(String message, Throwable cause) {
        super(message, cause);
    }

}