package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AMOMessageProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 11, 2010
 * Time: 8:01:03 AM
 */
public class AMOMessageProcessorMonitor extends BatchJobProcessorMonitor {
    public AMOMessageProcessorMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.AMOMessageProcessor,
              AMOMessageProcessor.ProcessNewAMOMessagesStep.class);

        setWarnOnMultipleAuditEntries(false);
    }
}
