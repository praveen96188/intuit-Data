package com.intuit.sbd.payroll.psp.domainsecondary.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.internal.DefaultFlushEventListener;

import javax.persistence.OptimisticLockException;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Sep 7, 2010
 * Time: 4:47:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSPFlushEventListener extends DefaultFlushEventListener {
    @Override
    public void onFlush(FlushEvent event) throws HibernateException {
        try {
            super.onFlush(event);
        } catch (StaleObjectStateException e) {
            HibernateUtils.parseStaleObjectStateException(e);
            throw e;
        } catch (OptimisticLockException ole) {
            HibernateUtils.parseStaleObjectStateException(ole);
            throw ole;
        }
    }
}
