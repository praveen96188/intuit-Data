package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.TimeConstraint;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * User: achaves
 * Date: Sep 22, 2008
 * Time: 11:10:01 PM
 */
public class BatchJobProcessorMonitor extends BatchJobProcessor {
    protected BatchJobType jobTypeToMonitor;
    protected Class jobActionToMonitor;
    protected boolean warnOnMultipleAuditEntries = true;

    /** The time constraint for the monitor.  May be null if not set up. */
    protected TimeConstraint monitorTimeConstraint;

    public BatchJobProcessorMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor,
                                    BatchJobType pJobTypeToMonitor, Class pJobActionToMonitor) {
        super(pRunMode, pBatchJobType, pJobId, pJobIdToMonitor);

        monitorTimeConstraint = TimeConstraint.getTimeConstraint(parameters, pJobTypeToMonitor.name());

        jobTypeToMonitor = pJobTypeToMonitor;
        jobActionToMonitor = pJobActionToMonitor;
    }

    @Override
    public void execute() {
        executeStep(new MonitorProcessorStep());
    }

    protected void setWarnOnMultipleAuditEntries(boolean pWarn) {
        warnOnMultipleAuditEntries = pWarn;
    }

    protected BatchJobType getJobTypeToMonitor() {
        return jobTypeToMonitor;
    }

    protected Class getJobActionToMonitor() {
        return jobActionToMonitor;
    }

    protected String getBatchJobToMonitorId() {
        return getJobInstanceParameters();
    }

    protected void verifyJobStepStarted(String pJobId, BatchJobType pJobType, Class pJobAction, TimeConstraint pTimeConstraint) {
        verifyJobStep(pJobId, pJobType, pJobAction, "Started", pTimeConstraint);
    }

    protected void verifyJobStepFinished(String pJobId, BatchJobType pJobType, Class pJobAction, TimeConstraint pTimeConstraint) {
        verifyJobStep(pJobId, pJobType, pJobAction, "Finished", pTimeConstraint);
    }

    protected void verifyJobStep(String pJobId, BatchJobType pJobType, Class pJobAction, String pStepState, TimeConstraint pTimeConstraint) {
        try {
            PayrollServices.beginUnitOfWork();

            if (pJobId.length() == 0) {
                // This is a recurring job
                DomainEntitySet<BatchJobAuditLog> auditLogEntries =findAuditTrail(pJobType, pJobAction.getSimpleName(), pStepState, pTimeConstraint);

                if (auditLogEntries.size() == 0) {
                    fail("The monitor could not verify that the action " + pJobAction.getSimpleName() +
                                 " of the process " + pJobType + " " + pJobId + " has " + pStepState);
                }

                if (monitorTimeConstraint != null) {
                    if (monitorTimeConstraint.hasValidSuccessfulRuns(auditLogEntries)) {
                        fail("The monitor action " + pJobAction.getSimpleName() +
                                     " of the process " + pJobType + " " + pJobId + " failed the successful runs check.");
                    }
                }

                if (warnOnMultipleAuditEntries && (auditLogEntries.size() > 1)) {
                    logger.warn("The monitor verified that more than one job " + pStepState + " for action " +
                                pJobAction.getSimpleName() + " of the process " + pJobType + " " + pJobId);
                }

                for (BatchJobAuditLog auditLogEntry : auditLogEntries) {
                    logger.info("verifying " + auditLogEntry.getId());
                    auditLogEntry.setIsVerified(true);
                }
            }
            else {
                // Non-recurring job: we know exactly which job to verify
                BatchJobAuditLog auditLogEntry = findAuditTrail(pJobType, pJobId, pJobAction.getSimpleName(), pStepState);

                if (auditLogEntry == null) {
                    fail("The monitor could not verify that the action " + pJobAction.getSimpleName() +
                                 " of the process " + pJobType + " " + pJobId + " has " + pStepState);
                }
                else {
                    auditLogEntry.setIsVerified(true);
                }
            }

            PayrollServices.commitUnitOfWork();
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void fail(String failure) {
        StringBuilder failMessage = new StringBuilder(failure);

        BatchJobAuditLog lastStartTime = findLastMessage(jobTypeToMonitor, getJobActionToMonitor(), "Started");
        if (lastStartTime != null) {
            failMessage.append("\nLast Job Step Start Time: ").append(lastStartTime.getCreatedDate().toLocal().toString());
        }

        BatchJobAuditLog lastFinishTime = findLastMessage(jobTypeToMonitor, getJobActionToMonitor(), "Finished");
        if (lastFinishTime != null) {
            failMessage.append("\nLast Job Step Finish Time: ").append(lastFinishTime.getCreatedDate().toLocal().toString());
        }

        BatchJobAuditLog lastJobMessage = findLastMessage(getJobTypeToMonitor());
        if (lastJobMessage != null) {
            failMessage.append("\nLast Job Step, ").append(lastJobMessage.getJobAction()).append(", ").append(lastJobMessage.getMessage()).append(": ").append(lastJobMessage.getCreatedDate().toLocal().toString());
        }

        BatchJobSetup bjs = BatchJobManager.getBatchJobSetup(getJobTypeToMonitor());
        failMessage.append("\nSchedule: ").append(bjs.getJobTimerExpression());
        if (bjs.getJobNamespace().contains("/HIGH")) {
            failMessage.append("\n***High priority job***");
        }

        throw new RuntimeException(failMessage.toString());
    }

    private static BatchJobAuditLog findLastMessage(BatchJobType pJobType, Class pJobActionToMonitor, String pMessage) {
        boolean manageTransaction = !Application.hasActiveTransaction();
        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(getJobNamespace(pJobType) + "%")
                                                                .And(BatchJobAuditLog.JobAction().equalTo(pJobActionToMonitor.getSimpleName())
                                                                                     .And(BatchJobAuditLog.Message().equalTo(pMessage)));

            return Application.find(BatchJobAuditLog.class, new Query<BatchJobAuditLog>().Where(where).OrderBy(BatchJobAuditLog.CreatedDate().Descending()).LimitResults(0, 1)).getFirst();
        } finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }

    }

    private static BatchJobAuditLog findLastMessage(BatchJobType pJobType) {
        boolean manageTransaction = !Application.hasActiveTransaction();
        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(getJobNamespace(pJobType) + "%");

            return Application.find(BatchJobAuditLog.class, new Query<BatchJobAuditLog>().Where(where).OrderBy(BatchJobAuditLog.CreatedDate().Descending()).LimitResults(0, 1)).getFirst();
        } finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }

    }

    public class MonitorProcessorStep extends BatchJobProcessorStep {
        public void execute() {
            verifyJobStepFinished(getBatchJobToMonitorId(), getJobTypeToMonitor(), getJobActionToMonitor(), jobStepTimeConstraint);
        }
    }
}