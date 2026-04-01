/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddRecoverBadDebtTransaction.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.BadDebtRecoverDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Map;

/**
 *
 * User: rkrishna
 * Date: Jan 4, 2008
 * Time: 9:14:03 AM

 */
public class AddRecoverBadDebtTransaction extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private BadDebtRecoverDTO mBadDebtRecoverDTO;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private FinancialTransaction mOrigFT;
    private TransactionTypeCode mRecoveryType;

    private FinancialTransaction mRecoveryFT;

    public AddRecoverBadDebtTransaction(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                        BadDebtRecoverDTO pBadDebtRecoverDTO) {
        this.mSourceSystemCode = pSourceSystemCode;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mBadDebtRecoverDTO = pBadDebtRecoverDTO;
    }

    public FinancialTransaction getFinancialTransaction() {
        return mRecoveryFT;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mBadDebtRecoverDTO.getSourcePayrollRunId() == null) {
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
                mBadDebtRecoverDTO.getSourcePayrollRunId());

        if (mPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                    mBadDebtRecoverDTO.getSourcePayrollRunId(), mBadDebtRecoverDTO.getSourcePayrollRunId(),
                    mSourceSystemCode.toString(), mSourceCompanyId);

            return validationResult;
        }

        /**
         * Verify Settlement type is valid and NOT ACH (schema)
         * Schema only validates that it's a valid txn settlement value. ACH is
         * one of them So we rule out ACH manually
         */
        if (mBadDebtRecoverDTO.isCustomer()) {
            if (!SettlementTypeDTO.ACH.equals(mBadDebtRecoverDTO.getSettlementType())) {
                validationResult.getMessages()
                        .RecoverBadDebtInvalidTxnSettlementType(EntityName.SettlementType, mSourceCompanyId,
                                mBadDebtRecoverDTO.getSettlementType().toString());
            }
        } else {
            if (SettlementTypeDTO.ACH.equals(mBadDebtRecoverDTO.getSettlementType())) {
                validationResult.getMessages()
                        .RecoverBadDebtInvalidTxnSettlementType(EntityName.SettlementType, mSourceCompanyId,
                                mBadDebtRecoverDTO.getSettlementType().toString());
            }
        }

        //validation amount
        ProcessResult validateDTOResult = mBadDebtRecoverDTO.validateBadDebtRecoverDTO();
        if (!validateDTOResult.isSuccess()) {
            validationResult.merge(validateDTOResult);
            return validationResult;
        }

        // make sure the original FT exists
        mOrigFT = Application.findById(FinancialTransaction.class,
                                           SpcfUniqueId.createInstance(mBadDebtRecoverDTO.getOriginalTransactionId()));
        if (mOrigFT == null) {
            validationResult.getMessages().NoEntityWithGivenId("FinancialTransaction",
                                                               mBadDebtRecoverDTO.getOriginalTransactionId());
            return validationResult;
        }

        // make sure the original FT is one of the expected types
        switch (mOrigFT.getTransactionType().getTransactionTypeCd()) {
            case EmployerDdDebit:
                if (mBadDebtRecoverDTO.isCustomer()) {
                    mRecoveryType = TransactionTypeCode.BadDebtCustomerRecovery;
                } else {
                    mRecoveryType = TransactionTypeCode.BadDebtRecovery;
                }
                break;
            case EmployerTaxDebit:
                if (mBadDebtRecoverDTO.isCustomer()) {
                    mRecoveryType = TransactionTypeCode.BadDebtCustomerRecoveryTax;
                } else {
                    mRecoveryType = TransactionTypeCode.BadDebtRecoveryTax;
                }
                break;
            case EmployerFeeDebit:
                if (mBadDebtRecoverDTO.isCustomer()) {
                    mRecoveryType = TransactionTypeCode.BadDebtCustomerRecoveryFee;
                } else {
                    mRecoveryType = TransactionTypeCode.BadDebtRecoveryFee;
                }
                break;

            case ServiceSalesAndUseTax:
                if (mBadDebtRecoverDTO.isCustomer()) {
                    mRecoveryType = TransactionTypeCode.BadDebtCustomerRecoverySalesAndUseTax;
                } else {
                    mRecoveryType = TransactionTypeCode.BadDebtRecoverySalesAndUseTax;
                }
                break;

            default:
                validationResult.getMessages().InvalidArgument(EntityName.FinancialTransaction,
                                                               mBadDebtRecoverDTO.getOriginalTransactionId(),
                                                               "TransactionTypeCode");
                return validationResult;
        }

        // this (recovery) action is only valid if something (anything) has been written off
        if (! hasRelatedWriteoff(mOrigFT)) {
            validationResult.getMessages().ActionNotValidForPayrollRun(EntityName.PayrollRun,
                                                                       mPayrollRun.getSourcePayRunId(),
                                                                       ActionEventCode.BadDebtRecover.toString(),
                                                                       mPayrollRun.getSourcePayRunId(),
                                                                       mPayrollRun.getPayrollRunStatus().toString());
            return validationResult;
        }


        SpcfDecimal totalPendingRecovery = mBadDebtRecoverDTO.getFinancialTxAmt();
        SpcfDecimal totalUnrecovered;

        //validate this + pending customer recovery ACH not more than total unrecovered
        switch (mRecoveryType) {
            case BadDebtRecovery:
            case BadDebtCustomerRecovery:
                totalPendingRecovery = totalPendingRecovery.add(getPendingCustomerRecoveryAmount(TransactionTypeCode.BadDebtCustomerRecovery));
                totalUnrecovered = getTransactionSum(mPayrollRun.getUnrecoveredDirectDepositAmount());
                break;
            case BadDebtRecoveryTax:
            case BadDebtCustomerRecoveryTax:
                totalPendingRecovery = totalPendingRecovery.add(getPendingCustomerRecoveryAmount(TransactionTypeCode.BadDebtCustomerRecoveryTax));
                totalUnrecovered = getTransactionSum(mPayrollRun.getUnrecoveredTaxAmount());
                break;
            case BadDebtCustomerRecoveryFee:
            case BadDebtRecoveryFee:
                totalPendingRecovery = totalPendingRecovery.add(getPendingCustomerRecoveryAmount(TransactionTypeCode.BadDebtCustomerRecoveryFee));
                totalUnrecovered = getTransactionSum(mPayrollRun.getUnrecoveredFeeAmounts());
                break;
            case BadDebtCustomerRecoverySalesAndUseTax:
            case BadDebtRecoverySalesAndUseTax:
                totalPendingRecovery = totalPendingRecovery.add(getPendingCustomerRecoveryAmount(TransactionTypeCode.BadDebtCustomerRecoverySalesAndUseTax));
                totalUnrecovered = getTransactionSum(mPayrollRun.getUnrecoveredSalesTaxAmounts());
                break;
            default:
                throw new RuntimeException("Unexpected recovery type " + mRecoveryType.toString());
        }

        if (totalPendingRecovery.isGreaterThan(totalUnrecovered)) {
            validationResult.getMessages()
                    .BadDebtRecoveryAmountTooLarge(EntityName.FinancialTransaction,
                            mPayrollRun.getSourcePayRunId());
        }


        //Validate date
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        SpcfCalendar pastDate = PSPDate.getPSPTime();
        pastDate.addDays(-45);
        validationResult.merge(mBadDebtRecoverDTO.validateDate(currentDate, pastDate));

        return validationResult;
    }

    private SpcfDecimal getPendingCustomerRecoveryAmount(TransactionTypeCode transactionTypeCode) {
        SpcfDecimal pendingCustomerRecovery = SpcfMoney.ZERO;
        for (FinancialTransaction ft : mPayrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{transactionTypeCode},
                new TransactionStateCode[]{TransactionStateCode.Created})) {
            pendingCustomerRecovery = pendingCustomerRecovery.add(ft.getFinancialTransactionAmount());
        }
        return pendingCustomerRecovery;
    }

    private SpcfDecimal getTransactionSum(Map<FinancialTransaction, SpcfMoney> transactions) {
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (SpcfMoney money : transactions.values()) {
            sum = sum.add(money);
        }
        return sum;
    }

    public ProcessResult<FinancialTransaction> process() {
        ProcessResult<FinancialTransaction> processResult = new ProcessResult<FinancialTransaction>();

        SettlementType settlementType = DDProcessesToDTO.getDomainSettlementType(mBadDebtRecoverDTO.getSettlementType());

        SpcfCalendar settlementDate = DateDTO.convertToSpcfCalendar(mBadDebtRecoverDTO.getTxDate());

        BankAccount creditBankAccount=null;
        BankAccount debitBankAccount=null;
        BankAccountOwnerType accountOwnerType=null;
        SpcfCalendar actualSettlementDate = settlementDate;

        if (mBadDebtRecoverDTO.isCustomer()) {
            IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(mRecoveryType, CreditDebitCode.Credit);
            IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(mRecoveryType, CreditDebitCode.Debit);

            creditBankAccount = creditIntuitBankAccount.getBankAccount();
            debitBankAccount = debitIntuitBankAccount.getBankAccount();

            accountOwnerType = BankAccountOwnerType.Intuit;

            actualSettlementDate = FinancialTransaction.getSettlementDate(mRecoveryType, mCompany.getOffloadGroup());
        }

        // Add the financial transaction to the database
        mRecoveryFT = FinancialTransaction.createFinancialTransaction(mCompany, mPayrollRun, null,
                                                                     creditBankAccount, debitBankAccount,
                                                                     accountOwnerType, accountOwnerType,
                                                                     mRecoveryType,
                                                                     mBadDebtRecoverDTO.getFinancialTxAmt(),
                                                                     settlementType,
                                                                     actualSettlementDate,
                                                                     mOrigFT.getSku(), mOrigFT, 1);

        if (! mBadDebtRecoverDTO.isCustomer()) {
            // Update to executed(“EX”) and make ledger entries
            mRecoveryFT = mRecoveryFT.updateFinancialTransactionState(TransactionStateCode.Executed);

            // Update to complete(“CP”) and make ledger entries
            mRecoveryFT = mRecoveryFT.updateFinancialTransactionState(TransactionStateCode.Completed);
        }

        processResult.setResult(mRecoveryFT);

        return processResult;
    }

    /**
     * Looks at all FTs related -- directly or indirectly -- to the input FT through the OriginalTransaction association.
     * Returns true if any related FT is a writeoff (has the Writeoff TransactionTypeGroupCode), or false otherwise.
     * @param pFT
     * @return
     */
    static private boolean hasRelatedWriteoff(FinancialTransaction pFT) {
        DomainEntitySet<FinancialTransaction> related = new DomainEntitySet<FinancialTransaction>();
        pFT.getRelatedTransactions(related);
        while (related.size() > 0) {
            FinancialTransaction ft = related.iterator().next();
            related.remove(ft);
            if (ft.getTransactionType().getTransactionTypeGroupCd() == TransactionTypeGroupCode.Writeoff) {
                return true;
            }
            else {
                ft.getRelatedTransactions(related);
            }
        }

        // no writeoffs anywhere in the family
        return false;
    }
}
