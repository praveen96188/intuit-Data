package com.intuit.sbd.payroll.psp.gateways.email.txe;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.model.ExactTargetResult;
import com.intuit.sbd.payroll.psp.emailsender.model.ExactTargetResults;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.httpclient.HttpStatus;

import java.util.Arrays;
import java.util.List;

public class TxeExactTargetResponseProcessor {


    private static final SpcfLogger sfLogger = Application.getLogger(TxeExactTargetResponseProcessor.class);

    private static final List<Integer> SUCCESS_HTTP_STATUSES = Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT, HttpStatus.SC_CREATED);

    //This method is used to process the response from TXE service
    protected void processResponse(EmailResponse response, EventEmailTemplate pTemplate, String pServiceTransactionId, List<IEventEmail> pEmailBatch) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("===========================================");
            sfLogger.debug("Processing service response...");
            sfLogger.debug("===========================================");
        }

        if (response == null) {
            String errStr = String.format("Service error (response is null) [Service Transaction ID: %s]", pServiceTransactionId);

            pTemplate.serviceFault(errStr, pEmailBatch);

            throw new EmailServiceException("TXE Service is not responding to requests. A request " +
                    "was made to send email, but a null response was received " +
                    "[Service Transaction ID: " + pServiceTransactionId + "]");
        }

        switch (response.getStatus()) {
            case HttpStatus.SC_BAD_REQUEST:
                processBadRequestResponse(response, pTemplate, pServiceTransactionId, pEmailBatch);
                break;
            case HttpStatus.SC_OK:
                processOKResponse(response, pTemplate, pServiceTransactionId);
                break;
            default:
                processServiceErrorResponse(response, pTemplate, pServiceTransactionId, pEmailBatch);
                break;

        }


    }

    private void processOKResponse(EmailResponse response, EventEmailTemplate pTemplate, String pServiceTransactionId) {
        //
        // error == null (no errors)
        //

        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("   ServiceTransactionId: " + pServiceTransactionId);
        }

        ExactTargetResults exactTargetResults = response.getResult();

        for (ExactTargetResult result : exactTargetResults.getResult()) {

            String recipientId = result.getRecipientId();

            if (sfLogger.isDebugEnabled()) {
                sfLogger.debug("   RecipientId: " + recipientId + "Result Status : " + result.isSuccess());
            }


            pTemplate.emailSent(recipientId);

        }
    }

    private void processServiceErrorResponse(EmailResponse response, EventEmailTemplate pTemplate, String pServiceTransactionId, List<IEventEmail> pEmailBatch) {

        // SYSTEM
        //
        // Retry will be attempted for each destination
        //
        String errStr = String.format("Service system error (%s) [Service Transaction ID: %s]", response.getStatusInfo(), pServiceTransactionId);

        pTemplate.serviceReturned(errStr, pEmailBatch);

        throw new EmailProcessingException(processError(response, null, pServiceTransactionId));
    }

    private void processBadRequestResponse(EmailResponse response, EventEmailTemplate pTemplate, String pServiceTransactionId, List<IEventEmail> pEmailBatch) {

        //
        // Retry will *not* be attempted for each destination
        //


        String errStr = String.format("Service input error (%s) [Service Transaction ID: %s]", response.getStatusInfo(), pServiceTransactionId);

        pTemplate.failedValidation(errStr, pEmailBatch);

        throw new EmailServiceException(processError(response, null, pServiceTransactionId));
    }

    /*
     * Log error
     */
    @SuppressWarnings("unchecked")
    protected String processError(EmailResponse pError, String pRecipientId, String pServiceTransactionId) {
        StringBuilder err = new StringBuilder();

        err.append(EmailUtils.sfNewLine);

        if (pError != null) {
            err.append("  *** Service Error Details ***");
            err.append(EmailUtils.sfNewLine);

            err.append("  * Service Transaction ID: ");
            err.append(pServiceTransactionId);
            err.append(EmailUtils.sfNewLine);

            err.append("  * Recipient ID:           ");
            err.append((pRecipientId != null) ? pRecipientId : "<unknown>");
            err.append(EmailUtils.sfNewLine);

            err.append("  * Error info :         ");
            err.append(pError.getStatusInfo());
            err.append(EmailUtils.sfNewLine);

            err.append("  * Error Code:             ");
            err.append(pError.getStatus());
            err.append(EmailUtils.sfNewLine);

            err.append("  *****************************");
        } else {
            err.append("An unspecified error has occurred in the IAS Notification Service:");
            err.append(EmailUtils.sfNewLine);
            err.append("  [ Service Transaction ID ] ");
            err.append(pServiceTransactionId);
            err.append(EmailUtils.sfNewLine);
            err.append("  [ Recipient ID ] ");
            err.append((pRecipientId != null) ? pRecipientId : "<unknown>");
        }

        return err.toString();
    }
}

