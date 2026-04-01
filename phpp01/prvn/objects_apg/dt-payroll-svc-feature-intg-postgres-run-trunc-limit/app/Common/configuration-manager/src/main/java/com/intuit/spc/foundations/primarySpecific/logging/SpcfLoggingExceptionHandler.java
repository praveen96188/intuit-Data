package com.intuit.spc.foundations.primarySpecific.logging;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class SpcfLoggingExceptionHandler implements com.lmax.disruptor.ExceptionHandler {

    public SpcfLoggingExceptionHandler() {
        System.out.println("SpcfLoggingExceptionHandler initialized");
    }

    @Override
    public void handleEventException(Throwable e, long sequence, Object event) {
        System.err.println("Exception in handleEventException, msg=" + ExceptionUtils.getStackTrace(e));
    }

    @Override
    public void handleOnStartException(Throwable e) {
        System.err.println("Exception in handleOnStartException, msg=" + ExceptionUtils.getStackTrace(e));
    }

    @Override
    public void handleOnShutdownException(Throwable e) {
        System.err.println("Exception in handleOnShutdownException, msg=" + ExceptionUtils.getStackTrace(e));
    }
}
