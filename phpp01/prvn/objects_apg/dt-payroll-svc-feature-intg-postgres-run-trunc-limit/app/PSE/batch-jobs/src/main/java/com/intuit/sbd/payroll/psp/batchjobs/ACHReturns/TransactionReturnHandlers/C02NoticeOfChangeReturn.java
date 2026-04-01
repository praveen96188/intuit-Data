package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Handles C02 (routing number) ACH transaction returns.
 *
 * Created by IntelliJ IDEA.
 * User: rkrishna, wnichols
 * Date: Feb 26, 2008
 * Time: 3:31:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class C02NoticeOfChangeReturn extends NocReturnHandler {

    private static SpcfLogger logger = Application.getLogger(C02NoticeOfChangeReturn.class);

    /**
     * "C02" implementation of TransactionReturnHandler.meetsCriteria().
     *
     * @param pTxnReturn
     * @return true when the TransactionReturn's bank return code is "C02"
     */
    public  boolean meetsCriteria(TransactionReturn pTxnReturn){
        return "C02".equals(pTxnReturn.getBankReturnCd());
    }

    /**
     * Field-specific implementation of NocReturnHandler.fieldsAreValid().
     * Checks the corrected bank routing number.
     *
     * @param pTxnReturn
     * @return
     */
    protected boolean fieldsAreValid(TransactionReturn pTxnReturn) {
        String newRoutingNumber = NoticeOfChangeUtils.getCorrectedBankRoutingNumber(pTxnReturn);
        return BankAccountDTO.isValidRoutingNumber(newRoutingNumber);
    }

    /**
     * Field-specific implementation of NocReturnHandler.updateFields().
     * Updates the bank routing number.
     *
     * @param pTxnReturn
     * @param pBankAccount
     */
    protected void updateFields(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        String newRoutingNumber = NoticeOfChangeUtils.getCorrectedBankRoutingNumber(pTxnReturn);
        if(pBankAccount.getEmployeeBankAccount() == null || !pBankAccount.getEmployeeBankAccount().getEmployee().canBeRecoveredByQB()) {
            pBankAccount.updateBankRoutingNumber(newRoutingNumber);
        } else {
            EmployeeBankAccountDTO employeeBankAccountDTO = PayrollServices.dtoFactory.create(pBankAccount.getEmployeeBankAccount());
            employeeBankAccountDTO.resetBankAccountId();
            employeeBankAccountDTO.getBankAccount().setRoutingNumber(newRoutingNumber);
            updateEmployeeBankAccount(employeeBankAccountDTO, pBankAccount);
        }
    }
}
