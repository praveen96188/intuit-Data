package com.intuit.sbd.payroll.psp.emailsender.model.DocumentService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class DocumentMetaData {
    private String documentId;
    private String sourceKey;
}
