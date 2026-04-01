package com.intuit.sbd.payroll.psp.adapters.sap.reportDownload;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpCommonEncryptedReader;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.BRMS3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3DownloadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.*;
/**
 * Created by: smodgil on 01/21/20.
 * Description: This class contains methods to download and decrypt the AML report files
 */
public class ReportExport {

    public static final String PSP_BATCHJOBS_S3_BUCKET = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_s3_bucket");
    public static final String PSP_ARCHIVE_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir");
    public static final String AML_DATA_FILE_PREFIX = "AML_Data_File_";
    public static final String ENCRYPTED_FILE_EXT = ".pgp";
    public static final String FILE_EXT = ".txt";
    private static final SpcfLogger logger = PayrollServices.getLogger(ReportExport.class);

    private String mDecryptionKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfa_intuit_private_key");
    private String mDecryptionKeyPassword = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfa_intuit_key_password");

    public File exportDecryptedFileFromPSP(String remoteFileName) throws Exception {
        exportEncryptedFileFromS3(PSP_ARCHIVE_DIR, remoteFileName, PSP_ARCHIVE_DIR, remoteFileName);
        String localEncryptionFileAbsolutePath = PSP_ARCHIVE_DIR + File.separator + remoteFileName;
        checkFileExistsInPSP(localEncryptionFileAbsolutePath);

        String localDecryptionFileName = remoteFileName.replace(ENCRYPTED_FILE_EXT, FILE_EXT);
        File decryptedFile = decryptFileInPSP(remoteFileName, localDecryptionFileName);

        if (!decryptedFile.exists()) {
            throw new FileNotFoundException("Decrypted file not found = "+decryptedFile.getName());
        }

        return decryptedFile;
    }

    private void exportEncryptedFileFromS3(String remoteDirectory, String remoteFileName, String localDirectory, String localFileName) throws Exception {
        String remoteFileAbsolutePath = null;
        String localFileAbsolutePath = localDirectory + File.separator + localFileName;

        try {
            deleteExistingFilesInPSP(localFileAbsolutePath);
            remoteFileAbsolutePath = BRMS3UploadUtils.getFullRemoteFilePath(remoteDirectory, remoteFileName);
            S3UploadUtils.downloadFromS3FileStore(PSP_BATCHJOBS_S3_BUCKET, remoteFileAbsolutePath, localFileAbsolutePath);
        } catch (S3DownloadException e) {
            logger.error("Exception occurred while exporting "+remoteFileName+" from S3 ("+remoteDirectory+") - {}", e);
            deleteExistingFilesInPSP(localFileAbsolutePath);
            throw e;
        } catch (S3ConnectionException e) {
            logger.error("Exception occurred during S3 connection - {}", e);
            deleteExistingFilesInPSP(localFileAbsolutePath);
            throw e;
        } catch (Exception e) {
            logger.error("Exception occurred while exporting "+remoteFileName+" from S3 ("+remoteDirectory+") to PSP ("+localDirectory+") - {}", e);
            deleteExistingFilesInPSP(localFileAbsolutePath);
            throw e;
        }
    }

    private File checkFileExistsInPSP(String absolutePath) throws FileNotFoundException {
        File file = new File(absolutePath);

        if (!file.exists()) {
            throw new FileNotFoundException("encryptedFile is not present in PSP ("+file.getAbsolutePath()+")");
        }

        return file;
    }

    private void deleteExistingFilesInPSP(String encryptedFileName) {
        String textFileName = encryptedFileName.replace(ENCRYPTED_FILE_EXT, FILE_EXT);
        File existingPgpFile = new File(encryptedFileName);
        File existingTextFile = new File(textFileName);

        if(existingPgpFile.exists()) {
            existingPgpFile.delete();
        }

        if(existingTextFile.exists()) {
            existingTextFile.delete();
        }
    }

    public String getFileName(String reportType, String date) throws Exception {
        SpcfCalendar selectedDate = SpcfCalendar.parse("MM/dd/yyyy", date);

        switch (reportType) {
            case "AMLReportProcessor":
                String formattedDate = StringFormatter.formatDate(selectedDate, "yyyyMMdd");
                return AML_DATA_FILE_PREFIX + formattedDate + ENCRYPTED_FILE_EXT;
            default:
                throw new Exception("Implementation not found for "+reportType);
        }
    }

    private File decryptFileInPSP(String localEncryptionFileName, String localDecryptionFileName) throws Exception {
        File decryptedFile = new File(PSP_ARCHIVE_DIR + File.separator + localDecryptionFileName);

        try {
            PgpFileUtils.pgpDecryptUnsingedFile( PSP_ARCHIVE_DIR + File.separator, localEncryptionFileName,
                    localDecryptionFileName, mDecryptionKey, mDecryptionKeyPassword, true);
            logger.info("Decrypted file = "+decryptedFile.getAbsolutePath()+" with length ="+decryptedFile.length());
        } catch(Exception ex) {
            logger.error("Decryption failed for file = "+decryptedFile.getName()+" with exception {}", ex);
            throw ex;
        }
        return decryptedFile;
    }

}
