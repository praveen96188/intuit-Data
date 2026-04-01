package com.intuit.sbd.payroll.psp.batchjobs.entity;

import java.util.ArrayList;
import java.util.List;

public interface UpdateStatusStrategy<T> {

    default void updateLastProcessedTime() {
    }

    default void handlePublishChunkFailure(List<T> chunk) {
    }

    default void handlePublishChunkSuccess(List<T> chunk) {
    }

    default String getPsIdsFromChunk(List<T> chunk) {
        ArrayList<String> psIds = new ArrayList<>();
        chunk.stream().forEach(entry -> psIds.add(((Object[]) entry)[0].toString()));
        return psIds.toString();
    }
}