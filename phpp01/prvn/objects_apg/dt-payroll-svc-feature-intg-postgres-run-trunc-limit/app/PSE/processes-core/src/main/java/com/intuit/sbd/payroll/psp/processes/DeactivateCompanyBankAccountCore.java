package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 *
 * User: mvillani
 * Date: Aug 30, 2007
 * Time: 4:53:34 PM

 */
public class DeactivateCompanyBankAccountCore extends Process implements IProcess {

    /**
     * Core process for adding a new employee bank account.
     *
     * @author Marcela Villani
     */

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private String sourceCompanyBankAccountId;
    private CompanyBankAccount foundCompanyBankAccount;
    private boolean shouldAllowPendingTransactions;
    private boolean ignoreSystemCapabilityChk;


    public CompanyBankAccount getCompanyBankAccount() {
        return foundCompanyBankAccount;
    }

    public DeactivateCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                       String pSourceCompanyBankAccountId, boolean pShouldAllowPendingTransactions,
                                       boolean pIgnoreSystemCapabilityChk) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        sourceCompanyBankAccountId = pSourceCompanyBankAccountId;
        shouldAllowPendingTransactions = pShouldAllowPendingTransactions;
        ignoreSystemCapabilityChk = pIgnoreSystemCapabilityChk;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (foundCompanyBankAccount.getStatusCd() == BankAccountStatus.Inactive) {
            // Bank account has been already deactivated between calls to validate() and process().
            return processResult;
        }

        // deactivate the account
        foundCompanyBankAccount = foundCompanyBankAccount.deactivate();

        processResult.setResult(foundCompanyBankAccount);
        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists
        Company foundCompany = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (foundCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        // Check if the company bank account exists
        foundCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(foundCompany,sourceCompanyBankAccountId);
        if (foundCompanyBankAccount == null) {
            // Verify that the company bank account is not already inactive
            if (foundCompany.deactivatedCBAExistsForSourceBankAccountId(sourceCompanyBankAccountId)) {
               validationResult.getMessages().CompanyBankAccountAlreadyInactive(EntityName.CompanyBankAccount,
                       sourceCompanyBankAccountId, sourceCompanyBankAccountId, sourceSystemCd.toString(),
                       sourceCompanyId);
            } else {
                validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                        sourceCompanyBankAccountId, sourceCompanyBankAccountId, sourceSystemCd.toString(),
                        sourceCompanyId);
            }
        } else if (!shouldAllowPendingTransactions) {
            validationResult.merge( checkPendingTransactions() );
        }

        if (!ignoreSystemCapabilityChk) {
            validationResult.merge( checkSystemCapability(foundCompany) );
        }

        return validationResult;
    }


    /**
     * This validation requires that there be no FinancialTransactions (other than bank verification debits) in the
     * Created or Executed states, and no unresolved TransactionReturns. 
     */
    private ProcessResult checkPendingTransactions() {
        ProcessResult result = new ProcessResult();

        // Validate that there are no (CREATED) financial transactions - excluding Employer Verification Debits
        DomainEntitySet<FinancialTransaction> created =
                FinancialTransaction.findFinancialTransactionsExcludedType(sourceSystemCd,
                        sourceCompanyId, foundCompanyBankAccount.getBankAccount(),
                    TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        if (!created.isEmpty()) {
            result.getMessages().CompanyBankAccountHasPendingFinancialTransactions(
                    EntityName.CompanyBankAccount, sourceCompanyBankAccountId,
                    sourceCompanyBankAccountId, sourceSystemCd.toString(), sourceCompanyId);
        }

        // Validate that there are no (EXECUTED) financial transactions - excluding Employer Verification Debits
        DomainEntitySet<FinancialTransaction> executed =
                FinancialTransaction.findFinancialTransactionsExcludedType(sourceSystemCd,
                        sourceCompanyId, foundCompanyBankAccount.getBankAccount(),
                    TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);
        if (!executed.isEmpty()) {
            result.getMessages().CompanyBankAccountHasRecentFinancialTransactions(
                    EntityName.CompanyBankAccount, sourceCompanyBankAccountId,
                    sourceCompanyBankAccountId, sourceSystemCd.toString(), sourceCompanyId);
        }

        // Validate that there are no unresolved bank returns for this bank account
        DomainEntitySet<TransactionReturn> returns =
                TransactionReturn.findTransactionReturnsExcludedStatus(foundCompanyBankAccount.getCompany(),
                    foundCompanyBankAccount.getBankAccount(),TransactionReturnStatusCode.Resolved);
         if (!returns.isEmpty()) {
             result.getMessages().CompanyBankAccountHasUnresolvedBankReturns(EntityName.CompanyBankAccount,
                     sourceCompanyBankAccountId, sourceCompanyBankAccountId, sourceSystemCd.toString(), sourceCompanyId);
         }

        return result;
    }

    /**
     * This validation requires that the ChangeEmployerBankAccount system capability be allowed for the company.
     */
    private ProcessResult checkSystemCapability(Company pCompany) {
        ProcessResult result = new ProcessResult();
        if (! pCompany.isAllowedCapability(SystemCapabilityCode.ChangeEmployerBankAccount)) {
            result.getMessages().CompanyOperationNotAllowed(pCompany.getSourceSystemCd().toString(),
                                                            pCompany.getSourceCompanyId(),
                                                            SystemCapabilityCode.ChangeEmployerBankAccount.toString());
        }
        return result;
    }
}
