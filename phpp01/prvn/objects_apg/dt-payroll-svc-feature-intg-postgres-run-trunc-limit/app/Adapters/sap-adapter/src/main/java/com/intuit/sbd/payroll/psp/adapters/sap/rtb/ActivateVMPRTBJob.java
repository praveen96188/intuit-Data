

package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * Created by nloharuka on 3/27/18.
 */
public class ActivateVMPRTBJob extends BaseRTBJob {
    private static final SpcfLogger logger = PayrollServices.getLogger(ActivateVMPRTBJob.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String PSID = "PSID";
    private static final int RECORD_PROCESSING_THRESHOLD = 10;

    private SourceSystemCode mSourceSystemCode;
    private int successfulCount = 0;


    public ActivateVMPRTBJob(byte[] fileBinary) throws Exception {
        super(fileBinary);
        mSourceSystemCode = SourceSystemCode.QBDT;
    }

    @Override
    public JobResult process() throws RTBJobException {
        JobResult jobResult = new JobResult();
        String sourceCompanyId = null;

        logger.info("Activating VMP for " + recordSize + " records. RecordSize=" + recordSize);

        for (int i = 0; i < recordSize; i++) {

            try {
                PayrollServices.beginUnitOfWork();
                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();

                //Step 1: Activate VMP Services ( and activate CloudV2)
                //for now we have considered the VMP & workerComp possible services under CloudV2
                activateCompanyService(sourceCompanyId.trim(),jobResult);
                PayrollServices.commitUnitOfWork();
                successfulCount++;
                logger.info("Successfully activated VMP service for PSID='"+ sourceCompanyId+"'");
                jobResult.addInfoMessage("Activation of company services is successful for PSID " + sourceCompanyId);
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Activation of company services is failed for PSID " + sourceCompanyId);
                jobResult.addErrorMessage("--------------------------------------------------------------------");
                logger.error("Activation of company services is unsuccessful for PSID=" + sourceCompanyId);
                pThrowable.printStackTrace();
            }
        }// for each record
        jobResult.addInfoMessage("Finished processing.");
        jobResult.addInfoMessage("===========================REPORT============================");
        jobResult.addInfoMessage("Total number of VMP services to be activated " + recordSize);
        jobResult.addInfoMessage("Number of VMP services successfully activated " + successfulCount);
        jobResult.addInfoMessage("Number of VMP services failed to be activated " + (recordSize - successfulCount));
        jobResult.setSuccess(true);
        jobResult.addInfoMessage("=============================================================");
        logger.info("Activation of VMP Service is completed. Successful Activations=" + successfulCount + " TotalRecords=" + recordSize);
        return jobResult;

    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if (!excelKeyValueList.containsKey(PSID)) {
            logger.error("VMP activation validation failure - PSID missing");
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
     * Activate VMP Services ( and activate CloudV2 before activation of VMP  )
     * @param pCompanyId
     * @throws Throwable
     */
    private void activateCompanyService(String pCompanyId, JobResult pJobResult) throws Throwable {

        //find company
        Company company = Company.findCompany(pCompanyId, mSourceSystemCode);
        if (company == null) {
            pJobResult.addErrorMessage("Company not found for PSID='" + pCompanyId + "'.");
            aeFactory.throwGenericException("Company not found for PSID='" + pCompanyId + "'.");
        }

        CompanyService vmpCompService = company.getService(ServiceCode.ViewMyPaycheck);
        CompanyService cloudV2CompService = company.getService(ServiceCode.CloudV2);


        if (cloudV2CompService==null){
            pJobResult.addErrorMessage("CloudV2 Service is not found for PSID='" + pCompanyId + "' which is mandatory to activate VMP.");
            aeFactory.throwGenericException("CloudV2 Service is not found for PSID='" + pCompanyId + "' which is mandatory to activate VMP.");
        }

        if (vmpCompService == null){
            pJobResult.addErrorMessage("VMP Service is not found for PSID='" + pCompanyId + "'.");
            aeFactory.throwGenericException("VMP Service is not found for PSID='" + pCompanyId + "'.");
        }

        if (cloudV2CompService.getStatusCd() == ServiceSubStatusCode.ActiveCurrent ){
            pJobResult.addErrorMessage("Already Activated CloudV2 service for PSID='"+ pCompanyId+"'");
        } else if (cloudV2CompService.getStatusCd() != ServiceSubStatusCode.ActiveCurrent ){
            cloudV2CompService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        }

        if (vmpCompService.getStatusCd() == ServiceSubStatusCode.ActiveCurrent ) {
            pJobResult.addErrorMessage("Already Activated VMP service for PSID='"+ pCompanyId+"'");
        }else if (vmpCompService.getStatusCd() != ServiceSubStatusCode.ActiveCurrent ) {
            vmpCompService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        }

    }

}
