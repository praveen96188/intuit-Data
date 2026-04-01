package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PrintedCheckBatchProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 11, 2011
 * Time: 8:43:03 AM
 */
public class PrintedCheckBatchMonitor extends BatchJobProcessorMonitor {
    public PrintedCheckBatchMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.PrintedCheckBatch,
              PrintedCheckBatchProcessor.ArchiveFiles.class);
    }
}
