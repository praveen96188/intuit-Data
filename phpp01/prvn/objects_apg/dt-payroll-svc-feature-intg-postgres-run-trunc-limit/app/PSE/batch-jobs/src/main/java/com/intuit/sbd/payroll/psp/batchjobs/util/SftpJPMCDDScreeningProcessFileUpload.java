package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.ReportType;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.AMLReport;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.IndustryReport;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.OFACReport;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.TPSUReport;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchEvent;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suganyas315 on 6/4/15.
 */
public class SftpJPMCDDScreeningProcessFileUpload {
    protected static final SpcfLogger sfLogger;
    private List<String> mFileMap = new ArrayList<String>();
    private final static String mSendDir = BatchUtils.getConfigString("psp_batch_ftp_send_dir"); // Directory in which the generated files are located
    private final static String mArchiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir"); // Directory in which the uploaded file are located

    private String remoteDirectory;
    private Transporter sftp = null;
    private int maxRetries =0;
    private int retryDelay = 0;

    static {
        sfLogger = Application.getLogger(SftpJPMCDDScreeningProcessFileUpload.class);
    }

    /**
     *
     * @param pReportType     - We have to pass the reportType as the username, password and all other connection details are specific to the report type
     * @param host            - The same report can be uploaded to different host
     * @param pFiles          - We can either upload specific list of files or upload all the files of the report type
     */
    public void upload(ReportType pReportType, String host, List<String> pFiles) {
        sfLogger.info("Attempting to upload " + pReportType + " reports for JPMC Screening Process");
        switch (pReportType){
            case OFAC:
                String dataFileSuffix = OFACReport.DATA_FILE_EXT;
                String encryptFile = BatchUtils.getConfigString("psp_jpmc_csi_encrypt_file");
                if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
                    dataFileSuffix = OFACReport.ENCRYPTED_DATA_FILE_EXT;
                }
                getOFACFiles(OFACReport.OFAC_PREFIX, dataFileSuffix, pFiles);
                setConfigurationDetailsForOFACReport(host);
                uploadFiles(ReportType.OFAC);                           //  // Uploading Data files
                getFiles(OFACReport.OFAC_PREFIX, OFACReport.TRG_FILE_EXT, pFiles);
                uploadFiles(ReportType.OFAC);                           // Uploading trigger files
                break;
            case INDUSTRY:
                getFiles(IndustryReport.INDUSTRY_TYPE_PREFIX, pFiles);
                setConfigurationDetailsForIndustryReport(host);
                uploadFiles(ReportType.INDUSTRY);                    // Upload the Industry report to G2
                break;
            case AML:
                getFiles(AMLReport.AML_DATA_FILE_PREFIX, pFiles);
                setConfigurationDetailsForAMLReport(host);          // Set the configuration details for sending AML Report
                uploadFiles(ReportType.AML);                        // Upload the AML files to host1
            case TPSU:
                getFiles(TPSUReport.TPS_PREFIX,pFiles);             //Uploading TPSU file
                setConfigurationDetailsForTPSUReport();
                uploadFiles(ReportType.TPSU);

        }
    }

    public void emailFile(ReportType pReportType, List<String> pFiles) {
        switch (pReportType){
            case AML:
                getFiles(AMLReport.AML_DATA_FILE_PREFIX, pFiles);
                emailFile(pReportType
                        , BatchUtils.getConfigString("psp_jpmc_aml_email_notify_list")
                        , BatchUtils.getConfigString("psp_jpmc_aml_email_from_address")
                        , BatchUtils.getConfigString("psp_jpmc_aml_email_subject") + PSPDate.getPSPTime().format(" yyyyMMdd")
                            , BatchUtils.getConfigString("psp_jpmc_aml_email_subject"));
                break;
            case INDUSTRY:
                getFiles(IndustryReport.INDUSTRY_TYPE_PREFIX, pFiles);
                emailFile(pReportType
                        , BatchUtils.getConfigString("psp_industry_report_email_notify_list")
                        , BatchUtils.getConfigString("psp_industry_report_email_from_address")
                        , BatchUtils.getConfigString("psp_industry_report_email_subject") + PSPDate.getPSPTime().format(" yyyyMMdd")
                        , BatchUtils.getConfigString("psp_industry_report_email_subject"));
                break;
        }
    }

    private void emailFile(ReportType pReportType, String pToAddress, String pFromAddress, String pSubject, String pMsgBody) {
        if (mFileMap.isEmpty()) {
            sfLogger.info("No File to upload");
        }
        try {
            if (mFileMap != null) {
                for (String fileName : mFileMap) {
                    String fileToUpload = mSendDir + File.separator + fileName;
                    sfLogger.info("Sending email of " + pReportType + " for file " + fileToUpload);
                    if (ReportType.INDUSTRY.equals(pReportType) && StreamUtil.isFileIDPSEncrypted(fileToUpload)) {
                        fileToUpload = StreamUtil.createDecryptedFileForEmail(fileToUpload);
                    }
                    MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server")
                            , pToAddress
                            , pFromAddress
                            , pSubject
                            , pMsgBody
                            , fileToUpload);
                    sfLogger.info("Email sent successfully");
                }
            }
        } catch (Exception e) {
            sfLogger.error("Error emailing " + pReportType + " Report for JPMC screening process ", e);
        }
    }

    public void archiveFiles(ReportType pReportType, List<String> pFiles) throws S3UploadException, S3ConnectionException {
        sfLogger.info("Attempting to Archive " + pReportType + " reports");
        String prefix = "";
        String batchJobName = null;
        switch (pReportType){
            case OFAC:
                prefix = OFACReport.OFAC_PREFIX;                                // Archive both Data File & Trigger files
                batchJobName = BatchJobType.OFACReportProcessor.name();
                break;
            case INDUSTRY:
                prefix = IndustryReport.INDUSTRY_TYPE_PREFIX;
                batchJobName = BatchJobType.IndustryReportProcessor.name();
                break;
            case AML:
                prefix = AMLReport.AML_DATA_FILE_PREFIX;
                batchJobName = BatchJobType.AMLReportProcessor.name();
                break;
            case TPSU:
                prefix = TPSUReport.TPS_PREFIX;
                batchJobName = BatchJobType.TPSUReportProcessor.name();
                break;
        }
        if(prefix.trim().length() > 0){
            List<File> filesInDirectory = new ArrayList<File>();
            if(pFiles != null){
                for(String fileName : pFiles){
                    filesInDirectory.add(new File(mSendDir + "/" + fileName));
                }
            }else{
               filesInDirectory = FileUtils.getFilesInDirectory(mSendDir, prefix, null);
            }

            for (int i = 0; i < filesInDirectory.size(); i++) {
                S3UploadUtils.archive(batchJobName,mArchiveDir,filesInDirectory.get(i).getAbsolutePath());
            }

        }
    }

    private void getFiles(String prefix, List<String> pFiles) {
        getFiles(prefix, null, pFiles);
    }

    private void getFiles(String prefix, String suffix, List<String> pFiles) {
        mFileMap.clear();
        if(pFiles != null){
            for(String fileName: pFiles){
                if(fileName.startsWith(prefix)){
                    mFileMap.add(fileName);
                }
            }
        }else{
            List<File> filesInDirectory = FileUtils.getFilesInDirectory(mSendDir, prefix, suffix);
            mFileMap = FileUtils.getAbsoluteFilenamesFromFiles(filesInDirectory);
        }
    }

    private void getOFACFiles(String prefix, String suffix, List<String> pFiles) {
        getFiles(prefix, suffix, pFiles);
        for(int length=0; length<mFileMap.size(); length++)
        {
            String fileName = mFileMap.get(length);
            if(fileName.contains(OFACReport.AUDIT_FILE)){
                mFileMap.remove(length);
            }
        }

    }

    private void setConfigurationDetailsForOFACReport(String host){
        sftp = BatchUtils.getJpmcCsiSftpConnection(new SftpJPMCDDScreeningFileUploadListener(), host);
        remoteDirectory = BatchUtils.getConfigString("psp_jpmc_csi_destination_dir");
        maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_jpmc_csi_ftp_max_retry"));
        retryDelay = Integer.parseInt(BatchUtils.getConfigString("psp_jpmc_csi_ftp_retry_delay"));
    }

    private void setConfigurationDetailsForAMLReport(String host){
        sftp = BatchUtils.getJmpcAmlConnection(new SftpJPMCDDScreeningFileUploadListener(), host);
        remoteDirectory = BatchUtils.getConfigString("psp_jpmc_aml_destination_dir");
        maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_jpmc_aml_ftp_max_retry"));
    }

    private void setConfigurationDetailsForIndustryReport(String host){
        sftp = BatchUtils.getJpmcG2SftpConnection(new SftpJPMCDDScreeningFileUploadListener(), host);
        remoteDirectory = BatchUtils.getConfigString("psp_jpmc_g2_destination_dir");
        maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_jpmc_g2_ftp_max_retry"));
        retryDelay = Integer.parseInt(BatchUtils.getConfigString("psp_jpmc_g2_ftp_retry_delay"));
    }

    private void setConfigurationDetailsForTPSUReport(){
        sftp = BatchUtils.getBankSftpConnectionForNocDownload(new SftpJPMCDDScreeningFileUploadListener());
        remoteDirectory = BatchUtils.getConfigString("psp_jpmc_tpsu_destination_dir");
        maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_bank_max_retry", "5"));
        BatchUtils.delay("psp_batch_bank_retry_delay", "1");
    }

    private boolean uploadFiles(ReportType pReportType){
        int retryCount = 0;
        boolean done = false;
        do{
            if (mFileMap.isEmpty()) {
                sfLogger.info("No File to upload");
                done = true;
            }
            try {
                sftp.setLogger(sfLogger);
                sftp.connect();
                sftp.changeLocalDir(mSendDir);
                sftp.changeRemoteDir(remoteDirectory);
                sftp.uploadFiles(mFileMap, 60000);
                done = true;
            } catch (Exception e) {
                if (retryCount == 0) {
                    sfLogger.error("Error sending " + pReportType + " Report for JPMC screening process ", e);
                } else if (retryCount < maxRetries) {
                    sfLogger.error("Error sending " + pReportType + " Report for JPMC screening process(retry attempt " + retryCount + ") ", e);
                } else {
                    throw new RuntimeException("Error sending " + pReportType + " Report for JPMC screening process(aborting process) ", e);
                }
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException("Error in disconnecting after file uploaded to JPMC screening(aborting process) ", e);
                }
            }
            if(!done){
                BatchUtils.delay(retryDelay);
            }
        }while (!done && (++retryCount <= maxRetries));
        return done;
    }

    private class SftpJPMCDDScreeningFileUploadListener extends JSchAdapter {
        public void upload(FileBean val) {
            sfLogger.info("File uploaded:" + val.getFilename());
        }

        public void connected(JSchEvent pEvent) {
            sfLogger.info("Successfully connected to JPMC Screening");
        }

        public void disconnected(JSchEvent pEvent) {
            sfLogger.info("Successfully Disconnected from JPMC Screening");
        }
    }
}

