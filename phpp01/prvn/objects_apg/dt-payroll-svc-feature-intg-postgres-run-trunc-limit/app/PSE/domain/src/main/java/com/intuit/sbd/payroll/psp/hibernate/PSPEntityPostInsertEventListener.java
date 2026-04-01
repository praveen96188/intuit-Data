package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

import java.util.Objects;

public class PSPEntityPostInsertEventListener implements PostInsertEventListener {
    private static final SpcfLogger logger = Application.getLogger(PSPEntityPostInsertEventListener.class);

    @Override
    public void onPostInsert(PostInsertEvent postInsertEvent) {
        Object entity = postInsertEvent.getEntity();

        if(!(entity instanceof DomainEntity)){
            return;
        }

        EntityEventContext entityEventContext = ThreadLocalManager.getEntityEventContext();

        if (Objects.isNull(entityEventContext)) {
            entityEventContext = new EntityEventContext();
        }

        entityEventContext.addEvent(postInsertEvent);
        ThreadLocalManager.setEntityEventContext(entityEventContext);

        boolean enableDetailedLogging = Boolean.parseBoolean(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_entity_processing_detailed_logging"));
        if (enableDetailedLogging) {
            logger.info(String.format("Action=Added_PostInsertEvent, EntityType=%s, EntityId=%s", entity.getClass().getCanonicalName(),
                    ((DomainEntity) entity).getId()));
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }
}