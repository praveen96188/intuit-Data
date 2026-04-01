package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.EftpsEnrollment;
import com.intuit.sbd.payroll.psp.domain.EftpsEnrollmentStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * User: dweinberg
 * Date: 2/1/13
 * Time: 11:05 AM
 */
public class CreateSecondaryEftpsEnrollment extends AddOrUpdateEftpsEnrollmentCore {

    private String mFedTaxId;
    private String mLegalName;
    private String mLegalZip;

    public CreateSecondaryEftpsEnrollment(SourceSystemCode pSrcSystemCd, String pSrcCompanyId, String pFedTaxId, String pLegalName, String pLegalZip) {
        super(pSrcSystemCd, pSrcCompanyId, EftpsEnrollmentStatus.PendingEnrollment);
        mFedTaxId = pFedTaxId;
        mLegalName = pLegalName;
        mLegalZip = pLegalZip;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = super.validate();
        if (mFedTaxId == null || !Validator.isMatchingPattern(mFedTaxId, "^[0-9]{9}$")) {
            validationResult.getMessages().InvalidValue(EntityName.Company, mFedTaxId, "FedTaxId");
        }

        if ((mLegalName == null) || !Validator.isValidLength(mLegalName, 1, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.Company, mFedTaxId, "LegalName");
        }

        if (mLegalZip == null || !Validator.isValidLength(mLegalZip, 5, 5)) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "ZipCode");
        }
        return validationResult;
    }

    @Override
    public ProcessResult<EftpsEnrollment> process() {
        ProcessResult<EftpsEnrollment> result = new ProcessResult<EftpsEnrollment>();

        EftpsEnrollment enrollment = EftpsEnrollment.createNewEnrollment(mCompanyAgency, true, mFedTaxId, mLegalName, mLegalZip);

        result.setResult(enrollment);
        return result;
    }
}
