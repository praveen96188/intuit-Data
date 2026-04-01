package com.intuit.sbd.payroll.psp.jss;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.TimeConstraint;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.util.BatchUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;

import org.apache.commons.lang3.StringUtils;

/**
 * Base class for all JSS Batch Job Monitors.
 * 
 * @author kmuthurangam
 *
 */
public abstract class JSSBatchJobMonitor extends JSSBatchJob {

	protected boolean warnOnMultipleAuditEntries = true;

	/** The time constraint for the monitor. May be null if not set up. */
	protected TimeConstraint monitorTimeConstraint;

	public JSSBatchJobMonitor(String[] pArguments) {
		super(pArguments);

		init();
	}

	public JSSBatchJobMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);

		init();
	}

	private void init() {
		monitorTimeConstraint = TimeConstraint.getTimeConstraint(getParameters(), getBatchJobToMonitor().name());
	}

	public abstract BatchJobType getBatchJobToMonitor();

	public abstract Class<?> getBatchJobActionToMonitor();

	protected String getBatchJobToMonitorId() {
		return BatchUtils.parseJobId(getJobInstanceParameters());
	}

	protected void setWarnOnMultipleAuditEntries(boolean pWarn) {
		warnOnMultipleAuditEntries = pWarn;
	}

	@Override
	protected void execute() throws Exception {
		executeStep(MonitorProcessorStep.class);
	}

	protected void verifyJobStepStarted(String pJobId, BatchJobType pJobType, Class pJobAction,
			TimeConstraint pTimeConstraint) {
		verifyJobStep(pJobId, pJobType, pJobAction, "Started", pTimeConstraint);
	}

	protected void verifyJobStepFinished(String pJobId, BatchJobType pJobType, Class<?> pJobAction,
			TimeConstraint pTimeConstraint) {
		verifyJobStep(pJobId, pJobType, pJobAction, "Finished", pTimeConstraint);
	}

	protected void verifyJobStep(String pJobId, BatchJobType pJobType, Class<?> pJobAction, String pStepState,
			TimeConstraint pTimeConstraint) {
		try {
			PayrollServices.beginUnitOfWork();

			if (pJobId.length() == 0) {
				// This is a recurring job
				DomainEntitySet<BatchJobAuditLog> auditLogEntries = findAuditTrail(pJobType, pJobAction.getSimpleName(),
						pStepState, pTimeConstraint);

				if (auditLogEntries.size() == 0) {
					fail("The monitor could not verify that the action " + pJobAction.getSimpleName()
							+ " of the process " + pJobType + " " + pJobId + " has " + pStepState);
				}

				if (monitorTimeConstraint != null) {
					if (monitorTimeConstraint.hasValidSuccessfulRuns(auditLogEntries)) {
						fail("The monitor action " + pJobAction.getSimpleName() + " of the process " + pJobType + " "
								+ pJobId + " failed the successful runs check.");
					}
				}

				if (warnOnMultipleAuditEntries && (auditLogEntries.size() > 1)) {
					getLogger().warn("The monitor verified that more than one job " + pStepState + " for action "
							+ pJobAction.getSimpleName() + " of the process " + pJobType + " " + pJobId);
				}

				for (BatchJobAuditLog auditLogEntry : auditLogEntries) {
					getLogger().info("verifying " + auditLogEntry.getId());
					auditLogEntry.setIsVerified(true);
				}
			} else {
				// Non-recurring job: we know exactly which job to verify
				BatchJobAuditLog auditLogEntry = findAuditTrail(pJobType, pJobId, pJobAction.getSimpleName(),
						pStepState);

				if (auditLogEntry == null) {
					fail("The monitor could not verify that the action " + pJobAction.getSimpleName()
							+ " of the process " + pJobType + " " + pJobId + " has " + pStepState);
				} else {
					auditLogEntry.setIsVerified(true);
				}
			}

			PayrollServices.commitUnitOfWork();
		} finally {
			PayrollServices.rollbackUnitOfWork();
		}
	}

	private void fail(String failure) {
		StringBuilder failMessage = new StringBuilder(failure);

		BatchJobAuditLog lastStartTime = findLastMessage(getBatchJobToMonitor(), getBatchJobActionToMonitor(),
				"Started");
		if (lastStartTime != null) {
			failMessage.append("\nLast Job Step Start Time: ")
					.append(lastStartTime.getCreatedDate().toLocal().toString());
		}

		BatchJobAuditLog lastFinishTime = findLastMessage(getBatchJobToMonitor(), getBatchJobActionToMonitor(),
				"Finished");
		if (lastFinishTime != null) {
			failMessage.append("\nLast Job Step Finish Time: ")
					.append(lastFinishTime.getCreatedDate().toLocal().toString());
		}

		BatchJobAuditLog lastJobMessage = findLastMessage(getBatchJobToMonitor());
		if (lastJobMessage != null) {
			failMessage.append("\nLast Job Step, ").append(lastJobMessage.getJobAction()).append(", ")
					.append(lastJobMessage.getMessage()).append(": ")
					.append(lastJobMessage.getCreatedDate().toLocal().toString());
		}

		BatchJobSetup bjs = BatchJobManager.getBatchJobSetup(getBatchJobToMonitor());
		failMessage.append("\nSchedule: ").append(bjs.getJobTimerExpression());
		if (bjs.getJobNamespace().contains("/HIGH")) {
			failMessage.append("\n***High priority job***");
		}

		throw new RuntimeException(failMessage.toString());
	}

	private static BatchJobAuditLog findLastMessage(BatchJobType pJobType, Class<?> pJobActionToMonitor,
			String pMessage) {
		boolean manageTransaction = !Application.hasActiveTransaction();
		try {
			if (manageTransaction) {
				PayrollServices.beginUnitOfWork();
			}

			Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(getJobNamespace(pJobType) + "%")
					.And(BatchJobAuditLog.JobAction().equalTo(pJobActionToMonitor.getSimpleName())
							.And(BatchJobAuditLog.Message().equalTo(pMessage)));

			return Application
					.find(BatchJobAuditLog.class,
							new Query<BatchJobAuditLog>().Where(where)
									.OrderBy(BatchJobAuditLog.CreatedDate().Descending()).LimitResults(0, 1))
					.getFirst();
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

			return Application
					.find(BatchJobAuditLog.class,
							new Query<BatchJobAuditLog>().Where(where)
									.OrderBy(BatchJobAuditLog.CreatedDate().Descending()).LimitResults(0, 1))
					.getFirst();
		} finally {
			if (manageTransaction) {
				PayrollServices.rollbackUnitOfWork();
			}
		}

	}

	public static class MonitorProcessorStep extends JSSBatchJobStep<JSSBatchJobMonitor> {
		public void execute() {
			try {
				getBatchJobProcessor().verifyJobStepFinished(getBatchJobProcessor().getBatchJobToMonitorId(),
						getBatchJobProcessor().getBatchJobToMonitor(),
						getBatchJobProcessor().getBatchJobActionToMonitor(), jobStepTimeConstraint);
			} catch (RuntimeException exception) {
				// JSS stops scheduling the jobs if there are certain number of failures. To continue scheduling the jobs
				// even after failures, any expected errors are to be logged and not to be thrown back or reported to JSS
				// which ensures the job completion in JSS. Below error can happen if the job failed to complete on time
				// before the monitor. So logging the error instead of re throwing. These errors are already captured and
				// alerts are send from Splunk
				if (StringUtils.contains(exception.getMessage(), "The monitor could not verify that the action")) {
					getLogger().error("Error while monitoring the step ", exception);
				} else {
					throw exception;
				}
			}
		}
	}

}
