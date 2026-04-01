package com.intuit.sbd.payroll.psp.jss.monitors.JPMCDDScreening;

import com.intuit.sbd.payroll.psp.jss.processors.JPMCDDScreening.OFACReportProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by suganyas315 on 7/27/15.
 *
 */
@ScheduledJob(name = "OFACReportMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class OFACReportMonitor extends JSSBatchJobMonitor {
    
    public OFACReportMonitor(String[] pArguments) {
    	        super(pArguments);
    }
    public OFACReportMonitor(String[] pArguments, String pJobId) {
    		        super(pArguments, pJobId);
    }		
    		
	@Override
    public BatchJobType getBatchJobToMonitor() {
    	return BatchJobType.OFACReportProcessor;
    }
    @Override
    public Class<?> getBatchJobActionToMonitor() {
    	return OFACReportProcessor.ArchiveFileStep.class;
    }
     
    @Override
    public void execute() throws Exception {
        if (BatchUtils.isWeekendOrHoliday()) {
        	getLogger().warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

			super.execute();
    }
}
