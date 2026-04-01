package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.FluxUtils;
import com.intuit.sbd.payroll.psp.common.utils.Reflection;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Batch job manager
 * <p/>
 * Entry point for all batch job related activities in PSP.
 * <p/>
 * Through both command line and code interfaces, installs and clears recurring batch jobs in
 * a batch job scheduler (currently flux)
 * <p/>
 * Through an API interface, installs ad-hoc/immediate batch jobs in a batch job scheduler
 * (currently flux)
 * <p/>
 * Through a command line interface, runs any batch job without going through the batch job
 * scheduler
 * <p/>
 * Concepts:
 * <p/>
 * BatchJobType: an enumeration that holds all possible batch jobs in PSP
 * <p/>
 * BatchJob: data objects that hold all the setup settings necessary to execute a certain
 * batch job type (cronjob-type timer expression to specify when to run it,
 * batch job processor class name, delay between retries in case of failure)
 * <p/>
 * BatchJobProcessor: a process type class that contains (or delegates to other classes) the logic to
 * execute a certain batch job instance. See javadoc for that class for how to implement a processor)
 * <p/>
 */
public final class BatchJobManager {
    /**
     * Command line interface
     *
     * @param pArgs
     */
    public static void main(String[] pArgs) {
        ExitCode exitCode = ExitCode.Nominal;

        try {
           executeCommand(pArgs);
        }
        catch (IllegalArgumentException e) {
            exitCode = ExitCode.IllegalArgument;
            String errorMessage = "PSP Batch Job Manager failed with exit code " + exitCode.toString() + ".";

            System.out.println(errorMessage);

            logger.fatal(errorMessage, e);
            showUsage();
        }
        catch (RuntimeException e) {
            exitCode = ExitCode.RuntimeError;
            String errorMessage = "PSP Batch Job Scheduler failed with exit code " + exitCode.toString() + ".";

            System.out.println(errorMessage);
            e.printStackTrace();

            logger.fatal("PSP Batch Job Scheduler failed with exit code " + exitCode.toString() + ".", e);
        }
        catch (Exception e) {
            exitCode = ExitCode.ApplicationError;
            String errorMessage = "PSP Batch Job Scheduler failed with exit code " + exitCode.toString() + ".";

            System.out.println(errorMessage);
            e.printStackTrace();

            logger.fatal("PSP Batch Job Scheduler failed with exit code " + exitCode.toString() + ".", e);
        }

        System.exit(exitCode.getExitValue());
    }

    public static String runJob(BatchJobType pBatchJobType, String... pArgs) {
        ArrayList<String> cmdLineArgs = new ArrayList<String>();
        cmdLineArgs.add("run");
        cmdLineArgs.add(pBatchJobType.name());
        for (String arg : pArgs) {
            cmdLineArgs.add(arg);
        }
        return executeCommand(cmdLineArgs.toArray(new String[]{}));
    }

    public static String runJobStep(BatchJobType pBatchJobType, Class pJobStep, String... pArgs) {
        ArrayList<String> cmdLineArgs = new ArrayList<String>();
        cmdLineArgs.add("runstep");
        cmdLineArgs.add(pBatchJobType.name());
        cmdLineArgs.add(pJobStep.getSimpleName());
        if (pArgs != null && pArgs.length > 0) {
            for (String arg : pArgs) {
                if (arg.trim().length() > 0) {
                    cmdLineArgs.add(arg);
                }
            }
        }
        return executeCommand(cmdLineArgs.toArray(new String[]{}));
    }

