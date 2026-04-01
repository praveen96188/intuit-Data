package com.intuit.sbd.payroll.psp.batchjobs.reports;


import java.util.List;

/**
 * @author kmuthurangam
 * <p>
 * RecordEnricher enriches the provided record and it supports two modes
 * <ul>
 *     <li>Single Mode</li>
 *     <li>Batch Mode</li>
 * </ul>
 * </p>
 */
public interface RecordEnricher<T> {

    default void enrichRecord(T type) {
        // Empty implementation to avoid unnecessary override by implementation classes
    }

    default void enrichRecords(List<T> type) {
        // Empty implementation to avoid unnecessary override by implementation classes
    }

}
