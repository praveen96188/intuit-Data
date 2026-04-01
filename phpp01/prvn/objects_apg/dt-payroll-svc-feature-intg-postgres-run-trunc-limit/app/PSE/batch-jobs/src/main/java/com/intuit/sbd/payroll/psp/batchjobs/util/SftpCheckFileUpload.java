package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedCheckFlatFileGenerator;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.AccountingReportFile;
import com.intuit.sbd.payroll.psp.domain.AccountingReportFileStatus;
import com.intuit.sbd.payroll.psp.domain.AccountingReportFileType;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 10, 2011
 * Time: 9:37:49 PM
 */
public class SftpCheckFileUpload {
    private static SpcfLogger logger = Application.getLogger(PrintedCheckFlatFileGenerator.class);

    private Map<String, SpcfUniqueId> mFileMap = new Hashtable<String, SpcfUniqueId>();
    private Map<String, SpcfUniqueId> mListenerFileMap = new Hashtable<String, SpcfUniqueId>();

    private class FileUploadListener extends JSchAdapter {
        public void upload(FileBean event) {
            SpcfUniqueId id = mListenerFileMap.get(event.getFilename());

            if (id == null) {
                throw new RuntimeException("Unable to correlate printed check file transmitted.");
            }

            try {
                PayrollServices.beginUnitOfWork();

                AccountingReportFile accountingReportFile = Application.findById(AccountingReportFile.class, id);

                if (accountingReportFile == null) {
                    throw new RuntimeException("Could not find printed check file: " + id);
                }

                accountingReportFile.setStatus(AccountingReportFileStatus.Transmitted);
                accountingReportFile.setTransmissionDate(PSPDate.getPSPTime());

                Application.save(accountingReportFile);

                PayrollServices.commitUnitOfWork();

                logger.info("Changing file " + event.getFilename() + " status to Transmitted");
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void upload(AccountingReportFileType pFileType) throws Exception {
        if(pFileType == null) {
            logger.info("File type is null. Skipping upload.");
            return;
        }

        logger.info("Attempting to upload " + pFileType + " file...");

        boolean done = false;

        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "10"));
        int retryCount = 0;

        do {
            // Retrieve the files to upload each time through in case
            // some files succeeded on previous iteration.
            getFilesToUpload(pFileType);
            FileStore fileStore = null;
            if (mFileMap.isEmpty()) {
                logger.warn("No " + pFileType + " files found awaiting upload.");
                done = true;
            } else {
                Transporter sftp = null;
                switch (pFileType) {
                    case PositivePay:
                        sftp = BatchUtils.getPositivePaySftpConnection(new FileUploadListener());
                        break;
                    case PrintedCheckReconPlus:
                    case TaxAccountsReconPlus:
                    case ReturnsAccountsReconPlus:
                        break;
                }

                try {
                    if (AccountingReportFileType.PositivePay.equals(pFileType) || AccountingReportFileType.ReturnsAccountsReconPlus.equals(pFileType)) {
                        sftp.setLogger(logger);

                        sftp.connect();

                        sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_send_dir"));
                    }
                    for (Map.Entry<String, SpcfUniqueId> stringSpcfUniqueIdEntry : mFileMap.entrySet()) {
                        mListenerFileMap.put(stringSpcfUniqueIdEntry.getKey(), stringSpcfUniqueIdEntry.getValue());
                    }
                    switch (pFileType) {
                        case PositivePay:
                            // todo remove this property from the system props table and put it into the batch conf
                            sftp.changeRemoteDir(SystemParameter.findStringValue(SystemParameter.Code.PRINTED_CHECKS_POSITIVE_PAY_FILE_DIRECTORY, "./"));
                            sftp.uploadFiles(mFileMap.keySet(), 60000);
                            break;
                        case PrintedCheckReconPlus:
                        case TaxAccountsReconPlus:
                            for (String pFileName : mFileMap.keySet()) {
                                logger.info("sending email for Recon file: "+ pFileName);
                                emailFile(pFileName);
                            }
                            break;
                        case ReturnsAccountsReconPlus:
                            for (String pFileName : mFileMap.keySet()) {
                                sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_reconplus_send_dir"));
                                String remoteFileName = BatchUtils.getConfigString("psp_batch_reconplus_returns_file_name");
                                mListenerFileMap.put(remoteFileName, mFileMap.get(pFileName));
                                sftp.uploadFile(pFileName, remoteFileName);
                            }
                            break;
                    }

                    done = true;
                } catch (Exception e) {
                    if (retryCount == 0) {
                        logger.error("Error sending " + pFileType + " files (attempting retry) ", e);
                    } else if (retryCount < maxRetries) {
                        logger.error("Error sending " + pFileType + " files (retry attempt " + retryCount + ") ", e);
                    } else {
                        throw new RuntimeException("Error sending " + pFileType + " files (aborting process) ", e);
                    }
                }
                if (!done) {
                    BatchUtils.delay("psp_batch_bank_retry_delay", "1");
                }
            }
        } while (!done && (++retryCount <= maxRetries));
    }

