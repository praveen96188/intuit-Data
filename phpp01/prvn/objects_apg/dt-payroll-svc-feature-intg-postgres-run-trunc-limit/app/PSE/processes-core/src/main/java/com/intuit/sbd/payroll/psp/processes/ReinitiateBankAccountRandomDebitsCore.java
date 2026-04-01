package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 11, 2008
 * Time: 10:07:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReinitiateBankAccountRandomDebitsCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mCompanyBankAccountId;
    private CompanyBankAccount mCompanyBankAccount;

    public ReinitiateBankAccountRandomDebitsCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                                 String pCompanyBankAccountId) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mCompanyBankAccountId = pCompanyBankAccountId;
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

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Verification Transactions have already offloaded or not.
        DomainEntitySet<FinancialTransaction> verificationTransactions = mCompanyBankAccount
                .getVerificationTransactions();

        for (FinancialTransaction finTxn : verificationTransactions) {
            TransactionStateCode currentStateCode = finTxn.calculateCurrentTransactionState().getTransactionStateCd();
            if (currentStateCode.equals(TransactionStateCode.Created)) {
                validationResult.getMessages().CompanyBankAccountCannotHaveRandomDebitsReissued(
                        EntityName.CompanyBankAccount, mCompanyBankAccountId, mCompanyBankAccountId,
                        mSourceSystemCd.toString(), mSourceCompanyId);
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // Add two verification transactions
        mCompanyBankAccount.addVerificationTransaction();
        mCompanyBankAccount.addVerificationTransaction();

        return processResult;
    }
}
