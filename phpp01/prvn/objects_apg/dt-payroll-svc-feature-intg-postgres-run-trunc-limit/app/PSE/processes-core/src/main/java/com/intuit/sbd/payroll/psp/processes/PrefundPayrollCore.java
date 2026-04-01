package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.PrefundPayrollTransactionDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 31, 2009
 * Time: 10:37:12 AM
 */
public class PrefundPayrollCore extends Process implements IProcess {

    private static final SpcfMoney ZERO = new SpcfMoney("0.00");

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private Company mCompany;
    private String mSourcePayrollRunId;
    private PayrollRun mPayrollRun;
    private SettlementType mSettlementType;
    private ArrayList<PrefundPayrollTransactionDTO> mTransactions;
    private ProcessResult<DomainEntitySet<FinancialTransaction>> mProcessResult;
    private SpcfCalendar mSettlementDate;

    public PrefundPayrollCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                              String pSourcePayrollRunId, SettlementType pSettlementType, SpcfCalendar pSettlementDate,
                              ArrayList<PrefundPayrollTransactionDTO> pTransactions) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourcePayrollRunId = pSourcePayrollRunId;
        this.mSettlementType = pSettlementType;
        this.mSettlementDate = pSettlementDate;
        this.mTransactions = pTransactions;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check if payroll run exists
        mPayrollRun = PayrollRun.findPayrollRun(mCompany,
                mSourcePayrollRunId);

        if (mPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                    mSourcePayrollRunId, mSourcePayrollRunId,
                    mSourceSystemCd.toString(), mSourceCompanyId);

            return validationResult;
        }

        // validate action is valid for the payroll run
        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.RecordPrefundingWire);
        if (!mPayrollRun.validateAction(actionEvent)) {
            validationResult.getMessages().ActionNotValidForPayrollRun(
                    EntityName.PayrollRun,
                    mPayrollRun.getSourcePayRunId(),
                    actionEvent.getCode().toString(),
                    mPayrollRun.getSourcePayRunId(),
                    mPayrollRun.getPayrollRunStatus().toString());
            return validationResult;
        }

        // check cuttoff time to make sure we are not offloading
        DomainEntitySet<FinancialTransaction> erDdDebitTransactions = mPayrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        if (erDdDebitTransactions.size() > 0 && !erDdDebitTransactions.get(0).isCancellationAllowed()) {
            validationResult.getMessages().PayrollRunAlreadyOffloadedPrefunding(EntityName.PayrollRun,
                    mPayrollRun.getSourcePayRunId(), mPayrollRun.getSourcePayRunId(), mSourceSystemCd.toString(),
                    mSourceCompanyId);
            return validationResult;
        }

        // validate the settlement type is non-ach
        if(mSettlementType == null || mSettlementType == SettlementType.ACH) {
            if(mSettlementType != null){
                validationResult.getMessages().InvalidSettlementTypeCode(EntityName.SettlementType,
                        mSettlementType.toString(), mSettlementType.toString());
            }
            else {
                validationResult.getMessages().InvalidValue(EntityName.SettlementType, null, EntityName.SettlementType.toString());
            }
            return validationResult;
        }

        // validate the settlement date
        if(mSettlementDate == null) {
            validationResult.getMessages().InvalidValue(EntityName.Date, null, "settlement date");
            return validationResult;
        }

        // validate amounts are all positive
        if(mTransactions == null || mTransactions.size() == 0){
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "The PrefundPayrollTransactionDTO collection");
            return validationResult;
        }
        for (PrefundPayrollTransactionDTO transaction : mTransactions) {
            // check the transaction amount
            if(transaction.getOriginalTransactionId() != null && transaction.validateTransaction(validationResult)){
                FinancialTransaction originalFinancialTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transaction.getOriginalTransactionId()));
                if(originalFinancialTransaction != null && originalFinancialTransaction.getPrefundingAchTransactionBalance().compareTo(transaction.getTransactionAmount()) > 0) {
                    validationResult.getMessages().AmountMustBeGreaterThan(EntityName.FinancialTransaction, mPayrollRun.getSourcePayRunId(), transaction.getTransactionAmount().toString(), originalFinancialTransaction.getPrefundingAchTransactionBalance().toString());
                }
            }

            // check related tax transaction amount if there is one
            if(transaction.getOriginalTaxTransactionId() != null && transaction.validateTaxTransction(validationResult)){
                FinancialTransaction originalTaxTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transaction.getOriginalTaxTransactionId()));
                if(originalTaxTransaction != null && originalTaxTransaction.getPrefundingAchTransactionBalance().compareTo(transaction.getTaxTransactionAmount()) > 0) {
                    validationResult.getMessages().AmountMustBeGreaterThan(EntityName.FinancialTransaction, mPayrollRun.getSourcePayRunId(), transaction.getTaxTransactionAmount().toString(), originalTaxTransaction.getPrefundingAchTransactionBalance().toString());
                }
            }
        }

        return validationResult;
    }

    public ProcessResult<DomainEntitySet<FinancialTransaction>> process() {
        mProcessResult = new ProcessResult<DomainEntitySet<FinancialTransaction>>();
        DomainEntitySet<FinancialTransaction> createdTransactions = new DomainEntitySet<FinancialTransaction>();

        for (PrefundPayrollTransactionDTO transaction : mTransactions) {
            FinancialTransaction transactionToCancel;
            FinancialTransaction taxTransactionToCancel = null;
            // find related ach transactions and cancel them
            transactionToCancel = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transaction.getOriginalTransactionId()));
            SpcfCalendar refundSettlementDate = transactionToCancel.getRefundTransactionSettlementDate();
            if(transaction.getOriginalTaxTransactionId() != null){
                taxTransactionToCancel = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transaction.getOriginalTaxTransactionId()));
            }

            // if the amount entered is greater than the amount refund the difference
            if(transaction.getTransactionAmount().compareTo(transactionToCancel.getPrefundingAchTransactionBalance()) > 0){
                TransactionTypeCode transactionTypeCode = null;
                switch (transactionToCancel.getTransactionType().getTransactionTypeCd()){
                    case EmployerDdDebit:
                        transactionTypeCode = TransactionTypeCode.EmployerDdRefundCredit;
                        break;
                    case EmployerFeeDebit:
                        transactionTypeCode = TransactionTypeCode.EmployerFeeRefundCredit;
                        break;
                }
                FinancialTransaction refund = createRefundTransaction(transactionToCancel, transactionTypeCode, (SpcfMoney)transaction.getTransactionAmount().subtract(transactionToCancel.getPrefundingAchTransactionBalance()), refundSettlementDate);
                if(refund != null){
                    createdTransactions.add(refund);
                }
            }
            if(taxTransactionToCancel != null && transaction.getTaxTransactionAmount().compareTo(taxTransactionToCancel.getPrefundingAchTransactionBalance()) > 0){
                FinancialTransaction refund = createRefundTransaction(taxTransactionToCancel, TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, (SpcfMoney)transaction.getTaxTransactionAmount().subtract(taxTransactionToCancel.getPrefundingAchTransactionBalance()), refundSettlementDate);
                if(refund != null){
                    createdTransactions.add(refund);
                }
            }

            // create the new non-ach transactions if needed
            CompanyBankAccount companyBankAccount = mPayrollRun
                    .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
            if(transactionToCancel.getPrefundingAchTransactionBalance().compareTo(ZERO) > 0){
                switch (transactionToCancel.getTransactionType().getTransactionTypeCd()){
                    case EmployerDdDebit:
                         CompanyService service = CompanyService.findCompanyService(mCompany, ServiceCode.DirectDeposit);
                        FinancialTransaction financialTransaction = FinancialTransaction.createERDebitTransaction(
                                mPayrollRun, companyBankAccount,
                                TransactionTypeCode.EmployerDdDebit,
                                transactionToCancel.getPrefundingAchTransactionBalance(),
                                mSettlementType,
                                mSettlementDate,
                                service);

                        // link up the non-ach transaction to the original ach transaction
                        financialTransaction.setOriginalTransaction(transactionToCancel);
                        if(taxTransactionToCancel != null) {
                            taxTransactionToCancel.addAssociatedTransactions(financialTransaction);
                        }

                        createdTransactions.add(financialTransaction);
                        break;
                    case EmployerFeeDebit:
                        BillingDetail billingDetail = null;
                        if(transactionToCancel.getBillingDetail() != null){
                            billingDetail = transactionToCancel.getBillingDetail();
                        }
                        else {
                            DomainEntitySet<FinancialTransaction> oldPrefundingTransactions =
                                    FinancialTransaction.findFinancialTransactions(mCompany, transactionToCancel, TransactionTypeCode.EmployerFeeDebit);
                            for (FinancialTransaction oldPrefundingTransaction : oldPrefundingTransactions) {
                                if(oldPrefundingTransaction.getBillingDetail() != null){
                                    billingDetail = oldPrefundingTransaction.getBillingDetail();
                                    break;
                                }
                            }
                        }

                        billingDetail = billingDetail.update(mPayrollRun, companyBankAccount, mSettlementType, mSettlementDate);

                        // link up the non-ach transaction to the original ach transaction
                        for (FinancialTransaction billingTransaction : billingDetail.getFinancialTransactionCollection()) {
                            if(billingTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeDebit){
                                billingTransaction.setOriginalTransaction(transactionToCancel);
                                if(taxTransactionToCancel != null) {
                                    taxTransactionToCancel.addAssociatedTransactions(billingTransaction);
                                }
                            } else if(billingTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.ServiceSalesAndUseTax){
                                billingTransaction.setOriginalTransaction(taxTransactionToCancel);
                                if(taxTransactionToCancel != null) {
                                    taxTransactionToCancel.addAssociatedTransactions(billingTransaction);
                                }
                            }
                        }

                        createdTransactions.addAll(billingDetail.getFinancialTransactionCollection());
                        break;
                }
            }

            // cancel the ach transactions
            if(transactionToCancel.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Cancelled){
                transactionToCancel.cancelFinancialTransaction();
            }
            if(taxTransactionToCancel != null && taxTransactionToCancel.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Cancelled){
                taxTransactionToCancel.cancelFinancialTransaction();
            }
        }

        // advance the non-ach (non-refund) transactions thru Executed to Completed
        TransactionState executedState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
        TransactionState completedState = Application.findById(TransactionState.class, TransactionStateCode.Completed);
        for (FinancialTransaction createdTransaction : createdTransactions) {
            if(createdTransaction.getSettlementTypeCd() != SettlementType.ACH){
                createdTransaction.addTransactionState(executedState);
                createdTransaction.addTransactionState(completedState);
            }
        }

        // create an event
        CompanyEvent.createPrefundingReceivedEvent(mCompany, mPayrollRun.getSourcePayRunId(), createdTransactions);

        // take the company off hold if there are no more pending payrolls        
        if(mCompany.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire) != null){
            DomainEntitySet<PayrollRun> pendingPayrolls = Application.find(PayrollRun.class, new Query<PayrollRun>()
                    .Where(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)
                    .And(PayrollRun.Company().equalTo(mCompany))
                    .And(PayrollRun.Id().notEqualTo(mPayrollRun.getId()))));
            if(pendingPayrolls == null || pendingPayrolls.size() == 0){
                PayrollServices.companyManager.removeOnHoldReason(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), ServiceSubStatusCode.PendingPrefundingWire);
                // remove on hold status from transctions we just created because they have not been commited yet
                for (FinancialTransaction createdTransaction : createdTransactions) {
                    if( createdTransaction.getOnHold()){
                        createdTransaction.updateOnHold(false);
                        Application.save(createdTransaction);
                    }
                }
            }
        }

        mProcessResult.setResult(createdTransactions);

        return mProcessResult;
    }

    private FinancialTransaction createRefundTransaction(FinancialTransaction orginalTranaction, TransactionTypeCode transactionType, SpcfMoney transactionAmount, SpcfCalendar settlementDate) {
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

        // this should never happen, but just in case we don't want a NPE
        if(companyBankAccount == null){
            mProcessResult.getMessages().ActiveBankAccountWarning(EntityName.FinancialTransaction, mPayrollRun.getSourcePayRunId(), TransactionTypeCode.EmployerDdRefundCredit.toString());
            return null;
        }

        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        // Add the financial transaction to the database
        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(
                mCompany, mPayrollRun, null, companyBankAccount.getBankAccount(),
                debitIntuitBankAccount.getBankAccount(), BankAccountOwnerType.Company, BankAccountOwnerType.Intuit,
                transactionType, transactionAmount, SettlementType.ACH, settlementDate);


        Collection<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransaction.setOriginalTransaction(orginalTranaction);
        orginalTranaction.addAssociatedTransactions(financialTransaction);
        financialTransactions.add(financialTransaction);

        TransactionResponse.createTransactionResponse(mCompany, financialTransactions, null);

        return financialTransaction;
    }
}
