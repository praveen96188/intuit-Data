package com.intuit.sbd.payroll.psp.jss;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.util.TimeExpressionConverter;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.schedule.SpecialSchedule;
import com.intuit.sbg.shared.batchjob.utils.ScheduleGenerator;

/**
 * Generates schedule based on the BatchJobType from database
 * 
 * @author kmuthurangam
 *
 */
public class JSSScheduleGenerator extends ScheduleGenerator {

	@Override
	public ScheduleHolder generateSchedule(ScheduledJob scheduledJob) {
		ScheduleHolder scheduleHolder = new ScheduleHolder();
		BatchJobType batchJobType = BatchJobType.valueOf(scheduledJob.name());
		BatchJobSetup batchJobSetup = BatchJobManager.getBatchJobSetup(batchJobType);
		String schedule = TimeExpressionConverter.convertFluxToQuartz(batchJobSetup.getJobTimerExpression());
		/*
		 * If jobs are not auto scheduled, they are scheduled to run immediately from another batch jobs. So setting
		 * SpecialSchedule.NOW as the default schedule
		 */
		if (batchJobSetup.getIsAutomaticallyScheduled()) {
			scheduleHolder.setSchedule(schedule);
		} else {
			scheduleHolder.setSchedule(SpecialSchedule.NOW);
		}

		// Set Interval to Integer.MIN_VALUE to avoid setting the Interval related attributes in JSS
		scheduleHolder.setInterval(Integer.MIN_VALUE);
		return scheduleHolder;
	}

}
