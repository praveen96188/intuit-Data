package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.NightlyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

@ScheduledJob(name = "NightlyBatchJobsMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class NightlyBatchJobsMonitor extends JSSBatchJobMonitor {

	public NightlyBatchJobsMonitor(String[] pArguments) {
		super(pArguments);
	}

	public NightlyBatchJobsMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return BatchJobType.NightlyBatchJobs;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return NightlyBatchJobsProcessor.DownloadAchReturnsFile.class;
	}

	@Override
	protected void execute() throws Exception {
		if (BatchUtils.isWeekendOrHoliday()) {
			getLogger().warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
			return;
		}

		super.execute();
	}

}
