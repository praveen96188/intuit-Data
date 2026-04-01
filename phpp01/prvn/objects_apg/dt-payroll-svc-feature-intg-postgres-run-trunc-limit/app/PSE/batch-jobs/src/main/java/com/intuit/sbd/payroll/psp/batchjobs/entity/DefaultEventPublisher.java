package com.intuit.sbd.payroll.psp.batchjobs.entity;

import java.util.List;

public class DefaultEventPublisher<T> implements EventPublisher<T> {
    private EntityFindStrategy entityFindStrategy;
    private PartitionStrategy partitionStrategy;
    private EventPublisherStrategy eventPublisherStrategy;
    private UpdateStatusStrategy updateStatusStrategy;

    public DefaultEventPublisher(EntityFindStrategy entityFindStrategy, UpdateStatusStrategy updateStatusStrategy,
                                 PartitionStrategy partitionStrategy, EventPublisherStrategy eventPublisherStrategy) {
        this.entityFindStrategy = entityFindStrategy;
        this.partitionStrategy = partitionStrategy;
        this.eventPublisherStrategy = eventPublisherStrategy;
        this.updateStatusStrategy = updateStatusStrategy;
    }

    @Override
    public List<T> getEntityList() {
        return entityFindStrategy.getEntityList();
    }

    @Override
    public List<List<T>> partition(List<T> entityList) {
        return partitionStrategy.partition(entityList);
    }

    @Override
    public void publishBatch(List<T> entity) {
        eventPublisherStrategy.publishBatch(entity);
    }

    @Override
    public void updateLastProcessedTime() {
        updateStatusStrategy.updateLastProcessedTime();
    }

    @Override
    public void handlePublishChunkFailure(List<T> chunk) {
        updateStatusStrategy.handlePublishChunkFailure(chunk);
    }

    @Override
    public void handlePublishSuccess(List<T> chunk) {
        updateStatusStrategy.handlePublishChunkSuccess(chunk);
    }

}