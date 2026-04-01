package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 22, 2009
 * Time: 2:13:02 PM
 * Uploads the Extracted files to ATF & TFS
 */
/* TODO : Renaming class as its upload files to both ATF & TFS (PSP-10946) */
public class ATFDataExtractProcessor extends BatchJobProcessor {
    SpcfLogger logger = Application.getLogger(ATFDataExtractProcessor.class);

    private class JobInfo {
        String name;
        String id;
        String action = null;
        boolean completed = false;
    }
    private List<JobInfo> mConcurrentJobs = new ArrayList<JobInfo>();

    private enum RemoteServerType {
        PRIMARY,
        SECONDARY
    }

    private int mYear=0;
    private int mQuarter=0;
    private String mRunType = ATFDataExtractRunType.UpdatedData.toString();
    private String mExtractBatchId;
    private boolean mOverrideExternalSystemCommunication = false;
    private String mDataLoadPropertiesFile = null;

    public static final int INVALID_ROW_ID_ERROR_CODE = 10632;
    private static final int WAIT_FOREVER = -1;

    public ATFDataExtractProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    public String getExtractBatchId() {
        return mExtractBatchId;
    }

    @Override
    public void validateRuntimeParameters() {
        logger.info("validateRuntimeParameters: Validating Runtime Parameters.");
        String parametersStr = getJobInstanceParameters();
        if (parametersStr != null) {
            String[] parameters = (parametersStr.trim()).split(" ");

            StringBuffer err = new StringBuffer();

            // Valid number of Parameters expected either 1 or 3 params OR 2 or 4 (in case of ftp override)
            //If running from BatchJobManager for an UPDATE where you want to communicate with external systems: ./BatchJobManager run ATFDataExtract UpdatedData true &
            //If running from BatchJobManager for an ALL for Q2 2011 where you want to communicate with external systems: ./BatchJobManager run ATFDataExtract QuarterlyData 2011 2 true &

            //Normally, UPDATEs are run via a scheduled Flux job after offload's update payroll status completes.
            // A TeamTrack has been entered to allow scheduling of an ALL via the operator console in SAP.
            if ((parameters.length == 1) || (parameters.length == 2) || (parameters.length == 3) || (parameters.length == 4)) {
                if (parameters.length == 1 || (parameters.length == 2)) {
                    // number of params is 1 or 2
                    if (parameters[0].equalsIgnoreCase(ATFDataExtractRunType.UpdatedData.toString())) {
                        mRunType = parameters[0];
                        if (parameters.length == 2) {
                            mOverrideExternalSystemCommunication = Boolean.valueOf(parameters[1]);
                        }
                        return;
                    } else if (parameters[0].equalsIgnoreCase(ATFDataExtractRunType.AnnualData.toString())) {
                        // W2 Counts Annual Extract for All Companies
                        mRunType = parameters[0];
                        if (parameters.length == 2) {
                            if ((parameters[1] == null) || (parameters[1].equalsIgnoreCase(""))) {
                                err.append("Invalid Parameter: '").
                                        append(parameters[1]).
                                        append("'. Please enter a Year. ");
                            } else {
                                mYear = Integer.parseInt(parameters[1]);
                            }
                            return;
                        }   else {
                            err.append("Invalid number of parameters.");
                        }
                    } else {
                        err.append("Invalid Parameter: '").
                                append(parameters[0]).
                                append("' OR invalid number of parameters. ");
                    }

                } else {
                    //number of params is 3 or 4
                    if (parameters[0].equalsIgnoreCase(ATFDataExtractRunType.QuarterlyData.toString())) {
                        mRunType = parameters[0];
                        if ((parameters[1] == null) || (parameters[1].equalsIgnoreCase(""))) {
                            err.append("Invalid Parameter: '").
                                    append(parameters[1]).
                                    append("'. Please enter a Year. ");
                        }

                        if ((parameters[2] == null) || (parameters[2].equalsIgnoreCase(""))) {
                            err.append("Invalid Parameter: '").
                                    append(parameters[2]).
                                    append("'. Please enter a valid Quarter ");
                        }

                        mYear = Integer.parseInt(parameters[1]);

                        int quarterVal =  Integer.parseInt(parameters[2]);
                        if (quarterVal > 0 && quarterVal < 5) {
                            mQuarter =  quarterVal;
                            if (parameters.length == 4) {
                                mOverrideExternalSystemCommunication = Boolean.valueOf(parameters[3]);
                            }
                            return;
                        } else {
                            err.append("Invalid Parameter: '").
                                    append(parameters[2]).
                                    append("'. Please enter a valid Quarter ");
                        }
                    } else if (parameters[0].equalsIgnoreCase(ATFDataExtractRunType.AnnualData.toString())) {
                        // W2 Counts Annual Extract for All Companies
                        mRunType = parameters[0];
                        if (parameters.length == 2 || parameters.length == 3) {
                            if ((parameters[1] == null) || (parameters[1].equalsIgnoreCase(""))) {
                                err.append("Invalid Parameter: '").
                                        append(parameters[1]).
                                        append("'. Please enter a Year. ");
                            } else {
                                mYear = Integer.parseInt(parameters[1]);
                            }
                            if(parameters.length == 3 && parameters[2] != null) {
                                mOverrideExternalSystemCommunication = Boolean.valueOf(parameters[2]);
                            }
                            return;
                        }   else {
                            err.append("Invalid number of parameters.");
                        }
                    }
                }
            } else {
                // Invalid number of params
                err.append("Invalid number of parameters.");
            }
            logger.error("ATFDataExtractProcessor: " + err.toString());
            throw new RuntimeException(err.toString());
        } else {
            // will default to running ATFDataExtract for Updated data
            mRunType =  ATFDataExtractRunType.UpdatedData.toString();
        }
    }

