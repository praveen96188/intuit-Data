package com.intuit.sbd.payroll.psp.batchjobs.raf;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentFile;
import com.intuit.sbd.payroll.psp.domain.RAFFileStatus;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.Application;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Mar 24, 2009
 * Time: 11:35:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class RAFEmailWriter {
    private static final String FMT = "%s,%s,%s\n";

    private SpcfLogger logger = Application.getLogger(RAFFileWriter.class);

    public RAFEmailWriter() {
    }
    public RAFEmailWriter(SpcfLogger pLogger) {
        logger = pLogger;
    }

    public void execute() {
        //get all finalized RAF files
        try{
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<RAFEnrollmentFile> finalizedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Finalized);
            for (RAFEnrollmentFile currentFile : finalizedEnrollmentFiles) {
                createSecuredCSVFile(currentFile);
            }

            //get all pending files for re-initiation and move the actual file from the archived directory back to the processing directory
            DomainEntitySet<RAFEnrollmentFile> recreationInitiatedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.RecreationInitiated);
            String bucketName = BatchUtils.getConfigString(S3UploadUtils.PSP_BATCHJOBS_S3_BUCKET);
            String processingDirectory = BatchUtils.getConfigString("psp_raf_ftp_srcdir");

            for (RAFEnrollmentFile currentFile : recreationInitiatedEnrollmentFiles) {

                String absoluteFileName = currentFile.getEmailFileName();
                File src_file = new File(absoluteFileName);
                File dest_file = new File(processingDirectory,src_file.getName());

                if(Application.isAWSEnvironment()){
                    String firstChar = absoluteFileName.substring(0,1);
                    String absoluteS3RemoteFileName;

                    if(firstChar.equals(File.separator)){
                        absoluteS3RemoteFileName = absoluteFileName.substring(1);
                    }else {
                        absoluteS3RemoteFileName = absoluteFileName;
                    }

                    absoluteFileName = dest_file.getAbsolutePath();
                    try{
                        S3UploadUtils.downloadFromS3FileStore(bucketName,absoluteS3RemoteFileName,absoluteFileName);
                    }catch (S3ConnectionException e){
                        throw e;
                    }catch (S3DownloadException e){
                        throw e;
                    }
                }else{
                    FileUtils.moveFileTo(src_file,processingDirectory);
                }

                currentFile.setStatus(RAFFileStatus.Finalized);
                currentFile.setEmailFileName(dest_file.getAbsolutePath());
            }
            PayrollServices.commitUnitOfWork();
        }catch (Throwable t){
            logger.error("unable to create email tapes", t);
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void createCSVFile(RAFEnrollmentFile pFile) {
        try {

            InputStreamReader is = new InputStreamReader(new FileInputStream(pFile.getFileName()), "US-ASCII");
            try {
                PrintStream os = new PrintStream(pFile.getEmailFileName(), "ISO-8859-1");
                try {
                    char[] line = new char[RAFFileWriter.REC_LEN];
                    boolean firstTime = true;
                    while (is.read(line) != -1) {
                        if (line[0] == 'T' && line[1] == 'P') {
                            // it is a taxpayer record
                            if (firstTime) {
                                // write the header
                                os.printf(FMT, "EIN", "Legal Name", "Add/Delete");
                            }
                            String taxpayerId = new String(line, 2, 9).trim();
                        String legalName = new String(line, 11, 35).trim();
                        String actionCode = new String(line, 143, 1);
                        os.printf(FMT, taxpayerId, legalName, actionCode);
                        firstTime = false;
                    }
                    }
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("unable to create raf email", e);
        }
    }
    private void createSecuredCSVFile(RAFEnrollmentFile pFile) {
        try {

            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            IDPSFileReader is = new IDPSFileReader(new File(pFile.getFileName()),key, "US-ASCII");
            try {
                //PrintStream os = new PrintStream("encrypted_printstream.csv", "ISO-8859-1");
                IDPSFileWriter secWriter = new IDPSFileWriter( new File(pFile.getEmailFileName()), key, "ISO-8859-1");
                PrintWriter os = new PrintWriter(secWriter);
                try {
                    char[] line = new char[RAFFileWriter.REC_LEN];
                    boolean firstTime = true;
                    while (is.read(line) != -1) {
                        if (line[0] == 'T' && line[1] == 'P') {
                            // it is a taxpayer record
                            if (firstTime) {
                                // write the header
                                os.printf("%s,%s,%s\n", "EIN", "Legal Name", "Add/Delete");
                            }
                            String taxpayerId = new String(line, 2, 9).trim();
                            String legalName = new String(line, 11, 35).trim();
                            String actionCode = new String(line, 143, 1);
                            os.printf("%s,%s,%s\n", taxpayerId, legalName, actionCode);
                            firstTime = false;
                        }
                    }
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("unable to create raf email", e);
        }
    }
}
