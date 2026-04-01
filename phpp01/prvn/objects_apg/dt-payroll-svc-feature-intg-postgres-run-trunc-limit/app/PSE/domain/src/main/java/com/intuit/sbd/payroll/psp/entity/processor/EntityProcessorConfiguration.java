package com.intuit.sbd.payroll.psp.entity.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EntityProcessorConfiguration {

    private Map<Class<?>, List<EntityProcessor>> registeredProcessorMap;

    private List<EntityProcessor> entityProcessors;

    @Autowired
    public EntityProcessorConfiguration(List<EntityProcessor> entityProcessors) {
        this.entityProcessors = entityProcessors;
        registeredProcessorMap = new HashMap<>();
        registerEntityProcessors();
    }

    private void registerEntityProcessors() {
        for (EntityProcessor entityProcessor : entityProcessors) {
            register(entityProcessor);
        }

    }

    private void register(EntityProcessor entityProcessor) {
        List<Class> interestedClasses = entityProcessor.getInterestedEntities();
        for (Class entityClass : interestedClasses) {
            addToRegisteredProcessorMap(entityClass, entityProcessor);
        }
    }

    private void addToRegisteredProcessorMap(Class entityClass, EntityProcessor entityProcessor) {
        if (!registeredProcessorMap.containsKey(entityClass)) {
            registeredProcessorMap.put(entityClass, new ArrayList<>());
        }
        registeredProcessorMap.get(entityClass).add(entityProcessor);
    }

    public List<EntityProcessor> getEntityProcessors(Class entityClass) {
        if (registeredProcessorMap.containsKey(entityClass)) {
            return registeredProcessorMap.get(entityClass);
        }
        return null;
    }

    public boolean hasRegisteredProcessors(Class entityClass){
        if (registeredProcessorMap.containsKey(entityClass)){
            return !registeredProcessorMap.get(entityClass).isEmpty();
        }
        return false;
    }

}
