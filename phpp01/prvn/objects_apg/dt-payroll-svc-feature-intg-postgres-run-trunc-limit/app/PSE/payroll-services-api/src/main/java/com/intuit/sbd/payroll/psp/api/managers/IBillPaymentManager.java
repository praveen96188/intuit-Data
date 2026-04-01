package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

import java.util.Collection;
import java.util.Map;

/**
 * This is the PSP service API that deals with all bill payment related information
 * <p>The API includes:
 * <p>Submitting a bill payment
 * <p>Adding a Payee
 * <p>Adding a Payee Bank Account
 */

public interface IBillPaymentManager {
    /**
     * Bill Payment submission
     *
     * @param pSourceSystem
     * @param pSourceCompanyId
     * @param pBillPayment
     * @return
     */
    ProcessResult<Collection<PayrollRun>> submitBillPayment(SourceSystemCode pSourceSystem, String pSourceCompanyId, Collection<BillPaymentDTO> pBillPayment);

    ProcessResult<Payee> addOrUpdatePayee(SourceSystemCode pSourceSystemCd, String pCompanyId, PayeeDTO pPayeeDto);

    ProcessResult<PayeeBankAccount> addOrUpdatePayeeBankAccount(SourceSystemCode pSourceSystem, String pSourceCompanyId, String pSourcePayeeId, PayeeBankAccountDTO pPayeeBankAccount);

    ProcessResult createWalletPayeeBankAccount(PayeeBankAccount payeeBankAccount);

    ProcessResult<Collection<PayrollRun>> cancelBillPaymentTransaction(
            SourceSystemCode pSourceSystem, String pSourceCompanyId,  Collection<String> pBillPaymentIds , String pSessionId);

}