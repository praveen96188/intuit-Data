package com.intuit.sbd.payroll.psp.batchjobs.ledgeroperations;

import com.intuit.etx.ContactsDocument;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.EoqSUITaxAdjustments;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.SUIRatePaymentsCleanUp;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.propertyoperators.In;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: dweinberg
 * Date: 11/9/12
 * Time: 5:33 PM
 */
public class LedgerOperationsProcessorTests {

    @Before
    public void runBeforeEachTest() {
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT, "NV-NUCS4072-PAYMENT");
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() {
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
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("265.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"FUTA\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 17, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("FUTA", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.FUTACreditReduction)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 18, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testHappyPathWithWages() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLaws(company, "200");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 11, 8);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "200", new LedgerOperationCreator(company, "1.00").withWages("100.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().set941().setPending().find());
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("1.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"ADDMED-EE\",\"TEST_COMPANY_1\",\"2012-12-31\",\"100.00\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 17, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        LiabilityAdjustment liabilityAdjustment = assertOne(Application.refresh(adjustmentPR).getLiabilityAdjustmentCollection());
        assertEquals(new SpcfMoney("1.00"), liabilityAdjustment.getAmount());
        assertEquals(new SpcfMoney("100.00"), liabilityAdjustment.getTaxableWages());
        assertEquals(new SpcfMoney("0.00"), liabilityAdjustment.getTotalWages());


        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("ADDMED-EE", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.AdditionalMedicareTaxDebitNotification)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 18, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathWithWagesNoLaw() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 11, 8);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "200", new LedgerOperationCreator(company, "1.00").withWages("100.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(0, MoneyMovementTransaction.findTaxPayments().set941().setPending().find().size());
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"ADDMED-EE\",\"TEST_COMPANY_1\",\"2012-12-31\",\"100.00\",\"ERROR: Company Law 200 for company QBDT:TEST_0001 does not exist.\"\n",
                job.getProcessedFileString());

        assertEquals(0, PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)).size());
        assertEquals(0, CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction).size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLateCreditReductionGetsCombined() {
        //PSRV004136
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

        DataLoadServices.setPSPDate(2013, 1, 25);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("265.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"FUTA\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 30, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("FUTA", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.FUTACreditReduction)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 31, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testHappyPathRateUpdate() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-UIETT-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2013-01-01", "87", LedgerOperationJobType.RateUpdate, new LedgerOperationCreator(company, "0.321", true));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"0.321\",\"Memo\",\"RateUpdate\",\"CA SUI-ER\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Y\",\"Success\"\n",
                job.getProcessedFileString());


        CompanyLawRate effectiveLawRate = CompanyLawRate.findEffectiveLawRate(CompanyLaw.findCompanyLaw(company, "87"), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(.321, effectiveLawRate.getRate());
    }

    @Test
    public void testHappyPathAdditionalFilingAmountUpdate() {
        String UHI_RATE = "MA Unemployment Health Insurance Rate";
        String MA_SUI_CREDIT = "MA SUI Credit";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 14);

        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2013-01-01", "AdditionalFilingAmountUpdate", LedgerOperationJobType.AdditionalFilingAmountUpdate, new LedgerOperationCreator(company, "0.321", UHI_RATE));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);

        LedgerOperationJobDTO creditDTO = createJobDTO("2013-01-01", "AdditionalFilingAmountUpdate", LedgerOperationJobType.AdditionalFilingAmountUpdate, new LedgerOperationCreator(company, "888.12", MA_SUI_CREDIT));
        LedgerOperationJob creditJob = createAndQueue(creditDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(additionalRateJob);
        assertEquals(LedgerOperationJobStatus.Complete, additionalRateJob.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"0.321\",\"Memo\",\"AdditionalFilingAmountUpdate\",\"" + UHI_RATE + "\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                additionalRateJob.getProcessedFileString());

        Application.refresh(creditJob);
        assertEquals(LedgerOperationJobStatus.Complete, creditJob.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"888.12\",\"Memo\",\"AdditionalFilingAmountUpdate\",\"" + MA_SUI_CREDIT + "\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                creditJob.getProcessedFileString());

        CompanyAgencyPaymentTemplate maPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate("MA-1700HI-PAYMENT"));
        CompanyFilingAmount uhiRate = maPaymentTemplate.getCompanyFilingAmount(AdditionalFilingAmount.findByName(UHI_RATE), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(.321, uhiRate.getAmount());

        CompanyFilingAmount suiCredit = maPaymentTemplate.getCompanyFilingAmount(AdditionalFilingAmount.findByName(MA_SUI_CREDIT), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(888.12, suiCredit.getAmount());
    }

    @Test
    public void testHappyPathAdditionalFilingAmountUpdateNVBond() {
        String NV_BOND = "NV Bond Contribution Rate";
        String NV_SUI_CREDIT = "NV SUI Credit";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 14);

        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2013-01-01", "AdditionalFilingAmountUpdate", LedgerOperationJobType.AdditionalFilingAmountUpdate, new LedgerOperationCreator(company, "0.321", NV_BOND));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);

        LedgerOperationJobDTO creditDTO = createJobDTO("2013-01-01", "AdditionalFilingAmountUpdate", LedgerOperationJobType.AdditionalFilingAmountUpdate, new LedgerOperationCreator(company, "888.12", NV_SUI_CREDIT));
        LedgerOperationJob creditJob = createAndQueue(creditDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(additionalRateJob);
        assertEquals(LedgerOperationJobStatus.Complete, additionalRateJob.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"0.321\",\"Memo\",\"AdditionalFilingAmountUpdate\",\"" + NV_BOND + "\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                additionalRateJob.getProcessedFileString());

        Application.refresh(creditJob);
        assertEquals(LedgerOperationJobStatus.Complete, creditJob.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"888.12\",\"Memo\",\"AdditionalFilingAmountUpdate\",\"" + NV_SUI_CREDIT + "\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                creditJob.getProcessedFileString());

        CompanyAgencyPaymentTemplate maPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate("NV-NUCS4072-PAYMENT"));
        CompanyFilingAmount uhiRate = maPaymentTemplate.getCompanyFilingAmount(AdditionalFilingAmount.findByName(NV_BOND), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(.321, uhiRate.getAmount());

        CompanyFilingAmount suiCredit = maPaymentTemplate.getCompanyFilingAmount(AdditionalFilingAmount.findByName(NV_SUI_CREDIT), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(888.12, suiCredit.getAmount());
    }

    @Test
    public void testHappyPathAdditionalFilingAmountUpdateNVBondCredit() {
        testHappyPathAdditionalFilingAmountUpdate("NV-NUCS4072-PAYMENT", "NV Bond Credit", 20.0);
        testHappyPathAdditionalFilingAmountUpdate("WY-WYO056-PAYMENT", "WY SUI Credit", 20.0);
    }

    @Test
    public void testNVBulkDebitNewPayrollBackDateHoldFinalisedPayment() {
        String NV_SUI_CREDIT = "NV SUI Credit";
        Company company = setupNVBulkDebitCompanyData();
        DataLoadServices.setPSPDate(2016, 5, 24);
        DataLoadServices.runMMTJobs(1);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = DataLoadServices.getReadyToSendTaxPayments(company, "NV-NUCS4072-PAYMENT");
        Application.rollbackUnitOfWork();
        for (MoneyMovementTransaction mmt : mmts) {
            DataLoadServices.finalizePayment(mmt);
        }
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of finalised MMTS for NV", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());

        Application.rollbackUnitOfWork();
        Application.beginUnitOfWork();
        company = Application.refresh(company);
        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndNVStateTaxes(new PayrollRunDTO(), company, new DateDTO(2016, 5, 27), employees);
        Application.commitUnitOfWork();
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);
        DataLoadServices.setPSPDate(2016, 5, 27);
        Application.beginUnitOfWork();
        nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of OnHold MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());
        assertEquals("amount of OnHold MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("OnHold reason for MMT", PaymentOnHoldReason.BackDate, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getActiveOnHoldReasons().getFirst().getOnHoldReasonCd());
        Application.rollbackUnitOfWork();
        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2016-04-01", "9031", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "77.00", NV_SUI_CREDIT));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of OnHold MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());
        assertEquals("amount of OnHold MMTS for NV", SpcfMoney.createInstance("239.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("OnHold reason for MMT", PaymentOnHoldReason.BackDate, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getActiveOnHoldReasons().getFirst().getOnHoldReasonCd());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNVBondBackDateHoldOverridenFinalisedPayment() {
        String NV_SUI_CREDIT = "NV SUI Credit";
        Company company = setupNVBulkDebitCompanyData();
        DataLoadServices.setPSPDate(2016, 5, 24);
        DataLoadServices.runMMTJobs(1);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = DataLoadServices.getReadyToSendTaxPayments(company, "NV-NUCS4072-PAYMENT");
        Application.rollbackUnitOfWork();
        for (MoneyMovementTransaction mmt : mmts) {
            DataLoadServices.finalizePayment(mmt);
        }
        DataLoadServices.setPSPDate(2016, 5, 27);
        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2016-04-01", "9031", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "77.00", NV_SUI_CREDIT));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);
        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        DomainEntitySet<MoneyMovementTransaction> paPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-UC2-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("amount of finalised MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        assertEquals("amount of pending MMTS for NV", SpcfMoney.createInstance("77.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).getFirst().getMoneyMovementTransactionAmount());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNVBondBackDateHoldOverridenOnHoldPayment() {
        String NV_SUI_CREDIT = "NV SUI Credit";
        Company company = setupNVBulkDebitCompanyData();
        DataLoadServices.setPSPDate(2016, 5, 24);
        DataLoadServices.runMMTJobs(1);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = DataLoadServices.getReadyToSendTaxPayments(company, "NV-NUCS4072-PAYMENT");
        for (MoneyMovementTransaction mmt : mmts) {
            mmt.addTaxPaymentOnHoldReason(PaymentOnHoldReason.Agent);
        }
        Application.commitUnitOfWork();
        DataLoadServices.setPSPDate(2016, 5, 27);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of MMTS for NV", 1, nvPayments.size());
        assertEquals("Count of finalised MMTS for NV", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("Count of Onhold MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());
        assertEquals("amount of Onhold MMTS for NV", SpcfMoney.createInstance("162.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getMoneyMovementTransactionAmount());
        Application.rollbackUnitOfWork();
        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2016-04-01", "9031", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "77.00", NV_SUI_CREDIT));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);
        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("Count of onhold MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());
        assertEquals("amount of onhold MMTS for NV", SpcfMoney.createInstance("239.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getMoneyMovementTransactionAmount());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNVBondBackDateHoldOverridenPendingPayment() {
        String NV_SUI_CREDIT = "NV SUI Credit";
        Company company = setupNVBulkDebitCompanyData();
        DataLoadServices.setPSPDate(2016, 5, 24);
        DataLoadServices.runMMTJobs(1);
        DataLoadServices.setPSPDate(2016, 5, 27);
        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2016-04-01", "9031", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "77.00", NV_SUI_CREDIT));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);
        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("Count of pending MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        assertEquals("amount of pending MMTS for NV", SpcfMoney.createInstance("239.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).getFirst().getMoneyMovementTransactionAmount());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNVBondBackDateHoldOverridenNoPendingPayment() {
        String NV_SUI_CREDIT = "NV SUI Credit";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateAgencyTaxpayerId(company, "NV-NUCS4072-PAYMENT", "011345678");
        DataLoadServices.setPSPDate(2013, 1, 10);
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Application.commitUnitOfWork();
        DataLoadServices.setPSPDate(2016, 5, 27);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        assertEquals("Count of finalised MMTS for NV", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        Application.rollbackUnitOfWork();
        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2016-04-01", "9031", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "77.00", NV_SUI_CREDIT));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);
        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());

        assertEquals("Count of finalised MMTS for NV", 0, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("Count of pending MMTS for NV", 1, nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        assertEquals("amount of pending MMTS for NV", SpcfMoney.createInstance("77.00"), nvPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).getFirst().getMoneyMovementTransactionAmount());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNVAndOthersBackDateHoldOverridenFinalisedPayment() {
        String NV_SUI_CREDIT = "NV SUI Credit";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-UC2-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateAgencyTaxpayerId(company, "NV-NUCS4072-PAYMENT", "011345678");
        DataLoadServices.setPSPDate(2013, 1, 10);
        DataLoadServices.addCompanyLaws(company, "40", "121", "137", "102", "150");
        DataLoadServices.updateAgencyTaxpayerId(company, "PA-UC2-PAYMENT", "1224567");
        DataLoadServices.updateACHAgentEnabledFlags(company, "PA-UC2-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company, "MA-1700HI-PAYMENT", "12245671");
        DataLoadServices.updateACHAgentEnabledFlags(company, "MA-1700HI-PAYMENT", true);
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
        DataLoadServices.setPSPDate(2016, 5, 18);
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);
        DataLoadServices.runMMTJobs(1);
        DataLoadServices.setPSPDate(2016, 5, 24);
        DataLoadServices.runMMTJobs(1);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = DataLoadServices.getReadyToSendTaxPayments(company, "NV-NUCS4072-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> mmts2 = DataLoadServices.getReadyToSendTaxPayments(company, "PA-UC2-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> mmts3 = DataLoadServices.getReadyToSendTaxPayments(company, "MA-1700HI-PAYMENT");
        mmts.addAll(mmts2);
        mmts.addAll(mmts3);
        Application.rollbackUnitOfWork();
        for (MoneyMovementTransaction mmt : mmts) {
            DataLoadServices.finalizePayment(mmt);
        }

        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        DomainEntitySet<MoneyMovementTransaction> paPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-UC2-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        DomainEntitySet<MoneyMovementTransaction> maPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MA-1700HI-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
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


        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2016-04-01", "9031", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "77.00", NV_SUI_CREDIT));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);
        additionalRateJobDTO = createJobDTO("2016-04-01", "9012", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "88.00", "PA SUI Credit"));
        additionalRateJob = createAndQueue(additionalRateJobDTO);
        additionalRateJobDTO = createJobDTO("2016-04-01", "9000", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "55.00", "MA SUI Credit"));
        additionalRateJob = createAndQueue(additionalRateJobDTO);
        additionalRateJobDTO = createJobDTO("2016-04-01", "9001", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "66.00", "MA UHI Credit"));
        additionalRateJob = createAndQueue(additionalRateJobDTO);
        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        paPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-UC2-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        maPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MA-1700HI-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
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
        assertEquals("amount of onhold MMTS for MA", SpcfMoney.createInstance("121.00"), maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("amount of onhold reason for MA", PaymentOnHoldReason.BackDate, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).getFirst().getActiveOnHoldReasons().getFirst().getOnHoldReasonCd());
        assertEquals("Count of pending MMTS for NV", 0, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNVMABulkDebitHoldOverridenFinalisedPayment() {
        String NV_SUI_CREDIT = "NV SUI Credit";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-UC2-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT, "NV-NUCS4072-PAYMENT,MA-1700HI-PAYMENT");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateAgencyTaxpayerId(company, "NV-NUCS4072-PAYMENT", "011345678");
        DataLoadServices.setPSPDate(2013, 1, 10);
        DataLoadServices.addCompanyLaws(company, "40", "121", "137", "102", "150");
        DataLoadServices.updateAgencyTaxpayerId(company, "PA-UC2-PAYMENT", "1224567");
        DataLoadServices.updateACHAgentEnabledFlags(company, "PA-UC2-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company, "MA-1700HI-PAYMENT", "12245671");
        DataLoadServices.updateACHAgentEnabledFlags(company, "MA-1700HI-PAYMENT", true);
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
        DataLoadServices.setPSPDate(2016, 5, 18);
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);
        DataLoadServices.runMMTJobs(1);
        DataLoadServices.setPSPDate(2016, 5, 24);
        DataLoadServices.runMMTJobs(1);
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = DataLoadServices.getReadyToSendTaxPayments(company, "NV-NUCS4072-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> mmts2 = DataLoadServices.getReadyToSendTaxPayments(company, "PA-UC2-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> mmts3 = DataLoadServices.getReadyToSendTaxPayments(company, "MA-1700HI-PAYMENT");
        mmts.addAll(mmts2);
        mmts.addAll(mmts3);
        Application.rollbackUnitOfWork();
        for (MoneyMovementTransaction mmt : mmts) {
            DataLoadServices.finalizePayment(mmt);
        }

        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        DomainEntitySet<MoneyMovementTransaction> paPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-UC2-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        DomainEntitySet<MoneyMovementTransaction> maPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MA-1700HI-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
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


        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2016-04-01", "9031", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "77.00", NV_SUI_CREDIT));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);
        additionalRateJobDTO = createJobDTO("2016-04-01", "9012", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "88.00", "PA SUI Credit"));
        additionalRateJob = createAndQueue(additionalRateJobDTO);
        additionalRateJobDTO = createJobDTO("2016-04-01", "9000", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "55.00", "MA SUI Credit"));
        additionalRateJob = createAndQueue(additionalRateJobDTO);
        additionalRateJobDTO = createJobDTO("2016-04-01", "9001", LedgerOperationJobType.BulkDebit, new LedgerOperationCreator(company, "66.00", "MA UHI Credit"));
        additionalRateJob = createAndQueue(additionalRateJobDTO);
        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        Application.beginUnitOfWork();
        nvPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NV-NUCS4072-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        paPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-UC2-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
        maPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MA-1700HI-PAYMENT").setNonDirect().setPendingOrFinalized().find().sort(MoneyMovementTransaction.InitiationDate());
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
        assertEquals("Count of pending MMTS for MA", 1, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).size());
        assertEquals("amount of onhold MMTS for MA", SpcfMoney.createInstance("121.00"), maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend)).getFirst().getMoneyMovementTransactionAmount());
        assertEquals("Count of pending MMTS for NV", 0, maPayments.find(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.OnHold)).size());
        Application.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT, "NV-NUCS4072-PAYMENT");
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSystemParamForBulkDebitPaymentTemplates() {
        List<String> pts = ProcessLedgerOperations.getListOfPaymentTemplatesForOverridingBackDateHold();
        assertEquals("Default values for OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT", 1, pts.size());
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT, "NV-NUCS4072-PAYMENT,MA-1700HI-PAYMENT");
        PayrollServices.commitUnitOfWork();
        pts = ProcessLedgerOperations.getListOfPaymentTemplatesForOverridingBackDateHold();
        assertEquals("Default values for OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT", 2, pts.size());
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT, "NV-NUCS4072-PAYMENT,MA-1700HI-PAYMENT,PA-UC2-PAYMENT");
        PayrollServices.commitUnitOfWork();
        pts = ProcessLedgerOperations.getListOfPaymentTemplatesForOverridingBackDateHold();
        assertEquals("Default values for OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT", 3, pts.size());
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT, "NV-NUCS4072-PAYMENT");
        PayrollServices.commitUnitOfWork();

    }

    private void testHappyPathAdditionalFilingAmountUpdate(String paymentTemplateName, String additionalFilingAmountName, Double amtOrRate) {
        runBeforeEachTest();

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateName, SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 14);

        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2013-01-01", "AdditionalFilingAmountUpdate", LedgerOperationJobType.AdditionalFilingAmountUpdate, new LedgerOperationCreator(company, amtOrRate.toString(), additionalFilingAmountName));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(additionalRateJob);
        assertEquals(LedgerOperationJobStatus.Complete, additionalRateJob.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"" + amtOrRate.toString() + "\",\"Memo\",\"AdditionalFilingAmountUpdate\",\"" + additionalFilingAmountName + "\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                additionalRateJob.getProcessedFileString());

        Application.refresh(additionalRateJob);
        assertEquals(LedgerOperationJobStatus.Complete, additionalRateJob.getStatus());

        CompanyAgencyPaymentTemplate maPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(paymentTemplateName));
        CompanyFilingAmount creditAmt = maPaymentTemplate.getCompanyFilingAmount(AdditionalFilingAmount.findByName(additionalFilingAmountName), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        //assertEquals(amtOrRate, creditAmt.getAmount());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testHappyPathAdditionalFilingAmountUpdateMAEmac() {
        String EMAC_RATE = "MA Er Medical Assistance Contribution";
        String MA_SUI_CREDIT = "MA SUI Credit";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 14);

        LedgerOperationJobDTO additionalRateJobDTO = createJobDTO("2013-01-01", "AdditionalFilingAmountUpdate", LedgerOperationJobType.AdditionalFilingAmountUpdate, new LedgerOperationCreator(company, "0.321", EMAC_RATE));
        LedgerOperationJob additionalRateJob = createAndQueue(additionalRateJobDTO);

        LedgerOperationJobDTO creditDTO = createJobDTO("2013-01-01", "AdditionalFilingAmountUpdate", LedgerOperationJobType.AdditionalFilingAmountUpdate, new LedgerOperationCreator(company, "888.12", MA_SUI_CREDIT));
        LedgerOperationJob creditJob = createAndQueue(creditDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(additionalRateJob);
        assertEquals(LedgerOperationJobStatus.Complete, additionalRateJob.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"0.321\",\"Memo\",\"AdditionalFilingAmountUpdate\",\"" + EMAC_RATE + "\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                additionalRateJob.getProcessedFileString());

        Application.refresh(creditJob);
        assertEquals(LedgerOperationJobStatus.Complete, creditJob.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"888.12\",\"Memo\",\"AdditionalFilingAmountUpdate\",\"" + MA_SUI_CREDIT + "\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                creditJob.getProcessedFileString());

        CompanyAgencyPaymentTemplate maPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate("MA-1700HI-PAYMENT"));
        CompanyFilingAmount uhiRate = maPaymentTemplate.getCompanyFilingAmount(AdditionalFilingAmount.findByName(EMAC_RATE), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(.321, uhiRate.getAmount());

        CompanyFilingAmount suiCredit = maPaymentTemplate.getCompanyFilingAmount(AdditionalFilingAmount.findByName(MA_SUI_CREDIT), SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(888.12, suiCredit.getAmount());
    }


    @Test
    public void testMASUICreditCreatesUHIEmail() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MA-1700HI-PAYMENT").find());
        assertEquals(new SpcfMoney("1888.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "102", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("1889.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"MA SUI-ER\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 17, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("MA SUI-ER", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.SameDayMAUHIDebitNotification)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 18, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));

        assertEquals(1, FinancialTransaction.findFinancialTransactionCountFromLedgerOperations(company));

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNYSUIBulkDebitCreatesEmail() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-45MN-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NY-45MN-PAYMENT").find());
        assertEquals(new SpcfMoney("1076.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "117", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("1077.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"NY SUI-ER\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 17, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("NY SUI-ER", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.BulkCreditDebitNotification)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 18, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testMISUIBulkDebitCreatesEmail() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MI-UIA1020-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MI-UIA1020-PAYMENT").find());
        assertEquals(new SpcfMoney("1224.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "105", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("1225.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"MI SUI-ER\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 17, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("MI SUI-ER", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.BulkCreditDebitNotification)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 18, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testWASUIBulkDebitCreatesEmail() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WA-F5208-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<Individual> individuals= Application.find(Individual.class);
        System.out.printf("Individual size"+ individuals.size());

        for(Individual ind : individuals) {
            ind.setEmail("vipinahirwar805+iamtestpass@gmail.com");
        }
        //company.setNotificationEmail("ayaanmohd28+iamtestpass@gmail.com");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("WA-F5208-PAYMENT").find());
        assertEquals(new SpcfMoney("1180.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "131", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("1181.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"WA SUI-ER\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 17, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("WA SUI-ER", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.BulkCreditDebitNotification)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 18, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testSUPNYMCTMTBulkDebitCreatesEmail() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 2);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NY-MTA305-PAYMENT").find());
        assertEquals(new SpcfMoney("1692.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 15);
        LedgerOperationJobDTO jobDTO = createJobDTO("2013-01-15", "197", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("1692.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"SUP-NY MCTMT\",\"TEST_COMPANY_1\",\"2013-01-15\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 18, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("SUP-NY MCTMT", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2013/01/15 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.BulkCreditDebitNotificationSUPNY)));
        assertEquals("1", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 22, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testMISUIBulkCredit() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MI-UIA1020-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        BatchJobManager.runJob(BatchJobType.EftpsPayment);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MI-UIA1020-PAYMENT").find());
        assertEquals(new SpcfMoney("1224.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "105", new LedgerOperationCreator(company, "-1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("1223.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"-1.00\",\"Memo\",\"BulkDebit\",\"MI SUI-ER\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Complete)));
        FinancialTransaction atc = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.AgencyTaxDebit));
        assertEquals(new SpcfMoney("1.00"), atc.getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("-1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("MI SUI-ER", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        DomainEntitySet<CompanyEventEmail> companyEventEmailSet = companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.BulkCreditDebitNotification));
        assertEquals(0, companyEventEmailSet.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testMOSUICreditCreatesEmail() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MO-MODES-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MO-MODES-PAYMENT").find());
        assertEquals(new SpcfMoney("428.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "107", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("429.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"MO SUI-ER\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 17, SpcfTimeZone.getLocalTimeZone()), impound.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("MO SUI-ER", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.SameDayMoFedAssessmentDebit)));
        assertEquals("4", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Quarter));
        assertEquals("2012", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Year));
        assertEquals("A law", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.LawId));
        assertEquals("1.00", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.Amount));
        assertEquals("January 18, 2013", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.DebitSettlementDate));
        assertEquals("Payrolladmin", companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminLastName));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAppliesFromATR() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 11, 1);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 12, 21);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940()); //offload early so ATR

        DataLoadServices.setPSPDate(2013, 1, 11);
        voidAPaycheck(payrollRun);

        PayrollServices.beginUnitOfWork();
        assertEquals(0, MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().setReadyToSend().find().size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().setReadyToSend().find());
        assertEquals(new SpcfMoney("0.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(new SpcfMoney("1.00"), assertOne(payment.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit))).getFinancialTransactionAmount());
        assertEquals(new SpcfMoney("1.00"), assertOne(payment.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit))).getFinancialTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"FUTA\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("0.00"), impound.getFinancialTransactionAmount());
        assertEquals(new SpcfMoney("1.00"), assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxOverpaymentApplied)).getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CreditReduction));
        assertEquals("1.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("FUTA", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Law));
        assertEquals("2012/12/31 08:00:00.0", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate));
        assertTrue(StringUtils.isNotEmpty(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId)));

        assertEquals("No emails for no debit", 0, companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.FUTACreditReduction)).size());

        PayrollServices.rollbackUnitOfWork();

    }

    public void voidAPaycheck(PayrollRun payrollRun) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        voidPaychecks.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testCompanyNotFound() {
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66", new LedgerOperationCreator(null, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(
                "\"QBDT\",\"PSID_X\",\"1.00\",\"Memo\",\"BulkDebit\",\"FUTA\",\"My Legal Name\",\"2012-12-31\",\"ERROR: Company QBDT:PSID_X does not exist.\"\n",
                job.getProcessedFileString());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCancelledAndTermedCompaniesDoNotProcess() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company cancelledCompany = DataLoadPalette.setupTaxCompany();
        Company termedCompany = DataLoadPalette.setupTaxCompany();

        DataLoadServices.cancelService(cancelledCompany, ServiceCode.Tax);
        DataLoadServices.cancelService(termedCompany, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.terminateService(termedCompany.getSourceSystemCd(), termedCompany.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 12);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66",
                                                    new LedgerOperationCreator(cancelledCompany, "1.00"),
                                                    new LedgerOperationCreator(termedCompany, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals("\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"FUTA\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n" +
                             "\"QBDT\",\"TEST_0002\",\"1.00\",\"Memo\",\"BulkDebit\",\"FUTA\",\"TEST_COMPANY_2\",\"2012-12-31\",\"Success\"\n",
                     job.getProcessedFileString());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testEOQVJobProcess_happyPath() {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction  mmt = Application.findById(MoneyMovementTransaction.class,mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        PayrollServices.rollbackUnitOfWork();

        LedgerOperationJobDTO jobDTO = createJobDTOForEOQV("2011-01-03", "85",
                new LedgerOperationCreator(company, "11.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals("\"QBDT\",\"158905\",\"11.00\",\"Memo\",\"EOQV\",\"AR SUI-ER\",\"TEST_COMPANY_1\",\"2011-01-03\",\"Success\"\n",
                job.getProcessedFileString());
        PayrollServices.rollbackUnitOfWork();

        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                        TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(company)));
        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxReceivable variance transaction created: ", TransactionTypeCode.EmployerSUITaxReceivable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxReceivable amount: ", "11.00", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-11.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        new SUIRatePaymentsCleanUp().process(null, true, SpcfCalendar.createInstance(2011, 01, 01), SpcfCalendar.createInstance(2011, 03, 31), SpcfCalendar.createInstance(2011, 01, 01),
                SpcfCalendar.createInstance(2011, 01, 01), null);

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone());
        new EoqSUITaxAdjustments().process(processingDate, null, true);

        // Check EOQ Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxCollection)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));
        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of eoq adjustment transactions created: ", 1, financialTransactions.size());
        ft = financialTransactions.get(0);
        assertEquals(3, ft.getFinancialTransactionStates().size());
        assertNotNull(ft.getFinancialTransactionStateByTransactionState(TransactionState.findTransactionState(TransactionStateCode.Created)));
        assertNotNull(ft.getFinancialTransactionStateByTransactionState(TransactionState.findTransactionState(TransactionStateCode.Executed)));
        assertNotNull(ft.getFinancialTransactionStateByTransactionState(TransactionState.findTransactionState(TransactionStateCode.Completed)));
        assertEquals("EmployerSUITaxCollection  transaction created: ", TransactionTypeCode.EmployerSUITaxCollection, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxCollection amount: ", "11.00", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollRun payrollRun = ft.getPayrollRun();
        assertEquals("period end date", SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), payrollRun.getPaycheckDate().toLocal());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.SourceId().equalTo(beginningTransactionId + ""));
        assertEquals("Liability check not created for debit", 1, liabilityChecks.size());
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertEquals("Token did not increment", beginningToken + 1, company.getCurrentToken());
        assertEquals("Next transaction id did not increment", beginningTransactionId + 1, Long.parseLong(company.getNextPayrollTransactionId()));
        LiabilityCheck liabilityCheck = liabilityChecks.get(0);
        assertEquals(beginningTransactionId, Long.parseLong(liabilityCheck.getSourceId()));
        assertEquals(beginningToken + 1, liabilityCheck.getQbdtTransactionInfo().getToken());
        assertEquals(SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), liabilityCheck.getPeriodEndDate().toLocal());
        assertEquals(1, liabilityCheck.getLiabilityCheckLineCollection().size());
        SpcfDecimal total = SpcfMoney.ZERO;
        for (LiabilityCheckLine liabilityCheckLine : liabilityCheck.getLiabilityCheckLineCollection()) {
            assertNotNull(liabilityCheckLine.getQbdtTransactionInfo().getAccountName());
            Assert.assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEOQVJobProcess_IRSLaws() {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        LedgerOperationJobDTO jobDTO = createJobDTOForEOQV("2011-01-03", "66",
                new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals("\"QBDT\",\"158905\",\"1.00\",\"Memo\",\"EOQV\",\"FUTA\",\"TEST_COMPANY_1\",\"2011-01-03\",\"ERROR: IRS laws are not allowed for EOQV\"\n",
                job.getProcessedFileString());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEOQVJobProcess_LimitExceeding() {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        LedgerOperationJobDTO jobDTO = createJobDTOForEOQV("2011-01-03", "66",
                new LedgerOperationCreator(company, "1100.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals("\"QBDT\",\"158905\",\"1100.00\",\"Memo\",\"EOQV\",\"FUTA\",\"TEST_COMPANY_1\",\"2011-01-03\",\"ERROR: The payment amount you have entered is higher than the permissible limit. Enter an amount less than $500 to create the entry.\"\n",
                job.getProcessedFileString());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testEOQVJobProcess_FinilizedMmtDoesNotExits() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        LedgerOperationJobDTO jobDTO = createJobDTOForEOQV("2011-01-03", "85",
                new LedgerOperationCreator(companies.get(0), "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals("\"QBDT\",\"158905\",\"1.00\",\"Memo\",\"EOQV\",\"AR SUI-ER\",\"TEST_COMPANY_1\",\"2011-01-03\",\"ERROR: No ATFFinalized MoneyMovementTransaction found\"\n",
                job.getProcessedFileString());
        PayrollServices.rollbackUnitOfWork();
    }

    private SpcfUniqueId setupCompanyAndTaxPayment() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        // Finalize AR-209B-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        PayrollServices.rollbackUnitOfWork();
        return mmtId;
    }


    @Test
    public void testOnHoldCompanyCreatesWarningAndPutsPayrollOnHold() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find());
        assertEquals(new SpcfMoney("264.00"), payment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 11, 20);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);

        DataLoadServices.setPSPDate(2013, 1, 12);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66", new LedgerOperationCreator(company, "1.00"));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        Application.refresh(payment);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(new SpcfMoney("265.00"), payment.getMoneyMovementTransactionAmount());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"1.00\",\"Memo\",\"BulkDebit\",\"FUTA\",\"TEST_COMPANY_1\",\"2012-12-31\",\"WARNING: Company is on hold\"\n",
                job.getProcessedFileString());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));
        FinancialTransaction impound = assertOne(adjustmentPR.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("1.00"), impound.getFinancialTransactionAmount());
        assertTrue(impound.getOnHold());
        assertEquals(TaxPaymentStatus.OnHold, payment.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();
    }



    @Test
    @Ignore("Slow test")
    public void testLargeFile() {
        List<LedgerOperationCreator> creatorList = new ArrayList<LedgerOperationCreator>(100);
        for (int i = 0; i < 100; i++) {
            Company company = DataLoadPalette.setupTaxCompany();
            creatorList.add(new LedgerOperationCreator(company, "1.00"));
        }
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66", creatorList.toArray(new LedgerOperationCreator[100]));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTORHappyPath() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 11, 1);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 12, 21);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 1, 11);
        voidAPaycheck(payrollRun);

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "1", LedgerOperationJobType.TOR, new LedgerOperationCreator(company));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"502.00\",\"Memo\",\"TOR\",\"FWT\",\"TEST_COMPANY_1\",\"2012-12-31\",\"Success\"\n",
                job.getProcessedFileString());

        LedgerOperation ledgerOperation = assertOne(job.getLedgerOperationCollection());
        assertEquals(LedgerOperationStatus.Completed, ledgerOperation.getStatus());
        assertEquals(new SpcfMoney("502.00"), ledgerOperation.getAmount());

        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment)));
        assertEquals(PayrollStatus.Complete, adjustmentPR.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = adjustmentPR.getFinancialTransactionCollection().sort(FinancialTransaction.Law().LawId());
        assertEquals(5, financialTransactions.size());
        FinancialTransaction fitFT = financialTransactions.get(0);
        assertEquals(TransactionTypeCode.AgencyRefundTOR, fitFT.getTransactionType().getTransactionTypeCd());
        assertEquals(new SpcfMoney("2.00"), fitFT.getFinancialTransactionAmount());
        assertEquals("1", fitFT.getLaw().getLawId());
        assertEquals(TransactionStateCode.Completed, fitFT.getCurrentTransactionState().getTransactionStateCd());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()), fitFT.getSettlementDate().toLocal());
        assertEquals(SettlementType.ApplyForward, fitFT.getSettlementTypeCd());

        PayrollServices.rollbackUnitOfWork();

        //clear the rest of the ledger with some normal activities
        DataLoadServices.setPSPDate(2013, 1, 14);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());

        DataLoadServices.setPSPDate(2013, 1, 23);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-25"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 30);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        //and make sure we have nothing but fees
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 158.16),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 158.00),
                                              new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.16));
    }


    @Test
    public void testTorWithNoBalance() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 11, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 12, 21);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "1", LedgerOperationJobType.TOR, new LedgerOperationCreator(company));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"\",\"Memo\",\"TOR\",\"FWT\",\"TEST_COMPANY_1\",\"2012-12-31\",\"WARNING: There is no balance to TOR\"\n",
                job.getProcessedFileString());

        LedgerOperation ledgerOperation = assertOne(job.getLedgerOperationCollection());
        assertEquals(LedgerOperationStatus.Completed, ledgerOperation.getStatus());
        assertNull(ledgerOperation.getAmount());

        assertEquals(0, PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment)).size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathDepositFrequencyUpdate() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-UIETT-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addCompanyLawRates(company);

        DataLoadServices.setPSPDate(2013, 1, 14);
        LedgerOperationJobDTO jobDTO = createJobDTO("2013-01-01", "67", LedgerOperationJobType.DepositFrequencyUpdate, new LedgerOperationCreator(company, DepositFrequencyCode.valueOf("QUARTERLY")));
        LedgerOperationJob job = createAndQueue(jobDTO);

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Complete, job.getStatus());
        assertEquals(
                "\"QBDT\",\"TEST_0001\",\"QUARTERLY\",\"Memo\",\"DepositFrequencyUpdate\",\"CA SDI-EE\",\"TEST_COMPANY_1\",\"2013-01-01\",\"Success\"\n",
                job.getProcessedFileString());

        EffectiveDepositFrequency edf = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company,
                                                                                                      PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"),
                                                                                                      SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals(DepositFrequencyCode.QUARTERLY, edf.getPaymentTemplateFrequency().getPaymentFrequencyId());
    }

    public static LedgerOperationJobDTO   createJobDTO(String checkDate, String lawId, LedgerOperationCreator... ledgerOperations) {
        return createJobDTO(checkDate, lawId, LedgerOperationJobType.BulkDebit, ledgerOperations);
    }

    public static LedgerOperationJobDTO createJobDTOForEOQV(String checkDate, String lawId, LedgerOperationCreator... ledgerOperations) {
        return createJobDTO(checkDate, lawId, LedgerOperationJobType.EOQV, ledgerOperations);
    }

    public static LedgerOperationJobDTO createJobDTO(String checkDate, String lawId, LedgerOperationJobType type, LedgerOperationCreator... ledgerOperations) {
        LedgerOperationJobDTO jobDTO = new LedgerOperationJobDTO();
        jobDTO.setOriginalFile("Blah");
        jobDTO.setType(type);

        for (LedgerOperationCreator ledgerOperation : ledgerOperations) {
            LedgerOperationDTO operationDTO = new LedgerOperationDTO();
            if (ledgerOperation.amount != null) {
                operationDTO.setAmount(new SpcfMoney(ledgerOperation.amount));
            }
            if (ledgerOperation.wageAmount != null) {
                operationDTO.setTaxableWages(new SpcfMoney(ledgerOperation.wageAmount));
            }

            operationDTO.setCheckDate(new DateDTO(checkDate));
            if (ledgerOperation.additionalFilingName != null) {
                PayrollServices.beginUnitOfWork();
                operationDTO.setLawId(AdditionalFilingAmount.findByName(ledgerOperation.additionalFilingName).getPaymentTemplate().getLawCollection().sort(Law.LawId()).getFirst().getLawId());
                PayrollServices.rollbackUnitOfWork();
                operationDTO.setAdditionalAmountName(ledgerOperation.additionalFilingName);
            } else {
                operationDTO.setLawId(lawId);
            }

            operationDTO.setMemo("Memo");
            operationDTO.setOriginalLegalName(ledgerOperation.company != null ? ledgerOperation.company.getLegalName() : "My Legal Name");
            operationDTO.setSourceCompanyId(ledgerOperation.company != null ? ledgerOperation.company.getSourceCompanyId() : "PSID_X");
            operationDTO.setSourceSystemCd(SourceSystemCode.QBDT);

            operationDTO.setRate(ledgerOperation.amount == null ? 0. : Double.parseDouble(ledgerOperation.amount));
            operationDTO.setPushToQuickBooks(ledgerOperation.pushToQuickBooks == null ? false : ledgerOperation.pushToQuickBooks);
            operationDTO.setDepositFrequencyCode(ledgerOperation.depositFrequencyCode);
            jobDTO.getLedgerOperations().add(operationDTO);
        }

        return jobDTO;
    }

    public static LedgerOperationJob createAndQueue(LedgerOperationJobDTO jobDTO) {
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.addLedgerOperationJob(jobDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class, LedgerOperationJob.Status().equalTo(LedgerOperationJobStatus.Created)));
        assertSuccess(PayrollServices.batchJobManager.queueLedgerOperationJob(job.getId()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Queued, job.getStatus());
        PayrollServices.rollbackUnitOfWork();

        return job;
    }

    @Test
    public void testDeleteHappyPath() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        LedgerOperationJobDTO jobDTO = createJobDTO("2012-12-31", "66", new LedgerOperationCreator(company, "1.00"));
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find());

        assertSuccess(PayrollServices.batchJobManager.addLedgerOperationJob(jobDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class, LedgerOperationJob.Status().equalTo(LedgerOperationJobStatus.Created)));
        assertSuccess(PayrollServices.batchJobManager.deleteLedgerOperationJob(job.getId()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Deleted, job.getStatus());
        PayrollServices.rollbackUnitOfWork();

        BatchJobManager.runJob(BatchJobType.LedgerOperations);
        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        // the ledger batch job should not pick the deleted jobs
        assertEquals(LedgerOperationJobStatus.Deleted, job.getStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    public static class LedgerOperationCreator {
        public Company company;
        public String amount;
        public String wageAmount;

        public String additionalFilingName;
        public Boolean pushToQuickBooks;

        public DepositFrequencyCode depositFrequencyCode;

        public LedgerOperationCreator(Company pCompany, String pAmount) {
            company = pCompany;
            amount = pAmount;
        }

        public LedgerOperationCreator(Company pCompany) {
            company = pCompany;
        }

        public LedgerOperationCreator(Company pCompany, String pAmount, Boolean pPushToQuickBooks) {
            company = pCompany;
            amount = pAmount;
            pushToQuickBooks = pPushToQuickBooks;
        }

        public LedgerOperationCreator(Company pCompany, String pAmount, String pAdditionalFilingName) {
            company = pCompany;
            amount = pAmount;
            additionalFilingName = pAdditionalFilingName;
        }

        public LedgerOperationCreator(Company pCompany, DepositFrequencyCode pDepositFrequencyCode) {
            company = pCompany;
            depositFrequencyCode = pDepositFrequencyCode;
        }

        public LedgerOperationCreator withWages(String pWageAmount) {
            wageAmount = pWageAmount;
            return this;
        }

    }

    private Company setupNVBulkDebitCompanyData() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-UC2-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateAgencyTaxpayerId(company, "NV-NUCS4072-PAYMENT", "011345678");
        DataLoadServices.setPSPDate(2013, 1, 10);
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
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndNVStateTaxes(new PayrollRunDTO(), company, new DateDTO(2016, 5, 20), employees);
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
        DataLoadServices.setPSPDate(2016, 5, 18);
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);
        DataLoadServices.runMMTJobs(1);
        return company;
    }



}
