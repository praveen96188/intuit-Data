package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by anandp233 on 5/6/14.
 */
public class UpdateTokenRTBJob extends BaseRTBJob {

    private static final SpcfLogger logger = PayrollServices.getLogger(UpdateTokenRTBJob.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String PSID = "PSID";
    private static final String NEXT_PAYROLL_TRANSACTION_ID = "NEXT_PAYROLL_TRANSACTION_ID";
    private static final String NEXT_PAYCHECK_ID = "NEXT_PAYCHECK_ID";
    private static final String NEXT_EMPLOYEE_ID = "NEXT_EMPLOYEE_ID";
    private static final String NEXT_PAYROLL_ITEM_ID = "NEXT_PAYROLL_ITEM_ID";
    private static final String CURRENT_TOKEN = "HIGH_TOKEN";
    //As of now are supporting for QBDT source system only
    private static final String SOURCE_SYSTEM_CD = "QBDT";
    private static final int RECORD_PROCESSING_THRESHOLD = 20;
    private int successfulCount = 0;

    public UpdateTokenRTBJob(byte[] fileBinary) throws Exception {
        super(fileBinary);
    }

    @Override
    public JobResult process() throws RTBJobException {
        JobResult jobResult = new JobResult();
        String sourceCompanyId = null;
        String nextPayrollTransId = null;
        String nextPaycheckId = null;
        String nextEmpId = null;
        String nextPayrollItemId = null;
        String nextCurrentToken = null;
        ProcessResult pr = null;
        logger.info("Updating the tokens for " + recordSize + " records. RecordSize=" + recordSize);

        for (int i = 0; i < recordSize; i++) {

            try {
                PayrollServices.beginUnitOfWork();
                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();
                nextPayrollTransId = ((List) excelKeyValueList.get(NEXT_PAYROLL_TRANSACTION_ID)).get(i).toString().trim();
                nextPaycheckId = ((List) excelKeyValueList.get(NEXT_PAYCHECK_ID)).get(i).toString().trim();
                nextEmpId = ((List) excelKeyValueList.get(NEXT_EMPLOYEE_ID)).get(i).toString().trim();
                nextPayrollItemId = ((List) excelKeyValueList.get(NEXT_PAYROLL_ITEM_ID)).get(i).toString().trim();
                nextCurrentToken = ((List) excelKeyValueList.get(CURRENT_TOKEN)).get(i).toString().trim();

                Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(SOURCE_SYSTEM_CD));
                CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
                if (StringUtils.isNotEmpty(nextCurrentToken)) {
                    companyDTO.setCurrentToken(Long.parseLong(nextCurrentToken));
                }
                if (StringUtils.isNotEmpty(nextEmpId)) {
                    companyDTO.setNextEmployeeId(nextEmpId);
                }
                if (StringUtils.isNotEmpty(nextPaycheckId)) {
                    companyDTO.setNextPaycheckId(nextPaycheckId);
                }
                if (StringUtils.isNotEmpty(nextPayrollItemId)) {
                    companyDTO.setNextPayrollItemId(nextPayrollItemId);
                }
                if (StringUtils.isNotEmpty(nextPayrollTransId)) {
                    companyDTO.setNextPayrollTransactionId(nextPayrollTransId);
                }

                pr = PayrollServices.companyManager.updateCompanyTokensAndIdsCore(SourceSystemCode.valueOf(SOURCE_SYSTEM_CD), sourceCompanyId, companyDTO, false);

                if (!pr.isSuccess()) {
                    throw new RuntimeException ("Error while updating company tokens for PSID=" + sourceCompanyId + ". Error Details :" + pr);
                }

                PayrollServices.commitUnitOfWork();
                successfulCount++;
                logger.info("Successfully updated company tokens for PSID - " + sourceCompanyId);
                jobResult.addInfoMessage("Successfully updated company tokens for PSID - " + sourceCompanyId);
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Error while updating company tokens for PSID " + sourceCompanyId);
                logger.error("Error while updating company tokens for PSID=" + sourceCompanyId + ". Error Details :" + pr);
                pThrowable.printStackTrace();
            }
        }// for each record
        jobResult.addInfoMessage("Finished processing.");
        jobResult.addInfoMessage("===========================REPORT============================");
        jobResult.addInfoMessage("Total number of companies to be update " + recordSize);
        jobResult.addInfoMessage("Number of companies successfully updated " + successfulCount);
        jobResult.addInfoMessage("Number of companies failed to be update " + (recordSize - successfulCount));
        jobResult.setSuccess(true);
        jobResult.addInfoMessage("=============================================================");
        logger.info("Updating the tokens are completed. SuccessfulUpdated=" + successfulCount + " TotalRecords=" + recordSize);
        return jobResult;


    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if (!excelKeyValueList.containsKey(PSID)) {
            logger.error("Update token validation failure - PSID missing");
            jobResult.addErrorMessage("PSID missing !!");
            jobResult.setSuccess(false);
            return jobResult;

        }

        if (!(excelKeyValueList.containsKey(NEXT_PAYROLL_TRANSACTION_ID) && excelKeyValueList.containsKey(NEXT_PAYCHECK_ID) &&  excelKeyValueList.containsKey(NEXT_EMPLOYEE_ID) && excelKeyValueList.containsKey(NEXT_PAYROLL_ITEM_ID) && excelKeyValueList.containsKey(CURRENT_TOKEN) )) {
            logger.error("Update token validation failure - missing token");
            jobResult.addErrorMessage("Token missing !!");
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
}
