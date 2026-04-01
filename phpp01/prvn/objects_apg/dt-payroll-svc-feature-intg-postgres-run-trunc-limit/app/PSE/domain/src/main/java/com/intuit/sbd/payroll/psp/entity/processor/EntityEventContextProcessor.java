package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import com.intuit.sbd.payroll.psp.entity.HibernateEventUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.AbstractEvent;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class EntityEventContextProcessor {

    private EntityProcessorConfiguration entityProcessorConfiguration;

    @Autowired
    public EntityEventContextProcessor(EntityProcessorConfiguration entityProcessorConfiguration) {
        this.entityProcessorConfiguration = entityProcessorConfiguration;
    }

    public EntityEventContext process(EntityEventContext entityEventContext) {
        try {
            Collection<AbstractEvent> eventsToProcess = null;

            if (Objects.isNull(entityEventContext)) {
                return null;
            }

            eventsToProcess = filterEventsToProcess(entityEventContext.getEvents());
            if (log.isDebugEnabled()) {
                log.debug(String.format("Action=Interested_Events, EventCount=%s, Events=%s",
                        eventsToProcess.size(), HibernateEventUtil.eventsListToString(eventsToProcess)));
            }

            for (AbstractEvent event : eventsToProcess) {
                processEvent(entityEventContext, event);
            }

        } finally {
            // clearing the events after processing, this will ensure that same events are not
            // processed twice when we are flushing/processing context twice in the same session
            entityEventContext.clearEventsToProcess();
        }
        return entityEventContext;
    }

    private Collection<AbstractEvent> filterEventsToProcess(Collection<AbstractEvent> events){
        Collection<AbstractEvent> eventsToProcess = new ArrayList<>();
        for (AbstractEvent event : events) {
            try {
                if (entityProcessorConfiguration.hasRegisteredProcessors(HibernateEventUtil.getEntity(event).getClass())) {
                    eventsToProcess.add(event);
                }
            } catch (Exception e){
                log.error(String.format("Action=Event_Filter_Exception, Event=%s", HibernateEventUtil.eventToString(event)), e);
            }
        }
        return eventsToProcess;
    }

    private void processEvent(EntityEventContext entityEventContext, AbstractEvent event) {
        try {
            List<EntityProcessor> processors = entityProcessorConfiguration.getEntityProcessors(HibernateEventUtil.getEntity(event).getClass());

            if (Objects.isNull((processors))) {
                return;
            }

            for (EntityProcessor processor : processors) {

                if (!isEntityPublishFlagEnabled((BaseEntityProcessor) processor)) {
                    continue;
                }
                EntityContext entityContext = processor.process(event);
                if (Objects.isNull(entityContext)) {
                    continue;
                }

                String sourceTid = MDC.get(IntuitCommonHeaders.INTUIT_HEADER_TID);
                if (Objects.nonNull(sourceTid)) {
                    sourceTid = sourceTid.replace("intuit_tid=", "");
                }
                entityContext.setSourceTid(sourceTid);
                entityEventContext.add(entityContext);
            }
        } catch (Exception e){
            log.error(String.format("Action=Entity_Processing_Exception, Event=%s", HibernateEventUtil.eventToString(event)), e);
            entityEventContext.getFailedToProcessEvents().add(event);
        }
    }

    private boolean isEntityPublishFlagEnabled(final BaseEntityProcessor processor) {
        boolean publishEnabled = false;
        String entityString = "," + processor.getEntityType().getSimpleName() + ",";
        String entityEnabledForPublish = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_entity_enabled_for_publish");
        if (entityEnabledForPublish.contains(entityString)) {
            publishEnabled = true;
        }
        return publishEnabled;
    }
}
