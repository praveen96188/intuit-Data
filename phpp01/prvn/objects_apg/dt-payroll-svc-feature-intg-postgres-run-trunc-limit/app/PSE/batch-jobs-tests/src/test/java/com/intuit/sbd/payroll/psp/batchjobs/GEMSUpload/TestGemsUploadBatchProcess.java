package com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.batchjobs.utils.GemsCompare;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Collection;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testing the GEMS upload process.
 */
public class TestGemsUploadBatchProcess {
    private Company mCompany;
    private PayrollRun mPayrollRun;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testSPCFMoneyStackOverflowTriggered() {
        SpcfMoney money = new SpcfMoney("0.00");
        SpcfMoney transaction = new SpcfMoney ("0.99");
        try {for (int x = 0; x < 40000; x++) {
                money = new SpcfMoney (money.add(transaction));
            }
            TestCase.fail();
        } catch (Throwable t) {
            assertTrue (t instanceof StackOverflowError);
        }
    }

    @Test
    public void testSPCFMoneySuccess() {
        SpcfMoney money = new SpcfMoney("0.00");
        SpcfMoney transaction = new SpcfMoney ("1.00");
        try {
            for (int x = 0; x < 40000; x++) {
                money = (SpcfMoney) money.add(transaction);
            }
            assertEquals(new SpcfMoney("40000.00"), money);
        } catch (Throwable t) {
            TestCase.fail(t.getMessage());
        }
    }

     /*
    @Test
    public void testBigNumbersPSRV1406() throws Exception {
        loadData(null);

        PayrollServices.beginUnitOfWork();
        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        SpcfMoney money = new SpcfMoney("0");

        int x=0;
        try {
            System.out.println("Free memory before:" + Runtime.getRuntime().freeMemory());
            DomainEntitySet<FinancialTransaction> transactions = new DomainEntitySet<FinancialTransaction>();
            for (x = 0; x < 40000; x++) {
                if (x % 1000 == 0) {
                    System.out.println("On iteration:" + x);
                }
                FinancialTransaction ft = new FinancialTransaction();
                ft.setCompany(mCompany);
                ft.setFinancialTransactionAmount(new SpcfMoney("0.99"));
                ft.setCreatedDate(PSPDate.getPSPTime());
                ft.setCreditBankAccountType(BankAccountOwnerType.Company);
                ft.setDebitBankAccountType(BankAccountOwnerType.Company);
                ft.setSettlementDate(PSPDate.getPSPTime());
                ft.setDebitBankAccount(new BankAccount());
                ft.setCreditBankAccount(new BankAccount());
                ft.setCreatorId("Unit Tester");
                
                transactions.add(ft);

                money = (SpcfMoney)money.add(ft.getFinancialTransactionAmount());
            }
            System.out.println("Free memory after:" + Runtime.getRuntime().freeMemory());
        } catch (Throwable t) {
            System.out.println("Made it to iteration: " + x);
            System.out.println("Money:" + money);
            t.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        System.out.println("Money total:" + money.toString());
    } */

