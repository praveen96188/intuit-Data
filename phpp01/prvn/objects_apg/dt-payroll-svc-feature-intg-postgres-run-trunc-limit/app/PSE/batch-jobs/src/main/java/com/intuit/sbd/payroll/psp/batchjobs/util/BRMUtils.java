package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.billing.EMSBSToBRMSyncFileGenerator;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Created by IntelliJ IDEA.
 * User: smodgil
 * Date: May 6, 2020
 * Time: 8:07:20 AM
 * Utility class for common functionality related to BRM Jobs.
 */
public class BRMUtils {
    private static final SpcfLogger logger = Application.getLogger(BRMUtils.class);

    public static String outputFilePath;
    public static String BRM_IDPS_APIKEY = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_idps_apikey");
    public static String LOCAL_WORK_DIR = null;
    public static String FILENAME_EXT = ".csv";
    public static String TEXT_FILENAME_EXT = ".txt";
    /**
     * This method is responsible for performing the IDPS encryption for the Assisted usage files using
     * the BRM policy,api key and endpoint
     */
    public static void performIdpsEncryption(String localWorkDir){

        logger.info("Starting to encrypt the file ");

        File inputFile=null;
        File outputFile=null;
        LOCAL_WORK_DIR=localWorkDir;
        Key key = EncryptionUtils.getIDPSKeyForBRMEncryption(BRM_IDPS_APIKEY);
        logger.info("Encryption key for usage file: "+key.getName());
        String fileType="";
        File[] filesToBeUploaded=null;

        File workDir = new File(LOCAL_WORK_DIR);
        try{

            filesToBeUploaded = workDir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {

                    return name.endsWith(FILENAME_EXT);
                }
            });


            if (filesToBeUploaded != null && filesToBeUploaded.length>0) {
                logger.info("Files found in folder:" +workDir + " length of files: "+ filesToBeUploaded.length);
                inputFile=filesToBeUploaded[0];
                logger.info("Found InputFile "+inputFile.getPath()+" at path : "+LOCAL_WORK_DIR);

                String inputFileLocation = inputFile.getPath();

                outputFilePath =inputFile.getPath();
                inputFile.renameTo(new File(inputFileLocation.replace(".csv",".txt")));
                //retrieve the txt input file
                inputFile=getFile(StringUtils.EMPTY);
                logger.info("Input file renamed to : "+inputFile.getName());
                fileType="output";
                outputFile = getFile(fileType);
                logger.info("Output file: "+outputFile.getName());
                logger.info("Starting to encrypt the usage file");
                EMSBSToBRMSyncFileGenerator.streamEncryptFileSingleThread(key, inputFile, outputFile);
                logger.info("Completed encrypting the usage file");
                inputFile.delete();
                logger.info("Deleted the inputFile");
            }else{
                logger.info("No files to process at location "+LOCAL_WORK_DIR);
            }

        }catch(IOException e3){
            logger.error("IOException occured while encrypting Usage file {}",e3);
        }catch(Exception e4){
            logger.error("Exception occured while encrypting Usage file {}",e4);
        }
        logger.info("Completed encryption for the file ");
    }

    /**
     * This method is meant for fetching and returning the file from the LOCAL_WORK_DIR based on the passed fileType
     */
    private static File getFile(String fileType) throws IOException {
        File file=null;
        File dir = new File(LOCAL_WORK_DIR);
        if(dir.exists() && dir.listFiles() !=null){
            logger.info("Total files at path" + dir.getPath() + " are "+dir.listFiles().length);

            for(File f: dir.listFiles()){
                logger.info("File in directory : "+f.getName());
            }
        }

        if(fileType.equalsIgnoreCase("output")){
            Path outputPath= Paths.get(outputFilePath);
            logger.info("OutputFilePath is: "+outputPath);
            Files.createFile(outputPath);

            file = getFileByType(file, dir, FILENAME_EXT);
        }else {
            file = getFileByType(file, dir, TEXT_FILENAME_EXT);
            logger.info("Input file returning for encryption : "+file.getName());
        }

        return file;
    }

    /**
     * This method is meant for fetching and returning the file from the LOCAL_WORK_DIR based on the passed fileType
     * directory and file extension
     */
    private static File getFileByType(File file, File dir, String s) {
        for (File f : dir.listFiles()) {
            logger.info("File name: "+f.getName() +" and length:" +f.length());
            if (f.getName().endsWith(s)) {
                file = f;
            }
        }
        return file;
    }

}
