package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.monitors.AchReturnsMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.monitors.LedgerBalanceMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.monitors.OffloadedTransactionsEventsMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.processors.LedgerBalanceProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.OffloadedTransactionsEventsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PrimaryDailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.FluxUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.TimeConstraint;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import flux.Engine;
import org.junit.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Sep 22, 2008
 * Time: 8:33:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class BatchJobManagerTests {
    private static Engine engine;
    private static final int MAX_SECONDS = 5;

    @BeforeClass
    public static void initializeTests() {
        if (!ConfigurationManager.getEnvironmentIdentifier().equals("build")) {
            assertTrue (true);
            return;
        }

        engine = FluxUtils.getNewFluxEngine();
        try {
            engine.start();
            FluxUtils.removeBatchJobs();
            PayrollServicesTest.beforeEachTest();
            PayrollServicesTest.truncateTables();
            PayrollServicesTest.updateTables();
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @AfterClass
    public static void unitializeTests() {
        if (!ConfigurationManager.getEnvironmentIdentifier().equals("build")) {
            assertTrue (true);
            return;
        }

        try {
            engine.dispose();
            PayrollServicesTest.afterEachTest();
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.updateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void TestJobRunsSuccesfully() {
        if (!ConfigurationManager.getEnvironmentIdentifier().equals("build")) {
            assertTrue (true);
            return;
        }

        try {
            PayrollServices.beginUnitOfWork();

            BatchJobSetup offloadedTransactionsEventsJob = PayrollServices.entityFinder.<BatchJobSetup>findById(BatchJobSetup.class, BatchJobType.OffloadedTransactionsEvents);
            String jobId = new BatchJobManager().scheduleJob(offloadedTransactionsEventsJob, "", null);
            assertTrue (jobRanSuccessfully(offloadedTransactionsEventsJob, jobId, OffloadedTransactionsEventsProcessor.CreateTransactionOffloadedEvents.class.getSimpleName()));
        }
        finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void TestMonitorRunsSuccesfully() {
        if (!ConfigurationManager.getEnvironmentIdentifier().equals("build")) {
            assertTrue (true);
            return;
        }

        try {
            PayrollServices.beginUnitOfWork();

            BatchJobSetup offloadedTransactionsEventsJob = PayrollServices.entityFinder.<BatchJobSetup>findById(BatchJobSetup.class, BatchJobType.OffloadedTransactionsEvents);
            String jobId = new BatchJobManager().scheduleJob(offloadedTransactionsEventsJob, "", null);
            assertTrue (jobRanSuccessfully(offloadedTransactionsEventsJob, jobId, OffloadedTransactionsEventsProcessor.CreateTransactionOffloadedEvents.class.getSimpleName()));

            BatchJobSetup offloadedTransactionsEventsJobMonitor = PayrollServices.entityFinder.<BatchJobSetup>findById(BatchJobSetup.class, BatchJobType.OffloadedTransactionsEventsMonitor);
            String jobMonitorId = new BatchJobManager().scheduleJob(offloadedTransactionsEventsJobMonitor, jobId, null);
            FluxUtils.expeditBatchJob(BatchJobProcessor.getJobNamespace(offloadedTransactionsEventsJobMonitor.getJobType()) + "/" + jobMonitorId);
            assertTrue (jobRanSuccessfully(offloadedTransactionsEventsJobMonitor, jobMonitorId, OffloadedTransactionsEventsMonitor.MonitorProcessorStep.class.getSimpleName()));

        }
        finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void TestRecurringJobRunsSuccesfully() {
        if (!ConfigurationManager.getEnvironmentIdentifier().equals("build")) {
            assertTrue (true);
            return;
        }

        try {
            PayrollServices.beginUnitOfWork();
            BatchJobSetup ledgerBalanceJob = PayrollServices.entityFinder.<BatchJobSetup>findById(BatchJobSetup.class, BatchJobType.LedgerBalance);
            String jobId = new BatchJobManager().scheduleJob(ledgerBalanceJob, "", null);

            FluxUtils.expeditBatchJob(BatchJobProcessor.getJobNamespace(ledgerBalanceJob.getJobType()));

            assertTrue (recurringJobRanSuccessfully(ledgerBalanceJob, OffloadedTransactionsEventsProcessor.CreateTransactionOffloadedEvents.class.getSimpleName()));
        }
        finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void TestMonitorForRecurringJobRunsSuccesfully() {
        if (!ConfigurationManager.getEnvironmentIdentifier().equals("build")) {
            assertTrue (true);
            return;
        }

        try {
            PayrollServices.beginUnitOfWork();
            FluxUtils.removeBatchJobs();
            BatchJobSetup ledgerBalanceJob = PayrollServices.entityFinder.<BatchJobSetup>findById(BatchJobSetup.class, BatchJobType.LedgerBalance);
            String jobId = new BatchJobManager().scheduleJob(ledgerBalanceJob, "", null);
            FluxUtils.expeditBatchJob(BatchJobProcessor.getJobNamespace(ledgerBalanceJob.getJobType()));
            assertTrue (recurringJobRanSuccessfully(ledgerBalanceJob, LedgerBalanceProcessor.UpdateLedgerBalance.class.getSimpleName()));

            BatchJobSetup ledgerBalanceJobMonitor = PayrollServices.entityFinder.<BatchJobSetup>findById(BatchJobSetup.class, BatchJobType.LedgerBalanceMonitor);
            String jobMonitorId = new BatchJobManager().scheduleJob(ledgerBalanceJobMonitor, "", null);
            FluxUtils.expeditBatchJob(BatchJobProcessor.getJobNamespace(ledgerBalanceJobMonitor.getJobType()));
            assertTrue (recurringJobRanSuccessfully(ledgerBalanceJobMonitor, LedgerBalanceMonitor.MonitorProcessorStep.class.getSimpleName()));

        }
        finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    private Boolean jobRanSuccessfully(BatchJobType pBatchJobType, String pJobId, String pActionName) {
        return jobRanSuccessfully(Application.<BatchJobSetup>findById(BatchJobSetup.class, pBatchJobType), pJobId, pActionName);
    }

    private Boolean jobRanSuccessfully(BatchJobSetup pJobSetup, String pJobid, String pActionName) {
        try {
            for (int i = 0; i < MAX_SECONDS; i++) {
                if (BatchJobProcessor.findAuditTrail(pJobSetup.getJobType(), pJobid, pActionName, "Finished") != null) {
                    return true;
                }

                Thread.sleep(1000);
            }
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return false;

    }
    
    /** Verifies that a monitor loads the parameters correctly */
    @Test
    public void TestMonitorParameter() {
        AchReturnsMonitor monitor = new AchReturnsMonitor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AchReturnsMonitor, "", "");

        assertTrue("No Parameters found.", monitor.getParameters().size() != 0);
    }

    private Boolean recurringJobRanSuccessfully(BatchJobSetup pJobSetup, String pActionName) {
        try {
            for (int i = 0; i < MAX_SECONDS; i++) {
                if (BatchJobProcessor.findAuditTrail(pJobSetup.getJobType(), pActionName, "Finished", (TimeConstraint) null).size() != 0) {
                    return true;
                }

                Thread.sleep(1000);
            }
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return false;

    }

    @Test
    public void TestJobFailsWithRuntimeException() {
        String message = null;

        try {
            BatchJobManager.executeCommand(new String[]{"run", "OffloadedTransactionsEvents", ""});
        } catch (RuntimeException e) {
            message = e.getMessage();
        }

        assertTrue(message.startsWith("Error when executing OffloadedTransactionsEvents class"));
    }

    @Test
    public void TestJobRunsSuccesfullyOutsideFlux() {
        String jobId = BatchJobManager.executeCommand(new String[]{"run", BatchJobType.LedgerBalance.toString()});
        assertTrue(jobRanSuccessfully(BatchJobType.LedgerBalance, jobId, "UpdateLedgerBalance"));
    }

    @Test
    public void TestJobStepRunsSuccesfullyOutsideFlux() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        // test normal job step execution
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep", BatchJobType.LedgerBalance.toString(), "UpdateLedgerBalance"});
        assertTrue(jobRanSuccessfully(BatchJobType.LedgerBalance, jobId, "UpdateLedgerBalance"));

        // test job step execution where declared step is member of super class
        jobId = BatchJobManager.executeCommand(new String[]{"runstep", BatchJobType.PrimaryDailyBatchJobs.toString(), "MissedPayrollProcessor", "20070601"});
        assertTrue(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "MissedPayrollProcessor"));
    }


    @Test
    public void TestIndividualJobSchedulesAndUnschedulesSuccessfully() {
        if (!ConfigurationManager.getEnvironmentIdentifier().equals("build")) {
            assertTrue (true);
            return;
        }

        BatchJobManager.executeCommand(new String[]{"schedule", BatchJobType.LedgerBalance.toString(), ""});
        BatchJobManager.executeCommand(new String[]{"schedule", BatchJobType.LedgerBalanceMonitor.toString(), ""});

        assertTrue(FluxUtils.isBatchJobScheduled(BatchJobProcessor.getJobNamespace(BatchJobType.LedgerBalance)));
        assertTrue(FluxUtils.isBatchJobScheduled(BatchJobProcessor.getJobNamespace(BatchJobType.LedgerBalanceMonitor)));

        BatchJobManager.executeCommand(new String[]{"unschedule", BatchJobType.LedgerBalance.toString(), ""});
        BatchJobManager.executeCommand(new String[]{"unschedule", BatchJobType.LedgerBalanceMonitor.toString(), ""});

        assertTrue(!FluxUtils.isBatchJobScheduled(BatchJobProcessor.getJobNamespace(BatchJobType.LedgerBalance)));
        assertTrue(!FluxUtils.isBatchJobScheduled(BatchJobProcessor.getJobNamespace(BatchJobType.LedgerBalanceMonitor)));
    }

    @Test
    public void testCreateAchFilesJobStepWithOffloadBatchId() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // create the company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload the ach transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

        PayrollServices.beginUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        String offloadBatchId = offloader.offload(OffloadGroup.Codes.STANDARD, null, ACHFileType.DD);
        Application.commitUnitOfWork();

        // create the ach files, passing the correct offload batch id
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                   BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                   "CreateAchFiles",
                                                                   offloadBatchId});

        assertTrue(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "CreateAchFiles"));

        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCreateAchFilesJobStepWithoutOffloadBatchId() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // create the company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload the ach transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // offload ach data
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                   BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                   "OffloadAchData"});

        assertTrue(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "OffloadAchData"));

        // create the ach files, omitting the offload batch id from the command line
        // (in this case, the CreateAchFiles job step should discover it on its own)
        jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                            BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                            "CreateAchFiles"});

        assertTrue(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "CreateAchFiles"));

        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCreateAchFilesJobStepWrongOffloadBatchId() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // create the company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload the ach transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // offload ach data
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                   BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                   "OffloadAchData"});

        assertTrue(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "OffloadAchData"));

        try {
            // attempt create the ach files with the wrong offload batch id
            jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                "CreateAchFiles",
                                                                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"});
        } catch (RuntimeException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            List<String> stack = Arrays.asList(sw.toString().split("\r?\n"));
            assertTrue(stack.toString().matches(".*Specified offload batch id not found \\(aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\\).*"));
        }

        assertFalse(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "CreateAchFiles"));

        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCreateAchFilesJobStepInvalidOffloadDate() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // create the company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload the ach transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // offload ach data
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                   BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                   "OffloadAchData"});

        assertTrue(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "OffloadAchData"));

        try {
            // attempt create the ach files with an invalid offload date
            // (the CreateAchFiles job step should attempt to retrieve the offload batch id for that date and fail)
            jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                                                                BatchJobType.PrimaryDailyBatchJobs.toString(),
                                                                "CreateAchFiles",
                                                                "20070930"}); // invalid offload date
        } catch (RuntimeException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            List<String> stack = Arrays.asList(sw.toString().split("\r?\n"));
            assertTrue(stack.toString().matches(".*Unable to locate offload batch for offload date: [0-9]{8}.*"));
        }

        assertFalse(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "CreateAchFiles"));

        PayrollServicesTest.afterEachTest();
    }

    private void markJobStepCompleted(String pJobNamespace, String pJobStep) {
        // dummy the start/finish of the given job step for the given namespace

        BatchJobAuditLog logStarted = new BatchJobAuditLog();
        logStarted.setCreatedDate(PSPDate.getPSPTime());
        logStarted.setJobNamespace(pJobNamespace);
        logStarted.setJobAction(pJobStep);
        logStarted.setMessage("Started");
        Application.save(logStarted);

        BatchJobAuditLog logFinished = new BatchJobAuditLog();
        logFinished.setCreatedDate(PSPDate.getPSPTime());
        logFinished.setJobNamespace(pJobNamespace);
        logFinished.setJobAction(pJobStep);
        logFinished.setMessage("Finished");
        Application.save(logFinished);
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testOffloadViaBatchJobManager() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // create the company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        // set the bank verification debits as Archived to get them out of play
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFileSet = BatchUtils.getNachaFilesByStatus(NACHAFileStatus.Finalized);
        for (NACHAFile nachaFile : nachaFileSet) {
            nachaFile.setStatus(NACHAFileStatus.Archived);
            nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            Application.save(nachaFile);
        }
        PayrollServices.commitUnitOfWork();

        // create a new payroll
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928163000");
        Application.commitUnitOfWork();

        // run the PrimaryDailyForecast job
        //BatchJobManager.executeCommand(new String[]{"run", BatchJobType.PrimaryDailyForecast.toString()});

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928171500");
        Application.commitUnitOfWork();

        String jobId = SpcfUniqueId.generateRandomUniqueId().toString();
        String jobNamespace = BatchJobProcessor.getJobNamespace(BatchJobType.PrimaryDailyBatchJobs) + "/" + jobId;

        // dummy out the job steps that present problems in a test environment...
        PayrollServices.beginUnitOfWork();

        // no Flux engine to connect to
        markJobStepCompleted(jobNamespace, "StartPrimaryAchOffloadMonitor");
        markJobStepCompleted(jobNamespace, "GemsAccountsReceivableProcessor");

        // don't want an email
        markJobStepCompleted(jobNamespace, "NotifyAchOffloadStarted");
        markJobStepCompleted(jobNamespace, "NotifyAchOffloadComplete");

        // no SFTP server to connect to
        markJobStepCompleted(jobNamespace, "UploadAchFiles");
        markJobStepCompleted(jobNamespace, "DownloadDicrFiles");

        // don't want to wait for timeout
        markJobStepCompleted(jobNamespace, "DownloadDicrFilesDelayPeriod");

        // will manually handle below (so monitor won't complain)
        markJobStepCompleted(jobNamespace, "ArchiveDailyFiles");

        PayrollServices.commitUnitOfWork();

        // continue the job using the job id from above
        BatchJobManager.executeCommand(new String[]{"rerun", BatchJobType.PrimaryDailyBatchJobs.toString(), jobId});

        // create DICR file recs and archive + confirm the ach files
        PayrollServices.beginUnitOfWork();

        nachaFileSet = BatchUtils.getNachaFilesByStatus(NACHAFileStatus.Finalized);

        for (NACHAFile nachaFile : nachaFileSet) {
            DICRFile dicrFile = new DICRFile();

            dicrFile.setCreatedDate(PSPDate.getPSPTime());
            dicrFile.setCreatorId(nachaFile.getCreatorId());
            dicrFile.setModifiedDate(PSPDate.getPSPTime());
            dicrFile.setModifierId(nachaFile.getModifierId());
            dicrFile.setCreditTxnTotalAmount(nachaFile.getCreditTxnTotalAmount());
            dicrFile.setDebitTxnTotalAmount(nachaFile.getDebitTxnTotalAmount());
            dicrFile.setFileName("xxxxxx");
            dicrFile.setNACHAFile(nachaFile);
            dicrFile.setStatus(DICRFileStatus.Archived);

            Application.save(dicrFile);

            nachaFile.setStatus(NACHAFileStatus.Archived);
            nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            nachaFile.setTransmissionDate(PSPDate.getPSPTime());
            nachaFile.setConfirmationCode("CONFIRMED");
            nachaFile.setConfirmationDate(PSPDate.getPSPTime());

            Application.save(nachaFile);
        }

        PayrollServices.commitUnitOfWork();

        // run the AchOffloadCompleteMonitor to confirm the offload was successful
        BatchJobManager.executeCommand(new String[]{"run", BatchJobType.AchOffloadCompleteMonitor.toString()});
    }

    // PSRV003437: Unable to 'rerun' NightlyBatchJobs process from command line
    @Test
    public void testRerunOfNightlyBatchJobs() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20121102000000");
        Application.commitUnitOfWork();

        String jobId = SpcfUniqueId.generateRandomUniqueId().toString();
        String jobNamespace = BatchJobProcessor.getJobNamespace(BatchJobType.NightlyBatchJobs) + "/" + jobId;

        // dummy out the job steps that present problems in a test environment...
        PayrollServices.beginUnitOfWork();

        // Mark all ACH Returns job steps as having been executed
        markJobStepCompleted(jobNamespace, "DownloadAchReturnsFile");
        markJobStepCompleted(jobNamespace, "PersistReturnsFile");
        markJobStepCompleted(jobNamespace, "ProcessAchReturnsBatch");
        markJobStepCompleted(jobNamespace, "NotifyReturnProcessingResults");

        PayrollServices.commitUnitOfWork();

        // continue the job using the job id from above
        BatchJobManager.executeCommand(new String[]{"rerun", BatchJobType.NightlyBatchJobs.toString(), jobId});

        // all we care about for this test is that the above steps were skipped and the ProcessAchTransactions step completed
        assertTrue(jobRanSuccessfully(BatchJobType.NightlyBatchJobs, jobId, "ProcessAchTransactions"));

        // create DICR file recs and archive + confirm the ach files
        PayrollServices.beginUnitOfWork();
    }

    // PSRV003554: Update AchTaxPaymentOffloadProcessor to parse command line args when executing indiv. job steps
    @Test
    public void testTaxPaymentOffloadProcessorJobStepWithCommandLineParams() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        // test normal job step execution
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep", BatchJobType.AchTaxPaymentOffload.toString(), "MissedPayrollProcessor", "20070601"});
        assertTrue(jobRanSuccessfully(BatchJobType.AchTaxPaymentOffload, jobId, "MissedPayrollProcessor"));
    }
    @Test
    public void testPrimaryDailyBatchJobsProcessor_Secondoffload() {
        boolean otherOffloadCompletedInsertFinancialTransactionState = false;
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        // Create an instance of the "second offload" aka  BatchJobType.ScheduledDailyBatchJobs
        // Test to see if the other offload process (PrimaryDailyBatchJobs) finished the  step InsertFinancialTransactionState
        // Should return false
        PrimaryDailyBatchJobsProcessor proc = new PrimaryDailyBatchJobsProcessor(BatchJobProcessor.RunMode.NotUsingFlux,BatchJobType.ScheduledDailyBatchJobs,"11", null);
        try {
            PayrollServices.beginUnitOfWork();
            otherOffloadCompletedInsertFinancialTransactionState = proc.otherOffloadCompletedInsertFinancialTransactionStateStep();
            assertFalse("bothOffloadsCompletedInsertFinancialTransactionState",otherOffloadCompletedInsertFinancialTransactionState);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // Setup Payroll/Offload data - run the step InsertFinancialTransactionState as PrimaryDailyBatchJobs
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload the ach transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925200000");
        Application.commitUnitOfWork();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

        PayrollServices.beginUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        String offloadBatchId = offloader.offload(OffloadGroup.Codes.STANDARD, null, ACHFileType.DD);
        Application.commitUnitOfWork();
        // Run the  InsertFinancialTransactionState from inside the BatchJobManager as BatchJobType.PrimaryDailyBatchJobs
        // This will create entries in the BatchJobAuditLog table
        String jobId = BatchJobManager.executeCommand(new String[]{"runstep",
                BatchJobType.PrimaryDailyBatchJobs.toString(),
                "InsertFinancialTransactionState",
                offloadBatchId});
        //Make sure the "InsertFinancialTransactionState" is completed
        assertTrue(jobRanSuccessfully(BatchJobType.PrimaryDailyBatchJobs, jobId, "InsertFinancialTransactionState"));
        PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

        //From the "second offload" handle, retest "otherOffloadCompletedInsertFinancialTransactionState"
        // This should return true
        try {
            PayrollServices.beginUnitOfWork();
            otherOffloadCompletedInsertFinancialTransactionState = proc.otherOffloadCompletedInsertFinancialTransactionStateStep();
            assertTrue("bothOffloadsCompletedInsertFinancialTransactionState",otherOffloadCompletedInsertFinancialTransactionState);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollServicesTest.afterEachTest();
    }


}
