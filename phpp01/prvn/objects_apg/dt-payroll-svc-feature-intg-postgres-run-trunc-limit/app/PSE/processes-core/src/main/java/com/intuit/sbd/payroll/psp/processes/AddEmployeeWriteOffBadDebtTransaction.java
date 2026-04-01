/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddEmployeeWriteOffBadDebtTransaction.java#1 $
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
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: Dawn Martens
 * Date: August 20, 2009
 * Time: 10:38:55 AM
 */
public class AddEmployeeWriteOffBadDebtTransaction extends Process implements IProcess {

    private static final SpcfDecimal ZERO = SpcfDecimal.createInstance(0);

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private String mSourcePayrollRunId;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private DomainEntitySet<TransactionReturn> transactionReturns;

    public AddEmployeeWriteOffBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                 String pSourcePayrollRunId) {
        this.mSourceSystemCode = pSourceSystemCode;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourcePayrollRunId = pSourcePayrollRunId;

        transactionReturns = new DomainEntitySet<TransactionReturn>();
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
        if (!mPayrollRun.validateAction(ActionEventCode.BadDebtWriteOffEEReturn)) {
            validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, mPayrollRun.getSourcePayRunId(), ActionEventCode.BadDebtWriteOffEEReturn.toString(), mPayrollRun.getSourcePayRunId());
            return validationResult;
        }

        /**
         * Verify there are no pending transactions of this type
         * If such transactions exist, throw the exception
         * Transaction cannot be created due to pending activity against this ledger account.
         */
        DomainEntitySet<FinancialTransaction> pendingReversalWriteOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(mCompany)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeReversalFailedWriteOff))
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))
                        .And(FinancialTransaction.PayrollRun().equalTo(mPayrollRun)));

        if (!pendingReversalWriteOffs.isEmpty()) {
            validationResult.getMessages()
                    .CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction,
                            mPayrollRun.getSourcePayRunId());
        }


        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        createWriteOff();

        // update PayrollRun status
        mPayrollRun.updatePayrollRunStatus(PayrollStatus.WrittenOff);

        // "Resolve" any unresolved TransactionReturns associated with this payroll run and a reversal
        TransactionTypeCode transactionTypeCode = TransactionTypeCode.EmployeeDdReversalDebit;
        transactionReturns = TransactionReturn.resolveTransactionReturns(mCompany, mSourcePayrollRunId, transactionTypeCode);

        return processResult;
    }

    private void createWriteOff() {
        SpcfMoney amountToWriteOff = new SpcfMoney(LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.EEReturnLiablility,
                mPayrollRun.getSourcePayRunId(), mCompany).abs());

        // compute the settlement date
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployeeReversalFailedWriteOff, mCompany.getOffloadGroup());

        // get the right bank accounts
        TransactionType ftType = TransactionType.findTransactionType(TransactionTypeCode.EmployeeReversalFailedWriteOff);
        IntuitBankAccount ibaCredit = IntuitBankAccount.findIntuitBankAccount(ftType, CreditDebitCode.Credit);
        IntuitBankAccount ibaDebit = IntuitBankAccount.findIntuitBankAccount(ftType, CreditDebitCode.Debit);

        // create the FT
        FinancialTransaction.createFinancialTransaction(mCompany,
                mPayrollRun,
                null,
                ibaCredit.getBankAccount(),
                ibaDebit.getBankAccount(),
                BankAccountOwnerType.Intuit,
                BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployeeReversalFailedWriteOff,
                amountToWriteOff,
                SettlementType.ACH,
                settlementDate,
                null,   //no original transaction
                null,   //no sku
                0);     //no sku quantity
    }
}