package com.intuit.sbd.payroll.psp.jss.monitors;


import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.StateReportProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * @author jesseanderson  
 */
@ScheduledJob(name = "StateReportMonitor",  resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class StateReportMonitor extends JSSBatchJobMonitor {   

    public StateReportMonitor(String[] pArguments) {
		super(pArguments);
	}

	public StateReportMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return BatchJobType.StateReport;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return StateReportProcessor.CreateStateReportFiles.class;
	}
	
	@Override
    public void execute() throws Exception {
        super.execute();
    }
}
