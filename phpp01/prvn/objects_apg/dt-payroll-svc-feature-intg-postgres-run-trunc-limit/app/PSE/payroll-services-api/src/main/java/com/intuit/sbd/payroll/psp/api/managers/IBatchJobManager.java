package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.LedgerOperationJobDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SUICreditsJobDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * @author Wiktor Kozlik
 */
public interface IBatchJobManager {

    /**
     * Scheduling a second offload. Currently, second offloads may only be scheduled for the current day at 19:00.
     * @param pOffloadGroup The offload group for which to schedule a second offload (i.e. STD)
     * @param pOffloadDateTime The date and time of the second offload (today @ 19:00)
     * @return
     */
    ProcessResult scheduleSecondOffload(OffloadGroup pOffloadGroup, SpcfCalendar pOffloadDateTime);

    ProcessResult initiateRAFTapeCreation(RAFActionCode pRAFActionCode);

    ProcessResult initiateRAFTapeRecreation(RAFEnrollmentFile pRAFEnrollmentFile);

    /**
     * Function to execute the Gems Monthly File Generation process thru the flux workflow.
     * @return ProcessResult result
     */
    ProcessResult monthlyGemsUpload();

    ProcessResult<CheckPrintBatch> updateCheckPrintBatchStatus(String pBatchId, CheckPrintBatchStatus pCheckPrintBatchStatus);

    ProcessResult addLedgerOperationJob(LedgerOperationJobDTO pLedgerOperationJobDTO);

    ProcessResult queueLedgerOperationJob(SpcfUniqueId jobId);

    ProcessResult createSUICreditsJob(SUICreditsJobDTO pSUICreditsJobDTO);
    ProcessResult deleteLedgerOperationJob(SpcfUniqueId jobId);
}
