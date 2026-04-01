package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.offload.DicrFileProcessor;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.NACHAFile;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.jss.processors.DailyBatchJobsProcessor;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 15, 2009
 * Time: 8:21:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SftpAchDicrFileDownload {
    protected static final SpcfLogger sfLogger;

    // the number of dicr files we are expecting to receive
    private int mExpectedFileCount = 0;

    static {
        sfLogger = Application.getLogger(SftpAchDicrFileDownload.class);
    }

    private class SftpAchDicrFileDownloadListener extends JSchAdapter {
        public void download(FileBean event) {
            File file = new File(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"), event.getFilename());

            try {
                PayrollServices.beginUnitOfWork();

                // processes dicr file and acknowledges associated nacha file
                DicrFileProcessor.processFile(file);

                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                sfLogger.error("Error processing DICR file. ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void download() {
        sfLogger.info("Attempting to download DICR files from bank...");

        boolean done = false;
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "5"));
        int retryCount = 0;

        do {
            // Check for nacha file records that are awaiting acknowledgement
            // (do inside loop in case previous iteration processed some files)
            checkPendingAcknowledgement();

            if (mExpectedFileCount == 0) {
                sfLogger.info("No NACHA file records found pending acknowledgement.");
                done = true;
            } else {
                Transporter sftp = BatchUtils.getBankSftpConnection(new SftpAchDicrFileDownloadListener());
                int fileCount = 0;

                try {
                    sftp.setLogger(sfLogger);

                    sftp.connect();

                    sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"));

                    boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
                    if (enableEncryption) {
                        sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_bank_encrypted_recv_dir"));
                    } else {
                        sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_bank_recv_dir"));
                    }

                    List<String> dicrFiles =
                            sftp.getRemoteDirListing(BatchUtils.getConfigString("psp_offload_ack_file_pattern"));

                    // download the dicr files and then delete them from the remote host
                    // (if anything fails, an exception will be thrown)
                    for (String file : dicrFiles) {
                        sftp.downloadFile(file);
                        sftp.deleteRemoteFile(file);
                        ++fileCount;
                    }

                    done = (fileCount == mExpectedFileCount);

                    // throw an exception to allow the retry.
                    if (!done) {
                        throw new RuntimeException("Not all DICR files received from bank (expected: " +
                                                   mExpectedFileCount + ", found: " + fileCount + ")");
                    }
                } catch (Exception e) {
                    if (retryCount == 0) {
                        sfLogger.error("Error receiving DICR files from bank (attempting retry) ", e);
                    } else if (retryCount < maxRetries) {
                        sfLogger.error("Error receiving DICR files from bank (retry attempt " + retryCount + ") ", e);
                    } else {
                        throw new RuntimeException("Error receiving DICR files from bank (aborting process) ", e);
                    }
                } finally {
                    try {
                        sftp.disconnect();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in disconnecting at Download DICR step ", e);
                    }
                }

                if (!done) {
                    BatchUtils.delay("psp_batch_bank_retry_delay", "2");
                }
            }
        } while (!done && (++retryCount <= maxRetries));
    }

    private void checkPendingAcknowledgement() {
        mExpectedFileCount = 0;

        try {
            PayrollServices.beginUnitOfWork();

            // get list of files to send to bank
            DomainEntitySet<NACHAFile> nachaFileSet =
                    BatchUtils.getNachaFilesByStatus(NACHAFileStatus.Transmitted,
                                                     NACHAFileStatus.PendingAcknowledgement);

            for (NACHAFile nachaFile : nachaFileSet) {
                if(DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFile)){
                    if(NACHAFile.ifNotEligibleForUpload(nachaFile)){
                        continue;
                    }
                    DailyBatchJobsProcessor.updatePSPONachaFile(nachaFile,NACHAFileStatus.Transmitted, NACHAFileStatus.PendingAcknowledgement,false);
                }
                sfLogger.info("Preparing to download DICR for ACH file " + nachaFile.getFileName() + " from bank...");

                nachaFile.setStatus(NACHAFileStatus.PendingAcknowledgement);
                nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(nachaFile);
                //Expected count is now std files only
                mExpectedFileCount++;
            }

           // mExpectedFileCount = nachaFileSet.size();

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
