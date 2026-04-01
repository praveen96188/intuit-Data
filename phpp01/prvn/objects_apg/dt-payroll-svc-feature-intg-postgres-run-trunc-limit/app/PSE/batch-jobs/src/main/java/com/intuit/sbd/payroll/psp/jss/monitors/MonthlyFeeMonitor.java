package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.MonthlyFeeProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/6/12
 * Time: 7:07 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "MonthlyFeeMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class MonthlyFeeMonitor extends JSSBatchJobMonitor {
  
	public MonthlyFeeMonitor(String[] pArguments) {
        super(pArguments);
	}
	public MonthlyFeeMonitor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.MonthlyFee;
    }
    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return MonthlyFeeProcessor.ProcessMonthlyOfferingFees.class;
    }
}
