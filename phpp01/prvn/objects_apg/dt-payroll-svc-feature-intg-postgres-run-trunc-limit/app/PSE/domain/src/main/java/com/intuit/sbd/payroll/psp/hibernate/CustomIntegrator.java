package com.intuit.sbd.payroll.psp.hibernate;

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
		listener.appendListeners(EventType.POST_UPDATE, new PSPEntityPostUpdateEventListener());
		listener.appendListeners(EventType.POST_INSERT, new PSPEntityPostInsertEventListener());
		listener.appendListeners(EventType.PRE_UPDATE, new PSPEntityPreUpdateEventListener());
		listener.appendListeners(EventType.PRE_DELETE, new PSPEntityPreDeleteEventListener());
		listener.appendListeners(EventType.PRE_INSERT, new PSPEntityPreInsertEventListener());
		listener.setListeners(EventType.LOAD, new PSPLoadEventListener());
		listener.setListeners(EventType.INIT_COLLECTION, new PSPInitializeCollectionEventListener());

	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

	}
}
