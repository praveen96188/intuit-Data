package com.intuit.sbd.payroll.psp.emailsender.model.OINP;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class OINPKafkaRequest {

    String type;

    OINPEventRequest payload;

}