    /**
     * Test case to generate the Gems Daily Upload file with a single SKU and zero tax amount
     * @throws Exception exception
     */
    @Test
    public void testGemsUploadProcess() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        loadData(null);

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20111027000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Generate Gems Daily Upload File
        PayrollServices.beginUnitOfWork();
        DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
        process.createFile();
        GemsUploadBatch batch = process.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        assertEquals("Upload Status", GemsUploadBatchStatus.Finalized, batch.getUploadStatus());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of DdServiceFee Executed txns", 1, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax Executed txns", 1, taxTxns.size());

        //Make sure the upload file is correct
        validateFile(
                Application.findFileOnClassPath("gemsupload/expected/testGemsDailyUpload_expected.txt"),
                batch.getFileName());
    }

    @Ignore
    @Test
    public void testGemsUploadProcess_scp() throws Exception {
        loadData(null);

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        BatchJobManager.runJob(BatchJobType.GemsAccountsReceivable);
    }

    /**
     * Test case to test the Reupload process for the given uploaded batch.
     * @throws Exception exception
     */
    @Test
    public void testGemsUploadProcess_ReUpload() throws Exception {
        //Submit Payroll

        SpcfCalendar taxExpirationDate = PSPDate.getPSPTime();
        taxExpirationDate.addYears(1);
        loadData(taxExpirationDate);

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Generate Gems Daily Upload file
        PayrollServices.beginUnitOfWork();
        DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
        process.createFile();
        GemsUploadBatch batch = process.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        assertEquals("Upload Status", GemsUploadBatchStatus.Finalized, batch.getUploadStatus());

        //Generate Gems Daily ReUpload file
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110929080000");
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        process = new DailyGemsUploadBatchProcess();
        process.createFile(batch.getBatchId(),PSPDate.getPSPTime());
        GemsUploadBatch reUploadedBatch = process.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        GemsUploadBatch oldBatch = Application.findById(GemsUploadBatch.class, batch.getId());
        PayrollServices.commitUnitOfWork();

        assertEquals("Old Uploaded Batch Status", GemsUploadBatchStatus.Superceded, oldBatch.getUploadStatus());
        assertEquals("Upload Status", GemsUploadBatchStatus.Finalized, reUploadedBatch.getUploadStatus());

        //Make sure the files are correct
        validateFile(
                Application.findFileOnClassPath("gemsupload/expected/testGemsDailyReUpload_expected.txt"),
                batch.getFileName());
    }

    /**
     * Test case to generate the Gems Daily upload for the multiple SKU's with tax amount
     * @throws Exception exceptions
     */
    @Test
    public void testGemsUploadProcess_WithMultipleSKU() throws Exception {
        //Submit Payrolls
        loadDataForMultiplePayrolls(null);

        //Add billing details
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        mCompany = Application.refresh(mCompany);

        CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);
        // add something besides the per-paycheck fee, and force its settlement date to match that of the other fee
        BillingDetail.createBillingDetailWithPriceAndSettlementDateOverride(payrollRun, companyBankAccount, OfferingServiceChargeType.ReversalFee, 1, new BigDecimal(50.00),
                                                      SpcfCalendar.createInstance(2011,10,1, SpcfTimeZone.getLocalTimeZone()), null, null);

        PayrollServices.commitUnitOfWork();

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110914000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110930000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        DomainEntitySet<FinancialTransaction> feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of DdServiceFee Executed txns", 2, feeTxns.size());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId02");
        feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of DdServiceFee Executed txns", 1, feeTxns.size());

        //Execute Gems Upload Batch process
        PayrollServices.beginUnitOfWork();
        DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
        process.createFile();
        GemsUploadBatch batch = process.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        //Make sure the generated daily upload file is correct
        validateFile(
                Application.findFileOnClassPath("gemsupload/expected/testGemsDailyUploadMultSKU_expected.txt"),
                batch.getFileName());
    }

    @Test
    public void testNegativeSKUQuantityAndAmount() throws Exception {
        SpcfCalendar targetPayrollDate = SpcfCalendar.createInstance(/*2011, 10, 2,*/ 2008, 4, 1, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar offloadDate = targetPayrollDate.copy();
        offloadDate.addDays(-4);

        SpcfCalendar today = offloadDate.copy();
        today.addDays(-23);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(today);//(SpcfCalendar.createInstance(2011, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        ProcessResult<PayrollRun> prPayroll = createCompanyAndSubmitPayroll(targetPayrollDate);
        PayrollServicesTest.assertSuccess("Payroll submission", prPayroll);

        PayrollRun payroll = prPayroll.getResult();
        Company company = payroll.getCompany();

        // advance the PSP time and offload the payroll transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(offloadDate);//(SpcfCalendar.createInstance(2011, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        // get the debit transactions
        DomainEntitySet<FinancialTransaction> ftDebits = findTransactions(payroll, TransactionTypeGroupCode.Debit, TransactionStateCode.Executed);
        Assert.assertEquals("Number of debit FTs", 3, ftDebits.size()); // 1 DD, 1 (per-paycheck) fee and 1 sales tax

        PayrollServices.commitUnitOfWork();

        // simulate an NSF bank return of those transactions
        DomainEntitySet<TransactionReturn> returnList = persistNSF(company.getSourceCompanyId(), company.getSourceSystemCd(), payroll.getSourcePayRunId());
        assertEquals("Number of txn returns", 1, returnList.size());

        // call the return handler
        Application.beginUnitOfWork();
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(returnList.get(0));
        handler.execute(returnList.get(0));
        Application.commitUnitOfWork();

        // make sure the NSF put the payroll in the right status
        PayrollServices.beginUnitOfWork();
        payroll = Application.refresh(payroll);
        DomainEntitySet<FinancialTransaction> ftRedebitsCreated = findTransactions(payroll, TransactionTypeGroupCode.Redebit, TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("PayrollRun status after NSF return", PayrollStatus.PendingAutoRedebit, payroll.getPayrollRunStatus());
        assertEquals("Redebits created", 3, ftRedebitsCreated.size()); // 1 for DD, 1 for (per-paycheck) fee, 1 sales tax

        // fully/partially collect any/all of DD/fee/tax
        PayrollServices.beginUnitOfWork();
        payroll = Application.refresh(payroll);
        ftDebits = findTransactions(payroll, TransactionTypeGroupCode.Debit, TransactionStateCode.Returned);

        FinancialTransaction origFT = ftDebits.get(0);
        Assert.assertEquals(TransactionTypeCode.EmployerDdDebit, origFT.getTransactionType().getTransactionTypeCd());
        Assert.assertTrue("Hibernate Session exists", Application.getHibernateSession() != null);
        createRelatedFT(TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Completed, 1.23, origFT, payroll);
        Application.commitUnitOfWork();

        // write off whatever is left
        Application.beginUnitOfWork();
        ProcessResult prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payroll.getSourcePayRunId());
        Application.commitUnitOfWork();

        assertSuccess("Writeoff", prWriteoff);

        Application.beginUnitOfWork();

        // make sure the writeoff put the payroll in the right status
        payroll = Application.refresh(payroll);
        assertEquals("PayrollRun status after writeoff", PayrollStatus.WrittenOff, payroll.getPayrollRunStatus());

        // make sure the return-related redebits are now Cancelled
        for (FinancialTransaction ftRedebit : ftRedebitsCreated) {
            ftRedebit = Application.findById(FinancialTransaction.class, ftRedebit.getId()); // get a fresh copy
            assertTrue(ftRedebit.getTransactionType().getTransactionTypeCd().toString()+" is Cancelled",
                       ftRedebit.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Cancelled);
        }

        Application.commitUnitOfWork();

        // offload ER WriteOff Fee
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Generate Gems Daily Upload File
        PayrollServices.beginUnitOfWork();
        DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
        process.createFile();
        GemsUploadBatch batch = process.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        assertEquals("Upload Status", GemsUploadBatchStatus.Finalized, batch.getUploadStatus());

        PayrollServices.beginUnitOfWork();
        payroll = Application.findById(PayrollRun.class, payroll.getId());
        DomainEntitySet<FinancialTransaction> feeTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerWriteOffFee},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of DdServiceFee Executed txns", 1, feeTxns.size());

        //Make sure the upload file is correct
        validateFile(
                Application.findFileOnClassPath("gemsupload/expected/testGemsDailyUploadNegativeValues_expected.txt"),
                batch.getFileName());
    }


    /**
     * Test case to test the Reupload process with the invalid batch id.
     * @throws Exception exception
     */
    @Test
    public void testGemsUploadProcessReUploadWithInvalidBatchId() throws Exception {
        //Submit Payroll

        SpcfCalendar taxExpirationDate = PSPDate.getPSPTime();
        taxExpirationDate.addYears(1);
        loadData(taxExpirationDate);

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Generate Gems Daily Upload file
        PayrollServices.beginUnitOfWork();
        DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
        process.createFile();
        GemsUploadBatch batch = process.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        assertEquals("Upload Status", GemsUploadBatchStatus.Finalized, batch.getUploadStatus());

        //Generate Gems Daily ReUpload file
        PayrollServices.beginUnitOfWork();
        try {
            PSPDate.setPSPTime("20110521000000");
            process.createFile("123",PSPDate.getPSPTime());
        } catch (Exception ex) {
            Assert.assertEquals("Exception Message", "Invalid Batch Id: 123", ex.getMessage());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Function to load the data for payroll
     *
     * @param pTaxExemptExpirationDate SpcfCalendar
     * @throws Exception exception
     */
    public void loadData(SpcfCalendar pTaxExemptExpirationDate) throws Exception {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmitForCompany123272727WithValidAddress(PSPDate.getPSPTime());
        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);

        // make sure that company is at an address that will be subject to sales tax
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(mCompany);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        if (pTaxExemptExpirationDate != null) {
            /*  Tax Exemption is intended   */
            dtoUpdate.setTaxExemptExpirationDate(new DateDTO(pTaxExemptExpirationDate));
            dtoUpdate.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        }
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(mCompany.getSourceSystemCd(),
                                                                                       mCompany.getSourceCompanyId(),
                                                                                       dtoUpdate);

        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Updating company address for taxability", prUpdate);
        mCompany = prUpdate.getResult();

        //Submit payroll
        PayrollServices.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
                "123272727", payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        // add a per-transmission charge -- it's subject to sales tax at the TAXABLE_ADDRESS... per-check is not
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> found = FinancialTransaction.findFinancialTransactions(
                                                    mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(),
                                                    TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        SpcfCalendar settlementDate = found.get(0).getSettlementDate().toLocal();
        PayrollRun payroll = processResult.getResult();
        payroll = Application.refresh(payroll);
        CompanyBankAccount cba = payroll.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);
        BillingDetail.createBillingDetail(payroll, cba, OfferingServiceChargeType.PerTransmission, 1, settlementDate, companyOffering.getOffering().getOfferingCode()); // a taxable fee -- per-check is not
        PayrollServices.commitUnitOfWork();

        //Claim offer for Company
        PayrollServices.beginUnitOfWork();
        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);

        Offer offer = Offer.findOfferByOfferCode("1099426"); // P57553 is 50% off per-paycheck charges
        CompanyOffer companyOffer = mCompany.claimOfferForCompany(offer);
        mPayrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        PayrollServices.commitUnitOfWork();

        assertTrue("Company Offer", companyOffer != null);
        assertTrue("Company Offer is Active", companyOffer.companyOfferIsActive());
    }

    /**
     * Function to load the data for payroll
     *
     * @param pTaxExemptExpirationDate SpcfCalendar
     * @throws Exception exception
     */
    private void loadDataForMultiplePayrolls(SpcfCalendar pTaxExemptExpirationDate) throws Exception {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Collection<PayrollRunDTO> payrollRunDTOCollection = psdl.loadMultiplePayrollsForCompany123272727WithValidAddresForGemsUploadMultiplePayrolls(PSPDate.getPSPTime());
        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);

        // make sure that company is at an address that will be subject to sales tax
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(mCompany);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        if (pTaxExemptExpirationDate != null) {
            /*  Tax Exemption is intended   */
            dtoUpdate.setTaxExemptExpirationDate(new DateDTO(pTaxExemptExpirationDate));
            dtoUpdate.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        }
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(mCompany.getSourceSystemCd(),
                                                                                       mCompany.getSourceCompanyId(),
                                                                                       dtoUpdate);

        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Updating company address for taxability", prUpdate);
        mCompany = prUpdate.getResult();

        for (PayrollRunDTO payrollRunDTO : payrollRunDTOCollection) {
            //Submit payroll
            PayrollServices.beginUnitOfWork();

            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
                    "123272727", payrollRunDTO);

            PayrollServices.commitUnitOfWork();
            assertTrue("Process Result", processResult.isSuccess());
        }

        //Claim offer for Company
        PayrollServices.beginUnitOfWork();
        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);
        Offer offer = Offer.findOfferByOfferCode("P57553"); // P57553 is 50% off per-paycheck charges
        CompanyOffer companyOffer = mCompany.claimOfferForCompany(offer);
        mPayrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        PayrollServices.commitUnitOfWork();

        assertTrue("Company Offer", companyOffer != null);
        assertFalse("Company Offer is Active", companyOffer.companyOfferIsActive()); //Offer is no more valid

    }

    /**
     * Function to validate the created file with the expected file
     * @param pExpectedFileName Expected File Name
     * @param pCreatedFileName Created File Name
     */
    private void validateFile(String pExpectedFileName, String pCreatedFileName) {
        try {
            BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
            Key key = IDPSFileStreamManager.newKeyHandleLatest();
            BufferedReader compareReader;

            if(StreamUtil.isFileIDPSEncrypted(pCreatedFileName)){
                compareReader = new BufferedReader(new IDPSFileReader(pCreatedFileName,key));
            }else {
                compareReader = new BufferedReader(new FileReader(pCreatedFileName));
            }

            GemsCompare compare = new GemsCompare();
            CompareResults compareResults = compare.compareGemsUploadFile(expectedReader, compareReader, ReportingFrequency.Daily);

            if (!compareResults.getStatus()) {
                System.out.println(compareResults.toString());
            }
            assertEquals("File "+pCreatedFileName+" matches expected file "+pExpectedFileName, true, compareResults.getStatus());

        } catch (Exception ex) {
            ex.printStackTrace();
            TestCase.fail(ex.getMessage());
        }
    }

    private PayrollRun getPayrollRun() {
        return Application.findById(PayrollRun.class, mPayrollRun.getId());
    }

    private ProcessResult<PayrollRun> createCompanyAndSubmitPayroll(SpcfCalendar pTargetPayrollDate) {
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        // this creates the company and other stuff and offloads the bank verfication debits
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        payrollRunDTO.setTargetPayrollTXDate(new DateDTO(pTargetPayrollDate));

        PayrollServices.commitUnitOfWork();

        SpcfMoney mPayrollNetAmount = payrollRunDTO.getPayrollDirectDepositAmount();

        // this submits the payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);

        if (prPayroll.isSuccess()) {
            // the DataLoader, used by PayrollSubmitDataLoader, creates Companies with invalid addresses, causing the
            // IAS sales-tax service to fail.  so we fix the address here... state and zip must agree
            Company company = prPayroll.getResult().getCompany();
            company.getLegalAddress().setState("NV");
            company.getLegalAddress().setZipCode("89512");
            Application.save(company);

            prPayroll.setResult( (PayrollRun)Application.findById(PayrollRun.class, prPayroll.getResult().getId()) );
        }
        PayrollServices.commitUnitOfWork();

        return prPayroll;
    }

    private DomainEntitySet<FinancialTransaction> findTransactions(PayrollRun pPayrollRun,
                                                             TransactionTypeGroupCode pGroupCd,
                                                             TransactionStateCode pStateCd) {
        TransactionState state = Application.findById(TransactionState.class, pStateCd);

        Criterion<FinancialTransaction> where = FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                                .And(FinancialTransaction.CurrentTransactionState().equalTo(state));

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .OrderBy(FinancialTransaction.TransactionType()); // sort them by type... this conveniently puts DD before Fee before SalesAndUseTax

        DomainEntitySet<FinancialTransaction> found = Application.find(FinancialTransaction.class, query);

        DomainEntitySet<FinancialTransaction> matches = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction ft : found) {
            if (ft.getTransactionType().getTransactionTypeGroupCd() == pGroupCd) {
                matches.add(ft);
            }
        }

        return matches;
    }

    private DomainEntitySet<TransactionReturn> persistNSF(String pCompanyID, SourceSystemCode pSourceSystem, String pPayrollRunId) {
        Application.beginUnitOfWork();

        Company company = Company.findCompany(
                pCompanyID, pSourceSystem);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunId);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2011, 12, 9, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2011, 12, 9, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : c1FinTxns) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("This is an NSF description");
            transactionReturn.setBankReturnTraceNumber(112L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2011, 12, 9,
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

    private FinancialTransaction createRelatedFT(TransactionTypeCode pTypeCd, TransactionStateCode pStateCd,
                                                 double pAmount, FinancialTransaction pOrigFT, PayrollRun pPayroll) {
        IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit),
                CreditDebitCode.Credit);

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(pPayroll.getCompany());

        // create the FTtestNegativeSKUQuantityAndAmount
        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(settlementDate, 1);
        CalendarUtils.clearTime(settlementDate);

        FinancialTransaction ft = FinancialTransaction.createFinancialTransaction(
                                    pPayroll.getCompany(), pPayroll, null, iba.getBankAccount(), cba.getBankAccount(),
                                    BankAccountOwnerType.Intuit, BankAccountOwnerType.Company, pTypeCd,
                                    new SpcfMoney(String.valueOf(pAmount)), SettlementType.ACH, settlementDate,
                                    pOrigFT.getSku(), pOrigFT, 1);

        // advance it to the requested state
        if (pStateCd != TransactionStateCode.Created) {
            TransactionState executed = Application.findById(TransactionState.class, TransactionStateCode.Executed);
            ft.addTransactionState(executed);

            if (pStateCd == TransactionStateCode.Completed) {
                TransactionState completed = Application.findById(TransactionState.class, TransactionStateCode.Completed);
                ft.addTransactionState(completed);
            } else if (pStateCd == TransactionStateCode.Returned) {
                TransactionState returned = Application.findById(TransactionState.class, TransactionStateCode.Returned);
                ft.addTransactionState(returned);
            }
        }

        return ft;
    }
    /**
     * if the day the transactions are supposed to be for  the last day of the month and the current date is the first day of the month
     * use the current date for the transaction dates
     * Else
     * The internal date should use the transaction date for the transactions that are going to be used
     * @throws Exception exception
     */
    @Test
    public void testGemsUploadProcess_InternalDateFirstDayOfMonth_PSRV00398() throws Exception {
        //Submit Payroll

        SpcfCalendar taxExpirationDate = PSPDate.getPSPTime();
        taxExpirationDate.addYears(1);
        loadData(taxExpirationDate);

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Generate Gems Daily Upload file
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20111001120000");
        Application.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
        process.createFile(0,SpcfCalendar.createInstance(2011,9,29,12,0,0,0));
        GemsUploadBatch batch = process.getUploadBatch();
        PayrollServices.commitUnitOfWork();

        assertEquals("Upload Status", GemsUploadBatchStatus.Finalized, batch.getUploadStatus());
        validateFile(
                Application.findFileOnClassPath("gemsupload/expected/testGemsDailyPSRV00398_expected.txt"),
                batch.getFileName());
    }
}
