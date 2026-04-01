package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Handles C01 (account number) ACH transaction returns.
 *
 * Created by IntelliJ IDEA.
 * User: rkrishna, wnichols
 * Date: Feb 26, 2008
 * Time: 2:21:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class C01NoticeOfChangeReturn extends NocReturnHandler {
    
    private static SpcfLogger logger = Application.getLogger(C01NoticeOfChangeReturn.class);

    /**
     * "C01" implementation of TransactionReturnHandler.meetsCriteria().
     *
     * @param pTxnReturn
     * @return true when the TransactionReturn's bank return code is "C01"
     */
    public  boolean meetsCriteria(TransactionReturn pTxnReturn){
        return "C01".equals(pTxnReturn.getBankReturnCd());
    }

    /**
     * Field-specific implementation of NocReturnHandler.fieldsAreValid().
     * Checks the corrected bank account number.
     *
     * @param pTxnReturn
     * @return
     */
    protected boolean fieldsAreValid(TransactionReturn pTxnReturn) {
        String newAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(pTxnReturn);
        return BankAccountDTO.isValidBankAccountNumber(newAccountNumber);
    }

    /**
     * Field-specific implementation of NocReturnHandler.updateFields().
     * Updates the bank account number.
     *
     * @param pTxnReturn
     * @param pBankAccount
     */
    protected void updateFields(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        String newAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(pTxnReturn);
        if(pBankAccount.getEmployeeBankAccount() == null) {
            pBankAccount.updateBankAccountNumber(newAccountNumber);
        } else if(!pBankAccount.getEmployeeBankAccount().getEmployee().canBeRecoveredByQB()) {
            pBankAccount.updateBankAccountNumber(newAccountNumber);
        } else {
            EmployeeBankAccountDTO employeeBankAccountDTO = PayrollServices.dtoFactory.create(pBankAccount.getEmployeeBankAccount());
            employeeBankAccountDTO.resetBankAccountId();
            employeeBankAccountDTO.getBankAccount().setAccountNumber(newAccountNumber);
            updateEmployeeBankAccount(employeeBankAccountDTO, pBankAccount);
        }
    }
}
