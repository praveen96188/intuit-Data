package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.filter.constants.PartitionedTablesDetails;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultLoadEventListener;
import org.hibernate.event.spi.LoadEvent;

import java.util.Map;
import java.util.Objects;
/**
 * This class is used to set DomainEntityChangeModel for findById and Lazy Load DB calls.
 * This is required because we are using Interceptor to dynamically add Partition Key to the queries.
 */
@Slf4j
public class PSPLoadEventListener extends DefaultLoadEventListener {
    private PartitionedTablesDetails partitionedTablesDetails;

    public PSPLoadEventListener() {
        partitionedTablesDetails = PayrollApplicationBeanFactory.getBean(PartitionedTablesDetails.class);
    }

    public void onLoad(LoadEvent event, LoadType loadType) throws HibernateException {
        try {
            if(partitionedTablesDetails.getClassNameClassMap().containsKey(event.getEntityClassName())) {
                Class aClass = partitionedTablesDetails.getClassNameClassMap().get(event.getEntityClassName());
                if(Objects.nonNull(aClass)) {
                    DomainEntityChangeManager.setDomainEntityChangeModelContext(aClass, null);
                }
            }
            Map<String, Class> createdDateClassNameClassMap = partitionedTablesDetails.getPartitionedClassNameClassMap().get(InterceptorConstant.CREATED_DATE_SQL);
            if(createdDateClassNameClassMap.containsKey(event.getEntityClassName())) {
                Class aClass = createdDateClassNameClassMap.get(event.getEntityClassName());
                if(Objects.nonNull(aClass)) {
                    DomainEntityChangeManager.setDomainEntityChangeModelContext(aClass, null);
                }
            }
            super.onLoad(event, loadType);
        } catch (Exception e) {
            log.error("Exception occurred during PSPLoadEventListener", e);
            throw new RuntimeException(e);
        } finally {
            DomainEntityChangeManager.removeDomainEntityChangeModel();
        }
    }
}
