package com.intuit.sbd.payroll.psp.api.impl.managers;


import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.api.managers.IBillPaymentManager;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.wallet.WalletCreateCore;

import java.util.Collection;
import java.util.Map;

/**
 * @author achaves
 *         Date: Nov 7, 2007
 *         Time: 10:19:33 PM
 */
class BillPaymentManager implements IBillPaymentManager {

    public ProcessResult<Collection<PayrollRun>> submitBillPayment(
            SourceSystemCode pSourceSystem, String pSourceCompanyId, Collection<BillPaymentDTO> pBillPayments) {

        BillPaymentSubmitCore processCore = new BillPaymentSubmitCore(pSourceSystem, pSourceCompanyId, pBillPayments);
        ProcessResult<Collection<PayrollRun>> processResult = processCore.execute();

        processResult.setResult(processCore.getPayrollRuns());

        return processResult;
    }

    public ProcessResult<Payee> addOrUpdatePayee(
            SourceSystemCode pSourceSystem, String pSourceCompanyId, PayeeDTO pPayeeDTO) {
        AddOrUpdatePayeeCore processCore = new AddOrUpdatePayeeCore(pSourceSystem, pSourceCompanyId, pPayeeDTO);

        ProcessResult<Payee> processResult = processCore.execute();

        processResult.setResult(processCore.getPayee());

        return processResult;

    }

    public ProcessResult<PayeeBankAccount> addOrUpdatePayeeBankAccount(
            SourceSystemCode pSourceSystem, String pSourceCompanyId, String pSourcePayeeId, PayeeBankAccountDTO pPayeeBankAccountDTO) {
        AddOrUpdatePayeeBankAccountCore processCore = new AddOrUpdatePayeeBankAccountCore(pSourceSystem, pSourceCompanyId, pSourcePayeeId, pPayeeBankAccountDTO);

        ProcessResult<PayeeBankAccount> processResult = processCore.execute();

        processResult.setResult(processCore.getPayeeBankAccount());

        return processResult;

    }

    public ProcessResult createWalletPayeeBankAccount(PayeeBankAccount payeeBankAccount) {
        return new WalletCreateCore(payeeBankAccount).execute();
    }

    public ProcessResult<Collection<PayrollRun>> cancelBillPaymentTransaction(
            SourceSystemCode pSourceSystem, String pSourceCompanyId, Collection<String> pBillPaymentCollection,String pSessionId ) {
        CancelTransactionsBillPayment processCore = new CancelTransactionsBillPayment(pSourceSystem, pSourceCompanyId, 
        		pBillPaymentCollection, pSessionId);

        ProcessResult<Collection<PayrollRun>> processResult = processCore.execute();

        processResult.setResult(processCore.getPayrollRuns());

        return processResult;

    }

}