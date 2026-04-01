package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.GemsGeneralLedgerProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:38:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GemsGeneralLedgerUploadMonitor extends BatchJobProcessorMonitor {
    public GemsGeneralLedgerUploadMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.GemsGeneralLedgerUpload,
              GemsGeneralLedgerProcessor.ArchiveGemsGeneralLedgerFile.class);
    }
}
