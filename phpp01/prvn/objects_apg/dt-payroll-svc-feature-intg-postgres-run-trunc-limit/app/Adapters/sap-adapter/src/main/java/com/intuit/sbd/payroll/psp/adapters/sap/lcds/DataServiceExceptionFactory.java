/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/lcds/DataServiceExceptionFactory.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.lcds;

import flex.messaging.MessageException;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Helper class for creating and throwing exceptions in SAP LCDS Data Services.
 *
 * @author Joe Warmelink
 */
public class DataServiceExceptionFactory {
    private SpcfLogger logger;

    public DataServiceExceptionFactory(SpcfLogger eLogger) {
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

    public void throwCompanyNotFound(String criteria, String criteriaValue, Throwable rootCause) {
        MessageException dse = new MessageException();
        String message = String.format(COMPANY_NOT_FOUND_MESSAGE, criteria, criteriaValue);
        logger.debug(COMPANY_NOT_FOUND_MESSAGE, rootCause);
        dse.setMessage(message);
        dse.setRootCause(rootCause);
        throw dse;
    }

    public void throwCompanyNotFound(String criteria, String criteriaValue) {
        MessageException dse = new MessageException();
        String message = String.format(COMPANY_NOT_FOUND_MESSAGE, criteria, criteriaValue);
        logger.debug(COMPANY_NOT_FOUND_MESSAGE);
        dse.setMessage(message);
        throw dse;
    }

    public void throwGetCompanyNoKeys() {
        MessageException dse = new MessageException();
        logger.debug(GET_COMPANY_NO_KEYS_MESSAGE);
        dse.setMessage(GET_COMPANY_NO_KEYS_MESSAGE);
        throw dse;
    }

    public void throwNoFillParameters() {
        MessageException dse = new MessageException();
        logger.debug(NO_FILL_PARAMETERS_MESSAGE);
        dse.setMessage(NO_FILL_PARAMETERS_MESSAGE);
        throw dse;
    }

    public void throwUnknownQuery(String className, String queryName) {
        MessageException dse = new MessageException();
        String message = String.format(UNKNOWN_QUERY_MESSAGE, className, queryName);
        logger.error(UNKNOWN_QUERY_MESSAGE);
        dse.setMessage(message);
        throw dse;
    }

    public void throwBadFillParameterCount(String className, String queryName, int requires, int found) {
        throwBadFillParameterCount(className, queryName, Integer.toString(requires), found);
    }

    public void throwBadFillParameterCount(String className, String queryName, String requires, int found) {
        MessageException dse = new MessageException();
        String message = String.format(BAD_FILL_PARAMETER_COUNT_MESSAGE, className, queryName, requires, found);
        logger.error(BAD_FILL_PARAMETER_COUNT_MESSAGE);
        dse.setMessage(message);
        throw dse;
    }

    public void throwErrorCreatingCompany(Throwable e) {
        MessageException dse = new MessageException();
        String message = String.format(COMPANY_CREATE_ERROR_MESSAGE, e.toString());
        logger.debug(message);
        dse.setMessage(message);
        dse.setRootCause(e);
        throw dse;
    }

    public void throwErrorUpdatingCompany(Throwable e) {
        MessageException dse = new MessageException();
        String message = String.format(COMPANY_UPDATE_ERROR_MESSAGE, e.toString());
        logger.debug(message);
        dse.setMessage(message);
        dse.setRootCause(e);
        throw dse;
    }

    public void throwDeleteCompanyNotImplemented() {
        MessageException dse = new MessageException();
        logger.error(COMPANY_DELETE_NOT_IMPLEMENTED_MESSAGE);
        dse.setMessage(COMPANY_DELETE_NOT_IMPLEMENTED_MESSAGE);
        throw dse;
    }

    public void throwPropertyDoesNotExistException(String methodName, String propertyName, Class classType) {
        MessageException dse = new MessageException();
        String message = String.format(PROPERTY_DOES_NOT_EXIST_MESSAGE, methodName, propertyName, classType.toString());
        logger.error(message);
        dse.setMessage(message);
        throw dse;
    }

    public void throwEntityProxyException(String methodName, Throwable e) {
        MessageException dse = new MessageException();
        String message = String.format(ENTITY_PROXY_EXCEPTION_MESSAGE, methodName, e.toString());
        logger.error(message, e);
        dse.setMessage(message);
        dse.setRootCause(e);
        throw dse;
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

        String details = t.getMessage();
        if (details.startsWith("java.lang.Exception : ")) {
            details = details.substring("java.lang.Exception : ".length());
        }
        // strip our exception type
        if (details.startsWith("com.intuit.sbd.payroll.psp.adapters.sap.SAPException : ")) {
            details = details.substring("com.intuit.sbd.payroll.psp.adapters.sap.SAPException : ".length());
        }

        dse.setDetails(details);
        throw dse;
    }

    private static final String COMPANY_NOT_FOUND_MESSAGE = "Company not found for search: %1$s = %2$s";
    private static final String NO_FILL_PARAMETERS_MESSAGE = "CompanyAssembler.fill() requires additional parameters.";
    private static final String UNKNOWN_QUERY_MESSAGE = "Error performing %1$s.fill(). Query name " +
            "'%2$s' unknown.";
    private static final String BAD_FILL_PARAMETER_COUNT_MESSAGE = "Error performing %1$s.fill(). " +
            "Query '%2$s' has parameter count mismatch. Requires %3$s but found %4$s.";
    private static final String GET_COMPANY_NO_KEYS_MESSAGE = "Company not found.  Missing primary key(s) " +
            "in getItem() map.";
    private static final String COMPANY_CREATE_ERROR_MESSAGE = "Error creating company.  Error: %1$s";
    private static final String COMPANY_UPDATE_ERROR_MESSAGE = "Error updating company.  Error: %1$s";
    private static final String COMPANY_DELETE_NOT_IMPLEMENTED_MESSAGE = "Error deleting company.  Delete is not " +
            "implemented for Company.";
    private static final String PROPERTY_DOES_NOT_EXIST_MESSAGE = "Error in method %1$s.  Property '%2$s' does " +
            "not exist on class '%3$s'.";
    private static final String ENTITY_PROXY_EXCEPTION_MESSAGE = "Error in method %1$s.  Error: %2$s";
    private static final String RETHROW_EXCEPTION_MESSAGE = "Error in method %1$s.%2$s().  Error: %3$s";

}
