package com.intuit.sbd.payroll.psp.jss;

import com.intuit.sbd.payroll.psp.batchjobs.util.TimeConstraint;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbg.shared.batchjob.BatchJobStep;
import com.intuit.sbg.shared.batchjob.exceptions.BatchJobStepExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all JSS Batch Job Step
 * <p/>
 * Implements:
 * <p/>
 * Automatic re-entrant of batch jobs (that is, executing the same batch job with the same parameters will automatically
 * bypass steps that have been sucessfully executed)
 * <p/>
 * Automatic logging of all step processing and error handling
 * <p/>
 * Usage:
 * <p/>
 * Each batch job step should be a class derived from JSSBatchJobStep -> Derived classes must override "execute" -> Derived
 * classes must specify its steps as classes derived from the internal class BatchJobStep
 * 
 * @author kmuthurangam
 *
 * @param <T>
 */
public abstract class JSSBatchJobStep<T extends JSSBatchJob> extends BatchJobStep<T> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected TimeConstraint jobStepTimeConstraint;

	public BatchJobType getBatchJobType() {
		return getBatchJobProcessor().getBatchJobType();
	}

	public T getBatchJobProcessor() {
		return this.batchJobProcessor;
	}

	@Override
	protected boolean alreadyExecuted() {
		return JSSBatchJob.findAuditTrail(getBatchJobType(), getBatchJobProcessor().getJobId(), this.toString(),
				"Finished") != null;
	}

	protected void logStepStarted() {
		getLogger()
				.info("Executing job step " + toString() + " (jobId= " + this.getBatchJobProcessor().getJobId() + ")");
		getBatchJobProcessor().sendToAuditTrail(getBatchJobType(), getBatchJobProcessor().getJobId(), this.toString(),
				"Started", "");
	}

	protected void logStepFinished() {
		getLogger()
				.info("Finished job step " + toString() + " (jobId= " + this.getBatchJobProcessor().getJobId() + ")");
		getBatchJobProcessor().sendToAuditTrail(getBatchJobType(), getBatchJobProcessor().getJobId(), this.toString(),
				"Finished", "");
	}

	public void executeStep() {
		initTimeConstraint();
		logStepStarted();
		try {
			execute();
		} catch (Throwable t) {
			throw new BatchJobStepExecutionException(this, t);
		}
		logStepFinished();
	}

	public Logger getLogger() {
		return logger;
	}

	private void initTimeConstraint() {
		// Create dot separated name for this job step and get time constraint if it is set up
		String jobStepName = getBatchJobType().name() + "." + toString();
		jobStepTimeConstraint = TimeConstraint.getTimeConstraint(getBatchJobProcessor().getParameters(), jobStepName);
	}

}