package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdministrationAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.forecast.ProcessBatchJobForecast;
import com.intuit.sbd.payroll.psp.batchjobs.ledgeroperations.LedgerOperationsProcessorTests;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

/**
 * User: rnorian
 * Date: Sep 29, 2009
 * Time: 7:18:25 PM
 */
public class AdministrationAdapterTests {

    private static Company1Dataloader c1dl;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testOffloadStatus_OffloadComplete() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        createOffloadLogEntries();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901170500");
        PayrollServices.commitUnitOfWork();

        createOffloadLogEntries();

        AdministrationAdapter adminAdapter = new AdministrationAdapter();
        SAPACHOffloadStatus offloadStatus = null;
        try {
            offloadStatus = adminAdapter.getOffloadStatus(CalendarUtils.convertToDate(PSPDate.getPSPTime()));
        } catch (Throwable t) {
            fail(t.getMessage());
        }

        assertEquals("estimated txn count", 6, offloadStatus.getEstimatedTransactionCount());
        assertEquals("job log entries", AdministrationAdapter.BatchJob.values().length, offloadStatus.getJobLogEntries().size());

        for (SAPACHOffloadJobLogEntry jobLogEntry : offloadStatus.getJobLogEntries()) {
            if (jobLogEntry.getJobName().equals(AdministrationAdapter.BatchJob.CreateAchFiles.name())) {
                assertEquals("estimated job duration", 66000, jobLogEntry.getEstimatedRunTimeInMillis());
            }
        }
    }

    @Ignore
    @Test
    public void testOffloadStatus_OffloadIncomplete() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090810000000");
        PayrollServices.commitUnitOfWork();

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD);
        SpcfCalendar offloadCal = offloadGroup.getCalendarForCutoffTime(PSPDate.getPSPTime());
        PSPDate.setPSPTime(offloadCal);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        createOffloadLogEntries();

        DomainEntitySet<BatchJobAuditLog> logs = PayrollServices.entityFinder.find(BatchJobAuditLog.class);
        for (BatchJobAuditLog log : logs) {
            SpcfCalendar local = log.getCreatedDate().toLocal();
            System.out.println("local = " + local);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        offloadCal = offloadGroup.getCalendarForCutoffTime(PSPDate.getPSPTime());
        PSPDate.setPSPTime(offloadCal);
        PayrollServices.commitUnitOfWork();

        AdministrationAdapter.BatchJob lastJob = AdministrationAdapter.BatchJob.OffloadAchData;
        String lastJobMessage = "Started";
        AdministrationAdapter.BatchJob.BatchJobStep lastStep = AdministrationAdapter.BatchJob.BatchJobStep.UpdateEDR_CCD;
        createOffloadLogEntries(lastJob, lastJobMessage, lastStep);

        AdministrationAdapter adminAdapter = new AdministrationAdapter();
        SAPACHOffloadStatus offloadStatus = null;
        try {
            offloadStatus = adminAdapter.getOffloadStatus(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        } catch (Throwable t) {
            fail(t.getMessage());
        }

        assertEquals("estimated txn count", 6, offloadStatus.getEstimatedTransactionCount());
        assertEquals("job log entries", AdministrationAdapter.BatchJob.values().length, offloadStatus.getJobLogEntries().size());

        for (SAPACHOffloadJobLogEntry jobLogEntry : offloadStatus.getJobLogEntries()) {
            if (jobLogEntry.getJobName().equals(AdministrationAdapter.BatchJob.CreateAchFiles.name())) {
                assertEquals("estimated job duration", 66000, jobLogEntry.getEstimatedRunTimeInMillis());
            }
        }

        assertLogEntries(offloadStatus.getJobLogEntries(), lastJob, lastJobMessage, lastStep);

/*
        DomainEntitySet<Forecast> forecasts = ForecastBE.getForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 9, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = ForecastBE.getForecasts(ForecastStatus.Closed, findDate);
        assertEquals(forecasts.size(), 1);

        Forecast forecast = forecasts.iterator().next();
        assertEquals(forecast.getActualTransactionCount(), 6);

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(forecastDetails.size(), 1);

        ForecastDetail forecastDetail = forecastDetails.iterator().next();
        assertEquals(forecastDetail.getJobAction(), "CreateAchFiles");
        assertEquals(forecastDetail.getActualRunTime(), 60000);

        forecasts = ForecastBE.getForecasts(ForecastStatus.Open, null);
        assertEquals(forecasts.size(), 1);

        forecast = forecasts.iterator().next();
        assertEquals(forecast.getEstimatedTransactionCount(), 6);

        forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(forecastDetails.size(), 1);

        forecastDetail = forecastDetails.iterator().next();
        assertEquals(forecastDetail.getJobAction(), "CreateAchFiles");
        assertEquals(forecastDetail.getEstimatedRunTime(), 198000);
        PayrollServices.commitUnitOfWork();
*/
    }

    private void assertLogEntries(ArrayList<SAPACHOffloadJobLogEntry> logEntries,
                                  AdministrationAdapter.BatchJob lastJob,
                                  String lastJobMessage,
                                  AdministrationAdapter.BatchJob.BatchJobStep lastStep) {
        boolean beforeLastJob = true;

        for (SAPACHOffloadJobLogEntry logEntry : logEntries) {
            if (logEntry.getJobName().equals(lastJob.name())) {
                if (lastJobMessage.equals("Started")) {
                    assertNotNull("job log - startDateTime", logEntry.getStartDateTime());
                    assertNull("job log - finishDateTime", logEntry.getFinishDateTime());
                } else {
                    assertNotNull("job log - startDateTime", logEntry.getStartDateTime());
                    assertNotNull("job log - finishDateTime", logEntry.getFinishDateTime());
                }
                beforeLastJob = false;
            } else {
                if (beforeLastJob) {
                    assertNotNull("job log - startDateTime", logEntry.getStartDateTime());
                    assertNotNull("job log - finishDateTime", logEntry.getFinishDateTime());
                } else {
                    assertNull("job log - startDateTime", logEntry.getStartDateTime());
                    assertNull("job log - finishDateTime", logEntry.getFinishDateTime());
                }
            }

            // special testing for OffloadAchData
            if (lastJob == AdministrationAdapter.BatchJob.OffloadAchData && lastStep != null &&
                    logEntry.getJobName().equals(AdministrationAdapter.BatchJob.OffloadAchData.name())) {

                String correctStatus = "Completed";
                for (SAPACHOffloadJobStepLogEntry stepLogEntry : logEntry.getStepLogs()) {
                    if (stepLogEntry.getStepName().equals(lastStep.getDisplayName())) {
                        correctStatus = "Executing";
                    } else if (correctStatus.equals("Executing")) {
                        correctStatus = "Pending";
                    }
                    assertEquals("step status", correctStatus, stepLogEntry.getStatus());
                }
            }
        }
    }

    private void createOffloadLogEntries() {
        createOffloadLogEntries(null, null, null);
    }

    private void createOffloadLogEntries(AdministrationAdapter.BatchJob lastJob, String jobMessage,
                                         AdministrationAdapter.BatchJob.BatchJobStep lastJobStep) {
        SpcfCalendar createdDate = PSPDate.getPSPTime();

        for (AdministrationAdapter.BatchJob job : AdministrationAdapter.BatchJob.values()) {
            PayrollServices.beginUnitOfWork();
            createdDate.addMinutes(1);
            createBatchJobAuditLog(createdDate, job.name(), "Started");
            PayrollServices.commitUnitOfWork();

            // OffloadAchData Steps
            for (AdministrationAdapter.BatchJob.BatchJobStep jobStep : job.getSteps()) {
                PayrollServices.beginUnitOfWork();
                createdDate.addMinutes(1);
                PayrollServices.commitUnitOfWork();

                PayrollServices.beginUnitOfWork();
                createEventLog(createdDate, jobStep.getDisplayName());
                PayrollServices.commitUnitOfWork();
                if (lastJobStep == jobStep)
                    break;
            }

            if (lastJob == job && jobMessage.equals("Started"))
                return;

            PayrollServices.beginUnitOfWork();
            createdDate.addMinutes(1);
            createBatchJobAuditLog(createdDate, job.name(), "Finished");
            PayrollServices.commitUnitOfWork();

            if (lastJob == job && jobMessage.equals("Finished"))
                return;
        }

    }


    private void createPayroll(int pIndex, SpcfCalendar pCheckDate) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(convertSpcfCalendarToString("yyyyMMdd000000", pCheckDate));
        PayrollRunDTO currentPayrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO(convertSpcfCalendarToString("yyyy-MM-dd", pCheckDate)));
        currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + pIndex);
        Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
            }
        }

        Company1Dataloader.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();
    }

    private void runAchOffload(SpcfCalendar pCheckDate) {
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(convertSpcfCalendarToString("yyyyMMdd170500", pCheckDate));
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    private String convertSpcfCalendarToString(String pFormat, SpcfCalendar pDate) {
        return new SimpleDateFormat(pFormat).format(CalendarUtils.convertToDate(pDate));
    }

    private void createBatchJobAuditLog(SpcfCalendar pDate, String pJobAction, String pMessage) {
        BatchJobAuditLog bjal = new BatchJobAuditLog();

        bjal.setCreatedDate(pDate);
        bjal.setJobNamespace("/PSP/HIGH/PrimaryDailyBatchJobs/2bb2b5b1-c66a-4f61-9633-9ce202f88668");
        bjal.setJobAction(pJobAction);
        bjal.setMessage(pMessage);

        Application.save(bjal);
    }

    private void createEventLog(SpcfCalendar createdDate, String eventMessage) {
        EventLog eventLog = new EventLog();
        eventLog.setCreatedDate(createdDate);
        eventLog.setMessageDttm(createdDate);
        eventLog.setApplicationName("Offload Stored Proc");
        eventLog.setArchitectureName("PSP");
        eventLog.setComponentName("PRC_OFFLOAD");
        eventLog.setMessage(eventMessage);
        Application.save(eventLog);
    }

    @Test
    public void testNewIncomingDatum() {
        // Load Company
        DataLoadServices.newCompany(SourceSystemCode.QBDT, "123");

        // Create a Source System Transmission
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO;
        Long initialToken = 0L;
        Random rnd = new Random();

        PayrollServices.beginUnitOfWork();
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(initialToken + 946);
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 30, 23, 49, 50, rnd.nextInt(999), SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        sourceSystemTransmissionDTO.setRequestDocument(OFX_REQUEST_DOC);
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.QBDT);
        sourceSystemTransmissionDTO.setIPAddress(String.format("%d.%d.%d.%d", 127 + rnd.nextInt(120), rnd.nextInt(254), rnd.nextInt(254), rnd.nextInt(254)));
        ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT,
                "123", transmissionId, sourceSystemTransmissionDTO);
    }

    @Test
    public void testGetAllTransmissions() {
            AdministrationAdapter adminAdapter = new AdministrationAdapter();

            // Load Company
            DataLoadServices.newCompany(SourceSystemCode.QBDT, "123");

            // Create a Source System Transmission
            SourceSystemTransmissionDTO sourceSystemTransmissionDTO;
            Long initialToken = 0L;
            Random rnd = new Random();
            for (int i = 1; i <= 945; i++) {
                PayrollServices.beginUnitOfWork();
                String transmissionId = SpcfUniqueId.createInstance(true).toString();
                sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
                sourceSystemTransmissionDTO.setRequestToken(initialToken + i);
                SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15 + (i / 61), rnd.nextInt(23), rnd.nextInt(59), rnd.nextInt(59), rnd.nextInt(999), SpcfTimeZone.getLocalTimeZone());
                PSPDate.setPSPTime(testTime);
                PayrollServices.commitUnitOfWork();

                sourceSystemTransmissionDTO.setRequestDocument(OFX_REQUEST_DOC);
                sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
                sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.QBDT);
                sourceSystemTransmissionDTO.setIPAddress(String.format("%d.%d.%d.%d", 127 + rnd.nextInt(120), rnd.nextInt(254), rnd.nextInt(254), rnd.nextInt(254)));

                ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT,
                        "123", transmissionId, sourceSystemTransmissionDTO);

                // Check that transmission was successfully created
                assertSuccess("initializeSourceSystemTransmission", processResult);
                SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
                PayrollServices.beginUnitOfWorkWithSecondary();
                SourceSystemTransmission savedSourceSystemTransmission = PayrollServices.entityFinderSecondary.findById(SourceSystemTransmission.class, sourceSystemTransmission.getId());
                PayrollServices.commitUnitOfWorkWithSecondary();

                // Finalize the Source System Transmission
                sourceSystemTransmissionDTO.setResponseToken(initialToken + i + 1);
                sourceSystemTransmissionDTO.setResponseDocument(OFX_RESPONSE_DOC);
                processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBDT,
                        "123", transmissionId, sourceSystemTransmissionDTO);

                // Check that transmission was successfully finalized
                assertSuccess("finalizeSourceSystemTransmission", processResult);
                sourceSystemTransmission = processResult.getResult();
            }
            ArrayList<SAPTransmission> transmissionList = null;
            try {
                /*  Reminder: Date(deprecated) uses 0 based months and year is {year minus 1900}   */
                transmissionList = adminAdapter.getAllTransmissions("QBDT", new Date(107, 10, 17), new Date(107, 10, 19), 0, 1000).getReturnsList();
            } catch (Throwable pThrowable) {
                pThrowable.printStackTrace();
            }
            assertEquals("Transmissions count ", 122, transmissionList.size());
    }

    @Test
    //mostly testing the non-standard queries
    public void testGetLedgerOperationJob() throws Throwable {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        LedgerOperationJob job = LedgerOperationsProcessorTests.createAndQueue(LedgerOperationsProcessorTests.createJobDTO("2012-12-31", "66", new LedgerOperationsProcessorTests.LedgerOperationCreator(null, "1.00"), new LedgerOperationsProcessorTests.LedgerOperationCreator(null, "2.00"), new LedgerOperationsProcessorTests.LedgerOperationCreator(null, "3.00")));

        SAPLedgerOperationJob sapLedgerOperationJob = assertOne(new AdministrationAdapter().getLedgerOperationJobs());
        assertEquals("Queued", sapLedgerOperationJob.getStatus());
        assertEquals(3, sapLedgerOperationJob.getTotalRecords());
        assertEquals(0, sapLedgerOperationJob.getProcessedRecords());

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        job.setStatus(LedgerOperationJobStatus.InProgress);
        job.getLedgerOperationCollection().get(0).setStatus(LedgerOperationStatus.Error);
        job.getLedgerOperationCollection().get(1).setStatus(LedgerOperationStatus.Completed);
        PayrollServices.commitUnitOfWork();

        sapLedgerOperationJob = assertOne(new AdministrationAdapter().getLedgerOperationJobs());
        assertEquals("InProgress", sapLedgerOperationJob.getStatus());
        assertEquals(3, sapLedgerOperationJob.getTotalRecords());
        assertEquals(2, sapLedgerOperationJob.getProcessedRecords());
    }

    @Test
    public void testLedgerJobUpload() throws Throwable {
        byte[] fileBinary = FileUtils.readFileToByteArray(new File(Application.findFileOnClassPath("resources/futatest.csv")));
        new AdministrationAdapter().uploadLedgerOperationsFile(fileBinary, "");

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals(new String(fileBinary), job.getOriginalFileString());
        assertEquals(LedgerOperationJobStatus.Created, job.getStatus());
        assertNull(job.getStartTime());
        assertNull(job.getFinishTime());
        assertEquals(LedgerOperationJobType.BulkDebit, job.getJobType());
        assertNull(job.getProcessedFile());

        DomainEntitySet<LedgerOperation> operations = job.getLedgerOperationCollection().sort(LedgerOperation.OriginalIndex());
        assertEquals(4, operations.size());
        assertOperationEquals(operations.get(0), 0, "100000001", new SpcfMoney("114.60"), "STELLA'S CRUSHING STUDENT DEBT, INC.");
        assertOperationEquals(operations.get(1), 1, "100000002", new SpcfMoney("198.29"), "STELLA'S IRRATIONAL FEAR OF SPIDERS");
        assertOperationEquals(operations.get(2), 2, "100000003", new SpcfMoney("667.73"), new SpcfMoney("235.12"), "STELLA'S GROWING SENSE OF UNEASE", "200");
        assertOperationEquals(operations.get(3), 3, "100000004", new SpcfMoney("21.00"), "STELLA'S DEEP APPRECIATION OF RUSSIAN LITERATURE");
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testNVAndOthersBackDateHoldOverridenFinalisedPaymentFromSAP() throws Throwable {
        String NV_SUI_CREDIT = "NV SUI Credit";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-UC2-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        Company macompany = DataLoadPalette.setupTaxCompany();
        Company pacompany = DataLoadPalette.setupTaxCompany();
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateAgencyTaxpayerId(company, "NV-NUCS4072-PAYMENT", "011345678");
        DataLoadServices.setPSPDate(2013, 1, 10);
        DataLoadServices.addCompanyLaws(company, "40", "121", "137", "102", "150");
        DataLoadServices.updateAgencyTaxpayerId(company, "PA-UC2-PAYMENT", "1224567");
        DataLoadServices.updateACHAgentEnabledFlags(company, "PA-UC2-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company, "MA-1700HI-PAYMENT", "12245671");
        DataLoadServices.updateACHAgentEnabledFlags(company, "MA-1700HI-PAYMENT", true);

        DataLoadServices.addCompanyLaws(pacompany, "40", "121", "137", "102", "150");
        DataLoadServices.addCompanyLaws(macompany, "40", "121", "137", "102", "150");
        DataLoadServices.updateAgencyTaxpayerId(pacompany, "PA-UC2-PAYMENT", "1224567");
        DataLoadServices.updateACHAgentEnabledFlags(pacompany, "PA-UC2-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(macompany, "MA-1700HI-PAYMENT", "12245671");
        DataLoadServices.updateACHAgentEnabledFlags(macompany, "MA-1700HI-PAYMENT", true);

        PayrollRunDTO payrollRunDTO = getPayrollrunDTO(company);
        PayrollRunDTO mapayrollRunDTO = getPayrollrunDTO(macompany);
        PayrollRunDTO papayrollRunDTO = getPayrollrunDTO(pacompany);
        DataLoadServices.setPSPDate(2016, 5, 18);
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);
        QBDTTestHelper.submitPayroll(macompany, mapayrollRunDTO);
        QBDTTestHelper.submitPayroll(pacompany, papayrollRunDTO);
        DataLoadServices.runMMTJobs(1);
        DataLoadServices.setPSPDate(2016, 5, 24);
        DataLoadServices.runMMTJobs(1);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = DataLoadServices.getReadyToSendTaxPayments(company, "NV-NUCS4072-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> mmts2 = DataLoadServices.getReadyToSendTaxPayments(pacompany, "PA-UC2-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> mmts3 = DataLoadServices.getReadyToSendTaxPayments(macompany, "MA-1700HI-PAYMENT");
        mmts.addAll(mmts2);
        mmts.addAll(mmts3);
        Application.rollbackUnitOfWork();
        for (MoneyMovementTransaction mmt : mmts) {
            DataLoadServices.finalizePayment(mmt);
        }

        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        DomainEntitySet<MoneyMovementTransaction> paPayments = MoneyMovementTransaction.findTaxPayments().setCompany(pacompany).setPaymentTemplateCd("PA-UC2-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        DomainEntitySet<MoneyMovementTransaction> maPayments = MoneyMovementTransaction.findTaxPayments().setCompany(macompany).setPaymentTemplateCd("MA-1700HI-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for NV", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());

        assertEquals("Count of finalised MMTS for PA", 1, paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for PA", SpcfMoney.createInstance("12.00"), paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for PA", 0, paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());

        assertEquals("Count of finalised MMTS for MA", 1, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for MA", SpcfMoney.createInstance("480.00"), maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for MA", 0, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());

        Application.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2016, 5, 27);

        byte[] fileBinary = FileUtils.readFileToByteArray(new File(Application.findFileOnClassPath("resources/NV_BulkDebit.csv")));
        new AdministrationAdapter().uploadLedgerOperationsFile(fileBinary, "NV test");
        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class, LedgerOperationJob.Status().equalTo(LedgerOperationJobStatus.Created)));
        assertSuccess(PayrollServices.batchJobManager.queueLedgerOperationJob(job.getId()));
        job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals(new String(fileBinary), job.getOriginalFileString());
        assertEquals(LedgerOperationJobStatus.Queued, job.getStatus());
        assertNull(job.getStartTime());
        assertNull(job.getFinishTime());
        assertEquals(LedgerOperationJobType.BulkDebit, job.getJobType());
        assertNull(job.getProcessedFile());
        DomainEntitySet<LedgerOperation> operations = job.getLedgerOperationCollection().sort(LedgerOperation.OriginalIndex());
        assertEquals(3, operations.size());
        assertOperationEquals(operations.get(0), 0, SourceSystemCode.QBDT, "TEST_0001", new SpcfMoney("77.00"), null, "NV Bond Credit", SpcfCalendar.createInstance(2016, 4, 1, SpcfTimeZone.getLocalTimeZone()), "TEST_COMPANY_1", LedgerOperationStatus.Created, null, "116");
        assertOperationEquals(operations.get(1), 1, SourceSystemCode.QBDT, "TEST_0003", new SpcfMoney("88.00"), null, "PA SUI Credit", SpcfCalendar.createInstance(2016, 4, 1, SpcfTimeZone.getLocalTimeZone()), "TEST_COMPANY_3", LedgerOperationStatus.Created, null, "121");
        assertOperationEquals(operations.get(2), 2, SourceSystemCode.QBDT, "TEST_0002", new SpcfMoney("66.00"), null, "MA SUI Credit", SpcfCalendar.createInstance(2016, 4, 1, SpcfTimeZone.getLocalTimeZone()), "TEST_COMPANY_2", LedgerOperationStatus.Created, null, "102");
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        paPayments = MoneyMovementTransaction.findTaxPayments().setCompany(pacompany).setPaymentTemplateCd("PA-UC2-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        maPayments = MoneyMovementTransaction.findTaxPayments().setCompany(macompany).setPaymentTemplateCd("MA-1700HI-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        assertEquals("amount of pending MMTS for NV", SpcfMoney.createInstance("77.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of onhold MMTS for PA", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());


        assertEquals("Count of finalised MMTS for PA", 1, paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for PA", SpcfMoney.createInstance("12.00"), paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of onhold MMTS for PA", 1, paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());
        assertEquals("amount of onhold MMTS for PA", SpcfMoney.createInstance("88.00"), paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for NV", 0, paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        assertEquals("amount of onhold reason for MA", PaymentOnHoldReason.BackDate, paPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getActiveOnHoldReasons().getFirst().getOnHoldReasonCd());

        assertEquals("Count of finalised MMTS for MA", 1, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for MA", SpcfMoney.createInstance("480.00"), maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for MA", 1, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());
        assertEquals("amount of onhold MMTS for MA", SpcfMoney.createInstance("66.00"), maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("amount of onhold reason for MA", PaymentOnHoldReason.BackDate, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getActiveOnHoldReasons().getFirst().getOnHoldReasonCd());
        assertEquals("Count of pending MMTS for NV", 0, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        Application.rollbackUnitOfWork();
    }

    private static PayrollRunDTO getPayrollrunDTO(Company company) {
        PayrollServices.beginUnitOfWork();

        company = Application.refresh(company);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(4, true);
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            ProcessResult processResult = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employeeDTO);
            assertTrue(processResult.isSuccess());
        }

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("123456");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Test");
        bankAccountDTO.setRoutingNumber("123123123");

        for (Employee employee : company.getEmployees()) {
            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee, bankAccountDTO);
            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employee.getSourceEmployeeId(),
                    employeeBankAccountDTO);
            assertTrue(processResult.isSuccess());
        }

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndNVPAMAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2016, 5, 20), employees);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        int count = 0;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            count++;
            if (count == 4) {
                for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                    ddTransactionDTO.setDDTransactionAmount(new BigDecimal(0));
                }
            }
        }
        PayrollServices.commitUnitOfWork();
        return payrollRunDTO;
    }

    @Test
    public void testRateJobUpload() throws Throwable {
        byte[] fileBinary = FileUtils.readFileToByteArray(new File(Application.findFileOnClassPath("resources/ratetest.csv")));
        new AdministrationAdapter().uploadLedgerOperationsFile(fileBinary, "");

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals(new String(fileBinary), job.getOriginalFileString());
        assertEquals(LedgerOperationJobStatus.Created, job.getStatus());
        assertNull(job.getStartTime());
        assertNull(job.getFinishTime());
        assertEquals(LedgerOperationJobType.RateUpdate, job.getJobType());
        assertNull(job.getProcessedFile());

        DomainEntitySet<LedgerOperation> operations = job.getLedgerOperationCollection().sort(LedgerOperation.OriginalIndex());
        assertEquals(2, operations.size());
        assertRateOperationEquals(operations.get(0), 0, "100000001", 114.60, "STELLA'S CRUSHING STUDENT DEBT, INC.", true);
        assertRateOperationEquals(operations.get(1), 1, "100000002", 198.29, "STELLA'S IRRATIONAL FEAR OF SPIDERS", false);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAdditionalAmountJobUpload() throws Throwable {
        byte[] fileBinary = FileUtils.readFileToByteArray(new File(Application.findFileOnClassPath("resources/additionalamounttest.csv")));
        new AdministrationAdapter().uploadLedgerOperationsFile(fileBinary, null);

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals(new String(fileBinary), job.getOriginalFileString());
        assertEquals(LedgerOperationJobStatus.Created, job.getStatus());
        assertNull(job.getStartTime());
        assertNull(job.getFinishTime());
        assertEquals(LedgerOperationJobType.AdditionalFilingAmountUpdate, job.getJobType());
        assertNull(job.getProcessedFile());

        DomainEntitySet<LedgerOperation> operations = job.getLedgerOperationCollection().sort(LedgerOperation.OriginalIndex());
        assertEquals(2, operations.size());
        assertAdditionalAmountOperationEquals(operations.get(0), 0, "100000003", 667.73, "STELLA'S GROWING SENSE OF UNEASE", "MA Unemployment Health Insurance Rate");
        assertAdditionalAmountOperationEquals(operations.get(1), 1, "100000004", 21.00, "STELLA'S DEEP APPRECIATION OF RUSSIAN LITERATURE", "MA SUI Credit");
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDepositFrequencyJobUpload() throws Throwable {
        byte[] fileBinary = FileUtils.readFileToByteArray(new File(Application.findFileOnClassPath("resources/depositfrequencytest.csv")));
        new AdministrationAdapter().uploadLedgerOperationsFile(fileBinary, "");

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals(new String(fileBinary), job.getOriginalFileString());
        assertEquals(LedgerOperationJobStatus.Created, job.getStatus());
        assertNull(job.getStartTime());
        assertNull(job.getFinishTime());
        assertEquals(LedgerOperationJobType.DepositFrequencyUpdate, job.getJobType());
        assertNull(job.getProcessedFile());

        DomainEntitySet<LedgerOperation> operations = job.getLedgerOperationCollection().sort(LedgerOperation.OriginalIndex());
        assertEquals(1, operations.size());
        assertDepositFrequencyOperationEquals(operations.get(0), 0, "100000001", "STELLA'S DISTASTE FOR BELGIUN BEERS, INC.", DepositFrequencyCode.MONTHLY);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLedgerJobUploadFailsWithBadFile() throws Throwable {
        byte[] file = FileUtils.readFileToByteArray(new File(Application.findFileOnClassPath("resources/futatestbad.csv")));
        try {
            new AdministrationAdapter().uploadLedgerOperationsFile(file, "");
            fail("Expected SAPException");
        } catch (SAPException exception) {
        }
    }

    @Test
    public void testCreateTORLedgerOperationJob() throws Throwable {
        DataLoadServices.reinitialize();
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2010, 6, 1);
        DataLoadPalette.setupTaxCompany();
        DataLoadPalette.setupTaxCompany();
        DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2011, 7, 15);

        new AdministrationAdapter().createTORLedgerOperationJob("IRS-941-PAYMENT", new Date("2011/06/30"));

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));

        byte[] fileBinary = FileUtils.readFileToByteArray(new File(Application.findFileOnClassPath("resources/tortest.csv")));
        assertEquals(new String(fileBinary), job.getOriginalFileString());
        assertEquals(LedgerOperationJobStatus.Created, job.getStatus());
        assertNull(job.getStartTime());
        assertNull(job.getFinishTime());
        assertEquals(LedgerOperationJobType.TOR, job.getJobType());
        assertNull(job.getProcessedFile());

        DomainEntitySet<LedgerOperation> operations = job.getLedgerOperationCollection().sort(LedgerOperation.OriginalIndex());
        assertEquals(3, operations.size());
        assertTorOperationEquals(operations.get(0), 0, "TEST_0001", "TEST_COMPANY_1");
        assertTorOperationEquals(operations.get(1), 1, "TEST_0002", "TEST_COMPANY_2");
        assertTorOperationEquals(operations.get(2), 2, "TEST_0003", "TEST_COMPANY_3");
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNoTORForNonAssistedCustomers() throws Throwable {
        //PSP-3803
        DataLoadServices.reinitialize();
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadPalette.setupTaxCompany();

        Company nonTaxCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.CloudV2, ServiceCode.ViewMyPaycheck);
        DataLoadServices.addCompanyLaws(nonTaxCompany, Law.SUTA);

        new AdministrationAdapter().createTORLedgerOperationJob("IRS-940-PAYMENT", new Date("2011/06/30"));

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals("Does not contain the non-tax company", "QBDT,TEST_0001,,TOR IRS-940-PAYMENT 2011 Q2,TOR,,TEST_COMPANY_1,2011-06-30" + System.getProperty("line.separator"), job.getOriginalFileString());
    }

    @Test
    public void testDDLimitsHappyPath() throws Throwable {
        StringBuilder fileBuilder = new StringBuilder("PSID,Recommended ER Limit,Recommended EE Limit").append(System.getProperty("line.separator"));

        List<Company> companyList = new ArrayList<Company>();
        for (int i = 1; i <= 10; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.DirectDeposit, ServiceCode.Cloud);
            companyList.add(company);
            fileBuilder.append(company.getSourceCompanyId())
                    .append(",\"")
                    .append(NumberFormat.getCurrencyInstance(Locale.US).format(i * 1000))
                    .append(" \",\"")
                    .append(NumberFormat.getCurrencyInstance(Locale.US).format(i * 100))
                    .append(" \"")
                    .append(System.getProperty("line.separator"));
        }
        new AdministrationAdapter().processBulkDDLimitUpdates(fileBuilder.toString());

        PayrollServices.beginUnitOfWork();
        int i = 1;
        for (Company company : companyList) {
            Application.refresh(company);
            DDCompanyServiceInfo ddService = (DDCompanyServiceInfo) company.getService(ServiceCode.DirectDeposit);
            assertEquals(new SpcfMoney(SpcfDecimal.createInstance(i * 1000)), ddService.getOverrideCompanyLimitAmount());
            assertEquals(new SpcfMoney(SpcfDecimal.createInstance(i * 100)), ddService.getOverrideEmployeeLimitAmount());
            ++i;
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDDLimitsFormatError() throws Throwable {
        String file = "PSID,Recommended ER Limit,Recommended EE Limit" + System.getProperty("line.separator") + "nope";
        try {
            new AdministrationAdapter().processBulkDDLimitUpdates(file);
            fail("expected exception");
        } catch (SAPException e) {
        }
    }

    private void assertOperationEquals(LedgerOperation operation, long index, String sourceCompanyId, SpcfMoney amount, SpcfMoney taxableWages, String legalName, String law) {
        assertOperationEquals(operation,
                index,
                SourceSystemCode.QBDT,
                sourceCompanyId,
                amount,
                taxableWages,
                "FUTA CREDIT REDUCTION",
                SpcfCalendar.createInstance(2012, 12, 31, SpcfTimeZone.getLocalTimeZone()),
                legalName,
                LedgerOperationStatus.Created,
                null,
                law);
    }


    private void assertOperationEquals(LedgerOperation operation, long index, String sourceCompanyId, SpcfMoney amount, String legalName) {
        assertOperationEquals(operation, index, sourceCompanyId, amount, null, legalName, "66");
    }

    private void assertRateOperationEquals(LedgerOperation operation, long index, String sourceCompanyId, double rate, String legalName, boolean pushToQuickBooks) {
        assertOperationEquals(operation,
                index,
                SourceSystemCode.QBDT,
                sourceCompanyId,
                null,
                null,
                "Rate memo",
                SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()),
                legalName,
                LedgerOperationStatus.Created,
                null,
                "102");

        RateLedgerOperation rateOperation = (RateLedgerOperation) operation;
        assertEquals(null, rateOperation.getAdditionalFilingAmountName());
        assertEquals(rate, rateOperation.getRate(), 0);
        assertEquals(pushToQuickBooks, rateOperation.getPushToQuickBooks());
    }

    private void assertAdditionalAmountOperationEquals(LedgerOperation operation, long index, String sourceCompanyId, double amount, String legalName, String additionalAmountName) {
        assertOperationEquals(operation,
                index,
                SourceSystemCode.QBDT,
                sourceCompanyId,
                null,
                null,
                "Additional amount memo",
                SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()),
                legalName,
                LedgerOperationStatus.Created,
                null,
                "102");

        RateLedgerOperation rateOperation = (RateLedgerOperation) operation;
        assertEquals(additionalAmountName, rateOperation.getAdditionalFilingAmountName());
        assertEquals(amount, rateOperation.getRate(), 0);
        assertEquals(false, rateOperation.getPushToQuickBooks());
    }

    private void assertDepositFrequencyOperationEquals(LedgerOperation operation, long index, String sourceCompanyId, String legalName, DepositFrequencyCode depositFrequencyCode) {
        assertOperationEquals(operation,
                index,
                SourceSystemCode.QBDT,
                sourceCompanyId,
                null,
                null,
                "Dep Freq Memo",
                SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()),
                legalName,
                LedgerOperationStatus.Created,
                null,
                "102");

        DepositFrequencyLedgerOperation rateOperation = (DepositFrequencyLedgerOperation) operation;
        assertEquals(depositFrequencyCode, rateOperation.getDepositFrequency());
    }

    private void assertTorOperationEquals(LedgerOperation operation, long index, String sourceCompanyId, String legalName) {
        assertOperationEquals(operation,
                index,
                SourceSystemCode.QBDT,
                sourceCompanyId,
                null,
                null,
                "TOR IRS-941-PAYMENT 2011 Q2",
                SpcfCalendar.createInstance(2011, 6, 30, SpcfTimeZone.getLocalTimeZone()),
                legalName,
                LedgerOperationStatus.Created,
                null,
                "1");
    }

    private void assertOperationEquals(LedgerOperation operation,
                                       long index,
                                       SourceSystemCode sourceSystemCode,
                                       String sourceCompanyId,
                                       SpcfMoney amount,
                                       SpcfMoney taxableWages,
                                       String memo,
                                       SpcfCalendar checkDate,
                                       String legalName,
                                       LedgerOperationStatus status,
                                       String messages,
                                       String lawId) {
        assertEquals(index, operation.getOriginalIndex());
        assertEquals(sourceSystemCode, operation.getSourceSystemCode());
        assertEquals(sourceCompanyId, operation.getSourceCompanyId());
        assertEquals(amount, operation.getAmount());
        assertEquals(taxableWages, operation.getWageAmount());
        assertEquals(memo, operation.getMemo());
        assertEquals(checkDate, operation.getCheckDate().toLocal());
        assertEquals(legalName, operation.getOriginalLegalName());
        assertEquals(status, operation.getStatus());
        assertEquals(messages, operation.getMessages());
        assertEquals(lawId, operation.getLaw().getLawId());
    }

    /*
    public static void main(String[] args) {
        SAPAdapterTests tester = new SAPAdapterTests();
        tester.runBeforeEachTest();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.UnitTest);
        tester.testOffloadStatus_OffloadIncomplete();
    }
    */

    private static String OFX_REQUEST_DOC = "<OFX>\n" +
            "<SIGNONMSGSRQV1>\n" +
            "<SONRQ>\n" +
            "<DTCLIENT>20080718001754\n" +
            "<USERID>8574536\n" +
            "<USERPASS>test1234\n" +
            "<LANGUAGE>ENG\n" +
            "<APPVER>50.00.R.3/20804#pro\n" +
            "<APPID>QBWPRO\n" +
            "<I.QBFILENAME>C:\\Documents and Settings\\All Users\\Documents\\Intuit\\QuickBooks\\Company Files\\Joes Cool Co.QBW\n" +
            "<I.QBFILEID>c8e251053a984b3b9e107e8daa9bb640\n" +
            "<I.IPADDRESS>FileInfo:QB_data_engine_18:172.17.214.180#10180\n" +
            "<I.QBUSERNAME>Admin\n" +
            "</SONRQ>\n" +
            "</SIGNONMSGSRQV1>\n" +
            "<I.PAYROLLMSGSRQV1>\n" +
            "<I.PAYROLLUPDATERQ>\n" +
            "<TOKEN>1\n" +
            "<REJECTIFMISSING>Y\n" +
            "<I.PAYROLLTRNRQ>\n" +
            "<TRNUID>87536D20-79F5-1000-BB15-CB9C31AB0026\n" +
            "<I.PAYROLLRQ>\n" +
            "<I.PAYROLLRUN>\n" +
            "<I.DTPAYCHKS>20070810\n" +
            "<I.PAYCHK>\n" +
            "<I.PAYCHKID>1\n" +
            "<I.EMPID>0\n" +
            "<I.DTTX>20070810\n" +
            "<I.PAYCHKTYPE>PAYCHK\n" +
            "<I.EMPNAME>Donovan McNabb\n" +
            "<I.CLASS>^@~*\n" +
            "<I.ACCTNAME>BofA\n" +
            "<I.AMT>$0.00\n" +
            "<I.PAYCHKINFO>\n" +
            "<I.SICKACCRUED>^@~*\n" +
            "<I.VACACCRUED>^@~*\n" +
            "<I.PRORATE>N\n" +
            "<I.CHKNUM>TOPRINT\n" +
            "</I.PAYCHKINFO>\n" +
            "<I.VOID>N\n" +
            "<I.ONSERVICE>Y\n" +
            "<I.DTPAYPDBEGIN>20071117\n" +
            "<I.DTPAYPDEND>20071130\n" +
            "<I.MEMO>Direct Deposit\n" +
            "<I.CLEARED>2\n" +
            "<I.DDLINE>\n" +
            "<I.DDACCT>\n" +
            "<I.ACCTNAME>Bank of Money\n" +
            "<I.AMT>^@~*\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "</I.DDACCT>\n" +
            "<I.PITEMID>0\n" +
            "<I.AMT>$-927.69\n" +
            "</I.DDLINE>\n" +
            "</I.PAYCHK>\n" +
            "<I.PAYCHK>\n" +
            "<I.PAYCHKID>2\n" +
            "<I.EMPID>0\n" +
            "<I.DTTX>20070810\n" +
            "<I.PAYCHKTYPE>PAYCHK\n" +
            "<I.EMPNAME>Abe Lincoln\n" +
            "<I.CLASS>^@~*\n" +
            "<I.ACCTNAME>Abe's Acct\n" +
            "<I.AMT>$0.00\n" +
            "<I.PAYCHKINFO>\n" +
            "<I.SICKACCRUED>^@~*\n" +
            "<I.VACACCRUED>^@~*\n" +
            "<I.PRORATE>N\n" +
            "<I.CHKNUM>TOPRINT\n" +
            "</I.PAYCHKINFO>\n" +
            "<I.VOID>N\n" +
            "<I.ONSERVICE>Y\n" +
            "<I.DTPAYPDBEGIN>20071117\n" +
            "<I.DTPAYPDEND>20071130\n" +
            "<I.MEMO>Direct Deposit\n" +
            "<I.CLEARED>0\n" +
            "<I.DDLINE>\n" +
            "<I.DDACCT>\n" +
            "<I.ACCTNAME>Abe's Bank\n" +
            "<I.AMT>$40.00\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>11122221111\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "</I.DDACCT>\n" +
            "<I.PITEMID>0\n" +
            "<I.AMT>$-40.00\n" +
            "</I.DDLINE>\n" +
            "<I.DDLINE>\n" +
            "<I.DDACCT>\n" +
            "<I.ACCTNAME>Abe's Bank\n" +
            "<I.AMT>^@~*\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>333322222233\n" +
            "<ACCTTYPE>CHECKING\n" +
            "</BANKACCTTO>\n" +
            "</I.DDACCT>\n" +
            "<I.PITEMID>0\n" +
            "<I.AMT>$-153.11\n" +
            "</I.DDLINE>\n" +
            "</I.PAYCHK>\n" +
            "<I.DDADVICE>\n" +
            "<I.DDAMT>$-0.00\n" +
            "<I.DD>\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "<I.EMPID>0\n" +
            "<I.AMT>$-927.69\n" +
            "<I.EMPNAME>Donovan McNabb\n" +
            "<I.SSN>567-12-3456\n" +
            "</I.DD>\n" +
            "<I.DD>\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "<I.EMPID>0\n" +
            "<I.AMT>$-40.00\n" +
            "<I.EMPNAME>Abe Lincoln\n" +
            "<I.SSN>567-12-3456\n" +
            "</I.DD>\n" +
            "<I.DD>\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "<I.EMPID>0\n" +
            "<I.AMT>$-153.11\n" +
            "<I.EMPNAME>Abe Lincoln\n" +
            "<I.SSN>567-12-3456\n" +
            "</I.DD>\n" +
            "</I.DDADVICE>\n" +
            "</I.PAYROLLRUN>\n" +
            "<I.PAYROLLRUN>\n" +
            "<I.DTPAYCHKS>20070816\n" +
            "<I.PAYCHK>\n" +
            "<I.PAYCHKID>3\n" +
            "<I.EMPID>0\n" +
            "<I.DTTX>20070816\n" +
            "<I.PAYCHKTYPE>PAYCHK\n" +
            "<I.EMPNAME>Donovan McNabb\n" +
            "<I.CLASS>^@~*\n" +
            "<I.ACCTNAME>BofA\n" +
            "<I.AMT>$0.00\n" +
            "<I.PAYCHKINFO>\n" +
            "<I.SICKACCRUED>^@~*\n" +
            "<I.VACACCRUED>^@~*\n" +
            "<I.PRORATE>N\n" +
            "<I.CHKNUM>TOPRINT\n" +
            "</I.PAYCHKINFO>\n" +
            "<I.VOID>N\n" +
            "<I.ONSERVICE>Y\n" +
            "<I.DTPAYPDBEGIN>20071117\n" +
            "<I.DTPAYPDEND>20071130\n" +
            "<I.MEMO>Direct Deposit\n" +
            "<I.CLEARED>2\n" +
            "<I.DDLINE>\n" +
            "<I.DDACCT>\n" +
            "<I.ACCTNAME>Bank of Money\n" +
            "<I.AMT>^@~*\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "</I.DDACCT>\n" +
            "<I.PITEMID>0\n" +
            "<I.AMT>$-8091.11\n" +
            "</I.DDLINE>\n" +
            "</I.PAYCHK>\n" +
            "<I.PAYCHK>\n" +
            "<I.PAYCHKID>4\n" +
            "<I.EMPID>0\n" +
            "<I.DTTX>20070816\n" +
            "<I.PAYCHKTYPE>PAYCHK\n" +
            "<I.EMPNAME>Abe Lincoln\n" +
            "<I.CLASS>^@~*\n" +
            "<I.ACCTNAME>Abe's Acct\n" +
            "<I.AMT>$0.00\n" +
            "<I.PAYCHKINFO>\n" +
            "<I.SICKACCRUED>^@~*\n" +
            "<I.VACACCRUED>^@~*\n" +
            "<I.PRORATE>N\n" +
            "<I.CHKNUM>TOPRINT\n" +
            "</I.PAYCHKINFO>\n" +
            "<I.VOID>N\n" +
            "<I.ONSERVICE>Y\n" +
            "<I.DTPAYPDBEGIN>20071117\n" +
            "<I.DTPAYPDEND>20071130\n" +
            "<I.MEMO>Direct Deposit\n" +
            "<I.CLEARED>0\n" +
            "<I.DDLINE>\n" +
            "<I.DDACCT>\n" +
            "<I.ACCTNAME>Abe's Bank\n" +
            "<I.AMT>$100.00\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>11122221111\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "</I.DDACCT>\n" +
            "<I.PITEMID>0\n" +
            "<I.AMT>$-100.00\n" +
            "</I.DDLINE>\n" +
            "<I.DDLINE>\n" +
            "<I.DDACCT>\n" +
            "<I.ACCTNAME>Abe's Bank\n" +
            "<I.AMT>^@~*\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>333322222233\n" +
            "<ACCTTYPE>CHECKING\n" +
            "</BANKACCTTO>\n" +
            "</I.DDACCT>\n" +
            "<I.PITEMID>0\n" +
            "<I.AMT>$-2012.44\n" +
            "</I.DDLINE>\n" +
            "</I.PAYCHK>\n" +
            "<I.DDADVICE>\n" +
            "<I.DDAMT>$-0.00\n" +
            "<I.DD>\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "<I.EMPID>0\n" +
            "<I.AMT>$-8091.11\n" +
            "<I.EMPNAME>Donovan McNabb\n" +
            "<I.SSN>567-12-3456\n" +
            "</I.DD>\n" +
            "<I.DD>\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "<I.EMPID>0\n" +
            "<I.AMT>$-100.00\n" +
            "<I.EMPNAME>Abe Lincoln\n" +
            "<I.SSN>567-12-3456\n" +
            "</I.DD>\n" +
            "<I.DD>\n" +
            "<BANKACCTTO>\n" +
            "<BANKID>113003842\n" +
            "<ACCTID>0011992288\n" +
            "<ACCTTYPE>SAVINGS\n" +
            "</BANKACCTTO>\n" +
            "<I.EMPID>0\n" +
            "<I.AMT>$-2012.44\n" +
            "<I.EMPNAME>Abe Lincoln\n" +
            "<I.SSN>567-12-3456\n" +
            "</I.DD>\n" +
            "</I.DDADVICE>\n" +
            "</I.PAYROLLRUN>\n" +
            "</I.PAYROLLRQ>\n" +
            "</I.PAYROLLTRNRQ>\n" +
            "</I.PAYROLLUPDATERQ>\n" +
            "</I.PAYROLLMSGSRQV1>\n" +
            "</OFX>";

    private static String OFX_RESPONSE_DOC = "<OFX>\n" +
            "<SIGNONMSGSRSV1>\n" +
            "<SONRS>\n" +
            "<STATUS>\n" +
            "<CODE>0\n" +
            "<SEVERITY>INFO\n" +
            "</STATUS>\n" +
            "<DTSERVER>20080718001917\n" +
            "<LANGUAGE>ENG\n" +
            "</SONRS>\n" +
            "</SIGNONMSGSRSV1>\n" +
            "<I.PAYROLLMSGSRSV1>\n" +
            "<I.PAYROLLUPDATERS>\n" +
            "<TOKEN>2\n" +
            "<I.PAYROLLTXNEXTID>3\n" +
            "<I.PAYCHKNEXTID>5\n" +
            "<I.EMPNEXTID>1\n" +
            "<I.PITEMNEXTID>1\n" +
            "<I.PAYROLLTRNRS>\n" +
            "<TRNUID>87536D20-79F5-1000-BB15-CB9C31AB0026\n" +
            "<STATUS>\n" +
            "<CODE>0\n" +
            "<SEVERITY>INFO\n" +
            "</STATUS>\n" +
            "<I.PAYROLLRS>\n" +
            "<I.PAYROLLTX>\n" +
            "<I.PAYROLLTXID>1\n" +
            "<I.NAME>QuickBooks Payroll Service\n" +
            "<I.ACCTNAME>BofA\n" +
            "<I.AMT>$-1126.29\n" +
            "<I.MEMO>Created by Payroll Services on 08/02/2007\n" +
            "<I.CLEARED>0\n" +
            "<I.DTTX>20070810\n" +
            "<I.REFNUM>^@~*\n" +
            "<I.PAYROLLTXTYPE>LIABCHK\n" +
            "<I.DTPAYPDEND>20070810\n" +
            "<I.VOID>N\n" +
            "<I.ONSERVICE>Y\n" +
            "<I.TXLINE>\n" +
            "<I.ACCTNAME>Payroll Expenses\n" +
            "<I.AMT>$2.10\n" +
            "<I.MEMO>Fee for 2 direct deposit(s) at $1.05 each\n" +
            "</I.TXLINE>\n" +
            "<I.TXLINE>\n" +
            "<I.ACCTNAME>Payroll Expenses\n" +
            "<I.AMT>$3.00\n" +
            "<I.MEMO>Direct Deposit Transmission Fee\n" +
            "</I.TXLINE>\n" +
            "<I.TXLINE>\n" +
            "<I.ACCTNAME>Payroll Expenses\n" +
            "<I.AMT>0.51\n" +
            "<I.MEMO>Sales Tax for null\n" +
            "</I.TXLINE>\n" +
            "<I.TXLINE>\n" +
            "<I.AMT>$1120.80\n" +
            "<I.ISDD>Y\n" +
            "</I.TXLINE>\n" +
            "</I.PAYROLLTX>\n" +
            "</I.PAYROLLRS>\n" +
            "<I.PAYROLLRS>\n" +
            "<I.PAYROLLTX>\n" +
            "<I.PAYROLLTXID>2\n" +
            "<I.NAME>QuickBooks Payroll Service\n" +
            "<I.ACCTNAME>BofA\n" +
            "<I.AMT>$-10209.04\n" +
            "<I.MEMO>Created by Payroll Services on 08/02/2007\n" +
            "<I.CLEARED>0\n" +
            "<I.DTTX>20070816\n" +
            "<I.REFNUM>^@~*\n" +
            "<I.PAYROLLTXTYPE>LIABCHK\n" +
            "<I.DTPAYPDEND>20070816\n" +
            "<I.VOID>N\n" +
            "<I.ONSERVICE>Y\n" +
            "<I.TXLINE>\n" +
            "<I.ACCTNAME>Payroll Expenses\n" +
            "<I.AMT>$2.10\n" +
            "<I.MEMO>Fee for 2 direct deposit(s) at $1.05 each\n" +
            "</I.TXLINE>\n" +
            "<I.TXLINE>\n" +
            "<I.ACCTNAME>Payroll Expenses\n" +
            "<I.AMT>$3.00\n" +
            "<I.MEMO>Direct Deposit Transmission Fee\n" +
            "</I.TXLINE>\n" +
            "<I.TXLINE>\n" +
            "<I.ACCTNAME>Payroll Expenses\n" +
            "<I.AMT>0.51\n" +
            "<I.MEMO>Sales Tax for null\n" +
            "</I.TXLINE>\n" +
            "<I.TXLINE>\n" +
            "<I.AMT>$10203.55\n" +
            "<I.ISDD>Y\n" +
            "</I.TXLINE>\n" +
            "</I.PAYROLLTX>\n" +
            "</I.PAYROLLRS>\n" +
            "</I.PAYROLLTRNRS>\n" +
            "</I.PAYROLLUPDATERS>\n" +
            "</I.PAYROLLMSGSRSV1>\n" +
            "</OFX>";
}
