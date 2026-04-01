package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import java.util.Collection;

/**
 * User: rsakhamuri
 * Date: Dec 31, 2007
 * Time: 12:10:09 PM
 */
public class CancelTransactionCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mPSETransactionId;
    private Company mCompany;
    private FinancialTransaction mFinancialTransaction;
    private TransactionResponse mTrasactionResponse;
    private boolean mIgnoreActionValidation;
    private boolean forceCreatorIdForCancellation = Boolean.FALSE;
    private String forcedCreatorId;

    public CancelTransactionCore(SourceSystemCode pSourceSystemCode,
                                 String pSourceCompanyId,
                                 String pPSETransactionId,
                                 boolean pIgnoreActionValidation) {
       this(pSourceSystemCode, pSourceCompanyId, pPSETransactionId, pIgnoreActionValidation, Boolean.FALSE, null);
    }

    public CancelTransactionCore(SourceSystemCode pSourceSystemCode,
                                 String pSourceCompanyId,
                                 String pPSETransactionId,
                                 boolean pIgnoreActionValidation, boolean pForceCreatorIdForCancellation, String pForcedCreatorId) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.mPSETransactionId = pPSETransactionId;
        this.mIgnoreActionValidation = pIgnoreActionValidation;
        this.forceCreatorIdForCancellation = pForceCreatorIdForCancellation;
        this.forcedCreatorId = pForcedCreatorId;
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

        if (mPSETransactionId == null || mPSETransactionId.length() == 0) {
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

        if (!mIgnoreActionValidation) {
            // Verify that cancel is a valid ACTION for this transaction, given
            // it�s tx type and current state.
            ActionEvent ftCancelActionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.FinancialTransactionCancel);
            ActionEvent feeCancelActionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.ERFeeCancel);
            ActionEvent erPayableRefundActionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.RefundERPayableCancel);

            if (!mFinancialTransaction.isValidAction(ftCancelActionEvent)
                    && !mFinancialTransaction.isValidAction(feeCancelActionEvent)
                    && !mFinancialTransaction.isValidAction(erPayableRefundActionEvent)) {
                validationResult.getMessages().ActionNotValidForFinancialTransaction(EntityName.FinancialTransaction,
                        mPSETransactionId, ftCancelActionEvent.getCode().toString(),
                        mPSETransactionId, mFinancialTransaction.getTransactionType().getTransactionTypeCd().toString(),
                        mFinancialTransaction.calculateCurrentTransactionState().getTransactionStateCd().toString());
                return validationResult;
            }
        }

        // verify whether we are cancelling ACH financial transaction after transaction cutoff time
        if (mFinancialTransaction.getSettlementTypeCd() == SettlementType.ACH) {

            if (!mFinancialTransaction.isCancellationAllowed()){
                validationResult.getMessages().TransactionNoLongerPendingCannotCancel(EntityName.FinancialTransaction,
                        mFinancialTransaction.getCompany().getSourceCompanyId(), mFinancialTransaction.getId().toString());
            }

        }

        return validationResult;
    }


    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // Update the state of the Tx to Canceled (CLD) and make
        // corresponding ledger entries
        FinancialTransaction financialTx;
        if(forceCreatorIdForCancellation){
            financialTx = mFinancialTransaction.cancelFinancialTransaction(Boolean.TRUE, forcedCreatorId);
        }else{
            financialTx = mFinancialTransaction.cancelFinancialTransaction();
        }
        // � If the original Fin Tx was previously
        // included in a Transaction Response,
        // � Create a new Transaction Response for the company                                               
        // � RequestID is null
        // � Associate the canceled transaction, only.
        DomainEntitySet<TransactionResponse> transactionResponseCollection =
                TransactionResponse.findTransactionResponses(financialTx);

        if (transactionResponseCollection.size() > 0) {
            Collection<FinancialTransaction> transactions = new DomainEntitySet<FinancialTransaction>();
            transactions.add(financialTx);
            mTrasactionResponse = TransactionResponse.createTransactionResponse(mCompany, transactions, null);
        }

        PayrollRun payRollRun = mFinancialTransaction.getPayrollRun();

        if(payRollRun != null && payRollRun.getPayrollRunStatus().equals(PayrollStatus.WrittenOff)){
            payRollRun.setPayrollRunStatus(PayrollStatus.DebitReturned);
            Application.save(payRollRun);
        }

        return processResult;
    }

}
