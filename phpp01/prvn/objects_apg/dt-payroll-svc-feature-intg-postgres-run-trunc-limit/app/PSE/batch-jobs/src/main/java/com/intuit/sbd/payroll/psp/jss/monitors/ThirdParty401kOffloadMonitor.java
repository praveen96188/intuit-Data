package com.intuit.sbd.payroll.psp.jss.monitors;


import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.ThirdParty401kOffloadProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: Jeff Jones
 */
@ScheduledJob(name = "ThirdParty401kOffloadMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class ThirdParty401kOffloadMonitor extends JSSBatchJobMonitor {
    
    public ThirdParty401kOffloadMonitor(String[] pArguments) {
		super(pArguments);
	}

	public ThirdParty401kOffloadMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return BatchJobType.ThirdParty401kOffload;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return  ThirdParty401kOffloadProcessor.Archive401kFiles.class;
	}
	
	@Override
    public void execute() throws Exception{
        if (BatchUtils.isWeekendOrHoliday()) {
        	getLogger().warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        super.execute();
    }
}
