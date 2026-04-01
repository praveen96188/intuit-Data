package com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 12, 2008
 * Time: 4:12:21 PM
 */
public class TestMonthlyGemsUploadBatchProcess {
    private static Company1Dataloader c1dl;
    private static Company2Dataloader c2dl;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        c2dl = new Company2Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        loadDataHappyPath();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test case to generate the Gems Monthly Balance Data for a given reporint period.
     */
    @Test
    public void testHappyPath_GenerateMonthlyGemsData() {
        //Lode Payrolls and Generate Monthly Gems Data
        GemsUploadBatch batch = loadDataToGenerateMonthlyGemsData("200710");

        PayrollServices.beginUnitOfWork();
        //Assertion for Upload status
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Assertions for GemsMonthly Balance Data
        MonthlyGemsFileValidator.validateData(batch, "200710", "200709");
        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * Test Case to generate Gems Monthly Balance Date for the given Current & Previous periods.
     */
    @Test
    public void testGenerateMonthlyGemsDataForPreviousAndCurrentPeriods() {
        //Generate GemsMonthly Data for Previous Period
        GemsUploadBatch batch = loadDataToGenerateMonthlyGemsData("200709");

        PayrollServices.beginUnitOfWork();
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);
        MonthlyGemsFileValidator.validateData(batch, "200709", "200708");
        PayrollServices.commitUnitOfWork();

        //Generate GemsMonthly Data for Current Period
        PayrollServices.beginUnitOfWork();
        MonthlyGemsUploadBatchProcess uploadProcess = new MonthlyGemsUploadBatchProcess();
        uploadProcess.process("gen", "200710", null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        batch = uploadProcess.getUploadBatch();
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);
        MonthlyGemsFileValidator.validateData(batch, "200710", "200709");
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test case to generate Gems Monthly Balance Data for the period which has already been generated.
     */
    @Test
    public void testGenerateMonthlyGems_AlreadyUploadedException() {

        //Generate GemsMonthly Data for Current Period
        GemsUploadBatch batch = loadDataToGenerateMonthlyGemsData("200710");

        PayrollServices.beginUnitOfWork();
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);
        MonthlyGemsFileValidator.validateData(batch, "200710", "200709");
        PayrollServices.commitUnitOfWork();

        //Generate GemsMonthly Data for Same Period which has already been generated.
        PayrollServices.beginUnitOfWork();
        MonthlyGemsUploadBatchProcess uploadProcess = new MonthlyGemsUploadBatchProcess();

        try{
            uploadProcess.process("gen", "200710", null);
            batch = uploadProcess.getUploadBatch();
        }catch(Exception ex){
            Assert.assertEquals("Exception Message", "java.lang.RuntimeException: The period being requested has already been uploaded to GEMS " +
                    "(batch id: "+batch.getBatchId()+"). If you need to re-generate a batch, use the 'regen' switch with the appropriate " +
                    "batch id."
                    , ex.getMessage());
        }

        PayrollServices.rollbackUnitOfWork();        
    }

    /**
     * Test case to generate the Monthly Gems File for a given batch id
     */
    @Test
    public void testGenerateMonthlyGemsFile() {
        //Generate Monthly Gems Data
        GemsUploadBatch batch = loadDataToGenerateMonthlyGemsData("200710");

        PayrollServices.beginUnitOfWork();
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);
        MonthlyGemsFileValidator.validateData(batch, "200710", "200709");
        PayrollServices.commitUnitOfWork();

