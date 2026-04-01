package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
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
 * Date: Oct 29, 2011
 * Time: 12:05:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class VANSimulator {
    private static final SpcfLogger logger;

    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(VANSimulator.class);
    }

    public String generatePaymentResponseEdiFile(String pFileSendDir, Integer pFileId, String pPaymentFileGroupTime, List<EdiResponseFileTxnDetails> pTxnDetails) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.VANSimulatorBatchJob);

        EdiPaymentResponseFileReadWrite ediPaymentResponseFileReadWrite = new EdiPaymentResponseFileReadWrite(pFileSendDir, pFileId, pPaymentFileGroupTime, pTxnDetails);
        try {
            ediPaymentResponseFileReadWrite.write();
            System.out.println("Response File for payment File:" + ediPaymentResponseFileReadWrite.getFileName());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return ediPaymentResponseFileReadWrite.getFileName();
    }

    public String generateAckEdiFile(String pFileSendDir, List<Integer> pTxnSetIds, String pFileId) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.VANSimulatorBatchJob);

        EdiAckFileReadWrite ediAckFileReadWrite = new EdiAckFileReadWrite(pFileSendDir, EftpsEdiType.EDI813, pFileId);

        for (Integer pTxnSetId : pTxnSetIds) {
            ediAckFileReadWrite.queueAckToWrite(EftpsEdiType.EDI813, String.valueOf(pTxnSetId));
        }
        ediAckFileReadWrite.write();
        return ediAckFileReadWrite.getFileName();
    }

    public String processPaymentFile(File pFile, String pFileSendDir) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.VANSimulatorBatchJob);

        EdiPaymentFileReadWrite paymentFileReadWrite = new EdiPaymentFileReadWrite(pFileSendDir);
        try {
            PayrollServices.beginUnitOfWork();
            paymentFileReadWrite.setFileName(pFile.getPath());
            paymentFileReadWrite.read();
            PayrollServices.commitUnitOfWork();
            System.out.println("Ack File (EDI 997) name:" + paymentFileReadWrite.getAckFileName() + " And Payment response File (EDI 151) Name:" + paymentFileReadWrite.getPaymentResponseFileName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentFileReadWrite.getPaymentResponseFileName();
    }

    public void processFile(File pFile, String pFileSendDir) throws IOException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.VANSimulatorBatchJob);
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
            case EDI997:
                //Simulator does not have to process this.
                System.out.println("This file is EDI 997");
        }
        System.out.println("Output File:" + responseFile);
    }

    /**
     * Main method to process EDI file.
     */
    public static void main(String[] args) {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.VANSimulatorBatchJob));

            String mFileRecvDir = System.getProperty("inboundDir");
            String mFileSendDir = System.getProperty("outboundDir");

            if (mFileRecvDir == null) {
                mFileRecvDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_edi_scp_van_dir");
            }

            File mRecvDir = new File(mFileRecvDir);

            if (!mRecvDir.exists() || !mRecvDir.isDirectory()) {
                throw new RuntimeException(String.format("No Receiving Directory  %s Found  at %s: ", mRecvDir.getName(), mRecvDir.getPath()));
            }

            VANSimulator vanSimulator = new VANSimulator();
            File mFiles[] = mRecvDir.listFiles();
            for (File mFile : mFiles) {
                try {
                    vanSimulator.processFile(mFile, mFileSendDir);
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

}
