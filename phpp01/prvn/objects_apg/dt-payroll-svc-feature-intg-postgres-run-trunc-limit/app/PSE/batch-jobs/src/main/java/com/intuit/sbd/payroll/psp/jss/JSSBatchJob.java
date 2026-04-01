package com.intuit.sbd.payroll.psp.jss;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobController;
import com.intuit.sbd.payroll.psp.batchjobs.util.TimeConstraint;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;
import com.intuit.sbd.payroll.psp.domain.BatchJobParameter;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.jss.util.BatchUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.BatchJob;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.jss.client.JSSRequestHeaders;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;
import java.lang.reflect.Field;
import java.util.*;

/**
 *
 * Base class for all JSS Batch Jobs.
 * <p/>
 * Implements:
 * <p/>
 * Automatic re-entranct of batch jobs (that is, executing the same batch job processor with the same parameters will
 * automatically bypass steps that have been sucessfully executed)
 * <p/>
 * Usage:
 * <p/>
 * Each batch job processor should be a class derived from BatchJobProcessor -> Derived classes must override "execute"
 *
 * JSS stops scheduling the jobs if there are certain number of failures. To continue scheduling the jobs even after
 * failures, any expected errors are to be logged and not to be thrown back or reported to JSS which ensures the job
 * completion in JSS.
 *
 * @author kmuthurangam
 *
 */
public abstract class JSSBatchJob extends BatchJob {

	public static final String DATA = "data";

    private static final Logger logger = LoggerFactory.getLogger(JSSBatchJob.class);

    private static final List<BatchJobType> SUPRESS_STALE_OBJECTS_BATCH_JOBS = new ArrayList<BatchJobType>();

	private PSPRequestContextManager pspRequestContextManager;

	public enum RunMode {
		JOB_SCHEDULER_SERVICE, COMMAND_LINE
	};

    static {
        try {
            String supressedBatchJobs = SystemParameter.findStringValue(SystemParameter.Code.SUPRESS_STALE_OBJECTS_BATCH_JOBS);
            logger.info("List of supressed Batch Jobs" +supressedBatchJobs);
            if (supressedBatchJobs != null) {
                String[] batchJobNames = supressedBatchJobs.split(",");
                for (String batchJobName : batchJobNames) {
                    batchJobName = batchJobName.trim();
                    if (batchJobName.length() > 0) {
                        try {
                        SUPRESS_STALE_OBJECTS_BATCH_JOBS.add(BatchJobType.valueOf(batchJobName));
                        }
                        catch(Exception e)
                        {
                           logger.error("Incorrect BatchJob name" + batchJobName);
                        }
                    }
                }

            }
        } catch (Throwable t) {

            logger.info(t.toString());
        }
    }

	private String jobInstanceParameters;

	private String jobName;

	private RunMode runMode = RunMode.COMMAND_LINE;

	/** All parameters found for the JOB_TYPE */
	private Map<String, String> parameters;

	public JSSBatchJob(String[] pArguments) {
		super(pArguments);
		init(pArguments, null);
	}

	public JSSBatchJob(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
		init(pArguments, pJobId);
	}

	private void init(String[] pArguments, String pJobId) {
		pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
		getLogger().info("[JSSBatchJob] constructor getArguments " + Arrays.toString(getArguments()));
		String jobId = null;
		String arguments = StringUtils.EMPTY;

		setRunMode();

		switch (getRunMode()) {
		case JOB_SCHEDULER_SERVICE:
			Object jssArguments = getJSSArguments();
			if (jssArguments instanceof String) {
				arguments = (String) jssArguments;
			} else if (jssArguments instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) jssArguments;
				jobId = BatchUtils.getValue(jsonObject, BatchUtils.JOB_ID);
				if (jobId != null) {
					pJobId = jobId;
				}
				arguments = BatchUtils.getValue(jsonObject, BatchUtils.ARGS);
				arguments = (arguments != null) ? arguments : StringUtils.EMPTY;
			}
			break;

		case COMMAND_LINE:
			arguments = getCommandLineArguments(getArguments());
			break;
		}

		jobId = BatchUtils.getValidJobId(pJobId);
		setJobId(jobId);
		setJobInstanceParameters(arguments);
		getLogger()
				.info("[JSSBatchJob] jobId " + getJobId() + " getJobInstanceParameters " + getJobInstanceParameters());

		// Loads all parameters
		loadParameters();
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	public void executeJob() {
		isJobEnabledForDS2STGEnv();
		PayrollServices.setCurrentPrincipal(SystemPrincipal.BatchJob);
		BatchJobType jobType = getBatchJobType();
		// Check if batch job can be run
		getLogger().info("[JSSBatchJob] jobType " + jobType);

		if (SystemParameter.findBooleanValue(SystemParameter.Code.BATCH_JOB_CONTROLLER_ENABLED)
				&& !BatchJobController.canRunBatchJob(jobType)) {
			throw new RuntimeException(
					"An instance of this batch job is already running, cannot run another instance simultaneously");
		}

		try {
			pspRequestContextManager.setRequestContext(null, RequestType.OLAP, getJobName());
			BatchJobController.batchJobStarted(jobType);
			super.executeJob();
			BatchJobController.batchJobFinished(jobType);
		} catch (Exception exception) {
			if (isErrorRecoverable(exception)) {
				getLogger().error("Supressing the exception as it is recoverable ", exception);
			} else {
				throw new RuntimeException(exception);
			}
		} finally {
			pspRequestContextManager.clearRequestContext();
		}

	}

