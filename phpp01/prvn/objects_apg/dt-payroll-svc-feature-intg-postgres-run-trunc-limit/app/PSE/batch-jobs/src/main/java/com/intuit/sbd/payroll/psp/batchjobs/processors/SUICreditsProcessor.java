package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.suicredits.ProcessSUICredits;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

public class SUICreditsProcessor extends BatchJobProcessor {

    public SUICreditsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Started SUICreditsProcessor Batch Job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(new ProcessSUICreditsStep());
        logger.info("Completed SUICreditsProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public class ProcessSUICreditsStep extends BatchJobProcessorStep {
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
