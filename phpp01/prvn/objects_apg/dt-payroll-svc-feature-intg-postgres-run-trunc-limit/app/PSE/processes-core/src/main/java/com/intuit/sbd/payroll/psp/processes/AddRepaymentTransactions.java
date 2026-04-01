/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddRepaymentTransactions.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.ActionEvent;
import com.intuit.sbd.payroll.psp.domain.ActionEventCode;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;

/**
 * User: rkrishna
 * Date: Dec 26, 2007
 * Time: 9:30:11 AM
 */
public class AddRepaymentTransactions extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private RedebitImpoundDTO mInputDTO;

    private FinancialTransaction mOrigFT;
    private TransactionTypeCode mRedebitTypeCd;
    private boolean mAmountCoversPayrollDebt = false;

    private FinancialTransaction mRedebitFT;

    private ArrayList<CancelTransactionCore> mCancelTransactionCoreProcesses = new ArrayList<CancelTransactionCore>();

    public AddRepaymentTransactions(SourceSystemCode pSrcSystemCd, String pSrcCompanyId, RedebitImpoundDTO pDTO) {
        mSourceSystemCode = pSrcSystemCd;
        mSourceCompanyId = pSrcCompanyId;
        mInputDTO = pDTO;
    }

    public FinancialTransaction getFinancialTransaction() {
        return mRedebitFT;
    }    

    public ProcessResult validate() {

        // Check if Company parameters are valid
        ProcessResult result = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId);
        if (! result.isSuccess()) {
            return result;
        }

        // DTO is required
        if (mInputDTO == null) {
            result.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, mSourceCompanyId, "RedebitImpoundDTO");
            return result;
        }
        
        // require a settlement type
        if (mInputDTO.getSettlementType() == null) {
            result.getMessages().RequiredInputMissingOrBlank(EntityName.SettlementType, mSourceCompanyId, "SettlementType");
            return result;
        }

        // can't be ACH
        if (mInputDTO.getSettlementType() == SettlementTypeDTO.ACH) {
            result.getMessages().InvalidValue(EntityName.SettlementType, mSourceCompanyId, "SettlementType");
            return result;
        }

        // the DTO does its own null checks
        result.merge( mInputDTO.validate() );
        if (! result.isSuccess()) {
            return result;
        }

        // require the original FT to exist
        mOrigFT = Application.findById(FinancialTransaction.class,
                                       SpcfUniqueId.createInstance(mInputDTO.getOriginalFinancialTxId()));
        if (mOrigFT == null) {
            result.getMessages().NoEntityWithGivenId("FinancialTransaction", mInputDTO.getOriginalFinancialTxId());
            return result;
        }

        // choose the redebit type based on the type of the original transaction
        switch(mOrigFT.getTransactionType().getTransactionTypeCd()) {
            case EmployerDdDebit:
            case EmployerDdRedebit:
                mRedebitTypeCd = TransactionTypeCode.EmployerDdRedebit;
                break;

            case EmployerTaxDebit:
            case EmployerTaxRedebit:
                mRedebitTypeCd = TransactionTypeCode.EmployerTaxRedebit;
                break;

            case EmployerFeeDebit:
            case EmployerFeeRedebit:
                mRedebitTypeCd = TransactionTypeCode.EmployerFeeRedebit;
                break;

            case ServiceSalesAndUseTax:
            case ServiceSalesAndUseTaxRedebit:
                mRedebitTypeCd = TransactionTypeCode.ServiceSalesAndUseTaxRedebit;
                break;

            default: 
                mRedebitTypeCd = TransactionTypeCode.EmployerDdRedebit;
                break;
        }

        // action validation depends on whether the original FT was a DD [re]debit or something else
        PayrollRun payrollRun = mOrigFT.getPayrollRun();
        // redebit record and add are valid for non-ach redebit transactions
        ActionEvent recordActionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.DDRedebitRecord);
        ActionEvent addActionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.DDRedebitAdd);
        if (!payrollRun.validateAction(recordActionEvent) && !payrollRun.validateAction(addActionEvent)) {
            result.getMessages().ActionNotValidForPayrollRun(EntityName.PayrollRun, payrollRun.getSourcePayRunId(),
                                                             recordActionEvent.getCode().toString() + " and " + addActionEvent.getCode().toString(),
                                                             payrollRun.getSourcePayRunId(),
                                                             payrollRun.getPayrollRunStatus().toString());
        }

        // settlement date must be between today and 45 days ago
        SpcfCalendar initDate = DateDTO.convertToSpcfCalendar(mInputDTO.getInitiationDate());
        SpcfCalendar today = PSPDate.getPSPTime();
        SpcfCalendar pastDate = PSPDate.getPSPTime();
        pastDate.addDays(-45);

        if (initDate.before(pastDate)) {
            result.getMessages().SettlementDateTooFarInPast(EntityName.Date, initDate.toString(), initDate.toString(),
                                                            mInputDTO.getSettlementType().toString());

        } else if (initDate.after(today)) {
            result.getMessages().SettlementDateTooFarInFuture(EntityName.Date, initDate.toString(), initDate.toString(),
                                                              mInputDTO.getSettlementType().toString());
        }

        // fail if any errors so far
        if (!result.isSuccess()) {
            return result;
        }

        return result;
    }

    public ProcessResult<FinancialTransaction> process() {
        ProcessResult result = new ProcessResult();
        // if there's an unresolved TransactionReturn for the original FT, and if this redebit, combined with other
        // redebits related to the return, will cover the amount of the returned ACH transaction, then we want to
        // resolve the return

        MoneyMovementTransaction mmt = mOrigFT.getMoneyMovementTransaction();
        boolean bNewAmountResolvesMMT = mmt.amountResolvesMMT(mInputDTO.getAmount());

        //If the amount resolves the MMT, resolve the original MMT txn return and the txn returns for all the redebits related to the txns related to the original MMT
        //If the amount DOES NOT cover the MMT, unresolve all related returns
        if (bNewAmountResolvesMMT) {
            mmt.resolveMMTAndRelatedMMTs();
        } else {
            mmt.unresolveMMTAndRelatedTransactionReturns();
        }
        
        // create the redebit
        SettlementType settlementType = DDProcessesToDTO.getDomainSettlementType(mInputDTO.getSettlementType());
        SpcfCalendar settlementDate = DateDTO.convertToSpcfCalendar(mInputDTO.getInitiationDate());
        mRedebitFT = FinancialTransaction.createFinancialTransaction(mOrigFT.getCompany(),
                mOrigFT.getPayrollRun(), null, null, null, null, null, mRedebitTypeCd, mInputDTO.getAmount(),
                settlementType, settlementDate, mOrigFT.getSku(), mOrigFT, 0);

        // advance its state through Executed to Completed to make ledger entries
        mRedebitFT = mRedebitFT.updateFinancialTransactionState(TransactionStateCode.Executed);
        mRedebitFT = mRedebitFT.updateFinancialTransactionState(TransactionStateCode.Completed);

        result.setResult(mRedebitFT);
        return result;
    }
}
