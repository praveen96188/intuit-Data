package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import com.intuit.sbd.payroll.psp.entity.HibernateEventUtil;
import com.intuit.sbd.payroll.psp.entity.processor.EntityEventContextProcessor;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.internal.DefaultFlushEventListener;

import javax.persistence.OptimisticLockException;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Sep 7, 2010
 * Time: 4:47:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSPFlushEventListener extends DefaultFlushEventListener {
    private EntityEventContextProcessor contextProcessor;

    public PSPFlushEventListener(){
        contextProcessor = PayrollApplicationBeanFactory.getBean(EntityEventContextProcessor.class);
    }

    private static final SpcfLogger logger = Application.getLogger(PSPFlushEventListener.class);
    @Override
    public void onFlush(FlushEvent event) throws HibernateException {
        try {
            super.onFlush(event);
            processEntityEventContext(event);

        } catch (StaleObjectStateException e) {
            ThreadLocalManager.flushEntityEventContext();
            HibernateUtils.parseStaleObjectStateException(e);
            throw e;
        } catch (OptimisticLockException ole) {
            ThreadLocalManager.flushEntityEventContext();
            HibernateUtils.parseStaleObjectStateException(ole);
            throw ole;
        } catch (Exception e) {
            ThreadLocalManager.flushEntityEventContext();
            throw e;
        }
    }

    //flush listener will be called multiple times as whenever we make a call to Application.find, there instead of rollback
    //we call the commit unit of work
    private void processEntityEventContext(FlushEvent event){
        try {
            EntityEventContext entityEventContext = ThreadLocalManager.getEntityEventContext();
            if (Objects.isNull(entityEventContext)) {
                return;
            }

            if (CollectionUtils.isEmpty(entityEventContext.getEvents())) {
                return;
            }

            boolean enableDetailedLogging = Boolean.parseBoolean(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_entity_processing_detailed_logging"));
            if (enableDetailedLogging) {
                logger.info(String.format("Action=Events_Captured, PostInsertEventCount=%s, PostUpdateEventCount=%s, Events=%s",
                        entityEventContext.getPostInsertEventCount(), entityEventContext.getPostUpdateEventCount(), entityEventContext.getEventsString()));
            }
            entityEventContext = contextProcessor.process(entityEventContext);

            if (CollectionUtils.isEmpty(entityEventContext.getEntityContexts()))
                return;

            createEventForEntityContexts(entityEventContext.getEntityContexts());

            super.onFlush(event);
        }catch (Exception e){
            logger.error("Action=Entity_Processing_Exception",e);
        }
    }

    private void createEventForEntityContexts(Collection<EntityContext> contextList){
        for (EntityContext context:contextList) {
            // flush listener gets called multiple times which creates multiple entityUpdate rows in the
            // table. To prevent that, putting a check on eventId. if event ID is not present then only create
            // event.
            if(StringUtils.isBlank(context.getEventId())) {
                HibernateEventUtil.createEntityUpdateEvent(context);
            }
        }
    }

}
