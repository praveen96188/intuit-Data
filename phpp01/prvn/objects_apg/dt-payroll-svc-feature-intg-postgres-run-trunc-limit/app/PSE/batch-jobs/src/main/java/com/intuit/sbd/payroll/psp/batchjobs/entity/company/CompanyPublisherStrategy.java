package com.intuit.sbd.payroll.psp.batchjobs.entity;

import com.intuit.sbd.payroll.psp.mapper.jackson.CustomObjectMapperResolver;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import com.intuit.sbg.psp.events.publisher.kafka.requests.KafkaPublishRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import java.util.*;
import com.intuit.sbd.payroll.psp.entity.publisher.EntityPublishMessage;

public class CompanyPublisherStrategy<T> implements EventPublisherStrategy<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyPublisherStrategy.class);

    private KafkaSDKPublisher kafkaPublisher;
    private int currentBatchNo = 0;
    private String topicName;
    private CompanyPublishStatusWorkflows companyPublishStatusWorkflows;
    private CustomObjectMapperResolver customObjectMapperResolver;
    private boolean republishMode;

    public CompanyPublisherStrategy(String topicName, CompanyPublishStatusWorkflows companyPublishStatusWorkflows, boolean republishMode) {
        customObjectMapperResolver = new CustomObjectMapperResolver();
        this.topicName = topicName;
        this.companyPublishStatusWorkflows = companyPublishStatusWorkflows;
        this.republishMode = republishMode;
    }

    void init() {
        kafkaPublisher = PayrollApplicationBeanFactory.getBean(KafkaSDKPublisher.class);
    }

    public Map<String, String> getHeaders() {
        LOGGER.info("job=initial_load,action=get_header_start");
        Map<String, String> messageHeaders = new HashMap<>();

        messageHeaders.put("intuit_tid", UUID.randomUUID().toString());
        messageHeaders.put("republish", String.valueOf(republishMode));
        messageHeaders.put("targetService", companyPublishStatusWorkflows.name());
        return messageHeaders;
    }

    @Override
    public void publishBatch(List<T> entity) {
        LOGGER.info("job=initial_load,action=publish_batch_started");
        init();
        ArrayList<String> psids = new ArrayList<>();

        entity.stream().forEach(entry -> psids.add(((Object[])entry)[0].toString()));

        String jsonMessage = getMessageString(psids, ++currentBatchNo);
        Map<String, String> headers = getHeaders();
        LOGGER.info("job=initial_load,action=publish_batch_intermediate,message=" + jsonMessage + ",messageHeaders=" + headers.toString() + ",topicName=" + topicName +",targetedWorkFlow=" + companyPublishStatusWorkflows);

        try {
            KafkaPublishRequest request = new KafkaPublishRequest(topicName, jsonMessage, headers);
            SendResult<String, String> sentResult = kafkaPublisher.publishSync(request);
            LOGGER.debug("job=initial_load,action=publish_batch_published,publishResult=" + sentResult.toString());
        } catch (Exception ex) {
            LOGGER.error("job=initial_load,Failed to Publish the message=" + jsonMessage+ ",Batch id="+ currentBatchNo + ",Exception=",ex);
            throw new RuntimeException("Failed to Publish the message", ex);
        }
        LOGGER.info("job=initial_load,action=publish_batch_completed,message=" + jsonMessage);
    }

    public String getMessageString(List<String> entity, int batchId) {
        LOGGER.info("job=initial_load,action=get_message_string");

        EntityPublishMessage entityPublishMessage = new EntityPublishMessage(Integer.toString(batchId), entity);
        return customObjectMapperResolver.serialize(entityPublishMessage);
    }
}