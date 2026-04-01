package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 21, 2010
 * Time: 1:20:15 PM
 */
public class EntitlementProcessorMonitor extends BatchJobProcessorMonitor {
    public EntitlementProcessorMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.EntitlementProcessor,
              EntitlementProcessor.EntitlementDisableStep.class);

        setWarnOnMultipleAuditEntries(false);
    }
}
