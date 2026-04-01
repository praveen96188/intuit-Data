package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.qbdtrequests.RetryUnprocessedQbdtRequests;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

public class RetryQbdtUnprocessedRequestProcessor extends BatchJobProcessor {

    public RetryQbdtUnprocessedRequestProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(new RetryUnprocessedQbdtRequestsStep());
        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class RetryUnprocessedQbdtRequestsStep extends BatchJobProcessorStep {

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
                logger.error(t);
                throw new RuntimeException("Exception in job step RetryUnprocessedQbdtRequestsStep ", t);
            }
        }
    }
}
