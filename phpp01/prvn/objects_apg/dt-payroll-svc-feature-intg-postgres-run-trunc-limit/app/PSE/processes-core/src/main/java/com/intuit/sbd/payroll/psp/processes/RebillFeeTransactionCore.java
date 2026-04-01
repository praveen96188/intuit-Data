package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RebillFeeTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;

/**
 * Takes the ID of a fee debit FT and the ID of a company bank account as input.  Refunds the fee and any related
 * sales tax (using the RefundEmployerTransactionCore process).  Then bills the same fee again.  Presumably, the
 * Company has a different offer or tax-exempt status at the time of the rebill than it did when the original fee
 * was charged.
 * <p/>
 * User: wnichols
 * Date: Jun 13, 2008
 * Time: 1:01:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class RebillFeeTransactionCore extends Process implements IProcess {
    private RebillFeeTransactionDTO mInputDTO;
    private DomainEntitySet<FinancialTransaction> mRefundableFTs = new DomainEntitySet<FinancialTransaction>();
    private ArrayList<RefundEmployerTransactionCore> mRefundProcesses = new ArrayList<RefundEmployerTransactionCore>();
    private SpcfDecimal mTotalRefunded = SpcfDecimal.createInstance(0);
    private FinancialTransaction mOriginalFT;

    private BillingDetail mOrigDetail;
    private CompanyBankAccount mCBA;
    private SpcfMoney mOverrideAmount;
    private Integer mOverrideQuantity;


    public RebillFeeTransactionCore(RebillFeeTransactionDTO pDTO) {
        mInputDTO = pDTO;
    }

    /**
     * Validates process inputs.
     *
     * @return
     */
    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        // fee debit FT ID is non-null
        String inputFtId = mInputDTO.getFeeDebitTransactionId();
        if (inputFtId == null || inputFtId.length() == 0) {
            result.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, null, "FeeDebitTransactionId");
            return result;
        }

        // FT exists with this ID
        mOriginalFT = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(inputFtId));
        if (mOriginalFT == null) {
            result.getMessages().NoEntityWithGivenId("FinancialTransaction", inputFtId);
            return result;
        }

        Company company = mOriginalFT.getCompany();

        // make sure this action is OK for this txn
        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.RefundRebillFee);
        if (!mOriginalFT.isValidAction(actionEvent)) {
            result.getMessages().ActionNotValidForFinancialTransaction(
                    EntityName.FinancialTransaction,
                    inputFtId,
                    actionEvent.getCode().toString(),
                    inputFtId,
                    mOriginalFT.getTransactionType().getTransactionTypeCd().toString(),
                    mOriginalFT.calculateCurrentTransactionState().getTransactionStateCd().toString());
            return result;
        }

        // find the BillingDetail
        while (mOriginalFT != null && mOriginalFT.getBillingDetail() == null) {
            mOriginalFT = mOriginalFT.getOriginalTransaction();
        }
        if (mOriginalFT == null) {
            throw new RuntimeException("No BillingDetail related to financial transaction with ID " + inputFtId);
        }

        mOrigDetail = mOriginalFT.getBillingDetail();

        // build up the list of fee and tax debit/redebit transactions to be refunded
        findRefundableTransactions();

        // build up the list of processes that will refund them, and perform their validations
        for (FinancialTransaction refundable : mRefundableFTs) {
            ERRefundDTO refundDTO = new ERRefundDTO();
            refundDTO.setFinancialTxAmt( refundable.getFinancialTransactionAmount() );
            refundDTO.setFinancialTxId( refundable.getId().toString() );
            refundDTO.setSettlementType(SettlementTypeDTO.ACH);
            refundDTO.setSupressRefundEmail(true);
            // refundDTO.setTxDate() ignored for ACH settlement
            RefundEmployerTransactionCore refundProcess = new RefundEmployerTransactionCore(company.getSourceSystemCd(),
                                                                                            company.getSourceCompanyId(),
                                                                                            refundDTO);
            result.merge(refundProcess.validate());
            mRefundProcesses.add(refundProcess);
        }
        if (! result.isSuccess()) {
            return result;
        }

        // if the rebill amount is overridden, validate that amount
        if (mInputDTO.getOverrideAmount() != null) {
            mOverrideAmount = mInputDTO.getOverrideAmount();

            //Ensure the override amount is positive
            if (mOverrideAmount.compareTo(new SpcfMoney("0.00")) <= 0) {
                result.getMessages().AmountNotPositive(EntityName.Fee, inputFtId);
            }
        }
        if (mInputDTO.getOverrideQuantity() != null) {
            mOverrideQuantity = mInputDTO.getOverrideQuantity();

            if (mOverrideQuantity <= 0) {
                result.getMessages().AmountNotPositive(EntityName.Fee, inputFtId);
            }
        }

        // make sure the company has an active CBA
        mCBA = CompanyBankAccount.findActiveCompanyBankAccount(company);
        if (mCBA == null) {
            result.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                    company.getSourceCompanyId(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        }

        return result;
    }

    /**
     * Rebills the fee.
     *
     * @return a ProcessResult<BillingDetail> with the BillingDetail for the rebilled fee (and possibly tax).
     */
    public ProcessResult<DomainEntitySet<BillingDetail>> process() {
        ProcessResult<DomainEntitySet<BillingDetail>> result = new ProcessResult<DomainEntitySet<BillingDetail>>();

        // process the refunds
        for (RefundEmployerTransactionCore refundProcess : mRefundProcesses) {
            result.merge( refundProcess.process() );
        }
        if (!result.isSuccess()) {
            return result;
        }

        // we want the rebilled fee to have the same settlement date as the refunds, so they'll offload together
        SpcfCalendar settlementDate = null;
        if (mRefundProcesses.size() > 0) {
            settlementDate = mRefundProcesses.get(0).getFinancialTransaction().getSettlementDate().toLocal();
        }

        // bill the new fee
        DomainEntitySet<BillingDetail> newDetails;
        OfferingCode associatedOfferingCode = null;
        OfferingServiceChargeGroup offeringServiceChargeGroup = OfferingServiceChargeGroup.getOfferingServiceChargeGroup(mOriginalFT.getCompany(), null, mOrigDetail.getOfferingServiceChargeType());
        if(offeringServiceChargeGroup != null) {
            associatedOfferingCode = offeringServiceChargeGroup.getOffering().getOfferingCode();
        }

        if (mOverrideAmount == null && mOverrideQuantity == null) {
            newDetails = BillingDetail.createBillingDetail(mOrigDetail.getPayrollRun(), mCBA, mOrigDetail.getOfferingServiceChargeType(),
                                    mOrigDetail.getQuantity(), settlementDate, associatedOfferingCode);
        } else {
            newDetails = BillingDetail.createBillingDetailWithPriceAndSettlementDateOverride(mOrigDetail.getPayrollRun(), mCBA,
                                                                      mOrigDetail.getOfferingServiceChargeType(),
                                                                      mOverrideQuantity == null ? mOrigDetail.getQuantity() : mOverrideQuantity,
                                                                      SpcfUtils.convertToBigDecimal(mOverrideAmount),
                                                                      settlementDate,
                                                                      associatedOfferingCode, mOrigDetail.getMemo());
        }
        for (BillingDetail newDetail : newDetails) {
            FinancialTransaction employerFeeDebit = null;
            for (FinancialTransaction ft : newDetail.getFinancialTransactionCollection()) {
                if (TransactionTypeCode.EmployerFeeDebit.equals(ft.getTransactionType().getTransactionTypeCd())) {
                    employerFeeDebit = ft;
                    break;
                }
            }

            // create company event
            CompanyEvent.createFeeRebilledEvent(mOrigDetail.getPayrollRun().getCompany(), mOrigDetail, newDetail, employerFeeDebit, mTotalRefunded);
        }

        result.setResult(newDetails);

        return result;
    }

    private SpcfMoney getCollectedAmount(FinancialTransaction pFT) {
        TransactionSummary summary = pFT.summarizeRelatedTransactions();
        return new SpcfMoney(summary.amtCollected);
    }


    private void findRefundableTransactions() {
        DomainEntitySet<FinancialTransaction> relatedFTs = new DomainEntitySet<FinancialTransaction>();
        relatedFTs.add(mOrigDetail.getFeeTransaction());
        if (mOrigDetail.getTaxTransaction() != null) {
            relatedFTs.add(mOrigDetail.getTaxTransaction());
        }
        while (relatedFTs.size() > 0) {
            FinancialTransaction ft = relatedFTs.iterator().next();
            relatedFTs.remove(ft);
            boolean bRefundable = false;

            // to be refundable, it must be a debit or redebit type...
            TransactionTypeGroupCode group = ft.getTransactionType().getTransactionTypeGroupCd();
            if (group == TransactionTypeGroupCode.Debit || group == TransactionTypeGroupCode.Redebit) {

                // ...and it must be Executed or Completed...
                TransactionStateCode state = ft.getCurrentTransactionState().getTransactionStateCd();
                if (state == TransactionStateCode.Executed || state == TransactionStateCode.Completed) {

                    mRefundableFTs.add(ft);
                    mTotalRefunded = mTotalRefunded.add(ft.getFinancialTransactionAmount());
                    bRefundable = true;

                }
            }

            // if this one wasn't refundable (e.g. because it was Cancelled or Returned), then look for related
            // transactions (e.g. redebits) that may be refundable
            if (! bRefundable) {
                ft.getRelatedTransactions(relatedFTs);
            }
        }
    }
}
