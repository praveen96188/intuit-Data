package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.tangosol.coherence.component.util.daemon.queueProcessor.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 23, 2009
 * Time: 10:36:45 AM
 */
public class CancelTransactionsBillPayment extends Process implements IProcess {
    private static final SpcfMoney ZERO = new SpcfMoney("0.00");
    private static final SpcfLogger logger = PayrollServices.getLogger(CancelTransactionsBillPayment.class);

    private Company mCompany = null;
    private HashMap<String, Integer> mBillPaymentsPerPayrollRun = new HashMap<String, Integer>();
    private HashMap<PayrollRun, ArrayList<FinancialTransaction>> mPayrollRuns = new HashMap();
    private List<FinancialTransaction> txnResponseList = new Vector<FinancialTransaction>(100, 100);
    private ArrayList<BillPayment> mBillPayments = new ArrayList<BillPayment>();
    private Collection<String> mBillPaymentIds;
    private SourceSystemCode mSourceSystemCd = null;
    private String mCompanyId = null;
    private String msessionId=null;
    
    private FinancialTransaction mOriginalErTxn;
    private boolean mHasPrefundingTransactions = false;

    public CancelTransactionsBillPayment(SourceSystemCode pSourceSystemCd, String pCompanyId,
                                         Collection<String> pBillPaymentIds, String psessionId) {
        mSourceSystemCd = pSourceSystemCd;
        mCompanyId = pCompanyId;
        mBillPaymentIds = pBillPaymentIds;
        msessionId=psessionId;
    }

    public List<FinancialTransaction> getFinancialTransactions() {
        return txnResponseList;
    }

