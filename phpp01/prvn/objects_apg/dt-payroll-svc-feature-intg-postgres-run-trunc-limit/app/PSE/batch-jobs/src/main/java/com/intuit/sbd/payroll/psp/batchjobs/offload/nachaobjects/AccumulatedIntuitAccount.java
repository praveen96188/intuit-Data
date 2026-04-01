/*
 * $Id: //psp/dev/PSE/BatchJobs/src/com/intuit/sbd/payroll/psp/batchjobs/offload/nachaobjects/AccumulatedIntuitAccount.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload.nachaobjects;

import com.intuit.sbd.payroll.psp.domain.IntuitBankAccount;
import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the transactions for one particular accumulated Intuit account within one particular accumulated
 * Intuit batch.  There can be 0 or more debit and credit transactions per accumulated account due to the record-based
 * limitations of an entry detail record's amount 
 */
public class AccumulatedIntuitAccount {
    private static final SpcfMoney SPCF_MONEY_ZERO = new SpcfMoney("0");
    //The current credit and debit transactions amounts are added to
    protected AccumulatedIntuitTransaction mCurrentCreditTxn;
    protected AccumulatedIntuitTransaction mCurrentDebitTxn;
    IntuitBankAccount intuitBankAccount;
    private ArrayList<AccumulatedIntuitTransaction> allTransactions;

    /**
     * Create a new debit and credit accumulated transaction with 0 value
     * @param pIntuitBankAccount Bank account to create this accumulated Intuit account for
     */
    public AccumulatedIntuitAccount(IntuitBankAccount pIntuitBankAccount) {
        intuitBankAccount = pIntuitBankAccount;
        allTransactions = new ArrayList<AccumulatedIntuitTransaction>();
        createNewAccumulatedTxn(CreditDebitCode.Credit, SPCF_MONEY_ZERO);
        createNewAccumulatedTxn(CreditDebitCode.Debit, SPCF_MONEY_ZERO);
    }

    /**
     * Gets all Accumulated Transactions associated with this Accumulated Account.  MAY ONLY BE CALLED ONCE PER ACCUMULATEDACCOUNT
     *
     * @return Collection of Accumulated Transaction for this Accumualted Account
     */
    public Collection<AccumulatedIntuitTransaction> getTransactions() {
        //Put current credit and debit transactions in the Collection if they're non-zero
        saveCurrentTransactionIfExistsNonZero(CreditDebitCode.Credit);
        saveCurrentTransactionIfExistsNonZero(CreditDebitCode.Debit);
        return allTransactions;
    }

    /**
     * Adds an amount to either the credit or debit portion fo this account and passes in the entry detail record that
     * contributed the amount
     * @param pCreditDebitCode Whether we need to add this to the credit or debit amount
     * @param pAmount The amount to add
     */
    public void addAmount(CreditDebitCode pCreditDebitCode, SpcfMoney pAmount) {

        //Transaction we'll add amount to; either the current debit or credit txn for this account
        AccumulatedIntuitTransaction currentTransaction;

        //Get the current Debit or Credit txn
        currentTransaction = getCurrentTransaction(pCreditDebitCode);
        addAmountToTxn(currentTransaction, pAmount);
    }

    /**
     * Create a new accumulated txn if adding the amount to the txns current amount will cause
     * it to exceed the limit.  Otherwise, add the amount to the transaction's current amount
     * @param pTransaction Transaction to add the amount to
     * @param pAmount Amount to add
     */
    private void addAmountToTxn(AccumulatedIntuitTransaction pTransaction, SpcfMoney pAmount) {
        SpcfMoney newAmount = (SpcfMoney) pTransaction.getAmount().add(pAmount);
        boolean bNewAmountExceedsLimit = newAmount.compareTo(EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT) > 0;
        if (bNewAmountExceedsLimit) {
            createNewAccumulatedTxn(pTransaction.getCreditOrDebit(), pAmount);
        } else {
            pTransaction.setAmount(newAmount);
        }
    }

    /**
     * Create a new accumluated Intuit transaction with the specified amount
     * @param pCreditOrDebit
     * @param pAmount
     */
    private void createNewAccumulatedTxn(CreditDebitCode pCreditOrDebit, SpcfMoney pAmount) {
        //Save off existing transaction
        saveCurrentTransactionIfExistsNonZero(pCreditOrDebit);

        //Create a new Transaction with the passed amount
        AccumulatedIntuitTransaction tmpTransaction = new AccumulatedIntuitTransaction(intuitBankAccount, pAmount,
                pCreditOrDebit);

        //Save the new transaction to the current transaction by type (Credit or Debit)
        if (pCreditOrDebit.equals(CreditDebitCode.Credit)) {
            mCurrentCreditTxn = tmpTransaction;
        } else if (pCreditOrDebit.equals(CreditDebitCode.Debit)) {
            mCurrentDebitTxn = tmpTransaction;
        }
    }

    /**
     * Adds the credit or debit transaction to the master list of transactions for this account
     * @param creditOrDebit Whether we need to add the credit or debit transaction to the list of master transactions
     */
    private void saveCurrentTransactionIfExistsNonZero(CreditDebitCode creditOrDebit) {
        if (creditOrDebit.equals(CreditDebitCode.Credit)) {
            //Ensure current credit txn is not NULL and its amount is greater than zero
            if (mCurrentCreditTxn != null && (mCurrentCreditTxn.getAmount()).compareTo(SPCF_MONEY_ZERO) > 0) {
                allTransactions.add(mCurrentCreditTxn);
            }
        } else if (creditOrDebit.equals(CreditDebitCode.Debit)) {
            //Ensure current debit txn is not NULL and its amount is greater than zero
            if (mCurrentDebitTxn != null && (mCurrentDebitTxn.getAmount()).compareTo(SPCF_MONEY_ZERO) > 0) {
                allTransactions.add(mCurrentDebitTxn);
            }
        }
    }

    /**
     *
     * @param pCreditDebitCode Whether the credit or debit transaction is required
     * @return The Credit or Debit current transaction
     */
    private AccumulatedIntuitTransaction getCurrentTransaction(CreditDebitCode pCreditDebitCode) {
        if (CreditDebitCode.Credit.equals(pCreditDebitCode)) {
            return mCurrentCreditTxn;
        } else {
            return mCurrentDebitTxn;
        }
    }

}
