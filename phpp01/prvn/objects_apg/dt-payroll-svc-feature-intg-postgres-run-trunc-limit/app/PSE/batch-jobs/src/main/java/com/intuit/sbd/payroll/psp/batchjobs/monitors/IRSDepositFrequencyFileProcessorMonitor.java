package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.IRSDepositFrequencyFileProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/30/12
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class IRSDepositFrequencyFileProcessorMonitor extends BatchJobProcessorMonitor {
    public IRSDepositFrequencyFileProcessorMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.IRSDepositFrequencyFileProcessor,
                IRSDepositFrequencyFileProcessor.ProcessDepositFrequencyFile.class);
    }
}
