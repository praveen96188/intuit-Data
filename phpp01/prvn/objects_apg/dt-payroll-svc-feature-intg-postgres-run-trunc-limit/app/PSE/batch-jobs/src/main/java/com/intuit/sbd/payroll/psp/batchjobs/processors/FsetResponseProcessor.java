package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.fset.FsetManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpFsetConnection;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.FsetFile;
import com.intuit.sbd.payroll.psp.domain.FsetFileStatus;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * User: ihannur
 * Date: 9/12/12
 * Time: 4:35 PM
 */
public class FsetResponseProcessor extends BatchJobProcessor {

    public FsetResponseProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        logger.info("Starting FSET Filing job");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);

        executeStep(new CheckForResponseFilesStep());
        executeStep(new ProcessFsetResponseFileStep());
        executeStep(new ArchiveFsetFilesStep());

        logger.info("Completed FSET Filing process batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class CheckForResponseFilesStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);

                SftpFsetConnection sftpFsetConnection = new SftpFsetConnection();
                sftpFsetConnection.downloadFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CheckForResponseFilesStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class ProcessFsetResponseFileStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);
                PayrollServices.beginUnitOfWork();
                DomainEntitySet<FsetFile> fsetFiles = Application.find(FsetFile.class, FsetFile.StatusCd().equalTo(FsetFileStatus.ReceivedByAgency));
                new FsetManager().processResponseFile(fsetFiles);
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessFsetResponseFileStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class ArchiveFsetFilesStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);
                new FsetManager().archiveFsetFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveFsetFilesStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }
}
