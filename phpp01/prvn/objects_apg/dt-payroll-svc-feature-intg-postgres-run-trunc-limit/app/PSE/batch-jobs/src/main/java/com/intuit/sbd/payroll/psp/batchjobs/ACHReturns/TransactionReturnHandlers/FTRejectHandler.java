package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;

/**
 * Base class for FinancialTransaction-specific handling of ACH Reject returns.
 *
 * User: wnichols
 * Date: Jun 6, 2008
 * Time: 12:23:08 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FTRejectHandler {

    /**
     * Returns true if this instance is able to handle the given FT for the given TransactionReturn, else false.
     *
     * @param pTxnReturn the TransactionReturn
     * @param pReturnedFT the FinancialTransaction
     * @return whether this instance can handle the given TransactionReturn
     */
    public abstract boolean meetsCriteria(TransactionReturn pTxnReturn, FinancialTransaction pReturnedFT);

    /**
     * Handles the FT for the given TransactionReturn.
     *
     * @param pTxnReturn the TransactionReturn
     * @param pReturnedFT the FinancialTransaction
     * @return true if the TransactionReturn may be resolved, else false
     */
    public abstract boolean execute(TransactionReturn pTxnReturn, FinancialTransaction pReturnedFT);

}
