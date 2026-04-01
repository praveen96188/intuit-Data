package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 1/10/12
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddFinancialLedgerAdjustmentTransactionCore extends Process {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private LedgerAccountCode mDebitAccountCode;
    private LedgerAccountCode mCreditAccountCode;
    private SpcfMoney mTransactionAmount;
    private String mPayrollRunId;
    private String mLawId;
    private String mNoteText;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private LedgerAccount mDebitAccount;
    private LedgerAccount mCreditAccount;
    private Law mLaw;
    private final static SpcfLogger logger = Application.getLogger(AddFinancialLedgerAdjustmentTransactionCore.class);

    public AddFinancialLedgerAdjustmentTransactionCore(SourceSystemCode pSourceSystemCode,
                                                       String pSourceCompanyId,
                                                       LedgerAccountCode pDebitAccount,
                                                       LedgerAccountCode pCreditAccount, 
                                                       SpcfMoney pAmount, String pPayrollRunId, String pLawId, String pNoteText) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mDebitAccountCode = pDebitAccount;
        mCreditAccountCode = pCreditAccount;
        mTransactionAmount = pAmount;
        mPayrollRunId = pPayrollRunId;
        mLawId = pLawId;
        mNoteText = pNoteText;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (mDebitAccountCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Debit account is null");
            return validationResult;
        }
        mDebitAccount = Application.findById(LedgerAccount.class, mDebitAccountCode);

        if (mCreditAccountCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Credit account is null");
            return validationResult;
        }
        mCreditAccount = Application.findById(LedgerAccount.class, mCreditAccountCode);

        if (mNoteText == null) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "Note Text is null");
            return validationResult;
        }

        if((mDebitAccount.getRequiresQuarterLaw() || mCreditAccount.getRequiresQuarterLaw()) &&
                (mPayrollRunId == null || mLawId == null)) {
            validationResult.getMessages().FLARequiresQuarterLaw(EntityName.FinancialTransaction, mDebitAccountCode.toString(), mCreditAccountCode.toString());
            return validationResult;
        }
        
        if(mPayrollRunId != null) {
            mPayrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(mPayrollRunId));
            if(mPayrollRun == null) {
                validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, null, mPayrollRunId, mSourceSystemCode.toString(), mSourceCompanyId);
                return validationResult;
            }
            if (mPayrollRun.getPayrollRunStatus() == PayrollStatus.Pending) {
                validationResult.getMessages().FLACanNotAddedToPendingPayroll(EntityName.PayrollRun, mPayrollRunId);
                return validationResult;
            }
        }
        
        if(mLawId != null) {
            mLaw = Application.findById(Law.class, mLawId);
            if(mLaw == null) {
                validationResult.getMessages().LawDoesNotExist(EntityName.Law, mLawId);
                return validationResult;
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<FinancialTransaction> process() {
        ProcessResult<FinancialTransaction> processResult = new ProcessResult<FinancialTransaction>();
        boolean createPostingRules = false;

        String transactionTypeCd = "FLAd" + mDebitAccount.getAccountAbbreviation() + "c" + mCreditAccount.getAccountAbbreviation();
        TransactionTypeCode transactionTypeCode = null;
        TransactionType transactionType;
        try {
            transactionTypeCode = TransactionTypeCode.valueOf(transactionTypeCd);
        } catch (Exception e) {
            // ignore, transaction type may not exist
        }

        // transaction type does not exist, look for temp FLA transaction type
        if(transactionTypeCode == null){
            //This will give the temp Transaction type if it is assigned temporarily for this combination
            transactionType = TransactionType.findTransactionTypeByName(transactionTypeCd);

            //Did not find a temporary assigned transaction type, look for unused temp transaction Type to assign for this combination
            if(transactionType == null) {
                transactionType = TransactionType.getUnusedFLATransactionType();
                if(transactionType == null){
                    //Exit process with error - all temp transaction Types are used.
                    processResult.getMessages().CanNotCreateFLATransaction(EntityName.FinancialTransaction, mDebitAccountCode.toString(), mCreditAccountCode.toString());
                    return processResult;
                }
                createPostingRules = true;
            }
        } else {
            transactionType = TransactionType.findTransactionType(transactionTypeCode);
        }

        if(createPostingRules) {
            //Creating posting rule for debit Account and Executed
            PostingRule postingRule = new PostingRule();
            postingRule.setCreditDebitInd("D");
            postingRule.setTransactionType(transactionType);
            postingRule.setTransactionState(TransactionState.findTransactionState(TransactionStateCode.Executed));
            postingRule.setLedgerAccount(mDebitAccount);
            postingRule.setPostingRuleCd(SpcfUniqueId.generateRandomUniqueId().toString());
            Application.getHibernateSession().save(postingRule);

            //Creating posting rule for Credit Account and Executed
            postingRule = new PostingRule();
            postingRule.setCreditDebitInd("C");
            postingRule.setTransactionType(transactionType);
            postingRule.setTransactionState(TransactionState.findTransactionState(TransactionStateCode.Executed));
            postingRule.setLedgerAccount(mCreditAccount);
            postingRule.setPostingRuleCd(SpcfUniqueId.generateRandomUniqueId().toString());
            Application.getHibernateSession().save(postingRule);

            //Creating posting rule for debit Account and Voided
            postingRule = new PostingRule();
            postingRule.setCreditDebitInd("C");
            postingRule.setTransactionType(transactionType);
            postingRule.setTransactionState(TransactionState.findTransactionState(TransactionStateCode.Voided));
            postingRule.setLedgerAccount(mDebitAccount);
            postingRule.setPostingRuleCd(SpcfUniqueId.generateRandomUniqueId().toString());
            Application.getHibernateSession().save(postingRule);

            //Creating posting rule for Credit Account and Voided
            postingRule = new PostingRule();
            postingRule.setCreditDebitInd("D");
            postingRule.setTransactionType(transactionType);
            postingRule.setTransactionState(TransactionState.findTransactionState(TransactionStateCode.Voided));
            postingRule.setLedgerAccount(mCreditAccount);
            postingRule.setPostingRuleCd(SpcfUniqueId.generateRandomUniqueId().toString());
            Application.getHibernateSession().save(postingRule);

            //Update temp Transaction Type to indicate that it is assigned for this combination
            transactionType.setName(transactionTypeCd);
            Application.getHibernateSession().save(transactionType);

            //Send email for using Temporary Transaction type for this combination
            logger.warn(String.format("Temporary FLA Transaction Type (%s) has been used for Debit Ledger Account: %s, Credit Ledger account:%s. Need to create new Transaction Type:%s",
                                        transactionType.getTransactionTypeCd(), mDebitAccountCode.toString(), mCreditAccountCode.toString(), transactionTypeCd));
        }

        SpcfCalendar todaysDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(todaysDate);
        //If mPayrollRun is not passed, create new payrollRun
        if(mPayrollRun == null){
            mPayrollRun = PayrollRun.createAdjustmentPayrollRun(mCompany, todaysDate);
            //Update Payroll run status to Complete
            mPayrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        }

        //Create Financial Transaction
        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(mCompany, mPayrollRun, null, null, null, null, null, transactionType.getTransactionTypeCd(), mTransactionAmount, SettlementType.Other, todaysDate, mLaw);

        //Update Financial transaction state to Executed
        financialTransaction.addTransactionState(TransactionState.findTransactionState(TransactionStateCode.Executed));
        Application.save(financialTransaction);

        processResult.setResult(financialTransaction);

        //Creating company Event
        CompanyEvent.createAccountingFinancialLedgerAdjustmentEvent(mCompany, mPayrollRun.getSourcePayRunId(), transactionType.getTransactionTypeCd(), mTransactionAmount, mNoteText);

        return processResult;
    }
}
