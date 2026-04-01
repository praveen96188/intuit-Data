package com.intuit.sbd.payroll.psp.common.utils.batchjobs3util;
import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3DownloadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSInputStream;
import com.intuit.sbd.payroll.psp.common.utils.mockutil.MockS3FileStore;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.sbg.shared.filestore.FileStoreFactory;
import com.intuit.sbg.shared.filestore.FileStoreType;
import com.intuit.sbg.shared.filestore.enums.EncryptionType;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * @author snasim
 * Utility tool for commandline file encryption,decryption,s3 download and bucket file listing
 */
public class IDPSS3FileUtility {
    private static Logger logger = LoggerFactory.getLogger("IDPSS3FileUtility");

    /**
     * @param sourceFileName
     * @param targetFileName
     */
    public static void encryptFile(String sourceFileName,String targetFileName){
        if(sourceFileName.equals(targetFileName)){
            System.out.println("Source File and target file locations are same");
        }
        try {
            encryptFile(new File(sourceFileName),new File(targetFileName));
        }catch(Exception e){
            logger.error("Error in encytping file : " + sourceFileName + e.getMessage());
        }
    }

    /**
     * @param sourceFileName
     * @param targetFileName
     */
    public static void encryptFile(File sourceFileName,File targetFileName){
        if(sourceFileName.equals(targetFileName)){
            System.out.println("Source File and target file locations are same");
        }
        try {
            Key key  = newKeyHandleLatest();
            StreamUtil.streamEncryptFileSingleThread(key,sourceFileName,targetFileName);
        }catch(Exception e){
            logger.error("Error in encytping file : " + sourceFileName + e.getMessage());
        }
    }

    /**
     * @param sourceFileName
     * @param targetFileName
     */
    public static void decryptFile(String sourceFileName,String targetFileName){
        if(sourceFileName.equals(targetFileName)){
            System.out.println("Source File and target file locations are same");
        }
        try {
            decryptFile(new File(sourceFileName),new File(targetFileName));
        }catch(Exception e){
            logger.error("Error in decrypting file : " + sourceFileName + e.getMessage());
        }
    }

    /**
     * @param sourceFileName
     * @param targetFileName
     */
    public static void decryptFile(File sourceFileName,File targetFileName){
        if(sourceFileName.equals(targetFileName)){
            System.out.println("Source File and target file locations are same");
        }
        try {
            Key key  = newKeyHandleLatest();
            StreamUtil.streamDecryptFileSingleThread(key,sourceFileName,targetFileName);
        }catch(Exception e){
            logger.error("Error in encytping file : " + sourceFileName + e.getMessage());
        }
    }

    /**
     * @param directoryPath
     * @param targetDirectoryPath
     */
    public static void encryptAllFiles(String directoryPath,String targetDirectoryPath){
        File[] files = getAllFilesFromDir(directoryPath);

        for (File file : files)
        {
            //String absPath = file.getAbsolutePath();

            encryptFile(new File(directoryPath,file.getName()),new File(targetDirectoryPath , file.getName()));
        }
    }

    /**
     * @param directoryPath
     * @param targetDirectoryPath
     */
    public static void decryptAllFiles(String directoryPath,String targetDirectoryPath){
        File[] files = getAllFilesFromDir(directoryPath);
        for (File file : files)
        {
            decryptFile(new File(directoryPath,file.getName()),new File(targetDirectoryPath , file.getName()));
        }
    }

    /**
     * @param directoryPath
     * @return
     */
    public static File[] getAllFilesFromDir(String directoryPath){
        File folder = new File(directoryPath);

        //Implementing FilenameFilter to retrieve only txt files

        FilenameFilter txtFileFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                if(name.endsWith(".pgp"))
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        };

        //Passing txtFileFilter to listFiles() method to retrieve only txt files

        File[] files = folder.listFiles(txtFileFilter);

