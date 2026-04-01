package com.intuit.sbd.payroll.psp.entity.publisher;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.entity.EntityContext;

public interface EntityPublisher<T extends DomainEntity> {

    default boolean publishRestrictedAndNonRestricted(EntityContext<T> entity) {
        return publishRestricted(entity) && publish(entity);
    }

    boolean publish(EntityContext<T> entity);

    boolean publishRestricted(EntityContext<T> entity);

    Class<?> getEntityClass();

}
