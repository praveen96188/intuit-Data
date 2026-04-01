package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.paycycle.ops.eftpsBp.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 10, 2010
 * Time: 5:45:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnrollmentFileReadWriteTest {

    @Test
    @Ignore
    public void testReadEnrollmentFile() {
        String mFileRecvDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_eftps_ftp_recv_dir");

        File mRecvDir = new File(mFileRecvDir);

        if (!mRecvDir.exists() || !mRecvDir.isDirectory()) {
            throw new RuntimeException(String.format("No Receiving Directory  %s Found  at %s: ", mRecvDir.getName(), mRecvDir.getPath()));
        }

        File mFiles[] = mRecvDir.listFiles();

        if (mFiles.length > 0) {
            for (File mFile : mFiles) {
                System.out.println("File name is :" + mFile.getPath());
                PayrollServices.beginUnitOfWork();
                EnrollmentFileReadWrite enrollmentFileReadWrite = new EnrollmentFileReadWrite(null);
                enrollmentFileReadWrite.setFileName(mFiles[1].getPath());
                try {
                    List<RejectionInfo> rejectionInfos = new ArrayList<RejectionInfo>();
//                rejectionInfos.add(new RejectionInfo("36","0006"));
//                rejectionInfos.add(new RejectionInfo("1","6565"));
                    enrollmentFileReadWrite.setRejectionInfos(rejectionInfos);

                    enrollmentFileReadWrite.read();
                    String ackFileName = enrollmentFileReadWrite.getAckFileName();
                    String enrollResponseFilename = enrollmentFileReadWrite.getEnrollmentResponseFileName();

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                PayrollServices.commitUnitOfWork();

            }
        }
    }

    @Test
    @Ignore
    public void testPaymentFileReading(){
        String mFileRecvDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_eftps_ftp_work_dir");

        File paymentFile = new File(mFileRecvDir+"/EftpsPayment110105021.813");

        if(paymentFile != null){
            PaymentFileReader paymentFileReader = new PaymentFileReader();
            paymentFileReader.setFileName(paymentFile.getPath());
            try {
                paymentFileReader.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Edi813SegmentList segmentList = paymentFileReader.getSegmentList();
            
        }
    }

    @Test
    @Ignore
    public void testPaymentFile(){
        String mFileRecvDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_eftps_ftp_work_dir");

        File paymentFile = new File(mFileRecvDir+"/EftpsPayment110105021.813");

        
        if(paymentFile != null){
            PaymentFileReadWrite paymentFileReader = new PaymentFileReadWrite(null);
            paymentFileReader.setFileName(paymentFile.getPath());
            try {
                paymentFileReader.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            paymentFileReader.getPaymentFileInfoWithAmounts();

        }
    }    
}
