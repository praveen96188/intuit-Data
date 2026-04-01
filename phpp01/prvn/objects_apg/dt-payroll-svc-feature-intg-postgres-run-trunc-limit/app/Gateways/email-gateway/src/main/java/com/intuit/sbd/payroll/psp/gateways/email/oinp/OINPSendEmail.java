package com.intuit.sbd.payroll.psp.gateways.email.oinp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPKafkaResponse;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSenderService;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbg.psp.events.publisher.kafka.exceptions.KafkaPublisherException;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OINPSendEmail {

    private final SpcfLogger sfLogger = Application.getLogger(OINPSendEmail.class);

    private final String intuit_tid = "intuit_tid";

    private final OINPResponseProcessor processOINPResponse;

    private OINPBulkRequestHelper requestHelper;

    private EmailSenderService emailSenderService;

    private OINPRequestValidator oinpRequestValidator;

    @Autowired
    public OINPSendEmail(OINPBulkRequestHelper oinpBulkRequestHelper, EmailSenderService emailSenderService, OINPResponseProcessor oinpResponseProcessor, OINPRequestValidator oinpRequestValidator)
    {
        this.requestHelper = oinpBulkRequestHelper;
        this.emailSenderService = emailSenderService;
        this.processOINPResponse = oinpResponseProcessor;
        this.oinpRequestValidator = oinpRequestValidator;
    }

    //This is used to send emails to OINP template wise - All email events in a batch for a template are processed
    public void sendTemplateMails(EventEmailTemplate pTemplate) throws Exception {

        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("================================================");
            sfLogger.debug("Sending email request to OINP service...");
            sfLogger.debug("================================================");
        }

        int bulkEmailRecipientCount = pTemplate.getRecipientCount();
        int reqErrCount =0;

        //1. Validate template and its OINP properties
        oinpRequestValidator.validateTemplate(pTemplate.getTemplateId().toString());

        //2. Get set of all common attributes for a given template
        Map<String,Object> commonTemplateAttributes = requestHelper.getCommonTemplateAttributes(pTemplate);

        //3. within template - batches of <100>
        for (List<IEventEmail> emailBatch : pTemplate.getRecipientsToTransmit()) {
            if (emailBatch.isEmpty()) continue;

            for (IEventEmail eventEmail : emailBatch) {

                try {
                    sendMail(pTemplate, eventEmail, commonTemplateAttributes);
                } catch (EmailProcessingException e) {
                    sfLogger.warn("OINP: A recoverable exception occurred during email event processing [template id: " +
                    pTemplate.getTemplateId().toString() +"]", e);
                    reqErrCount++;
                }
            }
        }

        sfLogger.info(String.format("OINP: Template Id: %s, Total recipients: %d, Error count with failedValidation: %d",pTemplate.getTemplateId(),bulkEmailRecipientCount,reqErrCount));
    }

    private void sendMail(EventEmailTemplate pTemplate, IEventEmail eventEmail, Map<String,Object> commonTemplateAttributes) {

        String curTransactionId = "<unknown>";

        try{
            EmailRequest emailRequest = requestHelper.getEmailRequest(pTemplate.getTemplateId().toString(), eventEmail, commonTemplateAttributes);

            curTransactionId = emailRequest.getIntuitTid();

            StopWatch timer = StopWatch.startTimer();

            //Send Request
            OINPKafkaResponse response = (OINPKafkaResponse) emailSenderService.sendMailViaOINP(emailRequest);

            sfLogger.info("OINP: Sending Email Via OINP.Template Id: " + pTemplate.getTemplateId() + " . Time Taken : " + timer.stop().getElapsedTimeString());

            //Process Response
            processOINPResponse.processResponse(response, pTemplate, curTransactionId, eventEmail);

        } catch (KafkaPublisherException e) {

            pTemplate.serviceFault("OINP: Service fault [Service Intuit TID: " + curTransactionId + "]", eventEmail.getRecipientId());

            throw new EmailServiceException("OINP: A request " +
            "was made to send email via Kafka, but KafkaPublisherException was thrown " +
                    "[Service Intuit TID: " + curTransactionId + "]",e);

        } catch (EmailServiceException | EmailProcessingException e) {
            throw e;
        } catch (Throwable t) {
            if (eventEmail != null) {
                pTemplate.serviceFault("OINP: Service fault [Service Intuit TID: " + curTransactionId + "]", eventEmail.getRecipientId());
            }

            throw new EmailServiceException("OINP: A service error has occurred in the OINP Notification Service " +
                    "[Service Intuit TID: " + curTransactionId + "]", t);
        }
    }

}

