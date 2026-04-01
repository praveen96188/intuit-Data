package com.intuit.sbd.payroll.psp.jss;

import com.google.gson.JsonObject;
import com.intuit.jss.client.model.Job;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.jss.util.BatchUtils;
import com.intuit.sbd.payroll.psp.jss.util.Log4jConfigurator;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagUtil;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.proxyInjector.service.ProxyServerSetup;
import com.intuit.sbg.shared.batchjob.BatchJobConfig;
import com.intuit.sbg.shared.batchjob.BatchJobConfigFactory;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.jss.client.JSSClientWrapper;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.util.concurrent.Future;

/**
 * Batch job manager for Job Scheduler Service (JSS)
 * <p/>
 * Entry point for all JSS batch job related activities. JSSBatchJobManager class is a wrapper around
 * com.intuit.sbg.shared.batchjob.BatchJobManager
 * <p/>
 * <p>
 * In addition to delegating the calls to BatchJobManager, it also initialize the Logging system
 * </p>
 *
 * @author kmuthurangam
 *
 */
public class JSSBatchJobManager {

    private static final Logger logger = LoggerFactory.getLogger(JSSBatchJobManager.class);

    public static void main(String[] args) {
        logger.info("Running JSSBatchJobManager for environmentName={} springProfile={}", Application.getEnvironmentName(), Application.getSpringProfile());

        Log4jConfigurator.configure();
        if(ProxyServerSetup.isProxyServerRequired()) {
            ProxyServerSetup.initialize();
        }

        // Initialise Lazy Feature Flags if its Parallel Env
        if(Application.isParallelEnv()) {
            FeatureFlagLazyLoader.getInstance().lazyLoadFeatureFlags();
            logger.info("Parallel Env Loaded Lazy Feature Flags="+ FeatureFlagUtil.getFeatureFlagStringSet(FeatureFlags.Key.PARALLEL_ENV_JSS_SCHEDULED_JOB_LIST));
        }

        BatchJobManager.main(args);
    }

    private static BatchJobConfig batchJobConfig;
    static {

        try {
            batchJobConfig = BatchJobConfigFactory.createInstance();

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

    }

    public static void updateJobSuspendStatus(String jobName, boolean suspend) {
        logger.info("Update the suspended status of the job with Group Name " + batchJobConfig.getGroupName()
                + " and Job Name " + jobName + " to " + suspend);
        Job job = JSSClientWrapper.findJobSchedule(batchJobConfig.getGroupName(), jobName);

        if (job == null) {
            logger.warn("Couldn't find job with Group Name " + batchJobConfig.getGroupName() + " and Job Name "
                    + jobName + " scheduled in JSS");
            return;
        }

        job.setSuspended(suspend);
        JSSClientWrapper.updateJobSchedule(job);
    }

    public static void runJob(String jobName) {
        Job job = JSSClientWrapper.findJobSchedule(batchJobConfig.getGroupName(), jobName);
        if (job == null) {
            logger.warn("Couldn't find job with Group Name " + batchJobConfig.getGroupName() + " and Job Name "
                    + jobName + " scheduled in JSS");
            return;
        }
        logger.info("Run job " + batchJobConfig.getGroupName() + " " + jobName);
        JSSClientWrapper.expediteJob(batchJobConfig.getGroupName(), jobName);
    }

    public static String scheduleJob(String jobName, String... args) {
        return scheduleJobWithTime(jobName, null, args);
    }

    static String getJobName(String displayJobName){
        String jobName[] = displayJobName.split("_");
        return jobName[0];

    }

    public static String scheduleJobWithTime(String jobName, String timerExpression, String... args) {
        String jobId = null;
        Job job = null;
        String displayJobName=null;
        try {
            displayJobName=jobName;
            jobName = getJobName(displayJobName);
            logger.info("displayJobName " + displayJobName + " jobName  " + jobName );
            Class<?> batchJobClass = BatchJobManager.findJobClass(batchJobConfig, jobName);
            for (ScheduledJob scheduledJob : BatchJobManager.getScheduledJobAnnotationsForClass(batchJobClass)) {
                if (scheduledJob.name().equals(jobName)) {
                    job = BatchJobManager.buildJssJob(BatchJobConfigFactory.createInstance(),
                            scheduledJob);
                    job.setName(displayJobName);
                    break;
                }
            }
            if(job==null){
                throw new RuntimeException("Could not find scheduled job:" + jobName);
            }
            jobId = SpcfUniqueId.generateRandomUniqueIdString();

            job.setData(getJson(jobId, args));
            // Remove the job before scheduling
            removeJob(batchJobConfig.getGroupName(), jobName);

            if(timerExpression != null) {
                job.setWhen(timerExpression);
            }

            // Generate the Job Id only if the scheduling is successful
            Future<Pair<ClientResponse, Job>> future = BatchJobManager.scheduleJob(job);
            Pair<ClientResponse, Job> pair = future.get();
            ClientResponse clientResponse = pair.getLeft();
            if (clientResponse.getClientResponseStatus() != Status.CREATED) {
                jobId = null;
            }
        } catch (Exception e) {
            logger.error("Error locating the Job class for the Job Name " + jobName, e);
            throw new RuntimeException(e.getMessage());
        }
        return jobId;
    }

    public static BatchJobSetup getBatchJobSetup(BatchJobType pJobType) {
        boolean manageTransaction = !Application.hasActiveTransaction();

        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            return PayrollServices.entityFinder.findById(BatchJobSetup.class, pJobType);
        }
        finally {
            if (manageTransaction) {
                PayrollServices.commitUnitOfWork();
            }
        }
    }

    /**
     * Remove the given job from the scheduler
     */
    protected static void removeJob(String groupName, String jobName) throws Exception {
        logger.info("Removing job: " + jobName);
        Job job = null;
        try {
            job = JSSClientWrapper.findJobSchedule(groupName, jobName);
        } catch (HystrixBadRequestException e) {
            if (e.getCause() != null &&
                    e.getCause() instanceof WebApplicationException &&
                    ((WebApplicationException) e.getCause()).getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                logger.info(jobName + " was not scheduled");
            } else {
                throw e;
            }
        }

        if (job != null) {
            JSSClientWrapper.deleteJobSchedule(groupName, jobName).get();
        }

        BatchJobManager.completeActiveJob(groupName, jobName);
    }

    private static String getJson(String jobId, String... args) {
        JsonObject jsonObject = new JsonObject();
        if (jobId != null) {
            jsonObject.addProperty(BatchUtils.JOB_ID, jobId);
        }
        jsonObject.addProperty(BatchUtils.ARGS, getBatchJobArguments(args));
        return jsonObject.toString();
    }

    private static String getBatchJobArguments(String... args) {
        StringBuffer batchJobArgs = new StringBuffer();
        for (String arg : args) {
            batchJobArgs.append(arg);
            batchJobArgs.append(StringUtils.SPACE);
        }
        return batchJobArgs.toString().trim();
    }

    private static void setBatchJobArguments(String... args) {

        String batchJobArgs = getBatchJobArguments(args);
		/*
		 * Schedule job command of com.intuit.sbg.shared.batchjob.BatchJobManager does not accept any arguments. One of the
		 * way to pass the arguments to the schedule job is via callbackDataGenerator attribute.
		 *
		 * Because of the dynamic nature of the arguments value, it is shared via Thread Context (ThreadLocal) variable
		 */
        ThreadLocalManager.setValue(batchJobArgs);
    }

}
