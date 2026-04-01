package com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.ReportType;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.TPSUReport;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpJPMCDDScreeningProcessFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charithah418 on 9/2/2016.
 */
public class TPSUReportProcessor extends BatchJobProcessor {

        private SpcfCalendar mProcessingDate;
        private boolean mValidProcessingDate = false;
        private List<String> mReportFileNames;

        public TPSUReportProcessor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
            super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        }

        @Override
        protected void execute() {
            if (mValidProcessingDate) {
                executeStep(new CreateTPSUFileStep());
                executeStep(new UploadTPSUFileStep());
                executeStep(new ArchiveTPSUFileStep());
            }
        }

        @Override
        protected void validateRuntimeParameters() {
            String pCommandLineArg = getJobInstanceParameters().trim();
            logger.info("Command Line Arguments: " + pCommandLineArg);

            try {
                Application.beginUnitOfWork();
                mProcessingDate = PSPDate.getPSPTime();
                CalendarUtils.clearTime(mProcessingDate);
                mValidProcessingDate = isFirstBusinessDayOfTheMonth(mProcessingDate);
                if (!mValidProcessingDate) {
                    logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
                }
            } finally {
                Application.commitUnitOfWork();
            }

            if (getRunMode() == BatchJobProcessor.RunMode.NotUsingFlux) {
                String[] args = null;
                if (pCommandLineArg.trim().length() > 0) {
                    args = pCommandLineArg.split(" ");
                }

                if (args != null && args.length >= 1) {
                    if (!args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
                        throw new RuntimeException("Invalid processing date specified. ");
                    }
                    try {
                        mProcessingDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[0]);
                        Application.beginUnitOfWork();
                        CalendarUtils.clearTime(mProcessingDate);
                        mValidProcessingDate = Boolean.TRUE;
                    } finally {
                        Application.commitUnitOfWork();
                    }
                }
            }
        }

        @Override
        protected void validateStepRuntimeParameters(String stepName) {
            String pCommandLineArgs = getJobInstanceParameters().trim();
            String[] args = null;
            if (pCommandLineArgs.trim().length() > 0) {
                args = pCommandLineArgs.split(" ");
            }

            if (CreateTPSUFileStep.class.getSimpleName().equalsIgnoreCase(stepName)) {
                validateCreateFileStepArguments(args);
            } else  if (UploadTPSUFileStep.class.getSimpleName().equalsIgnoreCase(stepName) || ArchiveTPSUFileStep.class.getSimpleName().equalsIgnoreCase(stepName)) {
                validateFileNames(args, TPSUReport.TPS_PREFIX);
            } else {
                StringBuilder err = new StringBuilder();
                err.append("The specified job step \"").
                        append(stepName).
                        append("\" does not exist in batch processor ").
                        append(this.getClass().getSimpleName()).
                        append(". The valid steps (with optional arguments) that can be executed are {").
                        append(CreateTPSUFileStep.class.getSimpleName()).append(",<yyyyMMDD> ").
                        append(UploadTPSUFileStep.class.getSimpleName()).append(" <file names seperated by comma>, ").
                        append(ArchiveTPSUFileStep.class.getSimpleName()).append(" <file names seperated by comma>, ");

                throw new RuntimeException(err.toString());
            }
        }

        private void validateCreateFileStepArguments(String[] args) {
            if (args != null && args.length >= 1) {
                if (args[0] == null || !args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
                    throw new RuntimeException("Invalid processing date specified. ");
                }
                try {
                    mProcessingDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[0]);
                    Application.beginUnitOfWork();
                    CalendarUtils.clearTime(mProcessingDate);
                    mValidProcessingDate = Boolean.TRUE;
                } finally {
                    Application.commitUnitOfWork();
                }
            } else {
                try {
                    Application.beginUnitOfWork();
                    mProcessingDate = PSPDate.getPSPTime();
                    CalendarUtils.clearTime(mProcessingDate);
                    mValidProcessingDate = !BatchUtils.isWeekendOrHoliday(mProcessingDate);
                } finally {
                    Application.commitUnitOfWork();
                }
            }
        }

        private void validateFileNames(String[] args, String prefix){
            if (args != null && args.length == 1) {
                String[] fileNames = args[0].split(",");
                mReportFileNames = new ArrayList<String>();
                for (String fileName : fileNames) {
                    if (!fileName.startsWith(prefix)) {
                        throw new RuntimeException("Invalid File name: " + fileName);
                    }
                    mReportFileNames.add(fileName);
                }
            }
        }

        public class CreateTPSUFileStep extends BatchJobProcessorStep {
            public void execute() {
                try {
                    PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                    logger.info("Starting CreateTPSUFileStep");
                    StopWatch timer = StopWatch.startTimer();

                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                    // Frequency is First business day of the month
                    SpcfCalendar mPreviousMonth = mProcessingDate.copy();
                    mPreviousMonth.addMonths(-1);
                    CalendarUtils.clearTime(mPreviousMonth);

                    SpcfCalendar mfromDate = CalendarUtils.getFirstDayOfMonth(mPreviousMonth);
                    CalendarUtils.clearTime(mfromDate);

                    SpcfCalendar mToDate = CalendarUtils.getFirstDayOfMonth(mProcessingDate);
                    CalendarUtils.clearTime(mToDate);
                    mToDate.addMilliseconds(-1);

                    new TPSUReport().createJPMCReport(mfromDate, mToDate);

                    PayrollServices.commitUnitOfWork();
                    logger.info("Completed CreateTPSUFileStep. Elapsed time: " + timer.stop().getElapsedTimeString());
                } catch (Throwable t) {
                    PayrollServices.rollbackUnitOfWork();
                    logger.error("Error in CreateTPSUFileStep for JPMC Screening process: " + t);
                    throw new RuntimeException("Exception in job step CreateTPSUFileStep ", t);
                }
            }
        }

        public class UploadTPSUFileStep extends BatchJobProcessorStep {
            public void execute() {
                try {
                    PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                    logger.info("Starting UploadTPSUFileStep");
                    StopWatch timer = StopWatch.startTimer();
                    new SftpJPMCDDScreeningProcessFileUpload().upload(ReportType.TPSU, null, mReportFileNames);
                    logger.info("Completed UploadTPSUFileStep: " + timer.stop().getElapsedTimeString());
                } catch (Throwable t) {
                    logger.error("Error in UploadTPSUFileStep for JPMC Screening process: " + t);
                    throw new RuntimeException("Exception in job step UploadTPSUFileStep ", t);
                }
            }
        }

        public class ArchiveTPSUFileStep extends BatchJobProcessorStep {
            public void execute() {
                try {
                    PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                    logger.info("Starting ArchiveTPSUFileStep");
                    StopWatch timer = StopWatch.startTimer();
                    new SftpJPMCDDScreeningProcessFileUpload().archiveFiles(ReportType.TPSU, mReportFileNames);
                    logger.info("Completed ArchiveTPSUFileStep: " + timer.stop().getElapsedTimeString());
                } catch (Throwable t) {
                    logger.error("Error in ArchiveTPSUFileStep for JPMC Screening process: " + t);
                    throw new RuntimeException("Exception in job step ArchiveTPSUFileStep ", t);
                }
            }
        }

        private static boolean isFirstBusinessDayOfTheMonth(SpcfCalendar pSpcfCalendar) {
            SpcfCalendar firstBusinessDay = CalendarUtils.getFirstBusinessDayOfMonth(pSpcfCalendar);
            return firstBusinessDay.equals(pSpcfCalendar);
        }
}
