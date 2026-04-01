package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;

/**
 * User: rsakhamuri
 * Date: Jan 3, 2008
 * Time: 1:46:48 PM
 */
public class VoidTransactionCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mPSETransactionId;
    private Company mCompany;
    private FinancialTransaction mFinancialTransaction;
    private TransactionResponse mTrasactionResponse;

    public VoidTransactionCore(SourceSystemCode pSourceSystemCode,
                               String pSourceCompanyId,
                               String pPSETransactionId) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.mPSETransactionId = pPSETransactionId;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check if company is on DD Service
/*        mCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(mCompany, ServiceCode.DirectDeposit);
        if (mCompanyService == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                    mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId,
                    ServiceCode.DirectDeposit.toString());
            return validationResult;
        }*/
        // Check financial transaction id is null
        if (mPSETransactionId == null || mPSETransactionId.length() == 0) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            mPSETransactionId, mPSETransactionId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        try {
            SpcfUniqueId.createInstance(mPSETransactionId);
        }
        catch (SpcfIllegalArgumentException ex) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            mPSETransactionId, mPSETransactionId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify the existence of the Financial Tx for the company
        mFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(mPSETransactionId));
        if (mFinancialTransaction == null) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            mPSETransactionId, mPSETransactionId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify that VOID is a valid ACTION for this transaction, given
        // tx settlement type and current state.
        ActionEventCode actionEventCode = mFinancialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyRefundTOR ?
                ActionEventCode.VoidTORTransaction : ActionEventCode.FinancialTransactionVoidTx;
        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, actionEventCode);
        if (!mFinancialTransaction.isValidAction(actionEvent)) {
            validationResult.getMessages().ActionNotValidForFinancialTransaction(EntityName.FinancialTransaction,
                    mPSETransactionId, actionEvent.getCode().toString(), mPSETransactionId,
                    mFinancialTransaction.getTransactionType().getTransactionTypeCd().toString(),
                    mFinancialTransaction.calculateCurrentTransactionState().getTransactionStateCd().toString());

        }
        return validationResult;
    }


    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // Update the state of the Tx to void (VOID) and make
        // corresponding ledger entries
        FinancialTransaction financialTx = mFinancialTransaction.updateFinancialTransactionState(
                TransactionStateCode.Voided);


        if(financialTx.getPayrollRun() != null) {
            DomainEntitySet<CompanyEvent> nsfEventsList = CompanyEvent.findCompanyEvents(mCompany,
                    EventTypeCode.NSF, CompanyEventStatus.Active, null, null);


            DomainEntitySet<CompanyEvent> debitReturnEventsList = CompanyEvent.findCompanyEvents(mCompany,
                    EventTypeCode.DDDebitReturn, CompanyEventStatus.Active, null, null);

            Collection<OnHoldReason> expiredOnHoldReasons = mCompany.getExpiredOnHoldReasons();

            // if there is a ledger balance, put the company back on hold and put any apply forward credits back onto voided payroll

            if (ledgerBalanceDue(financialTx)) {
                for (OnHoldReason onHoldReason : expiredOnHoldReasons) {
                    if (onHoldReason.getOnHoldReasonCd() == ServiceSubStatusCode.AchRejectOther) {
                        addOnHoldReason(onHoldReason.getOnHoldReasonCd(), debitReturnEventsList);
                    } else if (onHoldReason.getOnHoldReasonCd() == ServiceSubStatusCode.AchRejectR1R9) {
                        addOnHoldReason(onHoldReason.getOnHoldReasonCd(), nsfEventsList);
                    }
                }
            }

            // if the transaction was for a prefunding wire and the payroll has not offloaded place the company back on hold
            if(financialTx.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdDebit &&
                    financialTx.getSettlementTypeCd() != SettlementType.ACH &&
                    financialTx.getPayrollRun().getPayrollRunStatus() == PayrollStatus.Pending) {
                PayrollServices.companyManager.addOnHoldReason(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), ServiceSubStatusCode.PendingPrefundingWire);
            }

            changePayrollStatusForRedebit(financialTx);
        }

        return processResult;
    }


    private void changePayrollStatusForRedebit(FinancialTransaction pFinancialTransaction) {

        if (TransactionType.isRedebitTransactionType(pFinancialTransaction.getTransactionType().getTransactionTypeCd())) {

            PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();
            TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);

            // If there are pending redebits in "Created" state, change the status to "PendingRedebit"
            DomainEntitySet<FinancialTransaction> pendingRedebits = payrollRun.getFinancialTransactions(createdState, TransactionAssociationType.Redebit);
            if (pendingRedebits.size() > 0) {
                payrollRun.updatePayrollRunStatus(PayrollStatus.PendingRedebit);
            } else {
                // If there are pending redebits in "Executed" state, change the status to "RedebitOffloaded"
                TransactionState executedState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
                DomainEntitySet<FinancialTransaction> offloadedRedebits = payrollRun.getFinancialTransactions(executedState, TransactionAssociationType.Redebit);
                if (offloadedRedebits.size() > 0) {
                    payrollRun.updatePayrollRunStatus(PayrollStatus.RedebitOffloaded);
                } else {
                    if (ledgerBalanceDue(pFinancialTransaction)) {
                        //  If there is a balance due and there are  No pending redebits, change the status to "DebitReturned"
                        payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);
                    }
                }
            }
        }
    }

    private boolean ledgerBalanceDue(FinancialTransaction pFinancialTransaction) {
        PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();
        SpcfMoney ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                payrollRun.getSourcePayRunId(), payrollRun.getCompany());

        return (ledgerBalance.subtract(pFinancialTransaction.getFinancialTransactionAmount()).compareTo(SpcfDecimal.createInstance(0.00)) < 0);
    }

    private void addOnHoldReason(ServiceSubStatusCode pOnHoldReasonCode, DomainEntitySet<CompanyEvent> pCompanyEventsList) {
        for (CompanyEvent companyEvent : pCompanyEventsList) {
            Collection<String> eventDetailValue = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
            for (String finTxnID : eventDetailValue) {
                FinancialTransaction finTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(finTxnID));
                if (finTxn.getPayrollRun().getSourcePayRunId().equals(mFinancialTransaction.getPayrollRun().getSourcePayRunId())) {
                    PayrollServices.companyManager.addOnHoldReason(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), pOnHoldReasonCode);
                }
            }
        }
    }
}
