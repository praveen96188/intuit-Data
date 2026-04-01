package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.util.TransactionObserver;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyEventEmail;
import com.intuit.sbd.payroll.psp.Application;

import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 20, 2009
 * Time: 3:11:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailTransactionObserver extends TransactionObserver {
    List<CompanyEvent> eventQueue = new Vector<CompanyEvent>();

    private void clearQueue() {
        eventQueue.clear();
    }

    public void queueEvent(CompanyEvent pEvent) {
        eventQueue.add(pEvent);
    }

    public void unregistered() {
        clearQueue();
    }

    public void beforeTransactionBegin() {
        clearQueue();
    }

    public void beforeTransactionRollback() {
        clearQueue();
    }

    public void beforeTransactionCommit() {
        // flush the hibernate session cache so all newly created objects are visible to queries down stream.
        if (Application.getHibernateSession().isDirty()) {
            Application.getHibernateSession().flush();
        }

        CompanyEventEmail.createEmailForEvents(eventQueue);
    }

    public void afterTransactionCommit() {
        clearQueue();
    }
}
