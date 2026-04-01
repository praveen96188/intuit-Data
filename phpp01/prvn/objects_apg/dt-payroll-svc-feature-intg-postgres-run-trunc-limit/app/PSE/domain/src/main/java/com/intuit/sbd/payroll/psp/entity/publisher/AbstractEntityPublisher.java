package com.intuit.sbd.payroll.psp.entity.publisher;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.mapper.cdm.CDMMapper;
import com.intuit.sbd.payroll.psp.mapper.jackson.CustomObjectMapperResolver;
import com.intuit.sbg.nucleus.model.BaseModel;
import com.intuit.sbg.nucleus.model.ResourceModel;
import com.intuit.sbg.psp.events.core.kafka.EventHeaders;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import com.intuit.sbg.psp.events.publisher.kafka.requests.KafkaPublishRequest;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.support.SendResult;

import java.util.*;

@Slf4j
public abstract class AbstractEntityPublisher<E extends DomainEntity> implements EntityPublisher<E> {

    // TODO: put the headers in com.intuit.sbg.psp.events.core.kafka.EventHeaders
    private static final String DESKTOP = "Desktop";
    private static final String BACK_OFFICE_ID = "backOfficeId";
    private static final String SOURCE_TID = "source_tid";
    protected static final String EQUIFAX_INSUFFICIENT_DATA = "equifaxInsufficientData";
    protected static final String DELIMITER = "-";
    private KafkaSDKPublisher kafkaPublisher;
    private CDMMapper cdmMapper;
    private CustomObjectMapperResolver customObjectMapperResolver;

    public AbstractEntityPublisher(KafkaSDKPublisher kafkaPublisher, CDMMapper cdmMapper) {
        this.cdmMapper = cdmMapper;
        this.kafkaPublisher = kafkaPublisher;
        customObjectMapperResolver = new CustomObjectMapperResolver();
    }

    public boolean publish(EntityContext<E> entityContext) {
        entityContext.setEntityPublishTimeStamp(SpcfCalendar.getNow().getTimeInMilliseconds());
        String payload = null;
        try{
            payload = getCDMPayload(entityContext, getCDMMapperClass());
        } catch (Throwable e) {
            log.error("Action=EntityCDMMapping, EntityContext={}", entityContext);
            throw new EntityCDMMappingException(e);
        }
        EntityEvent entityEvent = createEntityEvent(entityContext, payload);
        return publishNonRestrictedEntity(entityEvent);
    }

    public boolean publishRestricted(EntityContext<E> entityContext) {
        String payload = getCDMPayload(entityContext, getRestrictedCDMMapperClass());
        EntityEvent entityEvent = createEntityEvent(entityContext, payload);
        return publishRestrictedEntity(entityEvent);
    }

    private EntityEvent createEntityEvent(EntityContext<E> entityContext, String payload) {
        EntityEvent entityEvent = new EntityEvent(entityContext.getEntityType().getCanonicalName(), entityContext.getEntityId().toString(), getEventTypeName(entityContext.getEventEnumType()), payload);
        entityEvent.setEntityVersion(entityContext.getEntityPublishTimeStamp());
        if (entityContext.getChangedAttributes() != null) {
            entityEvent.setChangedAttribute(Arrays.toString(entityContext.getChangedAttributes().toArray()));
        }
        entityEvent.setHeaders(getHeaders(entityContext));

        return entityEvent;
    }

    protected boolean publishRestrictedEntity(EntityEvent entityEvent) {
        return publishEntity(entityEvent);
    }

    protected boolean publishNonRestrictedEntity(EntityEvent entityEvent) {
        return publishEntity(entityEvent);
    }

