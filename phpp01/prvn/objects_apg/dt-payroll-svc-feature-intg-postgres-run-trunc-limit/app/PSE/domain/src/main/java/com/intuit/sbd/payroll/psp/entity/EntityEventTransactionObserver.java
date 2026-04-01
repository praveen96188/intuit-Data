package com.intuit.sbd.payroll.psp.entity;

import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.entity.publisher.EntityEventPublisherExecutor;
import com.intuit.sbd.payroll.psp.util.TransactionObserver;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Objects;

@Slf4j
public class EntityEventTransactionObserver extends TransactionObserver {

    private static final EntityEventPublisherExecutor entityEventPublisherExecutor = PayrollApplicationBeanFactory.getBean(EntityEventPublisherExecutor.class);

    @Override
    public void afterTransactionCommit() {
        publishEvent();
    }

    private void publishEvent() {
        try {
            EntityEventContext entityEventContext = ThreadLocalManager.getEntityEventContext();
            if (Objects.isNull(entityEventContext)) {
                return;
            }

            if (CollectionUtils.isNotEmpty(entityEventContext.getFailedToProcessEvents()))
                log.info(String.format("Action=FailedToProcessEvents, EventCount=%s, Events=%s",
                        entityEventContext.getFailedToProcessEvents().size(), entityEventContext.getFailedEventsString()));


            if (CollectionUtils.isNotEmpty(entityEventContext.getEntityContexts())) {
                log.info(String.format("Action=EntityContexts_To_Publish: EntityContextCreateCount=%s, EntityContextUpdateCount=%s, Contexts=%s",
                        entityEventContext.getEntityContextCreateCount(), entityEventContext.getEntityContextUpdateCount(), entityEventContext.getEntityContextsString()));

                entityEventPublisherExecutor.publishEvent(entityEventContext);
            }
        } catch (Exception e) {
            log.error("Action=EntityEventTransactionObserver_Exception", e);
        } finally {
            ThreadLocalManager.flushEntityEventContext();
        }
    }
}
