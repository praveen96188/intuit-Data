package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpAchDicrFileDownload;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpAchFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Dec 31, 2008
 * Time: 5:44:53 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DailyBatchJobsProcessor extends BatchJobProcessor {
    protected enum AchOffloadType {
        PRIMARY, SCHEDULED, TAX_PAYMENT
    }

    private static String OFFLOAD_BATCH_ID = "offload_batch_id";

    private SpcfCalendar mOffloadDate = null;
    private boolean mValidOffloadDate = false;
    private boolean mValidOffloadTime = false;
    private ACHFileType mACHFileType = ACHFileType.DD;

    protected abstract AchOffloadType getAchOffloadType();

    protected abstract void establishOffloadDate();

    protected abstract boolean validateOffloadTime();


    public DailyBatchJobsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    protected SpcfCalendar getOffloadDate() {
        return mOffloadDate;
    }

    protected boolean isValidOffloadDate() {
        return mValidOffloadDate;
    }

    protected boolean isValidOffloadTime() {
        return mValidOffloadTime;
    }


    protected void validateRuntimeParameters() {
        logger.info("Validating " + this.getClass().getSimpleName() + " command line: " +
                getJobInstanceParameters().trim());

        try {
            PayrollServices.beginUnitOfWork();

            establishOffloadDate();

            if (mOffloadDate == null) {
                throw new RuntimeException("Invalid offload date (null)");
            }

            mValidOffloadDate = !BatchUtils.isWeekendOrHoliday(mOffloadDate);
            mValidOffloadTime = validateOffloadTime();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    protected void validateStepRuntimeParameters(String stepName) {
        try {
            if (NotifyAchOffloadStarted.class.getSimpleName().equals(stepName) ||
                    OffloadAchData.class.getSimpleName().equals(stepName) ||
                    CreateAchFiles.class.getSimpleName().equals(stepName) ||
                    NotifyAchOffloadComplete.class.getSimpleName().equals(stepName) ||
                    DownloadDicrFiles.class.getSimpleName().equals(stepName) ||
                    UpdatePayrollStatus.class.getSimpleName().equals(stepName) ||
                    UpdateMoneyMovementTransaction.class.getSimpleName().equals(stepName) ||
                    UpdateTaxPaymentsAgencyStatus.class.getSimpleName().equals(stepName) ||
                    UpdateFinancialTransaction.class.getSimpleName().equals(stepName) ||
                    InsertFinancialTransactionState.class.getSimpleName().equals(stepName) ||
                    MissedPayrollProcessor.class.getSimpleName().equals(stepName) ||
                    MissedTransactionProcessor.class.getSimpleName().equals(stepName) ||
                    SalesTaxExceptionProcessor.class.getSimpleName().equals(stepName)) {
                validateRuntimeParameters();

                if (!isValidOffloadDate()) {
                    throw new RuntimeException("Specified offload date is bank holiday: " +
                                                       mOffloadDate.format(BatchUtils.DATE_FORMAT));
                }

                String[] args = getJobInstanceParameters().trim().split(" ");

                if (args.length > 0) {
                    for (String arg : args) {
                        // see if the offload batch id guid was specified on the command line
                        if (BatchUtils.isValidGuid(arg)) {
                            // save the offload batch id for use in later steps
                            getBatchJobContext().put(OFFLOAD_BATCH_ID, BatchUtils.formatGuid(arg));
                        }
                    }
                }
            } else if (UploadAchFiles.class.getSimpleName().equals(stepName)) {
                // no validation
            } else if (ArchiveDailyFiles.class.getSimpleName().equals(stepName)) {
                // no validation
            } else if (CreateTransactionOffloadedEvents.class.getSimpleName().equals(stepName)) {
                // no validation
            } else {
                throw new RuntimeException("Invalid job step.");
            }
        } catch (Throwable t) {
            StringBuffer err = new StringBuffer();

            err.append("The specified job step \"").
                    append(stepName).
                    append("\" does not exist in batch processor ").
                    append(this.getClass().getSimpleName()).
                    append(". The valid steps (with optional [] or required <> arguments) that can be executed are {").
                    append(NotifyAchOffloadStarted.class.getSimpleName()).append(" [yyyyMMdd], ").
                    append(OffloadAchData.class.getSimpleName()).append(" [yyyyMMdd], ").
                    append(CreateAchFiles.class.getSimpleName()).append(" [yyyyMMdd] [offload-batch-id], ").
                    append(UploadAchFiles.class.getSimpleName()).append(", ").
                    append(NotifyAchOffloadComplete.class.getSimpleName()).append(", ").
                    append(DownloadDicrFiles.class.getSimpleName()).append(", ").
                    append(ArchiveDailyFiles.class.getSimpleName()).append(", ").
                    append(UpdatePayrollStatus.class.getSimpleName()).append(", ").
                    append(UpdateMoneyMovementTransaction.class.getSimpleName()).append(", ").
                    append(UpdateTaxPaymentsAgencyStatus.class.getSimpleName()).append(", ").
                    append(UpdateFinancialTransaction.class.getSimpleName()).append(", ").
                    append(InsertFinancialTransactionState.class.getSimpleName()).append(", ").
                    append(CreateTransactionOffloadedEvents.class.getSimpleName()).append(", ").
                    append(MissedPayrollProcessor.class.getSimpleName()).append(" [yyyyMMdd], ").
                    append(MissedTransactionProcessor.class.getSimpleName()).append(" [yyyyMMdd], ").
                    append(SalesTaxExceptionProcessor.class.getSimpleName()).append(" [yyyyMMdd]").
                    append("}");

            throw new RuntimeException(err.toString(), t);
        }
    }

    protected void establishOffloadDate(SpcfCalendar pOffloadDate, boolean pIgnoreRolloverTime) {
        SpcfCalendar cOffloadDate = pOffloadDate.copy();

        if (!pIgnoreRolloverTime) {
            String currentTime = cOffloadDate.format(BatchUtils.TIME_FORMAT);
            String rolloverTime = BatchUtils.getConfigString("psp_offload_date_rollover_time", "15:00");

            if (rolloverTime.matches("[0-9]:[0-5][0-9]")) {
                // if rolloverTime is of the form H:mm, adjust it to 0H:mm
                rolloverTime = "0" + rolloverTime;
            } else if (!rolloverTime.matches("[0-1][0-9]:[0-5][0-9]")) {
                // if rolloverTime is invalid, default to 15:00
                rolloverTime = "15:00";
            }

            // Determine the actual offload date
            // (the rolloverTime is the HH:mm at which we accept the current date)
            // (if the current time is between 00:00 and rolloverTime, then use the previous business day)
            // (don't adjust ach offload type of SCHEDULED since this will improperly set the date back one day)
            if (currentTime.compareTo(rolloverTime) < 0) {
                CalendarUtils.addBusinessDays(cOffloadDate, -1);
            }
        }

        mOffloadDate = cOffloadDate;
    }

    protected void sendEmail(String pToFromProperty, String pSubjectProperty, String pMessageBody) {
        //
        // don't fail the overall batch job if there is an error sending email, just report the error in the log.
        //
        try {
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString(pToFromProperty), // to
                    BatchUtils.getConfigString(pToFromProperty), // from
                    BatchUtils.getConfigString(pSubjectProperty),
                    pMessageBody);
        } catch (Exception e) {
            logger.error("Failed to send email message for daily batch job. Email message was: " + pMessageBody, e);
        }
    }

    protected String getOffloadBatchId(SpcfCalendar pNormalizedOffloadDate) {
        // try to get the offload batch from the CreateAchFiles step (if available)
        String batchId = getBatchJobContext().get(OFFLOAD_BATCH_ID);

        // if the offload batch is not available in the batch job context (if we've reentered the job)
        // then we need to find the offload batch as best we can.
        if ((batchId == null) || (batchId.length() == 0)) {
            String offloadGroupCd = (getAchOffloadType().equals(AchOffloadType.TAX_PAYMENT)) ? OffloadGroup.Codes.TAXPAYMENT : OffloadGroup.Codes.STANDARD;
            OffloadGroup offloadGroup =  Application.find(OffloadGroup.class, OffloadGroup.OffloadGroupCd().equalTo(offloadGroupCd)).get(0);

            // get the most recent offload batch in a Completed state
            Expression<OffloadBatch> query = new Query<OffloadBatch>()
                    .Where(OffloadBatch.OffloadDate().equalTo(pNormalizedOffloadDate)
                            .And(OffloadBatch.OffloadGroup().equalTo(offloadGroup))
                            .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed))
                            .And(OffloadBatch.IsOffloadedTransactionsEventCreationComplete().equalTo(false)))
                    .OrderBy(OffloadBatch.CreatedDate().Descending()); // sort descending

            // get offload batches for the given offload date in descending order
            DomainEntitySet<OffloadBatch> batchSet = Application.find(OffloadBatch.class, query);

            if (batchSet.isEmpty()) {
                throw new RuntimeException("Unable to locate offload batch for offload date: " +
                        pNormalizedOffloadDate.format(BatchUtils.DATE_FORMAT));
            }

            batchId = batchSet.get(0).getId().toString();

            // save the offload batch id for use in later steps
            getBatchJobContext().put(OFFLOAD_BATCH_ID, batchId);
        }

        return batchId;
    }

    protected SpcfCalendar getNormalizedOffloadDate() {
        SpcfCalendar mormalizedOffloadDate = getOffloadDate();

        // Set the time portion to zeroes for comparison purposes
        mormalizedOffloadDate.setValues(mormalizedOffloadDate.getYear(),
                mormalizedOffloadDate.getMonth(),
                mormalizedOffloadDate.getDay());

        return mormalizedOffloadDate;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Job Steps
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class NotifyAchOffloadStarted extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                String message;

                // set up the message
                switch (getAchOffloadType()) {
                    case PRIMARY:
                        message = "Primary";
                        break;
                    case SCHEDULED:
                        message = "Second";
                        break;
                    case TAX_PAYMENT:
                        message = "Tax Payment";
                        break;
                    default:
                        message = "";
                        break;
                }


                message += " ACH offload has started for: " + getOffloadDate().format("EEE, MMM dd, yyyy");

                logger.info(message);

                sendEmail("psp_offload_notify_list", "psp_offload_notify_subject", message);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step NotifyAchOffloadStarted ", t);
            }
        }
    }

    public class OffloadAchData extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();
                    String offloadBatchId = null;
                    if (getAchOffloadType().equals(AchOffloadType.TAX_PAYMENT)) {
                        offloadBatchId = new OffloadACHTransactions().offload(OffloadGroup.Codes.TAXPAYMENT,
                                getOffloadDate(), ACHFileType.Tax);
                    } else {
                        offloadBatchId = new OffloadACHTransactions().offload(OffloadGroup.Codes.STANDARD,
                                getOffloadDate(), ACHFileType.DD);
                    }

                    // save the offload batch id for use in later steps
                    getBatchJobContext().put(OFFLOAD_BATCH_ID, offloadBatchId);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step OffloadAchData ", t);
            }
        }
    }

    public class CreateAchFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    String offloadBatchId = getOffloadBatchId(getNormalizedOffloadDate());
                    if (getAchOffloadType().equals(AchOffloadType.TAX_PAYMENT)) {
                        new OffloadACHTransactions().generateFiles(offloadBatchId, ACHFileType.Tax);
                    } else {
                        new OffloadACHTransactions().generateFiles(offloadBatchId, ACHFileType.DD);
                    }

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateAchFiles ", t);
            }
        }
    }

    public class UploadAchFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                new SftpAchFileUpload().upload();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UploadAchFiles ", t);
            }
        }
    }

    public class NotifyAchOffloadComplete extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                String message = buildMessage();

                logger.info(message);
