package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: rkrishna
 * Date: Jan 10, 2008
 * Time: 10:54:27 AM
 */
public class AddIntuit5DayReturnTransferTransaction extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private String mDDTxBatchId;

    private Company mCompany;
    private PayrollRun mPayrollRun;

    private DomainEntitySet<FinancialTransaction> mFinancialTransactionList = new DomainEntitySet<FinancialTransaction>();

    public AddIntuit5DayReturnTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                  String pDDTxBatchId) {
        this.mSourceSystemCode = pSourceSystemCode;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mDDTxBatchId = pDDTxBatchId;
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactionList() {
        return mFinancialTransactionList;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mDDTxBatchId == null) {
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

        mPayrollRun = PayrollRun.findPayrollRun(mCompany, mDDTxBatchId);

        if (mPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                    mDDTxBatchId, mDDTxBatchId, mSourceSystemCode.toString(), mSourceCompanyId);

            return validationResult;
        }

        /**
         * Verify there are no pending paycheck transactions for this payroll run.
         * That is Financial Txs associated with this payroll, where:
         * Tx Type = EEDDCR
         * Tx State = CR
         * If such transactions exist, throw the exception
         * Transaction cannot be created due to pending activity against this ledger account.
         */

        DomainEntitySet<FinancialTransaction> pendingPaycheckTransactions = mPayrollRun.
                getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        if (!pendingPaycheckTransactions.isEmpty()) {
            validationResult.getMessages().CreateTransactionFailurePendingLedgerActivity(
                    EntityName.FinancialTransaction, mPayrollRun.getSourcePayRunId());
        }        

        /**
         *
         * Verify that this is a valid ACTION for this payroll run given the
         * state of the ledger for the payroll run (see Rep UI Allowable
         * Actions.xls)
         * If not throw the exception. Action{0} not valid for payroll run
         * with DDTxBatchID {1} due to current ledger account balances.
         */
        if (!mPayrollRun.validateAction(ActionEventCode.Intuit5DayReturnTransfer)) {
            validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, mPayrollRun.getSourcePayRunId(), ActionEventCode.Intuit5DayReturnTransfer.toString(), mPayrollRun.getSourcePayRunId());
            return validationResult;
        }

        /**
         * Verify there is no existing financial transaction associated with this company and this payroll where
         * Tx Type is INT5DRTXFR
         * Current Tx Status is “CR”
         * If such a financial transaction exists throw the exception
         * “A pending transaction of this type already exists.  If you wish to create a new one,
         * the existing transaction must be canceled, first”
         */
        DomainEntitySet<FinancialTransaction> pendingIntuit5DayTransfers = mPayrollRun.
                getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.Intuit5DayReturnTransfer,
                                                  TransactionTypeCode.Intuit5DayFeeReturnTransfer,
                                                  TransactionTypeCode.Intuit5DaySalesTaxReturnTransfer},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        if (pendingIntuit5DayTransfers != null && pendingIntuit5DayTransfers.size() > 0) {
            validationResult.getMessages().PendingTransactionAlreadyExists(EntityName.FinancialTransaction,
                    mPayrollRun.getSourcePayRunId());
        }

        return validationResult;
    }

    public ProcessResult process() {
        SpcfMoney zero = new SpcfMoney("0.00");
        SpcfMoney ledgerBalance;
        FinancialTransaction ft;

        // handle Fee transfer (if any)
        ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.FeeIncome,
                                                                       mPayrollRun.getSourcePayRunId(),
                                                                       mCompany);

        if (!ledgerBalance.equals(zero)) {
            ft = FinancialTransaction.createBookTransferTransaction(mCompany,
                                                                    mPayrollRun,
                                                                    TransactionTypeCode.Intuit5DayFeeReturnTransfer,
                                                                    ledgerBalance);
            mFinancialTransactionList.add(ft);
        }

        // handle Sales & Use Tax transfer (if any)
        ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.SalesAndUseTax,
                                                                       mPayrollRun.getSourcePayRunId(),
                                                                       mCompany);

        if (!ledgerBalance.equals(zero)) {
            ft = FinancialTransaction.createBookTransferTransaction(mCompany,
                                                                    mPayrollRun,
                                                                    TransactionTypeCode.Intuit5DaySalesTaxReturnTransfer,
                                                                    ledgerBalance);
            mFinancialTransactionList.add(ft);
        }

        // handle DD Debit transfer
        ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.DDCurrentLiability,
                                                                       mPayrollRun.getSourcePayRunId(),
                                                                       mCompany);

        ft = FinancialTransaction.createBookTransferTransaction(mCompany,
                                                                mPayrollRun,
                                                                TransactionTypeCode.Intuit5DayReturnTransfer,
                                                                ledgerBalance);
        mFinancialTransactionList.add(ft);

        return new ProcessResult();
    }

}