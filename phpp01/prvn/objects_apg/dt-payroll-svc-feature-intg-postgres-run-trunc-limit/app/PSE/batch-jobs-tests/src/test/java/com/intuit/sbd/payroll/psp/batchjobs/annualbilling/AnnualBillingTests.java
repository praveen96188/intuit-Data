package com.intuit.sbd.payroll.psp.batchjobs.annualbilling;

import com.intuit.ems.payroll.psp.gateways.tfs.TFSGateway;
import com.intuit.ems.payroll.psp.gateways.tfs.TFSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.tfs.TFSMockGateway;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.FakeSalesTaxGateway;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AnnualBillingProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayImpl;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 12/3/12
 * Time: 10:43 AM
 */
public class AnnualBillingTests {

    @BeforeClass
    public static void beforeClass() {
        SalesTaxGatewayFactory.setInstanceClass(FakeSalesTaxGateway.class);
        SocketManagerFactory.setInstanceClass(MockSocketManager.class);
    }

    @AfterClass
    public static void afterClass() {
        SalesTaxGatewayFactory.setInstanceClass(SalesTaxGatewayImpl.class);
        SocketManagerFactory.setInstanceClass(null);
    }

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.reinitialize();
        TFSMockGateway.reset();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
        TFSGatewayFactory.setInstanceClass(TFSGateway.class);
    }

    @Test
    public void HappyPath() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(company.getSourceCompanyId(), 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        //Run the job with run mode = UsingFlux
        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.UsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        AnnualBillingItem annualBillingItem = annualBillingBatch.getAnnualBillingItemCollection().getFirst();
        assertEquals(50, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Completed, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        company = annualBillingItem.getCompany();

        //Make sure the emails are created
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeCreated);
        assertEquals(1, feeEvents.size());
        for (CompanyEvent companyEvent : feeEvents) {
            DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.FinancialTransactionId);
            assertEquals(4, companyEventDetails.size());
            DomainEntitySet<CompanyEventEmail> companyEventEmails = companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.BilledNonPayrollRelatedFee2));
            assertEquals(1, companyEventEmails.size());
        }

        SpcfCalendar checkDate = SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.clearTime(checkDate);

        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2013, 1, 4, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.clearTime(settlementDate);

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly);
        assertEquals(1, payrollRuns.size());

        PayrollRun payrollRun = payrollRuns.getFirst();
        assertEquals(PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
        assertEquals(checkDate, payrollRun.getPaycheckDate().toLocal());
        assertEquals(settlementDate, payrollRun.getPaycheckSettlementDate().toLocal());

        settlementDate = SpcfCalendar.createInstance(2013, 1, 3, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.clearTime(settlementDate);

        assertEquals(2, payrollRun.getBillingDetailCollection().size());
        for (BillingDetail billingDetail : payrollRun.getBillingDetailCollection()) {
            switch (billingDetail.getOfferingServiceChargeType()) {
                case W2Fee:
                    assertEquals(SpcfMoney.createInstance("212.58"), billingDetail.getItemTotal());
                    assertEquals(SpcfMoney.createInstance("212.50"), billingDetail.getUnitPrice());
                    assertEquals(SpcfMoney.createInstance(".08"), billingDetail.getTaxAmount());
                    assertEquals("W2 Fee", billingDetail.getItemName());
                    assertEquals("297362", billingDetail.getItemSku());
                    assertEquals("Fee per W2", billingDetail.getMemo());

                    assertEquals(2, billingDetail.getFinancialTransactionCollection().size());
                    for (FinancialTransaction ft : billingDetail.getFinancialTransactionCollection()) {
                        switch (ft.getTransactionType().getTransactionTypeCd()) {
                            case EmployerFeeDebit:
                                assertEquals(SpcfMoney.createInstance("212.50"), ft.getFinancialTransactionAmount());
                                assertEquals(settlementDate, ft.getSettlementDate().toLocal());
                                assertEquals(SettlementType.ACH, ft.getSettlementTypeCd());
                                assertEquals(TransactionStateCode.Created, ft.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                                assertEquals("297362", ft.getSku());
                                break;
                            case ServiceSalesAndUseTax:
                                assertEquals(SpcfMoney.createInstance(".08"), ft.getFinancialTransactionAmount());
                                assertEquals(settlementDate, ft.getSettlementDate().toLocal());
                                assertEquals(SettlementType.ACH, ft.getSettlementTypeCd());
                                assertEquals(TransactionStateCode.Created, ft.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                                assertEquals("297362", ft.getSku());
                                break;
                            default:
                                fail("Unexpected TransactionType");
                        }
                    }
                    break;
                case W2BaseFee:
                    assertEquals(SpcfMoney.createInstance("40.08"), billingDetail.getItemTotal());
                    assertEquals(SpcfMoney.createInstance("40.00"), billingDetail.getUnitPrice());
                    assertEquals(SpcfMoney.createInstance(".08"), billingDetail.getTaxAmount());
                    assertEquals("W2 Base Fee", billingDetail.getItemName());
                    assertEquals("297363", billingDetail.getItemSku());
                    assertEquals("W2 Processing Setup", billingDetail.getMemo());

                    assertEquals(2, billingDetail.getFinancialTransactionCollection().size());
                    for (FinancialTransaction ft : billingDetail.getFinancialTransactionCollection()) {
                        switch (ft.getTransactionType().getTransactionTypeCd()) {
                            case EmployerFeeDebit:
                                assertEquals(SpcfMoney.createInstance("40.00"), ft.getFinancialTransactionAmount());
                                assertEquals(settlementDate, ft.getSettlementDate().toLocal());
                                assertEquals(SettlementType.ACH, ft.getSettlementTypeCd());
                                assertEquals(TransactionStateCode.Created, ft.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                                assertEquals("297363", ft.getSku());
                                break;
                            case ServiceSalesAndUseTax:
                                assertEquals(SpcfMoney.createInstance(".08"), ft.getFinancialTransactionAmount());
                                assertEquals(settlementDate, ft.getSettlementDate().toLocal());
                                assertEquals(SettlementType.ACH, ft.getSettlementTypeCd());
                                assertEquals(TransactionStateCode.Created, ft.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                                assertEquals("297363", ft.getSku());
                                break;
                            default:
                                fail("Unexpected TransactionType");
                        }
                    }
                    break;
                default:
                    fail("Unexpected OfferingServiceChargeType");
            }
        }
        PayrollServices.rollbackUnitOfWork();

        //Test offloading the W2 Fees
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        assertEquals(PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals(TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        //Test moving the w2 fess to completed
        DataLoadServices.setPSPDate(2013, 1, 7);
        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        assertEquals(PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals(TransactionStateCode.Completed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void SkippedBilling() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(company.getSourceCompanyId(), 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        TaxCompanyServiceInfo taxService = (TaxCompanyServiceInfo) company.getService(ServiceCode.Tax);
        taxService.setFileAnnualReturns(true);
        taxService.setLastQuarterToFile(20124);
        taxService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(taxService);
        PayrollServices.commitUnitOfWork();

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        AnnualBillingItem annualBillingItem = annualBillingBatch.getAnnualBillingItemCollection().getFirst();
        assertEquals(50, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Skipped, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        company = annualBillingItem.getCompany();

        //Make sure the emails are created
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeCreated);
        assertEquals(0, feeEvents.size());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly);
        assertEquals(0, payrollRuns.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void SkippedBilling2() {
        DataLoadServices.setPSPDate(2016, 1, 2);
        Company compCancelled = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        Company compActive = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(compCancelled.getSourceCompanyId(), 5);
        pW2PageCountsByCompany.put(compActive.getSourceCompanyId(), 10);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        // Cancel Tax Service for company1
        PayrollServices.beginUnitOfWork();
        compCancelled = Application.refresh(compCancelled);
        TaxCompanyServiceInfo taxService = (TaxCompanyServiceInfo) compCancelled.getService(ServiceCode.Tax);
        taxService.setFileAnnualReturns(true);
        taxService.setLastQuarterToFile(20154);
        taxService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(taxService);
        PayrollServices.commitUnitOfWork();
        
        // Create Pending AnnualBillingBatch
        PayrollServices.beginUnitOfWork();
        AnnualBillingBatch mAnnualBillingBatch = new AnnualBillingBatch();
        mAnnualBillingBatch.setAnnualBillingBatchStatusCd(AnnualBillingBatchStatusCode.Pending);
        mAnnualBillingBatch.setFormTypeCd(FormTypeCode.W2);
        mAnnualBillingBatch.setFormYear(2012);
        Application.save(mAnnualBillingBatch);
        PayrollServices.commitUnitOfWork();
        
        // Create Pending AnnualBillingItem for compCancelled
        PayrollServices.beginUnitOfWork();
        AnnualBillingItem annualBillingItem = new AnnualBillingItem();
        annualBillingItem.setAnnualBillingBatch(mAnnualBillingBatch);
        annualBillingItem.setCompany(compCancelled);
        annualBillingItem.setFormCount(5);
        annualBillingItem.setAnnualBillingItemStatusCd(AnnualBillingItemStatusCode.Pending);
        Application.save(annualBillingItem);
        PayrollServices.commitUnitOfWork();
        
        // Create Pending AnnualBillingItem for compCancelled
        PayrollServices.beginUnitOfWork();
        annualBillingItem = new AnnualBillingItem();
        annualBillingItem.setAnnualBillingBatch(mAnnualBillingBatch);
        annualBillingItem.setCompany(compActive);
        annualBillingItem.setFormCount(10);
        annualBillingItem.setAnnualBillingItemStatusCd(AnnualBillingItemStatusCode.Pending);
        Application.save(annualBillingItem);
        PayrollServices.commitUnitOfWork();

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2015");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2015);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2015, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());
        assertEquals(2, annualBillingBatch.getAnnualBillingItemCollection().size());

        // validate compCancelled
        annualBillingItem = AnnualBillingItem.findPendingAnnualBillingItems(annualBillingBatch, AnnualBillingItemStatusCode.Skipped).getFirst();
        assertEquals(5, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Skipped, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        compCancelled = annualBillingItem.getCompany();

        //Make sure the emails are not created for compCancelled
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(compCancelled, EventTypeCode.FeeCreated);
        assertEquals(0, feeEvents.size());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(compCancelled, null, null, PayrollType.FeeOnly);
        assertEquals(0, payrollRuns.size());
        
        // Validate compActive
        // annualBillingBatch.getAnnualBillingItemCollection().sort(AnnualBillingItem.CreatedDate());
        annualBillingItem = AnnualBillingItem.findPendingAnnualBillingItems(annualBillingBatch, AnnualBillingItemStatusCode.Completed).getFirst();
        assertEquals(10, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Completed, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        compActive = annualBillingItem.getCompany();

        //Make sure the emails are created for compActive
        feeEvents = CompanyEvent.findCompanyEvents(compActive, EventTypeCode.FeeCreated);
        assertEquals(1, feeEvents.size());

        payrollRuns = PayrollRun.findPayrollRunsByType(compActive, null, null, PayrollType.FeeOnly);
        assertEquals(1, payrollRuns.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void companyNotFound() {
        DataLoadServices.setPSPDate(2013, 1, 2);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put("123456789", 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        assertEquals(0, annualBillingBatch.getAnnualBillingItemCollection().size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void NonTaxCompany() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(company.getSourceCompanyId(), 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Pending, annualBillingBatch.getAnnualBillingBatchStatusCd());

        AnnualBillingItem annualBillingItem = annualBillingBatch.getAnnualBillingItemCollection().getFirst();
        assertEquals(50, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Error, annualBillingItem.getAnnualBillingItemStatusCd());
        assertEquals("Tax service not found.", annualBillingItem.getErrorMessage());

        company = annualBillingItem.getCompany();

        //Make sure the emails are created
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeCreated);
        assertEquals(0, feeEvents.size());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly);
        assertEquals(0, payrollRuns.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void companyOnHold() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        company.addOnHoldReason(ServiceSubStatusCode.AchRejectR1R9);
        PayrollServices.commitUnitOfWork();

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(company.getSourceCompanyId(), 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        AnnualBillingItem annualBillingItem = annualBillingBatch.getAnnualBillingItemCollection().getFirst();
        assertEquals(50, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Completed, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        company = annualBillingItem.getCompany();

        //Make sure the emails are created
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeCreated);
        assertEquals(1, feeEvents.size());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly);
        assertEquals(1, payrollRuns.size());

        DomainEntitySet<FinancialTransaction> onHoldFTs = payrollRuns.getFirst().getFinancialTransactionCollection();
        for (FinancialTransaction onHoldFT : onHoldFTs) {
            assertTrue(onHoldFT.getOnHold());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void returnAnnualBillingFees() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(company.getSourceCompanyId(), 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        AnnualBillingItem annualBillingItem = annualBillingBatch.getAnnualBillingItemCollection().getFirst();
        assertEquals(50, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Completed, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        company = annualBillingItem.getCompany();

        //Make sure the emails are created
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeCreated);
        assertEquals(1, feeEvents.size());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly);
        assertEquals(1, payrollRuns.size());
        PayrollRun payrollRun = payrollRuns.getFirst();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2013, 1, 3);

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(payrollRun.getFinancialTransactionCollection().get(0));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.returnTxns(financialTransactions);

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        company = payrollRun.getCompany();
        DomainEntitySet<FinancialTransaction> redebitTxns = payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.EmployerFeeRedebit);
        assertEquals(2, redebitTxns.size());
        redebitTxns = payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        assertEquals(2, redebitTxns.size());

        // Verify the correct fee was charged for the NSF.
        FinancialTransaction feeTx = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created).getFirst();
        Fee fee = Application.find(Fee.class, Fee.FeeCd().equalTo(FeeTypeCode.FeeOnlyNSFFee)).getFirst();
        assertEquals("Correct NSF Fee Amount", fee.getAmount(), feeTx.getFinancialTransactionAmount());

        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(company, 2013, 1, 3);

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        redebitTxns = payrollRun.getFinancialTransactions(TransactionStateCode.Executed, TransactionTypeCode.EmployerFeeRedebit);
        assertEquals(2, redebitTxns.size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.returnTxns(redebitTxns);

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> returnedTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Returned));
        assertEquals(payrollRun.getFinancialTransactionCollection().size(), returnedTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void returnAnnualBillingFees_noActiveBankAccount() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(company.getSourceCompanyId(), 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        AnnualBillingItem annualBillingItem = annualBillingBatch.getAnnualBillingItemCollection().getFirst();
        assertEquals(50, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Completed, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        company = annualBillingItem.getCompany();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly);
        assertEquals(1, payrollRuns.size());
        PayrollRun payrollRun = payrollRuns.getFirst();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2013, 1, 3);

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(payrollRun.getFinancialTransactionCollection().get(0));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = DataLoadServices.createCompanyBankAccount();
        companyBankAccountDTO.setCompanyBankAccountID(cba.getSourceBankAccountId());
        ProcessResult<CompanyBankAccount> prAddBank =
                PayrollServices.companyManager.changeCompanyBankAccount(company.getSourceSystemCd(),
                                                                     company.getSourceCompanyId(),
                                                                     companyBankAccountDTO,
                                                                     true,
                                                                     false,
                                                                     false);
        assertSuccess("addCompanyBankAccount", prAddBank);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(financialTransactions);

        // no redebits created because there isn't an active bank account
        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        assertEquals(payrollRun.getPayrollRunStatus(), PayrollStatus.DebitReturned);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void zeroFormCount() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(company.getSourceCompanyId(), 0);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();

        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        AnnualBillingItem annualBillingItem = annualBillingBatch.getAnnualBillingItemCollection().getFirst();
        assertEquals(0, annualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Completed, annualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(annualBillingItem.getErrorMessage());

        company = annualBillingItem.getCompany();

        //Make sure the emails are created
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeCreated);
        assertEquals(0, feeEvents.size());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly);
        assertEquals(0, payrollRuns.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void whenCompanyIsDGDissociatedAndTFSReturnsData() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company dgDisassociatedCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        //Mark Company as DG Dissociated
        PayrollServices.beginUnitOfWork();
        dgDisassociatedCompany= Application.refresh(dgDisassociatedCompany);
        dgDisassociatedCompany.setIsDgDisassociated(Boolean.TRUE);
        PayrollServices.commitUnitOfWork();

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(dgDisassociatedCompany.getSourceCompanyId(), 50);

        TFSMockGateway.setW2PageCountsByCompany(pW2PageCountsByCompany);
        TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);

        //Run the job with run mode = UsingFlux
        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.UsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();


        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatch.getFormTypeCd());
        assertEquals(2012, annualBillingBatch.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Completed, annualBillingBatch.getAnnualBillingBatchStatusCd());

        assertEquals(0, annualBillingBatch.getAnnualBillingItemCollection().size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void whenCompanyIsDGDissociated() {
        DataLoadServices.setPSPDate(2013, 1, 2);
        Company dgDisassociatedCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        Company compActive = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Map<String, Integer> pW2PageCountsByCompany = new HashMap<String, Integer>();
        pW2PageCountsByCompany.put(dgDisassociatedCompany.getSourceCompanyId(), 5);
        pW2PageCountsByCompany.put(compActive.getSourceCompanyId(), 10);

        //Mark Company as DG Dissociated
        PayrollServices.beginUnitOfWork();
        dgDisassociatedCompany= Application.refresh(dgDisassociatedCompany);
        dgDisassociatedCompany.setIsDgDisassociated(Boolean.TRUE);
        PayrollServices.commitUnitOfWork();

        // Create Pending AnnualBillingBatch
        PayrollServices.beginUnitOfWork();
        AnnualBillingBatch mAnnualBillingBatch = new AnnualBillingBatch();
        mAnnualBillingBatch.setAnnualBillingBatchStatusCd(AnnualBillingBatchStatusCode.Pending);
        mAnnualBillingBatch.setFormTypeCd(FormTypeCode.W2);
        mAnnualBillingBatch.setFormYear(2012);
        Application.save(mAnnualBillingBatch);
        PayrollServices.commitUnitOfWork();

        // Create Pending AnnualBillingItem for DG Dissociated Company
        PayrollServices.beginUnitOfWork();
        dgDisassociatedCompany= Application.refresh(dgDisassociatedCompany);
        AnnualBillingItem dgDisassociatedAnnualBillingItem = new AnnualBillingItem();
        dgDisassociatedAnnualBillingItem.setAnnualBillingBatch(mAnnualBillingBatch);
        dgDisassociatedAnnualBillingItem.setCompany(dgDisassociatedCompany);
        dgDisassociatedAnnualBillingItem.setFormCount(5);
        dgDisassociatedAnnualBillingItem.setAnnualBillingItemStatusCd(AnnualBillingItemStatusCode.Pending);
        Application.save(dgDisassociatedAnnualBillingItem);
        PayrollServices.commitUnitOfWork();

        // Create Pending AnnualBillingItem for Active Company
        PayrollServices.beginUnitOfWork();
        compActive= Application.refresh(compActive);
        AnnualBillingItem annualBillingItemActive = new AnnualBillingItem();
        annualBillingItemActive.setAnnualBillingBatch(mAnnualBillingBatch);
        annualBillingItemActive.setCompany(compActive);
        annualBillingItemActive.setFormCount(10);
        annualBillingItemActive.setAnnualBillingItemStatusCd(AnnualBillingItemStatusCode.Pending);
        Application.save(annualBillingItemActive);
        PayrollServices.commitUnitOfWork();


        //Run the job with run mode = UsingFlux
        AnnualBillingProcessor annualBillingProcessor =
                new AnnualBillingProcessor(BatchJobProcessor.RunMode.UsingFlux, BatchJobType.AnnualBillingProcessor, UUID.randomUUID().toString(), "2012");
        annualBillingProcessor.executeJob();


        PayrollServices.beginUnitOfWork();

        AnnualBillingBatch annualBillingBatchAfterTest = AnnualBillingBatch.findAnnualBillingBatch(FormTypeCode.W2, 2012);
        assertEquals(FormTypeCode.W2, annualBillingBatchAfterTest.getFormTypeCd());
        assertEquals(2012, annualBillingBatchAfterTest.getFormYear());
        assertEquals(AnnualBillingBatchStatusCode.Pending, annualBillingBatchAfterTest.getAnnualBillingBatchStatusCd());
        assertEquals(2, annualBillingBatchAfterTest.getAnnualBillingItemCollection().size());

        // validate compCancelled
        AnnualBillingItem dgDisAnnualBillingItem = AnnualBillingItem.findPendingAnnualBillingItems(annualBillingBatchAfterTest, AnnualBillingItemStatusCode.Error).getFirst();
        assertEquals(5, dgDisAnnualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Error, dgDisAnnualBillingItem.getAnnualBillingItemStatusCd());
        assertEquals(String.format("Cannot process as Company associated with pAnnualBillingItemId=%s is DG Dissociated true", dgDisAnnualBillingItem.getId()), dgDisAnnualBillingItem.getErrorMessage());

        Company dgDisassociatedCompanyAfterTest = dgDisAnnualBillingItem.getCompany();

        //Make sure the emails are not created for compCancelled
        DomainEntitySet<CompanyEvent> feeEvents = CompanyEvent.findCompanyEvents(dgDisassociatedCompanyAfterTest, EventTypeCode.FeeCreated);
        assertEquals(0, feeEvents.size());

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByType(dgDisassociatedCompanyAfterTest, null, null, PayrollType.FeeOnly);
        assertEquals(0, payrollRuns.size());

        // Validate compActive
        // annualBillingBatch.getAnnualBillingItemCollection().sort(AnnualBillingItem.CreatedDate());
        AnnualBillingItem activeAnnualBillingItem = AnnualBillingItem.findPendingAnnualBillingItems(annualBillingBatchAfterTest, AnnualBillingItemStatusCode.Completed).getFirst();
        assertEquals(10, activeAnnualBillingItem.getFormCount());
        assertEquals(AnnualBillingItemStatusCode.Completed, activeAnnualBillingItem.getAnnualBillingItemStatusCd());
        assertNull(activeAnnualBillingItem.getErrorMessage());

        Company compActiveAfterTest = activeAnnualBillingItem.getCompany();

        //Make sure the emails are created for compActive
        feeEvents = CompanyEvent.findCompanyEvents(compActiveAfterTest, EventTypeCode.FeeCreated);
        assertEquals(1, feeEvents.size());

        payrollRuns = PayrollRun.findPayrollRunsByType(compActiveAfterTest, null, null, PayrollType.FeeOnly);
        assertEquals(1, payrollRuns.size());

        PayrollServices.rollbackUnitOfWork();
    }
}
