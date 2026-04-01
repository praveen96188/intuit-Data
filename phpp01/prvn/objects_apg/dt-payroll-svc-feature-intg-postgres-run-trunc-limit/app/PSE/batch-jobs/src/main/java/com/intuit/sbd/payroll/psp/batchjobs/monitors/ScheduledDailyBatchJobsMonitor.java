package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 17, 2009
 * Time: 1:17:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledDailyBatchJobsMonitor extends BatchJobProcessorMonitor {
    public ScheduledDailyBatchJobsMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.ScheduledDailyBatchJobs,
              DailyBatchJobsProcessor.NotifyAchOffloadStarted.class);
    }

    public void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        super.execute();
    }
}
