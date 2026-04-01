package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.EntityPublisher;
import com.intuit.sbd.payroll.psp.entity.publisher.PublisherFactory;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.beanutils.PropertyUtils;

import javax.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Retry publishing all the unpublished entities created by Real Time Entity publish framework from EntityUpdate table
 * TODO Convert into a batch job for any failures in Entity Publisher
 */
public class RealTimeEntityEventRetryProcessor {

    private PublisherFactory publisherFactory;

    public RealTimeEntityEventRetryProcessor() {
        this.publisherFactory = PayrollApplicationBeanFactory.getBean(PublisherFactory.class);
    }

    public static void main(String[] args) {
        RealTimeEntityEventRetryProcessor entityEventRetryProcessor = new RealTimeEntityEventRetryProcessor();
        entityEventRetryProcessor.init();
        entityEventRetryProcessor.retryPublish();
        entityEventRetryProcessor.destroy();
    }

    private void retryPublish() {
        DomainEntitySet<EntityUpdate> entityUpdates = findAllUnProcessedEntitiesToPublish();
        log("Successfully got all the entities to be republished " + entityUpdates.size());
        Set<EntityContext> entityContexts = createEntityContext(entityUpdates);
        log("Successfully got all the entities to be republished " + entityUpdates.size());
        publishEvent(entityContexts);
    }

    private void init() {
        Application.initialize();
        Application.beginUnitOfWork();
    }

    private DomainEntitySet<EntityUpdate> findAllUnProcessedEntitiesToPublish() {
        SpcfCalendar startTime = SpcfCalendar.getNow();
        startTime.addDays(-2);

        SpcfCalendar endTime = SpcfCalendar.getNow();


        Criterion<EntityUpdate> criterion = EntityUpdate.Status().equalTo(Status.InProgress)
                .And(EntityUpdate.ModifierId().equalTo("KafkaTest"))
                .And(EntityUpdate.CreatedDate().between(startTime, endTime));
        Expression<EntityUpdate> expression = new Query<EntityUpdate>()
                .Where(criterion);
        return Application.find(EntityUpdate.class, expression);
    }

    private Set<EntityContext> createEntityContext(DomainEntitySet<EntityUpdate> entityUpdates) {
        Set<EntityContext> entityContexts = new HashSet<>();
        for (EntityUpdate entityUpdate : entityUpdates) {
            entityContexts.add(createEntityContext(entityUpdate));
        }
        return entityContexts;
    }

    private EntityContext createEntityContext(EntityUpdate entityUpdate) {
        DomainEntity domainEntity = getEntity(entityUpdate);
        EntityContext entityContext = new EntityContext(domainEntity, entityUpdate.getEventType());
        entityContext.setEventId(entityUpdate.getuniqueId());
        entityContext.setCurrentEntity(domainEntity);
        entityContext.setCompany(entityUpdate.getCompany());
        return entityContext;
    }

    private <T> T getEntity(EntityUpdate entityUpdate) {
        DomainEntity domainEntity = null;
        switch (entityUpdate.getEntityName()) {
            case "Paycheck":
                domainEntity = findEntity(SpcfUniqueId.createInstance(entityUpdate.getEntityId()), Paycheck.class);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return (T) domainEntity;
    }

    private void setId(DomainEntity domainEntity, String entityId) {
        try {
            PropertyUtils.setSimpleProperty(domainEntity, "id", SpcfUniqueId.createInstance(entityId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishEvent(Set<EntityContext> entityContexts) {
        for (EntityContext entityContext : entityContexts) {
            publishEntityContext(entityContext);
        }
    }

    private boolean publishEntityContext(EntityContext context) {
        boolean isPublished = false;
        try {
            EntityPublisher publisher = publisherFactory.getPublisher(context.getEntityType());
            isPublished = publisher.publish(context);
        } catch (Exception e) {
            e.printStackTrace();
            log(String.format("Action=Publish_Failed_Exception, EntityContext=%s", context.toString()));
        } finally {
            //todo: new state to handle invalid publish request
            Status status = isPublished ? Status.Published : Status.Failed;
            updateEventStatus(context, status);
            log(String.format("PublishStatus=%s, EntityContext=%s", status, context.toString()));
            return isPublished;
        }
    }

    protected DomainEntity findEntity(SpcfUniqueId id, Class<?> sourceClass) {
        DomainEntity domainEntity = (DomainEntity) Application.findById(sourceClass, id);
        if (Objects.isNull(domainEntity)) {
            throw new EntityNotFoundException(String.format("Entity not found, entityType=%s, id=%s", sourceClass.getSimpleName(), id));
        }
        return domainEntity;
    }

    private void updateEventStatus(EntityContext context, Status status) {
        EntityUpdate entityUpdate = Application.findById(EntityUpdate.class,
                SpcfUniqueId.createInstance(context.getEventId()));
        entityUpdate.setStatus(status);
        Application.save(entityUpdate);
    }

    private void destroy() {
        Application.commitUnitOfWork();
        Application.uninitialize();
    }

    private void log(String message) {
        System.out.println(message);
    }
}
