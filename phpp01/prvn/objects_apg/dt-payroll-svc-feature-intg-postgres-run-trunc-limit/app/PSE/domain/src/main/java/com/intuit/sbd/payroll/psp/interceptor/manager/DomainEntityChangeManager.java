package com.intuit.sbd.payroll.psp.interceptor.manager;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.interceptor.factory.PreparedStatementInterceptorFactory;
import com.intuit.sbd.payroll.psp.interceptor.model.DomainEntityChangeModel;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;

import java.util.Objects;

public class DomainEntityChangeManager {

    private static PreparedStatementInterceptorFactory preparedStatementInterceptorFactory;

    public static void setDomainEntityChangeModelContext(Class<?> clazz, DomainEntity domainEntity){
        if(Objects.isNull(preparedStatementInterceptorFactory)) {
            preparedStatementInterceptorFactory = PayrollApplicationBeanFactory.getBean(PreparedStatementInterceptorFactory.class);
        }
        if (preparedStatementInterceptorFactory.getPreparedStatementEntities().contains(clazz.getCanonicalName())) {
            DomainEntityChangeModel domainEntityChangeModel = new DomainEntityChangeModel(clazz, domainEntity);
            ThreadLocalManager.getDomainEntityChangeThreadLocal().set((domainEntityChangeModel));
        }
    }

    public static void setDomainEntityChangeModelContext(DomainEntityChangeModel domainEntityChangeModel){
        ThreadLocalManager.getDomainEntityChangeThreadLocal().set(domainEntityChangeModel);
    }

    public static DomainEntityChangeModel getDomainEntityChangeModelContext(){
        return ThreadLocalManager.getDomainEntityChangeThreadLocal().get();
    }

    public static void removeDomainEntityChangeModel(){
        ThreadLocalManager.getDomainEntityChangeThreadLocal().set(null);
    }
}
