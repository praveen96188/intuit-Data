package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.sbd.payroll.psp.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Custom utility mapper class used to convert from one domain entity to the other. Support
 * conversion only for pre-defined mappers.
 *
 * @author kmuthurangam
 */
@Component
public class CDMMapper {

    @SuppressWarnings("rawtypes")
    private List<Mapper> mappers;
    private Map<String, Mapper> mapperMap;

    @Autowired
    public CDMMapper(List<Mapper> mappers) {
        this.mappers = mappers;
        mapperMap =  new HashMap<>();
        prepareMappers();
    }

    private void prepareMappers() {
        mappers.stream().forEach((mapper -> {addMapper(mapper);}));
    }

    public void addMapper(Mapper mapper) {
        mapperMap.put(getKey(mapper), mapper);
    }

    private String getKey(Mapper mapper) {
        return getKey(mapper.getSourceType().getTypeName(), mapper.getTargetType().getTypeName());
    }

    private String getKey(String sourceClassName, String targetClassName) {
        return String.join("#", sourceClassName, targetClassName);
    }

    private String getKey(Class sourceClass, Class targetClass) {
        return getKey(sourceClass.getCanonicalName(), targetClass.getCanonicalName());
    }

    private Mapper getMapper(Class sourceClass, Class targetClass) {
        return mapperMap.get(getKey(sourceClass, targetClass));
    }

    @SuppressWarnings("unchecked")
    public <U, V> V mapToTarget(U sourceEntity, Class<V> targetClass) {
        if(Objects.isNull(sourceEntity)){
            return null;
        }
        Mapper mapper = getMapper(Application.getDomainEntityType(sourceEntity), targetClass);
        Object targetEntity = mapper.mapToTarget(sourceEntity, targetClass);
        return targetClass.cast(targetEntity);
    }
}