    public static String executeCommand(String[] pArgs) {
        String jobId = "";
        PrimaryCommand command = parseCommandLine(Arrays.asList(pArgs));

        switch (command) {
            case unschedule:
                if (command.getJobType() == null) { // all jobs
                    new BatchJobManager(BatchJobProcessor.RunMode.UsingFlux).removeRecurringJobs();
                } else { // specific job
                    new BatchJobManager(BatchJobProcessor.RunMode.UsingFlux).removeRecurringJob(command.getJobType());
                }
                break;

            case schedule:
                if (command.getJobType() == null) { // all jobs
                    new BatchJobManager(BatchJobProcessor.RunMode.UsingFlux).removeRecurringJobs();
                    new BatchJobManager(BatchJobProcessor.RunMode.UsingFlux).scheduleRecurringJobs();
                } else { // specific job
                    new BatchJobManager(BatchJobProcessor.RunMode.UsingFlux).removeRecurringJob(command.getJobType());
                    new BatchJobManager(BatchJobProcessor.RunMode.UsingFlux).scheduleRecurringJob(command.getJobType());
                }
                break;

            case run:
                BatchJobType runJobType = command.getJobType();
                BatchJobSetup runJobSetup = getBatchJobSetup(runJobType);
                StringBuilder args = new StringBuilder();

                for (int i = 2; i < pArgs.length; i++) {
                    args.append(pArgs[i]);
                    args.append(" ");
                }

                jobId = command.getJobId().toString();

                new BatchJobManager(BatchJobProcessor.RunMode.NotUsingFlux).executeBatchJob(runJobSetup.getJobProcessorClassName(), runJobType, jobId, args.toString());
                break;

            case runstep:
                BatchJobType runstepJobType = command.getJobType();
                BatchJobSetup runstepJobSetup = getBatchJobSetup(runstepJobType);
                String jobStepName = pArgs[2];
                StringBuilder runstepArgs = new StringBuilder();

                for (int i = 3; i < pArgs.length; i++) {
                    runstepArgs.append(pArgs[i]);
                    runstepArgs.append(" ");
                }

                jobId = command.getJobId().toString();

                new BatchJobManager(BatchJobProcessor.RunMode.NotUsingFlux).executeBatchJobStep(runstepJobSetup.getJobProcessorClassName(), runstepJobType, jobId, jobStepName, runstepArgs.toString());
                break;

            case rerun:
                BatchJobType rerunJobType = command.getJobType();
                BatchJobSetup rerunJobSetup = getBatchJobSetup(rerunJobType);

                jobId = command.getJobId().toString();

                new BatchJobManager(BatchJobProcessor.RunMode.NotUsingFlux).executeBatchJob(rerunJobSetup.getJobProcessorClassName(), rerunJobType, jobId, "");
                break;

            case usage:
                showUsage();
                break;

            case verify:
                System.out.println("BatchJobManager deployed successfully.");
                break;
        }

        return jobId;
    }

    public BatchJobManager() {
        runMode = BatchJobProcessor.RunMode.UsingFlux;
    }

    public BatchJobManager(BatchJobProcessor.RunMode pRunMode) {
        runMode = pRunMode;
    }

    /**
     * Retrieve BatchJob settings and forward request to schedule a batch job to run
     *
     * @param pBatchJobType
     * @param pBatchJobInstanceParameters
     */
    public String scheduleJob(BatchJobType pBatchJobType, String pBatchJobInstanceParameters, String overrideSchedule) {
        return scheduleJob(getBatchJobSetup(pBatchJobType), pBatchJobInstanceParameters, overrideSchedule);
    }

    public String scheduleJob(BatchJobType pBatchJobType, String pBatchJobInstanceParameters) {
        return scheduleJob(pBatchJobType, pBatchJobInstanceParameters, null);
    }

    /**
     * Schedule a batch job to run according to the timer expression passed in BatchJobSetup
     * <p/>
     * The scheduler cluster must be running
     *
     * @param pBatchJobSetup
     */
    public String scheduleJob(BatchJobSetup pBatchJobSetup, String pBatchJobInstanceParameters, String overrideSchedule) {
        if (runMode == BatchJobProcessor.RunMode.NotUsingFlux) {
            System.out.println("================================================================");
            System.out.println("Trying to schedule a job when running from the command-line.");
            System.out.println("The only valid use case for that is when running a batch job ");
            System.out.println("that schedules other batch jobs from the command line.");
            System.out.println("");
            System.out.println("If that is the case, after the current batch job finishes,");
            System.out.println("manually run the job the code is trying to schedule by executing");
            System.out.println("the following from the command line:");
            System.out.println("");
            System.out.println("BatchJobManager run " + pBatchJobSetup.getJobType().toString() + " " + pBatchJobInstanceParameters);
            System.out.println("================================================================");
            System.out.println("");

            return "";
        }

        String jobParams = pBatchJobInstanceParameters;

        if ((jobParams == null) || (jobParams.length() == 0)) {
            jobParams = "<none>";
        }

        String newLine = System.getProperty("line.separator");
        String jobInfo = "Scheduling batch job:" + newLine;

        jobInfo += "  Job Name:           " + pBatchJobSetup.getJobType().toString() + newLine;
        jobInfo += "  Job Timer:          " + (overrideSchedule != null ? overrideSchedule : pBatchJobSetup.getJobTimerExpression()) + newLine;
        jobInfo += "  Job Auto-Scheduled: " + pBatchJobSetup.getIsAutomaticallyScheduled() + newLine;
        jobInfo += "  Job Retry Count:    " + pBatchJobSetup.getMaxRetries() + newLine;
        jobInfo += "  Job Retry Timer:    " + pBatchJobSetup.getDelayBetweenRetriesTimerExpression() + newLine;
        jobInfo += "  Job Class Name:     " + pBatchJobSetup.getJobProcessorClassName() + newLine;
        jobInfo += "  Job Parameters:     " + jobParams;

        logger.info(jobInfo);

        return FluxUtils.createBatchJob(pBatchJobSetup, pBatchJobInstanceParameters, overrideSchedule);
    }

