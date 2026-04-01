package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.WorkersCompProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: michaelp696
 *
 * Migrated To JSS by: nloharuka
 * Date: 5/04/17
 * PSP-13042
 */

@ScheduledJob(name = "WorkersCompMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class WorkersCompMonitor extends JSSBatchJobMonitor {

    public WorkersCompMonitor(String[] pArguments) {
        super(pArguments);
    }
    public WorkersCompMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.WorkersCompProcessor;
    }
    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return WorkersCompProcessor.PushPayrollData.class;
    }

}
