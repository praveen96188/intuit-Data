package com.intuit.sbd.payroll.psp.jss.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CreateFilingsSpecificTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.ServerConfigDetail;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.jss.*;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 4/23/17
 * Time: 8:28 AM
 * To change this template use File | Settings | File Templates.
 */

@ScheduledJob(name="ATFDataExtract", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ATFDataExtractProcessor extends JSSBatchJob {

    /*
     * SimpleSftpFile accepts only SpcfLogger which is incompatible with SLF4J logger. This solution is a workaround.
     *
     * TODO Change the SimpleSftpFile to accept the SLF4J logger as part of the cleanup work
     */
    private SpcfLogger spcfLogger = Application.getLogger(ATFDataExtractProcessor.class);

    public ATFDataExtractProcessor(String[] pArguments) {
        super(pArguments);
    }

    public ATFDataExtractProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }



    private static class JobInfo {
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

    public String getExtractBatchId() {
        return mExtractBatchId;
    }

    @Override
    public void validateRuntimeParameters() {
        getLogger().info("validateRuntimeParameters: Validating Runtime Parameters.");
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
            getLogger().error("ATFDataExtractProcessor: " + err.toString());
            throw new RuntimeException(err.toString());
        } else {
            // will default to running ATFDataExtract for Updated data
            mRunType =  ATFDataExtractRunType.UpdatedData.toString();
        }
    }

    @Override
    public void execute() {

        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        if (mRunType.equals(ATFDataExtractRunType.AnnualData.toString())) {
            if(SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_W2_COUNT_INFO_EXTRACT, false)) {
                executeStep(CreateNewATFExtractBatchStep.class);
                executeStep(W2CountsExtractStep.class);
            }
        } else {
            executeStep(CreateFilingsSpecificTransactionsStep.class); // perform some work prior to concurrent steps running
            executeStep(CreateNewATFExtractBatchStep.class);
            executeStep(CompanyLiabilitiesExtractStep.class);
            executeStep(CompanyPaymentsExtractStep.class);

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_DEPOSIT_FREQUENCY_EXTRACT, false)) {
                executeStep(DepositFrequencyExtractStep.class);
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_INFO_EXTRACT, false)) {
                executeStep(CompanyInfoExtractStep.class);
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_INFO_EXTRACT, false)) {
                executeStep(EmployeeInfoExtractStep.class);
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_WAGE_LIMITS_EXTRACT, false)) {
                executeStep(WageLimitsExtractStep.class);
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT, false)) {
                executeStep(CompanyTaxExtractStep.class);
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_RATE_EXTRACT, false)) {
                executeStep(CompanyTaxRateExtractStep.class);
            }

            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT, false)) {
                executeStep(EmployeeQuarterlyTotalsExtractStep.class);
            }
            if (SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_PAYROLL_ITEMS_EXTRACT, false)) {
                executeStep(CompanyPayrollItemExtractStep.class);
            }

        }
        //Only wait for jobs if we're using flux
        //if (getRunMode() == BatchJobProcessor.RunMode.UsingFlux) {
        executeStep(WaitForConcurrentJobs.class); // waits for the concurrent jobs to complete before returning
        //}

        //Only ftp and send the JMS message if
        // 1) we're using flux OR
        // 2) if we've overridden external system communication when running from the command line via BatchJobManager or during parallel testing
        if (getRunMode() == RunMode.JOB_SCHEDULER_SERVICE || mOverrideExternalSystemCommunication) {
            // Upload files to TFS (PSP-10946)
            executeStep(FTPFilesToTFSStep.class);
            //archive files to S3
            executeStep(ArchiveFilesToS3.class);
        }

        updateATFDataExtractBatchStatus(SpcfUniqueId.createInstance(mExtractBatchId), ATFDataExtractBatchStatus.Completed);
        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
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
                boolean finished = false;
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
                        getLogger().info("Concurrent job " + job.name + " has completed");
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

    private ATFDataExtractBatch updateATFDataExtractBatchStatus(SpcfUniqueId pExtractBatchGUID, ATFDataExtractBatchStatus pStatus) {
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
    public static class CreateNewATFExtractBatchStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AtfDataExtractBatchJob));

                // CreateNewATFExtractBatchStep process
                ATFDataExtractBatch extractBatch = createATFDataExtractBatch();
                getBatchJobProcessor().mExtractBatchId = extractBatch.getId().toString();
                //update the status of the batch to in progress
                getBatchJobProcessor().updateATFDataExtractBatchStatus(extractBatch.getId(), ATFDataExtractBatchStatus.InProgress);

            }
            catch (Throwable ex) {
                getLogger().error("Exception in CreateFilingsSpecificTransactions.main() ", ex);
                System.exit(1);
            }
        }

        private ATFDataExtractBatch createATFDataExtractBatch (){
            getLogger().info("createATFDataExtractBatch: Creating ATF DataExtract Batch.");
            PayrollServices.beginUnitOfWork();
            ATFDataExtractBatch extractBatch =  new ATFDataExtractBatch();
            extractBatch.setYear(getBatchJobProcessor().mYear);
            extractBatch.setQuarter(getBatchJobProcessor().mQuarter);
            extractBatch.setStartDate(PSPDate.getPSPTime());
            extractBatch.setRunType(ATFDataExtractRunType.valueOf(getBatchJobProcessor().mRunType));
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

    public static class CreateFilingsSpecificTransactionsStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
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
                getLogger().error("Exception in CreateFilingsSpecificTransactions.main() ", ex);
                System.exit(1);
            }
        }
    }

    public static class CompanyLiabilitiesExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFCompanyLiabilityExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFCompanyLiabilityExtract.name(), getBatchJobProcessor().mExtractBatchId + "%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class CompanyPaymentsExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFCompanyPaymentExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFCompanyPaymentExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class DepositFrequencyExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFDepositFrequencyExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFDepositFrequencyExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class CompanyInfoExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //

            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFCompanyInfoExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFCompanyInfoExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class EmployeeInfoExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFEmployeeInfoExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFEmployeeInfoExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class WageLimitsExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFWageLimitsExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFWageLimitsExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class CompanyTaxExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFCompanyTaxExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFCompanyTaxExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class CompanyTaxRateExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFCompanyTaxRateExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFCompanyTaxRateExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class EmployeeQuarterlyTotalsExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFEmployeeTotalsExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFEmployeeTotalsExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class CompanyPayrollItemExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.ATFCompanyPayrollItemExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.ATFCompanyPayrollItemExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class W2CountsExtractStep extends JSSBatchJobStep<ATFDataExtractProcessor> {
        public void execute() {
            //
            // Scheduling another job will allow it to run independent of this job
            //
            JobInfo jobInfo = new JobInfo();
            jobInfo.name = BatchJobType.W2CountsExtract.toString();
            jobInfo.id = JSSBatchJobManager.scheduleJob(BatchJobType.W2CountsExtract.name(), getBatchJobProcessor().mExtractBatchId+"%");
            getBatchJobProcessor().mConcurrentJobs.add(jobInfo);
        }
    }

    public static class WaitForConcurrentJobs extends JSSBatchJobStep<ATFDataExtractProcessor> {

        public void execute() {
            for ( JobInfo job : getBatchJobProcessor().mConcurrentJobs ) {
                getLogger().info("Waiting for " + job.name + ", job id: " + job.id);
            }

            // Wait as long as it takes...
            if (!getBatchJobProcessor().waitForJobs(WAIT_FOREVER)) {
                for ( JobInfo job : getBatchJobProcessor().mConcurrentJobs ) {
                    if (!job.completed) {
                        getLogger().error("Concurrent job " + job.name + " failed to complete (job id: " + job.id + ")");
                    }
                }
                throw new RuntimeException("Concurrent ATF jobs failed to complete in the allotted time");
            }
        }
    }
    private String populatePropertiesFileForDataLoad(String dirName, String fileName) {

        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(getExtractBatchId()));
        DomainEntitySet<ATFDataExtractFile> atfDataExtractFilesToFTP = extractBatch.getATFDataExtractFilesToFTP();
        int extractFileCount = atfDataExtractFilesToFTP.size();

        String dataLoadPropertiesFile = dirName + File.separator + "PSP_" + extractBatch.getBatchId() + File.separator + fileName;

        try {
            int fileCounter = 1;
            PrintWriter pw = new PrintWriter(new FileOutputStream(dataLoadPropertiesFile));
            pw.println("dataloader.batchid=" + extractBatch.getBatchId());
            pw.println("dataloader.filecount=" + extractFileCount);
            for (ATFDataExtractFile atfDataExtractFile : atfDataExtractFilesToFTP) {
                pw.println("dataloader.quarter.file"+ fileCounter++ +"="+atfDataExtractFile.getFileName().substring(atfDataExtractFile.getFileName().lastIndexOf(File.separator)+1));
            }

            pw.flush();
            pw.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return dataLoadPropertiesFile;
    }

    public static class FTPFilesToTFSStep extends JSSBatchJobStep<ATFDataExtractProcessor> {

        public void execute() {
            getLogger().info("Sending Extract files to TFS");
            PayrollServices.beginUnitOfWork();

            for (ATFDataExtractFile atfDataExtractFile : getBatchJobProcessor().getExtractFilesToUpload()) {
                // upload file to TFS
                try{
                    uploadOnS3Bucket(atfDataExtractFile.getFileName());
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
            getLogger().info("Successfully sent all Extract files to TFS");

            if(!getBatchJobProcessor().mRunType.equalsIgnoreCase(ATFDataExtractRunType.AnnualData.toString())) {
                String dirName =  BatchUtils.getConfigString("psp_atf_send_dir");
                String fileName = "pspdataload_" + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmmss") + ".properties";
                //Create dataLoad properties file
                getBatchJobProcessor().mDataLoadPropertiesFile = getBatchJobProcessor().populatePropertiesFileForDataLoad( dirName,fileName);

                getLogger().info("Sending Property file " + getBatchJobProcessor().mDataLoadPropertiesFile.substring(
                        getBatchJobProcessor().mDataLoadPropertiesFile.lastIndexOf(File.separator) + 1) + " to TFS");
                //FTP dataLoad properties file
                try{
                    uploadOnS3Bucket(getBatchJobProcessor().mDataLoadPropertiesFile);
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
                getLogger().info("Successfully sent Property file to TFS");
            }

            PayrollServices.commitUnitOfWork();
        }
    }
    //ArchiveFilesToS3.class
    public static class ArchiveFilesToS3 extends JSSBatchJobStep<ATFDataExtractProcessor>{
        public void execute(){
            getLogger().info("Archiving  Extract files to S3");
            try{
                PayrollServices.beginUnitOfWork();
                for (ATFDataExtractFile atfDataExtractFile : getBatchJobProcessor().getExtractFilesToUpload()) {
                    // upload file to S3
                        String workingDir = FilenameUtils.getFullPath(atfDataExtractFile.getFileName());
                        getLogger().info("Archiving  Directory is:" + workingDir);
                        String fileName = atfDataExtractFile.getFileName();
                        getLogger().info("Archiving  file to S3 is:" + fileName);
                        S3UploadUtils.archive(BatchJobType.ATFDataExtract.name(),workingDir,fileName);
                    }
                PayrollServices.commitUnitOfWork();
            }
            catch (Exception e){
                getLogger().error("ArchiveFileToS3 Step failed");
                throw new RuntimeException(e);
            }
            finally{
                PayrollServices.rollbackUnitOfWork();
            }
            getLogger().info("Successfully sent all Extract files to S3");

        }
    }
    private static void uploadOnS3Bucket(String fileName) throws Exception {
        FileStore fileStore = BatchUtils.getFileStore();
        File fileSend = new File(fileName);
        fileStore.writeFile(BatchUtils.getConfigString("psp_tfs_s3_bucket"), BatchUtils.getConfigString("psp_tfs_s3_folder") + FilenameUtils.getName(fileName), fileSend);
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