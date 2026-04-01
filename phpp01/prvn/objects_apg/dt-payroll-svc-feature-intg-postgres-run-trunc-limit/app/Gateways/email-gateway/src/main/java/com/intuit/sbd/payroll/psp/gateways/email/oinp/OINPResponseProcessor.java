package com.intuit.sbd.payroll.psp.gateways.email.oinp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPKafkaResponse;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.stereotype.Service;


@Service
public class OINPResponseProcessor {

    private static final SpcfLogger sfLogger = Application.getLogger(OINPResponseProcessor.class);

    //This method is used to process the response from OINP service
    protected void processResponse(OINPKafkaResponse response, EventEmailTemplate pTemplate, String intuitTid, IEventEmail pEmailEvent) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("===========================================");
            sfLogger.debug("Processing service response...");
            sfLogger.debug("===========================================");
        }

        //TODO: Does this have to be checked???
        if (response == null) {
            String errStr = String.format("Service error (response is null) [Service Intuit ID: %s]", intuitTid);

            pTemplate.serviceFault(errStr, pEmailEvent.getRecipientId());

            throw new EmailServiceException("OINP: Service is not responding to requests. A request " +
                    "was made to send email, but a null response was received " +
                    "[Service Intuit TID: " + intuitTid + "]");
        } else {

            //if response returned, process response
            processSuccessResponse(intuitTid, pEmailEvent, pTemplate);
        }

    }

    private void processSuccessResponse(String intuitTid, IEventEmail pEmailEvent, EventEmailTemplate pTemplate) {
        //
        // error == null (no errors)
        //

        String recipientId = pEmailEvent.getRecipientId();
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("   ServiceTransactionId: " + intuitTid + ", Recipient ID: " + recipientId);
        }

        pTemplate.emailSent(recipientId);
    }
}
