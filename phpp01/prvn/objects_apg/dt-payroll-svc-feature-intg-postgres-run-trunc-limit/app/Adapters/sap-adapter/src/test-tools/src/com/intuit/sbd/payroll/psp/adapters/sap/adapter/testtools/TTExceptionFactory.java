/*
 * $Id: //psp/dev/Adapters/SAP/test-tools/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/testtools/TTExceptionFactory.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter.testtools;

import flex.messaging.MessageException;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Helper class for creating and throwing exceptions in SAP LCDS Data Services.
 *
 * @author Joe Warmelink
 */
public class TTExceptionFactory {
    private SpcfLogger logger;

    public TTExceptionFactory(SpcfLogger eLogger) {
        logger = eLogger;
    }

    public static MessageException createDSException(String message, String details) {
        MessageException me = new MessageException();
        me.setMessage(message);
        me.setDetails(details);
        return me;
    }

    public static MessageException createDSException(String message, String details, Throwable rootCause) {
        MessageException dse = createDSException(message, details);
        dse.setRootCause(rootCause);
        return dse;
    }

    public void rethrowException(String className, String methodName, Throwable e) {
        MessageException dse = new MessageException();
        String message = String.format(RETHROW_EXCEPTION_MESSAGE, className, methodName, e.toString());
        dse.setMessage(message);
        dse.setRootCause(e);

        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }

        String details = t != null ? t.getMessage() : e.getMessage();

        dse.setDetails(details);
        throw dse;
    }

    private static final String RETHROW_EXCEPTION_MESSAGE = "Error in method %1$s.%2$s().  Error: %3$s";
}