    /**
     * Execute a batch job by instantianting its processor and running 'execute'
     *
     * @param pJobProcessorClassName
     * @param pJobType
     * @param pJobId
     * @param pJobParameters
     */
    public void executeBatchJob(String pJobProcessorClassName, BatchJobType pJobType, String pJobId, String pJobParameters) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.BatchJob);
            //Check if batch job can be run
            if(SystemParameter.findBooleanValue(SystemParameter.Code.BATCH_JOB_CONTROLLER_ENABLED) && !BatchJobController.canRunBatchJob(pJobType)){
                throw new RuntimeException("An instance of this batch job is already running, cannot run another instance simultaneously");
            }
            BatchJobController.batchJobStarted(pJobType);
            if (logger.isDebugEnabled())
                logger.debug("about to instantiate and execute job " + pJobType.toString() + " class=" + pJobProcessorClassName + " parameters=" + pJobParameters);

            BatchJobProcessor jobProcessor = (BatchJobProcessor) Reflection.createInstance(pJobProcessorClassName, new Class[]{BatchJobProcessor.RunMode.class, BatchJobType.class, String.class, String.class}, new Object[]{runMode, pJobType, pJobId, pJobParameters});
            jobProcessor.executeJob();
            BatchJobController.batchJobFinished(pJobType);
            if (logger.isDebugEnabled())
                logger.debug("finished instantiation and execution of job " + pJobType.toString() + " class=" + pJobProcessorClassName + " parameters=" + pJobParameters);
        }
        catch (Throwable t) {
            String errorMessage = "Error when executing " + pJobType.toString() + " class=" + pJobProcessorClassName + " parameters=" + pJobParameters + " " + t.getMessage();
            logger.fatal(errorMessage, t);
            System.out.println(errorMessage);

            throw new RuntimeException(errorMessage, t);
        }
    }

    /**
     * Execute a batch job by instantianting its processor and running 'execute'
     *
     * @param pJobProcessorClassName
     * @param pJobType
     * @param pJobId
     * @param pJobParameters
     */
    public void executeBatchJobStep(String pJobProcessorClassName, BatchJobType pJobType, String pJobId, String pJobStepName, String pJobParameters) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.BatchJob);

            if (logger.isDebugEnabled())
                logger.debug("about to instantiate and execute job step " + pJobType.toString() + " class=" + pJobProcessorClassName + "jobStep= " + pJobStepName + " parameters=" + pJobParameters);

            BatchJobProcessor jobProcessor = (BatchJobProcessor) Reflection.createInstance(pJobProcessorClassName, new Class[]{BatchJobProcessor.RunMode.class, BatchJobType.class, String.class, String.class}, new Object[]{runMode, pJobType, pJobId, pJobParameters});
            jobProcessor.executeJobStep(pJobStepName);

            if (logger.isDebugEnabled())
                logger.debug("finished instantiation and execution of job step " + pJobType.toString() + " class=" + pJobProcessorClassName + "jobStep= " + pJobStepName + " parameters=" + pJobParameters);
        }
        catch (Throwable t) {
            String errorMessage = "Error when executing " + pJobType.toString() + " class=" + pJobProcessorClassName + "jobStep= " + pJobStepName + " parameters=" + pJobParameters + " " + t.getMessage();
            logger.fatal(errorMessage, t);
            System.out.println(errorMessage);

            throw new RuntimeException(errorMessage, t);
        }
    }

    /**
     * Install the given job in the Flux engine (job must be defined in the batch job setup table and must be
     * configured as 'automatically scheduled')
     * @param pJobType The specific job to schedule
     */
    public void scheduleRecurringJob(BatchJobType pJobType) {
        boolean manageTransaction = !Application.hasActiveTransaction();

        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            BatchJobSetup bjs = getBatchJobSetup(pJobType);

            if (bjs == null) {
                throw new RuntimeException("No batch job setup found for specified batch job type (" +
                                           pJobType.toString() + ")");
            } else if (!bjs.getIsAutomaticallyScheduled()) {
                throw new RuntimeException("Specified batch job type is not configured for auto-scheduling (" +
                                           pJobType.toString() + ")");
            } else {
                scheduleJob(bjs, "", null);
            }
        }
        finally {
            if (manageTransaction) {
                PayrollServices.commitUnitOfWork();
            }
        }

    }

    /**
     * Install all recurring jobs by clearing and reinstalling them
     * <p/>
     * The scheduler cluster must not be running
     */
    public void scheduleRecurringJobs() {
        boolean manageTransaction = !Application.hasActiveTransaction();

        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            for (BatchJobSetup bjs : PayrollServices.entityFinder.<BatchJobSetup>findObjects(BatchJobSetup.class)) {
                if (bjs.getIsAutomaticallyScheduled()) {
                    scheduleJob(bjs, "", null);
                }
            }
        }
        finally {
            if (manageTransaction) {
                PayrollServices.commitUnitOfWork();
            }
        }

    }

    /**
     * Remove the given job from the Flux engine
     */
    public void removeRecurringJob(BatchJobType pJobType) {
        logger.info("Removing job: " + pJobType.toString());
        FluxUtils.removeBatchJob(BatchJobProcessor.getJobNamespace(pJobType));
    }

    /**
     * Remove all recurring jobs from the scheduler
     */
    public void removeRecurringJobs() {
        logger.info("Removing all jobs from Flux engine.");
        FluxUtils.removeBatchJobs();
    }

    /**
     * Static constructor and variables
     */
    private static final SpcfLogger logger = Application.getLogger(BatchJobManager.class);

    /**
     * Constructor and initialization
     */
    private BatchJobProcessor.RunMode runMode = BatchJobProcessor.RunMode.UsingFlux;

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

    // app/system exit enumeration
    private enum ExitCode {
        Nominal {public int getExitValue() {
            return 0;
        }},
        IllegalArgument {public int getExitValue() {
            return 1;
        }},
        RuntimeError {public int getExitValue() {
            return 2;
        }},
        ApplicationError {public int getExitValue() {
            return 3;
        }};

        public abstract int getExitValue();
    }

    private static PrimaryCommand parseCommandLine(List<String> pArgList) {
        if (pArgList.isEmpty()) {
            throw new IllegalArgumentException("No command arguments specified.");
        }

        PrimaryCommand command;

        try {
            command = PrimaryCommand.valueOf(pArgList.get(0).toLowerCase());

            //
            // Validate the parameters
            //
            switch (command) {
                case schedule:
                case unschedule:
                    if (!pArgList.get(1).equalsIgnoreCase("all")) {
                        command.setJobType(BatchJobType.valueOf(pArgList.get(1)));
                    }
                    break;

                case run:
                case runstep:
                    command.setJobType(BatchJobType.valueOf(pArgList.get(1)));
                    command.setJobId(SpcfUniqueId.generateRandomUniqueId());
                    break;

                case rerun:
                    command.setJobType(BatchJobType.valueOf(pArgList.get(1)));
                    command.setJobId(SpcfUniqueId.createInstance(pArgList.get(2)));

                    if (!BatchJobProcessor.jobExists(command.getJobType(), command.getJobId().toString())) {
                        throw new RuntimeException("To rerun a job, it must have been started before.");
                    }
                    break;

                default:
                    break;
            }
        }
        catch (Throwable t) {
            throw new IllegalArgumentException("Invalid command line argument specified: " + pArgList.toString() + " " + t.getMessage());
        }

        return command;
    }

    private static void showUsage() {
        System.out.println("Usage: BatchJobManager command [arg]");
        System.out.print("Command: [schedule, unschedule, run, runstep, rerun, usage]");
        System.out.println("   schedule      - Schedule (add to flux) all recurring PSP batch jobs");
        System.out.println("   unschedule    - Unschedule (clear from flux) all recurring PSP batch jobs");
        System.out.println("   run           - Run a specific batch job without flux");
        System.out.println("   runstep       - Run a specific batch job step without flux");
        System.out.println("   rerun         - Rerun a specific batch job without flux");
        System.out.println("   usage         - Show command line usage");
        System.out.println("   verify        - Verify that the batch job manager can properly execute");
        System.out.print("Argument: schedule [all, 'BatchJobType']);");
        System.out.print("Argument: unschedule [all, 'BatchJobType']);");
        System.out.print("Argument: run 'BatchJobType' params);");
        System.out.print("Argument: rerun 'BatchJobType' 'BatchJobIdAsGuid');");
        System.out.print("Argument: runstep 'BatchJobType' 'BatchJobTypeStep' params);");
        System.out.println("Sub-Command:");

        for (BatchJobType jobType : BatchJobType.values()) {
            System.out.println("   " + jobType.toString());
        }
    }

    private enum PrimaryCommand {
        schedule,
        unschedule,
        run,
        rerun,
        runstep,
        usage,
        verify;

        private BatchJobType mJobType;
        private SpcfUniqueId mJobId;

        public void setJobType(BatchJobType pJobType) {
            mJobType = pJobType;
        }

        public BatchJobType getJobType() {
            return mJobType;
        }

        public void setJobId(SpcfUniqueId pJobId) {
            mJobId = pJobId;
        }

        public SpcfUniqueId getJobId() {
            return mJobId;
        }
    }
}
