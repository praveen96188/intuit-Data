package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatch;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatchStatus;
import com.intuit.sbd.payroll.psp.domain.ReportingFrequency;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 15, 2009
 * Time: 9:42:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class SftpGemsFileUpload {
    protected static final SpcfLogger sfLogger;
    public static final String SFTP = "sftp";
    public static final int PORT = 22;

    private final String mSendDir = BatchUtils.getConfigString("psp_batch_ftp_send_dir");
    private final String mDestination = BatchUtils.getConfigString("psp_gems_scp_destination");
    private final String mDestinationBackup = BatchUtils.getConfigString("psp_gems_scp_backup_destination");

    // List of gems files to send to server. key = simple file name, value = associated GEMS uplod batch record unique id.
    private Map<String, SpcfUniqueId> mFileMap = new Hashtable<String, SpcfUniqueId>();

    static {
        sfLogger = Application.getLogger(SftpAchFileUpload.class);
    }

    private class SftpGemsFileUploadListener extends JSchAdapter {
        public void upload(FileBean event) {
            // for each file successfully received, update it's state in the db
            SpcfUniqueId id = mFileMap.get(event.getFilename());

            uploadCompleted(id);
        }
    }

    private class ScpFileUploadListener extends JSchAdapter {
        public void connected(String scpConnectedEvent) {
            sfLogger.info("Successfully connected..");
        }

        public void disconnected(String scpDisconnectedEvent) {
            sfLogger.info("Successfully Disconnected..");
        }

        public void download(FileBean scpFileDownloadedEvent) {
            sfLogger.info("File downloaded:" + scpFileDownloadedEvent.getFilename());
        }

        public void upload(FileBean event) {
            sfLogger.info(String.format("Successfully uploaded (SCP) file: %s .", event.getFilename()));

            // for each file successfully received, update it's state in the db
            SpcfUniqueId id = mFileMap.get(event.getFilename());
            uploadCompleted(id);
        }

    }

    private void uploadCompleted(SpcfUniqueId pId) {
        if (pId == null) {
            throw new RuntimeException("Unable to correlate GEMS file transmitted to server with GEMS upload batch prefetch data.");
        }

        try {
            PayrollServices.beginUnitOfWork();

            GemsUploadBatch batch = Application.findById(GemsUploadBatch.class, pId);

            if (batch == null) {
                throw new RuntimeException("Could not correlate GEMS file transmitted to server with GEMS upload batch record in the database.");
            }

            batch.setUploadStatus(GemsUploadBatchStatus.Transmitted);
            batch.setStatusEffectiveDate(PSPDate.getPSPTime());
            batch.setUploadDate(PSPDate.getPSPTime());

            Application.save(batch);

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public void upload(ReportingFrequency pBatchType) {
        sfLogger.info("Attempting to upload GEMS " +
                ((pBatchType == ReportingFrequency.Daily) ? "A/R" : "G/L") +
                " files to server...");

        boolean done = false;
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_gems_ftp_max_retry", "5"));
        int retryCount = 0;

        do {
            // Retrieve the files to upload each time through in case
            // some files succeeded on previous interation.
            getFilesToUpload(pBatchType);

            if (mFileMap.isEmpty()) {
                sfLogger.info("No GEMS upload batch records found awaiting upload to server.");
                done = true;
            } else {

                Transporter sftp = BatchUtils.getGemsConnection(new JSchAdapter());
                sfLogger.info("Using JSch SFTP to upload GEMS files");
                try {
                    sftp.connect();
                    sftp.changeLocalDir(mSendDir);

                    for (Map.Entry<String, SpcfUniqueId> fileEntry : mFileMap.entrySet()) {

                        sftp.changeRemoteDir(mDestination);
                        sftp.uploadFile(fileEntry.getKey());

                        if (mDestinationBackup != null) {
                            sftp.changeRemoteDir(mDestinationBackup);
                            sftp.uploadFile(fileEntry.getKey());

                        }

                        //When we were using JEscape ScpFileUploadListener.upload will get triggered
                        // for every file successfully uploaded, to mirror that here (since we dont have the
                        // listener) we need to call this method after every successful upload, we didnt get an
                        // exception so now we should have successfully uploaded the file.
                        // for each file successfully received, update it's state in the db
                        uploadCompleted(fileEntry.getValue());
                    }

                    done = true;
                } catch (Exception e) {
                    if (retryCount == 0) {
                        sfLogger.error("Error sending GEMS files to server (attempting retry) ", e);
                    } else if (retryCount < maxRetries) {
                        sfLogger.error("Error sending GEMS files to server (retry attempt " + retryCount + ") ", e);
                    } else {
                        throw new RuntimeException("Error sending GEMS files to server (aborting process) ", e);
                    }
                } finally {
                    if (sftp != null) {
                        try {
                            sftp.disconnect();
                        } catch (Exception e) {
                            sfLogger.error("Error is Disconnecting at Gems Upload step", e);
                        }
                    }
                }

                if (!done) {
                    BatchUtils.delay("psp_gems_ftp_retry_delay", "2");
                }
            }
        } while (!done && (++retryCount <= maxRetries));
    }

    private void getFilesToUpload(ReportingFrequency pBatchType) {
        try {
            // clear the file -> id map in case this isn't the first time through
            mFileMap.clear();

            PayrollServices.beginUnitOfWork();

            // get list of files to send to bank
            DomainEntitySet<GemsUploadBatch> batchSet =
                    BatchUtils.getGemsUploadFilesByStatus(pBatchType,
                            GemsUploadBatchStatus.Finalized,
                            GemsUploadBatchStatus.PendingTransmission);

            for (GemsUploadBatch batch : batchSet) {
                sfLogger.info("Preparing to upload GEMS " +
                        ((pBatchType == ReportingFrequency.Daily) ? "A/R" : "G/L") +
                        " file " + batch.getFileName() + " to server...");

                mFileMap.put(new File(batch.getFileName()).getName(), batch.getId());

                batch.setUploadStatus(GemsUploadBatchStatus.PendingTransmission);
                batch.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(batch);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
