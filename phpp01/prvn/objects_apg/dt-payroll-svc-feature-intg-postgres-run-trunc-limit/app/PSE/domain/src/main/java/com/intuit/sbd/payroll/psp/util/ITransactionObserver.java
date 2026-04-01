package com.intuit.sbd.payroll.psp.util;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 20, 2009
 * Time: 12:36:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ITransactionObserver {
    public void registered();
    public void unregistered();
    public void beforeTransactionBegin();
    public void afterTransactionBegin();
    public void beforeTransactionRollback();
    public void afterTransactionRollback();
    public void beforeTransactionCommit();
    public void afterTransactionCommit();
    public String getObserverName();
}
