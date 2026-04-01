package com.intuit.sbd.payroll.psp.entity;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.domain.Status;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class HibernateEventUtil {

    public static String eventToString(AbstractEvent event){
        DomainEntity entity = getEntity(event);
        String entityType = Objects.isNull(entity) ? StringUtils.EMPTY : entity.getClass().getCanonicalName();
        String entityId = Objects.isNull(entity) ? StringUtils.EMPTY : entity.getId().toString();

        return String.format("{EventType=%s, EntityType=%s, EntityId=%s}",
                event.getClass().getSimpleName(), entityType, entityId);
    }

    public static String eventsListToString(Collection<AbstractEvent> eventsList) {
        return  eventsList.stream()
                .map(event -> eventToString(event))
                .collect(Collectors.joining("\n"));

    }

    public static DomainEntity getEntity(AbstractEvent event) {
        if (event instanceof PostInsertEvent) {
            return (DomainEntity) ((PostInsertEvent) event).getEntity();
        } else if (event instanceof PostUpdateEvent) {
            return (DomainEntity) ((PostUpdateEvent) event).getEntity();
        }
        throw new UnsupportedOperationException(String.format("Event=%s is not supported.", eventToString(event)));
    }

    public static EntityUpdate createEntityUpdateEvent(EntityContext context){
        EntityUpdate entityUpdate = new EntityUpdate();
        entityUpdate.setEventType(context.getEventEnumType());
        entityUpdate.setEntityName(context.getEntityType().getSimpleName());
        entityUpdate.setEntityId(context.getEntityId().toString());
        entityUpdate.setCompany(context.getCompany());

        String changedAttributes = Objects.isNull(context.getChangedAttributes()) ? StringUtils.EMPTY : context.getChangedAttributes().toString();
        entityUpdate.setChangedAttributes(changedAttributes);

        entityUpdate.setStatus(Status.InProgress);
        context.setEventId(entityUpdate.getuniqueId());
        return Application.save(entityUpdate);
    }
}