    private void emailFile(String pFileName) throws Exception {
        File emailFileUnencrypted = null;
        String zipFileName = null;
        try {
            String workingDir = BatchUtils.getConfigString("psp_batch_ftp_send_dir")+File.separator;
            String unencryptedFileName = FilenameUtils.getBaseName(pFileName)+".txt";
            zipFileName = workingDir+FilenameUtils.getBaseName(pFileName)+".zip";
            PgpFileUtils.pgpDecryptUnsingedFile(workingDir
                    , pFileName
                    , unencryptedFileName
                    , BatchUtils.getConfigString("psp_tfa_intuit_private_key")
                    , BatchUtils.getConfigString("psp_tfa_intuit_key_password"));
            emailFileUnencrypted = new File(workingDir + unencryptedFileName);
            com.intuit.sbd.payroll.psp.common.utils.FileUtils.zip(emailFileUnencrypted,zipFileName);
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString("psp_batch_reconplus_email_toAddress"),
                    BatchUtils.getConfigString("psp_batch_reconplus_email_fromAddress"),
                    BatchUtils.getConfigString("psp_batch_reconplus_email_subject")+","+PSPDate.getPSPTime().format("MM/dd"),
                    BatchUtils.getConfigString("psp_batch_reconplus_email_subject"),
                    zipFileName);
            updateAccountingReportFileStatus(mFileMap.get(pFileName));
        } finally {
            FileUtils.deleteQuietly(emailFileUnencrypted);
            File file = new File(zipFileName);
            FileUtils.deleteQuietly(file);
        }
    }

    private void getFilesToUpload(AccountingReportFileType pFileType) {
        try {
            // clear the file -> id map in case this isn't the first time through
            mFileMap.clear();
            mListenerFileMap.clear();

            PayrollServices.beginUnitOfWork();

            // get list of files to send
            DomainEntitySet<AccountingReportFile> accountingReportFiles =
                    AccountingReportFile.findByTypeAndStatus(pFileType, AccountingReportFileStatus.Created, false);

            for (AccountingReportFile reportFile : accountingReportFiles) {
                File fileToUpload = new File(reportFile.getFileName());
                if (fileToUpload.exists() && fileToUpload.isFile()) {
                    logger.info("Preparing to upload check file " + reportFile.getFileName() + "...");
                    mFileMap.put(new File(reportFile.getFileName()).getName(), reportFile.getId());
                } else {
                    throw new RuntimeException("FAILURE: can't upload file (" + reportFile.getFileName() +  ") associated with " + reportFile + "  - file doesn't exist.");
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void updateAccountingReportFileStatus(SpcfUniqueId id){
        PayrollServices.beginUnitOfWork();

        AccountingReportFile accountingReportFile = Application.findById(AccountingReportFile.class, id);

        if (accountingReportFile == null) {
            throw new RuntimeException("Could not find printed check file: " + id);
        }

        accountingReportFile.setStatus(AccountingReportFileStatus.Transmitted);
        accountingReportFile.setTransmissionDate(PSPDate.getPSPTime());

        Application.save(accountingReportFile);

        PayrollServices.commitUnitOfWork();

        logger.info("Changing file " + accountingReportFile.getFileName() + " status to Transmitted");
    }
}
