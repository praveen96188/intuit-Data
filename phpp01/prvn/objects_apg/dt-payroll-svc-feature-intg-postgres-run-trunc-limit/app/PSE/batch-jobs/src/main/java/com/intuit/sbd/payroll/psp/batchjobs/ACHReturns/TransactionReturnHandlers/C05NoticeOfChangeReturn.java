package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.ACHBankAccountType;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Handles C05 (account type) ACH transaction returns.
 *
 * Created by IntelliJ IDEA.
 * User: rkrishna, wnichols
 * Date: Feb 26, 2008
 * Time: 3:33:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class C05NoticeOfChangeReturn extends NocReturnHandler {

    private static SpcfLogger logger = Application.getLogger(C05NoticeOfChangeReturn.class);

    /**
     * "C05" implementation of TransactionReturnHandler.meetsCriteria().
     *
     * @param pTxnReturn
     * @return true when the TransactionReturn's bank return code is "C05"
     */
    public  boolean meetsCriteria(TransactionReturn pTxnReturn){
        return "C05".equals(pTxnReturn.getBankReturnCd());
    }

    /**
     * Field-specific implementation of NocReturnHandler.fieldsAreValid().
     * Checks the corrected bank account type.
     *
     * @param pTxnReturn
     * @return
     */
    protected boolean fieldsAreValid(TransactionReturn pTxnReturn) {
        ACHBankAccountType newType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(pTxnReturn);
        return BankAccount.isValidBankAccountTypeCode(newType);
    }

    /**
     * Field-specific implementation of NocReturnHandler.updateFields().
     * Updates the bank account type.
     *
     * @param pTxnReturn
     * @param pBankAccount
     */
    protected void updateFields(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        ACHBankAccountType achBankAccountType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(pTxnReturn);
        BankAccountType bankAccountType = NoticeOfChangeUtils.getBankAccountType(achBankAccountType);

        if(pBankAccount.getEmployeeBankAccount() == null || !pBankAccount.getEmployeeBankAccount().getEmployee().canBeRecoveredByQB()) {
            pBankAccount.updateACHBankAccountTypeCd(achBankAccountType);
            if (bankAccountType != null) {
                pBankAccount.updateBankAccountTypeCd(bankAccountType);
            }
        } else {
            EmployeeBankAccountDTO employeeBankAccountDTO = PayrollServices.dtoFactory.create(pBankAccount.getEmployeeBankAccount());
            employeeBankAccountDTO.resetBankAccountId();
            employeeBankAccountDTO.getBankAccount().setAchAccountType(achBankAccountType);
            if (bankAccountType != null) {
                employeeBankAccountDTO.getBankAccount().setAccountType(bankAccountType);
            }
            updateEmployeeBankAccount(employeeBankAccountDTO, pBankAccount);
        }
    }
}
