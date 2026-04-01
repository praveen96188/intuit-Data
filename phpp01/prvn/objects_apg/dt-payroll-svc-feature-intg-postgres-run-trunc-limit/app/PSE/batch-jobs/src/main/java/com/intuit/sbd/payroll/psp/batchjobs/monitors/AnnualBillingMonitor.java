package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AnnualBillingProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 10/11/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnualBillingMonitor extends BatchJobProcessorMonitor {

    public AnnualBillingMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.AnnualBillingProcessor,
              AnnualBillingProcessor.AnnualBillingStep.class);

        setWarnOnMultipleAuditEntries(false);
    }

}
