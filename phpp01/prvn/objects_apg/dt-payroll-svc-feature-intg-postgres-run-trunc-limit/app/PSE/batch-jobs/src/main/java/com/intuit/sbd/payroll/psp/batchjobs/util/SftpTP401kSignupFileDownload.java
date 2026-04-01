package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kBatchStatusCode;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kSignUpBatch;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.List;

public class SftpTP401kSignupFileDownload {
    protected static final SpcfLogger sfLogger;

    private static final String SIGNUP_FILE_PATTERN = "Signup_Batch_Intuit_.+?\\.csv";

    static {
        sfLogger = Application.getLogger(SftpTP401kSignupFileDownload.class);
    }

    private class SftpTP401kSignUpFileDownloadListener extends JSchAdapter {
        public void download(FileBean event) {
            File file = new File(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"), event.getFilename());

            try {
                PayrollServices.beginUnitOfWork();

                ThirdParty401kSignUpBatch tp401kSignUpBatch = new ThirdParty401kSignUpBatch();
                tp401kSignUpBatch.setBatchId(generateNewBatchId());
                tp401kSignUpBatch.setFileName(file.getAbsolutePath());
                tp401kSignUpBatch.setDownloadDate(PSPDate.getPSPTime());
                tp401kSignUpBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
                tp401kSignUpBatch.setDownloadStatusCd(ThirdParty401kBatchStatusCode.Pending);

                Application.save(tp401kSignUpBatch);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void download() {
        sfLogger.info("Attempting to download 401k sign up file(s) from provider...");

        boolean done = false;
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_tp401k_max_retry", "10"));
        int retryCount = 0;

        do {
            Transporter sftp = BatchUtils.get401kSignupSftpConnection(new SftpTP401kSignUpFileDownloadListener());
            try {
                sftp.setLogger(sfLogger);

                sftp.connect();

                sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"));
                sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_tp401k_signup_dir"));

                List<String> signupFiles = sftp.getRemoteDirListing(SIGNUP_FILE_PATTERN);

                // download the files and then delete them from the remote host
                // (if anything fails, an exception will be thrown)
                //check for encryption idps flag
                for (String file : signupFiles) {
                    sftp.downloadFileSecurely(file);
                    sftp.deleteRemoteFile(file);
                }
                done = true;
            } catch (Exception e) {
                if (retryCount == 0) {
                    sfLogger.error("Error receiving 401k signup file from provider (attempting retry) ", e);
                } else if (retryCount < maxRetries) {
                    sfLogger.error("Error receiving 401k signup file from provider (retry attempt " + retryCount + ") ", e);
                } else {
                    throw new RuntimeException("Error receiving 401k signup file from provider (aborting process) ", e);
                }
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException("Error in disconnecting at TP401 upload step ", e);
                }
            }

            if (!done) {
                BatchUtils.delay("psp_batch_tp401k_retry_delay", "2");
            }
        } while (!done && (++retryCount <= maxRetries));
    }

    private int generateNewBatchId() {
        return Application.nextSequenceValue(SequenceId.SEQ_401K_SIGNUP_BATCH_ID, Long.class).intValue();
    }
}
