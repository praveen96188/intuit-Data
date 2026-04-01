package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpGemsFileUpload;
import com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload.MonthlyGemsUploadBatchProcess;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.ReportingFrequency;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatch;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatchStatus;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 15, 2009
 * Time: 2:58:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GemsGeneralLedgerProcessor extends BatchJobProcessor {
    public enum Commands { file, gen, regen }

    private Commands mCommand;
    private String mReportingPeriod = null;
    private String mBatchId = null;

    public GemsGeneralLedgerProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    private String getUsage() {
        return "Usage: gen <yyyyMM> | file <batch-id> | regen <batch-id> ";
    }

    protected Commands getCommand() {
        return mCommand;
    }

    protected String getReportingPeriod() {
        return mReportingPeriod;
    }

    protected String getBatchId() {
        return mBatchId;
    }

    protected void validateRuntimeParameters() {
        // getJobInstanceParameters() will never return null
        String commandLine = getJobInstanceParameters().trim();

        if ((getRunMode() == RunMode.UsingFlux) && (commandLine.length() == 0)) {
            // if running from scheduler with no command line params,
            // default to command 'gen' and derive processing date
            SpcfCalendar date;

            try {
                Application.beginUnitOfWork();

                date = PSPDate.getPSPTime();
                date.addMonths(-1);
            } finally {
                Application.rollbackUnitOfWork();
            }

            mCommand = Commands.gen;
            mReportingPeriod = date.format("yyyyMM");
        } else {
            logger.info("Validating GemsGeneralLedgerProcessor command line: " + commandLine);

            String[] args = getJobInstanceParameters().trim().split(" ");

            if ((args.length != 2) || (args[0].length() == 0) || (args[1].length() == 0)) {
                throw new RuntimeException("Wrong number of parameters. " + getUsage());
            }

            try {
                mCommand = Commands.valueOf(args[0]);
            } catch (Exception e) {
                throw new RuntimeException("Invalid command specified. " + getUsage());
            }

            if (mCommand.equals(Commands.gen)) {
                // next arg must be reporting period formatted as yyyyMM
                if (!args[1].matches(BatchUtils.VALIDYYYYMM)) {
                    throw new RuntimeException("Invalid reporting period specified. " + getUsage());
                }

                mReportingPeriod = args[1];
            } else { // command is file or regen
                // next arg must be batch id
                mBatchId = args[1];
            }
        }
    }

    protected void validateStepRuntimeParameters(String stepName) {
        if (GenerateGemsGeneralLedgerData.class.getSimpleName().equals(stepName)) {
            validateRuntimeParameters();

            if ((getCommand() == null) || (!getCommand().equals(Commands.gen) && !getCommand().equals(Commands.regen))) {
                throw new RuntimeException("Invalid command specified. Valid commands for step " + stepName +
                                           " are: gen <yyyyMM> | regen <batch-id>");
            }
        } else if (CreateGemsGeneralLedgerFile.class.getSimpleName().equals(stepName)) {
            validateRuntimeParameters();

            if ((getCommand() == null) || !getCommand().equals(Commands.file)) {
                throw new RuntimeException("Invalid command specified. Valid commands for step " + stepName +
                                           " are: file <batch-id>");
            }
        } else if (UploadGemsGeneralLedgerFile.class.getSimpleName().equals(stepName)) {
            // no validation
        } else if (NotifyGemsGeneralLedgerFileUploaded.class.getSimpleName().equals(stepName)) {
            // no validation
        } else if (ArchiveGemsGeneralLedgerFile.class.getSimpleName().equals(stepName)) {
            // no validation
        } else {
            StringBuffer err = new StringBuffer();

            err.append("The specified job step \"").
                append(stepName).
                append("\" does not exist in batch processor ").
                append(this.getClass().getSimpleName()).
                append(". The valid steps (with optional arguments) that can be executed are {").
                append(GenerateGemsGeneralLedgerData.class.getSimpleName()).append(" [gen <yyyyMM> | regen <batch-id>], ").
                append(CreateGemsGeneralLedgerFile.class.getSimpleName()).append(" [file <batch-id>], ").
                append(UploadGemsGeneralLedgerFile.class.getSimpleName()).append(", ").
                append(NotifyGemsGeneralLedgerFileUploaded.class.getSimpleName()).append(", ").
                append(ArchiveGemsGeneralLedgerFile.class.getSimpleName()).append("}");

            throw new RuntimeException(err.toString());
        }
    }
    
    protected void execute() {
        logger.info("Starting GEMS G/L batch job (command: " + getCommand().toString() + ")");
        StopWatch timer = StopWatch.startTimer();

        switch (getCommand()) {
            case gen:
            case regen:
                executeStep(new GenerateGemsGeneralLedgerData());
                break;

            default: // file
                executeStep(new CreateGemsGeneralLedgerFile());
                executeStep(new UploadGemsGeneralLedgerFile());
                executeStep(new NotifyGemsGeneralLedgerFileUploaded());
                executeStep(new ArchiveGemsGeneralLedgerFile());
                break;
        }

        logger.info("Completed GEMS G/L batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Job Steps
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class GenerateGemsGeneralLedgerData extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsGeneralLedgerBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new MonthlyGemsUploadBatchProcess().process(getCommand().toString(),
                                                                getReportingPeriod(),
                                                                getBatchId());

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step GenerateGemsGeneralLedgerData ", t);
            }
        }
    }

    public class CreateGemsGeneralLedgerFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsGeneralLedgerBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new MonthlyGemsUploadBatchProcess().process(getCommand().toString(),
                                                                getReportingPeriod(),
                                                                getBatchId());

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateGemsGeneralLedgerFile ", t);
            }
        }
    }

    public class UploadGemsGeneralLedgerFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsGeneralLedgerBatchJob);

                new SftpGemsFileUpload().upload(ReportingFrequency.Monthly);
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UploadGemsGeneralLedgerFile ", t);
            }
        }
    }

    public class NotifyGemsGeneralLedgerFileUploaded extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsGeneralLedgerBatchJob);

                String fileNames = "";

                try {
                    PayrollServices.beginUnitOfWork();

                    DomainEntitySet<GemsUploadBatch> batchSet =
                            BatchUtils.getGemsUploadFilesByStatus(ReportingFrequency.Monthly,
                                                                  GemsUploadBatchStatus.Transmitted);

                    for (GemsUploadBatch batch : batchSet) {
                        fileNames += new File(batch.getFileName()).getName() + BatchUtils.NEWLINE;
                    }
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }

                if (fileNames.length() == 0) {
                    logger.info("No GEMS upload batch records found in Transmitted state (notification skipped).");
                } else {
                    StringBuffer message = new StringBuffer();

                    message.append("The following monthly GL upload files are ready for GEMS Processing:");
                    message.append(BatchUtils.NEWLINE).append(BatchUtils.NEWLINE);
                    message.append(fileNames).append(BatchUtils.NEWLINE);
                    message.append("<EOM>");

                    logger.info(message.toString());

                    //
                    // Don't fail the overall batch job if there is an error sending email,
                    // just report the error in the log.
                    //
                    try {
                        MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                                             BatchUtils.getConfigString("psp_gems_monthly_notify_list"), // to
                                             BatchUtils.getConfigString("psp_gems_monthly_notify_list"), // from
                                             BatchUtils.getConfigString("psp_gems_monthly_notify_subject"),
                                             message.toString());
                    } catch (Exception e) {
                        logger.error("Failed to send email for GEMS G/L batch job. Email message was: " +
                                     message.toString(), e);
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step NotifyGemsGeneralLedgerFileUploaded ", t);
            }
        }
    }

    public class ArchiveGemsGeneralLedgerFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.GemsGeneralLedgerBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

                    DomainEntitySet<GemsUploadBatch> batchSet =
                            BatchUtils.getGemsUploadFilesByStatus(ReportingFrequency.Monthly,
                                                                  GemsUploadBatchStatus.Transmitted);

                    for (GemsUploadBatch batch : batchSet) {
                        BatchUtils.moveFile(batch.getFileName(), archiveDir);

                        batch.setUploadStatus(GemsUploadBatchStatus.Archived);
                        batch.setStatusEffectiveDate(PSPDate.getPSPTime());

                        Application.save(batch);
                    }

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveGemsGeneralLedgerFile ", t);
            }
        }
    }
}
