package com.intuit.sbd.payroll.psp.tools.ach;

import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 26, 2010
 * Time: 5:43:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class AchUploadManualOverrideTests {
    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();

        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));

        Company1Dataloader company = new Company1Dataloader();

        company.persistCompany1();

        Company1Dataloader.persistPayrollRun(company.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));

        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.commitUnitOfWork();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    private Boolean jobRanSuccessfully(BatchJobType pBatchJobType, String pJobId, String pActionName) {
        return jobRanSuccessfully(Application.<BatchJobSetup>findById(BatchJobSetup.class, pBatchJobType), pJobId, pActionName);
    }

    private Boolean jobRanSuccessfully(BatchJobSetup pJobSetup, String pJobid, String pActionName) {
        try {
            for (int i = 0; i < 5; i++) {
                if (BatchJobProcessor.findAuditTrail(pJobSetup.getJobType(), pJobid, pActionName, "Finished") != null) {
                    return true;
                }

                Thread.sleep(1000);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return false;
    }

    @Test
    public void testUpdateBatchJobAuditLog_ValidJobId() {
        // Run job PrimaryDailyBatchJobs, job step OffloadAchData (want batch job audit log updated)
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                   BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                   "OffloadAchData",
                                                                   PSPDate.getPSPTime().format("yyyyMMdd")});

        // confirm OffloadAchData job step ran successfully
        assertTrue("OffloadAchData job step successful",
                   jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "OffloadAchData"));

        // update batch job audit log to make it appear that the UploadAchFiles job step ran successfully
        AchUploadManualOverride.updateBatchJobAuditLog(jobId);

        // confirm system thinks UploadAchFiles job step ran successfully
        assertTrue("Batch job audit log updated (UploadAchFiles)",
                   jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "UploadAchFiles"));
    }

    @Test
    public void testUpdateBatchJobAuditLog_IncorrectJobId() {
        // Run job PrimaryDailyBatchJobs, job step OffloadAchData (want batch job audit log updated)
        String achJobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                      BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                      "OffloadAchData",
                                                                      PSPDate.getPSPTime().format("yyyyMMdd")});

        // confirm OffloadAchData job step ran successfully
        assertTrue("OffloadAchData job step successful",
                   jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, achJobId, "OffloadAchData"));

        // Run job LedgerBalance (want batch job audit log updated with different job id)
        String ledgerJobId = BatchJobManager.executeCommand(new String[]{"run", BatchJobType.LedgerBalance.toString()});

        // confirm LedgerBalance job steps ran successfully
        assertTrue("LedgerBalance job step successful (step UpdateLedgerBalance)",
                   jobRanSuccessfully(BatchJobType.LedgerBalance, ledgerJobId, "UpdateLedgerBalance"));

        try {
            // attempt to update batch job audit log with incorrect job id (use job id from ledger balance job)
            AchUploadManualOverride.updateBatchJobAuditLog(ledgerJobId);
        } catch (RuntimeException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            List<String> stack = Arrays.asList(sw.toString().split("\r?\n"));
            assertTrue(stack.toString().matches(".*Incompatible namespace associated with the given job id " +
                                                "\\(must be either PrimaryDailyBatchJobs or " +
                                                "ScheduledDailyBatchJobs\\)\\. Found: /PSP/HIGH/LedgerBalance/.*"));
        }

        // confirm system knows UploadAchFiles job step *did not* run successfully
        assertFalse("Batch job audit log not updated (UploadAchFiles)",
                    jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, achJobId, "UploadAchFiles"));
    }

    @Test
    public void testUpdateBatchJobAuditLog_InvalidJobId() {
        // Run job PrimaryDailyBatchJobs, job step OffloadAchData (want batch job audit log updated)
        String createAchJobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                            BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                            "OffloadAchData",
                                                                            PSPDate.getPSPTime().format("yyyyMMdd")});

        // confirm OffloadAchData job step ran successfully
        assertTrue("OffloadAchData job step successful",
                   jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, createAchJobId, "OffloadAchData"));

        try {
            // attempt to update batch job audit log with invalid job id
            AchUploadManualOverride.updateBatchJobAuditLog("xxxx");
        } catch (RuntimeException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            List<String> stack = Arrays.asList(sw.toString().split("\r?\n"));
            assertTrue(stack.toString().matches(".*Could not locate any audit log entries matching the given job id: xxxx.*"));
        }

        // confirm system knows UploadAchFiles job step *did not* run successfully
        assertFalse("Batch job audit log not updated (UploadAchFiles)",
                    jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, createAchJobId, "UploadAchFiles"));
    }
}
