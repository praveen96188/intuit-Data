package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kBatchStatusCode;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kSignUpBatch;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * User: rnorian
 * Date: Feb 12, 2011
 * Time: 1:21:38 PM
 */
public class ThirdParty401kSignUp {

    private static final String OUTPUT_DIRECTORY = BatchUtils.getConfigString("psp_batch_ftp_recv_dir");
    private static final String CENSUS_FILE_PREFIX = "Signup_Batch_Intuit_";
    private static final String FILE_EXT = ".csv";

    public static void signUp(Company pCompany) {
        //ADD,991234567,Test,12/01/2009,Acme Software, false
        StringBuilder builder = new StringBuilder();
        builder .append("ADD")
                .append(",").append(pCompany.getFedTaxId())
                .append(",").append("CustID-" + pCompany.getSourceCompanyId()) // custodial-id
                .append(",").append(PSPDate.getPSPTime().format("MM/dd/yyyy")) // effective date
                .append(",").append(pCompany.getLegalName())
                .append(",").append(" ").append(new Boolean(false).toString());// safe harbor

        ArrayList<String> singUpData = new ArrayList<String>();
        singUpData.add(builder.toString());

        signUp(singUpData);
    }

    public static void signUp(ArrayList<String> pFileData) {
        try {
            Application.beginUnitOfWork();
            String fileName = ThirdParty401kSignUp.create401kSignupFile(pFileData);
            ThirdParty401kSignUp.create401kSignupBatch(fileName);
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            DomainEntitySet<ThirdParty401kSignUpBatch> batchList = BatchUtils.getThirdParty401kSignUpBatchByStatus(ThirdParty401kBatchStatusCode.Pending);
            ThirdParty401kSignUpBatch batch = batchList.get(0);

            new ThirdParty401kSignUpFileParser().processSignupBatch(batch);

            batch.setDownloadStatusCd(ThirdParty401kBatchStatusCode.Archived);
            Application.save(batch);
            Application.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    public static String create401kSignupFile(ArrayList<String> pFileData) throws Exception {
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        SpcfCalendar systemTime = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        String fileName = OUTPUT_DIRECTORY + File.separator + CENSUS_FILE_PREFIX +
                StringFormatter.formatDate(pspDate, "yyyyMMdd") + "_" +
                StringFormatter.formatDate(systemTime, "HHmmss") + FILE_EXT;

        FileWriter fileWriter = new FileWriter(fileName);
        for (String record : pFileData) {
            writeData(fileWriter, record);
        }

        fileWriter.flush();
        fileWriter.close();

        return fileName;
    }

    private static void writeData(FileWriter pFileWriter, String pData) throws IOException {
        if (pData == null) {
            pData = "";
        }
        pFileWriter.write(pData);
    }

    public static void create401kSignupBatch(String pFileName) {
        ThirdParty401kSignUpBatch tp401kSignUpBatch = new ThirdParty401kSignUpBatch();

        tp401kSignUpBatch.setBatchId(generateNewBatchId());
        tp401kSignUpBatch.setDownloadDate(PSPDate.getPSPTime());
        tp401kSignUpBatch.setFileName(pFileName);
        tp401kSignUpBatch.setDownloadStatusCd(ThirdParty401kBatchStatusCode.Pending);
        tp401kSignUpBatch.setStatusEffectiveDate(PSPDate.getPSPTime());

        Application.save(tp401kSignUpBatch);
    }

    private static int generateNewBatchId() {
        return Application.nextSequenceValue(SequenceId.SEQ_401K_SIGNUP_BATCH_ID, Long.class).intValue();
    }



}
