package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.RAFActionCode;
import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentFile;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2008
 * Time: 7:08:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class InitiateRAFTapeCreation extends Process implements IProcess {
    private RAFActionCode rafActionCode;

    public InitiateRAFTapeCreation(RAFActionCode pRAFActionCode) {
        rafActionCode = pRAFActionCode;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (rafActionCode == null) {
            validationResult.getMessages().GenericError(EntityName.RAFEnrollment, null, "Cannot initiate RAFTapeCreation: must specify a RAFActionCode of Add Or Delete");
            return validationResult;
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        RAFEnrollmentFile.createFile(rafActionCode);

        try {
            // schedule RAF file creation
            BatchUtils.scheduleJob(BatchJobType.RAFWriter.name(), null);
        } catch (RuntimeException e) {
            Application.getLogger(InitiateRAFTapeCreation.class).error("Could not schedule RAF file creation", e);
            processResult.getMessages().GenericError(EntityName.RAFEnrollment, "0", "Failed to schedule RAFEnrollment file writer: "+e.getMessage());
        } catch (Exception e) {
            Application.getLogger(InitiateRAFTapeCreation.class).error("Could not schedule RAF file creation", e);
            processResult.getMessages().GenericError(EntityName.RAFEnrollment, "0", "Failed to schedule RAFEnrollment file writer: "+e.getMessage());
        }

        return processResult;
    }
}
