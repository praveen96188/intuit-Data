package com.intuit.sbd.payroll.psp.emailsender.service;

import com.intuit.sbd.payroll.psp.emailsender.domain.*;
import com.intuit.sbd.payroll.psp.emailsender.processor.EmailResponseProcessor;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.sun.jersey.api.client.*;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TransactionalEmailSendGridAttachmentStrategy extends EmailSendStrategyAbstract implements EmailSendStrategy {

    private final Logger logger = LoggerFactory.getLogger(TransactionalEmailSendGridAttachmentStrategy.class);

    @Autowired
    private TransactionalEmailSendGridStrategy transactionalEmailSendGridStrategy;

    @Autowired
    public TransactionalEmailSendGridAttachmentStrategy(EmailResponseProcessor emailResponseProcessor, EmailRequestValidator emailRequestValidator,
                                                        Client client, EmailSettings emailSettings) {
        super(client, emailSettings, emailRequestValidator, emailResponseProcessor);
    }

    @Override
    protected void preProcess(EmailRequest emailRequest) {
        String[] emailList = emailRequestValidator.getValidEmailList(emailRequest.getToEmailAddresses());
        emailRequest.setToEmailAddresses(emailList);
        emailRequestValidator.validate(emailRequest);
        List<MultiPart> formDataList = new ArrayList<>();
        for (String attachment : emailRequest.getAttachmentList()) {
            formDataList.add(getFormData(attachment));
        }
        emailRequest.setMultiParts(formDataList);

    }

    @Override
    protected EmailResponse process(EmailRequest emailRequest) {

        WebResource.Builder builder = getWebResourceBuilder();
        String intuit_tid = SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");
        builder.header("intuit_tid", intuit_tid);
        EmailResponse response = null;
        ClientResponse clientResponse = null;
        try {
            if (emailRequest.getMediaType() != null && emailRequest.getMediaType() == MediaType.MULTIPART_FORM_DATA) {
                for (MultiPart multiPart : emailRequest.getMultiParts()) {
                    clientResponse = builder.type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, multiPart);
                    response = emailResponseProcessor.getEmailResponse(clientResponse);
                    logger.info("response info for S3key: responseStatus={}, responseBody={}, responseTid={}", response.getStatus(),
                            response.getResponseBody(), response.getHeaders().get("intuit_tid"));
                    emailRequest.addAttachmentKey(response.getAttachmentKey());
                }

            }
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
        emailRequest.setMultiParts(null);
        emailRequest.setAttachmentList(null);
        transactionalEmailSendGridStrategy.sendMail(emailRequest);
    }

    @Override
    public EmailStrategyType getSupportedEmailStrategyType() {
        return EmailStrategyType.SendGridWithAttachments;
    }

    private MultiPart getFormData(String attachmentURI) {
        File file = new File(attachmentURI);
        BodyPart fileBody = new FileDataBodyPart("inputstream", file, MediaType.MULTIPART_FORM_DATA_TYPE);
        MultiPart formData = new FormDataMultiPart()
                .field("name", file.getName())
                .field("scan", "true")
                .bodyPart(fileBody);
        return formData;
    }
}
