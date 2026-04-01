package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus;
import com.intuit.sbd.payroll.psp.domain.QbdtUnprocessedRequest;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * Created by anandp233 on 3/31/14.
 */
public class UnprocessedPayrollRTBJob extends BaseRTBJob {


    private static final SpcfLogger logger = PayrollServices.getLogger(UnprocessedPayrollRTBJob.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String UNPROCESSED_REQUEST_ID = "UNPROCESSEREQUESTID";
    private static final String STATUS = "STATUS";
    private static final int RECORD_PROCESSING_THRESHOLD = 10;
    private int successfulCount = 0;


    public UnprocessedPayrollRTBJob(byte[] fileBinary) throws Exception {
        super(fileBinary);
    }

    @Override
    public JobResult process() throws RTBJobException {

        JobResult jobResult = new JobResult();
        String status = null;
        String unprocessedId = null;

        logger.info("Executing Unprocessed Payroll the for " + recordSize + " records. RecordSize=" + recordSize);

        for (int i = 0; i < recordSize; i++) {
            try {
                PayrollServices.beginUnitOfWork();

                unprocessedId = ((List) excelKeyValueList.get(UNPROCESSED_REQUEST_ID)).get(i).toString().trim();
                status = ((List) excelKeyValueList.get(STATUS)).get(i).toString().trim();
                QbdtRequestStatus qbdtRequestStatus = getQbdtRequestStatus(status);
                if (qbdtRequestStatus == null) {
                    jobResult.addErrorMessage("Provided status is not supported");
                    aeFactory.throwGenericException("Provided status is not supported");
                }

                //Step 2: Cancel Company Services (i.e TAX, DD services - for now we are cancelling TAX and DD service only )
                processUnprocessedRequest(unprocessedId.trim(), qbdtRequestStatus);
                PayrollServices.commitUnitOfWork();
                successfulCount++;
                logger.info("Successfully process the Unprocessed payroll id=" + unprocessedId);
                jobResult.addInfoMessage("Successfully process the Unprocessed payroll for id " + unprocessedId);
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Failed to process the Unprocessed payroll id " + unprocessedId);
                logger.error("Failed to process the Unprocessed payroll for id=" + unprocessedId);
                pThrowable.printStackTrace();
            }
        }// for each record
        jobResult.addInfoMessage("Finished processing.");
        jobResult.addInfoMessage("===========================REPORT============================");
        jobResult.addInfoMessage("Total number of Unprocessed Payroll to be process " + recordSize);
        jobResult.addInfoMessage("Number of Unprocessed Payroll successfully process " + successfulCount);
        jobResult.addInfoMessage("Number of Unprocessed Payroll failed to process " + (recordSize - successfulCount));
        jobResult.addInfoMessage("Please send an EMAIL to SBCGPayrollCRT@intuit.com about successful \"Processed\" Payroll companies.");
        jobResult.setSuccess(true);
        jobResult.addInfoMessage("=============================================================");
        logger.info("Unprocessed Payroll Job is completed. SuccessfulProcessed=" + successfulCount + " TotalRecords=" + recordSize);
        return jobResult;


    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if ((!excelKeyValueList.containsKey(UNPROCESSED_REQUEST_ID)) || (!excelKeyValueList.containsKey(STATUS))) {
            logger.error(" Unprocessed Payroll validation failure - "+UNPROCESSED_REQUEST_ID+" or "+STATUS+" missing");
            jobResult.addErrorMessage(UNPROCESSED_REQUEST_ID+" or "+STATUS+" missing!!");
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

    private void processUnprocessedRequest(String pQbdtUnprocessedRequestId, QbdtRequestStatus pStatus) throws Exception {
        SpcfUniqueId id = new SpcfUniqueIdImpl(pQbdtUnprocessedRequestId);
        QbdtUnprocessedRequest qbdtUnprocessedRequest = QbdtUnprocessedRequest.findUnprocessedRequest(id);
        if (qbdtUnprocessedRequest == null) {
            aeFactory.throwGenericException("QbdtUnprocessedRequest does not found for id " + pQbdtUnprocessedRequestId);
        }
        qbdtUnprocessedRequest.setStatus(pStatus);
        Application.save(qbdtUnprocessedRequest);
    }

    private QbdtRequestStatus getQbdtRequestStatus(String pStatus) {
        if ("P".equalsIgnoreCase(pStatus) || "Processed".equalsIgnoreCase(pStatus)) {
            return QbdtRequestStatus.Processed;
        } else if ("Q".equalsIgnoreCase(pStatus) || "Queued".equalsIgnoreCase(pStatus)) {
            return QbdtRequestStatus.Queued;
        }
        return null;
    }
}
