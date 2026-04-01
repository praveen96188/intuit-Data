package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AchDebitOffloadProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class AchDebitOffloadMonitor extends BatchJobProcessorMonitor {
    public AchDebitOffloadMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.AchDebitOffload,
              AchDebitOffloadProcessor.OffloadATFFinalizedPaymentsStep.class);
    }
}