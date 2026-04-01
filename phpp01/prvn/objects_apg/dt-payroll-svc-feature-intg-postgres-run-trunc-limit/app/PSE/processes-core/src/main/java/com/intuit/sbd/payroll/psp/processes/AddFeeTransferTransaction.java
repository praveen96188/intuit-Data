package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.dtos.FeeTransferDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

/**
 * User: rkrishna
 * Date: Jan 9, 2008
 * Time: 2:49:05 PM
 */
public class AddFeeTransferTransaction extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private FeeTransferDTO mFeeTransferDTO;

    private Company mCompany;
    private PayrollRun mPayrollRun;

    private FinancialTransaction mFinancialTransaction;

    public AddFeeTransferTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                     FeeTransferDTO pFeeTransferDTO) {
        this.mSourceSystemCode = pSourceSystemCode;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mFeeTransferDTO = pFeeTransferDTO;
    }

    public FinancialTransaction getFinancialTransaction() {
        return mFinancialTransaction;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mFeeTransferDTO.getSourcePayrollRunId() == null) {
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
                mFeeTransferDTO.getSourcePayrollRunId());

        if (mPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                    mFeeTransferDTO.getSourcePayrollRunId(), mFeeTransferDTO.getSourcePayrollRunId(),
                    mSourceSystemCode.toString(), mSourceCompanyId);

            return validationResult;
        }

        /**
         * Verify that this is a valid ACTION for this payroll run given the
         * state of the ledger for the payroll run (see Rep UI Allowable
         * Actions.xls)
         * If not throw the exception. Action{0} not valid for payroll run
         * with DDTxBatchID {1} due to current ledger account balances.
         */
        if (!mPayrollRun.validateAction(ActionEventCode.FeeTransfer)) {
            validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, mPayrollRun.getSourcePayRunId(), ActionEventCode.FeeTransfer.toString(), mPayrollRun.getSourcePayRunId());
            return validationResult;
        }
        //validation amount
        ProcessResult validateDTOResult = mFeeTransferDTO.validateFeeTransferDTO();
        if (!validateDTOResult.isSuccess()) {
            validationResult.merge(validateDTOResult);
            return validationResult;
        }

        /**
         * Verify txn amount is a less than or equal to the credit balance of the ER Return Receivables ledger account
         * for this payroll run.
         * If not throw exception �Txn Amount for Fee Transfer must not exceed the credit balance of the ER Return
         * Receivable ledger account for this payroll.�
         */
        SpcfDecimal maxTransferAmount = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, mPayrollRun.getSourcePayRunId(), mCompany, null, true);
        if (maxTransferAmount.getSign() < 0){
            maxTransferAmount = maxTransferAmount.negate();
        }
        
        if (mFeeTransferDTO.getFinancialTxAmt().compareTo(maxTransferAmount) > 0) {
            validationResult.getMessages().FeeTransferExceedsLedgerBalance(EntityName.DDTransaction,
                    mFeeTransferDTO.getFinancialTxAmt().toString());
            return validationResult;
        }

        //validate DD service if reversal
        if (mFeeTransferDTO.getFeeTypeCode().equals(OfferingServiceChargeType.ReversalFee)) {
            if (!mCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
                validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                        mCompany.getSourceCompanyId(), mCompany.getSourceSystemCd().toString(),
                        mCompany.getSourceCompanyId(), ServiceCode.DirectDeposit.toString());
                return validationResult;
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        TransactionTypeCode transactionTypeCode = TransactionTypeCode.IntuitFeeTransfer;

        TransactionType transactionType = TransactionType.findTransactionType(transactionTypeCode);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType,
                CreditDebitCode.Credit);

        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType,
                CreditDebitCode.Debit);

        OfferingServiceChargeGroup group = OfferingServiceChargeGroup.findFirstOfferingServiceChargeGroup(mPayrollRun.getCompany(), mFeeTransferDTO.getFeeTypeCode());
        OfferingServiceCharge charge = group.selectTier(1);

        mFinancialTransaction = FinancialTransaction.createFinancialTransaction(mCompany, mPayrollRun, null,
                creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                transactionTypeCode, mFeeTransferDTO.getFinancialTxAmt(), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(transactionTypeCode,
                        mCompany.getOffloadGroup()),
                charge.getSKU(), // same SKU as orig txn
                null, // FinancialTransaction pParentTransaction
                1);

        return processResult;
    }
}
