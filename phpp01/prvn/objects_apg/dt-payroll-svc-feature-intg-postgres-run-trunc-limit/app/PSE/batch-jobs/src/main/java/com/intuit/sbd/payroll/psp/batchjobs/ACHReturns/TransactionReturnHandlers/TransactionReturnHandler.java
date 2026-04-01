package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnStatusCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 25, 2008
 * Time: 2:54:47 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TransactionReturnHandler {

    /**
     * Returns true if this instance is able to handle the given TransactionReturn, else false.
     *
     * @param pTxnReturn the TransactionReturn
     * @return whether this instance can handle the given TransactionReturn
     */
    public abstract boolean meetsCriteria(TransactionReturn pTxnReturn);

    /**
     * Handles the TransactionReturn.
     *
     * @param pTxnReturn the TransactionReturn
     * @return the possibly-updated TransactionReturn
     */
    public abstract TransactionReturn execute(TransactionReturn pTxnReturn);

    /**
     * Returns the right TransactionReturnHandler instance to handle the given TransactionReturn.
     *
     * @param pTxnReturn TransactionReturn
     * @return An TransactionReturnHandler instance appropriate for the given TransactionReturn, or null if none is appropriate
     */
    static public TransactionReturnHandler getTransactionReturnHandler(TransactionReturn pTxnReturn) {
        // look for a handler that wants this return
        TransactionReturnHandler matchingHandler = null;
        for (TransactionReturnHandler handler : handlers) {
            // if this handler wants it...
            if (handler.meetsCriteria(pTxnReturn)) {
                if (matchingHandler == null) {
                    // this is the first handler to claim this return
                    matchingHandler = handler;
                } else {
                    // already found one... that's a problem
                    String msg = getDuplicateHandlerMessage(pTxnReturn);
                    logger.info(msg);
                    throw new RuntimeException(msg);
                }
            }
        }

        // if we didn't find any...
        if (matchingHandler == null) {
            String msg = "No NocReturnHandler found for TransactionReturn "+pTxnReturn.getId()+", bank return code "+pTxnReturn.getBankReturnCd();
            logger.info(msg);
            throw new RuntimeException(msg);
        }

        return matchingHandler;
    }

    static protected String getDuplicateHandlerMessage(TransactionReturn pTxnReturn) {
        String bankReturnCd = pTxnReturn.getBankReturnCd();
        String ftId = null;
        String ftTypeCd = null;

        // get as much info about this return as we can for use in the error message
        DomainEntitySet<FinancialTransaction> returnFTs;
        returnFTs = TransactionReturn.findFinancialTransaction(pTxnReturn);
        if (returnFTs!=null && returnFTs.size()>0 && returnFTs.get(0)!=null) {
            FinancialTransaction ft = returnFTs.get(0);
            ftId = ft.getId().toString();
            ftTypeCd = ft.getTransactionType().getTransactionTypeCd().toString();
        }

        // build the error message
        MessageList msgList = new MessageList();
        msgList.MorethanOneHandlerMatchesCriteria(EntityName.FinancialTransaction, bankReturnCd, ftTypeCd, ftId);
        return msgList.get(0).toString();
    }

    /**
     * These are the handlers
     */
    private static final TransactionReturnHandler[] handlers = new TransactionReturnHandler[] {
        new C01NoticeOfChangeReturn(),      // Bank account number NOC
        new C02NoticeOfChangeReturn(),      // Bank account routing number NOC
        new C03NoticeOfChangeReturn(),      // Bank account number and routing number NOC
        new C04NoticeOfChangeReturn(),      // Bank account name NOC
        new C05NoticeOfChangeReturn(),      // Bank account type code NOC
        new C06NoticeOfChangeReturn(),      // Bank account number and type code NOC
        new C07NoticeOfChangeReturn(),      // Bank account number, routing number and type code NOC
        new RejectReturnHandler(),          // all Reject ("R") reasons
    };

    private static SpcfLogger logger = Application.getLogger(TransactionReturnHandler.class);

    /**
     * Updates the TransactionReturn's status to Resolved.
     *
     * @param pTxnReturn
     * @return
     */
    protected TransactionReturn resolveACHReturn(TransactionReturn pTxnReturn) {
        return pTxnReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Resolved);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
