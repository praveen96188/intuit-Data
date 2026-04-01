package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.EftpsEnrollment;
import com.intuit.sbd.payroll.psp.domain.EftpsEnrollmentStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: dweinberg
 * Date: 2/1/13
 * Time: 11:11 AM
 */
public class UpdateEftpsEnrollmentCore extends AddOrUpdateEftpsEnrollmentCore {

    public UpdateEftpsEnrollmentCore(SourceSystemCode pSrcSystemCd, String pSrcCompanyId, EftpsEnrollmentStatus pEftpsEnrollmentStatus) {
        super(pSrcSystemCd, pSrcCompanyId, pEftpsEnrollmentStatus);
    }

    public UpdateEftpsEnrollmentCore(EftpsEnrollment pEftpsEnrollment, EftpsEnrollmentStatus pEftpsEnrollmentStatus) {
        super(pEftpsEnrollment, pEftpsEnrollmentStatus);
    }

    @Override
    public ProcessResult validate() {
        ProcessResult result = super.validate();

        //
        // Get existing enrollment (if any)
        //
        if (mExistingEnrollment == null) {
            mExistingEnrollment = mCompany.getCurrentEnrollment();
        }

        //
        // Determine if the transition is allowed
        //
        if (mExistingEnrollment != null &&
                mExistingEnrollment.getStatusCd() != mNewEftpsEnrollmentStatus &&
                !EftpsEnrollment.isAllowedTransition(mExistingEnrollment, mNewEftpsEnrollmentStatus)) {
            String sourceId = String.format("%s:%s", mSrcSystemCd.toString(), mSrcCompanyId);
            String oldStatus = (mExistingEnrollment.getStatusCd() == null) ? "<none>" : mExistingEnrollment.getStatusCd().toString();
            String newStatus = (mNewEftpsEnrollmentStatus == null) ? "<none>" : mNewEftpsEnrollmentStatus.toString();

            result.getMessages().EnrollmentStateTransitionNotAllowed(EntityName.EftpsEnrollment,
                                                                     sourceId,
                                                                     EntityName.EftpsEnrollment.toString(),
                                                                     oldStatus,
                                                                     newStatus);
        }


        return result;
    }
}
