package com.intuit.sbd.payroll.psp.emailsender.gateway.DocumentService;

import com.intuit.sbd.payroll.psp.emailsender.EmailAuthorizationManager;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import com.intuit.sbg.psp.documentservices.service.DocumentServiceRestClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class DocumentServiceGatewayConfig {

    @Value("${email-client.oinp-services.attachment-service}")
    private String attachmentService;

    @Bean("AttachmentServiceName")
    public String getAttachmentService()
    {
        return attachmentService;
    }

    @Bean
    public DocumentServiceGateway documentServiceGateway(DocumentServiceRestClient documentServiceRestClient, EmailAuthorizationManager authorizationManager, EmailRequestValidator validator) {
        return new DocumentServiceGatewayRestImpl(documentServiceRestClient,authorizationManager,validator);
    }
}
