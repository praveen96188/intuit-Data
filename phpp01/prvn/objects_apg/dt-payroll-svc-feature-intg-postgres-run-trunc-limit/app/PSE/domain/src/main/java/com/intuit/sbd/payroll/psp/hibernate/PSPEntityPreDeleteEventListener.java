package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;

@Slf4j
public class PSPEntityPreDeleteEventListener implements PreDeleteEventListener {

    public PSPEntityPreDeleteEventListener() {
        super();
    }

    // PreDeleteListener is called just before the prepare sql statement call in Hibernate.
    //Setting DomainEntityChangeModel context here. This is cleared in the PreparedStatementInterceptor in the post process step
    @Override
    public boolean onPreDelete(PreDeleteEvent preDeleteEvent) {

        try {
            if (preDeleteEvent.getEntity() instanceof DomainEntity) {
                DomainEntity entity = (DomainEntity) preDeleteEvent.getEntity();
                DomainEntityChangeManager.setDomainEntityChangeModelContext(entity.getClass(), entity);
            }
        } catch (Exception e) {
            log.error("Exception occured during PSPEntityPreDeleteEventListener", e);
        }

        return false;
    }
}
