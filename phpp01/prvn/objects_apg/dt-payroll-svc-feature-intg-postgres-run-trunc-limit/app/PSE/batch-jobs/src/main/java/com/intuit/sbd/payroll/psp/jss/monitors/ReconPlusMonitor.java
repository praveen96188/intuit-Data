package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.ReconPlusProcessor;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;


/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 18, 2011
 * Time: 2:20:51 PM
 */
@ScheduledJob(name = "ReconPlusMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class ReconPlusMonitor extends JSSBatchJobMonitor {   

    public ReconPlusMonitor(String[] pArguments) {
		super(pArguments);
	}

	public ReconPlusMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public BatchJobType getBatchJobToMonitor() {
		return BatchJobType.ReconPlus;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return ReconPlusProcessor.ArchiveReconPlusFiles.class;
	}

	@Override
    public void execute() throws Exception {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
        	getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
