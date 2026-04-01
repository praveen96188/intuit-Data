package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.context.threading.ChildThreadRequestContextHelper;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;

import java.util.Objects;

/**
 * This class is used to commit database changes in a separate thread. This functionality is needed when certain
 * changes must be saved to the database regardless of the current unit of work being rolled back or not.
 * To use it pass an object of this class to Application.executeTransactionThread(), just remember that
 * the transaction() method is being executed in a separate unit of work, so you have to be careful while
 * using objects that are associated with a different unit of work. In order to pass parameters to the TransactionThread
 * object you will either have to explicitely extend this class or use an anonymous class as shown below.
 *
 * <code>
 * Application.executeTransactionThread(new TransactionThread() {
 *    public ProcessResult transaction() {
 *        Company localCompany = Application.findById(Company.class, company.getId());
 *        CompanyEventBE.addPayrollSubmittedWithPendingNOC(localCompany, eeId);
 *        return new ProcessResult();
 *    }
 * });
 * </code>
 * 
 * @author Wiktor Kozlik
 */
public abstract class TransactionThread<T> implements Runnable {
    private static SpcfLogger logger = SpcfLogManager.getLogger(TransactionThread.class);
    private PspPrincipal principal;
    private Throwable exception;
    private T processResult;
    private ChildThreadRequestContextHelper childThreadRequestContextHelper;

    public TransactionThread() {
        principal = Application.getCurrentPrincipal();
        childThreadRequestContextHelper = new ChildThreadRequestContextHelper();
        childThreadRequestContextHelper.loadThreadLocals();
    }

    public abstract T transaction();

    public void run() {
        try {
            childThreadRequestContextHelper.setThreadLocals();
            if (principal != null) {
                Application.setCurrentPrincipal(principal);
            }
            Application.beginUnitOfWork();
            processResult = transaction();
            Application.commitUnitOfWork();
        }
        catch (Throwable t) {
            exception = t;
            logger.error(t.getMessage(), t);
        }
        finally {
            Application.rollbackUnitOfWork();
            childThreadRequestContextHelper.clearThreadLocals();
        }
    }

    public Throwable getException() {
        return exception;
    }

    public T getProcessResult() {
        return processResult;
    }
}
