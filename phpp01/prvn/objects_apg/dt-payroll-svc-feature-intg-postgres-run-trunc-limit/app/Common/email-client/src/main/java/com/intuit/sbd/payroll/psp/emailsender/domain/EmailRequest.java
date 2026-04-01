package com.intuit.sbd.payroll.psp.emailsender.domain;

import com.intuit.sbd.payroll.psp.emailsender.SendRequest;
import com.sun.jersey.multipart.MultiPart;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author vishalb849
 */
@Builder
@Getter
@Setter
public class EmailRequest {

    private String[] toEmailAddresses;
    private String fromEmailAddress;
    private String subject;
    private String htmlContent;
    private String replyEmailAddress;
    private List<String> attachmentList;
    private String fromEmailDisplayName;
    private Boolean pHighPriority;
    private EmailStrategyType emailStrategyType;
    private List<String> attachmentKeys;
    private String mediaType;
    private List<MultiPart> multiParts;
    private SendRequest sendRequest;

    /*for OINP*/
    private String templateName;
    private String templateObjectType;
    private Map<String, Object> templateAttributes; // attributes to be sent as eventData based on template
    private String intuitTid;
    private Map<String,String> documentMetaDataMap; //stores Document IDs and Source Keys

    public void addAttachmentKey(String attachmentKey) {
        if (Objects.isNull(this.attachmentKeys)) {
            this.attachmentKeys = new ArrayList<>();
        }
        this.attachmentKeys.add(attachmentKey);
    }

    public void addDocumentMetaData(String documentID, String sourceKey) {
        if (Objects.isNull(this.documentMetaDataMap)) {
            this.documentMetaDataMap = new HashMap<>();
        }
        this.documentMetaDataMap.put(documentID,sourceKey);
    }

    public Boolean getPriority() {
        return pHighPriority;
    }

    public void setAttachmentList(List<String> attachmentList) {
        this.attachmentList = attachmentList;
        setMediaType(MediaType.MULTIPART_FORM_DATA);
    }

    public EmailRequest setToEmailAddresses(String[] toEmailAddresses) {
        this.toEmailAddresses = toEmailAddresses;
        return this;
    }

    public EmailRequest setSendRequest(SendRequest sendRequest) {
        this.sendRequest = sendRequest;
        return this;
    }

    //TODO: POST phase 3, for initial members which were used in TxE create setters to convert to OINP
}
