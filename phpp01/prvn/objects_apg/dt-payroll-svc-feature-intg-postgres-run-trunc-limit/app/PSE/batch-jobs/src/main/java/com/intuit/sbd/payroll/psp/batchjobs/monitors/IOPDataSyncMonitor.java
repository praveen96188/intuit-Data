package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.IopSyncProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Mar 17, 2011
 * Time: 3:50:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class IOPDataSyncMonitor extends BatchJobProcessorMonitor {
    public IOPDataSyncMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.IOPDataSync,
              IopSyncProcessor.IopSync.class);

        setWarnOnMultipleAuditEntries(false);
    }
}
