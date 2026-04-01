package com.intuit.sbd.payroll.psp.exceptions;

public class MoneyMovementControlException extends RuntimeException {
    public MoneyMovementControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public MoneyMovementControlException(String message) {
        super(message);
    }
}
