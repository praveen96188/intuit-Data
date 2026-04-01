package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHEnrollments.ACHEnrollmentManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * User: ihannur
 * Date: 2/12/13
 * Time: 11:01 AM
 */
public class ACHEnrollmentResponseProcessor extends BatchJobProcessor {

    /**
     * @param pBatchJobType          - job type to be executed
     * @param pJobId
     * @param pJobInstanceParameters - parameters for a given execution
     */
    public ACHEnrollmentResponseProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessResponseFilesStep());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public class ProcessResponseFilesStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHEnrollmentResponseBatchJob);

                ACHEnrollmentManager.processResponseFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessResponseFilesStep - ACHEnrollmentResponse", t);
            }
        }
    }

}
