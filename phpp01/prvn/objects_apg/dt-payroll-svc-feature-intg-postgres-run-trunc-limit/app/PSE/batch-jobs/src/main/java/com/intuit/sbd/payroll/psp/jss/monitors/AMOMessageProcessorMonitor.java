package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.AMOMessageProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 11, 2010
 * Time: 8:01:03 AM
 */
@ScheduledJob(name = "AMOMessageProcessorMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class AMOMessageProcessorMonitor extends JSSBatchJobMonitor {
	public AMOMessageProcessorMonitor(String[] pArguments) {
			super(pArguments);
            setWarnOnMultipleAuditEntries(false);
        }
 	public AMOMessageProcessorMonitor(String[] pArguments, String pJobId) {
            super(pArguments, pJobId);
            setWarnOnMultipleAuditEntries(false);
            }

	@Override
	public BatchJobType getBatchJobToMonitor() {
		// TODO Auto-generated method stub
		return BatchJobType.AMOMessageProcessor;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		// TODO Auto-generated method stub
		 return  AMOMessageProcessor.ProcessNewAMOMessagesStep.class;
	}


}
