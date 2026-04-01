package com.intuit.sbd.payroll.psp.api;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.factory.IDTOFactory;
import com.intuit.sbd.payroll.psp.api.finders.IEntityFinder;
import com.intuit.sbd.payroll.psp.api.managers.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.processes.TransactionThreadSecondary;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.hibernate.Session;

import java.lang.reflect.Constructor;

/**
 * PSP in-process API
 * <br>Entry point for all methods that are visible from outside PSP
 * <p/>
 * </p>
 * Usage:
 * <p/>
 * <pre>
 *    PayrollServices.beginUnitOfWork();
 * <p/>
 *    try {
 *        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
 *        payrollRunDTO.setXXX();
 *        ...
 * <p/>
 *        PayrollServices.payrollManager.submitPayroll(payrollRunDTO);
 *        PayrollServices.commitUnitOfWork();
 *    }
 *    catch (Exception e) {
 *        PayrollServices.rollbackUnitOfWork();  // Must end unit of work before exiting
 *    }
 * <p/>
 * </pre>
 */
public class PayrollServices {
    private static SpcfLogger mLogger = SpcfLogManager.getLogger(PayrollServices.class);

    static {
        // Instantiate through reflection to not have compile-time dependency on impl
        companyManager = (ICompanyManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.CompanyManager");
        employeeManager = (IEmployeeManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.EmployeeManager");
        payrollManager = (IPayrollManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.PayrollManager");
        financialTransactionManager = (IFinancialTransactionManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.FinancialTransactionManager");
        subscriptionManager = (ISubscriptionManager)  createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.SubscriptionManager");
        systemParameterManager = (ISystemParameterManager)  createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.SystemParameterManager");
        entityFinder = (IEntityFinder) createInstance("com.intuit.sbd.payroll.psp.api.impl.finders.EntityFinder");
        entityFinderSecondary = (IEntityFinder) createInstance("com.intuit.sbd.payroll.psp.api.impl.finders.EntityFinderSecondary");
        dtoFactory = (IDTOFactory) createInstance("com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory");
        transmissionManagerSecondary = (ITransmissionManagerSecondary) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.TransmissionManagerSecondary");
        userManager = (IUserManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.UserManager");
        paymentManager = (IPaymentManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.PaymentManager");
        billPaymentManager = (IBillPaymentManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.BillPaymentManager");
		taxCreditsManager = (ITaxCreditsManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.TaxCreditsManager");
        entitlementManager = (IEntitlementManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.EntitlementManager");
        paystubManager = (IPaystubManager) createInstance("com.intuit.sbd.payroll.psp.api.impl.managers.PaystubManager");

        // Ignore class not found error for artifacts that do not include the batch jobs module
        IBatchJobManager tempBatchJobManager = null;
        String batchJobManagerImplClass = "com.intuit.sbd.payroll.psp.api.impl.managers.BatchJobManager";
        try {
            tempBatchJobManager = (IBatchJobManager) createInstance(batchJobManagerImplClass);
        } catch (RuntimeException e) {
            if(e.getCause() instanceof ClassNotFoundException && e.getCause().getMessage().equals(batchJobManagerImplClass)) {
                mLogger.error("Could not load batch job manager. If the batch jobs module is not needed for the artifact this error can be ignored.");
            } else {
                throw e;
            }
        }

        batchJobManager = tempBatchJobManager;
    }

    /**
     * Begin a unit of work
     */
    public static void beginUnitOfWork() {
        PayrollServices.beginUnitOfWork(FlushMode.AUTO);
    }

    public static void beginUnitOfWork(FlushMode pFlushMode) {
        Application.beginUnitOfWork(pFlushMode);
    }

    public static void beginUnitOfWork(FlushMode pFlushMode, boolean pReadOnly) {
        Application.beginUnitOfWork(pFlushMode, pReadOnly);
    }

    /**
     * Begin a unit of work
     */
    public static void beginUnitOfWorkWithSecondary() {
        PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.AUTO);
    }

    public static void beginUnitOfWorkWithSecondary(FlushMode pFlushMode) {
        ApplicationSecondary.beginUnitOfWork(pFlushMode);
        Application.beginUnitOfWork(pFlushMode);
    }

    public static void beginUnitOfWorkWithSecondary(FlushMode pFlushMode, boolean pReadOnly) {
        ApplicationSecondary.beginUnitOfWork(pFlushMode, pReadOnly);
        Application.beginUnitOfWork(pFlushMode, pReadOnly);
    }

    /**
     * Commit a unit of work
     */
    public static void commitUnitOfWork() {
        Application.commitUnitOfWork();
    }

    public static void commitUnitOfWorkWithSecondary() {
        ApplicationSecondary.commitUnitOfWork();
        Application.commitUnitOfWork();
    }

    /**
     * Rollback a unit of work
     */
    public static void rollbackUnitOfWork() {
        Application.rollbackUnitOfWork();
    }

    public static void rollbackUnitOfWorkWithSecondary() {
        ApplicationSecondary.rollbackUnitOfWork();
        Application.rollbackUnitOfWork();
    }

    public static void setCurrentPrincipal(PspPrincipal principal) {
        Application.setCurrentPrincipal(principal);
    }

    public static void setCurrentPrincipal(SystemPrincipal systemPrincipal) {
        Application.setCurrentPrincipal(new PspPrincipal(systemPrincipal));
    }

    public static void setProcessValidatesOnly(boolean pValidateOnly) {
        Application.setProcessValidatesOnly(pValidateOnly);
    }

    /**
     * Generic Finders
     */
    public static final IEntityFinder entityFinder;

    /**
     * Generic Finders Secondary
     */
    public static final IEntityFinder entityFinderSecondary;


    /**
     * Manages company related service methods
     */
    public static final ICompanyManager companyManager;

    /**
     * Manages employee related service methods
     */
    public static final IEmployeeManager employeeManager;

    /**
     * Manages paystub related service methods
     */
    public static final IPaystubManager paystubManager;

    /**
     * Manages entitlement related service methods
     */
    public static final IEntitlementManager entitlementManager;

    /**
     * Manages payroll related service methods
     */
    public static final IPayrollManager payrollManager;

    /**
     * Manages subscription related methods -
     * Logic to see if a company is in PSP
     * Logic to see if a company should be added to PSP
     * Password (PIN) related methods
     */
    public static final ISubscriptionManager subscriptionManager;

    /**
     * Manages the System Parameter Table
     */
    public static final ISystemParameterManager systemParameterManager;

    /**
     * Creates DTOs from domain objects
     */
    public static final IDTOFactory dtoFactory;

    /**
     * Manages financial transaction related service methods
     */
    public static final IFinancialTransactionManager financialTransactionManager;

    /**
     *  Manages Transmission related methods - Initialize, Finalize and Rollback Transmissions
     */
    public static final ITransmissionManagerSecondary transmissionManagerSecondary;

    /**
     * Manages User/Role related service methods
     */
    public static final IUserManager userManager;

    public static final IBatchJobManager batchJobManager;

    public static final IPaymentManager paymentManager;

    public static final IBillPaymentManager billPaymentManager;

    public static final ITaxCreditsManager taxCreditsManager;

    /**
     * Gets logger for passed class
     *
     * @return Initialized logger
     */
    public static SpcfLogger getLogger(Class c) {
        return SpcfLogManager.getLogger(c);
    }

    private static Object createInstance(String className) {
        try {
            Class cls = Class.forName(className);
            Constructor ct = cls.getDeclaredConstructor();
            ct.setAccessible(true);
            Object retobj = ct.newInstance();
            return retobj;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes transaction in a separate thread.
     *
     * @param pTransactionThread
     */
    public static ProcessResult executeTransactionThread(TransactionThread<ProcessResult> pTransactionThread) {
        Session session = Application.getHibernateSession();
        if (session != null && session.getHibernateFlushMode() == FlushMode.AUTO && session.isDirty()) {
            throw new RuntimeException("Transaction threads may not be executed from an Auto flush parent UOW.  It can create deadlock.");
        }
        return Application.executeTransactionThread(pTransactionThread);
    }

    /**
     * Executes transaction in a separate thread.
     *
     * @param pTransactionThread
     */
    public static ProcessResult executeTransactionThread(TransactionThreadSecondary<ProcessResult> pTransactionThread) {
        Session session = ApplicationSecondary.getHibernateSession();
        if (session != null && session.getHibernateFlushMode() == FlushMode.AUTO && session.isDirty()) {
            throw new RuntimeException("Transaction threads may not be executed from an Auto flush parent UOW.  It can create deadlock.");
        }
        return ApplicationSecondary.executeTransactionThread(pTransactionThread);
    }
}

