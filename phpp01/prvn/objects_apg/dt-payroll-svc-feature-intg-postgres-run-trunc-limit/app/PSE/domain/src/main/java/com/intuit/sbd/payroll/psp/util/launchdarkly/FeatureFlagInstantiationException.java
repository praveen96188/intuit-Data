package com.intuit.sbd.payroll.psp.util.launchdarkly;

public class FeatureFlagInstantiationException  extends RuntimeException {
    public FeatureFlagInstantiationException(String message){
        super(message);
    }

    public FeatureFlagInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
