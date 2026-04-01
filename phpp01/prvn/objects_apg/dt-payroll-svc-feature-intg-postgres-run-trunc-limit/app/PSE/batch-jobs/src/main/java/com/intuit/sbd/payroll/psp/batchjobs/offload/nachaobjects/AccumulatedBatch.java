/*
 * $Id: //psp/dev/PSE/BatchJobs/src/com/intuit/sbd/payroll/psp/batchjobs/offload/nachaobjects/AccumulatedBatch.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload.nachaobjects;

import com.intuit.sbd.payroll.psp.domain.IntuitBankAccount;
import com.intuit.sbd.payroll.psp.domain.NACHABatchType;
import com.intuit.sbd.payroll.psp.domain.NACHAFileType;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * An accumulated batch contains many Accumulated Intuit Accounts.  It extends the behavior of the base Batch with
 * getters and setters for the Accumulated Intuit Accounts associated with the batch
 * @author Dawn Martens
 */
public class AccumulatedBatch extends Batch {
    private static String INTUIT_ACCUM_BATCH_COMPANY_NAME;
    private static String INTUIT_ACCUM_BATCH_PAYROLL_ID;

    private TreeMap<IntuitBankAccount, AccumulatedIntuitAccount> accumulatedAccounts;

    static {
        //populate system parameters
        INTUIT_ACCUM_BATCH_COMPANY_NAME = SystemParameter
                .findSystemParameter(SystemParameter.Code.JPMC_ACCUM_BATCH_COMPANY_NAME).getSystemParameterValue();
        INTUIT_ACCUM_BATCH_PAYROLL_ID = SystemParameter
                .findSystemParameter(SystemParameter.Code.JPMC_ACCUM_BATCH_PAYROLL_ID).getSystemParameterValue();
    }

    /**
     * Initializes the base Batch and initializes the map of IntuitBankAccounts to AccumulatedIntuitAccounts
     * @param pBatchType Batch type
     * @param pFileType File type this accumulated batch will be in
     * @param pBatchDate Offload date for this batch.  The actual date on the batch is calculated based on file type and transaction type
     * @param pBatchTotals Batch totals for this batch
     */
    public AccumulatedBatch(NACHABatchType pBatchType, NACHAFileType pFileType, SpcfCalendar pBatchDate, NACHATotals pBatchTotals, String pStandardEntryDescription) {
        super(pBatchType, INTUIT_ACCUM_BATCH_COMPANY_NAME, INTUIT_ACCUM_BATCH_PAYROLL_ID, null, pBatchDate, pFileType, pBatchTotals, pStandardEntryDescription);
        accumulatedAccounts = new TreeMap<IntuitBankAccount, AccumulatedIntuitAccount>(new Comparator<IntuitBankAccount>() {
            public int compare(IntuitBankAccount pAcctOne, IntuitBankAccount pAcctTwo) {
                return pAcctOne.getId().compareTo(pAcctTwo.getId());
            }
        });
    }

    public Collection<AccumulatedIntuitAccount> getAccumulatedAccounts() {
        return accumulatedAccounts.values();
    }

    public void addAccumulatedAccount(AccumulatedIntuitAccount pAccumulatedAccount,
                                      IntuitBankAccount pIntuitBankAccount) {
        accumulatedAccounts.put(pIntuitBankAccount, pAccumulatedAccount);
    }

    public AccumulatedIntuitAccount getAccumulatedIntuitAccount(IntuitBankAccount pIntuitBankAccount) {
        return accumulatedAccounts.get(pIntuitBankAccount);
    }
}
