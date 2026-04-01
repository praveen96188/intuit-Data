package com.intuit.sbd.payroll.psp.emailsender.service;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.emailsender.processor.EmailResponseProcessor;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.sun.jersey.api.client.*;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TransactionalEmailSendGridStrategy extends EmailSendStrategyAbstract implements EmailSendStrategy {

    private final Logger logger = LoggerFactory.getLogger(TransactionalEmailSendGridStrategy.class);

    @Autowired
    private TransactionalEmailSendGridStrategy transactionalEmailSendGridStrategy;

    @Autowired
    public TransactionalEmailSendGridStrategy(EmailResponseProcessor emailResponseProcessor, EmailRequestValidator emailRequestValidator,
                                              Client client, EmailSettings emailSettings) {
        super(client, emailSettings, emailRequestValidator, emailResponseProcessor);
    }

    @Override
    protected void preProcess(EmailRequest emailRequest) {
        emailRequestValidator.validate(emailRequest);
        emailRequest.setMediaType("");
    }

    @Override
    protected EmailResponse process(EmailRequest emailRequest) {
        WebResource.Builder builder = getWebResourceBuilder();
        String intuit_tid = SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");
        builder.header("intuit_tid", intuit_tid);
        ClientResponse clientResponse = builder.post(ClientResponse.class, emailRequest);
        EmailResponse response = null;
        try {
            response = emailResponseProcessor.getEmailResponse(clientResponse);
        } finally {
            if (Objects.nonNull(clientResponse)) {
                clientResponse.close();
            }
        }


        return response;
    }

    @Override
    protected void postProcess(EmailRequest emailRequest, EmailResponse response) throws Exception {
        super.postProcess(emailRequest, response);
        emailRequest.setMediaType("");
    }

    @Override
    public EmailStrategyType getSupportedEmailStrategyType() {
        return EmailStrategyType.SendGrid;
    }
}