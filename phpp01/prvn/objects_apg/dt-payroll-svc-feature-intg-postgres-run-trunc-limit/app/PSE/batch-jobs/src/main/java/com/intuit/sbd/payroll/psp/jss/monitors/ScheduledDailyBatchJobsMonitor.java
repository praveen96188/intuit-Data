package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 17, 2009
 * Time: 1:17:51 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "ScheduledDailyBatchJobsMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class ScheduledDailyBatchJobsMonitor extends JSSBatchJobMonitor {
    public ScheduledDailyBatchJobsMonitor(String[] pArguments) {
        super(pArguments);
    }

    public ScheduledDailyBatchJobsMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.ScheduledDailyBatchJobs;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return DailyBatchJobsProcessor.NotifyAchOffloadStarted.class;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void execute() throws Exception {
        if (BatchUtils.isWeekendOrHoliday()) {
            getLogger().warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }
        super.execute();
    }
}
