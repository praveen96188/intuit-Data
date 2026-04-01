package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: dweinberg
 * Date: Dec 21, 2009
 * Time: 12:46:10 PM
 */
public class UnlockCompanyPINCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;

    private Company mCompany;

    public UnlockCompanyPINCore(SourceSystemCode pSourceSystemCd, String sourceCompanyId) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = sourceCompanyId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        //make sure company is locked
        if (mCompany.getAccountLockedUntil() == null || PSPDate.getPSPTime().after(mCompany.getAccountLockedUntil())){
            validationResult.getMessages().GenericError(EntityName.Company, mSourceCompanyId, "Company must be locked");
            return validationResult;
        }

        return validationResult;

    }

    @Override
    public ProcessResult process() {
        ProcessResult pr = new ProcessResult();

        //remove lock
        mCompany.setAccountLockedUntil(null);

        //set attempt count to be 1 less than max so if they fail, it will lock again
        int maxNumberOfAttempts = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(mSourceSystemCd,
                SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts).getParameterValue());
        mCompany.setNumberOfFailedLoginAttempts(maxNumberOfAttempts - 1);

        //add event
        CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.PINUnlocked);

        return pr;
    }
    
}
