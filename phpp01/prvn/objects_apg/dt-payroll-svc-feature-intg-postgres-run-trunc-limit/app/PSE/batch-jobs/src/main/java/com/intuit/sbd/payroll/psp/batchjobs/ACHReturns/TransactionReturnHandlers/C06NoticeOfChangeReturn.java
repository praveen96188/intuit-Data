package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.ACHBankAccountType;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Handles C06 (account type and number) ACH transaction returns.
 *
 * Created by IntelliJ IDEA.
 * User: rkrishna, wnichols
 * Date: Feb 26, 2008
 * Time: 3:33:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class C06NoticeOfChangeReturn extends NocReturnHandler {

    private static SpcfLogger logger = Application.getLogger(C06NoticeOfChangeReturn.class);

    /**
     * "C06" implementation of TransactionReturnHandler.meetsCriteria().
     *
     * @param pTxnReturn
     * @return true when the TransactionReturn's bank return code is "C06"
     */
    public  boolean meetsCriteria(TransactionReturn pTxnReturn){
        return "C06".equals(pTxnReturn.getBankReturnCd());
    }

    /**
     * Field-specific implementation of NocReturnHandler.fieldsAreValid().
     * Checks the corrected bank account type and number.
     *
     * @param pTxnReturn
     * @return
     */
    protected boolean fieldsAreValid(TransactionReturn pTxnReturn) {
        ACHBankAccountType newType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(pTxnReturn);
        String newAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(pTxnReturn);
        return (BankAccount.isValidBankAccountTypeCode(newType) &&
                BankAccountDTO.isValidBankAccountNumber(newAccountNumber));
    }

    /**
     * Field-specific implementation of NocReturnHandler.updateFields().
     * Updates the bank account type and number.
     *
     * @param pTxnReturn
     * @param pBankAccount
     */
    protected void updateFields(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        ACHBankAccountType achBankAccountType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(pTxnReturn);
        BankAccountType bankAccountType = NoticeOfChangeUtils.getBankAccountType(achBankAccountType);

        String newAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(pTxnReturn);

        if(pBankAccount.getEmployeeBankAccount() == null || !pBankAccount.getEmployeeBankAccount().getEmployee().canBeRecoveredByQB()) {
            pBankAccount.updateBankAccountNumber(newAccountNumber);
            pBankAccount.updateACHBankAccountTypeCd(achBankAccountType);
            if (bankAccountType != null) {
                pBankAccount.updateBankAccountTypeCd(bankAccountType);
            }
        } else {
            EmployeeBankAccountDTO employeeBankAccountDTO = PayrollServices.dtoFactory.create(pBankAccount.getEmployeeBankAccount());
            employeeBankAccountDTO.resetBankAccountId();
            employeeBankAccountDTO.getBankAccount().setAccountNumber(newAccountNumber);
            employeeBankAccountDTO.getBankAccount().setAchAccountType(achBankAccountType);
            if (bankAccountType != null) {
                employeeBankAccountDTO.getBankAccount().setAccountType(bankAccountType);
            }
            updateEmployeeBankAccount(employeeBankAccountDTO, pBankAccount);
        }
    }
}
