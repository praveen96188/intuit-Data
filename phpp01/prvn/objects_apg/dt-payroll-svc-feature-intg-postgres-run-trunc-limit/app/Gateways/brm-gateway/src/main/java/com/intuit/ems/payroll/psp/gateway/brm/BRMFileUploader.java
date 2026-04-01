package com.intuit.ems.payroll.psp.gateway.brm;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intuit.ems.payroll.psp.gateway.brm.BRMAssistedUsageFileUploader.PSP_S3_ERROR_DIR;

/**
 * Created with IntelliJ IDEA.
 * User: YifengS302
 * Date: 9/28/12
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class BRMFileUploader extends JSchAdapter {

    private static SpcfLogger mLogger = Application.getLogger(BRMFileUploader.class);

    public static String BRM_FILE_HEADER = "Groupid,SiteGeneratorPortalproduct,EventCode,Quantity,Timestamp\n";

    public static String SCP_SERVER = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_server");
    public static String SCP_PORT = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_port");
    public static String SCP_USERNAME = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_username");
    public static String SCP_PASSWORD = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_password");
    public static String SCP_DEST = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_destination");
    public static String SCP_ERROR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_error");

    public static String LOCAL_WORK_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_work_dir");
    public static String LOCAL_ARCHIVE_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_arcv_dir");
    public static String LOCAL_ERROR_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_err_dir");
    public static String LOCAL_RECV_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_recv_dir");

    public static String FILENAME_EXT = ".csv";
    public static String FILENAME_PATTERN = "PSP_SymphonyUsage_[timestamp]" + FILENAME_EXT;

    private List<File> filesToBeArchived;

    // upload all the csv
    public void upload() throws S3UploadException, S3ConnectionException {

        mLogger.info("Start BRM file uploading...");
        filesToBeArchived = new ArrayList<File>();
        File workDir = new File(LOCAL_WORK_DIR);
        String batchJobName = BatchJobType.EMSBSToBRMDataSyncProcessor.name();
        File[] filesToBeUploaded = workDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {

                return name.endsWith(FILENAME_EXT);
            }
        });
        mLogger.info("Uploading files:" + filesToBeUploaded.length);
        if (filesToBeUploaded != null) {

            Boolean iSBrmSympJobEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BRM_SYMPHONY_JOB_ENABLED, false);

            if(iSBrmSympJobEnabled){
                for (File aFile : filesToBeUploaded) {

                    try {
                        mLogger.info("Uploading BRM file to S3 Location");
                        BRMS3UploadUtils.uploadFileToBRMS3(BRMS3UploadUtils.BRM_S3_UPLOAD_FOLDER, aFile.getAbsolutePath());
                        mLogger.info("Uploaded BRM file to S3 Location");
                        filesToBeArchived.add(new File(aFile.getAbsolutePath()));
                    } catch (Exception e) {
                        mLogger.error("Error to upload file to BRM S3 location. The file is being moved to error folder: " + aFile.getAbsolutePath() + "Exception: " + e);
                        //Encrypt if it is in AWS Environment
                        if(Application.isAWSEnvironment()){
                            BRMS3UploadUtils.encryptFile(aFile.getAbsolutePath());
                        }
                        S3UploadUtils.archive(batchJobName,PSP_S3_ERROR_DIR,aFile.getAbsolutePath());
                    }
                }
            }
            else {
                Transporter sftp = null;
                try {
                    sftp = new Transporter(SCP_SERVER, SCP_USERNAME, SCP_PASSWORD);

                    sftp.addListener(this);
                    sftp.setDebug(false);
                    sftp.setKnownHostsPath(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_sftp_knownHosts", null));
                    sftp.connect();
                    sftp.changeLocalDir(LOCAL_WORK_DIR);
                    sftp.changeRemoteDir(SCP_DEST);
                    for (File aFile : filesToBeUploaded) {
                        try {

                            sftp.uploadFile(aFile.getName());

                        } catch (IOException e) {
                            mLogger.error("Error to upload file to BRM scp server. The file is being moved to error folder.", e, aFile.getAbsolutePath());
                            S3UploadUtils.archive(batchJobName,LOCAL_ERROR_DIR,aFile.getAbsolutePath());
                        }
                    }
                } catch (Throwable e) {
                    mLogger.error("Error to connect to BRM scp server. Please manually upload.", e);
                    List<File> files = Arrays.asList(filesToBeUploaded);
                    int i;
                    for(i=0;i<files.size();i++){
                        S3UploadUtils.archive(batchJobName,LOCAL_ERROR_DIR,files.get(i).getAbsolutePath());
                    }
                } finally {
                    if (sftp != null) {
                        try {
                            sftp.disconnect();
                        } catch (Exception e) {
                            mLogger.error("Error disconnecting from BRM Upload step", e);
                        }
                    }
                }
            }

            for(int i=0;i<filesToBeArchived.size();i++) {
                S3UploadUtils.archive(batchJobName,LOCAL_ARCHIVE_DIR,filesToBeArchived.get(i).getAbsolutePath());

            }
        } else {
            mLogger.info("Finished BRM uploading for 0 files");
        }
    }
    // upload all the csv
    public void uploadBRMUsageFiles() throws S3UploadException, S3ConnectionException {

        mLogger.info("Start BRM file uploading...");
        filesToBeArchived = new ArrayList<File>();
        File workDir = new File(LOCAL_WORK_DIR);
        File recvDir = new File(LOCAL_RECV_DIR);
        String batchJobName = BatchJobType.BRMUsageErrorFileProcessor.name();
        String fileNametoUpdateSystemParameter="";
        File[] filesToBeUploaded = workDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {

                return name.endsWith(FILENAME_EXT);
            }
        });
        mLogger.info("Uploading files:" + filesToBeUploaded.length);
        if (filesToBeUploaded != null) {
            try {
                for (File aFile : filesToBeUploaded) {
                    try {
                        BRMS3UploadUtils.uploadFileToS3FileStore(aFile.getAbsolutePath());


                    } catch (Exception e) {
                        mLogger.error("Error to upload file to BRM scp server. The file is being moved to error folder.", e.getCause(), aFile.getAbsolutePath());
                        S3UploadUtils.archive(batchJobName,LOCAL_ERROR_DIR,aFile.getAbsolutePath());
                    }
                    try{
                        StopWatch timer = StopWatch.startTimer();
                        mLogger.info("Started archiving ErroFile" +aFile.getName());
                        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BRMUsageErrorFileProcess));
                        BRMS3UploadUtils.archiveProcessedErroFiles(aFile.getName());
                        fileNametoUpdateSystemParameter=aFile.getName();
                        mLogger.info("Filename to be updated to systemparameter: " +fileNametoUpdateSystemParameter);
                        mLogger.info("Completed archiving ErroFile "+aFile.getName());

                    } catch (Throwable t) {
                        throw new RuntimeException("Exception in archiving File "+aFile.getName(), t);
                    }
                }
            } catch (Throwable e) {
                mLogger.error("Error to connect to BRM scp server. Please manually upload.", e);
                List<File> files = Arrays.asList(filesToBeUploaded);
                int i;
                for(i=0;i<files.size();i++){
                    S3UploadUtils.archive(batchJobName,LOCAL_ERROR_DIR,files.get(i).getAbsolutePath());
                }
            }finally {
                File recvDirectory = new File(LOCAL_RECV_DIR);
                for(File f :recvDirectory.listFiles()){
                    f.delete();
                    mLogger.info("Deleted file" +f.getName() +" from receive folder");
                }
                File workDirectory = new File(LOCAL_WORK_DIR);
                for(File f :workDirectory.listFiles()){
                    f.delete();
                    mLogger.info("Deleted file" +f.getName() +" from work folder");
                }
                if(!StringUtils.isEmpty(fileNametoUpdateSystemParameter)){
                    updateSystemParameter(fileNametoUpdateSystemParameter);
                }

            }
        } else {
            mLogger.info("Finished BRM uploading for 0 files");
        }
    }

    // download the error csv
    public void download(String fileName) {

        mLogger.info("Start BRM error file downloading...");
        Transporter sftp = null;
        try {
            sftp = new Transporter(SCP_SERVER, SCP_USERNAME, SCP_PASSWORD);
            sftp.addListener(this);
            sftp.setDebug(false);
            sftp.setKnownHostsPath(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_sftp_knownHosts", null));
            sftp.connect();
            sftp.changeLocalDir(LOCAL_RECV_DIR);
            sftp.changeRemoteDir(SCP_ERROR);
            sftp.downloadFileSecurely(fileName + FILENAME_EXT);

        } catch (Throwable e) {
            throw new RuntimeException("Error downloading the BRM error file ", e);
        } finally {
            if (sftp != null) {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    protected void moveFileTo(File pFile, String pDestFolder) {

        try {
            pFile.renameTo(new File(pDestFolder + pFile.getName()));
        } catch (Exception e) {
            mLogger.error("Error to move file " + pFile.getName() + " to " + pDestFolder);
        }
    }

    protected void moveFilesTo(List<File> pFiles, String pDestFolder) {

        if (pFiles != null) {
            for (File aFile : pFiles) {
                moveFileTo(aFile, pDestFolder);
            }
        }
    }

    public void download(FileBean scpFileDownloadedEvent) {
        mLogger.info("Finished BRM file downloading file " +
                LOCAL_RECV_DIR + scpFileDownloadedEvent.getFilename() + " to recv dir.");
    }

    public void upload(FileBean event) {

        File uploadedFile = new File(LOCAL_WORK_DIR + event.getFilename());

        mLogger.info("Uploaded filename :" + uploadedFile.getPath() + uploadedFile.getName());
        filesToBeArchived.add(uploadedFile);
        mLogger.info("Finished BRM file uploading, archiving file " + uploadedFile.getAbsolutePath() + " to archive dir.");
    }

    private void updateSystemParameter(String pFileName) {
        try {
            Application.beginUnitOfWork();
            ProcessResult pr = PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.BRM_SYMPHONY_FILE_NAME, pFileName.substring(0, pFileName.length() -4));
            if (!pr.isSuccess()) {
                mLogger.error("failed to add value for " + SystemParameter.Code.BRM_SYMPHONY_FILE_NAME);
            }
            Application.commitUnitOfWork();
        } catch (Throwable t) {
            mLogger.error("failed to add value for " + SystemParameter.Code.BRM_SYMPHONY_FILE_NAME);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

}
