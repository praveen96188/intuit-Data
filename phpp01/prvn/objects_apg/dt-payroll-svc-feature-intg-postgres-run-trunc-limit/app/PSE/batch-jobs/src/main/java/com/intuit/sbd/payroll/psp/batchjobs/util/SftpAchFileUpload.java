package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.NACHAFile;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.jss.processors.DailyBatchJobsProcessor;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 15, 2009
 * Time: 8:02:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class SftpAchFileUpload {
    protected static final SpcfLogger sfLogger;

    // List of nacha files to send to bank. key = simple file name, value = associated NACHAFile record unique id.
    private Map<String, SpcfUniqueId> mFileMap = new Hashtable<String, SpcfUniqueId>();

    static {
        sfLogger = Application.getLogger(SftpAchFileUpload.class);
    }

    private class SftpAchFileUploadListener extends JSchAdapter {
        public void upload(FileBean event) {
            // for each file successfully received, update it's state in the db
            SpcfUniqueId id = mFileMap.get(event.getFilename());

            if (id == null) {
                throw new RuntimeException("Unable to correlate ACH file transmitted to bank with NACHA file prefetch data.");
            }

            try {
                PayrollServices.beginUnitOfWork();

                NACHAFile nachaFile = Application.findById(NACHAFile.class, id);

                if (nachaFile == null) {
                    throw new RuntimeException("Could not correlate ACH file transmitted to bank with NACHA file record in the database.");
                }

                nachaFile.setStatus(NACHAFileStatus.Transmitted);
                nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                nachaFile.setTransmissionDate(PSPDate.getPSPTime());

                if(DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFile)){
                    DailyBatchJobsProcessor.updatePSPONachaFile(nachaFile,NACHAFileStatus.PendingTransmission, NACHAFileStatus.Transmitted,true);
                }

                Application.save(nachaFile);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void upload() {
        sfLogger.info("Attempting to upload ACH files to bank...");

        boolean done = false;

        //PSRV001522 Change the retries from 5 to 10
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "10"));
        int retryCount = 0;

        do {
            // Retrieve the files to upload each time through in case
            // some files succeeded on previous interation.
            getFilesToUpload();

            if (mFileMap.isEmpty()) {
                sfLogger.warn("No NACHA file records found awaiting upload to bank.");
                done = true;
            } else {
                Transporter sftp = BatchUtils.getBankSftpConnection(new SftpAchFileUploadListener());

                try {
                    sftp.setLogger(sfLogger);

                    sftp.connect();

                    sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_send_dir"));

                    boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
                    if (enableEncryption) {
                        sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_bank_encrypted_send_dir"));
                    } else {
                        sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_bank_send_dir"));
                    }

                    // PSRV001428 - changed delay between file uploads from 60s to 5m to avoid ACK files getting overridden.
                    int nachaFileUploadDelay = SystemParameter.findIntValue(SystemParameter.Code.JPMC_NACHA_FILE_UPLOAD_DELAY, 300000);
                    sftp.uploadFiles(mFileMap.keySet(), nachaFileUploadDelay); // at bank's request, delay a bit between files

                    done = true;
                } catch (Exception e) {
                    if (retryCount == 0) {
                        sfLogger.error("Error sending ACH files to bank (attempting retry) ", e);
                    } else if (retryCount < maxRetries) {
                        sfLogger.error("Error sending ACH files to bank (retry attempt " + retryCount + ") ", e);
                    } else {
                        throw new RuntimeException("Error sending ACH files to bank (aborting process) ", e);
                    }
                } finally {
                    try {
                        sftp.disconnect();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in disconnecting at ACH upload step ", e);
                    }
                }

                if (!done) {
                    //PSRV001522 Change the delay from 2 mins to 1
                    BatchUtils.delay("psp_batch_bank_retry_delay", "1");
                }
            }
        } while (!done && (++retryCount <= maxRetries));
    }

    private void getFilesToUpload() {
        try {
            // clear the file -> id map in case this isn't the first time through
            mFileMap.clear();

            PayrollServices.beginUnitOfWork();

            // get list of files to send to bank
            DomainEntitySet<NACHAFile> nachaFileSet =
                    BatchUtils.getNachaFilesByStatus(NACHAFileStatus.Finalized,
                            NACHAFileStatus.PendingTransmission);

            for (NACHAFile nachaFile : nachaFileSet) {
                if(DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFile)){
                    if(NACHAFile.ifNotEligibleForUpload(nachaFile)){
                        continue;
                    }
                    DailyBatchJobsProcessor.updatePSPONachaFile(nachaFile,NACHAFileStatus.Finalized, NACHAFileStatus.PendingTransmission,false);
                }
                nachaFile.setStatus(NACHAFileStatus.PendingTransmission);
                nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                sfLogger.info("Preparing to upload ACH file " + nachaFile.getFileName() + " to bank...");

                mFileMap.put(new File(nachaFile.getFileName()).getName(), nachaFile.getId());

                Application.save(nachaFile);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