        //Generate the Monthly Gems file for a given batch id.
        PayrollServices.beginUnitOfWork();
        MonthlyGemsUploadBatchProcess uploadProcess = new MonthlyGemsUploadBatchProcess();
        uploadProcess.process("file", null, String.valueOf(batch.getBatchId()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        batch = Application.findById(GemsUploadBatch.class, batch.getId());
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.PendingTransmission);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test case to regenrate an exisiting Batch for the given batch id
     */
    @Test
    public void testRegenerateExistingBatch() {
        GemsUploadBatch oldBatch = loadDataToGenerateMonthlyGemsData("200710");

        PayrollServices.beginUnitOfWork();
        MonthlyGemsUploadBatchProcess uploadProcess = new MonthlyGemsUploadBatchProcess();
        uploadProcess.process("regen", null, String.valueOf(oldBatch.getBatchId()));
        GemsUploadBatch batch = uploadProcess.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        oldBatch = Application.findById(GemsUploadBatch.class, oldBatch.getId());
        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalanceList = Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.GemsUploadBatch().equalTo(oldBatch));
        PayrollServices.commitUnitOfWork();

        assertEquals("Old Uploaded Batch Status", oldBatch.getUploadStatus(), GemsUploadBatchStatus.Superceded);
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);
        assertEquals("Gems MonthBalance List for Old Batch Id", 0, gemsMonthlyBalanceList.size());
    }

    @Test
    public void tesBadDebt_GenerateMonthlyGemsData() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1dl.returnERDDDB("BatchTest05", "R01");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        MonthlyGemsUploadBatchProcess uploadProcess = new MonthlyGemsUploadBatchProcess();
        uploadProcess.process("gen", "200709", null);
        GemsUploadBatch batch = uploadProcess.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Assertion for Upload status
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);

        //Assertions for GemsMonthly Balance Data
        MonthlyGemsFileValidator.validateData(batch, "200709", "200708");
        PayrollServices.commitUnitOfWork();

        //Generate the Monthly Gems file for a given batch id.
        PayrollServices.beginUnitOfWork();
        uploadProcess = new MonthlyGemsUploadBatchProcess();
        uploadProcess.process("file", null, String.valueOf(batch.getBatchId()));
        PayrollServices.commitUnitOfWork();        
    }

    /**
     * Test case to generate the Monthly Gems File for a given batch id
     */
    @Test
    public void testInvalidBatchId() {
        //Generate Monthly Gems Data
        GemsUploadBatch batch = loadDataToGenerateMonthlyGemsData("200710");

        PayrollServices.beginUnitOfWork();
        assertEquals("Upload Status", batch.getUploadStatus(), GemsUploadBatchStatus.Finalized);
        MonthlyGemsFileValidator.validateData(batch, "200710", "200709");
        PayrollServices.commitUnitOfWork();

        //Generate the Monthly Gems file for a given batch id.
        PayrollServices.beginUnitOfWork();
        MonthlyGemsUploadBatchProcess uploadProcess = new MonthlyGemsUploadBatchProcess();
        try{
            uploadProcess.process("regen", null, "12");
        }catch(Exception ex){
            Assert.assertEquals("Exception Message", "java.lang.RuntimeException: Invalid Batch Id: 12", ex.getMessage());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    public GemsUploadBatch loadDataToGenerateMonthlyGemsData(String pReportingPeriod) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1dl.returnERDDDB("BatchTest05", "R01");
        addCompany2Payroll2();
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        BatchJobManager.runJob(BatchJobType.LedgerBalance);

        PayrollServices.beginUnitOfWork();
        MonthlyGemsUploadBatchProcess uploadProcess = new MonthlyGemsUploadBatchProcess();
        uploadProcess.process("gen", pReportingPeriod, null);
        GemsUploadBatch batch = uploadProcess.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        return batch;
    }



    /**
     * Methods to load data*
     */
    public static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
        persistCompany2();
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-13"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
    }

    private static void persistCompany2() {
        c2dl.persistCompany2();
        PayrollRunDTO payrollRunDTO = c2dl.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany2Payroll2() {
        PayrollRunDTO payrollRunDTO = c2dl.get2ndCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-10"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

/*    private static void loadDataIntuitTxns() {
        ProcessResult<FinancialTransaction> procResult = PayrollServices.financialTransactionManager
                .addIntuit5DayReturnTransferTransaction(
                        SourceSystemCode.valueOf(c1dl.getCompany1().getSourceSystemCd().toString()),
                        c1dl.getCompany1().getCompanyId(), "BatchTest05");

        assertEquals(0, procResult.getMessages().size());
    }*/

    public static DomainEntitySet<TransactionReturn> persistTransactionReturn(String pCompanyID, SourceSystemCode pSourceSystem, String pPayrollRunId) {
        Application.beginUnitOfWork();

        Company company = Company.findCompany(
                pCompanyID, pSourceSystem);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunId);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : c1FinTxns) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R02");
            transactionReturn.setBankReturnDescription("This is a non-NSF description");
            transactionReturn.setBankReturnTraceNumber(112L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());

            returnList.add(Application.save(transactionReturn));
        }
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        Application.commitUnitOfWork();
        return returnList;
    }
}
