

package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.IERSGateway;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by anandp233 on 3/30/14.
 */
public class CancelVMPRTBJob extends BaseRTBJob {
    private static final SpcfLogger logger = PayrollServices.getLogger(CancelVMPRTBJob.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String PSID = "PSID";
    private static final int RECORD_PROCESSING_THRESHOLD = 100;

    private SourceSystemCode mSourceSystemCode;
    private int successfulCount = 0;


    public CancelVMPRTBJob(byte[] fileBinary) throws Exception {
        super(fileBinary);
        mSourceSystemCode = SourceSystemCode.QBDT;
    }

    @Override
    public JobResult process() throws RTBJobException {
        JobResult jobResult = new JobResult();
        String sourceCompanyId = null;

        logger.info("Cancelling the VMP for " + recordSize + " records. RecordSize=" + recordSize);

        for (int i = 0; i < recordSize; i++) {

            try {
                PayrollServices.beginUnitOfWork();
                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();

                //Step 1: Cancel VMP Services ( and cancel CloudV2 if no other service is active under the CloudV2 )
                //for now we have considered the VMP & workerComp possible services under CloudV2
                cancelCompanyService(sourceCompanyId.trim(),jobResult);
                PayrollServices.commitUnitOfWork();
                successfulCount++;
                logger.info("Successfully cancelled VMP service for PSID='"+ sourceCompanyId+"'");
                jobResult.addInfoMessage("Cancelling of company services is successful for PSID " + sourceCompanyId);
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Cancelling of company services is failed for PSID " + sourceCompanyId);
                jobResult.addErrorMessage("---------------------------------------------------------------------");
                logger.error("Cancelling of company services is unsuccessful for PSID=" + sourceCompanyId);
                pThrowable.printStackTrace();
            }
        }// for each record
        jobResult.addInfoMessage("Finished processing.");
        jobResult.addInfoMessage("===========================REPORT============================");
        jobResult.addInfoMessage("Total number of VMP services to be cancelled " + recordSize);
        jobResult.addInfoMessage("Number of VMP services successfully cancelled " + successfulCount);
        jobResult.addInfoMessage("Number of VMP services failed to be cancelled " + (recordSize - successfulCount));
        jobResult.setSuccess(true);
        jobResult.addInfoMessage("=============================================================");
        logger.info("Cancelling of VMP Service is completed. SuccessfulCancelled=" + successfulCount + " TotalRecords=" + recordSize);
        return jobResult;

    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if (!excelKeyValueList.containsKey(PSID)) {
            logger.error("VMP cancelling validation failure - PSID missing");
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
     * Cancel VMP Services ( and cancel CloudV2 if no other service is active under the CloudV2 )
     * @param pCompanyId
     * @throws Throwable
     */
    private void cancelCompanyService(String pCompanyId, JobResult pJobResult) throws Throwable {

        //find company
        Company company = Company.findCompany(pCompanyId, mSourceSystemCode);
        if (company == null) {
            pJobResult.addErrorMessage("Company not found for PSID='" + pCompanyId + "'.");
            aeFactory.throwGenericException("Company not found for PSID='" + pCompanyId + "'.");
        }

        CompanyService vmpCompService = company.getService(ServiceCode.ViewMyPaycheck);
        CompanyService workerCompService = company.getService(ServiceCode.WorkersComp);
        CompanyService cloudV2CompService = company.getService(ServiceCode.CloudV2);

        if (vmpCompService == null){
            pJobResult.addErrorMessage("VMP Service is not found for PSID='" + pCompanyId + "'.");
            aeFactory.throwGenericException("VMP Service is not found for PSID='" + pCompanyId + "'.");
        }

        if (vmpCompService.getStatusCd() == ServiceSubStatusCode.Cancelled || vmpCompService.getStatusCd() == ServiceSubStatusCode.Terminated ) {
            pJobResult.addErrorMessage("Already Cancelled/Terminated VMP service for PSID='" + pCompanyId + "'.");
        }else if (vmpCompService.getStatusCd() != ServiceSubStatusCode.Cancelled ) {
            vmpCompService.setStatusCd(ServiceSubStatusCode.Cancelled);
        }

        if (cloudV2CompService!=null && cloudV2CompService.getStatusCd() == ServiceSubStatusCode.Cancelled ){
            pJobResult.addErrorMessage("Already Cancelled/Terminated CloudV2 service for PSID='" + pCompanyId + "'.");
            return;
        }

        if (cloudV2CompService!=null && workerCompService==null){
            cloudV2CompService.setStatusCd(ServiceSubStatusCode.Cancelled);
        } else if (cloudV2CompService!=null && workerCompService!=null && workerCompService.getStatusCd().in(ServiceSubStatusCode.Cancelled,ServiceSubStatusCode.Terminated )){
            cloudV2CompService.setStatusCd(ServiceSubStatusCode.Cancelled);
        }

    }

}
