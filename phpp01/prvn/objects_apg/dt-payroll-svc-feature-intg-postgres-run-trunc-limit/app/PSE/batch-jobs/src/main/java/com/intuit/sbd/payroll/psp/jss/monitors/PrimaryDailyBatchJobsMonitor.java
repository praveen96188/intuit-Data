package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.PrimaryDailyBatchJobsProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 4/27/17
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "PrimaryDailyBatchJobsMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class PrimaryDailyBatchJobsMonitor extends JSSBatchJobMonitor {
    public PrimaryDailyBatchJobsMonitor(String[] pArguments) {
        super(pArguments);
    }

    public PrimaryDailyBatchJobsMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.PrimaryDailyBatchJobs;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return PrimaryDailyBatchJobsProcessor.NotifyAchOffloadStarted.class;
    }

    public void execute() throws Exception {
        if (BatchUtils.isWeekendOrHoliday()) {
            getLogger().warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }
        super.execute();
    }
}
