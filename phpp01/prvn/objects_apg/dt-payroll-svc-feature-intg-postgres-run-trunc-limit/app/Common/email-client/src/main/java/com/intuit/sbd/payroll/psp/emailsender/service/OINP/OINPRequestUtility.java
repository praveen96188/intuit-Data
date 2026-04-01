package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPEmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.model.DocumentService.DocSvcAttachmentData;
import com.intuit.sbd.payroll.psp.emailsender.model.DocumentService.DocSvcDocument;
import com.intuit.sbd.payroll.psp.emailsender.model.DocumentService.DocumentMetaData;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class OINPRequestUtility {

    private String sourceServiceName;
    private OINPEmailSettings oinpEmailSettings;
    private String attachmentServiceName;

    @Autowired
    public OINPRequestUtility(@Qualifier("SourceServiceName")String sourceServiceName, OINPEmailSettings oinpEmailSettings, @Qualifier("AttachmentServiceName")String attachmentServiceName){
        this.sourceServiceName = sourceServiceName;
        this.oinpEmailSettings = oinpEmailSettings;
        this.attachmentServiceName = attachmentServiceName;
    }

    public OINPEventRequest createOINPRequest(EmailRequest emailRequest)
    {
        //create eventMetadata
        OINPEventMetaData eventMetaData = getOINPMetaData("-1", emailRequest.getIntuitTid());

        OINPEventRequest oinpEvent = new OINPEventRequest();

        //creating event to be published to OINP
        oinpEvent.setEventData(emailRequest.getTemplateAttributes());
        oinpEvent.setEventMetaData(eventMetaData);
        oinpEvent.setSourceObjectId(UUID.randomUUID().toString());
        oinpEvent.setSourceServiceName(sourceServiceName);
        oinpEvent.setSourceObjectType(emailRequest.getTemplateObjectType());
        oinpEvent.setName(emailRequest.getTemplateName());

        if(emailRequest.getEmailStrategyType() == EmailStrategyType.OINPWithAttachments )
        {
            oinpEvent.setAttachmentData(createAttachmentData(emailRequest, attachmentServiceName));
        }
        return oinpEvent;
    }

    private DocSvcAttachmentData createAttachmentData(EmailRequest emailRequest, String attachmentService) {
        DocSvcAttachmentData attachmentData = new DocSvcAttachmentData();
        attachmentData.setDocuments(getDocumentList(emailRequest, attachmentService));
        return attachmentData;
    }

    public OINPKafkaRequest createOINPKafkaRequest(EmailRequest emailRequest) {

        OINPEventRequest payload = createOINPRequest(emailRequest);
        String type = oinpEmailSettings.getOinpEventType();

        OINPKafkaRequest oinpKafkaRequest = new OINPKafkaRequest();
        oinpKafkaRequest.setPayload(payload);
        oinpKafkaRequest.setType(type);

        return oinpKafkaRequest;
    }

    private List<DocSvcDocument> getDocumentList(EmailRequest emailRequest, String attachmentService) {
        List<DocSvcDocument> documents = new ArrayList<>();
        Map<String,String> documentsMap = emailRequest.getDocumentMetaDataMap();

        for( String documentId : documentsMap.keySet()) {
            DocumentMetaData metaData = new DocumentMetaData();
            metaData.setDocumentId(documentId);
            metaData.setSourceKey(documentsMap.get(documentId));

            DocSvcDocument document = new DocSvcDocument();
            document.setSource(attachmentService);
            document.setDocumentMetaData(metaData);
            documents.add(document);
        }

        return documents;
    }

    protected OINPEventMetaData getOINPMetaData(String authId, String intuitTid)
    {
        OINPEventMetaData metaData = new OINPEventMetaData();
        metaData.setAuthId(authId);
        metaData.setIntuitTid(intuitTid);
        LocalDateTime now = LocalDateTime.now();
        metaData.setCreatedDate(Timestamp.valueOf(now));

        return metaData;
    }

    public String getSourceServiceName()
    {
        return sourceServiceName;
    }

    // These keys include the attributes required in OINP eventData for mails sent via OINPStrategy and OINPStrategyWithAttachments
    public interface OINPStrategyPropertyKeys{

        public static final String TO_EMAIL_ADDRESSES = "toEmail";
        public static final String FROM_EMAIL_ADDRESS = "fromEmail";
        public static final String HTML_CONTENT = "htmlContent";
        public static final String SUBJECT = "subject";
    }
}
