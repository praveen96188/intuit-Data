package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BeanMapper<Source,Target> implements Mapper<Source,Target> {


    @Lazy
    @Autowired
    private MapperRegistry mapperRegistry;

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

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    protected void setCustomMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
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
