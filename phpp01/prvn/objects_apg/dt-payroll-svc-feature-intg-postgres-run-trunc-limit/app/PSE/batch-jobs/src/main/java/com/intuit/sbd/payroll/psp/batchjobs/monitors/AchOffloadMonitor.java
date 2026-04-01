package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 9:53:05 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AchOffloadMonitor extends BatchJobProcessorMonitor {
    protected boolean mDoFinalOffloadChecksOnly = false;

    public AchOffloadMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor,
                             BatchJobType pJobTypeToMonitor) {
        this(pRunMode, pBatchJobType, pJobId, pJobIdToMonitor, pJobTypeToMonitor, DailyBatchJobsProcessor.OffloadAchData.class);
    }

    public AchOffloadMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor,
                             BatchJobType pJobTypeToMonitor, Class pJobActionToMonitor) {
        super(pRunMode, pBatchJobType, pJobId, pJobIdToMonitor, pJobTypeToMonitor, pJobActionToMonitor);
    }

    protected void setDoFinalOffloadChecksOnly(boolean pDoFinalOffloadChecksOnly) {
        mDoFinalOffloadChecksOnly = pDoFinalOffloadChecksOnly;
    }

    protected StringBuffer doFileCountCheck(NACHAFile pNachaFile, DICRFile pDicrFile) {
        StringBuffer err = new StringBuffer();

        if (pDicrFile == null) {
            err.append("ACH file has not been acknowledged by bank (DICR not found):").append(BatchUtils.NEWLINE);
            err.append(">  NACHAFile id : ").append(pNachaFile.getId().toString()).append(BatchUtils.NEWLINE);
            err.append(">  ACH file name: ").append(pNachaFile.getFileName()).append(BatchUtils.NEWLINE);
        }

        return err;
    }

    protected StringBuffer doDicrFraudCheck(NACHAFile pNachaFile, DICRFile pDicrFile) {
        StringBuffer err = new StringBuffer();

        if (pDicrFile != null) {
            BigDecimal nCredit = SpcfUtils.convertToBigDecimal(pNachaFile.getCreditTxnTotalAmount());
            BigDecimal nDebit = SpcfUtils.convertToBigDecimal(pNachaFile.getDebitTxnTotalAmount());
            BigDecimal dCredit = SpcfUtils.convertToBigDecimal(pDicrFile.getCreditTxnTotalAmount());
            BigDecimal dDebit = SpcfUtils.convertToBigDecimal(pDicrFile.getDebitTxnTotalAmount());

            if (!dDebit.equals(nDebit) || !dCredit.equals(nCredit)) {
                err.append("NACHA and DICR credit/debit amounts do not match:").append(BatchUtils.NEWLINE);
                err.append(">  NACHA file record id    : ").append(pNachaFile.getId().toString()).append(BatchUtils.NEWLINE);
                err.append(">  NACHA file name         : ").append(pNachaFile.getFileName()).append(BatchUtils.NEWLINE);
                err.append(">  NACHA file credit amount: ").append(String.format("$%(,.2f", nCredit)).append(BatchUtils.NEWLINE);
                err.append(">  NACHA file debit amount : ").append(String.format("$%(,.2f", nDebit)).append(BatchUtils.NEWLINE);
                err.append(">  DICR file record id     : ").append(pDicrFile.getId().toString()).append(BatchUtils.NEWLINE);
                err.append(">  DICR file name          : ").append(pDicrFile.getFileName()).append(BatchUtils.NEWLINE);
                err.append(">  DICR file credit amount : ").append(String.format("$%(,.2f", dCredit)).append(BatchUtils.NEWLINE);
                err.append(">  DICR file debit amount  : ").append(String.format("$%(,.2f", dDebit)).append(BatchUtils.NEWLINE);
            }
        }

        return err;
    }

    protected StringBuffer doConfirmationCheck(NACHAFile pNachaFile) {
        StringBuffer err = new StringBuffer();

        String code = pNachaFile.getConfirmationCode();

        if ((code == null) || (code.length() == 0)) {
            err.append("ACH file has not been confirmed:").append(BatchUtils.NEWLINE);
            err.append(">  NACHAFile id : ").append(pNachaFile.getId().toString()).append(BatchUtils.NEWLINE);
            err.append(">  ACH file name: ").append(pNachaFile.getFileName()).append(BatchUtils.NEWLINE);
        }

        return err;
    }

    abstract protected void doFinalOffloadChecks();

    protected void doFinalOffloadChecks(OffloadBatch pOffloadBatch) {
        StringBuffer err = new StringBuffer();

        DomainEntitySet<NACHAFile> nachaFileList = pOffloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Archived);

        if (nachaFileList.isEmpty()) {
            err.append("No NACHAFile records could be located for offload batch (id: ");
            err.append(pOffloadBatch.getId().toString());
            err.append(")");
        } else {
            for (NACHAFile nachaFile : nachaFileList) {
                StringBuffer subErr = new StringBuffer();

                DICRFile dicrFile = BatchUtils.getDicrFileForNachaFile(nachaFile);

                subErr.append(doFileCountCheck(nachaFile, dicrFile));
                subErr.append(doDicrFraudCheck(nachaFile, dicrFile));
                //disabling the check on confirmation code since we are moving to AutoConfirmation mode on JPMC PSP-4929
                //subErr.append(doConfirmationCheck(nachaFile));

                if (subErr.length() > 0) {
                    if (err.length() == 0) {
                        err.append("The following errors were detected by the ACH Offload monitor:");
                        err.append(BatchUtils.NEWLINE);
                    }

                    err.append(BatchUtils.NEWLINE);
                    err.append(subErr);
                }
            }
        }

        if (err.length() > 0) {
            throw new RuntimeException(err.toString());
        }
    }

    public void execute() {
        //
        // Intentionally not calling super class' execute method since we don't want the default monitoring behavior.
        //

        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        if (!mDoFinalOffloadChecksOnly) {
            executeStep(new VerifyAchFileCreationStartedDelay());
            executeStep(new VerifyAchFileCreationStarted());
            executeStep(new VerifyAchFileCreationCompletedDelay());
            executeStep(new VerifyAchFileCreationCompleted());
            executeStep(new VerifyAchFileSendStartedDelay());
            executeStep(new VerifyAchFileSendStarted());
            executeStep(new VerifyAchFileSendCompleteDelay());
            executeStep(new VerifyAchFileSendComplete());
            executeStep(new VerifyDicrFileReceivedDelay());
            executeStep(new VerifyDicrFileReceived());
            executeStep(new PerformFinalOffloadChecksDelay());
        }

        executeStep(new PerformFinalOffloadChecks());
    }

    public class VerifyAchFileCreationStartedDelay extends BatchJobProcessorStep {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_creation_started_delay", "1");
        }
    }

    public class VerifyAchFileCreationStarted extends BatchJobProcessorStep {
        public void execute() {
            verifyJobStepStarted(getBatchJobToMonitorId(), getJobTypeToMonitor(), DailyBatchJobsProcessor.OffloadAchData.class, jobStepTimeConstraint);
        }
    }

    public class VerifyAchFileCreationCompletedDelay extends BatchJobProcessorStep {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_creation_complete_delay", "20");
        }
    }

    public class VerifyAchFileCreationCompleted extends BatchJobProcessorStep {
        public void execute() {
            verifyJobStepFinished(getBatchJobToMonitorId(), getJobTypeToMonitor(), DailyBatchJobsProcessor.CreateAchFiles.class, jobStepTimeConstraint);
        }
    }

    public class VerifyAchFileSendStartedDelay extends BatchJobProcessorStep {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_send_started_delay", "1");
        }
    }

    public class VerifyAchFileSendStarted extends BatchJobProcessorStep {
        public void execute() {
            verifyJobStepStarted(getBatchJobToMonitorId(), getJobTypeToMonitor(), DailyBatchJobsProcessor.UploadAchFiles.class, jobStepTimeConstraint);
        }
    }

    public class VerifyAchFileSendCompleteDelay extends BatchJobProcessorStep {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_send_complete_delay", "15");
        }
    }

    public class VerifyAchFileSendComplete extends BatchJobProcessorStep {
        public void execute() {
            verifyJobStepFinished(getBatchJobToMonitorId(), getJobTypeToMonitor(), DailyBatchJobsProcessor.UploadAchFiles.class, jobStepTimeConstraint);
        }
    }

    public class VerifyDicrFileReceivedDelay extends BatchJobProcessorStep {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_dicr_files_received_delay", "15");
        }
    }

    public class VerifyDicrFileReceived extends BatchJobProcessorStep {
        public void execute() {
            verifyJobStepFinished(getBatchJobToMonitorId(), getJobTypeToMonitor(), DailyBatchJobsProcessor.DownloadDicrFiles.class, jobStepTimeConstraint);
        }
    }

    public class PerformFinalOffloadChecksDelay extends BatchJobProcessorStep {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_confirmation_received_delay", "15");
        }
    }

    public class PerformFinalOffloadChecks extends BatchJobProcessorStep {
        public void execute() {
            // do any final offload confirmation checks (i.e. fraud, confirmation, etc.)
            doFinalOffloadChecks();
        }
    }
}
