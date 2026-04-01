package com.intuit.sbd.payroll.psp.mapper.cdm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BeanMapper<Source,Target> implements Mapper<Source,Target> {

    /* Used Lazy initializattionn due to unresolvable circular reference. Also used field injection to avoid the need to pass mapper object from all sub classes */
    @Lazy
    @Autowired
    private CDMMapper mapper;

    private Type sourceType;
    private Type targetType;

    public BeanMapper() {
        java.lang.reflect.Type genericSuperclass = this.getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            ParameterizedType superType = (ParameterizedType)this.getClass().getGenericSuperclass();

            try {
                this.sourceType = superType.getActualTypeArguments()[0];
                this.targetType = superType.getActualTypeArguments()[1];
            } catch (IllegalArgumentException var4) {
            }
        }
    }

    public CDMMapper getMapper() {
        return mapper;
    }

    public Type getSourceType() {
        if (this.sourceType == null) {
            throw new IllegalStateException("getSourceType() must be overridden when Type parameters are not supplied");
        } else {
            return this.sourceType;
        }
    }

    public Type getTargetType() {
        if (this.targetType == null) {
            throw new IllegalStateException("getSourceType() must be overridden when Type parameters are not supplied");
        } else {
            return this.targetType;
        }
    }

}
