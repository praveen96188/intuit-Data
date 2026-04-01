/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddWriteOffBadDebtTransaction.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: rkrishna
 * Date: Dec 28, 2007
 * Time: 10:38:55 AM
 */
public class AddWriteOffBadDebtTransaction extends Process implements IProcess {

    private static final SpcfDecimal ZERO = SpcfDecimal.createInstance(0);

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private String mSourcePayrollRunId;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private DomainEntitySet<TransactionReturn> transactionReturns;
    private ArrayList<CancelTransactionCore> cancelTransactionCoreProcesses;

    private SpcfDecimal mLedgerBalance;     // ERReturnReceivable balance -- limit of what can be written off
    private SpcfDecimal mTotalWrittenOff;   // sum of all writeoff FTs created by this process

    public AddWriteOffBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                         String pSourcePayrollRunId) {
        this.mSourceSystemCode = pSourceSystemCode;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourcePayrollRunId = pSourcePayrollRunId;

        transactionReturns = new DomainEntitySet<TransactionReturn>();
        cancelTransactionCoreProcesses = new ArrayList<CancelTransactionCore>();
    }

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

        // Verify that this is a valid ACTION for this payroll run given the
        // state of the ledger for the payroll run (see Rep UI Allowable
        // Actions.xls)
        // o If not throw the exception. Action{0} not valid for payroll run
        // with DDTxBatchID {1} due to current ledger account balances.
        if (!mPayrollRun.validateAction(ActionEventCode.BadDebtWriteOff, TransactionAssociationType.Redebit)) {
            validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, mPayrollRun.getSourcePayRunId(), ActionEventCode.BadDebtWriteOff.toString(), mPayrollRun.getSourcePayRunId());
            return validationResult;
        }

        /**
         * Verify there are no pending transactions against ER Return Receivable for this payroll run.
         * That is Financial Txs associated with this payroll, where:
         * Tx State = CR
         * CR Bank Account is INTUIT ER RETURNS bank account
         * OR
         * DB Bank Account is INTUIT ER RETURNS bank account
         * If such transactions exist, throw the exception
         * Transaction cannot be created due to pending activity against this ledger account.
         */

        DomainEntitySet<FinancialTransaction> payrollTransactions = mPayrollRun.
                getFinancialTransactions(null,
                        new TransactionStateCode[]{TransactionStateCode.Created});

        TransactionType transactionType = TransactionType.
                findTransactionType(TransactionTypeCode.IntuitEmployeeReturnTransfer);

        String intuitERReturnsCreditBankAccountId = IntuitBankAccount.findIntuitBankAccount(transactionType,
                CreditDebitCode.Credit).getBankAccount().getAccountNumber();

        String intuitERReturnsDebitBankAccountId = IntuitBankAccount.findIntuitBankAccount(transactionType,
                CreditDebitCode.Debit).getBankAccount().getAccountNumber();

        for (FinancialTransaction financialTransaction : payrollTransactions) {
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

            boolean intuitInitiatedReversal = false;
            if (PayrollStatus.PendingReversals.equals(mPayrollRun.getPayrollRunStatus()) && TransactionTypeCode.EmployeeDdReversalDebit.equals(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                intuitInitiatedReversal = true;
            }

            if ((intuitERReturnsCreditBankAccountId.equals(creditBankAccountId)
                    || intuitERReturnsDebitBankAccountId.equals(debitBankAccountId))
                    && !intuitInitiatedReversal
                    && !TransactionType.isRedebitTransactionType(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                validationResult.getMessages()
                        .CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction,
                                mPayrollRun.getSourcePayRunId());
            }
        }

        //If the payroll is in the PendingAutoRedebit or PendingRedebit state, create a new CancelTransactionCore proces for each redebit transaction in a Created state
        //The process will be "processed" in this class's process step
        if (PayrollStatus.PendingAutoRedebit.equals(mPayrollRun.getPayrollRunStatus()) || PayrollStatus.PendingRedebit.equals(mPayrollRun.getPayrollRunStatus())) {
            TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);
            DomainEntitySet<FinancialTransaction> redebitFinTxns = mPayrollRun.getFinancialTransactions(createdState, TransactionAssociationType.Redebit);
            for (FinancialTransaction currFinTxn : redebitFinTxns) {
                CancelTransactionCore currCancelTxCore = new CancelTransactionCore(mSourceSystemCode, mSourceCompanyId, currFinTxn.getId().toString(), true);
                validationResult.merge(currCancelTxCore.validate());
                cancelTransactionCoreProcesses.add(currCancelTxCore);
            }
        }

        //If the payroll is in the PendingReversals state, create a new CancelTransactionCore proces for each EmployeeDDReversal transaction in a Created state
        //The process will be "processed" in this class's process step
        if (PayrollStatus.PendingReversals.equals(mPayrollRun.getPayrollRunStatus())) {
            TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);
            TransactionType eeDDReversal = Application.findById(TransactionType.class, TransactionTypeCode.EmployeeDdReversalDebit);
            DomainEntitySet<FinancialTransaction> reversalFinTxns = mPayrollRun.getFinancialTransactions(createdState, eeDDReversal);
            for (FinancialTransaction currFinTxn : reversalFinTxns) {
                CancelTransactionCore currCancelTxCore = new CancelTransactionCore(mSourceSystemCode, mSourceCompanyId, currFinTxn.getId().toString(), true);
                validationResult.merge(currCancelTxCore.validate());
                cancelTransactionCoreProcesses.add(currCancelTxCore);
            }
        }


        //Cannot write off bad debt until pending payments are voided
        HashMap<FinancialTransaction, SpcfMoney> uncollectedTaxAmountsFTs = mPayrollRun.getUncollectedTaxAmount();
        SpcfDecimal uncollectedAmount = SpcfMoney.ZERO;
        if(uncollectedTaxAmountsFTs.size() > 1){
            throw new RuntimeException("More than one dd txn present");
        } else {
            for (SpcfMoney taxAmount : uncollectedTaxAmountsFTs.values()) {
                uncollectedAmount = taxAmount;
            }
        }

        SpcfDecimal taxCurrentCash = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.TaxCurrentCash, mSourcePayrollRunId, mCompany);
        SpcfDecimal erSUITaxDue = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERSUITaxDue, mSourcePayrollRunId, mCompany);
        if (taxCurrentCash.isLessThan(SpcfMoney.ZERO) && uncollectedAmount.isGreaterThan(SpcfMoney.ZERO) && !erSUITaxDue.equals(taxCurrentCash.negate())) {
            validationResult.getMessages().GenericError(EntityName.PayrollRun, mSourcePayrollRunId, "Pending tax payments must be voided and ER Payable must be applied before writing off bad debt.");
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        mLedgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                mPayrollRun.getSourcePayRunId(), mCompany);
        mLedgerBalance = mLedgerBalance.abs();

        // cancel any pending transactions that need cancelling
        for (CancelTransactionCore currCancelProc : cancelTransactionCoreProcesses) {
            processResult.merge(currCancelProc.process());
        }

        // get all FTs associated with the payroll
        ArrayList<FinancialTransaction> allPayrollFTs = new ArrayList(mPayrollRun.getFinancialTransactionCollection());

        // find the tax/fee/dd debits with uncollected amounts, creating writeoff FTs until the ledger balance is exhausted
        mTotalWrittenOff = ZERO;
        for (FinancialTransaction ft : allPayrollFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.ServiceSalesAndUseTax) {
                maybeCreateWriteoff(ft, TransactionTypeCode.EmployerWriteOffSalesAndUseTax);
            }
        }
        for (FinancialTransaction ft : allPayrollFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeDebit) {
                maybeCreateWriteoff(ft, TransactionTypeCode.EmployerWriteOffFee);
            }
        }
        for (FinancialTransaction ft : allPayrollFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdDebit) {
                maybeCreateWriteoff(ft, TransactionTypeCode.EmployerWriteOff);
            }
        }
        for (FinancialTransaction ft : allPayrollFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxDebit) {
                maybeCreateWriteoff(ft, TransactionTypeCode.EmployerWriteOffTax);
            }
        }

        // update PayrollRun status
        mPayrollRun.updatePayrollRunStatus(PayrollStatus.WrittenOff);

        // "Resolve" any unresolved TransactionReturns associated with this payroll run
        DomainEntitySet<TransactionReturn> txnReturns = TransactionReturn.findTransactionReturns(mSourcePayrollRunId, mCompany);
        if (txnReturns != null && txnReturns.size() > 0) {
            for (TransactionReturn tr : txnReturns) {
                if (tr.getReturnStatusCd() != TransactionReturnStatusCode.Resolved) {
                    transactionReturns.add(tr.updateTransactionReturnStatus(TransactionReturnStatusCode.Resolved));
                }
            }
        }

        return processResult;
    }

    private void maybeCreateWriteoff(FinancialTransaction pDebitFT, TransactionTypeCode pWriteoffType) {
        // if there's nothing left on the ledger, don't write off anything else
        if (mTotalWrittenOff.compareTo(mLedgerBalance) >= 0) {
            return;
        }

        // does this debit FT have any uncollected balance?
        TransactionSummary summary = pDebitFT.summarizeRelatedTransactions();
        if (summary.amtUncollected.compareTo(ZERO) > 0) {
            // limit the amount of this writeoff to the remaining (un-written-off) ledger balance
            SpcfDecimal writeoffAmount = summary.amtUncollected;
            SpcfDecimal maxAmount = mLedgerBalance.subtract(mTotalWrittenOff);
            if (writeoffAmount.compareTo(maxAmount) > 0) {
                writeoffAmount = maxAmount;
            }

            // create the writeoff...

            // compute the settlement date
            SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(pWriteoffType, mCompany.getOffloadGroup());

            // get the right bank accounts
            TransactionType ftType = TransactionType.findTransactionType(pWriteoffType);
            IntuitBankAccount ibaCredit = IntuitBankAccount.findIntuitBankAccount(ftType, CreditDebitCode.Credit);
            IntuitBankAccount ibaDebit = IntuitBankAccount.findIntuitBankAccount(ftType, CreditDebitCode.Debit);

            // if it's a full writeoff, the quantity is the original debit quantity, else it's 0
            int skuQuantity = (writeoffAmount.equals(pDebitFT.getFinancialTransactionAmount()) ? pDebitFT.getSkuQuantity() : 0);

            // create the FT
            FinancialTransaction.createFinancialTransaction(mCompany, mPayrollRun, null,
                                                           ibaCredit.getBankAccount(), ibaDebit.getBankAccount(),
                                                           BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                                                           pWriteoffType, new SpcfMoney(writeoffAmount), SettlementType.ACH,
                                                           settlementDate, pDebitFT.getSku(), pDebitFT, skuQuantity);

            mTotalWrittenOff = mTotalWrittenOff.add(writeoffAmount);
        }
    }
}
