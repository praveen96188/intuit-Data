package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LedgerOperationJobDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ledgeroperations.LedgerOperationsProcessorTests;
import com.intuit.sbd.payroll.psp.batchjobs.util.FluxUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import flux.Engine;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Ankit on 8/5/14.
 */
public class BatchJobControllerTests {
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
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        BatchJobController.restrictedBatchJobTypes.clear();
        BatchJobController.restrictedBatchJobTypes.add(BatchJobType.LedgerOperations);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void happyPathTest_LedgerOperation(){

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        SystemParameter sysParamBatchController = SystemParameter.findSystemParameter(SystemParameter.Code.BATCH_JOB_CONTROLLER_ENABLED);
        sysParamBatchController = Application.refresh(sysParamBatchController);
        sysParamBatchController.setSystemParameterValue("true");
        Application.save(sysParamBatchController);
        //Check the table to ensure that the job running status is false
        Expression<BatchJobStatus> batchJobStatusQuery =
                new Query<BatchJobStatus>()
                        .Where(BatchJobStatus.JobType().equalTo(BatchJobType.LedgerOperations));
        DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        if(batchJobStatusSet != null && batchJobStatusSet.isNotEmpty()){
            BatchJobStatus batchJobStatus = batchJobStatusSet.getFirst();
            batchJobStatus.setIsRunning(Boolean.FALSE);
            Application.save(batchJobStatus);
        }
        PayrollServices.commitUnitOfWork();
        //Set up batch job data
        LedgerOperationJob job = setupLedgerOperationsData();
        //Run the job
        try{
            BatchJobManager.runJob(BatchJobType.LedgerOperations);
        }catch(RuntimeException ex){
            assertTrue("Unable to run batch job", Boolean.FALSE);
        }
        //Verify it has run correctly
        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        PayrollServices.rollbackUnitOfWork();
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());

    }

    @Test
    public void systemParameterFalseIgnoreController_LedgerOperation(){

        PayrollServices.beginUnitOfWork();
        //Set system parameter to disable controller
        SystemParameter sysParamBatchController = SystemParameter.findSystemParameter(SystemParameter.Code.BATCH_JOB_CONTROLLER_ENABLED);
        sysParamBatchController = Application.refresh(sysParamBatchController);
        sysParamBatchController.setSystemParameterValue("false");
        Application.save(sysParamBatchController);
        //Check the table to ensure that the job running status is true
        Expression<BatchJobStatus> batchJobStatusQuery =
                new Query<BatchJobStatus>()
                        .Where(BatchJobStatus.JobType().equalTo(BatchJobType.LedgerOperations));
        DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        if(batchJobStatusSet != null && batchJobStatusSet.isNotEmpty()){
            BatchJobStatus batchJobStatus = batchJobStatusSet.getFirst();
            batchJobStatus.setIsRunning(Boolean.TRUE);
            Application.save(batchJobStatus);
        }

        PayrollServices.commitUnitOfWork();
        //Set up batch job data
        LedgerOperationJob job = setupLedgerOperationsData();
        //Run the job
        try{
            BatchJobManager.runJob(BatchJobType.LedgerOperations);
        }catch(RuntimeException ex){
            assertTrue("Unable to run batch job", Boolean.FALSE);
        }
        //Verify it has run correctly
        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        PayrollServices.rollbackUnitOfWork();
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());

    }

    @Test
    public void failureToExecuteMultipleInstances_LedgerOperation(){

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        SystemParameter sysParamBatchController = SystemParameter.findSystemParameter(SystemParameter.Code.BATCH_JOB_CONTROLLER_ENABLED);
        sysParamBatchController = Application.refresh(sysParamBatchController);
        sysParamBatchController.setSystemParameterValue("true");
        Application.save(sysParamBatchController);
        //Check the table to ensure that the job running status is true
        Expression<BatchJobStatus> batchJobStatusQuery =
                new Query<BatchJobStatus>()
                        .Where(BatchJobStatus.JobType().equalTo(BatchJobType.LedgerOperations));
        DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        if(batchJobStatusSet != null && batchJobStatusSet.isNotEmpty()){
            BatchJobStatus batchJobStatus = batchJobStatusSet.getFirst();
            batchJobStatus.setIsRunning(Boolean.TRUE);
            Application.save(batchJobStatus);
        }

        PayrollServices.commitUnitOfWork();
        //Set up batch job data
        LedgerOperationJob job = setupLedgerOperationsData();
        //Run the job
        Boolean isExceptionOccured = Boolean.FALSE;
        try{
            BatchJobManager.runJob(BatchJobType.LedgerOperations);
        }catch(RuntimeException ex){
            isExceptionOccured = Boolean.TRUE;
        }
        //verify exception occured
        assertTrue("Failed to prevent mulitple instances from being created",isExceptionOccured);
        //Verify it has run correctly
        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        PayrollServices.rollbackUnitOfWork();
        assertEquals(LedgerOperationJobStatus.Queued, job.getStatus());

    }

    @Test
    public void ignoreBatchJobsNotUnderController_LedgerOperation(){

        PayrollServices.beginUnitOfWork();
        //Remove ledger operation job from controller
        BatchJobController.restrictedBatchJobTypes.remove(BatchJobType.LedgerOperations);
        //Set system parameter to enable controller
        SystemParameter sysParamBatchController = SystemParameter.findSystemParameter(SystemParameter.Code.BATCH_JOB_CONTROLLER_ENABLED);
        sysParamBatchController = Application.refresh(sysParamBatchController);
        sysParamBatchController.setSystemParameterValue("true");
        Application.save(sysParamBatchController);
        //Check the table to ensure that the job running status is true
        Expression<BatchJobStatus> batchJobStatusQuery =
                new Query<BatchJobStatus>()
                        .Where(BatchJobStatus.JobType().equalTo(BatchJobType.LedgerOperations));
        DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        if(batchJobStatusSet != null && batchJobStatusSet.isNotEmpty()){
            BatchJobStatus batchJobStatus = batchJobStatusSet.getFirst();
            batchJobStatus.setIsRunning(Boolean.TRUE);
            Application.save(batchJobStatus);
        }

        PayrollServices.commitUnitOfWork();
        //Set up batch job data
        LedgerOperationJob job = setupLedgerOperationsData();
        try{
            BatchJobManager.runJob(BatchJobType.LedgerOperations);
        }catch(RuntimeException ex){
            assertTrue("Unable to run batch job", Boolean.FALSE);
        }
        //Verify it has run correctly
        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        PayrollServices.rollbackUnitOfWork();
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());

    }

    @Test
    public void batchJobRunningStatusTest_LedgerOperation(){

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        SystemParameter sysParamBatchController = SystemParameter.findSystemParameter(SystemParameter.Code.BATCH_JOB_CONTROLLER_ENABLED);
        sysParamBatchController = Application.refresh(sysParamBatchController);
        sysParamBatchController.setSystemParameterValue("true");
        Application.save(sysParamBatchController);
        //Check the table to ensure that the job running status is true
        Expression<BatchJobStatus> batchJobStatusQuery =
                new Query<BatchJobStatus>()
                        .Where(BatchJobStatus.JobType().equalTo(BatchJobType.LedgerOperations));
        DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        if(batchJobStatusSet != null && batchJobStatusSet.isNotEmpty()){
            BatchJobStatus batchJobStatus = batchJobStatusSet.getFirst();
            batchJobStatus.setIsRunning(Boolean.FALSE);
            Application.save(batchJobStatus);
        }

        PayrollServices.commitUnitOfWork();
        //Set up batch job data
        LedgerOperationJob job = setupLedgerOperationsData();
        try{
            BatchJobManager.runJob(BatchJobType.LedgerOperations);
        }catch(RuntimeException ex){
            assertTrue("Unable to run batch job", Boolean.FALSE);
        }
        //Verify it has run correctly
        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        //Verify the status and timestamp info is captured
        batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        assertFalse("Verify that the batch is not running now", batchJobStatusSet.getFirst().getIsRunning());
        assertTrue("start timestamp is not null", batchJobStatusSet.getFirst().getLastStartedTimeStamp()!=null);
        assertTrue("end timestamp is not null", batchJobStatusSet.getFirst().getLastEndedTimeStamp()!=null);
        PayrollServices.rollbackUnitOfWork();

    }

    private LedgerOperationJob setupLedgerOperationsData(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find());
        assertEquals(new SpcfMoney("264.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = LedgerOperationsProcessorTests.createJobDTO("2012-12-31", "66", new LedgerOperationsProcessorTests.LedgerOperationCreator(company, "1.00"));
        return LedgerOperationsProcessorTests.createAndQueue(jobDTO);
    }
}
