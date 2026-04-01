package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.TaxCreditsEchoSignSyncProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * User: dweinberg
 * Date: Sep 28, 2010
 * Time: 10:16:27 AM
 */
public class TaxCreditsEchoSignSyncMonitor extends BatchJobProcessorMonitor {
    public TaxCreditsEchoSignSyncMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.TaxCreditsEchoSign,
                TaxCreditsEchoSignSyncProcessor.ProcessEchoSignSyncs.class);

        setWarnOnMultipleAuditEntries(false);
    }
}
