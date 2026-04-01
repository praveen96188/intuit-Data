package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.TimeConstraint;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;
import com.intuit.sbd.payroll.psp.domain.BatchJobParameter;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.common.utils.Reflection;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all batch job processors
 * <p/>
 * Implements:
 * <p/>
 * Automatic reentrancy of batch jobs (that is, executing the same batch job processor with the same
 * parameters will automatically bypass steps that have been sucessfully executed)
 * <p/>
 * Automatic logging of all step processing and error handling, including NOC/IOC alerts
 * <p/>
 * Usage:
 * <p/>
 * Each batch job processor should be a class derived from BatchJobProcessor
 * -> Derived classes must override "execute"
 * -> Derived classes must specify its steps as classes derived from the internal class BatchJobProcessorStep
 */
abstract public class BatchJobProcessor {
    public enum RunMode {
        UsingFlux,
        NotUsingFlux
    }

    /**
     * @param pBatchJobType          - job type to be executed
     * @param pJobId
     * @param pJobInstanceParameters - parameters for a given execution
     */
    public BatchJobProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        logger = Application.getLogger(this.getClass());

        runMode = pRunMode;
        batchJobType = pBatchJobType;
        jobInstanceParameters = pJobInstanceParameters;
        jobId = pJobId;

        loadParameters();
    }

    /**
     * Loads all parameters for the job with a dot separated name.<br>
     * The name format is:<br>
     * JOB_TYPE[.JOB_STEP].PARAM_NAME<br>
     * Examples:<br>
     * CheckPrintMonitor.time_constraint<br>
     * AchOffloadMonitor.VerifyAchFileCreationStarted.time_constraint
     */
    private void loadParameters() {
        parameters = new HashMap<String, String>();

        Criterion<BatchJobParameter> where = BatchJobParameter.BatchJobSetup().JobType().equalTo(batchJobType);

        DomainEntitySet<BatchJobParameter> paramEntries = PayrollServices.entityFinder.find(BatchJobParameter.class, where);

        for (BatchJobParameter param : paramEntries) {
            // Create dot separated name based on type, step and name
            String keyName = param.getBatchJobSetup().getJobType().toString() + "." + (param.getJobStep() != null ? param.getJobStep() + "." : "") +
                    param.getParamName();

            logger.debug("Loaded batch job parameter " + keyName + " with value \"" + param.getParamValue() + "\"");

            parameters.put(keyName, param.getParamValue());
        }
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public String getJobInstanceParameters() {
        return jobInstanceParameters;
    }

    public String getJobId() {
        return jobId;
    }

    protected String setJobId(String jobId) {
        return this.jobId = jobId;
    }

    public BatchJobType getBatchJobType() {
        return batchJobType;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "/" + jobId;
    }

     /**
     * Run a job
     */
    public void executeJob() {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.BatchJob);
            validateRuntimeParameters();
            execute();
        }
        catch (Throwable t) {
            String errorMessage = "Job " + this.toString() + "; " + t.getMessage();
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(errorMessage, t);
        }
    }

    /**
     *
     * Run a step of a job
     */
    public void executeJobStep(String stepName) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.BatchJob);
            validateStepRuntimeParameters(stepName);
            executeStep(instantiateBatchJobProcessorStep(stepName));
        }
        catch (Throwable t) {
            String errorMessage = "Job " + this.toString() + "; " + t.getMessage();
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(errorMessage, t);
        }
    }

    /*
     *  Static methods to query BatchJobAuditLog
     */
    public static String getJobNamespace(BatchJobType pJobType) {
        BatchJobSetup bjs = BatchJobManager.getBatchJobSetup(pJobType);
        String namespace = bjs.getJobNamespace();

        if (!namespace.endsWith("/")) {
            namespace += "/";
        }

        return namespace + pJobType.toString();
    }

    public static Boolean jobExists(BatchJobType pJobType, String pJobId) {
        boolean manageTransaction = !Application.hasActiveTransaction();
        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            return PayrollServices.entityFinder.find(BatchJobAuditLog.class, BatchJobAuditLog.JobNamespace().equalTo(getJobAuditId(pJobType, pJobId))).size() > 0;
        }
        finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static BatchJobAuditLog findAuditTrail(BatchJobType pJobType, String pJobId, String pActionName, String pMessage) {
        boolean manageTransaction = !Application.hasActiveTransaction();
        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().equalTo(getJobAuditId(pJobType, pJobId))
                                                .And(BatchJobAuditLog.JobAction().equalTo(pActionName)
                                                .And(BatchJobAuditLog.Message().equalTo(pMessage)));
            
            DomainEntitySet<BatchJobAuditLog> logEntries = PayrollServices.entityFinder.find(BatchJobAuditLog.class, where);

            BatchJobAuditLog logEntry = null;
            if (logEntries.size() > 0) {
                logEntry = logEntries.get(0);
            }

            if (manageTransaction) {
                PayrollServices.commitUnitOfWork();
            }

            return logEntry;
        }
        finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }

    }

    public static DomainEntitySet<BatchJobAuditLog> findAuditTrail(BatchJobType pJobType, String pActionName, String pMessage, TimeConstraint timeConstraint) {
        boolean manageTransaction = !Application.hasActiveTransaction();
        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(getJobNamespace(pJobType) + "%")
                                                .And(BatchJobAuditLog.JobAction().equalTo(pActionName)
                                                .And(BatchJobAuditLog.Message().equalTo(pMessage)
                                                .And(BatchJobAuditLog.IsVerified().equalTo(false))));

            if (timeConstraint != null) {
                // Add time constraints for the batch jobs
                // Can't use between because there is a casting exception
                where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
                    .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));
            }

            DomainEntitySet<BatchJobAuditLog> logEntries = PayrollServices.entityFinder.find(BatchJobAuditLog.class, where);

            if (manageTransaction) {
                PayrollServices.commitUnitOfWork();
            }

            return logEntries;
        }
        finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }

    }

    /**
     * Derived classes need to override this method to provide implementation
     * The implementation is usually a series of calls to RunStep
     */
    abstract protected void execute();

    /**
     * Derived classes can override this method to validate the parameter(s)
     * passed at runtime (jobInstanceParameter)
     */
    protected void validateRuntimeParameters() {

    }

    /**
     * Derived classes can override this method to validate the parameter(s)
     * passed at runtime to run this step (jobInstanceParameter)
     */
    protected void validateStepRuntimeParameters(String stepName) {
    }


    /**
     * Context map to be used to pass information across steps of a batch job
     */
    private HashMap<String, String> batchJobContext = new HashMap<String, String>();
    protected HashMap<String, String> getBatchJobContext() {
        return batchJobContext;
    }

    /**
     * Internal class representing a step in a processor
     * It is a base class meant to be inherited by the classes that implement
     * the business logic for a processor step
     */
    abstract protected class BatchJobProcessorStep {
        /** The time constraint for the monitor.  May be null if not set up. */
        protected TimeConstraint jobStepTimeConstraint;

        public BatchJobProcessorStep() {
            // Create dot separated name for this job step and get time constraint if it is set up
            String jobStepName = BatchJobProcessor.this.getBatchJobType().name() + "." + toString();
            jobStepTimeConstraint = TimeConstraint.getTimeConstraint(parameters, jobStepName);
        }

        abstract public void execute();

        protected boolean alreadyExecuted() {
            return BatchJobProcessor.findAuditTrail(BatchJobProcessor.this.getBatchJobType(), BatchJobProcessor.this.getJobId(), this.toString(), "Finished") != null;
        }

        protected void logStepFinished() {
            //logger.info("Finished job step " + this.toString() + " (jobId: " + BatchJobProcessor.this.getJobId() + ")");
            BatchJobProcessor.this.sendToAuditTrail(BatchJobProcessor.this.getBatchJobType(), BatchJobProcessor.this.getJobId(), this.toString(), "Finished", "");
        }

        protected void logStepStarted() {
            logger.info("Executing job step " + this.toString() + " (jobId: " + BatchJobProcessor.this.getJobId() + ")");
            BatchJobProcessor.this.sendToAuditTrail(BatchJobProcessor.this.getBatchJobType(), BatchJobProcessor.this.getJobId(), this.toString(), "Started", "");
        }

        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    /**
     * Run a step of a processor
     * <p/>
     * This method guarantees that:
     * . Steps are reentrant
     * . Propper error logging is done
     *
     * @param pBatchJobProcessorStep
     */
    protected void executeStep(BatchJobProcessorStep pBatchJobProcessorStep) {
        if (!pBatchJobProcessorStep.alreadyExecuted()) {
            pBatchJobProcessorStep.logStepStarted();
            try {
                pBatchJobProcessorStep.execute();
                //System.out.println("Step " + pBatchJobProcessorStep.toString() + " executed");
            }
            catch (Throwable t) {
                throw new RuntimeException("Step " + pBatchJobProcessorStep.toString() + ": " + t.getMessage(), t);
            }

            pBatchJobProcessorStep.logStepFinished();
        }
        else {
            logger.info("Bypassing step " + pBatchJobProcessorStep.toString() + " because it has already been executed under jobId: " + BatchJobProcessor.this.getJobId());
        }
    }

    protected void sendToAuditTrail(BatchJobType pJobType, String pJobId, String pJobAction, String pMessage, String pMessageDetail) {
        try {
            PayrollServices.beginUnitOfWork();

            BatchJobAuditLog logEntry = new BatchJobAuditLog();

            logEntry.setJobNamespace(getJobAuditId(pJobType, pJobId));
            logEntry.setJobAction(pJobAction);
            logEntry.setMessage(pMessage);
            logEntry.setMessageDetail(pMessageDetail);
            logEntry.setIsVerified(false);

            Application.save(logEntry);

            PayrollServices.commitUnitOfWork();
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    protected static String getJobAuditId(BatchJobType jobType, String jobId) {
        return getJobNamespace(jobType) + "/" + jobId;
    }

    protected BatchJobProcessorStep instantiateBatchJobProcessorStep(String pStepName) {
        Class stepClass = null;

        // get the list of all member classes and check to see if the given class name is a member
        for (Class clazz : getClass().getClasses()) {
            if (clazz.getSimpleName().equals(pStepName)) {
                stepClass = clazz;
                break;
            }
        }

        if (stepClass == null) {
            throw new RuntimeException("No member classes found matching job step name '" + pStepName +
                                       "' in processor class " + getClass().getName());
        }

        try {
            return (BatchJobProcessorStep) Reflection.createInstance(stepClass, new Class[]{stepClass.getDeclaringClass()}, new Object[]{this});
        } catch (Throwable t) {
            throw new RuntimeException("Could not instantiate job step with name " + stepClass.getName() + " " + t.getMessage());
        }
    }

    /**
     * Gets all parameters found for the JOB_TYPE
     * @return All parameters found for the JOB_TYPE
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    protected SpcfLogger logger;
    private RunMode runMode;
    private BatchJobType batchJobType;
    private String jobId;
    private String jobInstanceParameters;

    /** All parameters found for the JOB_TYPE */
    protected Map<String, String> parameters;
}
