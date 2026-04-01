package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.AnnualBilling.AnnualBilling;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/28/12
 * Time: 4:33 PM
 */
public class AnnualBillingProcessor extends BatchJobProcessor {

    public AnnualBillingProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.AnnualBillingBatchJob);

        executeStep(new AnnualBillingStep());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class AnnualBillingStep extends BatchJobProcessorStep {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.AnnualBillingBatchJob);
            StopWatch sw = StopWatch.startTimer();
            String commandLine = getJobInstanceParameters().trim();

            String[] args = new String[1];
            if (getRunMode().equals(BatchJobProcessor.RunMode.UsingFlux)) {
                args[0] = String.valueOf(PSPDate.getPSPTime().getYear() - 1);
            } else {
                args = commandLine.split(" ");
            }

            AnnualBilling w2Billing = new AnnualBilling();
            w2Billing.main(args);

            logger.info("completed generating annual billing - in "+ sw.getElapsedTimeString());

        }
    }
}
