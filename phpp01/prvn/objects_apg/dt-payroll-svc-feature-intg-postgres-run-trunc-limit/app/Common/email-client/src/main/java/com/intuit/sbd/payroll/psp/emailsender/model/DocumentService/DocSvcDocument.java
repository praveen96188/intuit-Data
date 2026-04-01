package com.intuit.sbd.payroll.psp.emailsender.model.DocumentService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class DocSvcDocument {


    private String source;
    private boolean optionalDocument;
    private DocumentMetaData documentMetaData;
}
