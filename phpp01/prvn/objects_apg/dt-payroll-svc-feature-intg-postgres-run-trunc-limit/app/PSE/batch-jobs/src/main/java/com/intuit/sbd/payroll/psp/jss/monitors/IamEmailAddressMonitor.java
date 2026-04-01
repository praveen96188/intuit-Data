package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.IamEmailAddressProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

@ScheduledJob(name = "IamEmailAddressMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class IamEmailAddressMonitor extends JSSBatchJobMonitor {

	public IamEmailAddressMonitor(String[] pArguments) {
		super(pArguments);
	}

	public IamEmailAddressMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return BatchJobType.IamEmailAddressProcessor;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return IamEmailAddressProcessor.InsertEmailAddress.class;
	}

}
