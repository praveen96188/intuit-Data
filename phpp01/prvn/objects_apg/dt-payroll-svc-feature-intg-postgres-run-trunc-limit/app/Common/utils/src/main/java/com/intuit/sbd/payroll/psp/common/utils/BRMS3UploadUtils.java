package com.intuit.sbd.payroll.psp.common.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSInputStream;
import com.intuit.sbd.payroll.psp.common.utils.mockutil.MockAmazonS3;
import com.intuit.sbd.payroll.psp.common.utils.mockutil.MockS3FileStore;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.sbg.shared.filestore.FileStoreFactory;
import com.intuit.sbg.shared.filestore.FileStoreType;
import com.intuit.sbg.shared.filestore.enums.EncryptionType;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BRMS3UploadUtils {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(BRMS3UploadUtils.class);

    public static String BRM_S3_BUCKET = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_server_bucket");
    public static String BRM_S3_UPLOAD_FOLDER = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_upload_folder");
    public static String BRM_S3_ERROR_FOLDER = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_error_folder");
    public static String BRM_S3_ARCHIVE_FOLDER = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_errorarchive_folder");

    public static String LOCAL_RECV_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_scp_local_recv_dir");
    public static String BRM_IDPS_APIKEY = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_idps_apikey");
    private static String CHECK_REPEATED_ERROR_PROCESSING_STR="-error-error";
    private static AmazonS3 s3Client;

    // function to get the filestore object initialized with credentials for connecting to S3
    public static FileStore getFileStore() throws Exception {
        if(Application.isParallelEnv()) {
            logger.info("Parallel Env Mock S3 FileStore Object BRMS3UploadUtils Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile());
            return new MockS3FileStore();
        }
        logger.info("Setting up S3 object BRMS3UploadUtils");

        FileStore.FileStoreBuilder fileStore = FileStore.builder();
        // Expectation: PSP runs only in AWS env Pre-prod or Prod.
        // So only in Local env you need to have access & secret provided by T4i team
        if(!Application.isAWSEnvironment()){
            logger.info("Not In AWS Environment");
            String brmS3AccessKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_access_key");
            String brmS3Secret = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_secret_key");
            if(brmS3AccessKey.isEmpty() || brmS3Secret.isEmpty()) {
                logger.error("It must have BRM S3 access & secret keys");
                throw new Exception("You must to keep BRM S3 Access & Secret key here to continue");
            }
            fileStore.withS3AccessKey(brmS3AccessKey)
                    .withS3SecretKey(brmS3Secret)
                    .withProxyHost(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_launchdarkly_proxyHost"))
                    .withProxyPort(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_launchdarkly_proxyPort"));
        }
        // no encryption is put here since we are doing encryption explicitly instead of the file-store encryption
        fileStore.withEncryptionType(EncryptionType.NO_ENCRYPTION)
                .withFileStoreType(FileStoreType.S3);
        FileStore result = FileStoreFactory.buildFileStore(fileStore);
        return result;
    }

    public static void initializeAwsS3Client() throws Exception {
        if(Application.isParallelEnv()) {
            logger.info("Parallel Env Mock initializing s3Client Object BRMS3UploadUtils Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile() +" s3Client="+s3Client);
            s3Client = new MockAmazonS3();
            logger.info("Parallel Env Mock initialised s3Client Object BRMS3UploadUtils Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile() +" s3Client="+s3Client);
            return;
        }
        logger.info("Setting up S3 object BRMS3UploadUtils");

        logger.info("Getting the aws S3 credentials, EC2 instance profile or credential file");
        String brmS3AccessKey = "";
        String brmS3Secret = "";
        if(!Application.isAWSEnvironment()) {
            logger.info("Not In AWS Environment");
            brmS3AccessKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_access_key");
            brmS3Secret = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_secret_key");
            if (brmS3AccessKey.isEmpty() || brmS3Secret.isEmpty()) {
                logger.error("It must have BRM S3 access & secret keys");
                throw new Exception("You must to keep BRM S3 Access & Secret key here to continue");
            }
        }

        AWSCredentialsProviderChain awsCredentialsProviderChain = new AWSCredentialsProviderChain(
                // First we'll check for EC2 instance profile credentials.
                new InstanceProfileCredentialsProvider(false),

                // If we're not on an EC2 instance, fall back to checking for
                // credentials in the local configuration file.
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(brmS3AccessKey,brmS3Secret ))
        );

        if (awsCredentialsProviderChain.getCredentials() != null) {
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(awsCredentialsProviderChain)
                    .withRegion(Regions.DEFAULT_REGION)
                    .withClientConfiguration(new ClientConfiguration().withSignerOverride("AWSS3V4SignerType"))
                    .build();
        }
        logger.info("Initialized S3 client");
    }

    // used to upload a file to S3. bucketName - S3 bucket name, remoteFileName - remote file absolute path on the S3 bucket, file - the file to be uploaded
    public static void uploadFileToBRMS3(String remoteDir, String uploadFileName) throws S3UploadException,S3ConnectionException{
        logger.info("Starting S3 upload Dir: " + remoteDir + "FileName: " + uploadFileName);
        try{
            initializeAwsS3Client();

        }catch (Exception e){
            logger.error("Connect to BRM Failed " + e);
            throw new S3ConnectionException("Exception while getting FileStore to upload BRM AWS S3 service " , e);
        }

        try{
            File file = new File(uploadFileName);
            String remoteFileName = getFullRemoteFilePath(remoteDir, file.getName());
            logger.info("Starting upload to BRM s3 for file "+ uploadFileName +" to S3 bucket "+ BRM_S3_BUCKET +" and the remote directory on S3 is: "+remoteFileName);

            PutObjectRequest putObjectRequest = new PutObjectRequest(BRM_S3_BUCKET, remoteFileName, file);
            putObjectRequest.withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
            s3Client.putObject(putObjectRequest);

            logger.info("completed the upload to BRM s3 for the above file");
        }catch (Exception e){
            logger.error("Upload to BRM Failed " + e);
            throw new S3UploadException("Exception while uploading the file to BRM S3" , e);
        }

    }

    // used to upload a file to S3. bucketName - S3 bucket name, remoteFileName - remote file absolute path on the S3 bucket, file - the file to be uploaded
    public static void uploadToS3FileStore(String remoteDir, String uploadFileName) throws S3UploadException,S3ConnectionException{
        logger.info("Starting S3 upload Dir: " + remoteDir + "FileName: " + uploadFileName);
        FileStore fileStore;
        File file = new File(uploadFileName);
        try{
            fileStore = getFileStore();

        }catch (Exception e){
            logger.error("Connect to BRM Failed " + e);
            throw new S3ConnectionException("Exception while getting FileStore to upload BRM AWS S3 service " , e);
        }

        try{
            String remoteFileName = getFullRemoteFilePath(remoteDir, file.getName());
            logger.info("Starting upload to BRM s3 for file "+ uploadFileName +" to S3 bucket "+ BRM_S3_BUCKET +" and the remote directory on S3 is: "+remoteFileName);
            fileStore.writeFile(BRM_S3_BUCKET,remoteFileName,file);
            logger.info("completed the upload to BRM s3 for the above file");
        }catch (Exception e){
            logger.error("Upload to BRM Failed " + e);
            throw new S3UploadException("Exception while uploading the file to BRM S3" , e);
        }

    }

    // used to download the file from S3 to local file storage. bucketName - S3 bucket name, remoteFileName - remote file absolute path on the S3 bucket, localFileName - local file absolute path on the local file storage.
    public static void downloadFromS3FileStore(String remoteDir, String localFileName) throws S3DownloadException,S3ConnectionException{
        FileStore fileStore;
        try{
            fileStore = getFileStore();
        }catch (Exception e){
            throw new S3ConnectionException("Exception while connecting to BRM AWS S3 service " , e);
        }

        try{
            File file = new File(localFileName);
            String remoteFileName = getFullRemoteFilePath(remoteDir, file.getName());
            if(!file.exists()){
                // reading the S3 file as input stream and writing the input stream to the local file.
                logger.info("Starting download from BRM S3 for bucket "+ BRM_S3_BUCKET +" and for the remote file "+remoteFileName+" and the local file name for this is where it is downloaded is "+localFileName);
                InputStream inputStream = fileStore.readFileAsStream(BRM_S3_BUCKET,remoteFileName);
                FileUtils.copyInputStreamToFile(inputStream,file);
                logger.info("Completed download of file from BRM S3 for the above file");
            }else{
                // not downloading the file if already present in local.
                logger.info("Not downloading as the local file is already present");
            }
        }catch (Exception e){
            throw new S3DownloadException("Error while downloading the file from BRM S3 " , e);
        }
    }

    // this function removes trailing '/' and adds ending '/' if not present in the remoteDir string. This is done for proper absolute remote file name for S3.
    // trailing '/' creates an empty named folder on S3. missing ending '/' on directory name creates the file on S3 in the wrong directory location and with wrong name.
    public static String getFullRemoteFilePath(String remoteDir, String fileName){
        String remoteFilePath;
        logger.info("Inside getFullRemoteFilePath RemoteFir: " + remoteDir + " FileName: " + fileName);
        String lastChar = remoteDir.substring(remoteDir.length() - 1);
        String firstChar = remoteDir.substring(0,1);

        // removing trailing '/'
        if(firstChar.equals(File.separator)){
            remoteDir = remoteDir.substring(1);
        }

        if(lastChar.equals(File.separator)){
            remoteFilePath = remoteDir+fileName;
        }
        else {
            remoteFilePath =  remoteDir+File.separator+fileName;
        }

        logger.info("Completed getFullRemoteFilePath, returned name: " + remoteFilePath);
        return remoteFilePath;
    }

    // used to upload a file to S3. bucketName - S3 bucket name, remoteFileName - remote file absolute path on the S3 bucket, file - the file to be uploaded
    public static void uploadFileToS3FileStore(String uploadFileName) throws S3UploadException,S3ConnectionException{
        File file =null;
        try{
            initializeAwsS3Client();
        }catch (Exception e){
            logger.error("Connect to BRM Failed " + e);
            throw new S3ConnectionException("Exception while getting FileStore to upload BRM AWS S3 service " , e);
        }

        try{
            logger.info("File absolute path "+uploadFileName);
            if(uploadFileName.contains("PSP_SymphonyUsage")){
                file = new File(uploadFileName);
                String remoteFileName = getFullRemoteFilePath(BRM_S3_UPLOAD_FOLDER, file.getName());
                logger.info("Starting upload to BRM s3 for file "+ uploadFileName +" to S3 bucket "+ BRM_S3_BUCKET +" and the remote directory on S3 is: "+remoteFileName);
                PutObjectRequest putObjectRequest = new PutObjectRequest(BRM_S3_BUCKET, remoteFileName, file);
                putObjectRequest.withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
                s3Client.putObject(putObjectRequest);
            }
            logger.info("completed the upload to BRM s3 for the file"+file.getName());

        }catch (Exception e){
            logger.error("Upload to BRM Failed " + e);
            throw new S3UploadException("Exception while uploading the file to BRM S3" , e);
        }

    }

    /**
     * Download files from TFA and to process them.
     */
    public static void downloadFileFromS3() throws Exception {
        logger.info("Begin downloadFileFromS3.");
        FileStore fs = getFileStore();
        for (String fileName : fs.listFiles(BRM_S3_BUCKET,BRM_S3_ERROR_FOLDER,".*")) {
            if(!fileName.isEmpty() && !fileName.contains("archive") && fileName.endsWith(".csv")){
                logger.info("Downloading started for S3 file="+fileName);
                InputStream inputStream = fs.readFileAsStream(BRM_S3_BUCKET, fileName);
                File file = new File(LOCAL_RECV_DIR+File.separator+ FilenameUtils.getName(fileName));
                String remoteFileName = getFullRemoteFilePath(LOCAL_RECV_DIR, fileName);
                logger.info("Downloading File name "+file.getName() +" with absolute path ="+file.getAbsolutePath() );
                if(!fileName.isEmpty() && FilenameUtils.getName(fileName).startsWith("PSP") && fileName.contains(".csv")){
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        int read;
                        byte[] bytes = new byte[1024];
                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                    }
                    logger.info("Downloading completed form S3 file="+fileName);
                }

            }

        }

        logger.info("End downloadFileFromS3.");
    }


    /**
     * Method to Download the encrypted files from BRM S3 folder using the BRM IDPS key.
     */
    public static void downloadEncryptedFileFromS3() throws Exception {
        logger.info("Begin downloadFileFromS3.");
        FileStore fs = getFileStore();
        File file =null;
        File outputFile =null;

        try{
            for (String fileName : fs.listFiles(BRM_S3_BUCKET,BRM_S3_ERROR_FOLDER,".*")) {
                if(!fileName.isEmpty() && !fileName.contains("archive") && fileName.endsWith(".csv")) {

                    if (!(fileName.contains(CHECK_REPEATED_ERROR_PROCESSING_STR))) {
                        logger.info("Downloading started for S3 file=" + fileName);
                        InputStream inputStream = fs.readFileAsStream(BRM_S3_BUCKET, fileName);
                        file = new File(LOCAL_RECV_DIR + File.separator + FilenameUtils.getName(fileName).replace(".csv", ".txt"));
                        logger.info("Input file created: " + file.getName() + " with path " + file.getPath());
                        outputFile = new File(LOCAL_RECV_DIR + File.separator + FilenameUtils.getName(fileName).replace(".txt", ".csv"));
                        logger.info("Output file created: " + outputFile.getName() + " with path " + outputFile.getPath());
                        String remoteFileName = getFullRemoteFilePath(LOCAL_RECV_DIR, fileName);
                        logger.info("Downloading File name " + fileName);
                        if (!fileName.isEmpty() && FilenameUtils.getName(fileName).startsWith("PSP") && fileName.contains(".csv")) {
                            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                                int read;
                                byte[] bytes = new byte[1024];
                                while ((read = inputStream.read(bytes)) != -1) {
                                    outputStream.write(bytes, 0, read);
                                }
                            }

                        }

                        logger.info("Downloaded file: " + file.getName() + " with path: " + file.getPath());
                        logger.info("Downloaded decrypted file name: " + outputFile.getName() + " with path: " + outputFile.getPath());
                        Key key = EncryptionUtils.getIDPSKeyForBRMEncryption(BRM_IDPS_APIKEY);
                        logger.info("Encryption key for symphony usage file: " + key.getName());
                        StreamUtil.streamDecryptFileSingleThread(key, file, outputFile);
                        //delete the input file after use
                        file.delete();
                        logger.info("Downloading completed form S3 file=" + fileName);


                    }
                    else{
                        logger.info("The filename "+fileName +" is already reporcessed");
                    }
                }


            }

        }catch (Exception e){
            logger.error("Exception occured while downloading the BRMUsage error file from S3 bucket {}",e);
            if(file.exists()){
                file.delete();
            }
            if(outputFile.exists()){
                outputFile.delete();
            }
        }

        logger.info("End downloadFileFromS3.");
    }

    public static void archiveProcessedErroFiles(String fileName) throws S3ConnectionException{
        FileStore fileStore;
        logger.info("Begin archiving Processed Error Files.");

        try {
            initializeAwsS3Client();
            logger.info("Copying file "+fileName + " to " +File.separator+BRM_S3_ARCHIVE_FOLDER+fileName);
            if(!FilenameUtils.getName(fileName).isEmpty() && fileName.contains(".csv")){
                s3Client.copyObject(BRM_S3_BUCKET,BRM_S3_ERROR_FOLDER+fileName,BRM_S3_BUCKET,BRM_S3_ARCHIVE_FOLDER+fileName);
                logger.info("Copied file "+BRM_S3_ERROR_FOLDER+fileName + " from error folder to archive folder " +BRM_S3_ARCHIVE_FOLDER+fileName);
                logger.info("Deleting file "+fileName + " from error folder " + BRM_S3_ERROR_FOLDER);
                s3Client.deleteObject(BRM_S3_BUCKET,BRM_S3_ERROR_FOLDER+fileName);
                logger.info("Deleted file "+fileName + " from  error folder " +BRM_S3_ERROR_FOLDER);
            }

            logger.info("Completed archiving Processed Error files");

        }
        catch (Exception e){
            logger.error("Connect to BRM Failed " + e);
            throw new S3ConnectionException("Exception while archiving Files in BRM AWS S3 " , e.getCause());
        }

    }

    public static void encryptFile(String filePath){
        try {
            File file = new File(filePath);
            Key key = IDPSFileStreamManager.newKeyHandleLatest();
            IDPSInputStream encStream = new IDPSInputStream(new FileInputStream(file),true, key);
            Files.copy(encStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            encStream.close();
        }catch(IdpsException ex) {
            logger.error("Failed to encrypt file: " + filePath + "using IDPS exception: " + ex);
        }catch (IOException ex) {
            logger.error("Failed to encrypt file: " + filePath + "IO exception: " + ex);
        }
    }


}
