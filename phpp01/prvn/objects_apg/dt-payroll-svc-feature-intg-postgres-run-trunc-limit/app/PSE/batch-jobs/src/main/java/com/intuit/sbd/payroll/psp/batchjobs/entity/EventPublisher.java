package com.intuit.sbd.payroll.psp.batchjobs.entity;

import com.intuit.sbd.payroll.psp.Application;
import java.util.List;

import static com.intuit.spc.foundations.primary.logging.SpcfLogManager.getLogger;

public interface EventPublisher<T> {

    default void publish() {
        getLogger().info("job=initial_load,action=publish_starting");
        List<T> entityList = getEntityList();
        if (entityList.isEmpty()) {
            getLogger().info("job=initial_load,action=publish_list_empty,received the empty entity list to be processed");
            return;
        }

        List<List<T>> entity = partition(entityList);
        for (List<T> chunk : entity) {
            publishChunk(chunk);
        }
        updateLastProcessedTime();
        getLogger().info("job=initial_load,action=publish_completed");
    }

    default void publishChunk(List<T> chunk) {
        try {
            Application.beginUnitOfWork();
            try {
                getLogger().info("job=initial_load,action=publish_chunk_started");
                publishBatch(chunk);
                handlePublishSuccess(chunk);
                getLogger().info("job=initial_load,action=publish_chunk_succeeded");
            } catch (Exception ex) {
                getLogger().error("job=initial_load,action=publish_chunk_failed,Failed to Publish the chunk. Exception=", ex);
                handlePublishChunkFailure(chunk);
            }
            Application.commitUnitOfWork();
            getLogger().info("job=initial_load,action=publish_chunk_completed");
        } catch (Exception e) {
            getLogger().error("job=initial_load,action=publish_chunk_exception,Failed to Publish the chunk. Exception=", e);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    void handlePublishChunkFailure(List<T> chunk);

    void handlePublishSuccess(List<T> chunk);

    List<T> getEntityList();

    List<List<T>> partition(List<T> entityList);

    void publishBatch(List<T> entity);

    void updateLastProcessedTime();
}
