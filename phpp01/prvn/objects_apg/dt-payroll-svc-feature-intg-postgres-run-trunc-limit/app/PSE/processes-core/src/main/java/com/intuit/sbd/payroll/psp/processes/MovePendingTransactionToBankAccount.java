package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.BankAccountOwnerType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: May 7, 2008
 * Time: 1:32:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MovePendingTransactionToBankAccount extends Process {

    private SourceSystemCode sourceSystemCD;
    private String sourceCompanyID;
    private String sourceCompanyBankAccountID;
    private boolean shouldIgnoreBankAccountValidation;
    private Company domainCompany;
    private CompanyBankAccount destinationCompanyBankAccount;

    public MovePendingTransactionToBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                               String pSourceCompanyBankAccountId,
                                               boolean pShouldIgnoreBankAccountValidation) {
        sourceSystemCD = pSourceSystemCode;
        sourceCompanyID = pSourceCompanyId;
        sourceCompanyBankAccountID = pSourceCompanyBankAccountId;
        shouldIgnoreBankAccountValidation = pShouldIgnoreBankAccountValidation;        
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCD, sourceCompanyID));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (sourceCompanyBankAccountID == null) {
            validationResult.getMessages().BankAccountNotSpecified(EntityName.EmployeeBankAccount,
                                                                        sourceCompanyBankAccountID);
            return validationResult;
        }

        //Validate company exists
        domainCompany = Company.findCompany(sourceCompanyID, sourceSystemCD);
        if (domainCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyID,
                    sourceSystemCD.toString(), sourceCompanyID);
            return validationResult;
        }

        if (!shouldIgnoreBankAccountValidation) {
            // validate Company Bank Account exists and active
            destinationCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(domainCompany,
                                        sourceCompanyBankAccountID);
            if (destinationCompanyBankAccount == null) {
                // Verify that company bank account has not been deactivated at some time in the past
                if (domainCompany.deactivatedCBAExistsForSourceBankAccountId(
                        sourceCompanyBankAccountID)) {
                    validationResult.getMessages().CompanyBankAccountNotActive(EntityName.CompanyBankAccount,
                            sourceCompanyBankAccountID, sourceCompanyBankAccountID,
                            sourceSystemCD.toString(), sourceCompanyID);
                } else {
                    validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                            sourceCompanyBankAccountID, sourceCompanyBankAccountID,
                            sourceSystemCD.toString(), sourceCompanyID);
                }
            }
        }
        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        DomainEntitySet<FinancialTransaction> pendingFinancialTxns =
                FinancialTransaction.findPendingFinancialTransactions(domainCompany,
                                                                       TransactionTypeCode.EmployerVerificationDebit,
                                                                       TransactionStateCode.Created,
                                                                       BankAccountOwnerType.Company);

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = null;
        MoneyMovementTransaction moneyMovementTransaction = null;
        for (FinancialTransaction financialTransaction: pendingFinancialTxns) {

            destinationCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(domainCompany,
                                        sourceCompanyBankAccountID);

            if (null == destinationCompanyBankAccount) {
                throw new RuntimeException("No Active CompanyBankAccount exists with the specified ID:" + sourceCompanyBankAccountID);
            }

            if (financialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Company) {
                financialTransaction.setCreditBankAccount(destinationCompanyBankAccount.getBankAccount());
            } else {
                financialTransaction.setDebitBankAccount(destinationCompanyBankAccount.getBankAccount());
            }
            moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
            
            // add new entry detail records
            if (moneyMovementTransaction.getMoneyMovementPaymentMethod().equals(PaymentMethod.ACHDirectDeposit)) {
                MoneyMovementTransaction.recreateEntryDetailRecords(moneyMovementTransaction);
            }

        }

        return processResult;
    }

}
