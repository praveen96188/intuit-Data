package com.intuit.sbd.payroll.psp.tools.ach;

import com.intuit.sbd.payroll.psp.batchjobs.util.SftpAchFileManualOverride;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 20, 2010
 * Time: 11:51:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class AchUploadManualOverride {
    protected static final SpcfLogger sfLogger = Application.getLogger(AchUploadManualOverride.class);

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                throw new Exception("Invalid number of args. Usage: AchUploadManualOverride <job-id>");
            }

            PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

            sfLogger.info("Sending ACH files from BOS to alternate server...");

            // upload the ACH files from the BOS to the alternate ach upload server
            // and mark the related NACHAFile records as 'Transmitted'
            new SftpAchFileManualOverride().upload();

            sfLogger.info("Updating batch job audit log...");

            // update the audit log for the UploadAchFiles job step for the given job id
            updateBatchJobAuditLog(args[0]);

            sfLogger.info("Process complete.");
        } catch (Throwable t) {
            sfLogger.error(t);
            t.printStackTrace();
        }
    }

    public static void updateBatchJobAuditLog(String pJobId) {
        try {
            Application.beginUnitOfWork();

            Expression<BatchJobAuditLog> query;
            DomainEntitySet<BatchJobAuditLog> logEntries;
            String jobNamespace = "%/" + pJobId.toLowerCase();
            String jobAction = "UploadAchFiles";

            // retrieve the correct job namespace for this job id (should be either primary or secondary)
            query = new Query<BatchJobAuditLog>()
                    .Where(BatchJobAuditLog.JobNamespace().like(jobNamespace))
                    .OrderBy(BatchJobAuditLog.CreatedDate().Descending());

            logEntries = Application.find(BatchJobAuditLog.class, query);

            if (logEntries.isEmpty()) {
                throw new RuntimeException("Could not locate any audit log entries matching the given job id: " + pJobId);
            } else {
                String primaryNamespace = getJobNamespace(BatchJobType.PrimaryDailyBatchJobs, pJobId);
                String scheduledNamespace = getJobNamespace(BatchJobType.ScheduledDailyBatchJobs, pJobId);
                String namespace = logEntries.get(0).getJobNamespace();

                // retrieve the fully qualified job namespace associated to the given job id
                // (should be either /PSP/PrimaryDailyBatchJobs/<jobid> or /PSP/ScheduledDailyBatchJobs/<jobid>)
                if (primaryNamespace.equalsIgnoreCase(namespace) || scheduledNamespace.equalsIgnoreCase(namespace)) {
                    jobNamespace = namespace;
                } else {
                    // incompatible namespace associated with this job id
                    throw new RuntimeException("Incompatible namespace associated with the given job id (must be either " +
                                               BatchJobType.PrimaryDailyBatchJobs.toString() + " or " +
                                               BatchJobType.ScheduledDailyBatchJobs.toString() + "). " +
                                               "Found: " + namespace);
                }
            }

            sfLogger.info("Matched job id to namespace: " + jobNamespace);

            // check to see if a 'Started' record already exists for this job id (create if does not exist)
            query = new Query<BatchJobAuditLog>()
                    .Where(BatchJobAuditLog.JobNamespace().like(jobNamespace)
                            .And(BatchJobAuditLog.JobAction().equalTo(jobAction))
                            .And(BatchJobAuditLog.Message().equalTo("Started")))
                    .OrderBy(BatchJobAuditLog.CreatedDate().Descending());

            logEntries = Application.find(BatchJobAuditLog.class, query);

            if (logEntries.isEmpty()) {
                sfLogger.info("Creating 'Started' batch job audit log entry for: " +
                              "[JobAction]" + jobAction + " [JobNamespace]" + jobNamespace);

                // create the 'Finished' record for this job id in the audit log
                BatchJobAuditLog logEntryFinished = new BatchJobAuditLog();

                logEntryFinished.setJobAction(jobAction);
                logEntryFinished.setJobNamespace(jobNamespace);
                logEntryFinished.setMessage("Started");
                logEntryFinished.setMessageDetail("ACH upload manual override"); // notate that this had special handling
                logEntryFinished.setIsVerified(false);

                Application.save(logEntryFinished);
            }

            // check to see if a 'Finished' record already exists for this job id (create if does not exist)
            query = new Query<BatchJobAuditLog>()
                    .Where(BatchJobAuditLog.JobNamespace().equalTo(jobNamespace)
                            .And(BatchJobAuditLog.JobAction().equalTo(jobAction))
                            .And(BatchJobAuditLog.Message().equalTo("Finished")))
                    .OrderBy(BatchJobAuditLog.CreatedDate().Descending());

            logEntries = Application.find(BatchJobAuditLog.class, query);

            if (logEntries.isEmpty()) {
                sfLogger.info("Creating 'Finished' batch job audit log entry for: " +
                              "[JobAction]" + jobAction + " [JobNamespace]" + jobNamespace);

                // create the 'Finished' record for this job id in the audit log
                BatchJobAuditLog logEntryFinished = new BatchJobAuditLog();

                logEntryFinished.setJobAction(jobAction);
                logEntryFinished.setJobNamespace(jobNamespace);
                logEntryFinished.setMessage("Finished");
                logEntryFinished.setMessageDetail("ACH upload manual override"); // notate that this had special handling
                logEntryFinished.setIsVerified(false);

                Application.save(logEntryFinished);
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public static String getJobNamespace(BatchJobType pJobType, String pJobId) {
        BatchJobSetup bjs = PayrollServices.entityFinder.findById(BatchJobSetup.class, pJobType);
        String namespace = bjs.getJobNamespace();

        if (!namespace.endsWith("/")) {
            namespace += "/";
        }

        return namespace + pJobType.toString() + "/" + pJobId;
    }
}
