package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.taxcredits.SyncEchoSignDocuments;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * User: dweinberg
 * Date: Sep 28, 2010
 * Time: 10:10:10 AM
 */
public class TaxCreditsEchoSignSyncProcessor extends BatchJobProcessor {

    public TaxCreditsEchoSignSyncProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(new ProcessEchoSignSyncs());
        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessEchoSignSyncs extends BatchJobProcessorStep {

        @Override
        public void execute() {
            try {
                try {
                    PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));

                    PayrollServices.beginUnitOfWork();
                    new SyncEchoSignDocuments().sync();                    
                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                logger.error(t);
                throw new RuntimeException("Exception in job step ProcessEchoSignSyncs ", t);
            }
        }
    }
}
