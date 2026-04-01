package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.GemsAccountsReceivableProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:38:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class GemsAccountsReceivableMonitor extends BatchJobProcessorMonitor {
    public GemsAccountsReceivableMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.GemsAccountsReceivable,
              GemsAccountsReceivableProcessor.ArchiveGemsAccountsReceivableFile.class);
    }
}
