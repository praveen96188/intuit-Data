package com.intuit.sbd.payroll.psp.emailsender.gateway.DocumentService;

import com.intuit.sbd.payroll.psp.emailsender.EmailAuthorizationManager;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import com.intuit.sbg.psp.documentservices.service.DocumentServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentServiceGatewayRestImpl implements DocumentServiceGateway {

    private final DocumentServiceRestClient documentServiceRestClient;
    private final EmailAuthorizationManager authorizationManager;
    private final EmailRequestValidator validator;

    @Autowired
    public DocumentServiceGatewayRestImpl(DocumentServiceRestClient documentServiceRestClient, EmailAuthorizationManager authorizationManager, EmailRequestValidator validator)
    {
        this.documentServiceRestClient = documentServiceRestClient;
        this.authorizationManager = authorizationManager;
        this.validator = validator;
    }

    @Override
    public Object uploadDocument(String attachmentURI)
    {
        String docID = "";
        try {
            validator.validateAttachmentUri(attachmentURI);
            authorizationManager.setAuthorizationContext();
            docID = (String)documentServiceRestClient.uploadToDocumentService(attachmentURI);
            return docID;
        } catch (Exception e) {
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
    }

    @Override
    public String getSourceKey(String docId)
    {
        String sourceKey = "";
        try {
            validator.validateDocumentId(docId);
            authorizationManager.setAuthorizationContext();
            sourceKey = (String) documentServiceRestClient.getSourceKey(docId);
            return sourceKey;

        } catch (Exception e) {
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
    }

    @Override
    public Object getDocumentData(String docId)
    {
        try {
            validator.validateDocumentId(docId);
            authorizationManager.setAuthorizationContext();
            return documentServiceRestClient.getDocumentData(docId);

        } catch (Exception e) {
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
    }

}
