/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/AdapterExceptionFactory.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.SAPException;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * Helper class for creating and throwing exceptions in the SAP Adapter.
 *
 * @author Joe Warmelink
 */
public class AdapterExceptionFactory {
    private SpcfLogger logger;

    public AdapterExceptionFactory(SpcfLogger logger) {
        this.logger = logger;
    }

    public boolean errorsOccurred(List<ProcessResult> results) {
        for (ProcessResult result : results) {
            if (!result.isSuccess())
                return true;
        }

        return false;
    }

    public void throwGenericException(String errorMessage, Throwable t) throws Throwable {
        if(t instanceof SAPException) {
            // rethrow if the exception is one of ours
            throw t;
        }

        String message = String.format(GENERIC_MESSAGE, errorMessage, t.getMessage());
        String loggedMessage = message + "\nUser: " + Application.getCurrentPrincipal().getName() + "\n";
        logger.warn(loggedMessage, t);
        throw new SAPException(message, t);
    }

    public void throwGenericException(String errorMessage, String sourceSystem, String companyId, String entityType, String entityId, Throwable t) throws Throwable {
        if(t instanceof SAPException) {
            // rethrow if the exception is one of ours
            throw t;
        }

        if (entityId == null) {
            entityId = "null";
        }

        String message = errorMessage + "\n" + String.format(COMPANY_ENTITY_TARGET_FORMAT, sourceSystem, companyId);
        message += "\t" + String.format(ENTITY_TARGET_FORMAT, entityType, entityId);
        message = String.format(GENERIC_MESSAGE, message, t.getMessage());
        String loggedMessage = message + "\nUser: " + Application.getCurrentPrincipal().getName() + "\n";
        logger.error(loggedMessage, t);
        throw new SAPException(message, t);
    }

    public void throwGenericException(String errorMessage, String sourceSystem, String companyId, Throwable t) throws Throwable {
        if(t instanceof SAPException) {
            // rethrow if the exception is one of ours
            throw t;
        }

        String message = errorMessage + "\n" + String.format(COMPANY_ENTITY_TARGET_FORMAT, sourceSystem, companyId);
        message = String.format(GENERIC_MESSAGE, message, t.getMessage());
        String loggedMessage = message + "\nUser: " + Application.getCurrentPrincipal().getName() + "\n";
        logger.error(loggedMessage, t);
        throw new SAPException(message, t);
    }

    public void throwGenericException(String errorMessage) throws Exception {
        logger.debug(errorMessage);
        throw new SAPException(errorMessage);
    }

    public void throwGenericException(String pErrorMessage, ProcessResult pProcessResult) throws Exception {
        StringBuffer message = new StringBuffer();
        formatErrorMessages(message, pProcessResult.getMessages());
        String userMessage = String.format(GENERIC_MESSAGE, pErrorMessage, message.toString());
        logger.debug(userMessage);
        throw new SAPException(userMessage);
    }

    public void throwGenericException(String errorMessage, String targetEntityType, String targetEntityId, List<ProcessResult> prList) throws Exception {
        String message = String.format(GENERIC_MESSAGE, errorMessage, generateErrorMessage(prList, targetEntityType, targetEntityId));
        logger.debug(message);
        throw new SAPException(message);
    }

    public void throwGenericException(String errorMessage, List<ProcessResult> prList) throws Exception {
        String message = String.format(GENERIC_MESSAGE, errorMessage, formatErrorMessages(prList).toString());
        logger.debug(message);
        throw new SAPException(message);
    }

    public SAPException companyNotFoundException() {
        return new SAPException("The company was not found.  It is likely that the Source Company ID has changed.  Please search for the company again.");
    }

    /**
     * Format for converting a process message return into an error string to
     * display to the end user.
     *
     * <LEVEL> (<CODE>): <MESSAGE>
     * i.e. ERROR (206): Invalid settlement type.
     */
    private static final String ERROR_FORMAT = "%1$s (%2$s) %3$s";

    /**
     * Detail appended to the ERROR_FORMAT for help in identifying the affected data in the DB
     *
     * Company ID: <SourceSystem>:<CompanyId>    <TargetEntityType> ID: <TargetEntityId>
     * i.e. Company ID: QBOE:1234567
     */
    private static final String COMPANY_ENTITY_TARGET_FORMAT = " Company ID: %1$s:%2$s";

    /**
     * Detail appended to the ERROR_FORMAT for help in identifying the affected data in the DB
     *
     * <TargetEntityType> ID: <TargetEntityId>
     * i.e. Transaction ID: QSDC-1234-FAC13-23AG
     */
    private static final String ENTITY_TARGET_FORMAT = "%1$s ID: %2$s \n";

    private String generateErrorMessage(List<ProcessResult> prList, String targetEntityType, String targetEntityId) {
        StringBuffer details = formatErrorMessages(prList);
        details.append( String.format(ENTITY_TARGET_FORMAT, targetEntityType, targetEntityId) );
        return details.toString();
    }

    private String generateErrorMessage(List<ProcessResult> prList, String targetEntityType, String targetEntityId, String pSourceSystemCd, String pSourceCompanyId) {
        StringBuffer details = formatErrorMessages(prList);
        details.append( String.format(COMPANY_ENTITY_TARGET_FORMAT, pSourceSystemCd, pSourceCompanyId) );
        details.append("\t").append(String.format(ENTITY_TARGET_FORMAT, targetEntityType, targetEntityId));
        return details.toString();
    }

    private StringBuffer formatErrorMessages(List<ProcessResult> prList) {
        StringBuffer details = new StringBuffer();

        for (ProcessResult processResult : prList) {
            if (!processResult.isSuccess()) {
                formatErrorMessages(details, processResult.getMessages());
            }
        }

        return details;
    }

    private void formatErrorMessages(StringBuffer details, MessageList messages) {
        for (Message m : messages) {
            details .append( String.format(ERROR_FORMAT, m.getLevel(), m.getMessageCode(),  m.getMessage()) )
                    .append("\nat ")
                    .append(m.getInterestingStackElement())
                    .append("\n");

        }
    }

    private final static String GENERIC_MESSAGE = "%1$s\nDetails: %2$s";
}
