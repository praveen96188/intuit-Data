package com.intuit.sbd.payroll.psp.entity.publisher;

import java.util.Map;

//todo: use Lambok
public class EntityEvent {
    private String entityType;
    private String entityId;
    private String eventType;
    private String payload;
    private Map<String, String> headers;
    private Long entityVersion;
    private String changedAttribute;

    public EntityEvent(String entityType, String entityId, String eventType, String payload) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Long getEntityVersion() {
        return entityVersion;
    }

    public void setEntityVersion(Long entityVersion) {
        this.entityVersion = entityVersion;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getChangedAttribute() {
        return changedAttribute;
    }

    public void setChangedAttribute(String changedAttribute) {
        this.changedAttribute = changedAttribute;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
