package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.api.dtos.LedgerOperationJobDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SUICreditsJobDTO;
import com.intuit.sbd.payroll.psp.api.managers.IBatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * @author Wiktor Kozlik
 */
public class BatchJobManager implements IBatchJobManager {

    public ProcessResult scheduleSecondOffload(OffloadGroup pOffloadGroup, SpcfCalendar pOffloadDateTime) {
        return new SecondOffloadProcess(pOffloadGroup, pOffloadDateTime).execute();
    }

    public ProcessResult initiateRAFTapeCreation(RAFActionCode pRAFActionCode) {
        return new InitiateRAFTapeCreation(pRAFActionCode).execute();
    }

    public ProcessResult initiateRAFTapeRecreation(RAFEnrollmentFile pRAFEnrollmentFile) {
        return new InitiateRAFTapeRecreation(pRAFEnrollmentFile).execute();
    }

    public ProcessResult monthlyGemsUpload(){
        return new MonthlyGemsUploadProcess().execute();
    }

    public ProcessResult<CheckPrintBatch> updateCheckPrintBatchStatus(String pBatchId, CheckPrintBatchStatus pCheckPrintBatchStatus) {
        return new UpdateCheckPrintBatchStatusCore(pBatchId, pCheckPrintBatchStatus).execute();
    }

    public ProcessResult addLedgerOperationJob(LedgerOperationJobDTO pLedgerOperationJobDTO) {
        return new AddLedgerOperationJob(pLedgerOperationJobDTO).execute();
    }

    public ProcessResult queueLedgerOperationJob(SpcfUniqueId jobId) {
        return new QueueLedgerOperationJob(jobId).execute();
    }

    public ProcessResult createSUICreditsJob(SUICreditsJobDTO pSUICreditsJobDTO) {
        return new CreateSUICreditsJob(pSUICreditsJobDTO).execute();
    }
    public ProcessResult deleteLedgerOperationJob(SpcfUniqueId jobId) {
        return new DeleteLedgerOperationJob(jobId).execute();
    }


}