//TODO Marcela
                sendEmail("psp_offload_notify_list", "psp_offload_notify_subject", message);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step NotifyAchOffloadComplete ", t);
            }
        }

        private String buildMessage() {
            List<String> lines = new Vector<String>();
            String message;

            try {
                PayrollServices.beginUnitOfWork();

                DomainEntitySet<NACHAFile> nachaFiles = BatchUtils.getNachaFilesByStatus(NACHAFileStatus.Transmitted);
                String offloadType;

                // set up the message
                switch (getAchOffloadType()) {
                    case PRIMARY:
                        offloadType = "Primary";
                        break;
                    case SCHEDULED:
                        offloadType = "Second";
                        break;
                    case TAX_PAYMENT:
                        offloadType = "Tax Payment";
                        break;
                    default:
                        offloadType = "";
                        break;
                }


                if (nachaFiles.isEmpty()) {
                    message = "There are no ACH transactions to offload for the " + offloadType +
                            " ACH Offload dated: " + getOffloadDate().format("EEE, MMM dd, yyyy");
                    lines.add(message);
                } else {
                    message = offloadType + " ACH Offload file send summary for: " +
                            getOffloadDate().format("EEE, MMM dd, yyyy");

                    lines.add(message);

                    int introLength = message.length(); // get the length of the intro line to create underline
                    message = ""; // reuse message
                    for (int i = 0; i < introLength; ++i) {
                        message += "-";
                    }

                    lines.add(message);

                    // build the details of the message
                    for (NACHAFile nachaFile : nachaFiles) {
                        String credit = "N/A";
                        String debit = "N/A";
                        String fileStatus = "N/A";
                        String fileType = "N/A";
                        String fileId = "N/A";
                        String fileName = "N/A";

                        if (nachaFile.getCreditTxnTotalAmount() != null) {
                            BigDecimal val = SpcfUtils.convertToBigDecimal(nachaFile.getCreditTxnTotalAmount());
                            credit = String.format("$%(,.2f", val);
                        }

                        if (nachaFile.getDebitTxnTotalAmount() != null) {
                            BigDecimal val = SpcfUtils.convertToBigDecimal(nachaFile.getDebitTxnTotalAmount());
                            debit = String.format("$%(,.2f", val);
                        }

                        if (nachaFile.getStatus() != null) {
                            fileStatus = nachaFile.getStatus().toString();
                        }

                        if (nachaFile.getFileType() != null) {
                            fileType = nachaFile.getFileType().toString();
                        }

                        if (nachaFile.getFileIDModifier() != null) {
                            fileId = nachaFile.getFileIDModifier();
                        }

                        if (nachaFile.getFileName() != null) {
                            fileName = new File(nachaFile.getFileName()).getName();
                        }

                        lines.add(""); // add an empty line for delineation
                        lines.add("File Name    : " + fileName);
                        lines.add("File ID      : " + fileId);
                        lines.add("File Type    : " + fileType);
                        lines.add("File Status  : " + fileStatus);
                        lines.add("Total Credits: " + credit);
                        lines.add("Total Debits : " + debit);
                    }
                }
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            // assemble the message body
            message = ""; // reuse message
            for (String line : lines) {
                message += line + BatchUtils.NEWLINE;
            }

            return message;
        }
    }

    public class DownloadDicrFilesDelayPeriod extends BatchJobProcessorStep {
        public void execute() {
            BatchUtils.delay("psp_offload_dicr_file_receive_delay", "15");
        }
    }

    public class DownloadDicrFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                new SftpAchDicrFileDownload().download();

                confirmDownload();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step DownloadDicrFiles ", t);
            }
        }

        private void confirmDownload() {
            try {
                PayrollServices.beginUnitOfWork();

                DomainEntitySet<DICRFile> dicrFiles = BatchUtils.getDicrFilesByStatus(DICRFileStatus.Processed);

                sendEmailForAcknowledgedFiles(dicrFiles);
                sendAlertForUnacknowledgedFiles(dicrFiles);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }

        private void sendEmailForAcknowledgedFiles(DomainEntitySet<DICRFile> pDicrFiles) {
            logger.info("Sending confirmation email for acknowledged ach files");

            if (pDicrFiles.isEmpty()) {
                logger.warn("No dicr file records were found in a Processed state (no confirmation email sent)");
            } else {
                List<String> lines = new Vector<String>();
                String message = null;

                // set up the first line of the message
                switch (getAchOffloadType()) {
                    case PRIMARY:
                        message = "Primary";
                        break;
                    case SCHEDULED:
                        message = "Second";
                        break;
                    case TAX_PAYMENT:
                        message = "Tax Payment";
                        break;
                    default:
                        message = "";
                        break;
                }

                message += " ACH offload file acknowledgement summary for: " +
                        getOffloadDate().format("EEE, MMM dd, yyyy");

                lines.add(message);

                int introLength = message.length(); // get the length of the intro line to create underline
                message = ""; // reuse message
                for (int i = 0; i < introLength; ++i) {
                    message += "-";
                }

                lines.add(message);

                // build the details of the message
                for (DICRFile dicrFile : pDicrFiles) {
                    BigDecimal credit = SpcfUtils.convertToBigDecimal(dicrFile.getCreditTxnTotalAmount());
                    BigDecimal debit = SpcfUtils.convertToBigDecimal(dicrFile.getDebitTxnTotalAmount());

                    lines.add(""); // add an empty line for delineation
                    lines.add("ACH File Name : " + new File(dicrFile.getNACHAFile().getFileName()).getName());
                    lines.add("DICR File Name: " + new File(dicrFile.getFileName()).getName());
                    lines.add("Total Credits : " + String.format("$%(,.2f", credit));
                    lines.add("Total Debits  : " + String.format("$%(,.2f", debit));
                }

                // assemble the message body
                message = ""; // reuse message
                for (String line : lines) {
                    message += line + BatchUtils.NEWLINE;
                }

                logger.info(message);

                sendEmail("psp_offload_notify_list", "psp_offload_notify_subject", message);
            }

            if (!pDicrFiles.isEmpty()) {
            }
        }

        private void sendAlertForUnacknowledgedFiles(DomainEntitySet<DICRFile> pDicrFiles) {
            logger.info("Checking for unacknowledged ach files");

            // At this point, there should be no remaining nacha file records in a PendingAcknowledgement state.
            // If we find any, then this is an error we need to report.
            DomainEntitySet<NACHAFile> nachaFiles =
                    BatchUtils.getNachaFilesByStatus(NACHAFileStatus.PendingAcknowledgement);

            if (nachaFiles.isEmpty()) {
                logger.info("No nacha file records were found in a PendingAcknowledgement state (nominal)");
            } else {
                StringBuffer err = new StringBuffer();
                String offset = ">  ";

                err.append("One or more ACH Acknowledgement (DICR) files failed to download from bank.");
                err.append(BatchUtils.NEWLINE);

                if (!pDicrFiles.isEmpty()) {
                    err.append("The following ACH files have been successfully acknowledged:");
                    err.append(BatchUtils.NEWLINE);

                    for (DICRFile dicrFile : pDicrFiles) {
                        err.append(offset);
                        err.append(new File(dicrFile.getNACHAFile().getFileName()).getName());
                        err.append(" [");
                        err.append(new File(dicrFile.getFileName()).getName());
                        err.append("]");
                        err.append(BatchUtils.NEWLINE);
                    }
                }

                err.append("The following ACH files are still pending acknowledgement:");
                err.append(BatchUtils.NEWLINE);

                for (NACHAFile nachaFile : nachaFiles) {
                    err.append(offset);
                    err.append(new File(nachaFile.getFileName()).getName());
                    err.append(BatchUtils.NEWLINE);
                }

                throw new RuntimeException(err.toString());
            }
        }
    }

    public class ArchiveDailyFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

                    DomainEntitySet<DICRFile> dicrFiles = BatchUtils.getDicrFilesByStatus(DICRFileStatus.Processed);

                    for (DICRFile dicrFile : dicrFiles) {
                        BatchUtils.moveFile(dicrFile.getNACHAFile().getFileName(), archiveDir);
                        BatchUtils.moveFile(dicrFile.getFileName(), archiveDir);

                        dicrFile.setStatus(DICRFileStatus.Archived);
                        dicrFile.getNACHAFile().setStatus(NACHAFileStatus.Archived);
                        dicrFile.getNACHAFile().setStatusEffectiveDate(PSPDate.getPSPTime());

                        Application.save(dicrFile);
                    }

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveDailyFiles ", t);
            }
        }
    }

    public class UpdatePayrollStatus extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                logger.info("About to call prc_offload_update_payroll stored procedure");

                SpcfCalendar normalizedOffloadDate = getNormalizedOffloadDate();

                String offloadBatchId = getOffloadBatchId(normalizedOffloadDate);

                logger.info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPDATE_PAYROLL.getStoredProcedureName() +
                        " offloadBatchId="+offloadBatchId+" normalizedOffloadDate="+normalizedOffloadDate);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPDATE_PAYROLL, true,
                                                Pair.of(String.class, SystemPrincipal.AchOffloadBatchJob.getId()),
                                                Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())),
                                                Pair.of(String.class, offloadBatchId),
                                                Pair.of(Timestamp.class, new Timestamp(normalizedOffloadDate.getTimeInMilliseconds())));

                logger.info("Finished prc_offload_update_payroll stored procedure called for offload_batch_id = " + offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step prc_offload_update_payroll ", t);
            }
        }
    }

    public class InsertFinancialTransactionState extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                logger.info("About to call PRC_OFFLOAD_INSERT_FTS stored procedure");

                SpcfCalendar normalizedOffloadDate = getNormalizedOffloadDate();

                String offloadBatchId = getOffloadBatchId(normalizedOffloadDate);

                logger.info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_INSERT_FTS.getStoredProcedureName() +
                        " offloadBatchId="+offloadBatchId+" normalizedOffloadDate="+normalizedOffloadDate);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_INSERT_FTS, true,
                                                Pair.of(String.class, SystemPrincipal.AchOffloadBatchJob.getId()),
                                                Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())),
                                                Pair.of(String.class, offloadBatchId),
                                                Pair.of(Timestamp.class, new Timestamp(normalizedOffloadDate.getTimeInMilliseconds())));

                logger.info("Finished PRC_OFFLOAD_INSERT_FTS stored procedure returning offload_batch_id = " + offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step InsertFinancialTransactionState ", t);
            }
        }
    }

    public class UpdateMoneyMovementTransaction extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                logger.info("About to call PRC_OFFLOAD_UPDATE_MMT stored procedure");

                SpcfCalendar normalizedOffloadDate = getNormalizedOffloadDate();

                String offloadBatchId = getOffloadBatchId(normalizedOffloadDate);
                ACHFileType achFileType;
                if (getAchOffloadType().equals(AchOffloadType.TAX_PAYMENT)) {
                    achFileType = ACHFileType.Tax;
                } else {
                    achFileType = ACHFileType.DD;
                }

                logger.info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPDATE_MMT.getStoredProcedureName() +
                        " offloadBatchId="+offloadBatchId+" normalizedOffloadDate="+normalizedOffloadDate+" achFileType="+achFileType);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPDATE_MMT, true,
                                                Pair.of(String.class, SystemPrincipal.AchOffloadBatchJob.getId()),
                                                Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                                                Pair.of(String.class, offloadBatchId),
                                                Pair.of(Timestamp.class, new Timestamp(normalizedOffloadDate.getTimeInMilliseconds())),
                                                Pair.of(String.class, achFileType.toString()));

                logger.info("Finished PRC_OFFLOAD_UPDATE_MMT stored procedure returning offload_batch_id = " + offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateMoneyMovementTransaction ", t);
            }
        }
    }

    public class UpdateTaxPaymentsAgencyStatus extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                logger.info("About to call PRC_OFFLOAD_UPD_AGENCY_STATUS stored procedure");

                SpcfCalendar normalizedOffloadDate = getNormalizedOffloadDate();

                String offloadBatchId = getOffloadBatchId(normalizedOffloadDate);
                ACHFileType achFileType;
                if (getAchOffloadType().equals(AchOffloadType.TAX_PAYMENT)) {
                    achFileType = ACHFileType.Tax;

                    logger.info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPD_AGENCY_STATUS.getStoredProcedureName() +
                            " offloadBatchId="+offloadBatchId+" normalizedOffloadDate="+normalizedOffloadDate+" achFileType="+achFileType);
                    Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPD_AGENCY_STATUS, true,
                                                    Pair.of(String.class, SystemPrincipal.AchOffloadBatchJob.getId()),
                                                    Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                                                    Pair.of(String.class, offloadBatchId),
                                                    Pair.of(Timestamp.class, new Timestamp(normalizedOffloadDate.getTimeInMilliseconds())),
                                                    Pair.of(String.class, achFileType.toString()));
                }
                logger.info("Finished PRC_OFFLOAD_UPD_AGENCY_STATUS stored procedure returning offload_batch_id = " + offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateTaxPaymentsAgencyStatus ", t);
            }
        }
    }

    public class UpdateFinancialTransaction extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

                logger.info("About to call PRC_OFFLOAD_UPDATE_FT stored procedure");

                SpcfCalendar normalizedOffloadDate = getNormalizedOffloadDate();

                String offloadBatchId = getOffloadBatchId(normalizedOffloadDate);

                logger.info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD_UPDATE_FT.getStoredProcedureName() +
                        " offloadBatchId="+offloadBatchId+" normalizedOffloadDate="+normalizedOffloadDate);
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD_UPDATE_FT, true,
                                                Pair.of(String.class, SystemPrincipal.AchOffloadBatchJob.getId()),
                                                Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())),
                                                Pair.of(String.class, offloadBatchId),
                                                Pair.of(Timestamp.class, new Timestamp(normalizedOffloadDate.getTimeInMilliseconds())));

                logger.info("Finished PRC_OFFLOAD_UPDATE_FT stored procedure returning offload_batch_id = " + offloadBatchId);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateFinancialTransaction ", t);
            }
        }
    }

    public class CreateTransactionOffloadedEvents extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FeeEventsBatchJob);

                // fee events processor manages its own transaction
                new com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents().createTransactionOffloadedEvents();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateTransactionOffloadedEvents ", t);
            }
        }
    }

    public class MissedPayrollProcessor extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.MissedPayrollsBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    String notificationMessage = new ProcessMissedPayrolls().process(getOffloadDate().format(BatchUtils.DATE_FORMAT));
                    if (StringUtils.isNotEmpty(notificationMessage)) {
                        StringBuilder buf = new StringBuilder();

                        buf.append("The following payrolls have been processed by the Missed Payroll Processor:");
                        buf.append(BatchUtils.NEWLINE).append(BatchUtils.NEWLINE);
                        buf.append(notificationMessage);

                        // send the email to the alert list
                        sendEmail("psp_dd_distlist", "psp_missed_transactions_notifysubject", buf.toString());
                    }

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step MissedPayrollProcessor ", t);
            }
        }
    }

    public class MissedTransactionProcessor extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.MissedAchTransactionsBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    ProcessMissedACHTransactions processor = new ProcessMissedACHTransactions();

                    processor.process(getOffloadDate().format(BatchUtils.DATE_FORMAT));
                    String notificationMessage = processor.getNotificationMessage();
                    String errorMessage = processor.getErrorMessage();

                    PayrollServices.commitUnitOfWork();

                    if (notificationMessage != null) {
                        StringBuffer buf = new StringBuffer();

                        buf.append("The following transactions have been cancelled by the Missed Transaction Processor:");
                        buf.append(BatchUtils.NEWLINE).append(BatchUtils.NEWLINE);
                        buf.append(notificationMessage);

                        // send the email to the alert list
                        sendEmail("psp_dd_distlist", "psp_missed_transactions_notifysubject", buf.toString());
                    }

                    if (errorMessage != null) {
                        StringBuffer buf = new StringBuffer();
                        buf.append(errorMessage);
                        logger.error(buf.toString());
                    }

                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step MissedTransactionProcessor ", t);
            }
        }
    }

    public class GemsAccountsReceivableProcessor extends BatchJobProcessorStep {
        public void execute() {
            // process gems a/r concurrently
            BatchJobManager batchJobManager = new BatchJobManager();
            batchJobManager.scheduleJob(BatchJobType.GemsAccountsReceivable, getOffloadDate().format("yyyyMMdd"));
        }
    }

    /**
     * This check is performed when *second* offload is scheduled.
     * This is a check to see if the other offload finished the InsertFinancialTransactionStat step
     * @return
     */
    public boolean otherOffloadCompletedInsertFinancialTransactionStateStep() {
        SpcfCalendar today = PSPDate.getPSPTime();
        SpcfCalendar fivePM = SpcfCalendar.createInstance(today.getYear(), today.getMonth(), today.getDay(), 17, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar eleven55PM = SpcfCalendar.createInstance(today.getYear(), today.getMonth(), today.getDay(), 23, 55, 0, 0, SpcfTimeZone.getLocalTimeZone());
        BatchJobType theOtherOffloadJob =  (this.getBatchJobType().equals(BatchJobType.PrimaryDailyBatchJobs)) ? BatchJobType.ScheduledDailyBatchJobs :   BatchJobType.PrimaryDailyBatchJobs;
        DomainEntitySet<BatchJobAuditLog> auditLogs = null;
        Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(getJobNamespace(theOtherOffloadJob) + "%")
                                      .And(BatchJobAuditLog.JobAction().equalTo(DailyBatchJobsProcessor.InsertFinancialTransactionState.class.getSimpleName())
                                      .And(BatchJobAuditLog.Message().equalTo("Finished")
                                      .And(BatchJobAuditLog.IsVerified().equalTo(false))));
        where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(fivePM))
                                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(eleven55PM));
        Expression<BatchJobAuditLog> returnsCountQuery =
                        new Query<BatchJobAuditLog>()
                                .Select(BatchJobAuditLog.Id().Count())
                                .Where(where);
        return (Application.executeScalarAggQuery(BatchJobAuditLog.class, returnsCountQuery) > 0) ;
    }
    
    public class ScheduleSalesTaxExceptionProcessor extends BatchJobProcessorStep {
        public void execute() {
            BatchJobManager batchJobManager = new BatchJobManager();
            batchJobManager.scheduleJob(BatchJobType.SalesTaxExceptionProcessor, getOffloadDate().format("yyyyMMdd"));
        }
    }
}
