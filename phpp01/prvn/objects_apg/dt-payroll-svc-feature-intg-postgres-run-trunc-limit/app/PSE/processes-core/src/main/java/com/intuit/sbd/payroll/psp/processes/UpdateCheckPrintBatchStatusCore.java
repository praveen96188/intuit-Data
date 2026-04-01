package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.CheckPrintBatch;
import com.intuit.sbd.payroll.psp.domain.CheckPrintBatchStatus;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 3, 2010
 * Time: 12:32:45 PM
 */
public class UpdateCheckPrintBatchStatusCore extends Process implements IProcess {
    private CheckPrintBatch mCheckPrintBatch;
    private String mBatchId;
    private CheckPrintBatchStatus mCheckPrintBatchStatus;

    public UpdateCheckPrintBatchStatusCore(String pBatchId, CheckPrintBatchStatus pCheckPrintBatchStatus) {
        mBatchId = pBatchId;
        mCheckPrintBatchStatus = pCheckPrintBatchStatus;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mBatchId == null) {
            validationResult.getMessages().InvalidArgument(EntityName.CheckPrintBatch, "Check Print Batch Id", "Check Print Batch Id");
            return validationResult;
        }

        if (mCheckPrintBatchStatus == null) {
            validationResult.getMessages().InvalidArgument(EntityName.CheckPrintBatch, "Check Print Batch Status", "Check Print Batch Status");
            return validationResult;
        }

        // check if batch exists
        mCheckPrintBatch = Application.findById(CheckPrintBatch.class, SpcfUniqueId.createInstance(mBatchId));
        if (mCheckPrintBatch == null) {
            validationResult.getMessages().CheckPrintBatchDoesNotExist(EntityName.CheckPrintBatch, mBatchId, mBatchId);
            return validationResult;
        }

        return validationResult;
    }

    public ProcessResult<CheckPrintBatch> process() {
        ProcessResult<CheckPrintBatch> processResult = new ProcessResult<CheckPrintBatch>();

        mCheckPrintBatch.setCheckPrintBatchStatusCode(mCheckPrintBatchStatus);
        switch (mCheckPrintBatchStatus) {
            case Pending:
                mCheckPrintBatch.setCheckPrintBatchMessage(null);
                mCheckPrintBatch.setSentToPrinter(null);
                break;
        }
        mCheckPrintBatch = Application.save(mCheckPrintBatch);
        processResult.setResult(mCheckPrintBatch);
        processResult.setSuccess(true);

        return processResult;
    }
}
