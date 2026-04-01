package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.processes.common.QBDTProcessObserver;
import com.intuit.sbd.payroll.psp.util.IProcessObserver;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Process class that contains basic flow for all PSP process flows. All PSP Process Flows should follow
 * the basic flow pre-defined in this class.  The basic flow consist of validation and processing.
 * If validation fails, the processing step is not executed.
 * <p/>
 * The derived classes must implement {@link Process#validate} and {@link Process#process}.
 * However, the implemented methods may be empty for some flows.
 * <p/>
 * The Process class does not control the transaction scope.
 *
 * @author Wiktor Kozlik
 */
public abstract class Process implements IProcess {
    private static List<Class> PROCESS_OBSERVERS;

    static {
        PROCESS_OBSERVERS = new ArrayList<Class>();
        PROCESS_OBSERVERS.add(QBDTProcessObserver.class);
    }

    private List<IProcessObserver> mProcessObservers = new ArrayList<IProcessObserver>();

    /**
     * Executes the process flow. The basic flow has two main processes: validation and processing.
     * If validation fails the processing is not executed.
     */
    public ProcessResult execute() {
        return execute(Application.getProcessValidatesOnly());
    }

    public ProcessResult execute(boolean pValidateOnly) {
        ProcessResult processFlowResult;
        FlushMode previousFlushMode = setPreferredFlushMode();

        try {
            // register process observers
            for (Class aClass : PROCESS_OBSERVERS) {
                try {
                    IProcessObserver processObserver = (IProcessObserver)aClass.newInstance();
                    if(Application.registerProcessObserver(processObserver)) {
                        mProcessObservers.add(processObserver);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error registering process observer " + aClass.getName(), e);
                }
            }

            processFlowResult = validate();
            assert processFlowResult != null;

            if (pValidateOnly) {
                return processFlowResult;
            }

            if (processFlowResult.isSuccess()) {
                ProcessResult processingResult = process();
                processFlowResult.merge(processingResult);

                // if the validation step did NOT set a "result object", and if the processing step DID, then move it over
                if (processFlowResult.getResult() == null && processingResult != null && processingResult.getResult() != null) {
                    processFlowResult.setResult(processingResult.getResult());
                }

                for (IProcessObserver processObserver : mProcessObservers) {
                    processFlowResult.merge(processObserver.afterProcess());
                }
            }
        }
        finally {
            for (IProcessObserver processObserver : mProcessObservers) {
                Application.unregisterProcessObserver(processObserver);
            }
            restorePreviousFlushMode(previousFlushMode);
        }

        return processFlowResult;
    }

    /**
     * Sets the preferred hibernate cache flush mode for this process as configured in the Application object. If no
     * default Hibernate flush mode has been set in the Application, then it defaults to FlushMode.MANUAL.
     * @return The previous flush mode, or FlushMode.AUTO if there is no active transaction.
     * @see Application#setDefaultHibernateFlushMode(FlushMode pDefaultFlushMode)
     */
    private FlushMode setPreferredFlushMode() {
        FlushMode previousFlushMode = FlushMode.AUTO;
        FlushMode defaultFlushMode = Application.getDefaultHibernateFlushMode();

        if (Application.hasActiveTransaction()) {
            previousFlushMode = Application.getHibernateSession().getHibernateFlushMode();
        }

        if (defaultFlushMode == null) {
            defaultFlushMode = FlushMode.MANUAL;
        }

        Application.getHibernateSession().setFlushMode(defaultFlushMode);

        return previousFlushMode;
    }

    private void restorePreviousFlushMode(FlushMode pFlushMode) {
        if (Application.hasActiveTransaction()) {
            Application.getHibernateSession().setFlushMode(pFlushMode);
        }
    }

    /**
     * Validation step.
     */
    public abstract ProcessResult validate();

    /**
     * Processing step.
     */
    public abstract ProcessResult process();
}
