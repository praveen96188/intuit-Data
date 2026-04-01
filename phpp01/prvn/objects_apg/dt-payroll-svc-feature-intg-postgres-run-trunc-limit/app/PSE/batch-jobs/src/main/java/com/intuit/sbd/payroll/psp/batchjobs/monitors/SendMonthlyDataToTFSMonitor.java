package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EoqSUIAdjustmentsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.SendMonthlyDataToTFSProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS.SendMonthlyDataToTFS;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class SendMonthlyDataToTFSMonitor extends BatchJobProcessorMonitor {
    public SendMonthlyDataToTFSMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.SendMonthlyDataToTFSProcessor,
                SendMonthlyDataToTFSProcessor.SendMonthlyDataToTFSStep.class);
    }
}