package com.intuit.sbd.payroll.psp.emailsender.model.OINP;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intuit.sbd.payroll.psp.emailsender.model.DocumentService.DocSvcAttachmentData;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(
        ignoreUnknown = true
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OINPEventRequest {
    private String name;
    private String sourceServiceName;
    private String sourceObjectId;
    private String sourceObjectType;
    private Map<String,Object> eventData;
    private OINPEventMetaData eventMetaData;
    private DocSvcAttachmentData attachmentData;
}
