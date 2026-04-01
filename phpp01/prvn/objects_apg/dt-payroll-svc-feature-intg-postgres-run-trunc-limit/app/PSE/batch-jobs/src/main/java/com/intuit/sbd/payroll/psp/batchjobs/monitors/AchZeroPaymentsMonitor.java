package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AchZeroPaymentsProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class AchZeroPaymentsMonitor extends BatchJobProcessorMonitor {
    public AchZeroPaymentsMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.AchZeroPayments,
              AchZeroPaymentsProcessor.ProcessZeroPaymentsStep.class);
    }
}