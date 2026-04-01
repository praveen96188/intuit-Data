package com.intuit.sbd.payroll.psp.common.utils;

import com.amazonaws.regions.Regions;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.mockutil.MockS3FileStore;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.sbg.shared.filestore.FileStoreFactory;
import com.intuit.sbg.shared.filestore.FileStoreType;
import com.intuit.sbg.shared.filestore.enums.EncryptionType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class S3UploadUtils {
    private static Logger logger = LoggerFactory.getLogger("S3UploadUtils");

    public static String PSP_BATCHJOBS_S3_BUCKET = "psp_batch_s3_bucket";
    public static String PSP_BATCHJOBS_S3_BUCKET_REGION = "psp_batch_s3_bucket_region";
    public static String PSP_BATCHJOBS_S3_ACCESS_KEY = "psp_batch_s3_access_key";
    public static String PSP_BATCHJOBS_S3_SECRET = "psp_batch_s3_secret";
    public static String PSP_LAUNCHDARKLY_PROXY_HOST = "psp_launchdarkly_proxyHost";
    public static String PSP_LAUNCHDARKLY_PROXY_PORT = "psp_launchdarkly_proxyPort";

    // function to get the filestore object initialized with credentials for connecting to S3
    public static FileStore getFileStore(String region, String accessKey, String secretKey, String s3BucketAssumeRole) throws Exception {
        if(Application.isParallelEnv()) {
            logger.info("Parallel Env Mock S3 FileStore Object S3UploadUtils Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile());
            return new MockS3FileStore();
        }
        logger.info("Setting up S3 object S3UploadUtils");

        FileStore.FileStoreBuilder fileStore = FileStore.builder();
        // if IHP env, need access key and secret and launchdarkly proxy config for connecting to S3
        if(!Application.isAWSEnvironment()){
            logger.info("In IHP Environment");
            fileStore.withS3AccessKey(accessKey)
                    .withS3SecretKey(secretKey)
                    .withProxyHost(ConfigurationManager.getSettingValue(ConfigurationModule.Common, PSP_LAUNCHDARKLY_PROXY_HOST))
                    .withProxyPort(ConfigurationManager.getSettingValue(ConfigurationModule.Common, PSP_LAUNCHDARKLY_PROXY_PORT));
        }
        // no encryption is put here since we are doing encryption explicitly instead of the file-store encryption
        fileStore.withRegions(Regions.fromName(region)).withEncryptionType(EncryptionType.NO_ENCRYPTION)
                .withFileStoreType(FileStoreType.S3);

        if(!StringUtils.isEmpty(s3BucketAssumeRole)){
            logger.info("s3 assume role is being set to {}",s3BucketAssumeRole);
            fileStore.withS3RoleName(s3BucketAssumeRole);
        }

        FileStore result = FileStoreFactory.buildFileStore(fileStore);
        return result;
    }


    public static FileStore getFileStore() throws Exception {
        String region = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, PSP_BATCHJOBS_S3_BUCKET_REGION);
        String s3BucketAssumeRole = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iks_batch_s3_iam_assume_role");
        String accessKey = null;
        String secretKey = null;

        if(!Application.isAWSEnvironment()) {
            accessKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, PSP_BATCHJOBS_S3_ACCESS_KEY);
            secretKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, PSP_BATCHJOBS_S3_SECRET);
        }

        return getFileStore(region, accessKey, secretKey, s3BucketAssumeRole);
    }

    // used to upload a file to S3. bucketName - S3 bucket name, remoteFileName - remote file absolute path on the S3 bucket, file - the file to be uploaded
    public static void uploadToS3FileStore(String bucketName, String remoteFileName, File file) throws S3UploadException,S3ConnectionException{
        FileStore fileStore;
        try{
            fileStore = getFileStore();
        }catch (Exception e){
            throw new S3ConnectionException("Error while connecting to AWS S3 service" , e);
        }

        try{
            logger.info("Starting upload to s3 for file "+file.getAbsolutePath()+" to S3 bucket "+bucketName+" and the remote directory on S3 is: "+remoteFileName);
            fileStore.writeFile(bucketName,remoteFileName,file);
            logger.info("completed the upload to s3 for the above file");
        }catch (Exception e){
            throw new S3UploadException("Error while uploading the file to S3" , e);
        }

    }

    public static void uploadAllFiles(String sourceDirectory, String fileNameContains, String bucketName, String destinationPath) throws IOException {
        Objects.requireNonNull(sourceDirectory, "sourceDirectory cannot be null");
        Objects.requireNonNull(fileNameContains, "fileNameContains cannot be null");
        Objects.requireNonNull(bucketName, "bucketName cannot be null");
        Objects.requireNonNull(destinationPath, "destinationPath cannot be null");

        Path sourceDirectoryPath = Paths.get(sourceDirectory);
        Files.list(sourceDirectoryPath).filter(file -> file.toString().contains(fileNameContains)).forEach(path -> {
            upload(bucketName, path, destinationPath);
        });
    }

    public static void upload(String bucketName, Path sourcePath, String destinationPath) {
        Objects.requireNonNull(bucketName, "bucketName cannot be null");
        Objects.requireNonNull(sourcePath, "sourcePath cannot be null");
        Objects.requireNonNull(destinationPath, "destinationPath cannot be null");

        String fileName = FilenameUtils.getName(sourcePath.toString());
        try {
            String remoteFileName = Paths.get(destinationPath, fileName).toString();
            S3UploadUtils.uploadToS3FileStore(bucketName, remoteFileName, sourcePath.toFile());
        } catch (S3UploadException | S3ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    // used to download the file from S3 to local file storage. bucketName - S3 bucket name, remoteFileName - remote file absolute path on the S3 bucket, localFileName - local file absolute path on the local file storage.
    public static void downloadFromS3FileStore(String bucketName, String remoteFileName, String localFileName) throws S3DownloadException,S3ConnectionException{
        FileStore fileStore;
        try{
            fileStore = getFileStore();
        }catch (Exception e){
            throw new S3ConnectionException("Error while connecting to AWS S3 service" , e);
        }

        try{
            File file = new File(localFileName);
            if(!file.exists()){
                // reading the S3 file as input stream and writing the input stream to the local file.
                logger.info("Starting download from S3 for bucket "+bucketName+" and for the remote file "+remoteFileName+" and the local file name for this is where it is downloaded is "+localFileName);
                InputStream inputStream = fileStore.readFileAsStream(bucketName,remoteFileName);
                FileUtils.copyInputStreamToFile(inputStream,file);
                logger.info("Completed download of file from s3 for the above file");
            }else{
                // not downloading the file if already present in local.
                logger.info("Not downloading as the local file is already present");
            }
        }catch (Exception e){
            throw new S3DownloadException("Error while downloading the file from S3" , e);
        }
    }

    public static void downloadAndArchiveAllFiles(String bucketName, String sourceFolder, String destinationFolder, String archiveFolder) throws Exception {
        downloadAndArchiveAllFiles(getFileStore(), bucketName, sourceFolder, destinationFolder, archiveFolder);
    }

    public static void downloadAndArchiveAllFiles(FileStore fileStore, String bucketName, String sourceFolder, String destinationFolder, String archiveFolder) throws Exception {
        Objects.requireNonNull(fileStore, "fileStore cannot be null");
        Objects.requireNonNull(bucketName, "bucketName cannot be null");
        Objects.requireNonNull(sourceFolder, "sourceFolder cannot be null");
        Objects.requireNonNull(destinationFolder, "destinationFolder cannot be null");

        for (String fileName : fileStore.listFiles(bucketName, sourceFolder, ".*")) {

            if(StringUtils.isEmpty(FilenameUtils.getName(fileName))) {
                continue;
            }

            File file = new File(destinationFolder, FilenameUtils.getName(fileName));
            downloadFromS3FileStore(bucketName, fileName, file.toString());

            if(StringUtils.isEmpty(archiveFolder)) {
                continue;
            }
            logger.info("Archive started S3 file="+fileName);
            archiveFile(fileStore, bucketName, fileName, archiveFolder);
            logger.info("Archive completed S3 file="+fileName);
        }
    }

    public static void archiveFile(FileStore fileStore, String bucketName, String fileName, String archiveFolder) throws Exception {
        fileStore.moveFile(bucketName,fileName,archiveFolder+FilenameUtils.getName(fileName));
    }

    // this function removes trailing '/' and adds ending '/' if not present in the remoteDir string. This is done for proper absolute remote file name for S3.
    // trailing '/' creates an empty named folder on S3. missing ending '/' on directory name creates the file on S3 in the wrong directory location and with wrong name.
    private static String getFullRemoteFilePath(String remoteDir, String fileName){
        logger.info("Came into getFullRemoteFilePath function");
        logger.info("Before getFullRemoteFilePath:RemoteFir:FileName" + remoteDir + ":" + fileName);
        String lastChar = remoteDir.substring(remoteDir.length() - 1);
        String firstChar = remoteDir.substring(0,1);

        // removing trailing '/'
        if(firstChar.equals(File.separator)){
            remoteDir = remoteDir.substring(1);
            logger.info("firstChar After getFullRemoteFilePath:RemoteFir:FileName" + remoteDir);
        }

        if(lastChar.equals(File.separator)){
            logger.info("Lastchar After getFullRemoteFilePath:RemoteFir:FileName" + remoteDir+fileName);
            logger.info("Completed getFullRemoteFilePath function");
            return remoteDir+fileName;
        }
        else {
            logger.info("After getFullRemoteFilePath:RemoteFir:FileName" + remoteDir+File.separator+fileName);
            logger.info("Completed getFullRemoteFilePath function");
            return remoteDir+File.separator+fileName;
        }

    }

    public static String archive(String batchJobName, String archiveDir, String fileName) throws S3ConnectionException,S3UploadException{

        File file = new File(fileName);
        String result = null;
        String logFmt = "Archive Step : BatchjobName: " + batchJobName + " :archive Dir: " + archiveDir + " :fileName: " + fileName;
        logger.info(logFmt);
        // checking first if the parameters sent are valid
        if (archiveDir == null || archiveDir.trim().length() == 0) {
            logger.error("archive directory parameter to archive function is null.");
        }
        if(!file.exists()){
            //no file exists cannot upload
            String errorLog = String.format("%s File doesnot exists for BatchJob Name %s",fileName,batchJobName);
            throw new RuntimeException("File doesnot exists"  + errorLog);
        }
        // checking for the batchjob name parameter if it is valid or not
        if(batchJobName == null){
            logger.error("batch job name parameter to archive function is null.");
        }else{
            try{
                BatchJobType.valueOf(batchJobName);
            }catch (Exception e){
                logger.error("batch job name parameter for archive parameter is not a valid parameter.");
            }
        }
        if(Application.isIntegrationTestEnvironment()) {
            logger.info("In intg test env Flag S3 disabled");
            result = moveFile(file, archiveDir);
            return result;
        }

        String bucketName = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, PSP_BATCHJOBS_S3_BUCKET);
        logger.info("In archive AWS env");

        String fullRemoteFileName = S3UploadUtils.getFullRemoteFilePath(archiveDir,file.getName());
        S3UploadUtils.uploadToS3FileStore(bucketName,fullRemoteFileName, file);

        if(Application.isParallelEnv()) {
            logger.info("Parallel Env Mock delete file in S3 Archive Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile());
        } else {
            logger.info("Deleting file after S3 archiving");
            file.delete();
        }

        result = File.separator+fullRemoteFileName;

        return result;
    }

    private static String moveFile(File file, String archiveDir) {
        String result;
        File archiveDirFile = new File(archiveDir.trim());
        if (!archiveDirFile.exists()) {
            throw new RuntimeException(archiveDirFile.getAbsolutePath() + " does not exist. Unable to archive " + file.getAbsolutePath());
        }
        // if file is already in archive dir (?)
        if (archiveDirFile.getPath().equals(file.getParentFile().getPath())) {
            result = file.getAbsolutePath();
        } else {
            File targetFile = new File(archiveDir + "/" + file.getName());
            file.renameTo(targetFile);
            result = targetFile.getAbsolutePath();
        }
        return result;
    }

    /**
     * @param archiveDir
     * @param fileName
     * @return
     * @throws S3ConnectionException
     * @throws S3UploadException
     */
    public static String archiveWithNoBatchJob(String archiveDir, String fileName) throws S3ConnectionException,S3UploadException{

        File file = new File(fileName);
        String result = null;
        String logFmt = "Archive Step :  :archive Dir: " + archiveDir + " :fileName: " + fileName;
        logger.info(logFmt);
        // checking first if the parameters sent are valid
        if (archiveDir == null || archiveDir.trim().length() == 0) {
            logger.error("archive directory is null.");
        }
        if(!file.exists()){
            //no file exists cannot upload
            String errorLog = String.format("%s File doesnot exists %s",fileName);
            throw new RuntimeException("File doesnot exists"  + errorLog);
        }
        if(Application.isAWSEnvironment()){
            String bucketName = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, PSP_BATCHJOBS_S3_BUCKET);
            logger.info("In archive AWS env");

            String fullRemoteFileName = S3UploadUtils.getFullRemoteFilePath(archiveDir,file.getName());
            S3UploadUtils.uploadToS3FileStore(bucketName,fullRemoteFileName, file);
            file.delete();

            result = File.separator+fullRemoteFileName;
        }
        return result;
    }

}
