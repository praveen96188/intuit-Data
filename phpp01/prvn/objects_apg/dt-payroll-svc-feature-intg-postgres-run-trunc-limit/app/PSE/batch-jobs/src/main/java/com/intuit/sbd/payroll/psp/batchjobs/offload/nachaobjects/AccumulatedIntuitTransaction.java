/*
 * $Id: //psp/dev/PSE/BatchJobs/src/com/intuit/sbd/payroll/psp/batchjobs/offload/nachaobjects/AccumulatedIntuitTransaction.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload.nachaobjects;

import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.IntuitBankAccount;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;

/**
 * This class represents an Accumulated Intuit Transaction for a particular Intuit bank account.  It is either a credit or a debit
 * and its amount is the accumulated amount of many "individual" ER/EE (non-Intuit) transactions.  These individual transactions
 * must be updated with the trace number of the accumulated Intuit transaction
 *
 * @author Dawn Martens
 */
public class AccumulatedIntuitTransaction {
    private SpcfMoney amount;
    private IntuitBankAccount intuitBankAccount;
    private CreditDebitCode creditOrDebit;
    private String traceNumber;

    /**
     * Constructor initializes AccumulatedIntuitTransaction
     * @param pIntuitBankAccount Bank Account this transaction is for
     * @param pAmount Intial amount for this transaction
     * @param pCreditOrDebit Whether this transaction is a credit or debit
     */
    public AccumulatedIntuitTransaction(IntuitBankAccount pIntuitBankAccount, SpcfMoney pAmount,
                                        CreditDebitCode pCreditOrDebit) {
        amount = pAmount;
        creditOrDebit = pCreditOrDebit;
        intuitBankAccount = pIntuitBankAccount;
    }

    /**
     *
     * @return Whether this txn is a debit or credit
     */
    public CreditDebitCode getCreditOrDebit() {
        return creditOrDebit;
    }

    /**
     *
     * @return The total transaction amount
     */
    public SpcfMoney getAmount() {
        return amount;
    }

    /**
     * Sets the amount on the transaction
     * @param pAmount The amount to set on the transaction
     */
    public void setAmount(SpcfMoney pAmount) {
        amount = pAmount;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    /**
     *
     * @return Obtain the entry detail record data (minus trace number) for the accumulated transaction
     */
    public String getRecordData() {
        BankAccount bankAccountForRecordData = intuitBankAccount.getBankAccount();
        BigDecimal bdTxnAmount;

        bdTxnAmount = SpcfUtils.convertToBigDecimal(amount);

        String recordData = "";
        recordData += EntryDetailRecord.RECORD_TYPE_CODE;
        recordData += EntryDetailRecord
                .getTransactionCode(creditOrDebit, bankAccountForRecordData.getACHAccountTypeCd());
        recordData += bankAccountForRecordData.getRoutingNumber();
        recordData += StringFormatter.formatString(bankAccountForRecordData.getAccountNumber(),
                EntryDetailRecord.ACCOUNT_NUMBER_LENGTH);
        recordData += StringFormatter.formatCurrencyNoDecimalPoint(bdTxnAmount, EntryDetailRecord.AMOUNT_LENGTH);
        //Individual ID is Intuit bank account number
        recordData += StringFormatter.formatString(bankAccountForRecordData.getAccountNumber(),
                EntryDetailRecord.INDIVIDUAL_ID_LENGTH);
        //Indidividual name is the Intuit bank account description
        recordData += StringFormatter
                .formatString(intuitBankAccount.getDescription(), EntryDetailRecord.INDIVIDUAL_NAME_LENGTH);
        recordData += EntryDetailRecord.DEFAULT_DISCRETIONARY_DATA;
        recordData += EntryDetailRecord.ADDENDA_RECORD_INDICATOR_N;
        return recordData;
    }
}
