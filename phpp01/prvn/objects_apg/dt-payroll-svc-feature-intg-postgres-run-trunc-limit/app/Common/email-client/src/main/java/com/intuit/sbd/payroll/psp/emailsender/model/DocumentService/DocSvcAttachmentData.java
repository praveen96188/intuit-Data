package com.intuit.sbd.payroll.psp.emailsender.model.DocumentService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class DocSvcAttachmentData {

    private List<DocSvcDocument> documents;
}
