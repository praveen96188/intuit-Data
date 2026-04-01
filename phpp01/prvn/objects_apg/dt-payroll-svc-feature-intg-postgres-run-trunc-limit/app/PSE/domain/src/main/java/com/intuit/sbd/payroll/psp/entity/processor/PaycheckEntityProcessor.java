package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.AbstractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class PaycheckEntityProcessor extends BaseEntityProcessor<Paycheck> {

    private final List<Class> interestedEntityClasses;
    private final Map<String, String> attributesCdmMap;

    @Autowired
    public PaycheckEntityProcessor(EntityProcessorUtility entityProcessorUtility) {
        super(entityProcessorUtility);

        interestedEntityClasses = Collections.singletonList(Paycheck.class);

        // ideally PSP should publish all domain events (both user & system actions).
        // but currently no downstream consumers are interested in system level actions.
        // Hence, we are only publishing the user level actions
        attributesCdmMap = new HashMap<>(4);
        attributesCdmMap.put("Version", "Version");
        attributesCdmMap.put("Status", "Status");
        attributesCdmMap.put("ModifiedDate", "ModifiedDate");
        attributesCdmMap.put("SessionId", "SessionId");
    }

    @Override
    public List<Class> getInterestedEntities() {
        return interestedEntityClasses;
    }

    @Override
    public Set<String> getAttributeFilters() {
        return attributesCdmMap.keySet();
    }

    @Override
    protected String getCdmAttributeName(String pspAttribute) {
        return attributesCdmMap.get(pspAttribute);
    }

    @Override
    public EntityContext<Paycheck> process(AbstractEvent abstractEvent) {
        return createEntityContext(abstractEvent);
    }

    @Override
    public Class<?> getEntityType() {
        return Paycheck.class;
    }

    @Override
    protected Company getCompany(Paycheck entity) {
        return entity.getCompany();
    }

    @Override
    public Paycheck getEntity(Object entity) {
        if (entity instanceof Paycheck) {
            return (Paycheck) entity;
        }
        return null;
    }

}
