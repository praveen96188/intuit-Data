package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Handles C04 (account name) ACH transaction returns.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: rkrishna, wnichols
 * Date: Feb 26, 2008
 * Time: 3:33:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class C04NoticeOfChangeReturn extends NocReturnHandler {

    private static SpcfLogger logger = Application.getLogger(C04NoticeOfChangeReturn.class);

    /**
     * "C04" implementation of TransactionReturnHandler.meetsCriteria().
     *
     * @param pTxnReturn
     * @return true when the TransactionReturn's bank return code is "C04"
     */
    public boolean meetsCriteria(TransactionReturn pTxnReturn) {
        return "C04".equals(pTxnReturn.getBankReturnCd());
    }

    /**
     * Field-specific implementation of NocReturnHandler.fieldsAreValid().
     * We don't do anything about name updates.
     *
     * @param pTxnReturn
     * @return
     */
    protected boolean fieldsAreValid(TransactionReturn pTxnReturn) {
        // don't care about the name
        return true;
    }

    /**
     * Field-specific implementation of NocReturnHandler.updateFields().
     * We don't update the name, so this method does nothing.
     *
     * @param pTxnReturn
     * @param pBankAccount
     */
    protected void updateFields(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        // do nothing for the name
    }

    /**
     * We don't update the bank account for name changes, nor do we resolve the TransactionReturn.
     * All that remains is the system event.
     *
     * @param pTxnReturn
     * @return
     */
    public TransactionReturn execute(TransactionReturn pTxnReturn) {
        pTxnReturn = Application.findById(TransactionReturn.class, pTxnReturn.getId());
        pTxnReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Open);

        boolean resolveNOC = true;
        
        // see whether the non-Intuit bank account associated with this return should be updated
        BankAccount bankAccount = getBankAccountToUpdate(pTxnReturn);

        // if it should be updated...
        if (bankAccount != null) {
            // create a Notice Of Change system event (before updating any bank account fields)
            NoticeOfChangeUtils.createNoticeOfChangeSystemEventRule(pTxnReturn, bankAccount);

            // we want to keep the NOC transaction open for non-assisted employee returns if the ResolveEmployeeNOC payroll param exists and is false
            if (isNonAssistedEmployeeReturn(pTxnReturn) || isPayeeBankAccount(pTxnReturn)) {
                SourcePayrollParameter resolveEmployeeNOC = SourcePayrollParameter.findSourcePayrollParameter(pTxnReturn.getCompany().getSourceSystemCd(),
                                                                                                              SourcePayrollParameterCode.ResolveEmployeeNOC);

                if ((resolveEmployeeNOC != null) && !Boolean.parseBoolean(resolveEmployeeNOC.getParameterValue())) {
                    resolveNOC = false;
                    pTxnReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Open);
                }
            }
        }

// TODO - KP: Check if we need to auto-resolve C04s like we do for other NOCs
//        if (resolveNOC) {
//            resolveACHReturn(pTxnReturn);
//        }

        return pTxnReturn;
    }
}
