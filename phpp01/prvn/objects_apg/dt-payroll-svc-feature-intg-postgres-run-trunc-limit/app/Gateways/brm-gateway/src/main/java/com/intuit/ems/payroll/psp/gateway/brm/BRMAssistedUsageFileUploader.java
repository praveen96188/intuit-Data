package com.intuit.ems.payroll.psp.gateway.brm;

import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSInputStream;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tray2
 * Date: 8/28/19
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class BRMAssistedUsageFileUploader extends JSchAdapter {

    private static SpcfLogger mLogger = Application.getLogger(BRMAssistedUsageFileUploader.class);

    public static String ASST_BRM_FILE_HEADER = "Groupid,SiteGeneratorPortalproduct,EventCode,Quantity,Amount,EventId,Timestamp\n";

    public static String LOCAL_WORK_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_work_dir");
    public static String PSP_S3_ARCHIVE_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_arcv_dir");
    public static String PSP_S3_ERROR_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_err_dir");
    public static String LOCAL_RECV_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_recv_dir");

    public static String FILENAME_EXT = ".csv";
    public static String DAP_FILENAME_PATTERN = "DAP_Usage_[timestamp]" + FILENAME_EXT;

    private List<String> filesToBeArchived;

    // upload all the csv
    public void upload() throws S3UploadException, S3ConnectionException {

        mLogger.info("Start BRM DAP file uploading...");
        filesToBeArchived = new ArrayList<String>();
        File workDir = new File(LOCAL_WORK_DIR);
        String batchJobName = BatchJobType.AssistedUsageReportingToBRMProcessor.name();
        File[] filesToBeUploaded = workDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {

                return name.endsWith(FILENAME_EXT);
            }
        });
        if (filesToBeUploaded != null) {
            mLogger.info("Uploading files:" + filesToBeUploaded.length);
            for (File aFile : filesToBeUploaded) {
                try {
                    //Upload to BRM S3 location
                    BRMS3UploadUtils.uploadFileToBRMS3(BRMS3UploadUtils.BRM_S3_UPLOAD_FOLDER, aFile.getAbsolutePath());
                    filesToBeArchived.add(aFile.getAbsolutePath());
                } catch (Exception e) {
                    mLogger.error("Error to upload file to BRM S3 location. The file is being moved to error folder: " + aFile.getAbsolutePath() + "Exception: " + e);
                    //Encrypt if it is in AWS Environment
                    if(Application.isAWSEnvironment()){
                        BRMS3UploadUtils.encryptFile(aFile.getAbsolutePath());
                    }
                    S3UploadUtils.archive(batchJobName,PSP_S3_ERROR_DIR,aFile.getAbsolutePath());
                }
            }

            for(int i=0;i<filesToBeArchived.size();i++) {
                if(Application.isAWSEnvironment()) {
                    BRMS3UploadUtils.encryptFile(filesToBeArchived.get(i));
                }
                S3UploadUtils.archive(batchJobName,PSP_S3_ARCHIVE_DIR,filesToBeArchived.get(i));
            }
        } else {
            mLogger.info("Finished BRM Assisted uploading for 0 files");
        }
    }

}
