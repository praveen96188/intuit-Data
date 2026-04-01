package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * User: rnorian
 * Date: 5/9/11
 * Time: 8:26 PM
 */
public class EftpsResponseProcessor extends BatchJobProcessor {
    public EftpsResponseProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessWaitingResponseFiles());
        executeStep(new TransmitAS400Files());
        executeStep(new ArchiveProcessedFiles());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessWaitingResponseFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.processWaitingResponseFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step processWaitingResponseFiles ", t);
            }
        }
    }

    public class TransmitAS400Files extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.transmitAS400Files();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransferAS400Files ", t);
            }
        }
    }

    public class ArchiveProcessedFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.archiveFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveProcessedFiles ", t);
            }
        }
    }
}
