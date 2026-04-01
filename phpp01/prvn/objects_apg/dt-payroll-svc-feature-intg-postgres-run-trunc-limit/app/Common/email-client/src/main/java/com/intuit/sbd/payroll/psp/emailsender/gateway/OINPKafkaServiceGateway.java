package com.intuit.sbd.payroll.psp.emailsender.gateway;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPEmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPKafkaResponse;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPKafkaRequest;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import com.intuit.sbg.psp.events.publisher.kafka.exceptions.KafkaPublisherException;
import com.intuit.sbg.psp.events.publisher.kafka.requests.KafkaPublishRequest;
import com.intuit.sbg.psp.webserviceclient.support.json.JsonConverter;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to publish events to OINP via Kafka
 *
 * @author nramesh1
 */

@Component
public class OINPKafkaServiceGateway {

    private final Logger logger = LoggerFactory.getLogger(OINPKafkaServiceGateway.class);

    private final OINPEmailSettings oinpEmailSettings;
    private final JsonConverter jsonConverter;
    private final KafkaSDKPublisher kafkaPublisher;

    @Autowired
    public OINPKafkaServiceGateway(OINPEmailSettings oinpEmailSettings, JsonConverter jsonConverter, KafkaSDKPublisher kafkaPublisher)
    {
        this.oinpEmailSettings = oinpEmailSettings;
        this.jsonConverter = jsonConverter;
        this.kafkaPublisher = kafkaPublisher;
    }

    public OINPKafkaResponse publishEventViaKafka(OINPKafkaRequest oinpKafkaRequest){

        Validate.notNull(oinpKafkaRequest, "OINP Kafka request cannot be null");

        String payload = jsonConverter.serialize(oinpKafkaRequest);

        try {
            KafkaPublishRequest request = new KafkaPublishRequest(oinpEmailSettings.getOinpTopicName(), payload, getDefaultHeaders(oinpKafkaRequest));
            SendResult<String, String> response = kafkaPublisher.publishSync(request);

            logger.info("OINP: Event with Object type:{}, object id:{}, intuitTid: {} published successfully via Kafka: topic={}, partition={}, offset={}, timestamp={}",
                    oinpKafkaRequest.getPayload().getSourceObjectType(), oinpKafkaRequest.getPayload().getSourceObjectId(), oinpKafkaRequest.getPayload().getEventMetaData().getIntuitTid(),
                    response.getRecordMetadata().topic(), response.getRecordMetadata().partition(), response.getRecordMetadata().offset(), response.getRecordMetadata().timestamp());

            return new OINPKafkaResponse(response);

        } catch (KafkaPublisherException e) {
            logger.error("OINP: Failed to publish event (object type):" + oinpKafkaRequest.getPayload().getSourceObjectType() + "via Kafka: ", e);
            throw e;
        }
    }

    public Map<String,String> getDefaultHeaders(OINPKafkaRequest request) {
        Map<String,String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put("intuit_tid", request.getPayload().getEventMetaData().getIntuitTid());

        return headers;
    }
}
