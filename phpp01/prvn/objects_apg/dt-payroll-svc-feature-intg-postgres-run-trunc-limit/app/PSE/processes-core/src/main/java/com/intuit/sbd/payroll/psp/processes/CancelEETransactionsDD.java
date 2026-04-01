package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 23, 2009
 * Time: 10:36:45 AM
 */
public class CancelEETransactionsDD extends Process implements IProcess {
    private static final SpcfMoney ZERO = new SpcfMoney("0.00");
    private TransactionCancelEEDTO mDto = null;
    private Company mCompany = null;
    private PayrollRun mPayrollRun = null;
    private ArrayList<FinancialTransaction> mEeCancelTxnList = new ArrayList<FinancialTransaction>();
    private List<FinancialTransaction> txnResponseList = new Vector<FinancialTransaction>(10, 10);
    private DomainEntitySet<Paycheck> mPaychecks;
    private SourceSystemCode mSourceSystemCd = null;
    private String mCompanyId = null;
    private SpcfDecimal cancelTxnAmt = ZERO;
    private FinancialTransaction mOriginalErTxn;
    private boolean mHasPrefundingTransactions = false;
    public int mCancelledPaycheckCount = -1;

    public CancelEETransactionsDD(SourceSystemCode pSourceSystemCd, String pCompanyId,
                                  TransactionCancelEEDTO pDto, DomainEntitySet<Paycheck> pPaycheckCollection) {
        mSourceSystemCd = pSourceSystemCd;
        mCompanyId = pCompanyId;
        mDto = pDto;
        mPaychecks = pPaycheckCollection;
    }

    public List<FinancialTransaction> getFinancialTransactions() {
        return txnResponseList;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        mCompany = Company.findCompany(mCompanyId, mSourceSystemCd);
        mPayrollRun = PayrollRun.findPayrollRun(mCompany, mDto.getSourcePayrollRunId());

        if(mPaychecks != null && mPaychecks.size() > 0) {
            mCancelledPaycheckCount = 0;
            for(Paycheck paycheck : mPaychecks) {
                if (paycheck.getPaycheckSplitCollection() != null && !paycheck.getPaycheckSplitCollection().isEmpty()) {
                    boolean countedPaycheck = false;
                    for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
                        if(paycheckSplit.getFinancialTransaction() != null) {
                            if (!countedPaycheck && !paycheckSplit.getEmployeeBankAccount().getBankAccount().isPayCardAccount()) {
                                ++mCancelledPaycheckCount;
                                countedPaycheck = true;
                            }

                            // If any EE DD transactions to be cancelled are not in a CREATED state, we cannot proceed.
                            TransactionStateCode currentState = paycheckSplit.getFinancialTransaction().calculateCurrentTransactionState().getTransactionStateCd();
                            String sourcePaycheckID = paycheckSplit.getFinancialTransaction().getPaycheckSplit().getSourceDdTxnId();
                            if(currentState == TransactionStateCode.Cancelled) {
                                validationResult.getMessages().PaycheckAlreadyCanceled(EntityName.PayCheck, sourcePaycheckID, sourcePaycheckID);
                            } else if(currentState != TransactionStateCode.Created){
                                validationResult.getMessages().TransactionNoLongerPendingCannotCancel(
                                        EntityName.Paycheck,
                                        sourcePaycheckID,
                                        sourcePaycheckID);
                            }
                            mEeCancelTxnList.add(paycheckSplit.getFinancialTransaction());
                        }
                    }
                }
            }
        } else{
            // getting all paychecks to cancel/recall
            mEeCancelTxnList.addAll(FinancialTransaction.findPaycheckSplitFinancialTransactions(
                    mPayrollRun,
                    TransactionStateCode.Created));
        }