    private boolean publishEntity(EntityEvent entityEvent) {
        KafkaPublishRequest request = new KafkaPublishRequest(getTopic(), entityEvent.getPayload(), entityEvent.getHeaders());
        try {
            boolean payloadLogging = Boolean.parseBoolean(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_ENABLE_PAYLOAD_LOGGING"));
            if (payloadLogging) {
                log.info(String.format("Action=EntityPublishRequest, topicName=%s, headers=%s, requestBody=%s",
                        request.getTopicName(), request.getMessageHeaders().toString(), EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, request.getPayload(), entityEvent.getEntityId())));
            } else {
                log.info(String.format("Action=EntityPublishRequest, EntityID=%s, topicName=%s, headers=%s",
                        entityEvent.getEntityId(), request.getTopicName(), request.getMessageHeaders().toString()));
            }

            SendResult<String, String> response = kafkaPublisher.publishSync(request);

            log.info(String.format("Action=EntityPublishResponse, EntityId=%s topic=%s, partition=%s, offset=%s, timestamp=%s",
                    entityEvent.getEntityId(), response.getRecordMetadata().topic(), response.getRecordMetadata().partition(),
                    response.getRecordMetadata().offset(), response.getRecordMetadata().timestamp()));
        } catch(Exception e){
            throw new RuntimeException(String.format("Failed to publish, topicName=%s, headers=%s, requestBody=%s",
                    request.getTopicName(), request.getMessageHeaders().toString(), EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, request.getPayload(), entityEvent.getEntityId())), e);
        }
        return true;
    }

    private String getCDMPayload(EntityContext<E> context, Class<?> mapperClass) {
        ResourceModel object = (ResourceModel)cdmMapper.mapToTarget(context.getCurrentEntity(), mapperClass);
        overrideEntityVersion(object, String.valueOf(context.getEntityPublishTimeStamp()));
        return customObjectMapperResolver.serialize(object);
    }

    protected void overrideEntityVersion(ResourceModel object, String entityVersion) {
        object.setEntityVersion(entityVersion);
    }

    protected Map<String, String> getHeaders(EntityContext<E> entityContext) {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.putAll(getDefaultHeaders(entityContext));
        headersMap.putAll(getAdditionalHeaders(entityContext));
        return headersMap;
    }

    protected Map<String, String> getDefaultHeaders(EntityContext<E> entityContext) {
        Map<String, String> map = new HashMap<>();
        String changedAttributes = Objects.isNull(entityContext.getChangedAttributes()) ? StringUtils.EMPTY : Arrays.toString(entityContext.getChangedAttributes().toArray());

        map.put(EventHeaders.REALM_ID, StringUtils.isAllBlank(getCompany(entityContext).getIAMRealmId())? StringUtils.EMPTY: getCompany(entityContext).getIAMRealmId());
        map.put(BACK_OFFICE_ID, getCompany(entityContext).getSourceCompanyId());
        map.put(SOURCE_TID, StringUtils.isAllBlank(entityContext.getSourceTid()) ? StringUtils.EMPTY : entityContext.getSourceTid());
        map.put(EventHeaders.INTUIT_TID, UUID.randomUUID().toString());
        map.put(EventHeaders.ENTITY_ID, entityContext.getEntityId().toString());
        map.put(EventHeaders.SOURCE, DESKTOP);
        map.put(EventHeaders.EVENT_TYPE, getEventTypeName(entityContext.getEventEnumType()));
        map.put(EventHeaders.CHANGED_ATTRIBUTES, changedAttributes);
        map.put(EventHeaders.ENTITY_VERSION, String.valueOf(entityContext.getEntityPublishTimeStamp()));
        map.put(EventHeaders.IDEMPOTENCE_KEY, UUID.randomUUID().toString());

        return map;
    }

    String getEventTypeName(EventEnumType type) {
        switch (type) {
            case EntityCreate:
                return "ENTITY_CREATE";
            case EntityUpdate:
                return "ENTITY_UPDATE";
            case EntityDelete:
                return "ENTITY_DELETE";
        }
        return null;
    }

    protected Map<String, String> getAdditionalHeaders(EntityContext<E> entityContext) {
        Map<String, String> map = new HashMap<>();
        return map;
    }

    protected abstract Class<? extends BaseModel> getCDMMapperClass();

    protected abstract Class<? extends BaseModel> getRestrictedCDMMapperClass();

    protected abstract String getTopic();

    protected abstract Company getCompany(EntityContext<E> entityContext);

}
