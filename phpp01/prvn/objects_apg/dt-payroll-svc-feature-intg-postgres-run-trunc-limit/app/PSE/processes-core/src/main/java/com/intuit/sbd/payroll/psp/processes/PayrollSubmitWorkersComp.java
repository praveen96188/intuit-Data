package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheck;

/**
 * User: michaelp696
 */
public class PayrollSubmitWorkersComp extends Process implements IProcess {
    private PayrollRun payrollRun;

    public PayrollSubmitWorkersComp() {
    }

    public void setPayrollRun(PayrollRun payrollRun) {
        this.payrollRun = payrollRun;
    }

    @Override
    public ProcessResult validate() {
        return new ProcessResult();
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        DomainEntitySet<Paycheck> paychecks = payrollRun.getPaycheckCollection();
        for(Paycheck paycheck : paychecks) {
            WorkersCompPaycheck.createWorkersCompPaycheck(paycheck);
        }
        return processResult;
    }
}
