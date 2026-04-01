package com.intuit.sbd.payroll.psp.emailsender.builder;

import com.intuit.sbd.payroll.psp.emailsender.SendRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * This class act as builder to generate email request
 * TODO: On completion of all Phases across all services, builder in EmailRequest will be directly used, this class can be removed
 *
 * @author vishalb849
 */
public class EmailRequestBuilder {
    private String[] toEmailAddresses;
    private String fromEmailAddress;
    private String htmlContent;
    private String subject;
    private String replyEmailAddress;
    private String fromEmailDisplayName;
    private List<String> attachmentList;
    private String product;
    private Boolean pHighPriority;
    private EmailStrategyType emailStrategyType;
    private String mediaType;
    private SendRequest sendRequest;

    //Will internally build email request using this property
    private EmailRequest emailRequest;

    //TODO need to refactor the code so that a separate emailrequest constructor is present for single email
    public EmailRequestBuilder(String toEmailAddresses, String subject, String htmlContent) {
        this.toEmailAddresses = toEmailAddresses.split(",");
        this.subject = subject;
        this.htmlContent = htmlContent;
        this.emailRequest = EmailRequest.builder().toEmailAddresses(toEmailAddresses.split(","))
                .subject(subject)
                .htmlContent(htmlContent)
                .build();
    }

    public String[] getToEmailAddresses() {
        return toEmailAddresses;
    }

    public EmailRequestBuilder(SendRequest sendRequest) {
        this.sendRequest = sendRequest;
        this.emailRequest = EmailRequest.builder()
                .sendRequest(sendRequest)
                .build();

    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public EmailRequestBuilder setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
        this.emailRequest.setFromEmailAddress(fromEmailAddress);
        return this;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public String getFromEmailDisplayName() {
        return fromEmailDisplayName;
    }

    public EmailRequestBuilder setFromEmailDisplayName(String fromEmailDisplayName) {
        this.fromEmailDisplayName = fromEmailDisplayName;
        this.emailRequest.setFromEmailDisplayName(fromEmailDisplayName);
        return this;
    }

    public String getReplyEmailAddress() {
        return replyEmailAddress;
    }

    public EmailRequestBuilder setReplyEmailAddress(String replyEmailAddress) {
        this.replyEmailAddress = replyEmailAddress;
        this.emailRequest.setReplyEmailAddress(replyEmailAddress);
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getAttachmentList() {
        return attachmentList;
    }

    public EmailRequestBuilder setAttachmentList(List<String> attachmentList) {
        this.attachmentList = attachmentList;
        setMediaType(MediaType.MULTIPART_FORM_DATA);
        this.emailRequest.setAttachmentList(attachmentList);
        return this;
    }

    public String getProduct() {
        return product;
    }

    public EmailRequestBuilder setProduct(String product) {
        this.product = product;
        return this;
    }

    public Boolean getPriority() {
        return pHighPriority;
    }

    public EmailRequestBuilder setPriority(Boolean pHighPriority) {
        this.pHighPriority = pHighPriority;
        this.emailRequest.setPHighPriority(pHighPriority);
        return this;
    }

    public EmailStrategyType getEmailStrategyType() {
        return emailStrategyType;
    }

    public EmailRequestBuilder setEmailStrategyType(EmailStrategyType emailStrategyType) {
        this.emailStrategyType = emailStrategyType;
        this.emailRequest.setEmailStrategyType(emailStrategyType);
        return this;
    }

    public String getMediaType() {
        return mediaType;
    }

    public EmailRequestBuilder setMediaType(String mediaType) {
        this.mediaType = mediaType;
        this.emailRequest.setMediaType(mediaType);
        return this;
    }

    public EmailRequest build() {
        return emailRequest;
    }

    public SendRequest getSendRequest() {
        return sendRequest;
    }

    public EmailRequestBuilder setSendRequest(SendRequest sendRequest) {
        this.sendRequest = sendRequest;
        this.emailRequest.setSendRequest(sendRequest);
        return this;
    }
}