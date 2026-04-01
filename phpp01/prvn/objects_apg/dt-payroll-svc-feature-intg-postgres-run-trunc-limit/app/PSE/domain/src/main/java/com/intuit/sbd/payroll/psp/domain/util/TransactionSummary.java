package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeGroupCode;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * This class is a set of balances representing a kind of "mini-ledger" for a group of related FinancialTransactions.
 *
 *
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Jun 3, 2008
 * Time: 9:55:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionSummary {
    public SpcfDecimal amtUncollected = SpcfDecimal.createInstance(0);
    public SpcfDecimal amtCollected = SpcfDecimal.createInstance(0);
    public SpcfDecimal amtRefunded = SpcfDecimal.createInstance(0);
    public SpcfDecimal amtWrittenOff = SpcfDecimal.createInstance(0);
    public SpcfDecimal amtRecovered = SpcfDecimal.createInstance(0);
    public SpcfDecimal amtPending = SpcfDecimal.createInstance(0);

    public String toString() {
        return "TransactionSummary Uncollected="+amtUncollected
                    + ", Collected="+amtCollected
                    + ", Pending="+amtPending                
                    + ", Refunded="+amtRefunded
                    + ", WrittenOff="+amtWrittenOff
                    + ", Recovered="+amtRecovered;
    }

    /**
     * Updates the appropriate TransactionSummary balances based on the FT's open/closed state and its
     * TransactionTypeGroupCode.
     *
     * @param pFT
     */
    public void updateBalances(FinancialTransaction pFT) {
        SpcfMoney closedAmount = (pFT.isClosed() ? pFT.getFinancialTransactionAmount() : new SpcfMoney("0"));
        SpcfMoney openAmount = (pFT.isOpen() ? pFT.getFinancialTransactionAmount() : new SpcfMoney("0"));
        SpcfMoney pendingAmount = (pFT.isPending() ? pFT.getFinancialTransactionAmount() : new SpcfMoney("0"));

        TransactionTypeGroupCode ftGroup = pFT.getTransactionType().getTransactionTypeGroupCd();
        switch (ftGroup) {
            case Debit:
                amtCollected = amtCollected.add(closedAmount);
                amtUncollected = amtUncollected.add(openAmount);
                amtPending = amtPending.add(pendingAmount);
                break;

            case Redebit:
                amtUncollected = amtUncollected.subtract(closedAmount);
                amtCollected = amtCollected.add(closedAmount);
                amtPending = amtPending.add(pendingAmount);
                break;

            case Credit:
                amtCollected = amtCollected.subtract(closedAmount);
                amtRefunded = amtRefunded.add(closedAmount);
                amtPending = amtPending.add(pendingAmount);
                break;

            case Recredit:
                amtCollected = amtCollected.subtract(closedAmount);
                amtRefunded = amtRefunded.add(closedAmount);
                break;

            case Writeoff:
                amtUncollected = amtUncollected.subtract(closedAmount);
                amtWrittenOff = amtWrittenOff.add(closedAmount);
                break;

            case Recovery:
                amtWrittenOff = amtWrittenOff.subtract(closedAmount);
                amtRecovered = amtRecovered.add(closedAmount);
                break;

            case EscalationOrFraud:
                amtUncollected = amtUncollected.add(closedAmount);
                amtCollected = amtCollected.subtract(closedAmount);
                break;

            case CustomerRecovery:
                amtUncollected = amtUncollected.add(closedAmount);
                break;
            case SUIPayments:
                break;
            default:
                // if we get here, we don't know how to handle this transaction in a summary, so the summary is probably wrong
                throw new IllegalArgumentException("FT has unexpected/unsupported TransactionTypeGroupCd \"" + ftGroup +
                        "\", type is " + pFT.getTransactionType().getTransactionTypeCd() + ".");
        }
    }
}
