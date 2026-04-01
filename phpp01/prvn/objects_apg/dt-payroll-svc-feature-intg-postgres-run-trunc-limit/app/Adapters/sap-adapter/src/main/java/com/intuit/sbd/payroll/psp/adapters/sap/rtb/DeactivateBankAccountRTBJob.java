

package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * Created by nloharuka on 3/27/18.
 */
public class DeactivateBankAccountRTBJob extends BaseRTBJob {
    private static final SpcfLogger logger = PayrollServices.getLogger(DeactivateBankAccountRTBJob.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String PSID = "PSID";
    private static final int RECORD_PROCESSING_THRESHOLD = 10;

    private SourceSystemCode mSourceSystemCode;
    private int successfulCount = 0;


    public DeactivateBankAccountRTBJob(byte[] fileBinary) throws Exception {
        super(fileBinary);
        mSourceSystemCode = SourceSystemCode.QBDT;
    }

    @Override
    public JobResult process() throws RTBJobException {
        JobResult jobResult = new JobResult();
        String sourceCompanyId = null;

        logger.info("Deactivating Pending Verification Bank Account for " + recordSize + " records. RecordSize=" + recordSize);

        for (int i = 0; i < recordSize; i++) {

            try {
                PayrollServices.beginUnitOfWork();
                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();

                //Step 1: Mark Pending Verification Bank Account as Inactive
                deactivateBankAccount(sourceCompanyId.trim(), jobResult);
                PayrollServices.commitUnitOfWork();
                successfulCount++;
                logger.info("Successfully deactivated Pending Verification bank account for PSID='"+ sourceCompanyId+"'");
                jobResult.addInfoMessage("Deactivation of Pending Verification Bank Account is successful for PSID " + sourceCompanyId);
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Deactivation of Pending Verification Bank Account failed for PSID " + sourceCompanyId);
                jobResult.addErrorMessage("--------------------------------------------------------------------");
                logger.error("Deactivation of Pending Verification Bank Account is unsuccessful for PSID=" + sourceCompanyId);
                pThrowable.printStackTrace();
            }
        }// for each record
        jobResult.addInfoMessage("Finished processing.");
        jobResult.addInfoMessage("===========================REPORT============================");
        jobResult.addInfoMessage("Total number of Pending Verification Bank Account to be deactivated " + recordSize);
        jobResult.addInfoMessage("Number of Pending Verification Bank Account successfully deactivated " + successfulCount);
        jobResult.addInfoMessage("Number of Pending Verification Bank Account failed to be deactivated " + (recordSize - successfulCount));
        jobResult.setSuccess(true);
        jobResult.addInfoMessage("=============================================================");
        logger.info("Deactivation of Pending Verification Bank Account is completed. Successful Deactivation Count=" + successfulCount + " TotalRecords=" + recordSize);
        return jobResult;

    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if (!excelKeyValueList.containsKey(PSID)) {
            logger.error("Validation failure for Pending Verification Bank Account Deactivation - PSID missing");
            jobResult.addErrorMessage("PSID missing !!");
            jobResult.setSuccess(false);
            return jobResult;

        }

        if (recordSize > RECORD_PROCESSING_THRESHOLD) {
            logger.error("Record size is more than excepted.");
            jobResult.addErrorMessage("Record size is more than excepted. Max Supported number of records are " + RECORD_PROCESSING_THRESHOLD);
            jobResult.setSuccess(false);
            return jobResult;
        }

        jobResult.setSuccess(true);

        return jobResult;

    }

    /**
     * Deactivate PendingVerification Bank Account
     * @param pCompanyId
     * @throws Throwable
     */
    private void deactivateBankAccount(String pCompanyId, JobResult pJobResult) throws Throwable {

        //find company
        Company company = Company.findCompany(pCompanyId, mSourceSystemCode);
        if (company == null) {
            pJobResult.addErrorMessage("Company not found for PSID='" + pCompanyId + "'.");
            aeFactory.throwGenericException("Company not found for PSID='" + pCompanyId + "'.");
        }


        CompanyBankAccount cba= CompanyBankAccount.findCompanyBankAccount(company, BankAccountStatus.PendingVerification);

        if (cba==null){
            pJobResult.addErrorMessage("No PendingVerification Bank Account found for PSID='" + pCompanyId + "'.");
            aeFactory.throwGenericException("No PendingVerification Bank Account found for PSID='" + pCompanyId + "'.");
        }else{
            cba.setStatusCd(BankAccountStatus.Inactive);
        }


    }

}
