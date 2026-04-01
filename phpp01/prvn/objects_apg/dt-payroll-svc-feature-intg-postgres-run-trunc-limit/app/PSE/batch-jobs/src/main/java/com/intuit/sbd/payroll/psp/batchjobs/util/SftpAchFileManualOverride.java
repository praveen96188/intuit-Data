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
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 21, 2010
 * Time: 12:02:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class SftpAchFileManualOverride {
    protected static final SpcfLogger sfLogger;

    // List of nacha files to send to bank. key = simple file name, value = associated NACHAFile record unique id.
    private Map<String, SpcfUniqueId> mFileMap = new Hashtable<String, SpcfUniqueId>();

    static {
        sfLogger = Application.getLogger(SftpAchFileManualOverride.class);
    }

    private class SftpAchFileManualOverrideListener extends JSchAdapter {
        public void upload(FileBean event) {
            // for each file successfully uploaded, update it's state in the db
            SpcfUniqueId id = mFileMap.get(event.getFilename());

            if (id == null) {
                throw new RuntimeException("Unable to correlate ACH file sent from BOS with NACHA file prefetch data.");
            }

            try {
                PayrollServices.beginUnitOfWork();

                NACHAFile nachaFile = Application.findById(NACHAFile.class, id);

                if (nachaFile == null) {
                    throw new RuntimeException("Unable to correlate ACH file sent from BOS with NACHA file record in the database.");
                }

                nachaFile.setStatus(NACHAFileStatus.Transmitted);
                nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                nachaFile.setTransmissionDate(PSPDate.getPSPTime());

                Application.save(nachaFile);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void upload() {
        sfLogger.info("Attempting to send ACH files from BOS to alternate server...");

        boolean done = false;
        int maxRetries = 5;
        int retryCount = 0;

        do {
            // Retrieve the files to upload each time through in case
            // some files succeeded on previous interation.
            getFilesToUpload();

            if (mFileMap.isEmpty()) {
                sfLogger.warn("No NACHA file records found awaiting transmission.");
                done = true;
            } else {
                Transporter sftp = BatchUtils.getJschConnection(BatchUtils.getConfigString("psp_batch_alt_ach_server"),
                        BatchUtils.getConfigString("psp_batch_alt_ach_username"),
                        BatchUtils.getConfigString("psp_batch_alt_ach_password"),
                        false, new SftpAchFileManualOverrideListener(),
                        false, 10000);

                try {
                    // set the logger for this connection
                    sftp.setLogger(sfLogger);

                    // establish the connection
                    sftp.connect();

                    // change to the appropriate local dir for the source files
                    sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_send_dir"));

                    // send the files
                    sftp.uploadFiles(mFileMap.keySet(), 1000); // delay 1s between files

                    done = true;
                } catch (Exception e) {
                    if (retryCount == 0) {
                        sfLogger.error("Error sending ACH files from BOS (attempting retry) ", e);
                    } else if (retryCount < maxRetries) {
                        sfLogger.error("Error sending ACH files from BOS (retry attempt " + retryCount + ") ", e);
                    } else {
                        throw new RuntimeException("Error sending ACH files from BOS (aborting process) ", e);
                    }
                } finally {
                    try {
                        sftp.disconnect();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in disconnecting at ACH upload step ", e);
                    }
                }

                if (!done) {
                    BatchUtils.delay(1000);
                }
            }
        } while (!done && (++retryCount <= maxRetries));
    }

    private void getFilesToUpload() {
        try {
            // clear the file -> id map in case this isn't the first time through
            mFileMap.clear();

            PayrollServices.beginUnitOfWork();

            // get list of files to send
            DomainEntitySet<NACHAFile> nachaFileSet =
                    BatchUtils.getNachaFilesByStatus(NACHAFileStatus.Finalized,
                                                     NACHAFileStatus.PendingTransmission);

            for (NACHAFile nachaFile : nachaFileSet) {
                sfLogger.info("Preparing to send ACH file " + nachaFile.getFileName() +
                              " from BOS to alternate ACH upload server...");

                mFileMap.put(new File(nachaFile.getFileName()).getName(), nachaFile.getId());

                nachaFile.setStatus(NACHAFileStatus.PendingTransmission);
                nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(nachaFile);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
