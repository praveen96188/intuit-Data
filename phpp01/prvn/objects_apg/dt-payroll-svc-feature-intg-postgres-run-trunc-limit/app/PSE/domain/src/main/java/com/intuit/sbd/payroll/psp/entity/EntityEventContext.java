package com.intuit.sbd.payroll.psp.entity;

import java.util.*;
import java.util.stream.Collectors;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;


/**
 * @author rn5
 */
public class EntityEventContext {

    private List<AbstractEvent> eventsToProcess;
    private List<AbstractEvent> rawEvents;
    private Map<String, EntityContext> entityIdEntityContextMap;
    private List<AbstractEvent> failedToProcessEvents;
    private int postUpdateEventCount;
    private int postInsertEventCount;
    private int entityContextCreateCount;
    private int entityContextUpdateCount;


    public EntityEventContext() {
        this.eventsToProcess = new ArrayList<>();
        this.entityIdEntityContextMap = new HashMap<>();
        this.failedToProcessEvents = new ArrayList<>();
        this.rawEvents = new ArrayList<>();
    }

    public List<AbstractEvent> getEvents() {
        return eventsToProcess;
    }

    public List<AbstractEvent> getRawEvents() {
        return rawEvents;
    }

    public void setFailedToProcessEvents(List<AbstractEvent> failedToProcessEvents) {
        this.failedToProcessEvents = failedToProcessEvents;
    }

    public List<AbstractEvent> getFailedToProcessEvents(){
        return failedToProcessEvents;
    }

    public void addEvent(AbstractEvent event) {
        eventsToProcess.add(event);
        rawEvents.add(event);
        updateEventsCounts(event);
    }

    public void add(EntityContext entityContext) {
        EntityContext existingEntityContext = entityIdEntityContextMap.get(entityContext.getEntityKey());
        updateEntityContextCounts(entityContext);
        if (Objects.isNull(existingEntityContext)) {
            entityIdEntityContextMap.put(entityContext.getEntityKey(), entityContext);
            return;
        }

        switch (entityContext.getEventEnumType()) {
            case EntityCreate:
                changeEntityContextToEntityCreate(existingEntityContext);
                break;
            case EntityUpdate:
                mergeWithExistingEntityContextIfRequired(existingEntityContext, entityContext);
                break;
            case EntityDelete:
                //Ignore
                break;
        }
    }

    private void mergeWithExistingEntityContextIfRequired(EntityContext existingEntityContext, EntityContext newEntityContext) {
        if (existingEntityContext.getEventEnumType() == EventEnumType.EntityUpdate) {
            existingEntityContext.getChangedAttributes().addAll(newEntityContext.getChangedAttributes());
        }
    }

    private void changeEntityContextToEntityCreate(EntityContext entityContext) {
        entityContext.setEventEnumType(EventEnumType.EntityCreate);
        entityContext.setChangedAttributes(null);
    }

    public String getEventsString() {
        return HibernateEventUtil.eventsListToString(eventsToProcess);
    }

    public String getFailedEventsString() {
        return HibernateEventUtil.eventsListToString(failedToProcessEvents);
    }

    public String getEntityContextsString() {
        if (Objects.isNull(getEntityContexts()))
            return StringUtils.EMPTY;

        return  getEntityContexts().stream()
                .map(EntityContext::toString)
                .collect(Collectors.joining("\n"));
    }

    private void updateEventsCounts(AbstractEvent event) {
        if (event instanceof PostInsertEvent) {
            postInsertEventCount++;
        } else if (event instanceof PostUpdateEvent) {
            postUpdateEventCount++;
        }
    }

    private void updateEntityContextCounts(EntityContext entityContext) {
        EntityContext existingEntityContext = entityIdEntityContextMap.get(entityContext.getEntityKey());

        if (Objects.isNull(existingEntityContext)) {
            if (entityContext.getEventEnumType() == EventEnumType.EntityCreate) {
                entityContextCreateCount++;
            } else if (entityContext.getEventEnumType() == EventEnumType.EntityUpdate) {
                entityContextUpdateCount++;
            }
            return;
        }

        if (entityContext.getEventEnumType() == EventEnumType.EntityCreate &&
                existingEntityContext.getEventEnumType() == EventEnumType.EntityUpdate) {
            entityContextUpdateCount--;
            entityContextCreateCount++;
        }
    }

    public int getPostInsertEventCount() {
        return postInsertEventCount;
    }

    public int getPostUpdateEventCount() {
        return postUpdateEventCount;
    }

    public int getEntityContextCreateCount() {
        return entityContextCreateCount;
    }

    public int getEntityContextUpdateCount() {
        return entityContextUpdateCount;
    }

    public Collection<EntityContext> getEntityContexts() {
        return entityIdEntityContextMap.values();
    }

    public void clearEventsToProcess(){
        if(Objects.isNull(eventsToProcess)){
            return;
        }
        eventsToProcess.clear();
    }

    public EntityContext get(String entityKey){
        return entityIdEntityContextMap.get(entityKey);
    }
}
