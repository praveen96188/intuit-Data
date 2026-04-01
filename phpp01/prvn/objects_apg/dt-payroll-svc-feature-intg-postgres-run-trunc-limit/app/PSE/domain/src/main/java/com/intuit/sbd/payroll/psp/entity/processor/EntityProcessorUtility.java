package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.IDependentEntityContextProvider;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EntityProcessorUtility {

    private static final String DELIMITER = ".";

    public Set<String> getChangedAttributes(PostUpdateEvent postUpdateEvent) {
        return findDirtyProperties(postUpdateEvent.getDirtyProperties(), postUpdateEvent.getPersister());
    }

    private Set<String> findDirtyProperties(int[] dirtyProperties, EntityPersister persister) {
        String[] allPropertyNames = persister.getPropertyNames();
        Set<String> dirtyPropertyNames = new HashSet<>();

        for (int i = 0; i < dirtyProperties.length; ++i) {
            dirtyPropertyNames.add(allPropertyNames[dirtyProperties[i]]);
        }

        return dirtyPropertyNames;
    }

    public String getDecoratedPspAttributeName(Class<?> entityType, String attribute, DomainEntity entity){
        if (entityType.isInstance(entity)) {
            return attribute;
        }
        return String.join(DELIMITER, entity.getClass().getSimpleName(), attribute);
    }

    public Set<String> getChangeAttributes(EntityContext entityContext, Set<String> changeAttributes, IDependentEntityContextProvider prePublisher) {
        Set<String> updatedChangedAttributes = new HashSet<>();
        if (Objects.isNull(changeAttributes))
            return updatedChangedAttributes;

        for (String attribute : changeAttributes) {
            String changedAttributeName = getDecoratedPspAttributeName(entityContext.getEntityType(), attribute, Application.findById(entityContext.getEntityType(),entityContext.getEntityId()));
            if (prePublisher.isInterestedCdmAttribute(changedAttributeName)) {
                updatedChangedAttributes.add(changedAttributeName);
            }
        }
        return updatedChangedAttributes;
    }
}
