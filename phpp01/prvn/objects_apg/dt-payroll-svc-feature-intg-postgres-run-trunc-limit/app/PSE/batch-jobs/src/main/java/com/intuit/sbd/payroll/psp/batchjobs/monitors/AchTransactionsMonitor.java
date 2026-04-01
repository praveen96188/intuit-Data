package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.processors.NightlyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:37:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AchTransactionsMonitor extends BatchJobProcessorMonitor {
    public AchTransactionsMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.NightlyBatchJobs,
              NightlyBatchJobsProcessor.ProcessAchTransactions.class);
    }

    public void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        super.execute();
    }
}
