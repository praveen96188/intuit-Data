package com.intuit.sbd.payroll.psp.util;

/**
 * This class is used to observe database transaction activity. Extend this class and override the methods you require.
 * Each observer must be registered with the Application to be notified of transaction activity. Each registered
 * observer is thread local to the thread registering the observer.
 * User: kpaul
 * Date: Jun 20, 2009
 * Time: 1:03:43 PM
 * @see com.intuit.sbd.payroll.psp.Application#getTransactionObserver(String)
 * @see com.intuit.sbd.payroll.psp.Application#registerTransactionObserver(String, ITransactionObserver)
 * @see com.intuit.sbd.payroll.psp.Application#unregisterTransactionObserver(String)
 */
public abstract class TransactionObserver implements ITransactionObserver {
    /**
     * If this method is not overridden, the default name of the observer is the name of the class.
     * Override this method to provide a different naming scheme (i.e. instance specific naming)
     */
    public String getObserverName() {
        return getClass().getName();
    }

    /**
     * This method is called when the observer is registered with the Application.
     * @see com.intuit.sbd.payroll.psp.Application#registerTransactionObserver(String, ITransactionObserver)
     */
    public void registered() {}

    /**
     * This method is called when the observer is unregistered with the Application.
     * @see com.intuit.sbd.payroll.psp.Application#unregisterTransactionObserver(String)
     */
    public void unregistered() {}

    /**
     * Called just before the start of a new database transaction.
     * @see com.intuit.sbd.payroll.psp.Application#beginUnitOfWork()
     */
    public void beforeTransactionBegin() {}

    /**
     * Called just after the start of a new database transaction.
     * @see com.intuit.sbd.payroll.psp.Application#beginUnitOfWork()
     */
    public void afterTransactionBegin() {}

    /**
     * Called just before the rollback of a database transaction.
     * @see com.intuit.sbd.payroll.psp.Application#rollbackUnitOfWork()
     */
    public void beforeTransactionRollback() {}

    /**
     * Called just after the rollback of a database transaction.
     * @see com.intuit.sbd.payroll.psp.Application#rollbackUnitOfWork()
     */
    public void afterTransactionRollback() {}

    /**
     * Called just before the commit of a database transaction.
     * @see com.intuit.sbd.payroll.psp.Application#commitUnitOfWork()
     */
    public void beforeTransactionCommit() {}

    /**
     * Called just after the commit of a database transaction.
     * @see com.intuit.sbd.payroll.psp.Application#commitUnitOfWork()
     */
    public void afterTransactionCommit() {}
}
