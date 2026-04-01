package com.intuit.sbd.payroll.psp.emailsender.gateway.DocumentService;

public interface DocumentServiceGateway {

    Object uploadDocument(String attachmentURI);

    Object getSourceKey(String docId);

    Object getDocumentData(String docId);

}
