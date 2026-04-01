package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class MapperRegistry {

    private final List<BeanMapper> mappers;
    private HashMap<String, BeanMapper> mappersMap;

    @Autowired
    public MapperRegistry(List<BeanMapper> mappers) {
        this.mappers = mappers;
        createMappersMap(this.mappers);
    }

    private void createMappersMap(List<BeanMapper> mappers) {
        mappersMap = new HashMap<>();
        for(BeanMapper mapper : mappers) {
            addMapperToRegistry(mapper);
        }
    }

    protected void addMapperToRegistry(BeanMapper mapper) {
        String key = getKeyForMapper(mapper);
        mappersMap.put(key,mapper);
    }

    private String getKeyForMapper(BeanMapper mapper) {
        return getKeyForSourceTarget(mapper.getSourceType().getTypeName(), mapper.getTargetType().getTypeName());
    }


    private String getKeyForSourceTarget(String sourceEntityClassName, String targetEntityClassName) {
        return sourceEntityClassName + "#" + targetEntityClassName;
    }

    private String getKeyForSourceTarget(Class sourceEntityClass, Class targetEntityClass) {
        return getKeyForSourceTarget(sourceEntityClass.getCanonicalName(), targetEntityClass.getCanonicalName());
    }

    public BeanMapper getMapper(String mapperName) {
        if(mappersMap.containsKey(mapperName)) {
            return mappersMap.get(mapperName);
        } else {
            throw new RuntimeException("mapper with name="+mapperName+" is not registered");
        }
    }

    public BeanMapper getMapper(Class sourceEntityClass, Class targetEntityClass) {
        String key = getKeyForSourceTarget(sourceEntityClass,targetEntityClass);
        return mappersMap.get(key);
    }

    public <U, V> V mapToTarget(U sourceEntity, Class<V> targetClass) {
        // 1. get the mapper
        BeanMapper mapper = getMapper(sourceEntity.getClass(), targetClass);
        // return the target object
        Object targetObject = mapper.mapToTarget(sourceEntity, targetClass);
        return targetClass.cast(targetObject);
    }

}
