package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Apr 9, 2009
 * Time: 5:59:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReInitiateRAFEnrollment extends Process implements IProcess {
    private RAFEnrollment mExistingEnrollment;

    public ReInitiateRAFEnrollment(RAFEnrollment pRAFEnrollment) {
        mExistingEnrollment = pRAFEnrollment;
    }

    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        if (mExistingEnrollment == null) {
            result.getMessages().InvalidValue(EntityName.RAFEnrollment, null, "RAFEnrollment not specified.");
            return result;
        }

        if (mExistingEnrollment.getStatus() != RAFEnrollmentStatus.Rejected) {
            result.getMessages().GenericError(EntityName.RAFEnrollment, mExistingEnrollment.getId().toString(), "A RAFEnrollment cannot be re-enrolled unless its status is Rejected.");
            return result;
        }

        return result;
    }

    public ProcessResult<RAFEnrollment> process() {
        ProcessResult<RAFEnrollment> result = new ProcessResult<RAFEnrollment>();

        mExistingEnrollment.updateEnrollmentStatus(RAFEnrollmentStatus.Cancelled);
        RAFEnrollment newEnrollment = RAFEnrollment.createNewEnrollment(mExistingEnrollment.getCompanyAgency());

        result.setResult(newEnrollment);

        return result;
    }
}