        // check if all of the prefunding transactions have been canceled
        DomainEntitySet<FinancialTransaction> nonAchDebits = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                .Where(FinancialTransaction.PayrollRun().equalTo(mPayrollRun)
                .And(FinancialTransaction.SettlementTypeCd().notEqualTo(SettlementType.ACH))
                .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                .Or(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)))));
        mHasPrefundingTransactions = nonAchDebits != null && nonAchDebits.size() > 0;
        if(nonAchDebits != null){
            for (FinancialTransaction nonAchErDebit : nonAchDebits) {
                if(nonAchErDebit.calculateCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Voided){
                    validationResult.getMessages().CancelEmployeeTransactionsWithPrefundingRecorded(
                            EntityName.PayrollRun,
                            mPayrollRun.getSourcePayRunId(),
                            mPayrollRun.getSourcePayRunId(),
                            mSourceSystemCd.toString(),
                            mCompanyId);
                    return validationResult;
                }
            }
        }

        if(mHasPrefundingTransactions){
            // if the payroll has canceled prefunding transactions then the only option is to cancel all of the transactions
            // and subsequently the entire payroll. We are not going to recalculate the debit.
            DomainEntitySet<FinancialTransaction> eeDdCredits =
                    mPayrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                            new TransactionStateCode[]{TransactionStateCode.Created});

            if(eeDdCredits.size() != mPaychecks.size()){
                validationResult.getMessages().CancelAllEmployeeTransactionsWithPrefundingRecorded(
                        EntityName.PayrollRun,
                        mPayrollRun.getSourcePayRunId(),
                        mPayrollRun.getSourcePayRunId(),
                        mSourceSystemCd.toString(),
                        mCompanyId);
                return validationResult;
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // Only do processing if there are ee txn's to cancel...
        if (mEeCancelTxnList.size() > 0) {

            SpcfDecimal remainTxnAmt;
            SpcfDecimal totalTxnAmt = ZERO;
            FinancialTransaction newErDdDrTxn;

            // Calculate the sum of  EE DD CR transactions in a CREATED state (if any).
            DomainEntitySet<FinancialTransaction> eeDdCrTxnList =
                    mPayrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                            new TransactionStateCode[]{TransactionStateCode.Created});

            if (eeDdCrTxnList != null) {
                for (FinancialTransaction txn : eeDdCrTxnList) {
                    totalTxnAmt = totalTxnAmt.add(txn.getFinancialTransactionAmount());
                }
            }

            // Cancel EE transactions and calculate the sum of the cancelled transactions.
            for (FinancialTransaction txn : mEeCancelTxnList) {
                cancelTxnAmt = cancelTxnAmt.add(txn.getFinancialTransactionAmount());
                txn.cancelFinancialTransaction();
                txnResponseList.add(txn);
            }

            // Calculate remaining amount
            remainTxnAmt = totalTxnAmt.subtract(cancelTxnAmt);
            PayrollStatus mPayrollStatus = mPayrollRun.getPayrollRunStatus();

            if (mDto.isAgentCancel() && PayrollStatus.OffloadedDebit.equals(mPayrollStatus)) {
                // Save the ERDDDR txn for later processing in the process step...
                mOriginalErTxn = mPayrollRun.getFinancialTransactions(
                                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                                new TransactionStateCode[]{TransactionStateCode.Executed}).get(0);
                // Retrieve the active DD company bank account.
                CompanyBankAccount companyBankAccount =
                        CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
                // todo: what heppens if company bank account is null
                if (companyBankAccount != null) {
                    // The refund must be allowed and offloadable for this company
                    //todo: what happens if capability not allowed
                    if (mCompany.isAllowedCapability(SystemCapabilityCode.RefundOrCredit) &&
                            TransactionType.isOffloadable(TransactionTypeCode.EmployerDdRefundCredit, null, mCompany, null)) {

                        IntuitBankAccount intuitRefundDebitBankAccount =
                                IntuitBankAccount.findIntuitBankAccount(
                                        TransactionTypeCode.EmployerDdRefundCredit, CreditDebitCode.Debit);

                       /* int achWaitPeriodDays = Integer.parseInt(param.getParameterValue());

                        SpcfCalendar newSettlementDate = mOriginalErTxn.getSettlementDate().toLocal().copy();
                        CalendarUtils.addBusinessDays(newSettlementDate, achWaitPeriodDays + 1);*/
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
                                mPayrollRun,
                                null,                                           // paycheck split
                                companyBankAccount.getBankAccount(),            // credit bank account
                                intuitRefundDebitBankAccount.getBankAccount(),  // debit bank account
                                BankAccountOwnerType.Company,                   // credit bank account type
                                BankAccountOwnerType.Intuit,                    // debit bank account type
                                TransactionTypeCode.EmployerDdRefundCredit,     // transaction type
                                (SpcfMoney)cancelTxnAmt,     // transaction amount
                                SettlementType.ACH,                             // settlement type
                                newSettlementDate);                             // settlement date

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
                if(!mHasPrefundingTransactions){
                    mOriginalErTxn = mPayrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[]{TransactionStateCode.Created}).get(0);
                    mOriginalErTxn.cancelFinancialTransaction();
                    txnResponseList.add(mOriginalErTxn);
                }

                CompanyBankAccount companyBankAccount =
                        CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

                // If there are still DD txs remaining for the Payroll in question, create a new er debit txn
                // Amount = sum of remaining financial transactions where State = CR and Type = EEDDCR.
                // Same settlement date as the original ERDDDR transaction.
                if (remainTxnAmt.compareTo(ZERO) != 0) {
                    newErDdDrTxn = FinancialTransaction.createFinancialTransaction(
                            mCompany,
                            mPayrollRun,
                            null,                                           // paycheck split
                            mOriginalErTxn.getCreditBankAccount(),          // credit bank account
                            companyBankAccount.getBankAccount(),            // debit bank account
                            mOriginalErTxn.getCreditBankAccountType(),      // credit bank account type
                            BankAccountOwnerType.Company,                   // debit bank account type
                            TransactionTypeCode.EmployerDdDebit,            // transaction type
                            (SpcfMoney)remainTxnAmt,     // transaction amount
                            SettlementType.ACH,                             // settlement type
                            mOriginalErTxn.getSettlementDate().toLocal().copy());     // settlement date

                    txnResponseList.add(newErDdDrTxn);
                }

                //Update the BillingManager with the number non cancelled pay checks. If the whole payroll is cancelled
                //then paycheck quantity will be zero.
                try {
                    CompanyOffering companyOffering = mPayrollRun.getCompany().getOffering(ServiceCode.DirectDeposit);

                    int quantity = 0;
                    if(mCancelledPaycheckCount > -1) {
                        for (BillingDetail candidate : BillingDetail.findBillingDetails(mPayrollRun, OfferingServiceChargeType.DirectDepositFee)) {
                            if (candidate.isUpdateable()) {
                                quantity = candidate.getQuantity() - mCancelledPaycheckCount;
                                break;
                            }
                        }
                    }
                    BillingDetail.updateBillingDetail(mPayrollRun, companyBankAccount, OfferingServiceChargeType.DirectDepositFee, quantity, companyOffering.getOffering().getOfferingCode());
                } catch (IllegalArgumentException iax) {
                    // if this exception is thrown, then there is no BillingDetail for a PerPaycheck fee that can be updated...
                    // this will happen if (a) no PerPaycheck fee was ever created, or (b) the fee has offloaded.
                    // either way, we consume the exception
                }
            }

            mPayrollRun.setPayrollDirectDepositAmount(new SpcfMoney(remainTxnAmt));
            mPayrollRun = Application.save(mPayrollRun);
        }

        return processResult;
    }
}
