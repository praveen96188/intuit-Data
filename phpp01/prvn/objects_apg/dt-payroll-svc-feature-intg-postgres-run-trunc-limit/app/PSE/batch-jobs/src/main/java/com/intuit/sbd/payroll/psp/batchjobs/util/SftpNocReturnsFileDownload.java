package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sshetty
 * Date: 7/2/13
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SftpNocReturnsFileDownload {
    protected static final SpcfLogger sfLogger;

    static {
        sfLogger = Application.getLogger(SftpNocReturnsFileDownload.class);
    }

    class SftpNocReturnsFileDownloadListener extends JSchAdapter {
          //TODO: look at download and change file name with the event
//          public void download(SftpDownloadEvent event) {
//              File file = new File(BatchUtils.getConfigString("psp_noc_bank_recv_dir"), event.getFilename());
//              SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
//              file.renameTo(new File(String.format("psp-noc-%s.csv", spcfCalendar.format("yyyyMMdd"))));
//          }

    }

    public void download() {
        sfLogger.info("Attempting to download NOC Returns file(s) from bank...");

        boolean done = false;
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "5"));
        int retryCount = 0;

        do {
            Transporter sftp = BatchUtils.getBankSftpConnectionForNocDownload(getNocReturnsFileDownloadListener());
            try {
                sftp.setLogger(sfLogger);

                sftp.connect();

                sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"));

                boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
                if (enableEncryption) {
                    sftp.changeRemoteDir(BatchUtils.getConfigString("psp_noc_bank_encrypted_recv_dir"));
                } else {
                    sftp.changeRemoteDir(BatchUtils.getConfigString("psp_noc_bank_recv_dir"));
                }

                // Pattern is not required for NOC files since it is the only file in the folder
                List<String> bankFiles = sftp.getRemoteDirListing("\\*.\\*");

                // download the files and then delete them from the remote host
                // (if anything fails, an exception will be thrown)
                for (String file : bankFiles) {
                    File nocFile = sftp.downloadFile(file);
                    sftp.deleteRemoteFile(file);
                    SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
                    nocFile.renameTo(new File(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"), String.format("psp-noc-%s.csv", spcfCalendar.format("yyyyMMdd")))) ;
                }

                done = true;
            } catch (Exception e) {
                if (retryCount == 0) {
                    sfLogger.error("Error receiving NOC Returns file from bank (attempting retry) ", e);
                } else if (retryCount < maxRetries) {
                    sfLogger.error("Error receiving NOC Returns file from bank (retry attempt " + retryCount + ") ", e);
                } else {
                    throw new RuntimeException("Error receiving NOC Returns file from bank (aborting process) ", e);
                }
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException("Error in disconnecting after NOC file downloaded(aborting process) ", e);
                }
            }

            if (!done) {
                BatchUtils.delay("psp_batch_bank_retry_delay", "2");
            }
        } while (!done && (++retryCount <= maxRetries));
    }

    public SftpNocReturnsFileDownloadListener getNocReturnsFileDownloadListener() {
        return new SftpNocReturnsFileDownloadListener();
    }
}
