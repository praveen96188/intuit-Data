package com.intuit.sbd.payroll.psp.entity.publisher;

import com.intuit.eventbus.exceptions.SchemaViolationException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.context.threading.ChildThreadRequestContextHelper;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import com.intuit.sbd.payroll.psp.entity.HibernateEventUtil;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Slf4j
public class PublisherService implements Callable<Void> {

    private EntityEventContext entityEventContext;
    private PublisherFactory publisherFactory;
    private DependentEntityContextService dependentEntityContextService;
    private ChildThreadRequestContextHelper childThreadRequestContextHelper;

    public PublisherService(EntityEventContext entityEventContext, ChildThreadRequestContextHelper childThreadRequestContextHelper) {
        this.publisherFactory = PayrollApplicationBeanFactory.getBean(PublisherFactory.class);
        this.dependentEntityContextService = PayrollApplicationBeanFactory.getBean(DependentEntityContextService.class);
        this.entityEventContext = entityEventContext;
        this.childThreadRequestContextHelper = childThreadRequestContextHelper;
    }

    @Override
    public Void call() {
        setThreadLocalRequestContext();
        int publishedEntityCount=0;
        try {
            Application.beginUnitOfWork();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EntityPublisher));
            dependentEntityContextService.addEntityContext(entityEventContext);
            log.info("Action=Started_Publishing_Service, EntitiesToPublishCount={}", entityEventContext.getEntityContexts().size());
            for (EntityContext context : entityEventContext.getEntityContexts()) {
                try {
                    Company company = Application.findById(Company.class, context.getCompany().getId());
                    SpcfCalendar oneWeekAgoDate = SpcfCalendar.getNow();
                    oneWeekAgoDate.addDays(-7);
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().setCreatedDate(oneWeekAgoDate);
                    context.setCurrentEntity(findEntity(context.getEntityId(), context.getEntityType()));
                    if (publishEntityContext(context)) {
                        publishedEntityCount++;
                    }
                } finally {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().clearCreatedDate();
                }
            }
            Application.commitUnitOfWork();
        } catch (Exception e) {
            log.error("Action=Publisher_Service_Exception", e);
        } finally {
            int totalPublishRequests = entityEventContext.getEntityContexts().size();
            log.info("Action=Completed_Publishing_Service ,TotalPublishRequest={} PublishedEntityCount={}, FailedEntityCount={}",
                    totalPublishRequests, publishedEntityCount, totalPublishRequests - publishedEntityCount);
            Application.rollbackUnitOfWork();
            childThreadRequestContextHelper.clearThreadLocals();
        }
        return null;
    }

    private void setThreadLocalRequestContext(){
        childThreadRequestContextHelper.setThreadLocals();
        if(Objects.isNull(PSPRequestContextManagerHelper.getPSPRequestContextManager().getRequestContext())) {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContext(null,RequestType.EVENTPUBLISHER,"publisherService");
        }else {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().getRequestContext().setRequestType(RequestType.EVENTPUBLISHER);
        }
    }

    private boolean publishEntityContext(EntityContext context) {
        boolean isPublished = false;
        boolean isRecoverableError = true;
        try {
            EntityPublisher publisher = publisherFactory.getPublisher(context.getEntityType());
            isPublished = publisher.publish(context);
        } catch (Exception e) {
            log.error("Action=Publish_Failed_Exception, EntityContext={}", context, e);
            Throwable[] throwableList = ExceptionUtils.getThrowables(e);
            for(Throwable throwable : throwableList) {
                if(throwable.getClass().equals(SchemaViolationException.class) || throwable.getClass().equals(EntityCDMMappingException.class)) {
                    isRecoverableError = false;
                    break;
                }
            }
        } finally {
            Status status = isPublished ? Status.Published : Status.Failed;
            try{
                updateEventStatus(context, status, isRecoverableError);
            } catch (Exception e) {
                log.error("Action=Publish_Status_Failed_To_Update_Exception, EntityContext={}", context, e);
            }
            log.info("PublishStatus={}, EntityContext={}", status, context);
            return isPublished;
        }
    }

    private void updateEventStatus(EntityContext context, Status status, boolean isRecoverableError) {
        EntityUpdate entityUpdate = Application.findById(EntityUpdate.class,
                SpcfUniqueId.createInstance(context.getEventId()));
        /*
         Entity Update can be null for some context event id due to some bug while committing in table
         So creating new entityUpdate for updating the status
         */
        if(Objects.isNull(entityUpdate)) {
            entityUpdate = HibernateEventUtil.createEntityUpdateEvent(context);
        }
        if(!isRecoverableError) {
            entityUpdate.setRetryCount(-1);
        } else if(entityUpdate.getStatus().equals(Status.Failed)) {
            int retryCount = entityUpdate.getRetryCount();
            entityUpdate.setRetryCount(++retryCount);
        }
        entityUpdate.setStatus(status);
        Application.save(entityUpdate);
    }

    protected DomainEntity findEntity(SpcfUniqueId id, Class<?> sourceClass){
        DomainEntity domainEntity = (DomainEntity) Application.findById(sourceClass, id);
        if(Objects.isNull(domainEntity)){
            throw new EntityNotFoundException(String.format("Entity not found, entityType=%s, id=%s", sourceClass.getSimpleName(), id));
        }
        return domainEntity;
    }

}
