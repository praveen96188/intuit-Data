package com.intuit.sbd.payroll.psp.entity.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Component
public class DependentEntityContextFactory {

    private MultiValueMap<Class<?>, IDependentEntityContextProvider> dependentEntityContextProviderMap;

    @Autowired
    public DependentEntityContextFactory(List<IDependentEntityContextProvider> dependentEntityContextProviders) {
        dependentEntityContextProviderMap = new LinkedMultiValueMap<>();
        for (IDependentEntityContextProvider dependentEntityContextProvider : dependentEntityContextProviders) {
            dependentEntityContextProviderMap.add(dependentEntityContextProvider.getEntityClass(), dependentEntityContextProvider);
        }
    }

    public List<IDependentEntityContextProvider> getDependentEntityContextProviders(Class entityClass){
        return dependentEntityContextProviderMap.get(entityClass);
    }

}
