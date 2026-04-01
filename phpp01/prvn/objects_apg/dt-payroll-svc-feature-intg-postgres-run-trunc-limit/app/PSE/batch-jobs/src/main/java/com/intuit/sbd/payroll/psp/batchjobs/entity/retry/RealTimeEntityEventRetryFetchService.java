package com.intuit.sbd.payroll.psp.batchjobs.entity.retry;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class RealTimeEntityEventRetryFetchService {

    private Map<String,Class<?>> entityNameMap;
    private PSPRequestContextManager pspRequestContextManager;

    @Autowired
    public RealTimeEntityEventRetryFetchService(PSPRequestContextManager pspRequestContextManager) {
        entityNameMap = new HashMap<>();
        this.pspRequestContextManager = pspRequestContextManager;
    }

    public EntityEventContext createEntityEventContext(List<EntityUpdate> entityUpdates) {
        EntityEventContext entityEventContext = new EntityEventContext();
        try{
            Application.beginUnitOfWork();
            for (EntityUpdate entityUpdate : entityUpdates) {
                try{
                    pspRequestContextManager.setRequestContextCompany(entityUpdate.getCompany());
                    entityEventContext.add(createEntityContext(entityUpdate));
                } catch (Exception e) {
                    log.error("Exception while creating Entity Event Context for Id={}", entityUpdate.getId().toString(), e);
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
        return entityEventContext;
    }

    public EntityContext createEntityContext(EntityUpdate entityUpdate) throws ClassNotFoundException {
        DomainEntity domainEntity = getEntity(entityUpdate);
        EntityContext entityContext = new EntityContext(domainEntity, entityUpdate.getEventType());
        entityContext.setEventId(entityUpdate.getuniqueId());
        entityContext.setCurrentEntity(domainEntity);
        entityContext.setCompany(entityUpdate.getCompany());
        return entityContext;
    }

    private <T> T getEntity(EntityUpdate entityUpdate) throws ClassNotFoundException {
        String id = entityUpdate.getEntityId();
        String className = entityUpdate.getEntityName();
        Class<?> sourceClass = getClassFromName(className);
        DomainEntity domainEntity = findEntity(id, sourceClass);
        return (T) domainEntity;
    }

    protected DomainEntity findEntity(String id, Class<?> sourceClass) {
        DomainEntity domainEntity = (DomainEntity) Application.findById(sourceClass, SpcfUniqueId.createInstance(id));
        if (Objects.isNull(domainEntity)) {
            throw new EntityNotFoundException(String.format("Entity not found, entityType=%s, id=%s", sourceClass.getSimpleName(), id));
        }
        return domainEntity;
    }

    private Class<?> getClassFromName(String className) throws ClassNotFoundException {
        if(entityNameMap.containsKey(className)) {
            return entityNameMap.get(className);
        }
        String fullClassName = new StringBuilder()
                .append("com.intuit.sbd.payroll.psp.domain.")
                .append(className)
                .toString();
        Class<?> sourceClass = Class.forName(fullClassName);
        entityNameMap.putIfAbsent(className, sourceClass);
        return sourceClass;
    }
}
