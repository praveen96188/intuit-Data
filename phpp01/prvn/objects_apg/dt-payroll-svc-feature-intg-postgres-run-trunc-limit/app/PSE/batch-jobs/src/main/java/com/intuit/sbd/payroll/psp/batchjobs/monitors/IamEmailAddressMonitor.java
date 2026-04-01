package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.IamEmailAddressProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

public class IamEmailAddressMonitor extends BatchJobProcessorMonitor {

    public IamEmailAddressMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.IamEmailAddressProcessor,
              IamEmailAddressProcessor.InsertEmailAddress.class);
    }
}
