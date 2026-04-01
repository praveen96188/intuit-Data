package com.intuit.sbd.payroll.psp.emailsender.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class OINPEmailSettings {

    private static final String urlDelimiter = "";

    @Value("${email-client.oinp-services.url}")
    private String baseUrl;

    @Value("${email-client.oinp-services.endpoints.email-event}")
    private String oinpEventsApi;

    @Value("${email-client.oinp-services.kafka.topic-name}")
    private String oinpTopicName;

    @Value("${email-client.oinp-services.kafka.type}")
    private String oinpEventType;

    public String getOinpEventsApiEndpoint(){
        return String.join(urlDelimiter, getBaseUrl(), oinpEventsApi);
    }
}