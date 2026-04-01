package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheck;

import java.util.Collection;
import java.util.List;

/**
 * User: michaelp696
 */
public class CancelOrDeletePayrollWorkersComp extends Process implements IProcess {
    private Collection<Paycheck> paychecks;
    public CancelOrDeletePayrollWorkersComp(Collection<Paycheck> paychecks) {
        this.paychecks = paychecks;
    }

    @Override
    public ProcessResult validate() {
        return new ProcessResult();
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        for(Paycheck paycheck : paychecks) {
            WorkersCompPaycheck.cancelOrDeleteWorkersCompPaycheck(paycheck);
        }
        return processResult;
    }
}
