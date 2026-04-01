package com.intuit.sbd.payroll.psp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LazyLoadAttributeAspect {

    @Pointcut("execution(* org.hibernate.engine.spi.PersistentAttributeInterceptor.read*(..)) || execution(* org.hibernate.engine.spi.PersistentAttributeInterceptor.write*(..))")
    public void applyPointCut() {}

    @Around("applyPointCut()")
    public Object modifyLazyAttributeName(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        String fieldName = (String) args[1];
        if(fieldName.startsWith("m")) {
            fieldName = fieldName.substring(1);
        }
        args[1] = fieldName;
        return proceedingJoinPoint.proceed(args);
    }
}
