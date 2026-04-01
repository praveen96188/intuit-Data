package com.intuit.sbd.payroll.psp.mapper.cdm;


import java.lang.reflect.Type;

public interface Mapper<Source,Target> {

    Target mapToTarget(Source sourceEntity, Class<Target> targetClass);

    Type getSourceType();

    Type getTargetType();
}