    public Collection<PayrollRun> getPayrollRuns() {
        return mPayrollRuns.keySet();
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        // validate the source system code
        if (mSourceSystemCd == null) {
            validationResult.getMessages().SourceSystemCdNotSpecified(EntityName.SourceSystem,
                    mCompanyId);
        }

        // validate the company id
        if ((mCompanyId == null) || !Validator.isValidLength(mCompanyId, 1, 50)) {
            validationResult.getMessages().CompanyIdNotSpecified(EntityName.Company, mCompanyId);
        }


        if (!validationResult.isSuccess()) {
            return validationResult;
        }


        // retrieve the company
        mCompany = Company.findCompany(mCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(
                    EntityName.Company,
                    mCompanyId,
                    mSourceSystemCd.toString(),
                    mCompanyId);
            return validationResult;
        }

        // Check if Company is Active on Bill Payment Service

        if (!mCompany.isCompanyOnService(ServiceCode.BillPayment)) {
            validationResult.getMessages().CompanyDoesNotExistOnService(EntityName.Company, mCompanyId,
                    mSourceSystemCd.toString(), mCompanyId, ServiceCode.BillPayment.toString());
        }

        // Validate Bill Payments exist
        for (String paymentId : mBillPaymentIds) {
            BillPayment billPayment = BillPayment.findBillPaymentBySourceId(mCompany, paymentId);
            if (billPayment != null) {
                if (billPayment.getStatus() == BillPaymentStatusCode.Inactive) {
                    validationResult.getMessages().PaycheckAlreadyCanceled(EntityName.BillPayment, paymentId, paymentId);
                } else {
                    mBillPayments.add(billPayment);
                }
            } else {
                validationResult.getMessages().BillPaymentDoesNotExist(EntityName.BillPayment, paymentId, mSourceSystemCd.toString(), mCompanyId, paymentId);
                return validationResult;
            }

        }

        if (mBillPayments != null && mBillPayments.size() > 0) {
            for (BillPayment billPayment : mBillPayments) {
                // Build a list of payroll runs for the bill payments and corresponding transaction List
                PayrollRun payrollRun = billPayment.getPayrollRun();
                DomainEntitySet<FinancialTransaction> finTxnList;

                // Specific validations for Cancel
                // If the payroll status is not PENDING or OFFLOADED_DEBIT then we cannot proceed.
                if (Application.getCurrentPrincipal().isAgent()) {

                    ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class,
                            ActionEventCode.DDTransactionCancel);
                    PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus();
                    if (!payrollRun.validateAction(actionEvent)) {
                        validationResult.getMessages().ActionNotValidForPayrollRun(
                                EntityName.PayrollRun,
                                payrollRun.getSourcePayRunId(),
                                actionEvent.getCode().toString(),
                                payrollRun.getSourcePayRunId(),
                                payrollStatus.toString());
                        return validationResult;
                    }

                } else { // specific validations for Recall
                    if (!mCompany.isAllowedCapability(SystemCapabilityCode.RecallPayment)) {
                        validationResult.getMessages().CompanyOperationNotAllowed(
                                mCompany.getSourceSystemCd().toString(),
                                mCompany.getSourceCompanyId(), SystemCapabilityCode.RecallPayment.toString());
                        return validationResult;

                    }
                    for (FinancialTransaction finTxn : payrollRun.getFinancialTransactionCollection()) {
                        if (finTxn.calculateCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed) {
                            validationResult.getMessages().PayrollRunAlreadyOffloaded(EntityName.PayrollRun,
                                    payrollRun.getSourcePayRunId(), payrollRun.getSourcePayRunId(),
                                    mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId());
                            return validationResult;
                        }
                    }

                    // if a prefunding transaction has been added we do not allow a customer to edit the payroll
                    DomainEntitySet<FinancialTransaction> nonAchDebits = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                            .Where(FinancialTransaction.PayrollRun().equalTo(payrollRun)
                            .And(FinancialTransaction.SettlementTypeCd().notEqualTo(SettlementType.ACH))
                            .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                            .Or(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)))));
                    if (nonAchDebits != null && nonAchDebits.size() > 0 && payrollRun.getPayrollRunStatus() == PayrollStatus.Pending) {
                        validationResult.getMessages().RecallTransactionsWithPrefundingRecorded(
                                EntityName.PayrollRun,
                                payrollRun.getSourcePayRunId(),
                                payrollRun.getSourcePayRunId(),
                                mCompany.getSourceSystemCd().toString(),
                                mCompany.getSourceCompanyId());
                        return validationResult;
                    }
                }

                if (!mPayrollRuns.containsKey(payrollRun)) {
                    mPayrollRuns.put(payrollRun, new ArrayList<FinancialTransaction>());
                    mBillPaymentsPerPayrollRun.put(payrollRun.getId().toString(), 1);
                } else {
                    Integer numberOfBillPayments = mBillPaymentsPerPayrollRun.get(payrollRun.getId().toString());
                    numberOfBillPayments++;
                    mBillPaymentsPerPayrollRun.put(payrollRun.getId().toString(), numberOfBillPayments);

                }
                if (billPayment.getBillPaymentSplitCollection() != null && !billPayment.getBillPaymentSplitCollection().isEmpty()) {
                    for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                        // If any EE DD transactions to be cancelled are not in a CREATED state, we cannot proceed.
                        if (!billPaymentSplit.getFinancialTransaction().calculateCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created)) {
                            validationResult.getMessages().TransactionNoLongerPendingCannotCancel(
                                    EntityName.FinancialTransaction,
                                    billPayment.getSourceId(),
                                    billPayment.getSourceId());
                        }
                        ArrayList cancelTransactionList = mPayrollRuns.get(payrollRun);
                        cancelTransactionList.add(billPaymentSplit.getFinancialTransaction());
                    }
                }


            }
            for (PayrollRun payrollRun : mPayrollRuns.keySet()) {
                for (FinancialTransaction finTxn : payrollRun.getFinancialTransactionCollection()) {
                    if (finTxn.calculateCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Created) {
                        continue;
                    }
                    MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();

                    if (null == mmTxn) {
                        continue;
                    }

                    SpcfCalendar limitCalendar =
                            mCompany.getOffloadGroup().getCalendarForCutoffTime(
                                    mmTxn.getInitiationDate().toLocal());

                    // Check to see if it's after the the cutoff date/time
                    if (!PSPDate.getPSPTime().before(limitCalendar)) {
                        validationResult.getMessages().PayrollRunAlreadyOffloaded(
                                EntityName.PayrollRun,
                                payrollRun.getSourcePayRunId(),
                                payrollRun.getSourcePayRunId(),
                                mSourceSystemCd.toString(),
                                mCompanyId);
                        return validationResult;
                    }
                }
            }
        }

        // Todo Verify prefunding
        return validationResult;
    }

    public ProcessResult<Collection<PayrollRun>> process() {
        ProcessResult processResult = new ProcessResult();
        for (PayrollRun payrollRun : mPayrollRuns.keySet()) {

            ArrayList<FinancialTransaction> cancelTransactionList = mPayrollRuns.get(payrollRun);
            // Only do processing if there are payee txn's to cancel...
            if (cancelTransactionList.size() > 0) {

                Integer remainTxnCount = 0;
                SpcfDecimal totalTxnAmt = ZERO;
                SpcfDecimal cancelTxnAmt = ZERO;

                FinancialTransaction newErDdDrTxn;
                // For each payroll run in the list, cancel transactions and fees

                // Calculate the sum of EE DD CR transactions in a CREATED state (if any).
                DomainEntitySet<FinancialTransaction> eeDdCrTxnList =
                        payrollRun.getFinancialTransactions(
                                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                                new TransactionStateCode[]{TransactionStateCode.Created});

                if (eeDdCrTxnList != null) {
                    for (FinancialTransaction txn : eeDdCrTxnList) {
                        totalTxnAmt = totalTxnAmt.add(txn.getFinancialTransactionAmount());
                    }
                }

                Map<SpcfUniqueId, BillPayment> billPaymentMap = new HashMap<SpcfUniqueId, BillPayment>();

                // Cancel EE transactions and calculate the sum of the cancelled transactions.
                for (FinancialTransaction txn : cancelTransactionList) {
                    cancelTxnAmt = cancelTxnAmt.add(txn.getFinancialTransactionAmount());
                    txn.cancelFinancialTransaction();
                    BillPayment billPayment = txn.getBillPaymentSplit().getBillPayment();
                    billPayment.setStatus(BillPaymentStatusCode.Inactive);
                    billPayment.setSessionId(msessionId);
                    if (!Application.getCurrentPrincipal().isAgent()) {
                        CompanyEvent.createBillPaymentEvent(mCompany,billPayment.getSourceId(), billPayment.getId(), EventTypeCode.BillPaymentRecalled);
                    }

                    //save session id
                    Application.save(billPayment);

                    if (!billPaymentMap.containsKey(billPayment.getId())) {
                        billPaymentMap.put(billPayment.getId(), billPayment);
                    }

                    txnResponseList.add(txn);
                }

                PayrollStatus mPayrollStatus = payrollRun.getPayrollRunStatus();
                for (BillPayment billPayment : billPaymentMap.values()) {
                    if (Application.getCurrentPrincipal().isAgent() && PayrollStatus.OffloadedDebit.equals(mPayrollStatus)) {
                        DomainEntitySet<FinancialTransaction> erTransactions = payrollRun.getFinancialTransactions(
                                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                                new TransactionStateCode[]{TransactionStateCode.Created});

                        // Save the ERDDDR txn for later processing in the process step...
                        if (erTransactions.size() > 1) {
                            FinancialTransaction ft = billPayment.getBillPaymentSplitCollection().get(0).getFinancialTransaction();
                            mOriginalErTxn = ft.getRelatableTransaction();
                        } else {
                            mOriginalErTxn = erTransactions.get(0);
                        }

                        // Retrieve the active DD company bank account.
                        CompanyBankAccount companyBankAccount =
                                CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

                        if (companyBankAccount != null) {
                            // The refund must be allowed and offloadable for this company

                            if (mCompany.isAllowedCapability(SystemCapabilityCode.RefundOrCredit) &&
                                    TransactionType.isOffloadable(TransactionTypeCode.EmployerDdRefundCredit, null, mCompany, null)) {

                                IntuitBankAccount intuitRefundDebitBankAccount =
                                        IntuitBankAccount.findIntuitBankAccount(
                                                TransactionTypeCode.EmployerDdRefundCredit, CreditDebitCode.Debit);

                                SpcfCalendar newSettlementDate = mOriginalErTxn.getRefundTransactionSettlementDate();

                                // Create ER Fin Tx for the refund
                                //     - Tx Type is ERDDRFCR
                                //     - Tx State is CR
                                //     - Settlement date equals original ERDDDB Settlement_Date + (ACHWAITPD + 1) banking days
                                //     - Settlement type equals ACH
                                //     - Amount = the sum of the canceled EE DD CRs
                                //     - Co Bank Acct = original Company Bank account
                                //     - Associate with the original payroll
                                //     - Associate with the original financial transaction
                                //       (identifies the original financial transaction as having a refund against it)
                                //
                                // This new txn is refunding the totalAmountOfTaxToRecall from the intuit acct back to the client's acct.
                                newErDdDrTxn = FinancialTransaction.createFinancialTransaction(
                                        mCompany,
                                        payrollRun,
                                        null,                                                              // paycheck split
                                        companyBankAccount.getBankAccount(),                               // credit bank account
                                        intuitRefundDebitBankAccount.getBankAccount(),                     // debit bank account
                                        BankAccountOwnerType.Company,                                      // credit bank account type
                                        BankAccountOwnerType.Intuit,                                       // debit bank account type
                                        TransactionTypeCode.EmployerDdRefundCredit,                        // transaction type
                                        billPayment.getAmount(),            // transaction amount
                                        SettlementType.ACH,                                                // settlement type
                                        newSettlementDate);                                                // settlement date

                                txnResponseList.add(newErDdDrTxn);

                                // Associate new txn with the original txn
                                // (identifies the original financial transaction as having a refund against it)
                                // Transaction Type Code = ERDDDB, Transaction State Code = CP or EX.
                                newErDdDrTxn.setOriginalTransaction(mOriginalErTxn);
                                mOriginalErTxn.addAssociatedTransactions(newErDdDrTxn);

                                Application.save(newErDdDrTxn);
                            }
                        }
                    } else { // Either Recall or Cancel with Pending Payroll Status

                        // Cancel the existing ER debit transaction
                        if (!mHasPrefundingTransactions) {
                            DomainEntitySet<FinancialTransaction> erTransactions = payrollRun.getFinancialTransactions(
                                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                                    new TransactionStateCode[]{TransactionStateCode.Created});

                            if (erTransactions.size() > 1) {
                                FinancialTransaction ft = billPayment.getBillPaymentSplitCollection().get(0).getFinancialTransaction();
                                mOriginalErTxn = ft.getRelatableTransaction();
                            } else {
                                mOriginalErTxn = erTransactions.get(0);
                            }

                            mOriginalErTxn.cancelFinancialTransaction();
                            txnResponseList.add(mOriginalErTxn);
                        }
                    }
                }

                SpcfDecimal remainTxnAmt = totalTxnAmt.subtract(cancelTxnAmt);

                if (remainTxnAmt.compareTo(ZERO) != 0) {
                    // Get Active bill payments
                    Criterion<BillPayment> where = BillPayment.Status().equalTo(BillPaymentStatusCode.Active);
                    DomainEntitySet<BillPayment> activeBillPayments = payrollRun.getBillPaymentCollection().find(where);
                    remainTxnCount = activeBillPayments.size();
                }

                CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

                //Update the BillingManager with the number non cancelled payments. If the whole payroll is cancelled
                //then payments quantity will be zero.
                try {
                    CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.BillPayment);
                    BillingDetail.updateBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerPayment, remainTxnCount, companyOffering.getOffering().getOfferingCode());
                } catch (IllegalArgumentException iax) {
                    // if this exception is thrown, then there is no BillingDetail for a PerPaycheck fee that can be updated...
                    // this will happen if (a) no PerPaycheck fee was ever created, or (b) the fee has offloaded.
                    // either way, we consume the exception
                }

                payrollRun.setPayrollDirectDepositAmount(new SpcfMoney(remainTxnAmt));

                // Update the PayrollRun to reflect the changes.
                if (remainTxnAmt.compareTo(ZERO) == 0) {
                    // If ALL EE DD CR transactions for the payroll run were successfully cancelled,
                    // update the status of the payroll run to "Canceled".
                    payrollRun.setPayrollRunStatus(PayrollStatus.Canceled);
                    payrollRun.setStatusEffectiveDate(PSPDate.getPSPTime());
                    // Record Payroll Recalled Event if we recall entire payroll in a single call
                    if (!Application.getCurrentPrincipal().isAgent() && ((mBillPaymentIds == null) || (mBillPaymentIds.size() == 0))) {
                        CompanyEvent.createPayrollRunEvent(mCompany, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.PayrollRecalled);
                    }
                }

                payrollRun = Application.save(payrollRun);
            }
        }

        processResult.setResult(mPayrollRuns.keySet());

        return processResult;
    }
}