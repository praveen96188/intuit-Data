package com.intuit.sbd.payroll.psp.jss.raf;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.raf.RAFEmailWriter;
import com.intuit.sbd.payroll.psp.batchjobs.raf.RAFFileWriter;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentFile;
import com.intuit.sbd.payroll.psp.domain.RAFFileStatus;
import com.intuit.sbd.payroll.psp.gateways.efe.EfeGateway;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Mar 24, 2009
 * Time: 9:24:01 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "RAFWriter", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class RAFProcessor extends JSSBatchJob {

    public RAFProcessor(String[] pArguments) {
        super(pArguments);
    }

    public RAFProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    public void validateRuntimeParameters() {
        String parameters = getJobInstanceParameters();
    }

    @Override
    public void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        getLogger().info("Starting RAFFileWriter");
        timer = StopWatch.startTimer();
        executeStep(RAFFileWriterStep.class);
        getLogger().info("Completed RAFFileWriter: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting RAFEmailWriter");
        timer = StopWatch.startTimer();
        executeStep(RAFEmailWriterStep.class);
        getLogger().info("Completed RAFEmailWriter: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting RAFFileSendStep");
        timer = StopWatch.startTimer();
        executeStep(RAFFileSendStep.class);
        getLogger().info("Completed RAFFileSendStep: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting RAFEmailStep");
        timer = StopWatch.startTimer();
        executeStep(RAFEmailStep.class);
        getLogger().info("Completed RAFEmailStep: " + timer.stop().getElapsedTimeString());

        getLogger().info("Starting RAFArchiveStep");
        timer = StopWatch.startTimer();
        executeStep(RAFArchiveStep.class);
        getLogger().info("Completed RAFArchiveStep: " + timer.stop().getElapsedTimeString());
        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class RAFFileWriterStep extends JSSBatchJobStep<RAFProcessor> {
        @Override
        public void execute() {
            RAFFileWriter rafWriter = new RAFFileWriter();
            rafWriter.execute();
        }
    }

    public static class RAFEmailWriterStep extends JSSBatchJobStep<RAFProcessor> {
        @Override
        public void execute() {
            RAFEmailWriter rafEmailWriter = new RAFEmailWriter();
            rafEmailWriter.execute();
        }
    }

    public static class RAFFileSendStep extends JSSBatchJobStep<RAFProcessor> {
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
                        getLogger().error("Skipping send of RAF file " + rafFileRecord.getFileName() + " - file doesn't exist or can't be read.");
                        continue;
                    }

                    // send file
                    getLogger().info("sending RAF file " + rafFile.getName() + " to EFE...");
                    try {
                        StopWatch sw = StopWatch.startTimer();
                        EfeGateway efeGateway = EfeGateway.getInstance();
                        efeGateway.sendRAFEnrollmentFile(rafFile, senderId, senderAuthCode, senderEmail);
                        getLogger().info("file send completed in " + sw.getElapsedTimeString());
                        rafFileRecord.setStatus(RAFFileStatus.Transmitted);
                        rafFileRecord.setStatusEffectiveDate(PSPDate.getPSPTime());
                    } catch (Throwable t) {
                        getLogger().error("RAF Enrollment file send error for file " + rafFile.getName() + " - resend attempt will occur on next job run (or Expedite job again.)", t);
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

    public static class RAFEmailStep extends JSSBatchJobStep<RAFProcessor> {
        @Override
        public void execute() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<RAFEnrollmentFile> transmittedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Transmitted);
            for (RAFEnrollmentFile currentFile : transmittedEnrollmentFiles) {
                File emailFile = new File(currentFile.getEmailFileName());
                if (!emailFile.exists() || !emailFile.canRead()) {
                    getLogger().error("Skipping EMAIL of RAF file " + currentFile.getFileName() + " - file doesn't exist or can't be read.");
                    continue;
                }
                //decrypt file in case its encrypted
                if(StreamUtil.isFileIDPSEncrypted(emailFile)){
                    String tempFileName = StreamUtil.createDecryptedFileForEmail(currentFile.getEmailFileName());
                    MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                            BatchUtils.getConfigString("psp_raf_email_list"),
                            BatchUtils.getConfigString("psp_raf_email_list"),
                            BatchUtils.getConfigString("psp_raf_email_subject"),
                            "RAF Tape List for " + currentFile.getRAFActionCode() + " Enrollment File",
                            tempFileName);
                    try{
                        FileUtils.forceDelete(new File(tempFileName));
                    }catch(Exception e){
                        getLogger().error("Failed to delete file:" + tempFileName );
                    }
                }
                else {
                    MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                            BatchUtils.getConfigString("psp_raf_email_list"),
                            BatchUtils.getConfigString("psp_raf_email_list"),
                            BatchUtils.getConfigString("psp_raf_email_subject"),
                            "RAF Tape List for " + currentFile.getRAFActionCode() + " Enrollment File",
                            currentFile.getEmailFileName());
                }
                currentFile.setStatus(RAFFileStatus.Emailed);
                currentFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            }
            PayrollServices.commitUnitOfWork();
        }
    }

    public static class RAFArchiveStep extends JSSBatchJobStep<RAFProcessor> {
        @Override
        public void execute() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<RAFEnrollmentFile> emailedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Emailed);
            String archiveDir = BatchUtils.getConfigString("psp_raf_ftp_archdir");
            String batchJobName = BatchJobType.RAFWriter.name();
            for (RAFEnrollmentFile currentFile : emailedEnrollmentFiles) {
                try{
                    currentFile.setFileName(S3UploadUtils.archive(batchJobName,archiveDir,currentFile.getFileName()));
                    currentFile.setEmailFileName(S3UploadUtils.archive(batchJobName,archiveDir,currentFile.getEmailFileName()));
                }catch (Throwable t) {
                    throw new RuntimeException("Exception in job step RAFArchiveStep ", t);
                }
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
