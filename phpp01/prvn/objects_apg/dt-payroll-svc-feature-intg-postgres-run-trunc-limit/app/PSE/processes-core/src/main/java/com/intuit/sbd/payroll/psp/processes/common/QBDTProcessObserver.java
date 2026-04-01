package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 4/22/12
 * Time: 9:02 AM
 */
public class QBDTProcessObserver extends AbstractProcessObserver {
    private DomainEntitySet<FinancialTransaction> mFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
    private DomainEntitySet<PayrollRun> mPayrollRuns = new DomainEntitySet<PayrollRun>();

    public void addItem(DomainEntity pEntity) {
        if(pEntity instanceof FinancialTransaction) {
            mFinancialTransactions.add((FinancialTransaction) pEntity);
        } else if(pEntity instanceof PayrollRun) {
            mPayrollRuns.add((PayrollRun)pEntity);
        }
    }

    public ProcessResult afterProcess() {
        ProcessResult processResult = new ProcessResult();

        for (FinancialTransaction financialTransaction : mFinancialTransactions) {
            if(financialTransaction.getPayrollRun() != null &&
                    !mPayrollRuns.contains(financialTransaction.getPayrollRun())) {
                mPayrollRuns.add(financialTransaction.getPayrollRun());
            }
        }

        for (PayrollRun payrollRun : mPayrollRuns) {
            // this is a hack, but the transaction types are the same so, this is the only way to differentiate
            if(payrollRun.getPayrollRunType() != PayrollType.BillPayment && payrollRun.getFinancialTransactionCollection().size() > 0) {
                processResult.merge(PayrollServices.companyManager.generateLiabilityChecks(payrollRun.getCompany(), payrollRun));
            }
        }

        mFinancialTransactions.clear();
        mPayrollRuns.clear();

        return processResult;
    }
}