        for (File file : files)
        {
            System.out.println(file.getName());
        }
        return files;
    }

    /**
     * @return
     * @throws Exception
     */
    public static FileStore getFileStore() throws Exception {
        if(Application.isParallelEnv()) {
            logger.info("Parallel Env Mock S3 FileStore Object IDPSS3FileUtility Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile());
            return new MockS3FileStore();
        }
        logger.info("Setting up S3 object IDPSS3FileUtility");

        FileStore.FileStoreBuilder fileStore = FileStore.builder();
        if(!Application.isAWSEnvironment()){
            fileStore.withS3AccessKey(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,S3UploadUtils.PSP_BATCHJOBS_S3_ACCESS_KEY))
                    .withS3SecretKey(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,S3UploadUtils.PSP_BATCHJOBS_S3_SECRET))
                    .withProxyHost(ConfigurationManager.getSettingValue(ConfigurationModule.Common, S3UploadUtils.PSP_LAUNCHDARKLY_PROXY_HOST))
                    .withProxyPort(ConfigurationManager.getSettingValue(ConfigurationModule.Common, S3UploadUtils.PSP_LAUNCHDARKLY_PROXY_PORT));
        }
        fileStore.withEncryptionType(EncryptionType.NO_ENCRYPTION)
                .withFileStoreType(FileStoreType.S3);

        String s3BucketAssumeRole = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,"psp_iks_batch_s3_iam_assume_role");
        if(!StringUtils.isEmpty(s3BucketAssumeRole)){
            logger.info("s3 assume role is being set to {}",s3BucketAssumeRole);
            fileStore.withS3RoleName(s3BucketAssumeRole);
        }
        FileStore result = FileStoreFactory.buildFileStore(fileStore);
        return result;
    }

    /**
     * @param bucketName
     * @param rootPath
     * @param filterRegex
     * @return
     * @throws S3ConnectionException
     */
    public static Set<String> listFilesFromS3(String bucketName, String rootPath, String filterRegex) throws S3ConnectionException {
        FileStore fileStore;
        Set<String> filenameList = null;
        try{
            fileStore = getFileStore();
        }catch (Exception e){
            String str = org.apache.http.conn.ssl.SSLConnectionSocketFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            throw new S3ConnectionException("Error while connecting to AWS S3 service",e);
        }

        try{
            filenameList = fileStore.listFiles(bucketName,rootPath,filterRegex);
        }catch (Exception e){
            logger.error("Error in retrieving file from bucket : " + bucketName + e.getMessage());
        }
        for(String s:filenameList){
            System.out.println(s);
        }
        return filenameList;
    }

    /**
     * @param bucketName
     * @param remoteFileName
     * @param localFileName
     * @param decrypt
     * @throws S3DownloadException
     * @throws S3ConnectionException
     */
    public static void downloadFileFromS3(String bucketName, String remoteFileName, String localFileName,boolean decrypt) throws S3DownloadException,S3ConnectionException{
        FileStore fileStore;
        File file;
        try{
            fileStore = getFileStore();
        }catch (Exception e){
            throw new S3ConnectionException("Error while connecting to AWS S3 service",e);
        }

        try{
            Key key  = newKeyHandleLatest();
            InputStream inputStream = fileStore.readFileAsStream(bucketName,remoteFileName);
            file = new File(localFileName);
            if(decrypt) {
                IDPSInputStream is = new IDPSInputStream(inputStream, key);
                FileUtils.copyInputStreamToFile(is, file);
            }
            else{
                FileUtils.copyInputStreamToFile(inputStream, file);
            }
        }catch (Exception e){
            throw new S3DownloadException("Error while downloading the file from S3",e);
        }
        if(file != null)
            System.out.println("Downloaded File Successfully at:" + file.getAbsolutePath());
    }

    /**
     * @param src
     * @param ext
     * @return
     */
    public static File renameFileExt(File src, String ext){
        String fileWithoutExt = StreamUtil.FileWithoutExt(src.getAbsolutePath());
        String fileName = fileWithoutExt + "." + ext;
        File target = new File(fileName);
        src.renameTo(target);
        return target;

    }

    /**
     * @param file
     * @return
     */
    public static File encryptFileUsingIDPS(File file){
        String fileWithoutExt = StreamUtil.FileWithoutExt(file.getAbsolutePath());
        String fileName = fileWithoutExt + ".idps";
        String fileExt =  FilenameUtils.getExtension(file.getName());
        File encFile = new File(fileName);
        try{
            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            InputStream inpStream = new FileInputStream(file);
            int ret = StreamUtil.copyEncryptedBytes(inpStream,encFile,key);
            if(ret < 0){
                logger.warn("No file was written in encryption");
            }
            else{
                if(file.delete())//delete unencrypted file
                {
                    encFile = renameFileExt(encFile,fileExt);
                }
                else{
                    logger.error("Error in deleting file" + file.getName());
                }

            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return encFile;
    }
    public static File[] getAllFilesFromDir(String directoryPath,long hours){
        File folder = new File(directoryPath);

        //Implementing FilenameFilter to retrieve only txt files

        FileFilter txtFileFilter = new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                long purgeTime = System.currentTimeMillis() - (hours * 60L * 60L * 1000L);

                if(file.lastModified() < purgeTime){
                    return true;
                }
                return false;
            }
        };

        //Passing txtFileFilter to listFiles() method to retrieve only txt files

        File[] files = folder.listFiles(txtFileFilter);

        for (File file : files)
        {
            if (file.isDirectory()){
                getAllFilesFromDir(file.getPath(),hours);
            }
            else {
                System.out.println(file.getPath());
            }

        }
        return files;
    }
    /**
     * @param bucketName
     * @param remoteFileName
     * @param localFileName
     * @throws S3UploadException
     * @throws S3ConnectionException
     */
    public static void uploadFileToS3(String bucketName, String remoteFileName, String localFileName) throws S3UploadException,S3ConnectionException{
        FileStore fileStore;
        File file = new File(localFileName);
        try{
            fileStore = getFileStore();
        }catch (Exception e){
            throw new S3ConnectionException("Error while connecting to AWS S3 service",e);
        }
        try{
            if(StreamUtil.isFileIDPSEncrypted(file)) {
                fileStore.writeFile(bucketName, remoteFileName, file);
            }
            else{ //encypt and upload
                file = encryptFileUsingIDPS(file);
                fileStore.writeFile(bucketName, remoteFileName, file);
            }
        }catch (Exception e){
            throw new S3UploadException("Error while uploading the file to S3",e);
        }

    }
    public static void CompareFilesInS3(File[] fileNfsList,Set<String> s3List){
        //remove path from s3list
        Set<String> s3FileName = new HashSet<>();
        for (Iterator<String> it = s3List.iterator(); it.hasNext(); ) {
            String f = it.next();
            //
            File fl = new File(f);
            s3FileName.add(fl.getName());
        }

        if(s3FileName!= null) {
            for (File f : fileNfsList) {
                if (!s3FileName.contains(f.getName())) {
                    logger.error("Following file was not found in s3:" + f.getPath());
                    System.out.println("Following file was not found in s3:" + f.getPath());
                }
            }
        }
    }

    /**
     * @param sourceFileName
     * @param targetFileName
     */
    public static void messageDigest(String sourceFileName,String targetFileName){
        if(sourceFileName.equals(targetFileName)){
            logger.info("Source File and target file names are same");
        }
        try {
            messageDigest(new File(sourceFileName),new File(targetFileName));
        }catch(Exception e){
            logger.error("Error in message digest : " + sourceFileName + e.getMessage());
        }
    }

    /**
     * @param sourceFileName
     * @param targetFileName
     */
    public static void messageDigest(File sourceFileName,File targetFileName){

        try {
            String srcCheckSum = getCheckSum(sourceFileName);
            String targetCheckSum = getCheckSum(targetFileName);
            logger.info("srcCheckSum="+srcCheckSum);
            logger.info("targetCheckSum="+targetCheckSum);

            if(srcCheckSum.equals(targetCheckSum)){
                logger.info("Checksum value for sourceFileName="+sourceFileName+
                        " and targetFileName="+targetFileName+" are same");
            } else {
                logger.info("Checksum value for sourceFileName="+sourceFileName+
                        " and targetFileName="+targetFileName+" are NOT same");
            }
        }catch(Exception e){
            logger.error("Error in getting checksum :" + e.getMessage());
        }
    }

    private static String getCheckSum(File fileName) throws Exception {

        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(fileName);
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            // read the data from file and update that data in
            // the message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                messageDigest.update(byteArray, 0, bytesCount);
            }
            ;

            // close the input stream
            fis.close();

            // store the bytes returned by the digest() method
            byte[] bytes = messageDigest.digest();

            for (int i = 0; i < bytes.length; i++) {

                // the following line converts the decimal into
                // hexadecimal format and appends that to the
                // StringBuilder object
                sb.append(Integer
                        .toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            sb.toString();

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return sb.toString();
    }

    public static void main(String[] args){
        // create the command line parser
       CommandLineParser parser = new PosixParser();
        final String applicationName = "IDPSS3FileUtility";
// create the Options
        Options options = getOptions();

        if (args.length < 1){
            printUsage(applicationName + " (Posix)", options, System.out);

        }

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            String srcFile = "";
            String trgFile = "";

            // validate that block-size has been set
            if( line.hasOption( "e" ) ) { //encryption
                // print the value of block-size
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("t"))){
                    System.out.println( line.getOptionValue( "t" ) );
                    trgFile = line.getOptionValue( "t" );
                }
                encryptFile(srcFile,trgFile);
            }
            if(line.hasOption("d")) {//decrypt file
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("t"))){
                    System.out.println( line.getOptionValue( "t" ) );
                    trgFile = line.getOptionValue( "t" );
                }
                decryptFile(srcFile,trgFile);
            }
            if(line.hasOption("ed")) {//encrypt directory
                if(line.hasOption(("sd"))){
                    System.out.println( line.getOptionValue( "sd" ) );
                    srcFile = line.getOptionValue( "sd" );
                }
                if(line.hasOption(("td"))){
                    System.out.println( line.getOptionValue( "td" ) );
                    trgFile = line.getOptionValue( "td" );
                }
                encryptAllFiles(srcFile,trgFile);
            }
            if(line.hasOption("dd")) {//encrypt directory
                if(line.hasOption(("sd"))){
                    System.out.println( line.getOptionValue( "sd" ) );
                    srcFile = line.getOptionValue( "sd" );
                }
                if(line.hasOption(("td"))){
                    System.out.println( line.getOptionValue( "td" ) );
                    trgFile = line.getOptionValue( "td" );
                }
                decryptAllFiles(srcFile,trgFile);
            }
            if(line.hasOption("lf")) {//list files from s3 bucket
                String bucketName = "";
                String regPatt = "";
                if(line.hasOption(("b"))){
                    System.out.println( line.getOptionValue( "b" ) );
                    bucketName = line.getOptionValue( "b" );
                }
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("r"))){
                    System.out.println( line.getOptionValue( "r" ) );
                    regPatt = line.getOptionValue( "r" );
                }
                try{
                listFilesFromS3(bucketName,srcFile,regPatt);
                }catch(Exception e){
                    System.out.println("Error in getting list of files frm s3:" + e.getMessage());
                }

            }
            if(line.hasOption("fs")) {//get file from s3 bucket
                String bucketName = "";
                if(line.hasOption(("b"))){
                    System.out.println( line.getOptionValue( "b" ) );
                    bucketName = line.getOptionValue( "b" );
                }
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("t"))){
                    System.out.println( line.getOptionValue( "t" ) );
                    trgFile = line.getOptionValue( "t" );
                }
                try{
                    downloadFileFromS3(bucketName,srcFile,trgFile,true);
                }catch(Exception e){
                    System.out.println("Error in getting list of files frm s3:" + e.getMessage());
                }

            }
            if(line.hasOption("fe")) {//get file from s3 bucket
                String bucketName = "";
                if(line.hasOption(("b"))){
                    System.out.println( line.getOptionValue( "b" ) );
                    bucketName = line.getOptionValue( "b" );
                }
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("t"))){
                    System.out.println( line.getOptionValue( "t" ) );
                    trgFile = line.getOptionValue( "t" );
                }
                try{
                    downloadFileFromS3(bucketName,srcFile,trgFile,false);
                }catch(Exception e){
                    System.out.println("Error in getting list of files frm s3:" + e.getMessage());
                }

            }
            if(line.hasOption("fu")) {//upload file to s3 bucket
                String bucketName = "";
                if(line.hasOption(("b"))){
                    System.out.println( line.getOptionValue( "b" ) );
                    bucketName = line.getOptionValue( "b" );
                }
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("t"))){
                    System.out.println( line.getOptionValue( "t" ) );
                    trgFile = line.getOptionValue( "t" );
                }
                try{
                    downloadFileFromS3(bucketName,srcFile,trgFile,false);
                }catch(Exception e){
                    System.out.println("Error in uploading file to s3:" + e.getMessage());
                }

            }
            if(line.hasOption("fd")) {//upload file to s3 bucket
                String bucketName = "";
                if(line.hasOption(("b"))){
                    System.out.println( line.getOptionValue( "b" ) );
                    bucketName = line.getOptionValue( "b" );
                }
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("t"))){
                    System.out.println( line.getOptionValue( "t" ) );
                    trgFile = line.getOptionValue( "t" );
                }
                try{
                    uploadFileToS3(bucketName,srcFile,trgFile);
                }catch(Exception e){
                    System.out.println("Error in uploading file to s3:" + e.getMessage());
                }

            }
            if(line.hasOption("nf")) {//list files based on time elapsed of file modified
                long tElapsed = 0;
                if(line.hasOption(("sd"))){
                    System.out.println( line.getOptionValue( "sd" ) );
                    srcFile = line.getOptionValue( "sd" );
                }
                if(line.hasOption(("hr"))){
                    System.out.println( line.getOptionValue( "hr" ) );

                    String str = line.getOptionValue( "hr" );
                    tElapsed = Long.parseLong(str);
                }
                getAllFilesFromDir(srcFile,tElapsed);
            }
            if(line.hasOption("cf")) {//list files based on time elapsed of file modified
                long tElapsed = 0;
                String srcDir = "";
                if(line.hasOption(("sd"))){
                    System.out.println( line.getOptionValue( "sd" ) );
                    srcDir = line.getOptionValue( "sd" );
                }
                if(line.hasOption(("hr"))){
                    System.out.println( line.getOptionValue( "hr" ) );

                    String str = line.getOptionValue( "hr" );
                    tElapsed = Long.parseLong(str);
                }
                String bucketName = "";
                String regPatt = "";
                if(line.hasOption(("b"))){
                    System.out.println( line.getOptionValue( "b" ) );
                    bucketName = line.getOptionValue( "b" );
                }
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("r"))){
                    System.out.println( line.getOptionValue( "r" ) );
                    regPatt = line.getOptionValue( "r" );
                }
                try{
                    File[] fileNfsList = getAllFilesFromDir(srcDir,tElapsed);
                    Set<String> s3List  = listFilesFromS3(bucketName,srcFile,regPatt);
                    CompareFilesInS3(fileNfsList,s3List);
                }catch(Exception e){
                    System.out.println("Error in getting list of files frm s3:" + e.getMessage());
                }

            }
            if( line.hasOption( "h" ) ) {
                // print the value of block-size
                printHelp(
                        options, 80, "POSIX HELP", "End of POSIX Help",
                        3, 5, true, System.out);
            }
            if( line.hasOption( "md5" ) ) { //messageDigest
                // print the value of block-size
                if(line.hasOption(("s"))){
                    System.out.println( line.getOptionValue( "s" ) );
                    srcFile = line.getOptionValue( "s" );
                }
                if(line.hasOption(("t"))){
                    System.out.println( line.getOptionValue( "t" ) );
                    trgFile = line.getOptionValue( "t" );
                }
                messageDigest(srcFile,trgFile);
            }
        }
        catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }
    }
    private static Options getOptions() {

        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        options.addOption("e", "encryptFile", false, "encrypt with needed option -s=srcFile -t=trgFile");
        options.addOption("s", "sourceFile", true, "Source file for encrypting or decrypting");
        options.addOption("t", "targetFile", true, "Target file generated after encryption or decryption");
        options.addOption("ed", "encryptDir", false, "encrypts all files in dir with option -sd=srcDir -td=trgDir");
        options.addOption("d", "decryptFile", false, "decrypt with needed option -s=srcFile -t=trgFile");
        options.addOption("dd", "decryptDir", false, "decrypts all files in dir with option -sd=srcDir -td=trgDir");
        options.addOption("lf", "listS3Files", false, "list all files in bucket use with option -b=bucketName -s=rootPath -r=regexPattern");
        options.addOption("fs", "getS3DecFile", false, "get decrypted file from bucket use with option -b=bucketName -s=srcFile -t=trgLocation");
        options.addOption("sd", "sourceDir", true, "Source dir for encrypting or decrypting");
        options.addOption("td", "targetDir", true, "Target dir for keeping encrypted or decrypted files");
        options.addOption("b", "bucketName", true, "Bucket Name for S3 to list files");
        options.addOption("r", "regPattern", true, "regex pattern to retrieve files from s3 bucket");
        options.addOption("fe", "getS3EncFile", false, "get encrypted file from bucket use with option -b=bucketName -s=srcFile -t=trgLocation");
        options.addOption("fu", "uploadS3EncFile", false, "upload encrypted file to bucket(checks if encrypted else encrypts and sends) use with option -b=bucketName -s=srcFile -t=trgLocation");
        options.addOption("nf","nfsFileList",false,"get file list from a directory based on time filter use with options -sd=fileDir -hr=hour");
        options.addOption("cf","compareFileList",false,"get file list from a directory and s3 and compare for s3 archive use with options -sd=fileDir -hr=hour -b=bucketName -s=rootPath -r=regexPattern");
        options.addOption("hr","hour",true,"number of hours for which file was last created or modified to get old files");
        options.addOption("md5", "messageDigest", false, "hash value with needed option -s=srcFile -t=trgFile");

        return options;
    }

    public static void printUsage(
            final String applicationName,
            final Options options,
            final OutputStream out)
    {
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter usageFormatter = new HelpFormatter();
        usageFormatter.printUsage(writer, 80, applicationName, options);
        writer.flush();
    }
    public static void printHelp(
            final Options options,
            final int printedRowWidth,
            final String header,
            final String footer,
            final int spacesBeforeOption,
            final int spacesBeforeOptionDescription,
            final boolean displayUsage,
            final OutputStream out)
    {
        final String commandLineSyntax = "java com.intuit.sbd.payroll.psp.common.utils.batchjobs3util.IDPSS3FileUtility ";
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                commandLineSyntax,
                header,
                options,
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);
        writer.flush();
    }
    private static IdpsClient idpsClient ;

    private static String keyName = "PSP/" + "TestJob" + "AES256_GCM";

    public static IdpsClient getIdpsClient() {
        return idpsClient;
    }

    static {
        ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration("PSP-Keys");
        String apiKeyId = config.getString("psp_idps_api_key_id");
        String apiSecretKey = config.getString("psp_idps_api_secret_key");
        String apiPolicy = config.getString("psp_idps_api_policy");
        String accessType = config.getString("psp_idps_access_type");
        String endpoint = config.getString("psp_idps_endpoint");
        keyName = config.getString("psp_idps_batchjobs_keyname");
        Properties idpsProperties = new Properties();
        idpsProperties.setProperty("endpoint", endpoint);
        if(!apiPolicy.isEmpty()){
            logger.info("apiPolicy : " + apiPolicy);
            idpsProperties.setProperty("policy_id", apiPolicy);
            if (StringUtils.isNotBlank(accessType)) {
                idpsProperties.setProperty("access_type", accessType);
            }
        } else {
            idpsProperties.setProperty("api_key_id", apiKeyId);
            idpsProperties.setProperty("api_secret_key", apiSecretKey);
        }

        try {
            idpsClient = IdpsClient.Factory.newInstance(idpsProperties);
            idpsClient.setCryptoLocation(IdpsClient.CryptoLocation.LOCAL);
        } catch (IdpsException | IOException e) {
            logger.info("Exception : "+e.getMessage());
            e.printStackTrace();
        } catch (Exception exp){
            logger.info("Exception : "+exp.getMessage());
            exp.printStackTrace();
        }
    }

    public static Key newKeyHandleLatest()
    {
        Key key = null;
        try {
            key = idpsClient.newKeyHandleLatest(keyName);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            logger.info("Error in fetching key  " + keyName + "from IDPS");
            throw new RuntimeException(ex);
        }
        return key;
    }
}
