package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 24, 2011
 * Time: 11:14:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class EdiResponseProcessor extends BatchJobProcessor {
    public EdiResponseProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
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

        executeStep(new PreProcessWaitingResponseFiles());
        executeStep(new ProcessWaitingResponseFiles());
        executeStep(new TransmitAS400Files());
        executeStep(new ArchiveProcessedFiles());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessWaitingResponseFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.processWaitingEDIResponseFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step processWaitingResponseFiles ", t);
            }
        }
    }

    public class PreProcessWaitingResponseFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.preProcessWaitingEDIResponseFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step preProcessWaitingResponseFiles ", t);
            }
        }
    }

    public class TransmitAS400Files extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.transmitAS400StateEDIFiles(); 
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransferAS400Files ", t);
            }
        }
    }

    public class ArchiveProcessedFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.archiveEDIFiles();

                //Clean AS400 files from AS400 directory, if any files are left after processing.
                EftpsUtil.cleanDirectory(EftpsUtil.getEdiAS400Dir());

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveProcessedFiles ", t);
            }
        }
    }

}
