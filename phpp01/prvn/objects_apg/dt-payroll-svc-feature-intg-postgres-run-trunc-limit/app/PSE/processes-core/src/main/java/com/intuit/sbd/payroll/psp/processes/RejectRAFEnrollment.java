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
public class RejectRAFEnrollment extends Process implements IProcess {
    private RAFEnrollment mExistingEnrollment;
    private String mRejectReason;

    public RejectRAFEnrollment(RAFEnrollment pRAFEnrollment, String pRejectReason) {
        mExistingEnrollment = pRAFEnrollment;
        mRejectReason = pRejectReason;
    }

    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        if (mExistingEnrollment == null) {
            result.getMessages().InvalidValue(EntityName.RAFEnrollment, null, "RAFEnrollment not specified.");
            return result;
        }

        if (!mExistingEnrollment.isTransitionAllowed( RAFEnrollmentStatus.Rejected)) {
            result.getMessages().GenericError(EntityName.RAFEnrollment, mExistingEnrollment.getId().toString(), "A RAFEnrollment cannot be rejected in its current status: "+mExistingEnrollment.getStatus());
            return result;
        }

        return result;
    }

    public ProcessResult process() {
        ProcessResult result = new ProcessResult();
        Company company = mExistingEnrollment.getCompanyAgency().getCompany();

        CompanyService taxService = company.getCompanyService(ServiceCode.Tax);

        mExistingEnrollment.setStatusReason(mRejectReason); 
        mExistingEnrollment.updateEnrollmentStatus(RAFEnrollmentStatus.Rejected);

        if (taxService!=null && !company.isCompanyOnService(ServiceCode.Tax)) {
            mExistingEnrollment.updateEnrollmentStatus(RAFEnrollmentStatus.Cancelled);
        }

        return result;
    }
}