    @Override
    public void execute() {

        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        if (mRunType.equals(ATFDataExtractRunType.AnnualData.toString())) {
            if(SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_W2_COUNT_INFO_EXTRACT, false)) {
                executeStep(new CreateNewATFExtractBatchStep());
                executeStep(new W2CountsExtractStep());
            }
        } else {
            executeStep(new CreateFilingsSpecificTransactionsStep()); // perform some work prior to concurrent steps running
            executeStep(new CreateNewATFExtractBatchStep());
            executeStep(new CompanyLiabilitiesExtractStep());
            executeStep(new CompanyPaymentsExtractStep());

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_DEPOSIT_FREQUENCY_EXTRACT, false)) {
                executeStep(new DepositFrequencyExtractStep());
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_INFO_EXTRACT, false)) {
                executeStep(new CompanyInfoExtractStep());
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_INFO_EXTRACT, false)) {
                executeStep(new EmployeeInfoExtractStep());
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_WAGE_LIMITS_EXTRACT, false)) {
                executeStep(new WageLimitsExtractStep());
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT, false)) {
                executeStep(new CompanyTaxExtractStep());
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_RATE_EXTRACT, false)) {
                executeStep(new CompanyTaxRateExtractStep());
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT, false)) {
                executeStep(new EmployeeQuarterlyTotalsExtractStep());
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_PAYROLL_ITEMS_EXTRACT, false)) {
                executeStep(new CompanyPayrollItemExtractStep());
            }

        }
        //Only wait for jobs if we're using flux
        if (getRunMode() == RunMode.UsingFlux) {
            executeStep(new WaitForConcurrentJobs()); // waits for the concurrent jobs to complete before returning
        }

        //Only ftp and send the JMS message if
        // 1) we're using flux OR
        // 2) if we've overridden external system communication when running from the command line via BatchJobManager or during parallel testing
        if (getRunMode() == RunMode.UsingFlux || mOverrideExternalSystemCommunication) {
            // Upload files to ATF
            executeStep(new FTPFilesToATFStep());// if we're using flux, runs only if WaitForConcurrentJobs step completes successfully

            // Upload files to TFS (PSP-10946)
            executeStep(new FTPFilesToTFSStep());
        }

        updateATFDataExtractBatchStatus(SpcfUniqueId.createInstance(mExtractBatchId), ATFDataExtractBatchStatus.Completed);
        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    //Utility methods for waiting
    private boolean hasJobFinished(final String pJobId, final String pJobAction) {
        //
        // Poll the BATCH_JOB_AUDIT_LOG for the 'Finished' entry for the given job id and/or job action (job step)
        // (do this in a separate transaction thread for neatness so as not to pollute working cache with audit log noise)
        //

        return Application.executeTransactionThread(new TransactionThread<Boolean>() {
            public Boolean transaction() {
                Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like("%" + pJobId)
                        .And(BatchJobAuditLog.Message().equalTo("Finished"));

                if (pJobAction != null) {
                    where = where.And(BatchJobAuditLog.JobAction().equalTo(pJobAction));
                }

                boolean finished=false;

                try {
                    finished = !PayrollServices.entityFinder.find(BatchJobAuditLog.class, where).isEmpty();
                } catch (Throwable t) {
                    boolean handled = false;
                    while (t.getCause() != null && !handled) {
                        t = t.getCause();
                        if (t instanceof SQLException && ((SQLException) t).getErrorCode() == INVALID_ROW_ID_ERROR_CODE) {
                            finished = false;
                            handled = true;
                        }
                    }

                    if (!handled) {
                        throw new RuntimeException(t);
                    }
                }

                return finished;

            }
        });
    }

    private boolean waitForJobs(int pWaitTimeout) {
        //
        // Waits up to pWaitTimeout millis for the given job to complete, polling every 60 seconds.
        //

        boolean jobsRunning = true;
        StopWatch sw = StopWatch.startTimer();

        // While there are still jobs running and we still have time.
        while (jobsRunning && (pWaitTimeout == WAIT_FOREVER || sw.getElapsedMillis() < pWaitTimeout)) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Check on the status of each non-complete job.
            for (JobInfo job : mConcurrentJobs) {
                if (!job.completed) {
                    if (hasJobFinished(job.id, job.action)) {
                        job.completed = true;
                        logger.info("Concurrent job " + job.name + " has completed");
                    }
                }
            }

            // See if there is anything left to do.
            jobsRunning = false;
            for (JobInfo job : mConcurrentJobs) {
                if (!job.completed) {
                    jobsRunning = true;
                }
            }

        }

        // All jobs should be done unless we ran out of time.
        return !jobsRunning;
    }


    //Utility methods for use by all extracts
    public void transportFile(String pFileName, ServerConfigDetail configObj) {
        int retryCount = 0;
        boolean retry = true;
        while(retry) {
            com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter sftp = null;
            if (configObj.getPassword() != null) {
                sftp = BatchUtils.getATFExtractSftpConnection(new JSchAdapter(), configObj.getHost(),
                        configObj.getUser(), configObj.getPassword(), configObj.getTimeout());
            } else {
                sftp = BatchUtils.getTFSExtractSftpConnection(new JSchAdapter(), configObj.getHost(),
                        configObj.getUser(), configObj.getPrivateKey(), configObj.getTimeout());
            }
            try {
                sftp.setLogger(logger);
                sftp.connect();
                sftp.changeRemoteDir(configObj.getDestDir());
                sftp.uploadFile(pFileName);
                retry = false;
            } catch (Exception e) {
                if (retryCount < configObj.getMaxRetries()) {
                    ++retryCount;
                    retry = true;
                    logger.info("SFTP failed on try number " + retryCount + ".  Sleeping for 10 seconds.", e);
                    try {
                        //Sleep for 10 seconds
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        //Do nothing
                    }
                    logger.info("Retrying SFTP...");
                } else {
                    logger.info("SFTP failed after maximum number of " + configObj.getMaxRetries() + " retries.");
                    retry = false;
                    throw new RuntimeException(e.getMessage());
                }
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException("Error in disconnecting at ATF/TFS upload step (aborting process) ", e);
                }
            }
        }
    }

    private ServerConfigDetail getATFServerConfig(RemoteServerType serverType) {

        ServerConfigDetail serverConfig = null;

        if (serverType == RemoteServerType.PRIMARY) {
            serverConfig = getServerConfig(BatchUtils.getConfigString("psp_atf_extract_ftp_server"),
                    Integer.parseInt(BatchUtils.getConfigString("psp_atf_extract_ftp_port", "22")),
                    BatchUtils.getConfigString("psp_atf_ftp_username"),
                    BatchUtils.getConfigString("psp_atf_ftp_password"), null,
                    BatchUtils.getConfigString("psp_atf_ftp_destdir"),
                    Integer.parseInt(BatchUtils.getConfigString("psp_atfextract_ftp_connection_timeout", "10000")),
                    Integer.parseInt(BatchUtils.getConfigString("psp_atf_ftp_max_retry", "5")));
        } else {
            serverConfig = getServerConfig(BatchUtils.getConfigString("psp_atf_secondary_extract_ftp_server"),
                    Integer.parseInt(BatchUtils.getConfigString("psp_atf_secondary_extract_ftp_port", "22")),
                    BatchUtils.getConfigString("psp_atf_secondary_ftp_username"),
                    BatchUtils.getConfigString("psp_atf_secondary_ftp_password"), null,
                    BatchUtils.getConfigString("psp_atf_secondary_ftp_destdir"),
                    Integer.parseInt(BatchUtils.getConfigString("psp_atfextract_ftp_connection_timeout", "10000")),
                    Integer.parseInt(BatchUtils.getConfigString("psp_atf_secondary_ftp_max_retry", "5")));
        }

        return serverConfig;
    }

    private  ATFDataExtractBatch updateATFDataExtractBatchStatus(SpcfUniqueId pExtractBatchGUID, ATFDataExtractBatchStatus pStatus) {
        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, pExtractBatchGUID);
        extractBatch.setBatchStatus(pStatus);
        extractBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        Application.save(extractBatch);
        PayrollServices.commitUnitOfWork();
        return extractBatch;
    }

    // ***************************************************************************************************************
    //
    // Inner classes representing job steps for this batch job
    //
    // ***************************************************************************************************************
    public class CreateNewATFExtractBatchStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AtfDataExtractBatchJob));

                // CreateNewATFExtractBatchStep process
                ATFDataExtractBatch extractBatch = createATFDataExtractBatch();
                mExtractBatchId = extractBatch.getId().toString();
                //update the status of the batch to in progress
                updateATFDataExtractBatchStatus(extractBatch.getId(), ATFDataExtractBatchStatus.InProgress);

            }
            catch (Throwable ex) {
                logger.fatal("Exception in CreateFilingsSpecificTransactions.main() ", ex);
                System.exit(1);
            }
        }

        private ATFDataExtractBatch createATFDataExtractBatch (){
            logger.info("createATFDataExtractBatch: Creating ATF DataExtract Batch.");
            PayrollServices.beginUnitOfWork();
            ATFDataExtractBatch extractBatch =  new ATFDataExtractBatch();
            extractBatch.setYear(mYear);
            extractBatch.setQuarter(mQuarter);
            extractBatch.setStartDate(PSPDate.getPSPTime());
            extractBatch.setRunType(ATFDataExtractRunType.valueOf(mRunType));
            extractBatch.setBatchStatus(ATFDataExtractBatchStatus.Submitted);
            extractBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            extractBatch.setBatchId(getNextATFBatchId());
            Application.save(extractBatch);
            PayrollServices.commitUnitOfWork();
            return extractBatch;
        }
    }

    synchronized public static long getNextATFBatchId() {
        return Application.executeTransactionThread(new TransactionThread<Long>() {
            public Long transaction() {
                return Application.nextSequenceValue(SequenceId.SEQ_ATF_BATCH_ID_NBR, Long.class);
            }
        });
    }

    public class CreateFilingsSpecificTransactionsStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AtfDataExtractBatchJob));

                // CreateFilingsSpecificTransactions process
                try {
                    PayrollServices.beginUnitOfWork();

                    new CreateFilingsSpecificTransactions().processCompanyDailyLiabilities(CreateFilingsSpecificTransactions.UPDATE, null, null);

                    PayrollServices.commitUnitOfWork();
                }
                finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
            catch (Throwable ex) {
                logger.fatal("Exception in CreateFilingsSpecificTransactions.main() ", ex);
                System.exit(1);
            }
        }
    }

    public class CompanyLiabilitiesExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFCompanyLiabilityExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFCompanyLiabilityExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new CompanyLiabilitiesExtractProcess(getRunMode(), BatchJobType.ATFCompanyLiabilityExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class CompanyPaymentsExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFCompanyPaymentExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFCompanyPaymentExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new CompanyPaymentsExtractProcess(getRunMode(), BatchJobType.ATFCompanyPaymentExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class DepositFrequencyExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFDepositFrequencyExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFDepositFrequencyExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new DepositFrequencyExtractProcess(getRunMode(), BatchJobType.ATFDepositFrequencyExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class CompanyInfoExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFCompanyInfoExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFCompanyInfoExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new CompanyInfoExtractProcess(getRunMode(), BatchJobType.ATFCompanyInfoExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class EmployeeInfoExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFEmployeeInfoExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFEmployeeInfoExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new EmployeeInfoExtractProcess(getRunMode(), BatchJobType.ATFEmployeeInfoExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class WageLimitsExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFWageLimitsExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFWageLimitsExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new WageLimitsExtractProcess(getRunMode(), BatchJobType.ATFWageLimitsExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class CompanyTaxExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFCompanyTaxExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFCompanyTaxExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new CompanyTaxExtractProcess(getRunMode(), BatchJobType.ATFCompanyTaxExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class CompanyTaxRateExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFCompanyTaxRateExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFCompanyTaxRateExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new CompanyTaxRateExtractProcess(getRunMode(), BatchJobType.ATFCompanyTaxRateExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class EmployeeQuarterlyTotalsExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFEmployeeTotalsExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFEmployeeTotalsExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new EmployeeTotalsExtractProcess(getRunMode(), BatchJobType.ATFEmployeeTotalsExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class W2CountsExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.W2CountsExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.W2CountsExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new W2CountsExtractProcess(getRunMode(), BatchJobType.W2CountsExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }

    public class WaitForConcurrentJobs extends BatchJobProcessorStep {

        public void execute() {
            for ( JobInfo job : mConcurrentJobs ) {
                logger.info("Waiting for " + job.name + ", job id: " + job.id);
            }

            // Wait as long as it takes...
            if (!waitForJobs(WAIT_FOREVER)) {
                for ( JobInfo job : mConcurrentJobs ) {
                    if (!job.completed) {
                        logger.error("Concurrent job " + job.name + " failed to complete (job id: " + job.id + ")");
                    }
                }
                throw new RuntimeException("Concurrent ATF jobs failed to complete in the allotted time");
            }
        }
    }

    private class FTPFilesToATFStep extends BatchJobProcessorStep {
        public void execute() {
            logger.info("Sending Extract files to ATF");
            PayrollServices.beginUnitOfWork();

            ServerConfigDetail primaryServerConfig = getATFServerConfig(RemoteServerType.PRIMARY);
            ServerConfigDetail secondaryServerConfig = getATFServerConfig(RemoteServerType.SECONDARY);

            for (ATFDataExtractFile atfDataExtractFile : getExtractFilesToUpload()) {
                logger.info("Initiating ATF File upload for SFTP (Primary)");
                transportFile(atfDataExtractFile.getFileName(), primaryServerConfig);

                // If we are configured to send to a secondary server, attempt that send as well.
                if (SystemParameter.findBooleanValue(SystemParameter.Code.ATF_SEND_TO_SECONDARY_FTP_SERVER, false)) {
                    logger.info("Initiating ATF File upload for SFTP (Secondary)");
                    transportFile(atfDataExtractFile.getFileName(), secondaryServerConfig);
                }
            }
            PayrollServices.commitUnitOfWork();
        }
    }

    private class FTPFilesToTFSStep extends BatchJobProcessorStep {

        public void execute() {
            logger.info("Sending Extract files to TFS");
            PayrollServices.beginUnitOfWork();

            // Reading TFS server config details
            ServerConfigDetail serverConfig = getServerConfig(BatchUtils.getConfigString("psp_tfs_ftp_host"),
                    Integer.parseInt(BatchUtils.getConfigString("psp_tfs_ftp_port", "22")),
                    BatchUtils.getConfigString("psp_tfs_ftp_username"),
                    null, BatchUtils.getConfigString("psp_tfs_ftp_private_key"),
                    BatchUtils.getConfigString("psp_tfs_ftp_destdir"),
                    Integer.parseInt(BatchUtils.getConfigString("psp_tfs_ftp_connection_timeout", "10000")),
                    Integer.parseInt(BatchUtils.getConfigString("psp_tfs_ftp_max_retry", "5")));

            for (ATFDataExtractFile atfDataExtractFile : getExtractFilesToUpload()) {
                // upload file to TFS
                transportFile(atfDataExtractFile.getFileName(), serverConfig);
            }
            logger.info("Successfully sent all Extract files to TFS");

            if(!mRunType.equalsIgnoreCase(ATFDataExtractRunType.AnnualData.toString())) {
                logger.info("Sending Property file " + mDataLoadPropertiesFile.substring(
                        mDataLoadPropertiesFile.lastIndexOf(File.separator)+1) + " to TFS");
                //FTP dataLoad properties file
                transportFile(mDataLoadPropertiesFile, serverConfig);
                logger.info("Successfully sent Property file to TFS");
            }

            PayrollServices.commitUnitOfWork();
        }
    }

    public class CompanyPayrollItemExtractStep extends BatchJobProcessorStep {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            if (getRunMode() == RunMode.UsingFlux) {
                JobInfo jobInfo = new JobInfo();
                jobInfo.name = BatchJobType.ATFCompanyPayrollItemExtract.toString();

                BatchJobManager batchJobManager = new BatchJobManager();
                jobInfo.id = batchJobManager.scheduleJob(BatchJobType.ATFCompanyPayrollItemExtract, mExtractBatchId+"%");

                mConcurrentJobs.add(jobInfo);
            } else {
                new CompanyPayrollItemExtractProcess(getRunMode(), BatchJobType.ATFCompanyPayrollItemExtract, getJobId(), mExtractBatchId+"%").execute();
            }
        }
    }


    private ServerConfigDetail getServerConfig(String host, int port, String username, String password,
                                               String privateKey, String destDir, int connTimeout, int maxRetry) {

        ServerConfigDetail configObj = new ServerConfigDetail();
        configObj.setHost(host);
        configObj.setPort(port);
        configObj.setUser(username);
        configObj.setPassword(password);
        configObj.setPrivateKey(privateKey);
        configObj.setDestDir(destDir);
        configObj.setMaxRetries(maxRetry);
        configObj.setTimeout(connTimeout);

        return configObj;
    }

    private DomainEntitySet<ATFDataExtractFile> getExtractFilesToUpload() {
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(mExtractBatchId));
        DomainEntitySet<ATFDataExtractFile> dataExtractFilesToFtp = extractBatch.getATFDataExtractFilesToFTP();
        return dataExtractFilesToFtp;
    }

}
