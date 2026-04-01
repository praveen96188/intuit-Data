/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddEmployeeReturnRefundTransaction.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;

/**
 * User: rkrishna
 * Date: Dec 13, 2007
 * Time: 4:16:37 PM
 */
public class AddEmployeeReturnRefundTransaction extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private RefundDTO mRefundDTO;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private BankAccount mBankAccount = null;
    private IntuitBankAccount mDebitIntuitBankAccount;
    private TransactionResponse mTransactionResponse;
    private FinancialTransaction mFinancialTransaction;

    public AddEmployeeReturnRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                              RefundDTO pRefundDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mRefundDTO = pRefundDTO;
    }

    public TransactionResponse getTransactionResponse() {
        return mTransactionResponse;
    }

    public FinancialTransaction getFinancialTransaction() {
        return mFinancialTransaction;
    }


    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();
        CompanyBankAccount companyBankAccount;

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mRefundDTO.getSourcePayrollRunId() == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.PayrollRun, mSourceCompanyId, "PayrollRunId");
            return validationResult;
        }

        //Check if comany exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        //Check if payroll run exists

        mPayrollRun = PayrollRun.findPayrollRun(mCompany,
                mRefundDTO.getSourcePayrollRunId());

        if (mPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                    mRefundDTO.getSourcePayrollRunId(), mRefundDTO.getSourcePayrollRunId(),
                    mSourceSystemCode.toString(), mSourceCompanyId);

            return validationResult;
        }

        TransactionType transactionType = TransactionType.
                findTransactionType(TransactionTypeCode.EmployerDdRejectRefundCredit);

        mDebitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {
            //Find active company bank account
            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

            if (companyBankAccount == null) {
                validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                        mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);

                return validationResult;
            } else {
                mBankAccount = companyBankAccount.getBankAccount();
            }

            // Verify the company is associated with the DD Service
            DDCompanyServiceInfo mDomainDDCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                    .findCompanyService(mCompany, ServiceCode.DirectDeposit);

            if (mDomainDDCompanyServiceInfo == null) {
                validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                        mSourceCompanyId, mSourceSystemCode.toString(),
                        mSourceCompanyId, ServiceCode.DirectDeposit.toString());
            }

            // Throw an exception if there are any pending paycheck transactions
            // for this payroll run
            DomainEntitySet<FinancialTransaction> pendingTransactions = mPayrollRun.
                    getFinancialTransactions(null,
                            new TransactionStateCode[]{TransactionStateCode.Created});

            String intuitBankId = mDebitIntuitBankAccount.getBankAccount().getAccountNumber();

            for (FinancialTransaction financialTransaction : pendingTransactions) {
                BankAccount creditBankAccount = financialTransaction.getCreditBankAccount();
                BankAccount debitBankAccount = financialTransaction.getDebitBankAccount();

                String creditBankAccountId = null;
                String debitBankAccountId = null;

                if (creditBankAccount != null) {
                    creditBankAccountId = creditBankAccount.getAccountNumber();
                }

                if (debitBankAccount != null) {
                    debitBankAccountId = debitBankAccount.getAccountNumber();
                }

                if (intuitBankId.equals(creditBankAccountId) || intuitBankId.equals(debitBankAccountId)) {
                    validationResult.getMessages().PendingTransactionAlreadyExists(EntityName.FinancialTransaction,
                            mPayrollRun.getSourcePayRunId());
                }
            }

            /**
             * (DD179) Verify there is no existing financial transaction associated with this company and this
             * payroll where Tx Type is EEDDRVDB
             * Current Tx Status is “CR” or “EX”
             * If such a financial transaction exists throw the exception
             * This transaction cannot be created until all existing reversal transactions for this payroll have a
             * status of Complete.
             */

            DomainEntitySet<FinancialTransaction> erReversalDebitTransactions = mPayrollRun.
                    getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                            new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Executed});

            if (!erReversalDebitTransactions.isEmpty()) {
                validationResult.getMessages()
                        .CreateTxnFailureReversalTxnIncomplete(EntityName.FinancialTransaction,
                                mPayrollRun.getSourcePayRunId());
            }

            /**
             * Verify that this is a valid ACTION for this payroll run given the
             * state of the ledger for the payroll run (see Rep UI Allowable
             * Actions.xls)
             * If not throw the exception. Action{0} not valid for payroll run
             * with DDTxBatchID {1} due to current ledger account balances.
             */
            if (!mPayrollRun.validateAction(ActionEventCode.EEReturnRefund)) {
                validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun,
                        mPayrollRun.getSourcePayRunId(), ActionEventCode.EEReturnRefund.toString(),
                        mPayrollRun.getSourcePayRunId());
                return validationResult;
            }
        } else {
            //validation amount
            ProcessResult validateDDRefundDTOtResult = mRefundDTO.validateDDRefundDTO();
            validationResult.merge(validateDDRefundDTOtResult);

            SpcfCalendar currentDate = PSPDate.getPSPTime();

            SpcfCalendar pastDate = PSPDate.getPSPTime();
            pastDate.addDays(-45);

            //Validate date
            validationResult.merge(mRefundDTO.validateDate(currentDate, pastDate));
        }


        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        SpcfMoney mFinancialTxAmt = mRefundDTO.getFinancialTxAmt();
        SpcfCalendar settlementDate = DateDTO.convertToSpcfCalendar(mRefundDTO.getTxDate());

        BankAccountOwnerType creditBankAccountOwnerType = BankAccountOwnerType.Company;
        BankAccountOwnerType dreditBankAccountOwnerType = BankAccountOwnerType.Intuit;

        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {

            mFinancialTxAmt = LedgerAccount.getLedgerAccountBalanceByPayroll(
                    LedgerAccountCode.EEReturnLiablility, mPayrollRun.getSourcePayRunId(), mCompany);

            settlementDate = FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup());
        } else {
            creditBankAccountOwnerType = null;
        }

        com.intuit.sbd.payroll.psp.domain.SettlementType domainSettlementType = DDProcessesToDTO.
                getDomainSettlementType(mRefundDTO.getSettlementType());

        // Add the financial transaction to the database
        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(
                mCompany, mPayrollRun, null, mBankAccount,
                mDebitIntuitBankAccount.getBankAccount(), creditBankAccountOwnerType, dreditBankAccountOwnerType,
                TransactionTypeCode.EmployerDdRejectRefundCredit, mFinancialTxAmt, domainSettlementType,
                settlementDate);

        /**
         * If the transaction is ACH, create a new financial transaction response
         */
        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {
            Collection<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
            financialTransactions.add(financialTransaction);

            mTransactionResponse = TransactionResponse
                    .createTransactionResponse(mCompany, financialTransactions, null);
        } else {
            // Otherwise update transaction to executed, then completed
            mFinancialTransaction = financialTransaction.updateFinancialTransactionState(
                    TransactionStateCode.Executed);

            mFinancialTransaction = financialTransaction.updateFinancialTransactionState(
                    TransactionStateCode.Completed);
        }

        //Resolve any unresolved transactions
        DomainEntitySet<FinancialTransaction> ddDebitTxns = mPayrollRun.
                getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});

        for (FinancialTransaction finalcialTransaciton : ddDebitTxns) {
            finalcialTransaciton.resolveTransactionReturns();
        }

        return processResult;
    }
}