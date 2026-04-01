package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.amo.ProcessNewAMOMessages;
import com.intuit.sbd.payroll.psp.batchjobs.amo.ProcessSavedAMOMessages;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 3:45:00 PM
 */
public class AMOMessageProcessor extends BatchJobProcessor {
    public AMOMessageProcessor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    public void execute() {
        logger.info("Starting amo message batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessSavedAMOMessagesStep());
        executeStep(new ProcessNewAMOMessagesStep());

        logger.info("Completed amo message batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessSavedAMOMessagesStep extends BatchJobProcessor.BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AMOBatchJob));
                new ProcessSavedAMOMessages().processSavedAMOMessages();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessSavedAMOMessages ", t);
            }  finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class ProcessNewAMOMessagesStep extends BatchJobProcessor.BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AMOBatchJob));
                new ProcessNewAMOMessages().processNewAMOMessages();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessNewAMOMessages ", t);
            }
        }
    }
}
