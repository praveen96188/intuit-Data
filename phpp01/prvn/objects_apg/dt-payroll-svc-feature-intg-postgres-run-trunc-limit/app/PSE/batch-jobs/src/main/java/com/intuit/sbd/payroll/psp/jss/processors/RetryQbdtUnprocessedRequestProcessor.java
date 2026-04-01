package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.qbdtrequests.RetryUnprocessedQbdtRequests;
import com.intuit.sbd.payroll.psp.batchjobs.qbdtrequests.ResetQbdtFlags;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 4/25/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "QbdtUnprocessedRequestsRetry", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class RetryQbdtUnprocessedRequestProcessor extends JSSBatchJob {

    public RetryQbdtUnprocessedRequestProcessor(String[] pArguments) {
        super(pArguments);
    }

    public RetryQbdtUnprocessedRequestProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(ResetQbdtFlagsStep.class);
        executeStep(RetryUnprocessedQbdtRequestsStep.class);
        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    /**
     * PSP-13615
     * Added ResetQbdtFlagsStep to reset the QBDT Flags
     */
    public static class ResetQbdtFlagsStep extends JSSBatchJobStep<RetryQbdtUnprocessedRequestProcessor> {

        @Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RetryUnprocessedQbdtRqBatchJob));

                new ResetQbdtFlags().resetFlags();

            } catch (Throwable t) {
                getLogger().error("Exception in ResetQbdtFlagsStep : ", t);
                throw new RuntimeException("Exception in job step ResetQbdtFlagsStep ", t);
            }
        }
    }

    public static class RetryUnprocessedQbdtRequestsStep extends JSSBatchJobStep<RetryQbdtUnprocessedRequestProcessor> {

        @Override
        public void execute() {
            try {
                try {
                    PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RetryUnprocessedQbdtRqBatchJob));

                    PayrollServices.beginUnitOfWork();
                    new RetryUnprocessedQbdtRequests().retryUnprocessedRequests();
                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                getLogger().error("Exception in RetryUnprocessedQbdtRequestsStep : ", t);
                throw new RuntimeException("Exception in job step RetryUnprocessedQbdtRequestsStep ", t);
            }
        }
    }
}
