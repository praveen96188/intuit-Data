package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationJob;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationJobStatus;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * User: sshetty
 * Date: 9/25/13
 * Time: 12:52 AM
 */
public class DeleteLedgerOperationJob extends Process {
    private SpcfUniqueId jobId;
    private LedgerOperationJob job;


    public DeleteLedgerOperationJob(SpcfUniqueId pJobId) {
        jobId = pJobId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (jobId == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LedgerOperation, "", "JobId");
        }

        job = Application.findById(LedgerOperationJob.class, jobId);

        if (job == null) {
            validationResult.getMessages().EntityDoesNotExist(EntityName.LedgerOperation, "", "LedgerOperationJob", jobId.toString());
        }

        if (job.getStatus() != LedgerOperationJobStatus.Created) {
            validationResult.getMessages().GenericError(EntityName.LedgerOperation, jobId.toString(), "Job must be in created status");
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        job.setStatus(LedgerOperationJobStatus.Deleted);

        return processResult;
    }
}


