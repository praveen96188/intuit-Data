package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import org.apache.commons.lang.StringUtils;

/**
 * User: wnichols, kpaul
 * Date: Mar 18, 2009
 * Time: 10:36:57 AM
 */
public abstract class AddOrUpdateEftpsEnrollmentCore extends Process {
    protected SourceSystemCode mSrcSystemCd = null;
    protected String mSrcCompanyId = null;
    protected CompanyAgency mCompanyAgency = null;
    protected EftpsEnrollment mExistingEnrollment = null;
    protected EftpsEnrollmentStatus mNewEftpsEnrollmentStatus = null;
    protected Company mCompany = null;

    public AddOrUpdateEftpsEnrollmentCore(SourceSystemCode pSrcSystemCd, String pSrcCompanyId, EftpsEnrollmentStatus pEftpsEnrollmentStatus) {
        mSrcSystemCd = pSrcSystemCd;
        mSrcCompanyId = pSrcCompanyId;
        mNewEftpsEnrollmentStatus = pEftpsEnrollmentStatus;
    }

    public AddOrUpdateEftpsEnrollmentCore(EftpsEnrollment pEftpsEnrollment, EftpsEnrollmentStatus pEftpsEnrollmentStatus) {
        mExistingEnrollment = pEftpsEnrollment;
        mCompanyAgency = mExistingEnrollment.getCompanyAgency();
        mCompany = mCompanyAgency.getCompany();
        mSrcSystemCd = mCompany.getSourceSystemCd();
        mSrcCompanyId = mCompany.getSourceCompanyId();
        mNewEftpsEnrollmentStatus = pEftpsEnrollmentStatus;
    }

    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        if (mSrcCompanyId == null || !Validator.isValidLength(mSrcCompanyId, 1, 50)) {
            result.getMessages().InvalidValue(EntityName.Company, mSrcCompanyId, "SourceCompanyId");
        }

        if (mSrcSystemCd == null || !Validator.isValidLength(mSrcSystemCd.toString(), 1, 10)) {
            result.getMessages().InvalidValue(EntityName.Company, mSrcCompanyId, "SourceSystemCD");
        }

        if (!result.isSuccess()) {
            return result;
        }

        //
        // Find the company if not already assigned
        //
        if (mCompany == null) {
            mCompany = Company.findCompany(mSrcCompanyId, mSrcSystemCd);

            if (mCompany == null) {
                result.getMessages().CompanyDoesNotExist(EntityName.Company, mSrcCompanyId, mSrcSystemCd.toString(), mSrcCompanyId);
                return result;
            }
        }

        //
        // If we're Enrolling, validate everything we need from the company to enroll the client
        //
        if (mNewEftpsEnrollmentStatus == EftpsEnrollmentStatus.PendingEnrollment) {
            //
            // The company must have non-null and non-empty values for all enrollment-related properties
            //
            if (StringUtils.isEmpty(mCompany.getFedTaxId())) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "FedTaxId");
            }

            if (StringUtils.isEmpty(mCompany.getLegalName())) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "LegalName");
            }

            Address legalAddress = mCompany.getLegalAddress();

            if (legalAddress == null) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "LegalAddress");
            } else if (StringUtils.isEmpty(legalAddress.getZipCode())) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSrcCompanyId, "LegalZipCode");
            }

            if (!result.isSuccess()) {
                return result;
            }

            //
            // Ensure the company is eligible for eftps
            //
            if (!mCompany.isEligibleForEftps()) {
                result.getMessages().CompanyNotActiveOnService(EntityName.Company,
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
        if (mCompanyAgency == null) {
            mCompanyAgency = CompanyAgency.findCompanyAgency(mCompany, Agency.IRS);

            if (mCompanyAgency == null) {
                result.getMessages().CompanyAgencyNotFound(EntityName.Company, mSrcCompanyId, mSrcSystemCd.toString(), mSrcCompanyId, "IRS");
                return result;
            }
        }


        return result;
    }

    public ProcessResult<EftpsEnrollment> process() {
        ProcessResult<EftpsEnrollment> result = new ProcessResult<EftpsEnrollment>();
        EftpsEnrollment enrollment;

        switch (EftpsEnrollment.resolveTransitionAction(mExistingEnrollment, mNewEftpsEnrollmentStatus)) {
            case ENROLL:
                enrollment = EftpsEnrollment.createNewEnrollment(mCompanyAgency);
                break;

            case REENROLL:
                mExistingEnrollment.updateEnrollmentStatus(EftpsEnrollmentStatus.Cancelled);
                //Secondary enrollments never re-enroll; a new one must be created
                if (!mExistingEnrollment.getSecondary()) {
                    enrollment = EftpsEnrollment.createNewEnrollment(mCompanyAgency);
                } else {
                    enrollment = mExistingEnrollment;
                }
                break;

            case UPDATE:
                enrollment = mExistingEnrollment.updateEnrollmentStatus(mNewEftpsEnrollmentStatus);
                break;

            default: // NOTALLOWED
                enrollment = mExistingEnrollment; // essentially a no-op (just for safety, but should never happen)
                break;
        }

        result.setResult(enrollment);

        return result;
    }

}
