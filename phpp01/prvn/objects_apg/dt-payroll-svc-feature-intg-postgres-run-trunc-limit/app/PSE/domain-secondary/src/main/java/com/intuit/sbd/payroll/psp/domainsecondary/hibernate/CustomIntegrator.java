package com.intuit.sbd.payroll.psp.domainsecondary.hibernate;

import com.intuit.sbd.payroll.psp.hibernate.PSPEntityInsertEventListener;
import com.intuit.sbd.payroll.psp.hibernate.PSPEntityPreDeleteEventListener;
import com.intuit.sbd.payroll.psp.hibernate.PSPEntityPreUpdateEventListener;
import com.intuit.sbd.payroll.psp.hibernate.PSPEntityUpdateEventListener;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * @author cbhat
 * CustomIntegrator to attach EventListeners
 */
public class CustomIntegrator implements Integrator {

	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
        EventListenerRegistry listener = serviceRegistry.getService(EventListenerRegistry.class);
        listener.appendListeners(EventType.FLUSH, new PSPFlushEventListener());
        listener.appendListeners(EventType.AUTO_FLUSH, new PSPAutoFlushEventListener());
		listener.appendListeners(EventType.SAVE, new PSPEntityInsertEventListener());
		listener.appendListeners(EventType.UPDATE, new PSPEntityUpdateEventListener());
		listener.appendListeners(EventType.PRE_UPDATE, new PSPEntityPreUpdateEventListener());
		listener.appendListeners(EventType.PRE_DELETE, new PSPEntityPreDeleteEventListener());

	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

	}
}
