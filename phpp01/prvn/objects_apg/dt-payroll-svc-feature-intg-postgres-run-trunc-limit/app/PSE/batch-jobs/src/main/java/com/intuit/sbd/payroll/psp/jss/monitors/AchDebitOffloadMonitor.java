package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.AchDebitOffloadProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
@ScheduledJob(name = "AchDebitOffloadMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class AchDebitOffloadMonitor extends JSSBatchJobMonitor {
    public AchDebitOffloadMonitor(String[] pArguments) {
        super(pArguments);
    }

    public AchDebitOffloadMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.AchDebitOffload;
    }
    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return AchDebitOffloadProcessor.OffloadATFFinalizedPaymentsStep.class;
    }
}