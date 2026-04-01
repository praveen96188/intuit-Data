package com.intuit.sbd.payroll.psp.entity;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.util.TransactionObserver;

public class DGTransactionObserver extends TransactionObserver {

    @Override
    public void afterTransactionBegin() {
        if (!AuthUser.hasSAPAdminAccess()) {
            Application.getHibernateSession().enableFilter("defaultFilter").setParameter("isDgAssociated", 0);
        }
    }
}
