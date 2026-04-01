package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.util.MoneyMovementControlUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;

@Slf4j
public class PSPEntityPreUpdateEventListener implements PreUpdateEventListener {


    // PreUpdateListener is called just before the prepare sql statement call in Hibernate.
    //Setting DomainEntityChangeModel context here. This is cleared in the PreparedStatementInterceptor in the post process step

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();

        if(!(entity instanceof DomainEntity)){
            return false;
        }

        if(entity instanceof FinancialTransaction) {
            MoneyMovementControlUtil.validateFinancialTransaction(((FinancialTransaction) entity));
        }

        try {
            DomainEntity domainEntity = (DomainEntity)event.getEntity();
            DomainEntityChangeManager.setDomainEntityChangeModelContext(domainEntity.getClass(), domainEntity);
        } catch (Exception e) {
            log.error("Exception occured during PSPEntityPreUpdateEventListener", e);
        }
        return false;
    }
}
