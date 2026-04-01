package com.intuit.sbd.payroll.psp.entity.publisher;

import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import com.intuit.sbd.payroll.psp.entity.HibernateEventUtil;
import com.intuit.sbd.payroll.psp.entity.processor.EntityProcessorUtility;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DependentEntityContextService {
    private final EntityProcessorUtility entityProcessorUtility;
    private final DependentEntityContextFactory dependentEntityContextFactory;

    @Autowired
    public DependentEntityContextService(EntityProcessorUtility entityProcessorUtility,DependentEntityContextFactory dependentEntityContextFactory) {
        this.entityProcessorUtility = entityProcessorUtility;
        this.dependentEntityContextFactory = dependentEntityContextFactory;
    }

    public void addEntityContext(EntityEventContext entityEventContext) {
        List<EntityContext> preProcessEntityEventContext = new ArrayList<>();
        preProcessEntityEventContext.addAll(entityEventContext.getEntityContexts());
        for (EntityContext entityContext : preProcessEntityEventContext) {
            List<IDependentEntityContextProvider> entityContextProvider
                    = dependentEntityContextFactory.getDependentEntityContextProviders(entityContext.getEntityType());
            if (CollectionUtils.isEmpty(entityContextProvider)) {
                continue;
            }
            for (IDependentEntityContextProvider prePublisher : entityContextProvider) {
                List<EntityContext> dependentEntityContexts = new ArrayList<>();
                switch (entityContext.getEventEnumType()) {
                    case EntityCreate:
                        dependentEntityContexts = prePublisher.process(entityContext);
                        break;
                    case EntityUpdate:
                        Set<String> updatedChangedAttributes = entityProcessorUtility.getChangeAttributes(entityContext, entityContext.getChangedAttributes(), prePublisher);
                        if (!updatedChangedAttributes.isEmpty()) {
                            dependentEntityContexts = prePublisher.process(entityContext, updatedChangedAttributes);
                        }
                        break;
                    default:
                        break;
                }
                addEntityContext(entityEventContext, dependentEntityContexts);
            }
        }
    }

    private void addEntityContext(EntityEventContext entityEventContext,List<EntityContext> dependentEntityContexts) {
        for (EntityContext eventContext:
                dependentEntityContexts) {
            if (Objects.isNull(entityEventContext.get(eventContext.getEntityKey()))) {
                entityEventContext.add(eventContext);
                HibernateEventUtil.createEntityUpdateEvent(eventContext);
            }
        }
    }

}
