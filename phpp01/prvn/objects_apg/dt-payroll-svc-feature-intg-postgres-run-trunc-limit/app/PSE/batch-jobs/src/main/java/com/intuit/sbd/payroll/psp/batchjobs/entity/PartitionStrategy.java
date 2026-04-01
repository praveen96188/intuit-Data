package com.intuit.sbd.payroll.psp.batchjobs.entity;

import java.util.List;

public interface PartitionStrategy<T> {
    List<List<T>> partition(List<T> entityList);
}