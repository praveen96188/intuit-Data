package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Collection;
import java.util.HashMap;

/**
 * User: rsakhamuri
 * Date: Dec 11, 2007
 * Time: 3:22:47 PM
 */
public class AddRedebitImpoundTransactionCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private RedebitImpoundDTO redebitImpoundDTO;
    private Company mCompany;
    private FinancialTransaction originalFinancialTxn;
    private PayrollRun mPayrollRun;
    private CompanyBankAccount mCompanyBankAccount;
    private TransactionResponse mTrasactionResponse;
    private FinancialTransaction mRedebitTransaction;
    private TransactionSummary txnSummary;
    private HashMap<SpcfUniqueId, CancelTransactionCore> cancelTransactionCoreProcesses;

    public AddRedebitImpoundTransactionCore(SourceSystemCode pSourceSystemCode,
                                            String pSourceCompanyId,
                                            RedebitImpoundDTO pRedebitImpoundDTO) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.redebitImpoundDTO = pRedebitImpoundDTO;
        cancelTransactionCoreProcesses = new HashMap<SpcfUniqueId, CancelTransactionCore>();
    }

    public FinancialTransaction getFinancialTransaction() {
        return mRedebitTransaction;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (redebitImpoundDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.DDTransaction, null, "RedebitImpoundDTO");
            return validationResult;
        }
        // validate the DTO
        validationResult.merge(redebitImpoundDTO.validate());
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

        // Check if company bank account is Active
        mCompanyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

        if (mCompanyBankAccount == null) {
            validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                    mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check original financial transaction exists
        originalFinancialTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                SpcfUniqueId.createInstance(redebitImpoundDTO.getOriginalFinancialTxId()));

        if (originalFinancialTxn == null) {
            validationResult.getMessages().FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                    redebitImpoundDTO.getOriginalFinancialTxId(),
                    redebitImpoundDTO.getOriginalFinancialTxId(), mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }
        // Check whether this is a valid action
        mPayrollRun = originalFinancialTxn.getPayrollRun();
        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.DDRedebitAdd);

        if (!TransactionType.isValidTypeToAddRedebit(originalFinancialTxn.getTransactionType().getTransactionTypeCd())) {
            validationResult.getMessages().ActionNotValidForFinancialTransaction(EntityName.FinancialTransaction,
                    originalFinancialTxn.getId().toString(),
                    actionEvent.getCode().toString(),
                    originalFinancialTxn.getId().toString(),
                    originalFinancialTxn.getTransactionType().getTransactionTypeCd().toString(),
                    originalFinancialTxn.calculateCurrentTransactionState().getTransactionStateCd().toString());
            return validationResult;
        }

        // also verify that there is no existing redebits for the same category (payroll, fee, service sales and use tax)
        // make sure there's not already an un-cancelled redebit child transaction
        for (FinancialTransaction child : originalFinancialTxn.getAssociatedTransactionsCollection()) {
            TransactionTypeCode childTypeCd = child.getTransactionType().getTransactionTypeCd();

            // if this child is one of the redebit types...
            if ((childTypeCd.equals(TransactionTypeCode.EmployerFeeRedebit) &&
                    (originalFinancialTxn.getTransactionType().getTransactionTypeCd() ==
                            TransactionTypeCode.EmployerFeeDebit)) ||
                    (childTypeCd.equals(TransactionTypeCode.ServiceSalesAndUseTaxRedebit)) &&
                            (originalFinancialTxn.getTransactionType().getTransactionTypeCd() ==
                                    TransactionTypeCode.ServiceSalesAndUseTax)) {
                // if it's pending, we have a problem
                TransactionStateCode childStateCd = child.calculateCurrentTransactionState().getTransactionStateCd();
                if (childStateCd.equals(TransactionStateCode.Created)) {
                    validationResult.getMessages().MultipleReissuanceOfSameFee(EntityName.FinancialTransaction,
                            originalFinancialTxn.getId().toString(),
                            mSourceSystemCd.toString(), mSourceCompanyId, originalFinancialTxn.getId().toString());
                    return validationResult;
                }
                // else not created, and we're OK
            }
            // else not a reissue, and we're OK
        }


        txnSummary = originalFinancialTxn.summarizeRelatedTransactions();
        String offeringServiceChargeType = null;
        if (TransactionType.isFeeTransactionType(originalFinancialTxn.getTransactionType().getTransactionTypeCd())) {
            offeringServiceChargeType =
                    OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(originalFinancialTxn.getSku()).toString();
        }

        if (txnSummary.amtUncollected.compareTo(redebitImpoundDTO.getAmount()) < 0) {
            validationResult.getMessages().RedebitAmountExceedsUncollectedAmount(EntityName.FinancialTransaction,
                    originalFinancialTxn.getId().toString(), redebitImpoundDTO.getAmount().toString(),
                    originalFinancialTxn.getTransactionType().getTransactionTypeCd().toString(),
                    offeringServiceChargeType);
            return validationResult;
        }

        // verify the Initiation date is not in past or a not a holiday
        SpcfCalendar initiationDate = DateDTO.convertToSpcfCalendar(redebitImpoundDTO.getInitiationDate());
        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        CalendarUtils.clearTime(initiationDate);
        boolean isInitiationDateInPast = initiationDate.before(today);
        if (isInitiationDateInPast || CalendarUtils.isWeekendOrHoliday(initiationDate)) {
            validationResult.getMessages().SettlementDateNotFutureBankingDay(EntityName.Date, null,
                    initiationDate.toString(), SettlementType.ACH.toString());
            return validationResult;
        }

        //If the payroll is in the PendingReversals state, create a new CancelTransactionCore proces for each EmployeeDDReversal transaction in a Created state
        //The process will be "processed" in this class's process step
        if (PayrollStatus.PendingReversals.equals(mPayrollRun.getPayrollRunStatus())) {
            TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);
            TransactionType eeDDReversal = Application.findById(TransactionType.class, TransactionTypeCode.EmployeeDdReversalDebit);
            DomainEntitySet<FinancialTransaction> reversalFinTxns = mPayrollRun.getFinancialTransactions(createdState, eeDDReversal);
            for (FinancialTransaction currFinTxn : reversalFinTxns) {
                CancelTransactionCore currCancelTxCore = new CancelTransactionCore(mSourceSystemCd, mSourceCompanyId, currFinTxn.getId().toString(), true);
                validationResult.merge(currCancelTxCore.validate());
                cancelTransactionCoreProcesses.put(currFinTxn.getId(), currCancelTxCore);
            }
        }
        return validationResult;

    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        //resolve MMT?  Only if this amount + the pending amount from THIS transaction type PLUS all the other transactions associated with the MMT
        // is equal to the pending collection amount
        MoneyMovementTransaction mmt = originalFinancialTxn.getMoneyMovementTransaction();
        boolean bNewAmountResolvesMMT = mmt.amountResolvesMMT(redebitImpoundDTO.getAmount());

        //If the amount resolves the MMT, resolve the original MMT txn return and the txn returns for all the redebits related to the txns related to the original MMT
        //If the amount DOES NOT cover the MMT, unresolve all related returns
        if (bNewAmountResolvesMMT) {
            mmt.resolveMMTAndRelatedMMTs();
        } else {
            mmt.unresolveMMTAndRelatedTransactionReturns();
        }

        TransactionTypeCode txnTypeCode = findTxnTypeCodeForOriginalType(originalFinancialTxn.getTransactionType().getTransactionTypeCd());

        SpcfCalendar settlementDate = DateDTO.convertToSpcfCalendar(redebitImpoundDTO.getInitiationDate()).copy();
        boolean beforeCutoff = mCompany.getOffloadGroup().isBeforeActualCutoffTime();
        if (beforeCutoff) {
            CalendarUtils.addBusinessDays(settlementDate, 1);
        } else {
            CalendarUtils.addBusinessDays(settlementDate, 2);
        }

        IntuitBankAccount intuit = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(txnTypeCode),
                CreditDebitCode.Credit);

        // Add the transaction to the database
        FinancialTransaction financialTx =
                FinancialTransaction.createFinancialTransaction(originalFinancialTxn.getCompany(),
                        originalFinancialTxn.getPayrollRun(),
                        null,
                        intuit.getBankAccount(),
                        mCompanyBankAccount.getBankAccount(),
                        BankAccountOwnerType.Intuit,
                        BankAccountOwnerType.Company,
                        txnTypeCode,
                        redebitImpoundDTO.getAmount(),
                        SettlementType.ACH,
                        settlementDate,
                        originalFinancialTxn.getSku(),
                        originalFinancialTxn,
                        originalFinancialTxn.getSkuQuantity());

        mRedebitTransaction = financialTx;

        //Call the process method for all the CancelTransactionCore processes we created in validate
        for (SpcfUniqueId ftId : cancelTransactionCoreProcesses.keySet()) {
            // PSRV000492: if this FT has not already been cancelled, e.g. because of a redebit like this one, cancel it now
            FinancialTransaction ft = PayrollServices.entityFinder.findById(FinancialTransaction.class, ftId);
            if (ft!=null && ft.getCurrentTransactionState().getTransactionStateCd()==TransactionStateCode.Created) {
                CancelTransactionCore currCancelProc = cancelTransactionCoreProcesses.get(ftId);
                processResult.merge(currCancelProc.process());
            } // else don't execute that cancel process
        }

        // Create a transaction response for the ER DD REDEBIT
        Collection<FinancialTransaction> transactions = new DomainEntitySet<FinancialTransaction>();
        transactions.add(financialTx);
        mTrasactionResponse = TransactionResponse.createTransactionResponse(mCompany, transactions, null);

        return processResult;
    }

    private TransactionTypeCode findTxnTypeCodeForOriginalType(TransactionTypeCode pTransactionTypeCode) {
        TransactionTypeCode newTxnTypeCode = null;
        switch (pTransactionTypeCode) {
            case EmployerFeeDebit:
            case EmployerFeeRedebit:
                newTxnTypeCode = TransactionTypeCode.EmployerFeeRedebit;
                break;
            case EmployerDdDebit:
                newTxnTypeCode = TransactionTypeCode.EmployerDdRedebit;
                break;
            case EmployerTaxDebit:
                newTxnTypeCode = TransactionTypeCode.EmployerTaxRedebit;
                break;
            case ServiceSalesAndUseTax:
            case ServiceSalesAndUseTaxRedebit:
                newTxnTypeCode = TransactionTypeCode.ServiceSalesAndUseTaxRedebit;
                break;
        }

        return newTxnTypeCode;
    }

}
