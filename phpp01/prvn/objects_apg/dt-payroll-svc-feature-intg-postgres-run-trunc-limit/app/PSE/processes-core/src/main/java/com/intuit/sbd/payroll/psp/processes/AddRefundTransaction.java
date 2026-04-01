/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddRefundTransaction.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
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
import java.math.BigDecimal;

/**
 * User: rkrishna
 * Date: Dec 10, 2007
 * Time: 4:27:55 PM
 */
public class AddRefundTransaction extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private RefundDTO mRefundDTO;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private BankAccount mBankAccount = null;
    private DDCompanyServiceInfo mDomainDDCompanyServiceInfo;
    private FinancialTransaction mFinancialTransaction;
    private TransactionResponse mTransactionResponse;


    public AddRefundTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, RefundDTO pRefundDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mRefundDTO = pRefundDTO;
    }

    public FinancialTransaction getFinancialTransaction() {
        return mFinancialTransaction;
    }

    public TransactionResponse getTransactionResponse() {
        return mTransactionResponse;
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

        //Check if company exists
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
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, mRefundDTO.getSourcePayrollRunId(),
                    mRefundDTO.getSourcePayrollRunId(), mSourceSystemCode.toString(), mSourceCompanyId);

            return validationResult;
        }

        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {
            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

            if (companyBankAccount == null) {
                validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                        mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);
                return validationResult;
            } else {
                mBankAccount = companyBankAccount.getBankAccount();
            }

            // Verify the company is associated with the DD Service
            mDomainDDCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                    .findCompanyService(mCompany, ServiceCode.DirectDeposit);

            if (mDomainDDCompanyServiceInfo == null) {
                validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                        mSourceCompanyId, mSourceSystemCode.toString(),
                        mSourceCompanyId, ServiceCode.DirectDeposit.toString());
            }

            // Throw an exception if all the EE CR transactions for this payroll run are in pending state
            DomainEntitySet<FinancialTransaction> allEETransactions = mPayrollRun.
                    getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                            null);

            DomainEntitySet<FinancialTransaction> createdEETransactions = mPayrollRun.
                    getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                            new TransactionStateCode[]{TransactionStateCode.Created});

            //If all the EmployeeDdCredit transactions are in Created state throw an exception.
            if (allEETransactions.size() == createdEETransactions.size()) {
                validationResult.getMessages().CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction,
                        mPayrollRun.getSourcePayRunId());
            }
            // Throw an exception if there are any pending paycheck transactions
            // for this payroll run

            DomainEntitySet<FinancialTransaction> transactions = mPayrollRun.
                    getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.Intuit5DayReturnTransfer,
                                                      TransactionTypeCode.Intuit5DayFeeReturnTransfer,
                                                      TransactionTypeCode.Intuit5DaySalesTaxReturnTransfer},
                            new TransactionStateCode[]{TransactionStateCode.Created});

            if (transactions != null && transactions.size() > 0) {
                validationResult.getMessages().CreateTransactionFailurePendingLedgerActivity(EntityName.FinancialTransaction,
                        mPayrollRun.getSourcePayRunId());
            }

            // Throw an exception if there are any pending refund transactions
            DomainEntitySet<FinancialTransaction> pendingRefundTransactions = mPayrollRun.
                    getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                            new TransactionStateCode[]{TransactionStateCode.Created});

            if (pendingRefundTransactions != null && pendingRefundTransactions.size() > 0) {
                validationResult.getMessages().PendingTransactionAlreadyExists(EntityName.FinancialTransaction,
                        mPayrollRun.getSourcePayRunId());
            }

            /**
             * Verify that this is a valid ACTION for this payroll run given the
             * state of the ledger for the payroll run (see Rep UI Allowable
             * Actions.xls)
             * If not throw the exception. Action{0} not valid for payroll run
             * with DDTxBatchID {1} due to current ledger account balances.
             */
            if (!mPayrollRun.validateAction(ActionEventCode.DDRefund)) {
                validationResult.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun, mPayrollRun.getSourcePayRunId(), ActionEventCode.DDRefund.toString(), mPayrollRun.getSourcePayRunId());
                return validationResult;
            }
        } else {
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

        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {

            BigDecimal cancelTxnAmt = new BigDecimal(0);
            BigDecimal refundTxnAmt = new BigDecimal(0);
            
            DomainEntitySet<FinancialTransaction> eECancelTxnList =
                    mPayrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                            new TransactionStateCode[]{TransactionStateCode.Cancelled});

            if (eECancelTxnList != null) {
                for (FinancialTransaction txn : eECancelTxnList) {
                    cancelTxnAmt = cancelTxnAmt.add(SpcfUtils.convertToBigDecimal(txn.getFinancialTransactionAmount()));
                }
            }

            DomainEntitySet<FinancialTransaction> eRDDRefundTxnList =
                    FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(mPayrollRun,
                            TransactionTypeCode.EmployerDdRefundCredit, TransactionStateCode.Cancelled);

            if (eRDDRefundTxnList != null) {
                for (FinancialTransaction txn : eRDDRefundTxnList) {
                    refundTxnAmt = refundTxnAmt.add(SpcfUtils.convertToBigDecimal(txn.getFinancialTransactionAmount()));
                }
            }

            // Calculate refund transaction amount
            mFinancialTxAmt = SpcfUtils.convertToSpcfMoney(cancelTxnAmt.subtract(refundTxnAmt));

            DomainEntitySet<FinancialTransaction> transactions = mPayrollRun.
                    getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                            null);

            settlementDate = transactions.get(0).getRefundTransactionSettlementDate();
        }

        TransactionType transactionType = TransactionType.
                findTransactionType(TransactionTypeCode.EmployerDdRefundCredit);

        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        com.intuit.sbd.payroll.psp.domain.SettlementType domainSettlementType = DDProcessesToDTO.
                getDomainSettlementType(mRefundDTO.getSettlementType());

        // Add the financial transaction to the database
        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(
                mCompany, mPayrollRun, null, mBankAccount,
                debitIntuitBankAccount.getBankAccount(), BankAccountOwnerType.Company, BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerDdRefundCredit, mFinancialTxAmt, domainSettlementType, settlementDate);

        // If the transaction is ACH, create a new financial transaction
        // response
        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {

            Collection<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
            financialTransactions.add(financialTransaction);

            mTransactionResponse = TransactionResponse.createTransactionResponse(mCompany,
                    financialTransactions, null);
        } else {
            // Otherwise update transaction to executed, then completed

            mFinancialTransaction = financialTransaction.updateFinancialTransactionState(
                    TransactionStateCode.Executed);

            mFinancialTransaction = financialTransaction.updateFinancialTransactionState(
                    TransactionStateCode.Completed);
        }

        return processResult;
    }
}
