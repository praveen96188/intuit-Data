package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.util.MoneyMovementControlUtil;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;

public class PSPEntityPreInsertEventListener implements PreInsertEventListener {
    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();

        if(!(entity instanceof DomainEntity)){
            return false;
        }

        if(entity instanceof FinancialTransaction) {
            MoneyMovementControlUtil.validateFinancialTransaction(((FinancialTransaction) entity));
        }

        return false;
    }
}
