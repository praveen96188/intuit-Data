package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.GemsGeneralLedgerProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:38:53 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "GemsGeneralLedgerUploadMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class GemsGeneralLedgerUploadMonitor extends JSSBatchJobMonitor {
  
    public GemsGeneralLedgerUploadMonitor(String[] pArguments) {
		super(pArguments);
	}

	public GemsGeneralLedgerUploadMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return  BatchJobType.GemsGeneralLedgerUpload;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return  GemsGeneralLedgerProcessor.ArchiveGemsGeneralLedgerFile.class;
	}
}
