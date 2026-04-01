package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Handles C03 (account + routing numbers) ACH transaction returns.
 *
 * Created by IntelliJ IDEA.
 * User: rkrishna, wnichols
 * Date: Feb 26, 2008
 * Time: 3:32:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class C03NoticeOfChangeReturn extends NocReturnHandler {

    private static SpcfLogger logger = Application.getLogger(C03NoticeOfChangeReturn.class);

    /**
     * "C03" implementation of TransactionReturnHandler.meetsCriteria().
     *
     * @param pTxnReturn
     * @return true when the TransactionReturn's bank return code is "C03"
     */
    public  boolean meetsCriteria(TransactionReturn pTxnReturn){
        return "C03".equals(pTxnReturn.getBankReturnCd());
    }

    /**
     * Field-specific implementation of NocReturnHandler.fieldsAreValid().
     * Checks the corrected bank routing and account numbers.
     *
     * @param pTxnReturn
     * @return
     */
    protected boolean fieldsAreValid(TransactionReturn pTxnReturn) {
        String newAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(pTxnReturn);
        String newRoutingNumber = NoticeOfChangeUtils.getCorrectedBankRoutingNumber(pTxnReturn);
        return (BankAccountDTO.isValidBankAccountNumber(newAccountNumber) &&
                BankAccountDTO.isValidRoutingNumber(newRoutingNumber));
    }

    /**
     * Field-specific implementation of NocReturnHandler.updateFields().
     * Updates the bank routing and account numbers.
     *
     * @param pTxnReturn
     * @param pBankAccount
     */
    protected void updateFields(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        String newAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(pTxnReturn);
        String newRoutingNumber = NoticeOfChangeUtils.getCorrectedBankRoutingNumber(pTxnReturn);

        if(pBankAccount.getEmployeeBankAccount() == null || !pBankAccount.getEmployeeBankAccount().getEmployee().canBeRecoveredByQB()) {
            pBankAccount.updateBankAccountNumber(newAccountNumber);
            pBankAccount.updateBankRoutingNumber(newRoutingNumber);
        } else {
            EmployeeBankAccountDTO employeeBankAccountDTO = PayrollServices.dtoFactory.create(pBankAccount.getEmployeeBankAccount());
            employeeBankAccountDTO.resetBankAccountId();
            employeeBankAccountDTO.getBankAccount().setAccountNumber(newAccountNumber);
            employeeBankAccountDTO.getBankAccount().setRoutingNumber(newRoutingNumber);
            updateEmployeeBankAccount(employeeBankAccountDTO, pBankAccount);
        }
    }
}
