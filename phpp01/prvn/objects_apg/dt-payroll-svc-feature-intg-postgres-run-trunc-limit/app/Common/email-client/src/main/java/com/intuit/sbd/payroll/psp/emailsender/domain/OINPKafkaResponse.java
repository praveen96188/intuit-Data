package com.intuit.sbd.payroll.psp.emailsender.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.kafka.support.SendResult;

@Getter
@Setter
public class OINPKafkaResponse extends EmailResponse{

    private SendResult<String, String> kafkaResponse;

    public OINPKafkaResponse(SendResult<String,String> kafkaResponse) {
        this.kafkaResponse = kafkaResponse;
    }

}
