package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kValidationProcess;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;

/**
 * User: Jeff Jones
 * Flux Workflow to validate 401k data and send emails when it finds problems.
 */
public class ThirdParty401kValidationProcessor extends BatchJobProcessor{

    public ThirdParty401kValidationProcessor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }
        logger.info("Starting third party 401k validation batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new Validate401kData());

        logger.info("Completed third party 401k validation batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class Validate401kData extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new ThirdParty401kValidationProcess().validate401kData();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step Validate401kData ", t);
            }
        }
    }
}
