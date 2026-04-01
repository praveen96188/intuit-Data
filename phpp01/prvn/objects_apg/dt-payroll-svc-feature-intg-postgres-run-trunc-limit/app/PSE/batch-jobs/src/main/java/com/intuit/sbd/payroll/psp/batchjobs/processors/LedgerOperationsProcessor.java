package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.ledgeroperations.ProcessLedgerOperations;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * User: dweinberg
 * Date: 11/9/12
 * Time: 2:04 PM
 */
public class LedgerOperationsProcessor extends BatchJobProcessor {
    public LedgerOperationsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    public void execute() {
        logger.info("Starting ledger operations batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessQueuedLedgerOperationJobs());

        logger.info("Completed ledger operations batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessQueuedLedgerOperationJobs extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.LedgerOperationsBatchJob);
                new ProcessLedgerOperations().process();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessQueuedLedgerOperationJobs ", t);
            }
        }
    }
}
