package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: mamin
 * Date: Apr 9, 2009
 * Time: 5:59:24 PM
 */
public class UpdateRAFEnrollmentStatusCore extends Process implements IProcess {
    private SourceSystemCode mSrcSystemCd;
    private String mSrcCompanyId;
    private CompanyAgency mCompanyToIRS;
    private RAFEnrollment mExistingEnrollment;
    private RAFEnrollmentStatus mNewRAFEnrollmentStatus;
    private Company mCompany;

    public UpdateRAFEnrollmentStatusCore(SourceSystemCode pSrcSystemCd, String pSrcCompanyId, RAFEnrollment pRAFEnrollment, RAFEnrollmentStatus pRAFEnrollmentStatus) {
        mSrcSystemCd = pSrcSystemCd;
        mSrcCompanyId = pSrcCompanyId;
        mExistingEnrollment = pRAFEnrollment;
        mNewRAFEnrollmentStatus = pRAFEnrollmentStatus;
    }

    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        mCompany = Company.findCompany(mSrcCompanyId, mSrcSystemCd);
        if (mCompany == null) {
            result.getMessages().CompanyDoesNotExist(EntityName.Company, mSrcCompanyId, mSrcSystemCd.toString(), mSrcCompanyId);
            return result;
        }


        //
        // If we're Enrolling, validate everything we need from the company to enroll the client
        //
        if (RAFEnrollmentStatus.PendingEnrollment.equals(mNewRAFEnrollmentStatus)) {
            //
            // The company must have non-null and non-empty values for all enrollment-related properties
            //
            if (isNullOrEmpty(mCompany.getFedTaxId())) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "FedTaxId");
            }

            if (isNullOrEmpty(mCompany.getLegalName())) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "LegalName");
            }

            Address legalAddress = mCompany.getLegalAddress();

            if (legalAddress == null) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "LegalAddress");
            } else if (isNullOrEmpty(legalAddress.getZipCode())) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "LegalZipCode");
            }

            if (!result.isSuccess()) {
                return result;
            }

            //
            // Ensure the company is eligible for eftps
            //
            if (!mCompany.isEligibleForRAF()) {
                result.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                                                               mSrcCompanyId,
                                                               mSrcSystemCd.toString(),
                                                               mSrcCompanyId,
                                                               ServiceCode.Tax.toString());
                return result;
            }
        }

        //
        // Confirm the IRS company agency is available
        //
        mCompanyToIRS = CompanyAgency.findCompanyAgency(mCompany, Agency.IRS);
        if (mCompanyToIRS == null) {
            result.getMessages().CompanyAgencyNotFound(EntityName.Company, mSrcCompanyId, mSrcSystemCd.toString(), mSrcCompanyId, "IRS");
            return result;
        }

        if (mExistingEnrollment == null && mNewRAFEnrollmentStatus != RAFEnrollmentStatus.PendingEnrollment) {
            result.getMessages().GenericError(EntityName.RAFEnrollment, null, "Cannot create an enrollment in a status other than PendingEnrollment for company: "+mCompany.getSourceCompanyId());
        }
        
        //
        // Determine if the transition is allowed
        //

        if (mExistingEnrollment!=null && !mExistingEnrollment.getStatus().equals(mNewRAFEnrollmentStatus) && !mExistingEnrollment.isTransitionAllowed(mNewRAFEnrollmentStatus)) {
            String sourceId = String.format("%s:%s", mSrcSystemCd.toString(), mSrcCompanyId);
            String oldStatus = ((mExistingEnrollment == null) || mExistingEnrollment.getStatus() == null) ? "<none>" : mExistingEnrollment.getStatus().toString();
            String newStatus = (mNewRAFEnrollmentStatus == null) ? "<none>" : mNewRAFEnrollmentStatus.toString();


            result.getMessages().EnrollmentStateTransitionNotAllowed(EntityName.RAFEnrollment,
                                                                     sourceId,
                                                                     EntityName.RAFEnrollment.toString(),
                                                                     oldStatus,
                                                                     newStatus);
            }
        return result;
    }

    public ProcessResult<RAFEnrollment> process() {
        ProcessResult<RAFEnrollment> result = new ProcessResult<RAFEnrollment>();
        RAFEnrollment enrollment;

        if (mExistingEnrollment == null) {
            enrollment = RAFEnrollment.createNewEnrollment(mCompanyToIRS);
        } else {
            enrollment = mExistingEnrollment.updateEnrollmentStatus(mNewRAFEnrollmentStatus);
        }

        result.setResult(enrollment);

        return result;
    }

    private boolean isNullOrEmpty(String pValue) {
        return (pValue == null) || (pValue.trim().length() == 0);
    }
}
