package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Iterator;

/**
 * User: mvillani
 * Date: Aug 28, 2007
 * Time: 1:48:49 PM
 */
public class VerifyCompanyBankAccountCore extends Process implements IProcess {
    /**
     * Core process for  verifying the two transaction amounts that were debited from
     * the company�s bank account.
     * When these amounts match the amounts in the verification transaction for this company
     * the service changes the status for the company bank account from Pending Verification to Active.
     *
     * author Marcela Villani
     */

    private boolean mAgentVerify;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mSourceCompanyBankAccountId;
    private SpcfMoney amount1;
    private SpcfMoney amount2;
    private boolean mPassedVerification = false;
    private CompanyBankAccount mFoundCompanyBankAccount;
    private Company mCompany;
    private DeactivateCompanyBankAccountCore deactivateActiveCBAprocess;


    public CompanyBankAccount getCompanyBankAccount() {
        return mFoundCompanyBankAccount;
    }

    public VerifyCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                        String pSourceCompanyBankAccountId, SpcfMoney pAmount1, SpcfMoney pAmount2, boolean pAgentVerify) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mSourceCompanyBankAccountId = pSourceCompanyBankAccountId;
        amount1 = pAmount1;
        amount2 = pAmount2;
        mAgentVerify = pAgentVerify;
    }

    public ProcessResult process() {
        ProcessResult<CompanyBankAccount> processResult = new ProcessResult<CompanyBankAccount>();
        if (mPassedVerification || mAgentVerify) {

            // Change status from PendingVerification to Active
            mFoundCompanyBankAccount.updateBankAccountStatus(BankAccountStatus.Active);

            // Set status effective date to current date
            mFoundCompanyBankAccount.setStatusEffectiveDate(PSPDate.getPSPTime());

            // Set verifyRetryCount = 0
            mFoundCompanyBankAccount.setVerifyRetryCount(0);

            for (CompanyService companyService : mCompany.getCompanyServiceCollection()) {
                if (companyService.getStatusCd() == ServiceSubStatusCode.PendingBankVerification) {
                    ServiceSubStatusCode nextServiceSubStatusCd = companyService.getNextValidServiceStatus(ServiceSubStatusCode.PendingBankVerification);

                    companyService.updateCompanyServiceStatus(nextServiceSubStatusCd);
                }
            }
            CompanyEvent.createCBAVerifiedEvent(mFoundCompanyBankAccount);
            
            // We have to deactivate current active bank account
            if (deactivateActiveCBAprocess != null) {
                processResult.merge(deactivateActiveCBAprocess.process());
            }

            // this is not the right place for this, but it's all I got
            if(mCompany.getSourceSystemCd() == SourceSystemCode.QBDT) {
                DomainEntitySet<FinancialTransaction> offloadedTransactions =
                        mFoundCompanyBankAccount.getVerificationTransactions().find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Executed,
                                                                                                                                                           TransactionStateCode.Completed));
                for (FinancialTransaction financialTransaction : offloadedTransactions.sort(FinancialTransaction.FinancialTransactionAmount())) {
                    processResult.merge(PayrollServices.companyManager.generateLiabilityCheck(mCompany, financialTransaction));
                }
            }
        }

        if (mAgentVerify) {
            //agent can verify before transactions offload, so cancel any pending ones.
            FinancialTransaction.cancelPendingEmployerVerificationDebits(mCompany);
        }

        mFoundCompanyBankAccount = Application.save(mFoundCompanyBankAccount);

        processResult.setResult(mFoundCompanyBankAccount);
        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult<CompanyBankAccount> validationResult = new ProcessResult<CompanyBankAccount>();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
        }
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (!mCompany.isAllowedCapability(SystemCapabilityCode.VerifyCompanyBankAccount)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                mCompany.getSourceSystemCd().toString(),
                mCompany.getSourceCompanyId(), SystemCapabilityCode.VerifyCompanyBankAccount.toString());
             return validationResult;
        }

        SourcePayrollParameter allowReverifyParam = SourcePayrollParameter.findSourcePayrollParameter(
                mSourceSystemCd, SourcePayrollParameterCode.AllowReverifyBankAccount);
        boolean allowReverify = allowReverifyParam.getParameterValue().equals("1") && !mCompany.isPINCreated();


        // We will deactivate current active bank account
        CompanyBankAccount currentActiveBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
        if (currentActiveBankAccount != null && !(currentActiveBankAccount.getSourceBankAccountId().equals(mSourceCompanyBankAccountId))) {
            deactivateActiveCBAprocess = new DeactivateCompanyBankAccountCore(
                    mSourceSystemCd, mSourceCompanyId, currentActiveBankAccount.getSourceBankAccountId(), true, false); 
            validationResult.merge(deactivateActiveCBAprocess.validate());
        }

        // Check if the company bank account exists

        mFoundCompanyBankAccount = CompanyBankAccount
                .findCompanyBankAccount(mCompany, mSourceCompanyBankAccountId);
        if (mFoundCompanyBankAccount == null) {
            if (mCompany.deactivatedCBAExistsForSourceBankAccountId(
                    mSourceCompanyBankAccountId)) {
               validationResult.getMessages().CompanyBankAccountStatusNotPendingVerification(
                            EntityName.CompanyBankAccount, mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                            mSourceSystemCd.toString(), mSourceCompanyId,
                            BankAccountStatus.Inactive.toString());
            } else {
                validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                    mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            }

        } else {
            // Verify that company bank account has a status of Pending Verification
            if (mFoundCompanyBankAccount.getStatusCd() != BankAccountStatus.PendingVerification) {
                if (mFoundCompanyBankAccount.getStatusCd() == BankAccountStatus.Active) {
                    if (!allowReverify) {
                        validationResult.getMessages().CompanyBankAccountAlreadyVerified(EntityName.CompanyBankAccount,
                                mSourceCompanyBankAccountId, mSourceCompanyBankAccountId, mSourceSystemCd.toString(),
                                mSourceCompanyId);
                    }
                }
            }

            // Check VerificationRetryCount
            if (!mAgentVerify && mCompany.isCompanyOnService(ServiceCode.DirectDeposit) && mFoundCompanyBankAccount.getVerifyRetryCount() >=
                    new Long(LimitRule.findLimitRule(mCompany, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.CompanyBankAccountVerificationAttemptLimit).getValue())) {
                validationResult.getMessages().CompanyBankAccountGreaterVerifyRetryCount(EntityName.CompanyBankAccount,
                        mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                        mSourceSystemCd.toString(), mSourceCompanyId,
                        Long.valueOf(mFoundCompanyBankAccount.getVerifyRetryCount()).toString());
            }

            if (!mAgentVerify) {
                // Check if Verification Transactions exist and what their status is
                DomainEntitySet<FinancialTransaction> verificationTransactions = mFoundCompanyBankAccount
                        .getVerificationTransactions();
                if (verificationTransactions.size() != 2) {
                    validationResult.getMessages().CompanyBankAccountNoRecentVerificationTxns(EntityName.CompanyBankAccount,
                                                                                              mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                                                                                              mSourceSystemCd.toString(), mSourceCompanyId);
                    validationResult.setResult(mFoundCompanyBankAccount);
                    return validationResult;
                }
                for (FinancialTransaction financialTransaction : verificationTransactions) {
                    TransactionStateCode currentTransactionState = financialTransaction.calculateCurrentTransactionState()
                                                                                       .getTransactionStateCd();
                    if ((currentTransactionState.equals(TransactionStateCode.Created)) ||
                            (financialTransaction.getSettlementDate().toLocal().after(PSPDate.getPSPTime()))) {
                        validationResult.getMessages().CompanyBankAccountVerificationTxnsNotIssuedToBank(
                                EntityName.CompanyBankAccount, mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                                mSourceSystemCd.toString(), mSourceCompanyId);
                    }

                    if (currentTransactionState.equals(TransactionStateCode.Cancelled)) {
                        validationResult.getMessages().CompanyBankAccountVerificationTxnsCanceled(
                                EntityName.CompanyBankAccount, mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                                mSourceSystemCd.toString(), mSourceCompanyId);
                    }

                    if (currentTransactionState.equals(TransactionStateCode.Returned)) {
                        validationResult.getMessages().CompanyBankAccountVerificationTxnsReturned(
                                EntityName.CompanyBankAccount, mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                                mSourceSystemCd.toString(), mSourceCompanyId);
                    }
                }
                if (!validationResult.isSuccess()) {
                    validationResult.setResult(mFoundCompanyBankAccount);
                    return validationResult;
                }

                if ((compareTransactionAmounts(verificationTransactions, amount1, amount2))) {
                    mPassedVerification = true;
                }
                else {
                    validationResult.getMessages().CompanyBankAccountFailedVerification(EntityName.CompanyBankAccount,
                                                                                        mSourceCompanyBankAccountId, mSourceCompanyBankAccountId,
                                                                                        mSourceSystemCd.toString(), mSourceCompanyId);
                    validationResult.merge(process());
                }
            }
            validationResult.merge(updateRetryFields());
        }
        validationResult.setResult(mFoundCompanyBankAccount);
        return validationResult;
    }


    private boolean compareTransactionAmounts(DomainEntitySet<FinancialTransaction> pVerificationTransactions,
                                              SpcfMoney pAmount1, SpcfMoney pAmount2) {
        boolean amountsMatch = false;

        Iterator<FinancialTransaction> it = pVerificationTransactions.iterator();
        it.next();
        FinancialTransaction secondItem = it.next();

        boolean testMatchFirstCombination = pVerificationTransactions.get(0).getFinancialTransactionAmount()
                .equals(pAmount1) && secondItem.getFinancialTransactionAmount()
                .equals(pAmount2);
        boolean testMatchSecondCombination = pVerificationTransactions.get(0).getFinancialTransactionAmount()
                .equals(pAmount2) && secondItem.getFinancialTransactionAmount()
                .equals(pAmount1);

        if (testMatchFirstCombination || testMatchSecondCombination) {
            amountsMatch = true;
        }

        return amountsMatch;
    }

    /**
     * Function to update the Last Retry Date Time irrespective of Failure/Success validation.
     * @return result
     */
    private ProcessResult updateRetryFields() {
        ProcessResult result = new ProcessResult();
        if(mFoundCompanyBankAccount != null){
            // Set LastRetryDateTime
            mFoundCompanyBankAccount.setLastRetryDate(PSPDate.getPSPTime());

            // Update VerifyRetryCount if didn't pass verification
            if (!mPassedVerification) {
               // Add 1 to verifyRetryCount
               mFoundCompanyBankAccount.setVerifyRetryCount(mFoundCompanyBankAccount.getVerifyRetryCount() + 1);
            }

            mFoundCompanyBankAccount = Application.save(mFoundCompanyBankAccount);
        }

        mFoundCompanyBankAccount = CompanyBankAccount
                .findCompanyBankAccount(mCompany, mSourceCompanyBankAccountId);

        return result;
    }
}
