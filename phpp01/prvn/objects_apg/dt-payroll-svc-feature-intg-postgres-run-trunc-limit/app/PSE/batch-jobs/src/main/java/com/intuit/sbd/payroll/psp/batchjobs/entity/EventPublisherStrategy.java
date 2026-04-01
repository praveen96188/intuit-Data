package com.intuit.sbd.payroll.psp.batchjobs.entity;

import java.util.List;

public interface EventPublisherStrategy<T> {
    void publishBatch(List<T> entity);
}