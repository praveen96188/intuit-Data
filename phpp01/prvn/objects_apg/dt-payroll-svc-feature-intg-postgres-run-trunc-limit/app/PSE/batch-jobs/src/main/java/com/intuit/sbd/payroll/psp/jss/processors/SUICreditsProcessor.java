package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.suicredits.ProcessSUICredits;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

@ScheduledJob(name = "SUICreditsBatchJob", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class SUICreditsProcessor extends JSSBatchJob {

    public SUICreditsProcessor(String[] pArguments) {
        super(pArguments);
    }
    public SUICreditsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Started SUICreditsProcessor Batch Job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(ProcessSUICreditsStep.class);
        getLogger().info("Completed SUICreditsProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class ProcessSUICreditsStep extends JSSBatchJobStep<SUICreditsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.SUICreditsBatchJob);
                new ProcessSUICredits().process();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessSUICreditsStep ", t);
            }
        }
    }
}
