package com.intuit.sbd.payroll.psp.jss;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagUtil;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.utils.AutoScheduleGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Set Auto Schedule based on the BatchJobType from database
 * 
 * @author kmuthurangam
 *
 */
@Slf4j
public class JSSAutoScheduleGenerator extends AutoScheduleGenerator {

	@Override
	public boolean autoSchedule(ScheduledJob scheduledJob) {
		BatchJobType batchJobType = BatchJobType.valueOf(scheduledJob.name());
		BatchJobSetup batchJobSetup = BatchJobManager.getBatchJobSetup(batchJobType);

		if(Application.isParallelEnv()) {
			Set<String> parallelEnvScheduledJobList = FeatureFlagUtil.getFeatureFlagStringSet(FeatureFlags.Key.PARALLEL_ENV_JSS_SCHEDULED_JOB_LIST);
			log.info("Parallel Env Job autoSchedule scheduledJob={} isAutomaticallyScheduled={} parallelEnvScheduledJobList={}", scheduledJob.name(), batchJobSetup.getIsAutomaticallyScheduled(), parallelEnvScheduledJobList);
			if(parallelEnvScheduledJobList.contains(scheduledJob.name()) && batchJobSetup.getIsAutomaticallyScheduled()) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}

		return batchJobSetup.getIsAutomaticallyScheduled();
	}

}
