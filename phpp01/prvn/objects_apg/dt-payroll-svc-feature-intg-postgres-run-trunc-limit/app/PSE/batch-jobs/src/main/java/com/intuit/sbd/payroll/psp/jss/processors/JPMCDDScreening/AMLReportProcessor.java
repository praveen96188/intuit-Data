package com.intuit.sbd.payroll.psp.jss.processors.JPMCDDScreening;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.ReportType;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.AMLReport;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpJPMCDDScreeningProcessFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suganyas315 on 7/6/15.
 */

@ScheduledJob(name = "AMLReportProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class AMLReportProcessor extends JSSBatchJob {
    private SpcfCalendar mProcessingDate;
    private boolean mValidProcessingDate = Boolean.FALSE;
    private List<String> mAMLReportFileNames;


    public AMLReportProcessor(String[] pArguments) {
        super(pArguments);
	}
	public AMLReportProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}

    @Override
    protected void execute() {
        if (mValidProcessingDate) {
            executeStep(CreateFileStep.class);
            executeStep(EmailFileStep.class);
            executeStep(ArchiveFileStep.class);
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
            mValidProcessingDate = !BatchUtils.isWeekendOrHoliday(mProcessingDate);
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

        if (CreateFileStep.class.getSimpleName().equalsIgnoreCase(stepName)) {
            validateCreateFileStepArguments(args);
        } else if (EmailFileStep.class.getSimpleName().equalsIgnoreCase(stepName)) {
            validateUploadFileStepArguments(args);
        } else if (ArchiveFileStep.class.getSimpleName().equalsIgnoreCase(stepName)) {
            validateArchiveFileStepArguments(args);
        } else {
            StringBuilder err = new StringBuilder();
            err.append("The specified job step \"").
                    append(stepName).
                    append("\" does not exist in batch processor ").
                    append(this.getClass().getSimpleName()).
                    append(". The valid steps (with optional arguments) that can be executed are {").
                    append(CreateFileStep.class.getSimpleName()).append(",<yyyyMMDD> ").
                    append(EmailFileStep.class.getSimpleName()).append(" <file names seperated by comma>, ").
                    append(ArchiveFileStep.class.getSimpleName()).append(" <file names seperated by comma>, ");
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

    private void validateUploadFileStepArguments(String[] args) {
        if (args != null && args.length == 1) {
            String[] fileNames = args[0].split(",");
            validateFileNames(fileNames);
        }
    }

    private void validateArchiveFileStepArguments(String[] args) {
        if (args != null && args.length == 1) {
            String[] fileNames = args[0].split(",");
            validateFileNames(fileNames);
        }
    }

    private void validateFileNames(String[] fileNames){
            mAMLReportFileNames = new ArrayList<String>();
            for (String fileName : fileNames) {
                if (!fileName.startsWith(AMLReport.AML_DATA_FILE_PREFIX)) {
                    throw new RuntimeException("Invalid File name: " + fileName);
                }
                mAMLReportFileNames.add(fileName);
            }
    }

    public static class CreateFileStep extends JSSBatchJobStep<AMLReportProcessor> {
    	@Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                getLogger().info("Starting CreateFileStep");
                StopWatch timer = StopWatch.startTimer();

                PayrollServices.beginUnitOfWork(FlushMode.MANUAL,true);

                //Frequency - Runs everyday
                SpcfCalendar mfromDate = getBatchJobProcessor().mProcessingDate.copy();
                CalendarUtils.addBusinessDays(mfromDate, -1);
                CalendarUtils.clearTime(mfromDate);

                new AMLReport().createJPMCReport(mfromDate, getBatchJobProcessor().mProcessingDate);

                PayrollServices.commitUnitOfWork();
                getLogger().info("Completed CreateFileStep. Elapsed time: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();
                getLogger().error("Error in CreateFileStep for JPMC Screening process: " + t);
                throw new RuntimeException("Exception in job step CreateFileStep ", t);
            }
        }
    }

    public static class EmailFileStep extends JSSBatchJobStep<AMLReportProcessor> {
        @Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                getLogger().info("Starting EmailFileStep");
                StopWatch timer = StopWatch.startTimer();
                new SftpJPMCDDScreeningProcessFileUpload().emailFile(ReportType.AML, getBatchJobProcessor().mAMLReportFileNames);
                getLogger().info("Completed EmailFileStep: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                getLogger().error("Error in EmailFileStep for JPMC Screening process: " + t);
                throw new RuntimeException("Exception in job step EmailFileStep ", t);
            }
        }
    }

    public static class ArchiveFileStep extends JSSBatchJobStep<AMLReportProcessor> {
    	@Override
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.JPMCDirectDepositScreeningBatchJob);
                getLogger().info("Starting ArchiveFileStep");
                StopWatch timer = StopWatch.startTimer();
                new SftpJPMCDDScreeningProcessFileUpload().archiveFiles(ReportType.AML, getBatchJobProcessor().mAMLReportFileNames);
                getLogger().info("Completed ArchiveFileStep: " + timer.stop().getElapsedTimeString());
            } catch (Throwable t) {
                getLogger().error("Error in ArchiveFileStep for JPMC Screening process: " + t);
                throw new RuntimeException("Exception in job step ArchiveFileStep ", t);
            }
        }
    }
}