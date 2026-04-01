package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SftpAchTraceFileDownload {
    protected static final SpcfLogger sfLogger;

    static {
        sfLogger = Application.getLogger(SftpAchTraceFileDownload.class);
    }

    class SftpAchTraceFileDownloadListener extends JSchAdapter {
        @Override
        public void download(FileBean val) {
            File file = new File(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"), val.getFilename());

        }
    }


    public List<String> download() {
        sfLogger.info("Attempting to download ACH Trace file(s) from bank...");

        boolean done = false;
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "5"));
        int retryCount = 0;
        List<String> traceFileNames = new ArrayList<>();

        do {
            Transporter sftp = BatchUtils.getBankSftpConnection(getSftpAchTraceFileDownloadListener());
            try {
                sftp.setLogger(sfLogger);

                sftp.connect();

                sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"));

                // Preprod, the file needs to be unencrypted and file is always encrypted in PROD
                sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_bank_encrypted_ach_trace_recv_dir"));

                List<String> bankTraceFiles = sftp.getRemoteDirListing(BatchUtils.getConfigString("psp_trace_file_pattern"));

                // download the files and then delete them from the remote host
                // (if anything fails, an exception will be thrown)
                for (String file : bankTraceFiles) {
                    traceFileNames.add(file);
                    sftp.downloadFile(file);
                    try {
                        sftp.deleteRemoteFile(file);
                    } catch (SftpException sftpException) {
                        sfLogger.error("Error deleting ACH Trace file from bank. Possibly, it is already deleted.", sftpException);
                    }
                }

                done = true;
            } catch (Exception e) {
                if (retryCount == 0) {
                    sfLogger.error("Error receiving ACH Trace file from bank (attempting retry) ", e);
                } else if (retryCount < maxRetries) {
                    sfLogger.error("Error receiving ACH Trace file from bank (retry attempt " + retryCount + ") ", e);
                } else {
                    throw new RuntimeException("Error receiving ACH Trace file from bank (aborting process) ", e);
                }
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException("Error in disconnecting at ACH file download ", e);
                }
            }

            if (!done) {
                BatchUtils.delay("psp_batch_bank_retry_delay", "2");
            }
        } while (!done && (++retryCount <= maxRetries));
        return traceFileNames;
    }

    public SftpAchTraceFileDownloadListener getSftpAchTraceFileDownloadListener() {
        return new SftpAchTraceFileDownloadListener();
    }


}
