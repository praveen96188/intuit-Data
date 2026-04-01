package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import org.hibernate.event.spi.AbstractEvent;

import java.util.List;

public interface EntityProcessor<T extends DomainEntity> {

    List<Class> getInterestedEntities();

    EntityContext<T> process(AbstractEvent abstractEvent);
}
