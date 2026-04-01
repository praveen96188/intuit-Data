package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentFile;
import com.intuit.sbd.payroll.psp.domain.RAFFileStatus;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 15, 2009
 * Time: 8:02:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class SftpRAFFileUpload {
    protected static final SpcfLogger sfLogger;

    // List of raf files to send to as/400. key = full file name, value = associated RAFFile record unique id.
    private Map<String, SpcfUniqueId> mFileMap = new Hashtable<String, SpcfUniqueId>();

    static {
        sfLogger = Application.getLogger(SftpRAFFileUpload.class);
    }

    private class SftpRAFFileUploadListener extends JSchAdapter {
        public void upload(FileBean event) {
            // for each file successfully received, update it's state in the db
            SpcfUniqueId id = mFileMap.get(event.getFilename());

            if (id == null) {
                throw new RuntimeException("Unable to correlate RAF file transmitted to AS/400 with RAF file prefetch data.");
            }

            try {
                PayrollServices.beginUnitOfWork();

                RAFEnrollmentFile rafFile = Application.findById(RAFEnrollmentFile.class, id);

                if (rafFile == null) {
                    throw new RuntimeException("Could not correlate RAF file transmitted to AS/400 with RAF file record in the database.");
                }

                rafFile.setStatus(RAFFileStatus.Transmitted);
                rafFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                Application.save(rafFile);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void upload() {
        sfLogger.info("Attempting to upload RAF files to RN...");

        boolean done = false;

        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "10"));
        int retryCount = 0;

        do {
            // Retrieve the files to upload each time through in case
            // some files succeeded on previous iteration.
            getFilesToUpload();

            if (mFileMap.isEmpty()) {
                sfLogger.warn("No RAF file records found awaiting upload to AS/400.");
                done = true;
            } else {
                Transporter sftp = BatchUtils.getRAFSftpConnection(new SftpRAFFileUploadListener());
                try {
                    sftp.setLogger(sfLogger);

                    sftp.connect();

                    sftp.changeLocalDir(BatchUtils.getConfigString("psp_raf_ftp_srcdir"));
                    sftp.changeRemoteDir(BatchUtils.getConfigString("psp_raf_ftp_destdir"));

                    sftp.uploadFiles(mFileMap.keySet(), 60000); // delay a bit between files

                    done = true;
                } catch (Exception e) {
                    if (retryCount == 0) {
                        sfLogger.error("Error sending RAF files to AS/400 (attempting retry) ", e);
                    } else if (retryCount < maxRetries) {
                        sfLogger.error("Error sending RAF files to AS/400 (retry attempt " + retryCount + ") ", e);
                    } else {
                        throw new RuntimeException("Error sending RAF files to AS/400 (aborting process) ", e);
                    }
                } finally {
                    try {
                        sftp.disconnect();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in disconnecting at RAF upload step ", e);
                    }
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
            DomainEntitySet<RAFEnrollmentFile> rafFileSet =
                    RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Finalized, RAFFileStatus.PendingTransmission);

            for (RAFEnrollmentFile rafFile : rafFileSet) {
                sfLogger.info("Preparing to upload RAF file " + rafFile.getFileName() + " to AS/400...");

                mFileMap.put(new File(rafFile.getFileName()).getName(), rafFile.getId());

                rafFile.setStatus(RAFFileStatus.PendingTransmission);
                rafFile.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(rafFile);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
