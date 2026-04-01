package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 4/22/12
 * Time: 9:08 AM 
 */
public interface IProcessObserver {
    public void registered();
    public void unregistered();
    public void addItem(DomainEntity pEntity);

    /**
     * Called after process is executed in the Process execute method.
     * @see com.intuit.sbd.payroll.psp.processes.Process#execute()
     */
    public ProcessResult afterProcess();
    
    public String getName();
}
