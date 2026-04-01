package com.intuit.sbd.payroll.psp.hibernate;

import java.io.Serializable;

import org.hibernate.event.internal.DefaultUpdateEventListener;
import org.hibernate.Hibernate;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.event.spi.SaveOrUpdateEvent;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.domain.util.EntityListenerFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * This class is a custom hibernate listener which gets
 * triggered for all updates
 * and used to update table
 * PSP_ENTITY_UPDATE with event updates
 * for the entities configured
 *
 * @author dchoudhary1
 *
 */
public class PSPEntityUpdateEventListener extends DefaultUpdateEventListener {

    private static final SpcfLogger logger = Application.getLogger(PSPEntityUpdateEventListener.class);

	@Override
	public  Serializable performSaveOrUpdate(SaveOrUpdateEvent event) {
		try{
			logger.info("Begin PSPEntityUpdateEventListener");
	        Object entity = event.getEntity();
			logger.info("the value of entity"+entity.toString());
	        EntityChangeListener entityChange=EntityListenerFactory.getentityClass(entity);
	        if(entityChange!=null){
	        	logger.info("Triggered the PSPEntityUpdateEventListener for entity"+entityChange);
	        	//No need to handle for now as no Entity except SystemParameter
	        	//calling this
	        }
	    	return super.performSaveOrUpdate(event);
		}catch(Exception exception){
			logger.error("Exception occurred while saving entity with error:"+ exception.getMessage());
	    	return super.performSaveOrUpdate(event);

		}

	}

}

