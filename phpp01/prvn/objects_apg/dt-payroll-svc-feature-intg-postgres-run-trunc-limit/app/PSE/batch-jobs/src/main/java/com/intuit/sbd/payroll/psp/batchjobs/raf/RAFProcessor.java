package com.intuit.sbd.payroll.psp.batchjobs.raf;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentFile;
import com.intuit.sbd.payroll.psp.domain.RAFFileStatus;
import com.intuit.sbd.payroll.psp.gateways.efe.EfeGateway;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.Application;

import java.io.File;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Mar 24, 2009
 * Time: 9:24:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class RAFProcessor extends BatchJobProcessor {

    public RAFProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    public void validateRuntimeParameters() {
        String parameters = getJobInstanceParameters();
    }

    @Override
    public void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        logger.info("Starting RAFFileWriter");
        timer = StopWatch.startTimer();
        executeStep(new RAFFileWriterStep());
        logger.info("Completed RAFFileWriter: " + timer.stop().getElapsedTimeString());

        logger.info("Starting RAFEmailWriter");
        timer = StopWatch.startTimer();
        executeStep(new RAFEmailWriterStep());
        logger.info("Completed RAFEmailWriter: " + timer.stop().getElapsedTimeString());

        logger.info("Starting RAFFileSendStep");
        timer = StopWatch.startTimer();
        executeStep(new RAFFileSendStep());
        logger.info("Completed RAFFileSendStep: " + timer.stop().getElapsedTimeString());

        logger.info("Starting RAFEmailStep");
        timer = StopWatch.startTimer();
        executeStep(new RAFEmailStep());
        logger.info("Completed RAFEmailStep: " + timer.stop().getElapsedTimeString());

        logger.info("Starting RAFArchiveStep");
        timer = StopWatch.startTimer();
        executeStep(new RAFArchiveStep());
        logger.info("Completed RAFArchiveStep: " + timer.stop().getElapsedTimeString());
        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class RAFFileWriterStep extends BatchJobProcessorStep {
        @Override
        public void execute() {
            RAFFileWriter rafWriter = new RAFFileWriter();
            rafWriter.execute();
        }
    }

    public class RAFEmailWriterStep extends BatchJobProcessorStep {
        @Override
        public void execute() {
            RAFEmailWriter rafEmailWriter = new RAFEmailWriter(logger);
            rafEmailWriter.execute();
        }
    }

    public class RAFFileSendStep extends BatchJobProcessorStep {
        @Override
        public void execute() {
            try {
                String senderId = BatchUtils.getConfigString("psp_efe_raf_enrollment_senderid");
                String senderAuthCode = BatchUtils.getConfigString("psp_efe_raf_enrollment_authcode");
                String senderEmail = BatchUtils.getConfigString("psp_efe_raf_enrollment_notification_email");

                PayrollServices.setCurrentPrincipal(SystemPrincipal.RAFProcessorBatchJob);
                PayrollServices.beginUnitOfWork();

                for (RAFEnrollmentFile rafFileRecord : RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Finalized, RAFFileStatus.PendingTransmission)) {
                    // validate file
                    File rafFile = new File(rafFileRecord.getFileName());
                    if (!rafFile.exists() || !rafFile.canRead()) {
                        rafFileRecord.setStatus(RAFFileStatus.Error);
                        rafFileRecord.setStatusEffectiveDate(PSPDate.getPSPTime());
                        Application.save(rafFileRecord);
                        logger.error("Skipping send of RAF file " + rafFileRecord.getFileName() + " - file doesn't exist or can't be read.");
                        continue;
                    }

                    // send file
                    logger.info("sending RAF file " + rafFile.getName() + " to EFE...");
                    try {
                        StopWatch sw = StopWatch.startTimer();
                        EfeGateway efeGateway = EfeGateway.getInstance();
                        efeGateway.sendRAFEnrollmentFile(rafFile, senderId, senderAuthCode, senderEmail);
                        logger.info("file send completed in " + sw.getElapsedTimeString());
                        rafFileRecord.setStatus(RAFFileStatus.Transmitted);
                        rafFileRecord.setStatusEffectiveDate(PSPDate.getPSPTime());
                    } catch (Throwable t) {
                        logger.error("RAF Enrollment file send error for file " + rafFile.getName() + " - resend attempt will occur on next job run (or Expedite job again.)", t);
                    }
                    Application.save(rafFileRecord);
                }

                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step RAFSendFile ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class RAFEmailStep extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<RAFEnrollmentFile> transmittedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Transmitted);
            for (RAFEnrollmentFile currentFile : transmittedEnrollmentFiles) {
                File emailFile = new File(currentFile.getEmailFileName());
                if (!emailFile.exists() || !emailFile.canRead()) {
                    logger.error("Skipping EMAIL of RAF file " + currentFile.getFileName() + " - file doesn't exist or can't be read.");
                    continue;
                }

                MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                        BatchUtils.getConfigString("psp_raf_email_list"),
                        BatchUtils.getConfigString("psp_raf_email_list"),
                        BatchUtils.getConfigString("psp_raf_email_subject"),
                        "RAF Tape List for "+currentFile.getRAFActionCode()+" Enrollment File",
                        currentFile.getEmailFileName());
                currentFile.setStatus(RAFFileStatus.Emailed);
                currentFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            }
            PayrollServices.commitUnitOfWork();
        }
    }

    public class RAFArchiveStep extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<RAFEnrollmentFile> emailedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Emailed);
            for (RAFEnrollmentFile currentFile : emailedEnrollmentFiles) {
                String archiveDir = BatchUtils.getConfigString("psp_raf_ftp_archdir");
                File archiveRafFile = archive(archiveDir, currentFile.getFileName());
                File archiveEmailFile = archive(archiveDir, currentFile.getEmailFileName());
                currentFile.setFileName(archiveRafFile.getAbsolutePath());
                currentFile.setEmailFileName(archiveEmailFile.getAbsolutePath());
                currentFile.setStatus(RAFFileStatus.Completed);
            }
            PayrollServices.commitUnitOfWork();
        }
    }

    public static File archive(String pArchiveDir, String pFileName) {
        if (pArchiveDir == null || pArchiveDir.trim().length() == 0) {
            pArchiveDir = new File(".").getAbsolutePath();
        }

        File archiveDir = new File(pArchiveDir.trim());
        if (!archiveDir.exists()) {
            throw new RuntimeException(archiveDir.getAbsolutePath() + " does not exist. Unable to archive " + pFileName);
        }

        // if file is already in archive dir (?)
        if (archiveDir.getPath().equals(new File(pFileName).getParentFile().getPath())) {
            return new File(pFileName);
        }

        return BatchUtils.moveFile(pFileName, pArchiveDir);
    }
    /*
    public static void main(String[] args) {
        RAFProcessor processor = new RAFProcessor(RunMode.NotUsingFlux, BatchJobType.RAFWriter, "12388", null);
        processor.executeJob();
    }
    */
}
