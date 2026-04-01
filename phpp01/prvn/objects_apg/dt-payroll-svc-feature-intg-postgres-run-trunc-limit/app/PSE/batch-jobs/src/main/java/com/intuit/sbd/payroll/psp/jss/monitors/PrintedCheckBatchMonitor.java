package com.intuit.sbd.payroll.psp.jss.monitors;


import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.PrintedCheckBatchProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 11, 2011
 * Time: 8:43:03 AM
 */
@ScheduledJob(name = "PrintedCheckBatchMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class PrintedCheckBatchMonitor extends JSSBatchJobMonitor {
    
	public PrintedCheckBatchMonitor(String[] pArguments) {
		super(pArguments);
	}

	public PrintedCheckBatchMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return BatchJobType.PrintedCheckBatch;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return PrintedCheckBatchProcessor.ArchiveFiles.class;
	}

}
