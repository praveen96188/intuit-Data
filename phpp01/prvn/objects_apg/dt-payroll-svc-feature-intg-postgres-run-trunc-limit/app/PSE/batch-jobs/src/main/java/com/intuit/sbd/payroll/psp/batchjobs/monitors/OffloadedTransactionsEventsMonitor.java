package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.OffloadedTransactionsEventsProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Sep 22, 2008
 * Time: 11:10:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class OffloadedTransactionsEventsMonitor extends BatchJobProcessorMonitor {
    public OffloadedTransactionsEventsMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.OffloadedTransactionsEvents,
              OffloadedTransactionsEventsProcessor.CreateTransactionOffloadedEvents.class);
    }
}