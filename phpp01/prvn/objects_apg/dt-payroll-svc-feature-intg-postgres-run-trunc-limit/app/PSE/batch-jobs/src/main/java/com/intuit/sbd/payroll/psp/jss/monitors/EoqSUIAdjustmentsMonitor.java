package com.intuit.sbd.payroll.psp.jss.monitors;


import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.EoqSUIAdjustmentsProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
@ScheduledJob(name = "EoqSUIAdjustmentsMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EoqSUIAdjustmentsMonitor extends JSSBatchJobMonitor {
   
    public EoqSUIAdjustmentsMonitor(String[] pArguments) {
		super(pArguments);
	}

	public EoqSUIAdjustmentsMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return  BatchJobType.EoqSUIAdjustments;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return EoqSUIAdjustmentsProcessor.LiabilityAdjustmentsCleanupStep.class;
	}
}