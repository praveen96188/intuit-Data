package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.IProcessObserver;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 4/22/12
 * Time: 10:39 AM
 */
public class AbstractProcessObserver implements IProcessObserver {
    public void registered() {}

    public void unregistered() {}

    public void addItem(DomainEntity pEntity) {}

    public ProcessResult afterProcess() {
        return new ProcessResult();
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
