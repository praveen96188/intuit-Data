package com.intuit.sbd.payroll.psp.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.internal.DefaultAutoFlushEventListener;

import javax.persistence.OptimisticLockException;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Sep 7, 2010
 * Time: 4:55:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSPAutoFlushEventListener extends DefaultAutoFlushEventListener {
    @Override
    public void onAutoFlush(AutoFlushEvent event) throws HibernateException {
        try {
            super.onAutoFlush(event);
        } catch (StaleObjectStateException e) {
            HibernateUtils.parseStaleObjectStateException(e);
            throw e;
        } catch (OptimisticLockException ole) {
            HibernateUtils.parseStaleObjectStateException(ole);
            throw ole;
        }
    }
}
