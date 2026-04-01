package com.intuit.sbd.payroll.psp.domainsecondary;

public class DeductionProviderNotFoundException extends RuntimeException {
    public DeductionProviderNotFoundException(String message, Throwable throwable) {
        super(message,throwable);
    }
}
