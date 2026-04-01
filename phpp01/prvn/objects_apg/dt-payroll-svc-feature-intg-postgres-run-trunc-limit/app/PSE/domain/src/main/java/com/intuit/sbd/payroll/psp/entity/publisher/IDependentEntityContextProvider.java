package com.intuit.sbd.payroll.psp.entity.publisher;

import com.intuit.sbd.payroll.psp.entity.EntityContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface IDependentEntityContextProvider {

    default List<EntityContext> process(EntityContext entityEventContext){
        return process(entityEventContext,new HashSet<>());
    }

    List<EntityContext> process(EntityContext entityContext, Set<String> updatedChangedAttributes);

    Class getEntityClass();
    boolean isInterestedCdmAttribute(String attribute);

}
