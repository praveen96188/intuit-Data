package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.jss.processors.FsetFilingProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: ihannur
 * Date: 9/12/12
 * Time: 4:32 PM
 */
@ScheduledJob(name = "FsetFilingMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class,singleton = false)
public class FsetFilingMonitor extends JSSBatchJobMonitor {

    
    public FsetFilingMonitor(String[] pArguments) {
        	super(pArguments);
	}
	public FsetFilingMonitor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
	    return BatchJobType.FsetFilingProcessor;
	}
	@Override
	public Class<?> getBatchJobActionToMonitor() {
	    return FsetFilingProcessor.ArchiveFsetFilesStep.class;
	}
    public void execute() throws Exception {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
        	getLogger().warn(getClass().getSimpleName() + " monitor skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
