package com.intuit.sbd.payroll.psp.emailsender.gateway;

import com.intuit.sbd.payroll.psp.emailsender.domain.OINPEmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPKafkaResponse;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventMetaData;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventRequest;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPKafkaRequest;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import com.intuit.sbg.psp.events.publisher.kafka.requests.KafkaPublishRequest;
import com.intuit.sbg.psp.webserviceclient.support.json.JsonConverter;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.kafka.support.SendResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OINPKafkaServiceGatewayTest {

    @InjectMocks
    OINPKafkaServiceGateway oinpKafkaServiceGateway;

    @Mock
    KafkaSDKPublisher kafkaSDKPublisher;

    @Mock
    JsonConverter jsonConverter;

    @Mock
    OINPEmailSettings oinpEmailSettings;

    @Test
    public void validatePublishEventViaKafkaTest() {

        OINPKafkaRequest testOINPRequest = new OINPKafkaRequest();
        OINPEventRequest testEvent = new OINPEventRequest();
        OINPEventMetaData testMetaData = new OINPEventMetaData();
        testMetaData.setIntuitTid("intuit_tid");
        testEvent.setEventMetaData(testMetaData);
        testOINPRequest.setPayload(testEvent);

        String testPayload = "testPayload";
        String topicName = "testOINPTopic";

        TopicPartition testPartition = new TopicPartition(topicName,0);
        SendResult<String,String> response = new SendResult(null,new RecordMetadata(testPartition,0L,0L,0L,0L,0,0));

        when(jsonConverter.serialize(testOINPRequest)).thenReturn(testPayload);
        when(oinpEmailSettings.getOinpTopicName()).thenReturn(topicName);

        when(kafkaSDKPublisher.publishSync(any(KafkaPublishRequest.class))).thenReturn(response);
        OINPKafkaResponse kafkaResponse = oinpKafkaServiceGateway.publishEventViaKafka(testOINPRequest);
        assertEquals(kafkaResponse.getKafkaResponse().getProducerRecord(),response.getProducerRecord());
        assertEquals(kafkaResponse.getKafkaResponse().getRecordMetadata(),response.getRecordMetadata());

    }
}
