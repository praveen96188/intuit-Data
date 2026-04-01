package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.ops.eftpsBp.GenericEdiFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 15, 2010
 * Time: 10:20:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class TFASimulator {
    private static final SpcfLogger logger;

    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(TFASimulator.class);
    }

    public String processPaymentFile(File pFile, String pFileSendDir) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        PaymentFileReadWrite paymentFileReadWrite = new PaymentFileReadWrite(pFileSendDir);
        try {
            PayrollServices.beginUnitOfWork();
            paymentFileReadWrite.setFileName(pFile.getPath());
            paymentFileReadWrite.setOutboundDir(pFileSendDir);
            paymentFileReadWrite.read();
            PayrollServices.commitUnitOfWork();
            System.out.println("Ack File name:" + paymentFileReadWrite.getAckFileName() + " And EnrollmentResponse File Name:" + paymentFileReadWrite.getPaymentResponseFileName());
        } catch (IOException e) {
            e.printStackTrace();            
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentFileReadWrite.getPaymentResponseFileName();
    }

    public String processEnrollmentFile(File pFile, String pFileSendDir) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        EnrollmentFileReadWrite enrollmentFileReadWrite = new EnrollmentFileReadWrite(pFileSendDir);
        try {
            PayrollServices.beginUnitOfWork();
            enrollmentFileReadWrite.setFileName(pFile.getPath());
            enrollmentFileReadWrite.setOutboundDir(pFileSendDir);
            enrollmentFileReadWrite.read();
            PayrollServices.commitUnitOfWork();
            System.out.println("Ack File name:" + enrollmentFileReadWrite.getAckFileName() + " And EnrollmentResponse File Name:" + enrollmentFileReadWrite.getEnrollmentResponseFileName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return enrollmentFileReadWrite.getEnrollmentResponseFileName();
    }

    public void processFile(File pFile, String pFileSendDir) throws IOException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        String responseFile = null;
        GenericEdiFile genericEdiFile = new GenericEdiFile();
        genericEdiFile.setFileName(pFile.getPath());
        genericEdiFile.read();
        switch (genericEdiFile.getEdiFileType()) {
            case EDI151:
                //Simulator does not have to process this.
                System.out.println("This file is EDI 151");
                break;
            case EDI813:
                System.out.println("This file is EDI 813");
                responseFile = processPaymentFile(pFile, pFileSendDir);
                break;
            case EDI821:
                System.out.println("This file is EDI 821");
                break;
            case EDI824:
                //Simulator does not have to process this.
                System.out.println("This file is EDI 824");
                break;
            case EDI826:
                System.out.println("This file is EDI 826");
                break;
            case EDI827:
                //Simulator does not have to process this.
                System.out.println("This file is EDI 827");
                break;
            case EDI838:
                System.out.println("This file is EDI 838");
                responseFile = processEnrollmentFile(pFile, pFileSendDir);
                break;
            case EDI997:
                System.out.println("This file is EDI 997");
        }
        System.out.println("Output File:"+responseFile);
    }


    /**
     * Main method to process EDI file.
     *
     */
    public static void main(String[] args) {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TFASimulatorBatchJob));

            String mFileRecvDir = System.getProperty("inboundDir");
            String mFileSendDir = System.getProperty("outboundDir");

            if(mFileRecvDir == null){
                mFileRecvDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_eftps_ftp_tfa_dir");
            }

            File mRecvDir = new File(mFileRecvDir);

            if (!mRecvDir.exists() || !mRecvDir.isDirectory()) {
                throw new RuntimeException(String.format("No Receiving Directory  %s Found  at %s: ", mRecvDir.getName(), mRecvDir.getPath()));
            }

            TFASimulator tfaSimulator = new TFASimulator();
            File mFiles[] = mRecvDir.listFiles();
            for (File mFile : mFiles) {
                try {
                    tfaSimulator.processFile(mFile, mFileSendDir);
                } finally {
                    mFile.delete();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Error processing returns file. ", t);
            PayrollServices.rollbackUnitOfWork();
            System.exit(1);
        }
    }

    public String processPaymentFileWithErrors(File pFile, String pFileSendDir,List<RejectionInfo> pRejectionInfos,List<ReturnSegInfo> pReturnSegInfo) {
        PaymentFileReadWrite paymentFileReadWrite = new PaymentFileReadWrite(pFileSendDir);;
        try {
            PayrollServices.beginUnitOfWork();
            paymentFileReadWrite.setFileName(pFile.getPath());
            paymentFileReadWrite.setOutboundDir(pFileSendDir);

            if(pReturnSegInfo!=null && pReturnSegInfo.size() > 0)
            {
                paymentFileReadWrite.setReturnInfos(pReturnSegInfo);
            }
            if(pRejectionInfos!=null && pRejectionInfos.size() > 0)
            {
                paymentFileReadWrite.setRejectionInfos(pRejectionInfos);
            }
            paymentFileReadWrite.read();
            PayrollServices.commitUnitOfWork();
            System.out.println("Ack File name:" + paymentFileReadWrite.getAckFileName() + " And PaymentResponse File Name:" + paymentFileReadWrite.getPaymentResponseFileName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentFileReadWrite.getPaymentResponseFileName();
    }

    public String processEnrollmentFileWithErrors(File pFile, String pFileSendDir,List<RejectionInfo>  rejInfo) {
        EnrollmentFileReadWrite enrollmentFileReadWrite = new EnrollmentFileReadWrite(pFileSendDir);
        try {
            PayrollServices.beginUnitOfWork();
            enrollmentFileReadWrite.setFileName(pFile.getPath());
            enrollmentFileReadWrite.setOutboundDir(pFileSendDir);
            enrollmentFileReadWrite.setRejectionInfos(rejInfo);
            enrollmentFileReadWrite.read();
            PayrollServices.commitUnitOfWork();
            System.out.println("Ack File name:" + enrollmentFileReadWrite.getAckFileName() + " And EnrollmentResponse File Name:" + enrollmentFileReadWrite.getEnrollmentResponseFileName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return enrollmentFileReadWrite.getEnrollmentResponseFileName();
    }

}