	@Override
	public void executeJobStep(String stepName) {
		PayrollServices.setCurrentPrincipal(SystemPrincipal.BatchJob);
		super.executeJobStep(stepName);
	}

	public void setJobInstanceParameters(String jobInstanceParameters) {
		this.jobInstanceParameters = jobInstanceParameters;
	}

	public String getJobInstanceParameters() {
		return jobInstanceParameters;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getContentType() {
		String contentType = BatchJob.findArgumentValue("Content-Type", getArguments());
		return contentType != null ? contentType : "";
	}

	public BatchJobType getBatchJobType() {
		for (ScheduledJob scheduledJob : BatchJobManager.getScheduledJobAnnotationsForClass(getClass())) {
			if (scheduledJob.name().equals(getJobName())) {
				return BatchJobType.valueOf(scheduledJob.name());
			}
		}
		return null;

	}

	public void setJobId(String jobId) {
		try {
			Field privateField = FieldUtils.getDeclaredField(BatchJob.class, "jobId", true);
			FieldUtils.writeField(privateField, this, jobId);
		} catch (IllegalAccessException e) {
			logger.error("setJobId failed. ", e);
		}
	}

	public static BatchJobAuditLog findAuditTrail(BatchJobType pJobType, String pJobId, String pActionName,
			String pMessage) {
		boolean manageTransaction = !Application.hasActiveTransaction();
		try {
			if (manageTransaction) {
				PayrollServices.beginUnitOfWork();
			}

			Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().equalTo(getJobAuditId(pJobType, pJobId))
					.And(BatchJobAuditLog.JobAction().equalTo(pActionName)
							.And(BatchJobAuditLog.Message().equalTo(pMessage)));

			DomainEntitySet<BatchJobAuditLog> logEntries = PayrollServices.entityFinder.find(BatchJobAuditLog.class,
					where);

			BatchJobAuditLog logEntry = null;
			if (logEntries.size() > 0) {
				logEntry = logEntries.get(0);
			}

			if (manageTransaction) {
				PayrollServices.commitUnitOfWork();
			}

			return logEntry;
		} finally {
			if (manageTransaction) {
				PayrollServices.rollbackUnitOfWork();
			}
		}

	}

	public static DomainEntitySet<BatchJobAuditLog> findAuditTrail(BatchJobType pJobType, String pActionName,
			String pMessage, TimeConstraint timeConstraint) {
		boolean manageTransaction = !Application.hasActiveTransaction();
		try {
			if (manageTransaction) {
				PayrollServices.beginUnitOfWork();
			}

			Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(getJobNamespace(pJobType) + "%")
					.And(BatchJobAuditLog.JobAction().equalTo(pActionName).And(BatchJobAuditLog.Message()
							.equalTo(pMessage).And(BatchJobAuditLog.IsVerified().equalTo(false))));

			if (timeConstraint != null) {
				// Add time constraints for the batch jobs
				// Can't use between because there is a casting exception
				where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
						.And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));
			}

			DomainEntitySet<BatchJobAuditLog> logEntries = PayrollServices.entityFinder.find(BatchJobAuditLog.class,
					where);

			if (manageTransaction) {
				PayrollServices.commitUnitOfWork();
			}

			return logEntries;
		} finally {
			if (manageTransaction) {
				PayrollServices.rollbackUnitOfWork();
			}
		}

	}

	protected void sendToAuditTrail(BatchJobType pJobType, String pJobId, String pJobAction, String pMessage,
			String pMessageDetail) {
		try {
			getLogger().debug("Start Audit Trail " + pJobType.name() + " Job ID " + pJobId + " Job Action " + pJobAction
					+ " Message " + pMessage);
			PayrollServices.beginUnitOfWork();

			BatchJobAuditLog logEntry = new BatchJobAuditLog();

			logEntry.setJobNamespace(getJobAuditId(pJobType, pJobId));
			logEntry.setJobAction(pJobAction);
			logEntry.setMessage(pMessage);
			logEntry.setMessageDetail(pMessageDetail);
			logEntry.setIsVerified(false);

			Application.save(logEntry);

			PayrollServices.commitUnitOfWork();
			getLogger().debug("Finish Audit Trail " + pJobType.name() + " Job ID " + pJobId + " Job Action "
					+ pJobAction + " Message " + pMessage);
		} finally {
			PayrollServices.rollbackUnitOfWork();
		}
	}

	protected static String getJobAuditId(BatchJobType jobType, String jobId) {
		return getJobNamespace(jobType) + "/" + jobId;
	}

	public static String getJobNamespace(BatchJobType pJobType) {
		BatchJobSetup bjs = JSSBatchJobManager.getBatchJobSetup(pJobType);
		String namespace = bjs.getJobNamespace();

		if (!namespace.endsWith("/")) {
			namespace += "/";
		}

		return namespace + pJobType.toString();
	}

	public void setRunMode() {
		String[] arguments = getArguments();
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i].contains("job_schedule_service_job_name")) {
				this.runMode = RunMode.JOB_SCHEDULER_SERVICE;
				break;
			}
		}
	}

	public RunMode getRunMode() {
		return this.runMode;
	}

	private String getCommandLineArguments(String[] pArguments) {
		StringBuilder arguments = new StringBuilder();

		if (pArguments.length >= 2) {
			setJobName(pArguments[1]);
		}

		for (int i = 2; i < pArguments.length; i++) {
			if (StringUtils.contains(pArguments[i], JSSRequestHeaders.INSTANCE_ID)) {
				continue;
			}
			arguments.append(pArguments[i]);
			arguments.append(StringUtils.SPACE);
		}
		return arguments.toString();
	}

	private Object getJSSArguments() {
		getLogger().info("[JSSBatchJob] getArguments " + Arrays.toString(getArguments()));
		String arguments = BatchJob.findArgumentValue(JSSBatchJob.DATA, getArguments());
		getLogger().info("[JSSBatchJob] JSSBatchJob.DATA " + arguments);
        //second argument is a jobname in the arguments list, so setting the second argument to jobname.
		setJobName(getArguments()[1]);
		if (arguments == null) {
			return StringUtils.EMPTY;
		}
		if (!BatchUtils.isJSONValid(arguments)) {
			return arguments;
		}
		return new JsonParser().parse(arguments).getAsJsonObject();
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

		Criterion<BatchJobParameter> where = BatchJobParameter.BatchJobSetup().JobType().equalTo(getBatchJobType());

		DomainEntitySet<BatchJobParameter> paramEntries = PayrollServices.entityFinder.find(BatchJobParameter.class,
				where);

		for (BatchJobParameter param : paramEntries) {
			// Create dot separated name based on type, step and name
			String keyName = param.getBatchJobSetup().getJobType().toString() + "."
					+ (StringUtils.isNotBlank(param.getJobStep()) ? param.getJobStep() + "." : "") + param.getParamName();

			getLogger()
					.debug("Loaded batch job parameter " + keyName + " with value \"" + param.getParamValue() + "\"");

			parameters.put(keyName, param.getParamValue());
		}
	}

	/**
	 * Checks whether the exception is recoverable or not
	 *
	 * @param exception
	 * @return
	 */
	private boolean isErrorRecoverable(Exception exception) {
		/*
		 * As High frequency jobs run very often and StaleObjectException on the entity can be recovered during the next run.
		 */
		if (!SUPRESS_STALE_OBJECTS_BATCH_JOBS.contains(getBatchJobType())) {
			return false;
		}

		Throwable rootCause = ExceptionUtils.getRootCause(exception);
		if (exception instanceof StaleObjectStateException || exception instanceof OptimisticLockException || rootCause instanceof StaleObjectStateException) {
			return true;
		}

		return false;
	}

	private void isJobEnabledForDS2STGEnv() {
		if(!Application.isParallelEnv()) {
			return;
		}

		String triggeredJobName = getJobName();
		String env = Application.getEnvironmentName();
		String springProfile = Application.getSpringProfile();

		Set<String> enabledJobsSet = getEnabledJobsForDS2STGEnv();
		if(enabledJobsSet.isEmpty()) {
			logger.error("No JSS Jobs enabled triggeredJobName={} env={} springProfile={} enabledJobsSet={}", triggeredJobName, env, springProfile, enabledJobsSet);
			throw new RuntimeException(String.format("No JSS Jobs enabled triggeredJobName=%s env=%s springProfile=%s enabledJobsSet=%s", triggeredJobName, env, springProfile, enabledJobsSet));
		}

		if(enabledJobsSet.contains(triggeredJobName)) {
			logger.info("JSS Job enabled triggeredJobName={} env={} springProfile={} enabledJobsSet={}", triggeredJobName, env, springProfile, enabledJobsSet);
		} else {
			logger.error("JSS Job not enabled, failing the job triggeredJobName={} env={} springProfile={} enabledJobsSet={}",triggeredJobName, env, springProfile, enabledJobsSet);
			throw new RuntimeException("JSS Job not enabled, failing the job triggeredJobName="+triggeredJobName);
		}
	}

	private Set<String> getEnabledJobsForDS2STGEnv() {
		String enabledJobs = FeatureFlags.get().stringValue(FeatureFlags.Key.IS_DS2_STG_BATCH_JOB_ENABLED, "");
		logger.info("Enabled JSS Jobs  IXPFlag={} enabledJobs={}", FeatureFlags.Key.IS_DS2_STG_BATCH_JOB_ENABLED, enabledJobs);
		if(StringUtils.isEmpty(enabledJobs)) {
			return Collections.emptySet();
		}
		return new HashSet<>(Arrays.asList(enabledJobs.trim().split(",")));
	}
}
