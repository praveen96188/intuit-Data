package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: rsakhamuri
 * Date: Jan 3, 2008
 * Time: 4:07:08 PM
 */
public class RefundEmployerTransactionCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private ERRefundDTO mRefundDTO;

    private Company mCompany;
    private BankAccount mBankAccount = null;
    private IntuitBankAccount mDebitIntuitBankAccount;
    private TransactionResponse mTransactionResponse;
    private FinancialTransaction mFinancialTransaction;
    private TransactionTypeCode mNewTxnType;
    private TransactionTypeCode mOrgTxnType;
    private FinancialTransaction mRefundFinancialTransaction;

    public RefundEmployerTransactionCore(SourceSystemCode pSourceSystemCode,
                                         String pSourceCompanyId, ERRefundDTO pRefundDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mRefundDTO = pRefundDTO;
    }

    public TransactionResponse getTransactionResponse() {
        return mTransactionResponse;
    }

    public FinancialTransaction getFinancialTransaction() {
        return mRefundFinancialTransaction;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        CompanyBankAccount companyBankAccount;

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Check if comany exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify whether   Tx Id is Null
        String pseTransactionId = mRefundDTO.getFinancialTxId();
        if (pseTransactionId == null || pseTransactionId.length() == 0) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            pseTransactionId, pseTransactionId, mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }
        // Verify the existence of the Financial Tx for the company
        // If not throw the exception: Financial Transaction {2} does not
        // exist for company {0}:{1}
        mFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pseTransactionId));
        if (mFinancialTransaction == null) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            pseTransactionId, pseTransactionId, mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify that cancel is a valid ACTION for this transaction, given
        // it�s tx type and current state.
        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.IssueReissueRefundEr);

        if (!mFinancialTransaction.isValidAction(actionEvent)) {

            validationResult.getMessages().ActionNotValidForFinancialTransaction(EntityName.FinancialTransaction,
                    pseTransactionId, actionEvent.getCode().toString(),
                    pseTransactionId,
                    mFinancialTransaction.getTransactionType().getTransactionTypeCd().toString(),
                    mFinancialTransaction.calculateCurrentTransactionState().getTransactionStateCd().toString());

            return validationResult;
        }

        if (! mCompany.isAllowedCapability(SystemCapabilityCode.RefundOrCredit)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                mCompany.getSourceSystemCd().toString(),
                mCompany.getSourceCompanyId(), SystemCapabilityCode.RefundOrCredit.toString());
            return validationResult;
        }

        // Verify that Financial Tx already have an associated
        // refund transaction in the db. (ignore associated refund txs that
        // are �Canceled�)
        // If one of them has already has an associated refund transaction
        // throw the exception: Financial Transaction {2} cannot be
        // refunded, because a refund has already been attempted.
        DomainEntitySet<FinancialTransaction> associatedTxCollection = mFinancialTransaction.getAssociatedTransactionsCollection();
        TransactionStateCode childTxStateCode = null;
        for (FinancialTransaction finTx : associatedTxCollection) {
            childTxStateCode = finTx.calculateCurrentTransactionState().getTransactionStateCd();
            if ((finTx.getTransactionType().getAssociationType().equals(TransactionAssociationType.Refund))
                    && !((TransactionStateCode.Cancelled.equals(childTxStateCode))
                    || (TransactionStateCode.Voided.equals(childTxStateCode)))) {
                validationResult.getMessages().TransactionAlreadyRefunded(EntityName.FinancialTransaction,
                        pseTransactionId, pseTransactionId, mSourceSystemCode.toString(), mSourceCompanyId);

                return validationResult;
            }
        }

        mOrgTxnType = mFinancialTransaction.getTransactionType().getTransactionTypeCd();
        mNewTxnType = getRefundType(mOrgTxnType);
        if (mNewTxnType == null) {
            validationResult.getMessages().TransactionTypeInvalidForRefunded(EntityName.FinancialTransaction,
                                                                             pseTransactionId, mOrgTxnType.toString());
            return validationResult;
        }

        //One time courtesy BackdatedPayroll Fee refund is allowed
        if(Application.getCurrentPrincipal().isAgent() && !AuthUser.findUser(Application.getCurrentPrincipal().getId()).hasOperation(OperationId.CreateMultipleBackdatingRefunds)
                && TransactionType.isFeeTransactionType(mOrgTxnType)) {
            BillingDetail billingDetail = mFinancialTransaction.getBillingDetail();
            if (TransactionType.isRedebitTransactionType(mOrgTxnType)) {
                billingDetail = mFinancialTransaction.getOriginalTransaction().getBillingDetail();
            }
            if(billingDetail != null && billingDetail.getOfferingServiceChargeType() == OfferingServiceChargeType.BackdatedPayroll &&
                    CompanyEvent.findCompanyEventDetails(mCompany, EventTypeCode.FeeRefunded, EventDetailTypeCode.FeeType, OfferingServiceChargeType.BackdatedPayroll.toString()).size() >= 1) {
                validationResult.getMessages().AlreadyRefundedOneTimeCourtesyRefund(EntityName.FinancialTransaction, pseTransactionId, OfferingServiceChargeType.BackdatedPayroll.toString());
                return validationResult;
            }
        }

        TransactionType transactionType = TransactionType.findTransactionType(mNewTxnType);

        mDebitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);


        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {
            //Find active company bank account
            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

            if (companyBankAccount == null) {
                validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                        mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);

                return validationResult;
            } else {
                mBankAccount = companyBankAccount.getBankAccount();
            }

            if (! CompanyService.isCompanyOnDirectDepositOrTaxService(mCompany)) {
                validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                        mSourceCompanyId, mSourceSystemCode.toString(),
                        mSourceCompanyId, ServiceCode.DirectDeposit.toString() + " or " + ServiceCode.Tax.toString());
                return validationResult;
            }


        } else {
            //validate the amount amount
            ProcessResult validateDDRefundDTOtResult = mRefundDTO.validateAmount();
            validationResult.merge(validateDDRefundDTOtResult);

            // Validate the date provided
            SpcfCalendar currentDate = PSPDate.getPSPTime();

            SpcfCalendar pastDate = PSPDate.getPSPTime();
            pastDate.addDays(-45);

            validationResult.merge(mRefundDTO.validateDate(currentDate, pastDate));
        }

        if (!mCompany.passesAdditionalCancelTermValidation(false, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId(), SystemCapabilityCode.RefundOrCredit.toString());
        }

        return validationResult;
    }



    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        BankAccountOwnerType creditBankAccountOwnerType = BankAccountOwnerType.Company;
        BankAccountOwnerType debitBankAccountOwnerType = BankAccountOwnerType.Intuit;

        // REP000042 - Resolve the unresolved TransactionReturnBO instance for this transaction if there's one
        mFinancialTransaction.resolveTransactionReturns();

        if (SettlementTypeDTO.ACH.equals(mRefundDTO.getSettlementType())) {

            /*
               * o Create a new ER Fin Tx ? State = CR ? Settlement Type = ACH ?
               * Settlement Date = soonest possible based on the cutoff time
               * for the company (most often tomorrow) ? Amount = the amount
               * of the original transaction ? Company Bank Account is
               * provided ? Tx Type is based on the tx type of the original: �
               * ER DD Redebit ? ER Double Payment Refund CR � EE DD Reversal
               * Debit ? ER DD Reversal Refund CR � EE DD CR ? ER DD Reject
               * Refund CR � ER Return Fee Debit ? ER Return Fee Refund CR �
               * ER Reversal Fee Debit ? ER Reversal Fee Refund CR � ER *
               * Refund CR ? ER DD Returned Refund CR ? Make corresponding
               * ledger entries ? Associate with the original payroll ?
               * Associate with the original financial transaction (identifies
               * the original financial transaction as having a refund against
               * it) o Create a new Transaction Response for the company ?
               * Associate the new refund financial transaction ? Request ID
               * is null
            */

            SpcfCalendar settlementDate;
            //if (TransactionTypeBE.isFeeTransactionType(mOrgTxnType)) {
            if ((mOrgTxnType == TransactionTypeCode.EmployerFeeDebit) ||
                (mOrgTxnType == TransactionTypeCode.EmployerFeeRedebit) ||
                (mOrgTxnType == TransactionTypeCode.ServiceSalesAndUseTax) ||
                (mOrgTxnType == TransactionTypeCode.ServiceSalesAndUseTaxRedebit)) {
                // not necessarily the earliest possible
                settlementDate = mFinancialTransaction.getRefundTransactionSettlementDate();
            } else {
                // earliest possible
                settlementDate = FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup());
            }

            // Add the financial transaction to the database
            FinancialTransaction refundFinancialTransaction = FinancialTransaction.createFinancialTransaction(
                                                                mCompany,
                                                                mFinancialTransaction.getPayrollRun(),
                                                                null,
                                                                mBankAccount,
                                                                mDebitIntuitBankAccount.getBankAccount(),
                                                                creditBankAccountOwnerType,
                                                                debitBankAccountOwnerType,
                                                                mNewTxnType,
                                                                mFinancialTransaction.getFinancialTransactionAmount(),
                                                                SettlementType.ACH,
                                                                settlementDate,
                                                                mFinancialTransaction.getSku(),
                                                                mFinancialTransaction,
                                                                mFinancialTransaction.getSkuQuantity());

            // Add the association to the original transaction
            refundFinancialTransaction.getTransactionType().setAssociationType(TransactionAssociationType.Refund);
            refundFinancialTransaction.setOriginalTransaction(mFinancialTransaction);
            mFinancialTransaction.addAssociatedTransactions(refundFinancialTransaction);

            if (refundFinancialTransaction.getTransactionType().getIncludeInTransactionResponse()) {
                TransactionResponse.createTransactionResponseForFinancialTx(refundFinancialTransaction);
            }

            mFinancialTransaction = Application.save(mFinancialTransaction);
            mRefundFinancialTransaction = refundFinancialTransaction;

        } else { // not ACH

            SpcfCalendar settlementDate = DateDTO.convertToSpcfCalendar(mRefundDTO.getTxDate());

            // Add the financial transaction to the database
            FinancialTransaction refundFT = FinancialTransaction.createFinancialTransaction(
                                            mCompany,
                                            mFinancialTransaction.getPayrollRun(),
                                            null,
                                            null,
                                            mDebitIntuitBankAccount.getBankAccount(),
                                            creditBankAccountOwnerType,
                                            debitBankAccountOwnerType,
                                            mNewTxnType,
                                            mRefundDTO.getFinancialTxAmt(),
                                            ProcessesToDTO.getDomainSettlementType(mRefundDTO.getSettlementType()),
                                            settlementDate,
                                            mFinancialTransaction.getSku(),
                                            mFinancialTransaction,
                                            mFinancialTransaction.getSkuQuantity());

            // Add the association to the original transaction
            refundFT.getTransactionType().setAssociationType(TransactionAssociationType.Refund);
            refundFT.setOriginalTransaction(mFinancialTransaction);
            mFinancialTransaction.addAssociatedTransactions(refundFT);

            mFinancialTransaction = Application.save(mFinancialTransaction);

            // Update transaction to executed, then completed
            refundFT = refundFT.updateFinancialTransactionState(TransactionStateCode.Executed);
            refundFT = refundFT.updateFinancialTransactionState(TransactionStateCode.Completed);
            mRefundFinancialTransaction = refundFT;
        }

        if (TransactionType.isFeeTransactionType(mOrgTxnType) ||
                (mOrgTxnType == TransactionTypeCode.ServiceSalesAndUseTax) ||
                (mOrgTxnType == TransactionTypeCode.ServiceSalesAndUseTaxRedebit)) {

            BillingDetail billingDetail = mFinancialTransaction.getBillingDetail();
            if (TransactionType.isRedebitTransactionType(mOrgTxnType)) {
                billingDetail = mFinancialTransaction.getOriginalTransaction().getBillingDetail();
            }

            //since the text of the e-mail never makes sense for non-ACH, will suppress here even if the DTO says not to.
            boolean suppressEmail = mRefundDTO.getSupressRefundEmail() || mRefundDTO.getSettlementType() != SettlementTypeDTO.ACH;

            CompanyEvent.createFeeRefundedEvent(billingDetail,
                                                mRefundFinancialTransaction,
                                                suppressEmail);
        }

        return processResult;
    }

    private static TransactionTypeCode getRefundType(TransactionTypeCode pOrigType) {
        switch (pOrigType) {
            // debit and redebit types
            case EmployerDdRedebit:
                return TransactionTypeCode.EmployerDoublePaymentRefundCredit;
            case EmployerTaxRedebit:
                return TransactionTypeCode.EmployerTaxDoublePaymentRefundCredit;

            case EmployerFeeDebit:
            case EmployerFeeRedebit:
                return TransactionTypeCode.EmployerFeeRefundCredit;

            case ServiceSalesAndUseTax:
            case ServiceSalesAndUseTaxRedebit:
                return TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit;

            // refund types
            case EmployerDdRefundCredit:
            case EmployerDoublePaymentRefundCredit:
            case EmployerDdRejectRefundCredit:
            case EmployerDdReversalRefundCredit:
                return TransactionTypeCode.EmployerDdReturnedRefundCredit;

            case EmployerFeeRefundCredit:
                return TransactionTypeCode.EmployerFeeReturnedRefundCredit;

            case ServiceSalesAndUseTaxRefundCredit:
                return TransactionTypeCode.ServiceSalesAndUseTaxReturnedRefundCredit;

            // employee types
            case EmployeeDdReversalDebit:
                return TransactionTypeCode.EmployerDdReversalRefundCredit;

            case EmployeeDdCredit:
                return TransactionTypeCode.EmployerDdRejectRefundCredit;

            // recredit types refund themselves
            case EmployerDdReturnedRefundCredit:
            case EmployerFeeReturnedRefundCredit:
            case ServiceSalesAndUseTaxReturnedRefundCredit:
                return pOrigType;

            // unexpected type
            default:
                return null;
        }
    }
}
