package com.intuit.sbd.payroll.psp.domainsecondary.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * This class is used to commit database changes in a separate thread. This functionality is needed when certain
 * changes must be saved to the database regardless of the current unit of work being rolled back or not.
 * To use it pass an object of this class to ApplicationSecondary.executeTransactionThread(), just remember that
 * the transaction() method is being executed in a separate unit of work, so you have to be careful while
 * using objects that are associated with a different unit of work. In order to pass parameters to the TransactionThreadSecondary
 * object you will either have to explicitely extend this class or use an anonymous class as shown below.
 *
 * <code>
 * ApplicationSecondary.executeTransactionThread(new TransactionThreadSecondary() {
 *    public ProcessResult transaction() {
 *        ProcessResult sst = ApplicationSecondary.findById(SourceSystemTransmission.class, transmissionPayrollRun.getSourceSystemTransmissionId());
 *        return sst;
 *    }
 * });
 * </code>
 * 
 * @author Shivay Sharma
 */
public abstract class TransactionThreadSecondary<T> implements Runnable {
    private static SpcfLogger logger = SpcfLogManager.getLogger(TransactionThreadSecondary.class);
    private PspPrincipal principal;
    private Throwable exception;
    private T processResult;

    public TransactionThreadSecondary() {
        principal = Application.getCurrentPrincipal();
    }

    public abstract T transaction();

    public void run() {
        try {
            if (principal != null) {
                Application.setCurrentPrincipal(principal);
            }
            Application.beginUnitOfWork();
            ApplicationSecondary.beginUnitOfWork();
            processResult = transaction();
            ApplicationSecondary.commitUnitOfWork();
            Application.commitUnitOfWork();
        }
        catch (Throwable t) {
            exception = t;
            logger.error(t.getMessage(), t);
        }
        finally {
            Application.rollbackUnitOfWork();
            ApplicationSecondary.rollbackUnitOfWork();
        }
    }

    public Throwable getException() {
        return exception;
    }

    public T getProcessResult() {
        return processResult;
    }
}
