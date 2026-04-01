package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 10, 2008
 * Time: 5:02:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResetBankVerifyRetryCountCore extends Process implements IProcess {
    
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mCompanyBankAccountId;
    private CompanyBankAccount mCompanyBankAccount;

    public ResetBankVerifyRetryCountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                         String pCompanyBankAccountId) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyBankAccountId = pCompanyBankAccountId;
    }

    public CompanyBankAccount getCompanyBankAccount() {
        return mCompanyBankAccount;
    }    

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists
        Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check if company bank account exists and is Active
        mCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(
                company, mCompanyBankAccountId);

        if (mCompanyBankAccount == null) {
            if (company.deactivatedCBAExistsForSourceBankAccountId(mCompanyBankAccountId)) {
                validationResult.getMessages().CompanyBankAccountNotActive(EntityName.CompanyBankAccount,
                        mCompanyBankAccountId, mCompanyBankAccountId, mSourceSystemCd.toString(), mSourceCompanyId);
            } else {
                validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                        mCompanyBankAccountId, mCompanyBankAccountId, mSourceSystemCd.toString(), mSourceCompanyId);
            }
            
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (mCompanyBankAccount != null) {
            mCompanyBankAccount.setVerifyRetryCount(0L);

            mCompanyBankAccount = Application.save(mCompanyBankAccount);
        }

        return processResult;
    }
}
