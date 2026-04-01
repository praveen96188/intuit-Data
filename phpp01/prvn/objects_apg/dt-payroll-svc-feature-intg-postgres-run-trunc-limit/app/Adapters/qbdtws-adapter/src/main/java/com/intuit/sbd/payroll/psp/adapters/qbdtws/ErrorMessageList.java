package com.intuit.sbd.payroll.psp.adapters.qbdtws;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessage;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessageLevel;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.text.MessageFormat;
import java.util.List;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 11, 2009
 * Time: 10:15:35 AM
 */
public class ErrorMessageList {
    private static SpcfLogger logger = PayrollServices.getLogger(ErrorMessageList.class);

    public static QBProcessingMessage unexpectedError() {
        return createErrorMessage(1);
    }

    public static QBProcessingMessage unexpectedError(String messageEntityType, String entitySourceSystemId, String details) {
        return createErrorMessage(4, messageEntityType, entitySourceSystemId, details);
    }

    public static QBProcessingMessage invalidArgument(String pArgumentName) {
        return createErrorMessage(2, pArgumentName);
    }

    public static QBProcessingMessage companyDoesNotExist(String pPSID) {
        return createErrorMessage(3, pPSID);
    }

    public static QBProcessingMessage invalidPin() {
        return createErrorMessage(5);
    }

    public static QBProcessingMessage fieldDataNotValid(String fieldName, String parent) {
        return createErrorMessage(6, fieldName, parent);
    }

    public static QBProcessingMessage unsupportedVersion(String version) {
        return createErrorMessage(7, version);
    }

    public static QBProcessingMessage billPaymentDoesNotExist(String sourcePaymentId, String sourceSystemCd, String sourceCompanyId) {
        return createErrorMessage(8, sourcePaymentId, sourceSystemCd, sourceCompanyId);
    }

    public static QBProcessingMessage invalidDate(String date, String fieldName) {
        return createErrorMessage(9, date, fieldName);
    }

    public static QBProcessingMessage agencyNumberMissingFor401kTaxTrackingType(String employeeFullName) {
        return createErrorMessage(QBProcessingMessageLevel.INFO, 10, employeeFullName);
    }

    public static QBProcessingMessage paymentAlreadySubmitted(String sourcePaymentId) {
        return createErrorMessage(11, sourcePaymentId);
    }

    public static QBProcessingMessage NoActiveCloudService() {
        return createErrorMessage(12);
    }

    public static QBProcessingMessage EmployessWithDuplicateSSN(String employee1Name, String ssn, String employee2Name) {
        return createErrorMessage(13, employee1Name, ssn, employee2Name);
    }

    public static QBProcessingMessage ServiceUnavailable() {
        return createErrorMessage(14);
    }

    public static QBProcessingMessage Non1099PayeeNotSupported() {
        return createErrorMessage(15);
    }
    /**
     * Merge a PayrollServices ProcessResult error messages into a response error message list.
     *
     * @param pr                PayrollServices ProcessingResult message list
     * @param processingMessages  Response document QBProcessingErrors list
     */
    public static void mergeResults(ProcessResult pr, List<QBProcessingMessage> processingMessages) {
        for (Message message : pr.getMessages()) {
            processingMessages.add(ErrorMessageList.convertPSPMessage(message));
        }
    }

    public static QBProcessingMessage convertPSPMessage(Message pPSPMessage) {
        Integer pspMessageCode = Integer.parseInt(pPSPMessage.getMessageCode());
        QBProcessingMessage processingMessage = new QBProcessingMessage();
        processingMessage.setCode(pspMessageCode);
        processingMessage.setLevel(QBProcessingMessageLevel.fromPSPMessageLevel(pPSPMessage.getLevel().name()));
        processingMessage.setMessage(pPSPMessage.getMessage());
        return processingMessage;
    }

    private static QBProcessingMessage createErrorMessage(int messageNumber, Object... args) {
        ErrorMessageDefinition definition = ErrorMessageDefinition.getMessageDefinition(messageNumber);
        return createErrorMessage(QBProcessingMessageLevel.ERROR, definition, args);
    }

    private static QBProcessingMessage createErrorMessage(QBProcessingMessageLevel pMessageLevel, int messageNumber, Object... args) {
        ErrorMessageDefinition definition = ErrorMessageDefinition.getMessageDefinition(messageNumber);
        return createErrorMessage(pMessageLevel, definition, args);
    }

    private static QBProcessingMessage createErrorMessage(QBProcessingMessageLevel pMessageLevel, ErrorMessageDefinition messageDefinition, Object... args) {
        QBProcessingMessage message = new QBProcessingMessage();
        message.setCode(messageDefinition.getNumber());

        MessageFormat format = new MessageFormat(messageDefinition.getMessageFormat());
        message.setMessage(format.format(args));
        message.setLevel(pMessageLevel);
        return message;
    }
}
