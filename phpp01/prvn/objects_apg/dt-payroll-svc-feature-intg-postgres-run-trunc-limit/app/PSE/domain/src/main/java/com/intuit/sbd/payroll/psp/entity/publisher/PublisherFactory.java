package com.intuit.sbd.payroll.psp.entity.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PublisherFactory {
    private Map<Class<?>, EntityPublisher> entityPublisherMap;

    private List<EntityPublisher> publishers;

    @Autowired
    public PublisherFactory(List<EntityPublisher> publishers) {
        this.publishers = publishers;
        entityPublisherMap = new HashMap<>();
        register();
    }

    private void register() {
        for (EntityPublisher publisher : publishers) {
            entityPublisherMap.put(publisher.getEntityClass(), publisher);
        }
        log.info("entityPublisherMap Registered, keySet={}", entityPublisherMap.keySet());
    }

    public EntityPublisher getPublisher(Class entityClass) {
        if (!entityPublisherMap.containsKey(entityClass)) {
            throw new UnsupportedOperationException("No Publisher found for " + entityClass.getCanonicalName());
        }
        return entityPublisherMap.get(entityClass);

    }
}
