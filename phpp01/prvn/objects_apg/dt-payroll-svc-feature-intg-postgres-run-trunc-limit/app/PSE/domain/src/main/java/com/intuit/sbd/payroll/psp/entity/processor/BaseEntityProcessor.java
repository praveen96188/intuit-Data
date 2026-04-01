package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.HibernateEventUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

import java.util.*;

@Slf4j
public abstract class BaseEntityProcessor<T extends DomainEntity> implements EntityProcessor<T> {
    private static final String NO_SUITABLE_ENTITY_FOUND = "No_Suitable_Entity_Found";
    private static final String PROCESSED_SUCCESSFULLY = "Processed_Successfully";
    private static final String NO_INTERESTED_ATTRIBUTES_FOUND = "No_Interested_Attributes_Found";
    private EntityProcessorUtility entityProcessorUtility;

    public BaseEntityProcessor(EntityProcessorUtility entityProcessorUtility) {
        this.entityProcessorUtility = entityProcessorUtility;
    }

    protected boolean isInterestedAttribute(String attribute) {
        return getAttributeFilters().contains(attribute);
    }

    protected EntityContext<T> createEntityContext(AbstractEvent abstractEvent) {
        if (abstractEvent instanceof PostInsertEvent) {
            return createEntityContextFromInsertEvent((PostInsertEvent) abstractEvent);
        } else if (abstractEvent instanceof PostUpdateEvent) {
            return createEntityContextFromUpdateEvent((PostUpdateEvent) abstractEvent);
        }
        return null;
    }

    private EntityContext<T> createEntityContextFromInsertEvent(PostInsertEvent postInsertEvent) {
        EntityContext<T> entityContext = null;
        T entity = getEntity(postInsertEvent.getEntity());

        if (Objects.isNull(entity)) {
            logProcessingResult(postInsertEvent, null, null, NO_SUITABLE_ENTITY_FOUND);
            return null;
        }

        if (getEntityType().isInstance(postInsertEvent.getEntity())) {
            entityContext = new EntityContext(entity, EventEnumType.EntityCreate);
        } else {
            Set<String> changedAttributes = new HashSet<>();
            changedAttributes.add(getCdmAttributeName(postInsertEvent.getEntity().getClass().getSimpleName()));
            entityContext = new EntityContext(entity, EventEnumType.EntityUpdate, changedAttributes);
        }
        entityContext.setCompany(getCompany(entity));
        logProcessingResult(postInsertEvent, entityContext, null, PROCESSED_SUCCESSFULLY);
        return entityContext;
    }

    private EntityContext<T> createEntityContextFromUpdateEvent(PostUpdateEvent postUpdateEvent) {
        T entity = getEntity(postUpdateEvent.getEntity());

        if (Objects.isNull(entity)) {
            logProcessingResult(postUpdateEvent, null, null, NO_SUITABLE_ENTITY_FOUND);
            return null;
        }

        Set<String> changedAttributes = entityProcessorUtility.getChangedAttributes(postUpdateEvent);
        Set<String> filteredChangedAttributes = getFilteredChangedAttribute(changedAttributes, (DomainEntity)postUpdateEvent.getEntity());

        if (!filteredChangedAttributes.isEmpty()) {
            EntityContext entityContext = new EntityContext(entity, EventEnumType.EntityUpdate, filteredChangedAttributes);
            entityContext.setCompany(getCompany(entity));
            logProcessingResult(postUpdateEvent, entityContext, null, PROCESSED_SUCCESSFULLY);
            return entityContext;
        }
        logProcessingResult(postUpdateEvent, null, Arrays.toString(changedAttributes.toArray()), NO_INTERESTED_ATTRIBUTES_FOUND);
        return null;
    }

    protected Set<String> getFilteredChangedAttribute(Set<String> changedAttributes, DomainEntity entity){
        Set<String> updatedChangedAttributes = new HashSet<>();

        for (String attribute : changedAttributes) {
            String changedAttributeName = entityProcessorUtility.getDecoratedPspAttributeName(getEntityType(), attribute, entity);

            if (isInterestedAttribute(changedAttributeName)) {
                updatedChangedAttributes.add(getCdmAttributeName(changedAttributeName));
            }
        }
        return updatedChangedAttributes;
    }

    protected abstract Set<String> getAttributeFilters();

    protected abstract String getCdmAttributeName(String pspAttribute);

    protected abstract T getEntity(Object entity);

    protected abstract Class<?> getEntityType();

    protected abstract Company getCompany(T entity);

    protected void logProcessingResult(AbstractEvent event, EntityContext context, String changedAttributes, String result){
        String delimiter = ", ";
        String logStr = String.format("Action=Processed_Event, Event=%s, Processor=%s",
                HibernateEventUtil.eventToString(event), this.getClass().getSimpleName());


        if(Objects.nonNull(context)){
            String contextLog = String.format("EntityContext=%s", context.toString());
            logStr = String.join(delimiter, logStr, contextLog);

        }

        if(StringUtils.isNotBlank(changedAttributes)){
            String changedAttributeLog = String.format("ChangedAttributes=%s", changedAttributes);
            logStr = String.join(delimiter, logStr, changedAttributeLog);
        }

        String resultLog = String.format("Result=%s", result);
        logStr = String.join(delimiter, logStr, resultLog);

        log.info(logStr);
    }



}
