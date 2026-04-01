package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.gateway.DocumentService.DocumentServiceGateway;
//import com.intuit.sbd.payroll.psp.emailsender.service.OINP.Documentservice.DocumentServiceHelperService;
import com.intuit.sbd.payroll.psp.emailsender.gateway.OINPRestServiceGateway;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of OINPWithAttachmentsStrategy
 *
 * @author nramesh1
 */

@Component
public class OINPWithAttachmentsStrategy extends OINPStrategy {

    private final Logger logger = LoggerFactory.getLogger(OINPWithAttachmentsStrategy.class);
    private DocumentServiceGateway documentServiceGateway;
    @Autowired
    public OINPWithAttachmentsStrategy(DocumentServiceGateway documentServiceGateway, EmailRequestValidator emailRequestValidator, OINPRequestUtility oinpRequestUtility, OINPRestServiceGateway oinpRestServiceGateway)
    {
        super(emailRequestValidator, oinpRequestUtility, oinpRestServiceGateway);
        this.documentServiceGateway=documentServiceGateway;
    }

    @Override
    public EmailStrategyType getSupportedEmailStrategyType() {
        return EmailStrategyType.OINPWithAttachments;
    }

    @Override
    protected void preProcess(EmailRequest emailRequest) {
        super.preProcess(emailRequest);
        emailRequestValidator.validateAttachments(emailRequest);
        for (String attachmentURI : emailRequest.getAttachmentList()) {

            String docId = (String) documentServiceGateway.uploadDocument(attachmentURI);
            String sourceKey = (String) documentServiceGateway.getSourceKey(docId);
            emailRequest.addDocumentMetaData(docId, sourceKey);
        }
    }

    @Override
    protected EmailResponse process(EmailRequest emailRequest) throws Exception {
        logger.info("OINP: Attachments uploaded to Document Service. Proceed to publish email with attachments");
        return super.process(emailRequest);
    }

}
