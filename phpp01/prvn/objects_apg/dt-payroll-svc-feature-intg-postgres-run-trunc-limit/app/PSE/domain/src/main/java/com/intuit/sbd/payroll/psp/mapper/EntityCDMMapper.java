package com.intuit.sbd.payroll.psp.mapper;

import com.intuit.sbg.nucleus.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Custom utility mapper class used to convert from one domain entity to the other. Support
 * conversion only for pre-defined mappers.
 *
 * @param <U> - Source entity
 * @param <V> - Destination entity
 * @author kmuthurangam
 */
@Component
public class EntityCDMMapper {

    @SuppressWarnings("rawtypes")
    @Autowired
    @Qualifier("objectMapper")
    private Mapper objectMapper;

    @SuppressWarnings("unchecked")
    public <U, V> V mapToTarget(U sourceEntity, Class<V> targetClass) {
        Object targetEntity = objectMapper.mapToTarget(sourceEntity, targetClass);
        return targetClass.cast(targetEntity);
    }
}
