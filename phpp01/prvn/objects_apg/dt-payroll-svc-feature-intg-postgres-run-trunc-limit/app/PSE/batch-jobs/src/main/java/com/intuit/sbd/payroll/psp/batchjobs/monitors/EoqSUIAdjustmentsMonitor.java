package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EoqSUIAdjustmentsProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class EoqSUIAdjustmentsMonitor extends BatchJobProcessorMonitor {
    public EoqSUIAdjustmentsMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.EoqSUIAdjustments,
                EoqSUIAdjustmentsProcessor.LiabilityAdjustmentsCleanupStep.class);
    }
}