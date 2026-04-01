package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.AMLReport;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpCommonEncryptedReader;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


import java.io.*;

/**
 * Created by: smodgil on 01/21/20.
 * Description: This class contains various test scenarios to test the AML report download feature
 */
public class ReportDownloadTests {

    private static SpcfLogger logger = SpcfLogManager.getLogger(ReportDownloadTests.class);
    private final static String mSendDir = BatchUtils.getConfigString("psp_batch_ftp_recv_dir"); // Directory in which the generated files are located
    private final static String mArchiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir"); // Directory in which the uploaded file are located
    private String mDecryptionKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfa_intuit_private_key");
    private String mDecryptionKeyPassword = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfa_intuit_key_password");




    @BeforeClass
    public static void downloadEncryptedAMLFile(){
        String processingDate="20200117";
        String fileName = AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File dataFile = new File(mSendDir,fileName);
        File downloadFileFile =downloadFile(dataFile);
        Assert.assertEquals(true,downloadFileFile.exists());
    }

    @Test
    public void downloadAMLFileWithFutureDate(){
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        String processingDate =today.format("yyyyMMdd");
        CalendarUtils.addBusinessDays(today, +1);
        String fileName = AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File dataFile = new File(mSendDir,fileName);
        File downloadFileFile =downloadFile(dataFile);
        Assert.assertTrue(downloadFileFile==null);
    }

    @Test
    public void decDownloadedAMLFile(){
        File decryptedFile =null;
        String line;
        try{
            String processingDate ="20200117";
            String fileName = mArchiveDir+File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
            File archivedFile = new File(fileName);
            if(archivedFile.exists()){
                PgpCommonEncryptedReader reader = new PgpCommonEncryptedReader(mDecryptionKey,mDecryptionKeyPassword);
                reader.open(archivedFile);

                decryptedFile = new File(mArchiveDir+File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.FILE_EXT);
                BufferedWriter bw = new BufferedWriter(new FileWriter(decryptedFile));
                while ((line = reader.readLine()) != null) {
                    bw.write(line);
                    bw.append("\n");
                }
                bw.close();
                reader.close();
                //bw.flush();

            }
            if(decryptedFile!=null) {
                Assert.assertEquals(true, decryptedFile.length() > 0);
            }
        }catch (Exception e){
            logger.error("Exception occured while decrypting AML File"+e.getCause());
        }

    }



    @Test
    public void deleteDownloadedAMLFile(){
        String processingDate="20200117";
        String fileName = mArchiveDir+File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        String decTxtFileName = mArchiveDir+File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.FILE_EXT;
        File archivedEncPgpFile = new File(fileName);
        File archivedDecTxtFile = new File(decTxtFileName);
        if(archivedEncPgpFile!=null && archivedDecTxtFile !=null){
            archivedEncPgpFile.delete();
            archivedDecTxtFile.delete();
            Assert.assertEquals(true,!archivedEncPgpFile.exists());
            Assert.assertEquals(true, !archivedDecTxtFile.exists());
        }
    }


    @Test
    public void testNonZeroDownloadedAMLFile(){

        String processingDate="20200117";
        String fileName = mSendDir+File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File file = new File(fileName);
        Assert.assertEquals(true,file.exists() && file.length()>0);
    }

    @Test
    public void testZeroLengthDownloadedAMLFile(){

        String processingDate="20200117";
        String fileName = mSendDir+File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File file = new File(fileName);
        if(file.exists() && file.length()==0){
            Assert.assertEquals(true,file.exists() && file.length()==0);
        }
    }

    private static File downloadFile(File file){

        File targetFile=null;

        try{
            if(file.exists()){
                InputStream initialStream = new FileInputStream(
                        new File(file.getPath()));
                byte[] buffer = new byte[initialStream.available()];

                initialStream.read(buffer);

                targetFile = new File(mArchiveDir +File.separator + file.getName());

                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
            }else{
                return targetFile;
            }

        }catch (Exception e){
            logger.error("Exception occured while downloading AML File"+e.getCause());
        }
        return targetFile;
    }

}
