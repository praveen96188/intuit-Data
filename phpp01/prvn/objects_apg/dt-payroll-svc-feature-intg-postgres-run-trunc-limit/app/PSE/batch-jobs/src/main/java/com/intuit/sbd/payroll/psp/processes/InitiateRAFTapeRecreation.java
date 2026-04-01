package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentFile;
import com.intuit.sbd.payroll.psp.domain.RAFFileStatus;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobManager;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.psp.common.gateway.JSSGateway;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2008
 * Time: 7:08:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class InitiateRAFTapeRecreation extends Process implements IProcess {
    private RAFEnrollmentFile originalEnrollmentFile;

    public InitiateRAFTapeRecreation(RAFEnrollmentFile pRAFEnrollmentFile) {
        originalEnrollmentFile = pRAFEnrollmentFile;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // the requested offload group must be valid (non-null)
        if (originalEnrollmentFile == null) {
            validationResult.getMessages().InvalidValue(EntityName.RAFEnrollment, null, "RAFEnrollment");
            return validationResult;
        }

        if (originalEnrollmentFile.getStatus() != RAFFileStatus.Completed) {
            validationResult.getMessages().GenericError(EntityName.RAFEnrollment, originalEnrollmentFile.getId().toString(), "Cannot initiation re-creation for RAF Enrollment file in status: "+originalEnrollmentFile.getStatus());
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        RAFEnrollmentFile.initiateRecreation(originalEnrollmentFile);

        try {
            // schedule RAF file recreation
            BatchUtils.scheduleJob(BatchJobType.RAFWriter.name(), null);
        } catch (RuntimeException e) {
            Application.getLogger(InitiateRAFTapeRecreation.class).error("Could not schedule RAF tape re-creation", e);
            processResult.getMessages().GenericError(EntityName.RAFEnrollment, "0", "Failed to schedule RAFEnrollment tape writer: "+e.getMessage());
        } catch (Exception e) {
            Application.getLogger(InitiateRAFTapeRecreation.class).error("Could not schedule RAF tape re-creation", e);
            processResult.getMessages().GenericError(EntityName.RAFEnrollment, "0", "Failed to schedule RAFEnrollment tape writer: "+e.getMessage());
        }

        return processResult;
    }
}
