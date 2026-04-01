package com.intuit.sbd.payroll.psp.emailsender.model.OINP;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class OINPEventMetaData {
    private String authId;
    private Date createdDate;
    private String intuitTid;

}
