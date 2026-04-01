package com.intuit.sbd.payroll.psp.entity;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;

//todo: use lombok
//todo: refactoring
public class EntityContext<T extends DomainEntity> {
    private static final String DELIMITER = "#";
    private T rawEntity;
    private EventEnumType eventEnumType;
    private Set<String> changedAttributes;
    private String eventId;
    private String sourceTid;
    private T currentEntity;
    private long entityPublishTimeStamp;
    private Company company;

    public EntityContext(T entity, EventEnumType eventEnumType) {
        this.rawEntity = entity;
        this.eventEnumType = eventEnumType;
    }

    public EntityContext(T entity, EventEnumType eventEnumType, Set<String> changedAttributes) {
        this.rawEntity = entity;
        this.eventEnumType = eventEnumType;
        this.changedAttributes = changedAttributes;
    }

    public Set<String> getChangedAttributes() {
        return changedAttributes;
    }

    public void setChangedAttributes(Set<String> changedAttributes) {
        this.changedAttributes = changedAttributes;
    }

    public EventEnumType getEventEnumType() {
        return eventEnumType;
    }

    public void setEventEnumType(EventEnumType eventEnumType) {
        this.eventEnumType = eventEnumType;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return this.eventId;
    }

    public Class getEntityType() {
        return Application.getDomainEntityType(rawEntity);
    }

    public SpcfUniqueId getEntityId() {
        return rawEntity.getId();
    }

    public T getCurrentEntity() {
        return currentEntity;
    }

    public void setCurrentEntity(T currentEntity) {
        this.currentEntity = currentEntity;
    }

    public String getEntityKey() {
        return String.join(DELIMITER, getEntityType().getCanonicalName(), getEntityId().toString());
    }

    public String getSourceTid() {
        return sourceTid;
    }

    public void setSourceTid(String sourceTid) {
        this.sourceTid = sourceTid;
    }

    public long getEntityPublishTimeStamp() {
        return entityPublishTimeStamp;
    }

    public void setEntityPublishTimeStamp(long entityPublishTimeStamp) {
        this.entityPublishTimeStamp = entityPublishTimeStamp;
    }

    public String toString() {
        String log = "{EntityType=%s, EntityId=%s, EntityEventType=%s, EntityUpdateId=%s}";

        String entityType = getEntityType().getCanonicalName();
        String entityId = getEntityId().toString();
        String entityEventType = getEventEnumType().toString();
        String entityUpdateId = Objects.isNull(getEventId())? StringUtils.EMPTY: getEventId();

        return String.format(log, entityType, entityId, entityEventType, entityUpdateId);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
