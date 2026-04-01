package com.intuit.sbd.payroll.psp.batchjobs.soxReport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaHTTPPublisher;
import com.intuit.sbg.psp.events.publisher.kafka.requests.HttpPublishRequest;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
public class SoxRequestPublisher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KafkaHTTPPublisher kafkaPublisher;

    @Autowired
    public SoxRequestPublisher(KafkaHTTPPublisher kafkaPublisher){
        this.kafkaPublisher = kafkaPublisher;
    }

    protected Boolean publish(String url, Object data) throws Exception{
        HttpPublishRequest request = createRequest(url, data);

        HttpServiceResponse response = kafkaPublisher.publishSync(request);

        return HttpStatus.SC_ACCEPTED == response.getStatusCode();
    }

    protected HttpPublishRequest createRequest(String url, Object data) throws JsonProcessingException {

        Map<String,String> headers = new HashMap<>();
        headers.put(HttpHeaders.CACHE_CONTROL,"no-cache");
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        HttpPublishRequest req = new HttpPublishRequest(url, parseToJSON(data) , headers, true);
        return req;
    }

    private String parseToJSON(Object data) throws JsonProcessingException {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException jsonProcessingException) {
            logger.error("Event=SoxReportBatchJobError: Json processing threw an exception!! ", jsonProcessingException);
            throw jsonProcessingException;
        }
    }
}
