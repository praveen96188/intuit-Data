package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.processors.LedgerBalanceProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Sep 22, 2008
 * Time: 11:10:01 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "LedgerBalanceMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class LedgerBalanceMonitor extends JSSBatchJobMonitor {
	
	public LedgerBalanceMonitor(String[] pArguments) {
        super(pArguments);
    }

    public LedgerBalanceMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.LedgerBalance;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return LedgerBalanceProcessor.UpdateLedgerBalance.class;
    }
}