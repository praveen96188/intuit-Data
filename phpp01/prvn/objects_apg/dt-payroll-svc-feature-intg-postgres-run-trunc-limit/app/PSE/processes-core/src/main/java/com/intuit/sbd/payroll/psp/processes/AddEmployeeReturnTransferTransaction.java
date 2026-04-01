/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddEmployeeReturnTransferTransaction.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */


package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: rkrishna
 * Date: Dec 3, 2007
 * Time: 1:50:03 PM
 */
public class AddEmployeeReturnTransferTransaction extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private String mSourcePayrollRunId;

    private Company mCompany;
    private PayrollRun mPayrollRun;

    private DomainEntitySet<TransactionReturn> transactionReturns;

    public AddEmployeeReturnTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                    String pSourcePayrollRunId) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mSourcePayrollRunId = pSourcePayrollRunId;

        transactionReturns = new DomainEntitySet<TransactionReturn>();
    }

    /**
     * Returns any transaction responses obtained after a recall
     *
     * @return Collection of responses after a recall attempt
     */
    public DomainEntitySet<TransactionReturn> getTransactionReturns() {
        return transactionReturns;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mSourcePayrollRunId == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.PayrollRun, mSourceCompanyId, "PayrollRunId");
            return validationResult;
        }

        //Check if company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        //Check if payroll run exists
        mPayrollRun = PayrollRun.findPayrollRun(mCompany, mSourcePayrollRunId);

        if (mPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, mSourcePayrollRunId,
                    mSourcePayrollRunId, mSourceSystemCode.toString(), mSourceCompanyId);

            return validationResult;
        }

        /**
         * Verify that this is a valid ACTION for this payroll run given the
         * state of the ledger for the payroll run (see Rep UI Allowable
         * Actions.xls)
         * If not throw the exception. Action{0} not valid for payroll run
         * with DDTxBatchID {1} due to current ledger account balances.
         */
        if (!mPayrollRun.validateAction(ActionEventCode.EEReturnTransfer)) {
            validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, mPayrollRun.getSourcePayRunId(), ActionEventCode.EEReturnTransfer.toString(), mPayrollRun.getSourcePayRunId());
            return validationResult;
        }
        /**
         * Verify there are no pending refund transactions against EE Return Liability for this payroll run.
         * That is Financial Txs associated with this payroll, where:
         * Tx Type = ERDDRJRFCR or ERDDRVRFCR
         * Tx State = CR
         * If such transactions exist, throw the exception
         * Transaction cannot be created due to pending activity against this ledger account.
         */
        DomainEntitySet<FinancialTransaction> rejectTransactions = mPayrollRun.
                getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> reversalRefundTransactions = mPayrollRun.
                getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdReversalRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        if ((rejectTransactions != null && rejectTransactions.size() > 0) ||
                reversalRefundTransactions != null && reversalRefundTransactions.size() > 0) {
            validationResult.getMessages()
                    .CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction,
                            mPayrollRun.getSourcePayRunId());
        }

        /**
         * Verify there is no existing financial transaction associated with
         * this company and this payroll where
         * Tx Type is INTEERTXFR
         * Current Tx Status is “CR”
         * If such a financial transaction exists throw the exception
         * “A pending transaction of this type already exists. If you wish to
         * create a new one, the existing transaction must be canceled, first”
         */

        DomainEntitySet<FinancialTransaction> intuitEEReturnTransferTransactions = mPayrollRun.
                getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.IntuitEmployeeReturnTransfer},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        if (intuitEEReturnTransferTransactions != null && intuitEEReturnTransferTransactions.size() > 0) {
            validationResult.getMessages()
                    .PendingTransactionAlreadyExists(EntityName.FinancialTransaction,
                            mPayrollRun.getSourcePayRunId());
        }

        /**
         * Verify there is no existing financial transaction associated with this company and this payroll
         * where Tx Type is EEDDRVDB
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

        return validationResult;
    }

    public ProcessResult process() {

        ProcessResult processResult = new ProcessResult();

        TransactionType transactionType = TransactionType.
                findTransactionType(TransactionTypeCode.IntuitEmployeeReturnTransfer);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Credit);
        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        SpcfMoney financialTransactionAmount = LedgerAccount.getLedgerAccountBalanceByPayroll(
                LedgerAccountCode.EEReturnLiablility, mPayrollRun.getSourcePayRunId(), mCompany);

        FinancialTransaction.createFinancialTransaction(mCompany, mPayrollRun, null,
                creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.IntuitEmployeeReturnTransfer, financialTransactionAmount, SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.IntuitEmployeeReturnTransfer,
                        mCompany.getOffloadGroup()));

        /**
         * Find any unresolved txn returns associated with any EEDDCR
         * transactions in THIS PAYROLL and update the status of those txn
         * returns to “Resolved”
         */
        TransactionTypeCode transactionTypeCode = TransactionTypeCode.EmployeeDdCredit;
        transactionReturns = TransactionReturn.resolveTransactionReturns(mCompany, mSourcePayrollRunId, transactionTypeCode);

        return processResult;
    }
}
