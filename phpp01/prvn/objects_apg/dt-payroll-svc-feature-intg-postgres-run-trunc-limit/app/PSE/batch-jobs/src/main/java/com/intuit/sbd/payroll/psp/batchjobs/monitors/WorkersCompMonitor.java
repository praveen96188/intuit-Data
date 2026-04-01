package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.WorkersCompProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * User: michaelp696
 */
public class WorkersCompMonitor extends BatchJobProcessorMonitor {

    public WorkersCompMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.WorkersCompProcessor,
              WorkersCompProcessor.PushPayrollData.class);
    }
}
