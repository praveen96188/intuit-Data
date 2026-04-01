package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.DICRFile;
import com.intuit.sbd.payroll.psp.domain.NACHAFile;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domain.OffloadBatch;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 9:53:05 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AchOffloadMonitor extends JSSBatchJobMonitor {
    protected boolean mDoFinalOffloadChecksOnly = false;

    public AchOffloadMonitor(String[] pArguments) {
        super(pArguments);
    }

    public AchOffloadMonitor(String[] pArguments, String pJobId) {
        super(pArguments,pJobId);
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return DailyBatchJobsProcessor.OffloadAchData.class;
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
            getLogger().warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        if (!mDoFinalOffloadChecksOnly) {
            executeStep(VerifyAchFileCreationStartedDelay.class);
            executeStep(VerifyAchFileCreationStarted.class);
            executeStep(VerifyAchFileCreationCompletedDelay.class);
            executeStep(VerifyAchFileCreationCompleted.class);
            executeStep(VerifyAchFileSendStartedDelay.class);
            executeStep(VerifyAchFileSendStarted.class);
            executeStep(VerifyAchFileSendCompleteDelay.class);
            executeStep(VerifyAchFileSendComplete.class);
            executeStep(VerifyDicrFileReceivedDelay.class);
            executeStep(VerifyDicrFileReceived.class);
            executeStep(PerformFinalOffloadChecksDelay.class);
        }

        executeStep(PerformFinalOffloadChecks.class);
    }

    public static class VerifyAchFileCreationStartedDelay extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_creation_started_delay", "1");
        }
    }

    public static class VerifyAchFileCreationStarted extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            getBatchJobProcessor().verifyJobStepStarted(getBatchJobProcessor().getBatchJobToMonitorId(), getBatchJobProcessor().getBatchJobToMonitor(), DailyBatchJobsProcessor.OffloadAchData.class, jobStepTimeConstraint);
        }
    }

    public static class VerifyAchFileCreationCompletedDelay extends JSSBatchJobStep<AchOffloadMonitor> {
       public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_creation_complete_delay", "20");
        }
    }

    public static class VerifyAchFileCreationCompleted extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            getBatchJobProcessor().verifyJobStepFinished(getBatchJobProcessor().getBatchJobToMonitorId(), getBatchJobProcessor().getBatchJobToMonitor(), DailyBatchJobsProcessor.CreateAchFiles.class, jobStepTimeConstraint);
        }
    }

    public static class VerifyAchFileSendStartedDelay extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_send_started_delay", "1");
        }
    }

    public static class VerifyAchFileSendStarted extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            getBatchJobProcessor().verifyJobStepStarted(getBatchJobProcessor().getBatchJobToMonitorId(), getBatchJobProcessor().getBatchJobToMonitor(), DailyBatchJobsProcessor.UploadAchFiles.class, jobStepTimeConstraint);
        }
    }

    public static class VerifyAchFileSendCompleteDelay extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_ach_file_send_complete_delay", "15");
        }
    }

    public static class VerifyAchFileSendComplete extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            getBatchJobProcessor().verifyJobStepFinished(getBatchJobProcessor().getBatchJobToMonitorId(), getBatchJobProcessor().getBatchJobToMonitor(), DailyBatchJobsProcessor.UploadAchFiles.class, jobStepTimeConstraint);
        }
    }

    public static class VerifyDicrFileReceivedDelay extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_dicr_files_received_delay", "15");
        }
    }

    public static class VerifyDicrFileReceived extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            getBatchJobProcessor().verifyJobStepFinished(getBatchJobProcessor().getBatchJobToMonitorId(), getBatchJobProcessor().getBatchJobToMonitor(), DailyBatchJobsProcessor.DownloadDicrFiles.class, jobStepTimeConstraint);
        }
    }

    public static class PerformFinalOffloadChecksDelay extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            BatchUtils.delay("psp_offload_monitor_confirmation_received_delay", "15");
        }
    }

    public static class PerformFinalOffloadChecks extends JSSBatchJobStep<AchOffloadMonitor> {
        public void execute() {
            // do any final offload confirmation checks (i.e. fraud, confirmation, etc.)
            getBatchJobProcessor().doFinalOffloadChecks();
        }
    }
}
