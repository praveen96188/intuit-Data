package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.jss.processors.FsetResponseProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: ihannur
 * Date: 9/12/12
 * Time: 4:33 PM
 */


@ScheduledJob(name = "FsetResponseMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class,singleton = false)
public class FsetResponseMonitor extends JSSBatchJobMonitor {
 
    
    public FsetResponseMonitor(String[] pArguments) {
    	super(pArguments);
	}
	public FsetResponseMonitor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}
	
	@Override
	public BatchJobType getBatchJobToMonitor() {
	    return BatchJobType.FsetResponseProcessor;
	}
	@Override
	public Class<?> getBatchJobActionToMonitor() {
	    return FsetResponseProcessor.ArchiveFsetFilesStep.class;
	}

    public void execute() throws Exception {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            getLogger().warn(getClass().getSimpleName() + " monitor skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
