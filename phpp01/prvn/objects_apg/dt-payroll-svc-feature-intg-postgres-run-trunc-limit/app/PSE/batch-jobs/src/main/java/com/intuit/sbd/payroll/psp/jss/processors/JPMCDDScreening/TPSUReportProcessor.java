package com.intuit.sbd.payroll.psp.jss.processors.JPMCDDScreening;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.ReportType;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.TPSUReport;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpJPMCDDScreeningProcessFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charithah418 on 9/2/2016.
 */
@ScheduledJob(name = "TPSUReportProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class TPSUReportProcessor extends JSSBatchJob {

    private SpcfCalendar mProcessingDate;
    private boolean mValidProcessingDate = false;
    private List<String> mReportFileNames;

    public TPSUReportProcessor(String[] pArguments) {
        super(pArguments);
    }
    public TPSUReportProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        if (mValidProcessingDate) {
            executeStep(CreateTPSUFileStep.class);
            executeStep(UploadTPSUFileStep.class);
            executeStep(ArchiveTPSUFileStep.class);
        }
    }

    @Override
    protected void validateRuntimeParameters() {
        String pCommandLineArg = getJobInstanceParameters().trim();
        getLogger().info("Command Line Arguments: " + pCommandLineArg);

        try {
            Application.beginUnitOfWork();
            mProcessingDate = PSPDate.getPSPTime();
            CalendarUtils.clearTime(mProcessingDate);
            mValidProcessingDate = isFirstBusinessDayOfTheMonth(mProcessingDate);
            if (!mValidProcessingDate) {
                getLogger().warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            }
        } finally {
            Application.commitUnitOfWork();
        }

        String[] args = null;
        if (pCommandLineArg.trim().length() > 0) {
            args = pCommandLineArg.split(" ");
        }

        if (args != null && args.length >= 1) {
            if (args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
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

    public static class CreateTPSUFileStep extends JSSBatchJobStep<TPSUReportProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                getLogger().info("Starting CreateTPSUFileStep");
                StopWatch timer = StopWatch.startTimer();

                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                // Frequency is First business day of the month
                SpcfCalendar mPreviousMonth = getBatchJobProcessor().mProcessingDate.copy();
                mPreviousMonth.addMonths(-1);
                CalendarUtils.clearTime(mPreviousMonth);

                SpcfCalendar mfromDate = CalendarUtils.getFirstDayOfMonth(mPreviousMonth);
                CalendarUtils.clearTime(mfromDate);

                SpcfCalendar mToDate = CalendarUtils.getFirstDayOfMonth(getBatchJobProcessor().mProcessingDate);
                CalendarUtils.clearTime(mToDate);
                mToDate.addMilliseconds(-1);

                new TPSUReport().createJPMCReport(mfromDate, mToDate);

                PayrollServices.commitUnitOfWork();
                getLogger().info("Completed CreateTPSUFileStep. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();
                getLogger().error("Error in CreateTPSUFileStep for JPMC Screening process: " + t);
                throw new RuntimeException("Exception in job step CreateTPSUFileStep ", t);
            }
        }
    }

    public static class UploadTPSUFileStep extends JSSBatchJobStep<TPSUReportProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                getLogger().info("Starting UploadTPSUFileStep");
                StopWatch timer = StopWatch.startTimer();
                new SftpJPMCDDScreeningProcessFileUpload().upload(ReportType.TPSU, null, getBatchJobProcessor().mReportFileNames);
                getLogger().info("Completed UploadTPSUFileStep: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                getLogger().error("Error in UploadTPSUFileStep for JPMC Screening process: " + t);
                throw new RuntimeException("Exception in job step UploadTPSUFileStep ", t);
            }
        }
    }

    public static class ArchiveTPSUFileStep extends JSSBatchJobStep<TPSUReportProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                getLogger().info("Starting ArchiveTPSUFileStep");
                StopWatch timer = StopWatch.startTimer();
                new SftpJPMCDDScreeningProcessFileUpload().archiveFiles(ReportType.TPSU, getBatchJobProcessor().mReportFileNames);
                getLogger().info("Completed ArchiveTPSUFileStep: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                getLogger().error("Error in ArchiveTPSUFileStep for JPMC Screening process: " + t);
                throw new RuntimeException("Exception in job step ArchiveTPSUFileStep ", t);
            }
        }
    }

    private static boolean isFirstBusinessDayOfTheMonth(SpcfCalendar pSpcfCalendar) {
        SpcfCalendar firstBusinessDay = CalendarUtils.getFirstBusinessDayOfMonth(pSpcfCalendar);
        return firstBusinessDay.equals(pSpcfCalendar);
    }
}
