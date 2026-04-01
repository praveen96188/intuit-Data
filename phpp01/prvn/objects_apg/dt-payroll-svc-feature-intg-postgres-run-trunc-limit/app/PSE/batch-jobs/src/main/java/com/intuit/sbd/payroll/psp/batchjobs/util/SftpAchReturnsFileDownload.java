package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnBatch;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnBatchStatusCode;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 16, 2009
 * Time: 2:27:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SftpAchReturnsFileDownload {
    protected static final SpcfLogger sfLogger;

    static {
        sfLogger = Application.getLogger(SftpAchReturnsFileDownload.class);
    }

    class SftpAchReturnsFileDownloadListener extends JSchAdapter {

        @Override
        public void download(FileBean val) {
            File file = new File(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"), val.getFilename());

            try {
                PayrollServices.beginUnitOfWork();
                // PSRV004193: ACH Returns Job Fails Because it Downloads the Same File More Than Once
                // Make sure this downloaded file was not already downloaded and queued for processing
                DomainEntitySet<TransactionReturnBatch> transactionReturnBatches = Application.find(TransactionReturnBatch.class, TransactionReturnBatch.ACHReturnFileName().equalTo(file.getAbsolutePath())) ;
                if(transactionReturnBatches!=null && transactionReturnBatches.isNotEmpty()) {
                    sfLogger.warn("A transaction batch with this filename:" + file.getAbsolutePath() + " already exists. Skipping this file.");
                } else {
                    TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
                    transactionReturnBatch.setACHReturnFileName(file.getAbsolutePath());
                    transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
                    transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
                    transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
                    Application.save(transactionReturnBatch);
                }
                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }

    }

    public void download() {
        sfLogger.info("Attempting to download ACH Returns file(s) from bank...");

        boolean done = false;
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "5"));
        int retryCount = 0;

        do {
            Transporter sftp = BatchUtils.getBankSftpConnection(getAchReturnsFileDownloadListener());
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

                List<String> bankFiles = sftp.getRemoteDirListing(BatchUtils.getConfigString("psp_returns_file_pattern"));

                // download the files and then delete them from the remote host
                // (if anything fails, an exception will be thrown)
                for (String file : bankFiles) {
                    sftp.downloadFile(file);
                    sftp.deleteRemoteFile(file);
                }

                done = true;
            } catch (Exception e) {
                if (retryCount == 0) {
                    sfLogger.error("Error receiving ACH Returns file from bank (attempting retry) ", e);
                } else if (retryCount < maxRetries) {
                    sfLogger.error("Error receiving ACH Returns file from bank (retry attempt " + retryCount + ") ", e);
                } else {
                    throw new RuntimeException("Error receiving ACH Returns file from bank (aborting process) ", e);
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
    }

    public SftpAchReturnsFileDownloadListener getAchReturnsFileDownloadListener() {
        return new SftpAchReturnsFileDownloadListener();
    }
}
