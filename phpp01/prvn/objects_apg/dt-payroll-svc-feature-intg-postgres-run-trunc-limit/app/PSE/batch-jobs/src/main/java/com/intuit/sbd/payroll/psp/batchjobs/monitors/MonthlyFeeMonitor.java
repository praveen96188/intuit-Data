package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.MonthlyFeeProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/6/12
 * Time: 7:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class MonthlyFeeMonitor extends BatchJobProcessorMonitor {
    public MonthlyFeeMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.MonthlyFee,
              MonthlyFeeProcessor.ProcessMonthlyOfferingFees.class);
    }
}
