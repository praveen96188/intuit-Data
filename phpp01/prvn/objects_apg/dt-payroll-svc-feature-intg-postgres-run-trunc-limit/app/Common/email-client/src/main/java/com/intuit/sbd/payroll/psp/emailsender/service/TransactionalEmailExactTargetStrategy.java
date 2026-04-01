package com.intuit.sbd.payroll.psp.emailsender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbd.payroll.psp.emailsender.NotificationDataType;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.processor.EmailResponseProcessor;
import com.intuit.sbd.payroll.psp.emailsender.utils.UtilsHelper;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.sun.jersey.api.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class TransactionalEmailExactTargetStrategy extends EmailSendStrategyAbstract implements EmailSendStrategy {
    private final Logger logger = LoggerFactory.getLogger(TransactionalEmailExactTargetStrategy.class);

    private static final String sfSourceSystem = "PSP";
    private static final String sfProviderSystem = "PSPEmailGateway";
    private static final UtilsHelper utilsHelper = new UtilsHelper();


    @Autowired
    public TransactionalEmailExactTargetStrategy(EmailResponseProcessor emailResponseProcessor, EmailRequestValidator emailRequestValidator,
                                                 Client client, EmailSettings emailSettings) {
        super(client, emailSettings, emailRequestValidator, emailResponseProcessor);
    }


    @Override
    public EmailStrategyType getSupportedEmailStrategyType() {
        return EmailStrategyType.ExactTarget;
    }

    @Override
    protected void preProcess(EmailRequest emailRequest) {

    }

    @Override
    protected EmailResponse process(EmailRequest emailRequest) throws JsonProcessingException {
        WebResource.Builder builder = getWebResourceBuilder();
        String intuit_tid = "PSP-"+SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");
        addHeaders(intuit_tid, builder);
        String emailRequestString = null;

        logEmailDigest(intuit_tid, emailRequest);
        emailRequestString = utilsHelper.getJsonString(emailRequest);
        ClientResponse clientResponse = builder.post(ClientResponse.class, emailRequestString);
        EmailResponse response = null;
        try {
            response = emailResponseProcessor.getEmailResponse(clientResponse, emailRequest);
        } finally {
            if (Objects.nonNull(clientResponse)) {
                clientResponse.close();
            }
        }

        return response;


    }


    @Override
    protected void postProcess(EmailRequest emailRequest, EmailResponse response) throws Exception {

    }

    protected void logEmailDigest(String transactionId, EmailRequest pBody) {
        //
        // PSRV003631 - Log a digest of email id's so we can detect redundant emails
        //

        StringBuilder digest = new StringBuilder();

        for (NotificationDataType.Destinations.Destination dest : pBody.getSendRequest().getNotification().getDestinations().getDestination()) {
            digest.append(String.format("[Txn id: %s, Rec id: %s]%n", "PSP-" + transactionId, dest.getRecipientId()));
        }

        if (digest.length() > 0) {
            logger.info(String.format("%n<begin email digest>%n%s<end email digest>", digest.toString()));
        }
    }

    protected WebResource.Builder addHeaders(String intuit_tid, WebResource.Builder builder) {
        builder.header("intuit_tid", intuit_tid).header("SourceSystem", sfSourceSystem).header("CallerSystem", sfProviderSystem);
        return builder;
    }
}