package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import com.intuit.sbd.payroll.psp.interceptor.model.DomainEntityChangeModel;

/**
 * Created with IntelliJ IDEA.
 * User: ssaxena2
 * Date: 1/9/17
 * Time: 3:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThreadLocalManager {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static String getValue() {
        return threadLocal.get();
    }

    public static void setValue(String value) {
        threadLocal.set(value);
    }

    public static void flush(){
       threadLocal.remove();

    }

    private static final ThreadLocal<Boolean> holdEventCreationRequired = new ThreadLocal<Boolean>();

    public static Boolean isHoldEventCreationRequired() {
        return holdEventCreationRequired.get();
    }

    public static void setHoldEventCreationRequired(Boolean isRequired) {
         holdEventCreationRequired.set(isRequired);
    }

    public static void flushHoldEventCreationRequired(){
        holdEventCreationRequired.remove();

    }

    private static ThreadLocal<EntityEventContext> entityEventContext = new ThreadLocal<EntityEventContext>() {
        protected EntityEventContext initialValue() {
            return new EntityEventContext();
        };
    };

    public static EntityEventContext getEntityEventContext() {
        return entityEventContext.get();
    }

    public static void setEntityEventContext(EntityEventContext context) {
        entityEventContext.set(context);
    }

    public static void flushEntityEventContext(){
        entityEventContext.remove();
    }

    private static ThreadLocal<DomainEntityChangeModel> domainEntityChangeThreadLocal = new ThreadLocal<>();

    public static ThreadLocal<DomainEntityChangeModel> getDomainEntityChangeThreadLocal() {
        return domainEntityChangeThreadLocal;
    }
}
