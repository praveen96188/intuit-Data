package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flux.*;

import java.io.FileInputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Oct 4, 2008
 * Time: 9:05:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class FluxUtils {
    private static final Factory fluxFactory;
    private static final Properties props;

    private static SpcfLogger logger = PayrollServices.getLogger(FluxUtils.class);

    static {
        fluxFactory = Factory.makeInstance();
        props = new Properties();

        ISpcfImmutableConfiguration config = ConfigurationManager.getSettings(ConfigurationModule.BatchJobs);
        for (SpcfPair<String, Object> pair : config.getConfigurationEntries()) {
            props.setProperty(pair.getKeyItem(), pair.getValueItem().toString());
        }
    }

    public static String createBatchJob(BatchJobSetup pBatchJobSetup, String pBatchJobInstanceParameters, String overrideSchedule) {
        String jobId = "";

        try {
            // Flux models jobs using flow charts. Create a flow chart.
            String flowChartName = BatchJobProcessor.getJobNamespace(pBatchJobSetup.getJobType());

            if (!pBatchJobSetup.getIsAutomaticallyScheduled()) {
                jobId = SpcfUniqueId.generateRandomUniqueIdString();
                flowChartName += "/" + jobId;
            }

            Engine fluxEngine = FluxUtils.getRunningFluxEngine();
            EngineHelper helper = FluxUtils.makeEngineHelper();
            FlowChart flowChart = helper.makeFlowChart(flowChartName);

            // Flow charts consist of actions and triggers. An action performs some
            // act, step, or task. A trigger waits for an event to occur.

            // Setup the java action to call the right batch job processor
            String[] classParts = pBatchJobSetup.getJobProcessorClassName().split("\\.");
            String defaultActionName = classParts[classParts.length - 1];

            // Create a timer trigger.
            String jobTimerExpression = pBatchJobSetup.getJobTimerExpression();
            if (overrideSchedule != null) {
                jobTimerExpression = overrideSchedule;
            }
            TimerTrigger timerTrigger = flowChart.makeTimerTrigger(defaultActionName + "TimerTrigger (" + jobTimerExpression + ")");

            // Fire this trigger according to timer expression
            timerTrigger.setTimeExpression(jobTimerExpression);

            // Don't allow any missed trigger firings to be made up
            timerTrigger.setLateTimeWindow("+1m", false);

            JavaAction jobAction = flowChart.makeJavaAction(defaultActionName + "Action");
            jobAction.setListener(BatchJobAction.class);
            jobAction.setKey(pBatchJobSetup.getJobProcessorClassName() + ":" + pBatchJobSetup.getJobType().toString() + ":" + pBatchJobInstanceParameters + ":" + jobId);

            // set flow from timer trigger to job action
            timerTrigger.addFlow(jobAction);

            if (pBatchJobSetup.getMaxRetries() > 0) {
                // Setup the delay trigger for failed attempts
                DelayTrigger delayTrigger = flowChart.makeDelayTrigger(defaultActionName + "DelayTrigger (" + pBatchJobSetup.getDelayBetweenRetriesTimerExpression() + ")");
                delayTrigger.setCount(pBatchJobSetup.getMaxRetries());
                delayTrigger.setDelayTimeExpression(pBatchJobSetup.getDelayBetweenRetriesTimerExpression());

                // set up the remaining flows
                jobAction.setErrorFlow(delayTrigger);
                delayTrigger.addFlow(jobAction);

                if (pBatchJobSetup.getIsAutomaticallyScheduled()) {
                    jobAction.setElseFlow(timerTrigger);  // action goes back to timer trigger if job is recurring
                    delayTrigger.setExpirationFlow(timerTrigger); // action goes back to timer trigger if retries expire
                }
            } else { // if retries are set to zero
                NullAction nullAction = flowChart.makeNullAction(defaultActionName + "NullAction (error)");

                // set up the remaining flows
                jobAction.setErrorFlow(nullAction);

                if (pBatchJobSetup.getIsAutomaticallyScheduled()) {
                    jobAction.setElseFlow(timerTrigger);  // action goes back to timer trigger if job is recurring
                    nullAction.addFlow(timerTrigger); // action goes back to timer trigger if job is recurring
                }
            }

            logger.info("flowChartName = " + flowChartName);
            if (shouldPauseFlowChartForThisEnvironment(flowChartName)) {
                logger.info("Pausing " + flowChartName);
                flowChart.pause();
            }

            // The job has been created. Add it to the fluxEngine.
            fluxEngine.put(flowChart);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return jobId;
    }

    public static void pauseBatchJob(String pBatchJobId) {
        try {
            getRunningFluxEngine().pause(pBatchJobId);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void resumeBatchJob(String pBatchJobId) {
        try {
            getRunningFluxEngine().resume(pBatchJobId);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void expeditBatchJob(String pBatchJobId) {
        try {
            getRunningFluxEngine().expedite(pBatchJobId);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static boolean isBatchJobScheduled(String pBatchJobId) {
        boolean isScheduled = false;

        try {
            isScheduled = (getRunningFluxEngine().get(pBatchJobId) != null);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return isScheduled;
    }

    public static void removeBatchJob(String pJobName) {
        try {
            Engine engine = FluxUtils.getRunningFluxEngine();
            engine.remove(pJobName);
        }
        catch (Exception e) {
            throw new RuntimeException("Error removing workflows from Flux engine.", e);
        }
    }

    public static void removeBatchJobs() {
        removeBatchJob("/PSP/*"); // remove all jobs
    }

    /**
     * Connect to existing scheduler cluster
     *
     * @return
     */
    public static Engine getRunningFluxEngine() {
        try {
            Configuration config = fluxFactory.makeConfiguration();

            config.setDatabaseType(DatabaseType.getInstance(props.getProperty("psp_batch_flux_db_type")));
            config.setDriver(props.getProperty("psp_batch_flux_db_driver"));
            config.setJdbcUsername(props.getProperty("psp_batch_flux_db_username"));
            config.setJdbcEncryptedPassword(props.getProperty("psp_batch_flux_db_password"));
            config.setUrl(props.getProperty("psp_batch_flux_db_url"));

            Cluster cluster = fluxFactory.lookupCluster(config);

            if (cluster.isSecured()) {
                cluster.setLoginInfo(props.getProperty("psp_batch_flux_engine_username"),
                        props.getProperty("psp_batch_flux_engine_password"));
            }

            return cluster.getReachableEngine();
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Instantiate a new local scheduler engine in a non-started status
     *
     * @return
     */
    public static Engine getNewFluxEngine() {
        try {
            String fecFile = Application.findFileOnClassPath("start-unsecured-flux-engine.fec");
            Properties fluxEngineProperties = new Properties();
            fluxEngineProperties.load(new FileInputStream(fecFile));

            Configuration fluxConfig = fluxFactory.makeConfiguration(fluxEngineProperties);
            return fluxFactory.makeEngine(fluxConfig);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static class BatchJobAction implements ActionListener {
        public Object actionFired(KeyFlowContext flowContext) throws Exception {
            String flowKey = (String) flowContext.getKey();
            //System.out.println("Action running for " + flowKey);
            String[] flowKeyParts = flowKey.split(":");

            String jobProcessorClassName = flowKeyParts[0];
            String jobTypeString = flowKeyParts[1];
            String jobParameters = "";
            if (flowKeyParts.length > 2) {
                jobParameters = flowKeyParts[2];
            }
            String jobId = "";
            if (flowKeyParts.length > 3) {
                jobId = flowKeyParts[3];
            }

            /**
             *  if job has no id (it is a recurring job), create one
             *  and store in flowContext.
             *  On a retry, the jobId will be retrieved from there.
             *  This is key for the re-entrancy of job steps
             *
             */
            if (jobId.length() == 0) {
                if (!flowContext.contains("JobId")) {
                    flowContext.put("JobId", SpcfUniqueId.generateRandomUniqueIdString());
                }
                jobId = (String) flowContext.get("JobId");
            }


            //System.out.println("Action running for " + jobProcessorClassName);
            new BatchJobManager().executeBatchJob(jobProcessorClassName, BatchJobType.valueOf(jobTypeString), jobId, jobParameters);

            /**
             *
             * We need to remove the jobId if the job is succesfull
             */
            if (flowContext.contains("JobId")) {
                flowContext.remove("JobId");
            }

            return null;
        }
    }

    private static boolean shouldPauseFlowChartForThisEnvironment(String pBatchJob) {
        Boolean response = false;
        try {
            String excludedString = BatchUtils.getConfigString("psp_paused_batch_jobs");
            if (excludedString != null && excludedString.length() > 0) {
                ArrayList<String> excludedList =
                        new ArrayList<String>(Arrays.asList(excludedString.split(",")));
                if (excludedList.contains(pBatchJob)) {
                    response = true;
                }
            }
        } catch (Exception e) {
            //suppress exception
        }
        return response;
    }

    private static EngineHelper makeEngineHelper() {
        return fluxFactory.makeEngineHelper();
    }

    public static String getTimerExpressionFromSpcfCalendar(SpcfCalendar calendar) {
        //Does not include year because Flux will break.  So don't try using this to schedule for more than a year.
        return String.format("%s %s %s %s %s %s * * * * *",
                calendar.getMillisecond(),
                calendar.getSecond(),
                calendar.getMinute(),
                calendar.getHour(),
                calendar.getDay(),
                calendar.getMonth() - 1);
    }

    public static Set<String> findAllScheduledJobs(){
        Set<String> scheduledJobNames = new HashSet<String>();
        try {
            FlowChartIterator flowChartIterator = getRunningFluxEngine().get();
            while(flowChartIterator.hasNext()){
                FlowChart flowChart = flowChartIterator.next();
                scheduledJobNames.add(flowChart.getName());
            }
        } catch (Exception e) {
            logger.error("Error finding jobs scheduled in flux " ,e);
        }
        return scheduledJobNames;
    }

    public static long getTodayScheduleTime(String timerExpression) {
        long calculatedScheduleTime = 0L;
        try {
            Factory factory = Factory.makeInstance();
            EngineHelper helper = factory.makeEngineHelper();

            Cron cron = helper.makeCron();
            cron.accept(timerExpression);

            SpcfCalendar calendar = PSPDate.getPSPTime();
            calendar.setValues(calendar.getYear(), calendar.getMonth(), calendar.getDay(), 0, 0, 0, 0); // Reset hour, minutes, seconds & milliseconds
            Date scheduledTime = cron.next(new Date(calendar.getTimeInMilliseconds())); // Get today scheduled time
            calculatedScheduleTime = scheduledTime.getTime();
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
        return calculatedScheduleTime;
    }

    public static long getNextScheduleTime(String timerExpression, SpcfCalendar calendar) {
        long calculatedScheduleTime = 0L;
        try {
            Factory factory = Factory.makeInstance();
            EngineHelper helper = factory.makeEngineHelper();

            Cron cron = helper.makeCron();
            cron.accept(timerExpression);

            calendar.setValues(calendar.getYear(), calendar.getMonth(), calendar.getDay(), 0, 0, 0, 0); // Reset hour, minutes, seconds & milliseconds
            Date scheduledTime = cron.next(new Date(calendar.getTimeInMilliseconds())); // Get today scheduled time
            calculatedScheduleTime = scheduledTime.getTime();
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
        return calculatedScheduleTime;
    }
}
