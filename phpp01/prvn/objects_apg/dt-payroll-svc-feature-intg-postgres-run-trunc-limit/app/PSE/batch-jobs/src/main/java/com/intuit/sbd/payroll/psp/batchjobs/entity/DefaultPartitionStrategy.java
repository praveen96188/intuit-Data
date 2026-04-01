package com.intuit.sbd.payroll.psp.batchjobs.entity;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultPartitionStrategy<T> implements PartitionStrategy<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPartitionStrategy.class);
    private int chunkSize;

    public DefaultPartitionStrategy(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public List<List<T>> partition(List<T> entityList) {
        LOGGER.info("job=initial_load,action=partition_list_started");
        return ListUtils.partition(entityList, chunkSize);
    }
}