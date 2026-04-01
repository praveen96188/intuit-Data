/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddEmployerReturnRefundTransaction.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: rkrishna
 * Date: Dec 17, 2007
 * Time: 11:31:11 AM
 */
public class AddEmployerReturnRefundTransaction extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private RefundDTO mRefundDTO;    //if ACH, amounts and settlement date are ignored

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private CompanyBankAccount companyBankAccount;
    private IntuitBankAccount mDebitIntuitBankAccount;
    private TransactionResponse mTransactionResponse;
    private FinancialTransaction mFinancialTransaction;
    private SpcfDecimal mAchRefundableAmount;
    private SpcfDecimal mAchTaxRefundableAmount;

    private static SpcfMoney ZERO = new SpcfMoney("0.00");



    public AddEmployerReturnRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
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
                    mRefundDTO.getSourcePayrollRunId(), mRefundDTO.getSourcePayrollRunId(), mSourceSystemCode.toString(),
                    mSourceCompanyId);

            return validationResult;
        }

        TransactionType transactionType = TransactionType.
                findTransactionType(TransactionTypeCode.EmployerDdReturnedRefundCredit);

        mDebitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {
            //Find active company bank account
            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

            if (companyBankAccount == null) {
                validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                        mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);

                return validationResult;
            }


            if (! CompanyService.isCompanyOnDirectDepositOrTaxService(mCompany)) {
                validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                        mSourceCompanyId, mSourceSystemCode.toString(), 
                        mSourceCompanyId, ServiceCode.DirectDeposit.toString() + " or " + ServiceCode.Tax.toString());
            }

            /**
             * Verify that this is a valid ACTION for this payroll run given the
             * state of the ledger for the payroll run (see Rep UI Allowable
             * Actions.xls)
             * If not throw the exception. Action{0} not valid for payroll run
             * with DDTxBatchID {1} due to current ledger account balances.
             */
            if (!mPayrollRun.validateAction(ActionEventCode.ERReturnRefund)) {
                validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, mPayrollRun.getSourcePayRunId(), ActionEventCode.ERReturnRefund.toString(), mPayrollRun.getSourcePayRunId());
                return validationResult;
            }

            // make sure there is some amount left to be refunded
            mAchRefundableAmount = LedgerAccount.getLedgerAccountBalanceByPayrollTaxSeparate(LedgerAccountCode.ERReturnReceivable, mPayrollRun.getSourcePayRunId(), mCompany, null, false, true);
            mAchTaxRefundableAmount = LedgerAccount.getLedgerAccountBalanceByPayrollTaxSeparate(LedgerAccountCode.ERReturnReceivable, mPayrollRun.getSourcePayRunId(), mCompany, null, true, true);

            if (mAchRefundableAmount.isLessThanEqualTo(ZERO) && mAchTaxRefundableAmount.isLessThanEqualTo(ZERO)) {
                validationResult.getMessages().CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction, mPayrollRun.getSourcePayRunId());
                return validationResult;
            }

        } else {
            //validation amount
            ProcessResult validateDDRefundDTOtResult = mRefundDTO.validateDDRefundDTO();
            validationResult.merge(validateDDRefundDTOtResult);

            SpcfCalendar currentDate = PSPDate.getPSPTime();

            SpcfCalendar pastDate = PSPDate.getPSPTime();
            pastDate.addDays(-45);

            // Validate the date provided
            validationResult.merge(mRefundDTO.validateDate(currentDate, pastDate));
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        SpcfDecimal ftAmount = mRefundDTO.isRefundTaxOnly() ? SpcfMoney.ZERO : mRefundDTO.getFinancialTxAmt();
        SpcfDecimal taxFtAmount = mRefundDTO.isRefundTaxOnly() ? mRefundDTO.getFinancialTxAmt() : SpcfMoney.ZERO;
        SpcfCalendar settlementDate = DateDTO.convertToSpcfCalendar(mRefundDTO.getTxDate());
        BankAccount bankAccount = null;

        BankAccountOwnerType creditBankAccountOwnerType = BankAccountOwnerType.Company;
        BankAccountOwnerType dreditBankAccountOwnerType = BankAccountOwnerType.Intuit;

        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {

            ftAmount = mAchRefundableAmount;
            taxFtAmount = mAchTaxRefundableAmount;

            settlementDate = FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup());

            bankAccount = companyBankAccount.getBankAccount();

        } else {
            creditBankAccountOwnerType = null;
        }

        com.intuit.sbd.payroll.psp.domain.SettlementType domainSettlementType = DDProcessesToDTO.
                getDomainSettlementType(mRefundDTO.getSettlementType());

        DomainEntitySet<FinancialTransaction> newFinancialTransactions = new DomainEntitySet<FinancialTransaction>();

        if (ftAmount.isGreaterThan(ZERO)) {
            newFinancialTransactions.add(FinancialTransaction.createFinancialTransaction(
                    mCompany, mPayrollRun, null, bankAccount,
                    mDebitIntuitBankAccount.getBankAccount(), creditBankAccountOwnerType, dreditBankAccountOwnerType,
                    TransactionTypeCode.EmployerDdReturnedRefundCredit, new SpcfMoney(ftAmount), domainSettlementType,
                    settlementDate));
        }

        if (taxFtAmount.isGreaterThan(ZERO)) {
            newFinancialTransactions.add(FinancialTransaction.createFinancialTransaction(
                    mCompany, mPayrollRun, null, bankAccount,
                    mDebitIntuitBankAccount.getBankAccount(), creditBankAccountOwnerType, dreditBankAccountOwnerType,
                    TransactionTypeCode.EmployerTaxReturnedRefundCredit, new SpcfMoney(taxFtAmount), domainSettlementType,
                    settlementDate));
        }

        // If the transactions are ACH, create a new financial transaction response
        if (mRefundDTO.getSettlementType() == SettlementTypeDTO.ACH) {
            mTransactionResponse = TransactionResponse.createTransactionResponse(mCompany, newFinancialTransactions, null);
        } else {
            for (FinancialTransaction newFinancialTransaction : newFinancialTransactions) {
                // Otherwise update transaction to executed, then completed
                mFinancialTransaction = newFinancialTransaction.updateFinancialTransactionState(
                        TransactionStateCode.Executed);

                mFinancialTransaction = newFinancialTransaction.updateFinancialTransactionState(
                        TransactionStateCode.Completed);
            }
        }


        return processResult;
    }
}
