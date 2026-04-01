package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.sbd.payroll.psp.mapper.EntityCDMMapper;
import com.intuit.sbg.nucleus.mapper.AbstractMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @param <A>
 * @param <B>
 * @author kmuthurangam
 */
public abstract class BeanMapper<A, B> extends AbstractMapping<A, B> {

    @Autowired
    private EntityCDMMapper entityCDMMapper;

    public EntityCDMMapper getEntityCDMMapper() {
        return entityCDMMapper;
    }

}
