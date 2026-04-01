package com.intuit.sbd.payroll.psp.emailsender.service;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.filter.RequestFilter;
import com.intuit.sbd.payroll.psp.emailsender.processor.EmailResponseProcessor;
import com.intuit.sbd.payroll.psp.emailsender.service.OINP.OINPRequestUtility;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.sun.jersey.api.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;


public abstract class EmailSendStrategyAbstract implements EmailSendStrategy {

    private final Logger logger = LoggerFactory.getLogger(EmailSendStrategyAbstract.class);

    protected Client client;
    protected EmailSettings emailSettings;
    protected EmailRequestValidator emailRequestValidator;
    protected EmailResponseProcessor emailResponseProcessor;


    public EmailSendStrategyAbstract(Client client, EmailSettings emailSettings, EmailRequestValidator emailRequestValidator, EmailResponseProcessor emailResponseProcessor) {
        this.client = client;
        this.emailSettings = emailSettings;
        this.emailRequestValidator = emailRequestValidator;
        this.emailResponseProcessor = emailResponseProcessor;
    }

    public WebResource.Builder getWebResourceBuilder() {
        WebResource resource = client.resource(getUrl());
        WebResource.Builder builder = resource.getRequestBuilder();
        processFilters(builder);
        return builder;
    }

    @Override
    public EmailResponse sendMail(EmailRequest emailRequest) throws Exception {
        String springProfile = System.getProperty("spring.profiles.active");
        if(springProfile.equalsIgnoreCase("ds2") || springProfile.equalsIgnoreCase("stg")) {
            logger.info("Parallel Env Mock sendMail springProfile={} emailStrategyType={}", springProfile, emailRequest.getEmailStrategyType());
            logger.info("Parallel Env Mock sendMail TxE toEmailAddresses={} subject={}", emailRequest.getToEmailAddresses(), emailRequest.getSubject());
            logger.info("Parallel Env Mock sendMail OINP toEmail={} subject={}", Objects.isNull(emailRequest.getTemplateAttributes()) ? "" : emailRequest.getTemplateAttributes().get(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES),
                    Objects.isNull(emailRequest.getTemplateAttributes()) ? "" : emailRequest.getTemplateAttributes().get(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT));
            HttpServiceResponse mockHttpServiceResponse =  new HttpServiceResponse.HttpServiceResponseBuilder().statusCode(200).build();
            EmailResponse mockResponse = new EmailResponse(mockHttpServiceResponse);
            logger.info("Parallel Env Mock sendMail return mockResponse={}", mockResponse);
            return mockResponse;
        }

        EmailResponse response = null;
        try {
            preProcess(emailRequest);
            response = process(emailRequest);
            postProcess(emailRequest, response);

            //TODO: After completion of all phases, MultiValued Map for headers will be replaced by HTTPServiceResponse headers. The if condition can be removed
            if(response.getHeaders()!= null) {
                logger.info("response info: responseStatus={}, responseBody={}, responseTid={}", response.getStatus(),
                        response.getResponseBody(), response.getHeaders().get("intuit_tid"));
            } else if(response.getHttpServiceResponseHeaders()!=null)
            {
                logger.info("OINP: response info: responseStatus={}, responseBody={}, responseTid={}", response.getStatus(),
                        response.getResponseBody(), response.getHttpServiceResponseHeaders().get("intuit_tid"));
            }

        } catch (
                Exception e) {
            throw e;
        }
        return response;
    }

    protected abstract EmailResponse process(EmailRequest emailRequest) throws Exception;

    protected void postProcess(EmailRequest emailRequest, EmailResponse response) throws Exception {

    }

    protected void preProcess(EmailRequest emailRequest) {

    }

    protected String getBaseUrl() {
        return emailSettings.getUrl();
    }

    private void processFilters(WebResource.Builder builder) {
        // Process all request filters
        List<RequestFilter> requestFilters = emailSettings.getRequestFilters();
        requestFilters.forEach(requestFilter -> {
            requestFilter.filter(builder);
        });
    }

    protected String getUrl() {
        String endpoint = null;
        switch (getSupportedEmailStrategyType()) {
            case SendGrid:
                endpoint = emailSettings.getSendgridApi();
                break;
            case SendGridWithAttachments:
                endpoint = emailSettings.getSendGridUploadAttachmentApi();
                break;
            case ExactTarget:
                endpoint = emailSettings.getExactTargetApi();
                break;
        }
        return getBaseUrl() + endpoint;
    }
}