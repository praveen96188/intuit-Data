package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus;
import com.intuit.sbd.payroll.psp.domain.QbdtUnprocessedRequest;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * Created by anandp233 on 4/3/14.
 */
public class UnprocessedPayrollInfoRTBJob extends BaseRTBJob {

    private static final SpcfLogger logger = PayrollServices.getLogger(UnprocessedPayrollInfoRTBJob.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String PSID = "PSID";

    private static final String message = "Marked Processed by RTB Excel";
    private static final int RECORD_PROCESSING_THRESHOLD = 10;
    private QbdtRequestStatus[] requestStatuses;
    private int successfulCount = 0;


    public UnprocessedPayrollInfoRTBJob(byte[] fileBinary) throws Exception {
        super(fileBinary);
        requestStatuses = new QbdtRequestStatus[]{QbdtRequestStatus.Error, QbdtRequestStatus.Processing};
    }

    @Override
    public JobResult process() throws RTBJobException {

        JobResult jobResult = new JobResult();
        String sourceCompanyId = null;

        logger.info("Executing Unprocessed Payroll the for " + recordSize + " records. RecordSize=" + recordSize);

        for (int i = 0; i < recordSize; i++) {
            try {
                PayrollServices.beginUnitOfWorkWithSecondary();
                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();
                getUnprocessedPayrollInfo(jobResult, sourceCompanyId.trim());
                //for successfull record
                successfulCount++;
                PayrollServices.commitUnitOfWorkWithSecondary();
                logger.info("Successfully process the Unprocessed payroll info PSID=" + sourceCompanyId);
                jobResult.addInfoMessage("Successfully process the Unprocessed payroll info for PSID " + sourceCompanyId);
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWorkWithSecondary();
                jobResult.addErrorMessage("Failed to process the Unprocessed payroll id " + sourceCompanyId);
                jobResult.addErrorMessage("---------------------------------------------------------------");
                logger.error("Failed to process the Unprocessed payroll for id=" + sourceCompanyId);
                pThrowable.printStackTrace();
            }
        }
        // for each record
        jobResult.addInfoMessage("Finished processing.");
        jobResult.addInfoMessage("===========================REPORT============================");
        jobResult.addInfoMessage("Total number of PSIDs info to be process " + recordSize);
        jobResult.addInfoMessage("Number of PSIDs info successfully process " + successfulCount);
        jobResult.addInfoMessage("Number of PSIDs info failed to process " + (recordSize - successfulCount));
        jobResult.setSuccess(true);
        jobResult.addInfoMessage("=============================================================");
        logger.info("Unprocessed Payroll Info Job is completed. SuccessfulProcessed=" + successfulCount + " TotalRecords=" + recordSize);
        return jobResult;


    }

    private void getUnprocessedPayrollInfo(JobResult pJobResult, String pPSID) throws Throwable {
        Company mCompany = Company.findCompany(pPSID, SourceSystemCode.QBDT);
        DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests = QbdtUnprocessedRequest.findUnprocessedRequests(mCompany,false, requestStatuses);
        if (qbdtUnprocessedRequests == null || qbdtUnprocessedRequests.size() == 0) {
            pJobResult.addWarningMessage("No unprocessed payroll info records are found for PSID " + pPSID);
            aeFactory.throwGenericException("No unprocessed payroll info records are found for PSID " + pPSID);
        } else if (qbdtUnprocessedRequests.size() > 0) {
            pJobResult.addInfoMessage("Found " + qbdtUnprocessedRequests.size() + " Unprocessed Payroll Info Records for PSID " + pPSID);
        }
        for (QbdtUnprocessedRequest qbdtUnprocessedRequest : qbdtUnprocessedRequests) {
            pJobResult.addInfoMessage("Unprocessed Payroll ID=" + qbdtUnprocessedRequest.getId() + " Status=" + qbdtUnprocessedRequest.getStatus());
            if((qbdtUnprocessedRequest.getStatus().equals(QbdtRequestStatus.Error))){
                qbdtUnprocessedRequest.setStatus(QbdtRequestStatus.Processed);
                pJobResult.addInfoMessage("Unprocessed Payroll ID=" + qbdtUnprocessedRequest.getId() + " marked as processed and ErrorMessage set as null.");
            }
            else {
                qbdtUnprocessedRequest.setStatus(QbdtRequestStatus.Processed);
                qbdtUnprocessedRequest.setErrorMessage(message);
                pJobResult.addInfoMessage("Unprocessed Payroll ID=" + qbdtUnprocessedRequest.getId() + " marked as processed.");
            }
        }

    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if ((!excelKeyValueList.containsKey(PSID))) {
            logger.error("Unprocessed Payroll Info validation failure - PSID missing");
            jobResult.addErrorMessage("PSID missing!!");
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
