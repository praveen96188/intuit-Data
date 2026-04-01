package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.AchZeroPaymentsProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
@ScheduledJob(name = "AchZeroPaymentsMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class AchZeroPaymentsMonitor extends JSSBatchJobMonitor {
   
    public AchZeroPaymentsMonitor(String[] pArguments) {
        super(pArguments);
    }

    public AchZeroPaymentsMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.AchZeroPayments;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return AchZeroPaymentsProcessor.ProcessZeroPaymentsStep.class;
    }
}