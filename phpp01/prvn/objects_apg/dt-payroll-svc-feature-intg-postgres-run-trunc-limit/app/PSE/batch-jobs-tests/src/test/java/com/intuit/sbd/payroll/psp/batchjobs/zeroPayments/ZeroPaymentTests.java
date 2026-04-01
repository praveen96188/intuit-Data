package com.intuit.sbd.payroll.psp.batchjobs.zeroPayments;

import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.batchjobs.zeropayments.ProcessZeroPayments;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Oct 31, 2011
 * Time: 1:33:31 PM
 */
public class ZeroPaymentTests {



    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    /**
     * Test  Zero Payment Canceled Company  - Last Tax Quarter = 0 - No Zero Payment Created
     */
    @Test
    public void testCanceledCompanyZeroLastTaxQuarterZeroPayment() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        // Cancel Tax Service
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(),
                company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        assertTrue(companyServicePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());

        PayrollServices.rollbackUnitOfWork();

    }


    /**
     * Test  Zero Payment Canceled Company  - Last Tax Quarter < Current Quarter - No Zero Payment Created
     */
    @Test
    public void testCanceledCompanyPastLastTaxQuarterZeroPayment() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        // Cancel Tax Service
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        TaxServiceInfoDTO taxServiceInfoDTO = new TaxServiceInfoDTO();
        taxServiceInfoDTO.setServiceCode(ServiceCode.Tax);
        taxServiceInfoDTO.setLastQuarterToFile(20113);

        assertSuccess(PayrollServices.companyManager.updateService(SourceSystemCode.QBDT, sourceCompanyId, taxServiceInfoDTO));
        ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(),
                company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        assertTrue(companyServicePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());

        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * Test  Zero Payment Canceled Company  - Last Tax Quarter  = Current Quarter - Zero Payment Created
     */
    @Test
    public void testCanceledCompanyCurrentLastTaxQuarterZeroPayment() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        // Cancel Tax Service
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        TaxServiceInfoDTO taxserviceInfoDTO = new TaxServiceInfoDTO();
        taxserviceInfoDTO.setServiceCode(ServiceCode.Tax);
        taxserviceInfoDTO.setLastQuarterToFile(20114);
        ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(),
                                                                                                            company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        assertSuccess(PayrollServices.companyManager.updateService(SourceSystemCode.QBDT, sourceCompanyId, taxserviceInfoDTO));
        assertTrue(companyServicePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());

        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * Test  Zero Payment Canceled Company  - Last Tax Quarter  = Current Quarter - Zero Payment Created
     */
    @Test
    public void testCanceledCompanyFutureLastTaxQuarterZeroPayment() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        // Cancel Tax Service
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        TaxServiceInfoDTO taxServiceInfoDTO = new TaxServiceInfoDTO();
        taxServiceInfoDTO.setServiceCode(ServiceCode.Tax);
        taxServiceInfoDTO.setLastQuarterToFile(20114);
        ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(),
                                                                                                            company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        assertSuccess(PayrollServices.companyManager.updateService(SourceSystemCode.QBDT, sourceCompanyId, taxServiceInfoDTO));
        assertTrue(companyServicePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());

        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * Test Zero Payment With Updated Deposit Frequency from MONTHLY to QUARTERLY
     */
    @Test
    public void testZeroPayment_WithUpdatedDepositFrequency() {
        String paymentTemplateCd = "WI-WT6-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"WI"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.SEMIMONTHLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20130101000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2013, 1, 1);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.SEMIMONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("MMT Due Date", "20130131", mmt.getDueDate().format("yyyyMMdd"));
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();

        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        dto.setAgencyId("WIDOR");
        SpcfCalendar newEffectiveDate = SpcfCalendar.createInstance(2013, 1, 1);
        dto.setEffectiveDate(newEffectiveDate);
        dto.setPaymentTemplateCd("WI-WT6-PAYMENT");
        dto.setPaymentFrequencyId(DepositFrequencyCode.QUARTERLY);



        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, sourceCompanyId, dto);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Update Effective Deposit Frequency", processResult);


        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2013, 1, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.SEMIMONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();


    }

    /**
     * Test MD's Zero Payment
     */
    @Test
    public void testMDZeroPayment() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();


    }

    /**
     * Test MD's Accelerated Zero Payment
     */
    @Test
    public void testMDAcceleratedZeroPayment() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.ACCELERATED, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();


    }

    /**
     * Test MD's Accelerated No Zero Payment
     */
    @Test
    public void testMDAcceleratedNoZeroPmtCreated() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.ACCELERATED, SpcfCalendar.createInstance(2011, 10, 1));
        DateDTO payrollDate = new DateDTO("2011-10-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        }

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();
    }




    /**
     * Test MD's Accelerated No Zero Payment
     */
    @Test
    public void testMDAccelerated_Monthly_ZeroPmtCreated() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 30));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.ACCELERATED, SpcfCalendar.createInstance(2011, 10, 1));

        DateDTO payrollDate = new DateDTO("2013-02-01");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        }

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20130202000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2013, 2, 2);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        PayrollServices.commitUnitOfWork();


    }

    /**
     * Test MD's Accelerated No Zero Payment
     */
    @Test
    public void testMDAcceleratedNoZeroPmtCreated_ExecutedMMT() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.ACCELERATED, SpcfCalendar.createInstance(2011, 10, 1));
        DateDTO payrollDate = new DateDTO("2011-10-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        }

        //Offload Debit and Payment
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 10, 11, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test OK's Semiweekly Zero Payment
     */
    @Test
    public void testOKSemiweeklyZeroPayment() {
        String paymentTemplateCd = "OK-OW9A-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"OK"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();


    }


    /**
     * Test OK's Semiweekly No Zero Payment
     */
    @Test
    public void testOKSemiweeklyNoZeroPayment() {
        String paymentTemplateCd = "OK-OW9A-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"OK"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 10, 1));

        DateDTO payrollDate = new DateDTO("2011-10-03");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        }
        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();


    }

    /**
     * Test OK's Semiweekly BackDate with Zero Payment Created
     */
    @Test
    public void testOKSemiweeklyBackDateWithZeroPayment() {
        String paymentTemplateCd = "OK-OW9A-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"OK"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        PayrollServices.commitUnitOfWork();

        // Submit Backdated Payroll

        DateDTO payrollDate = new DateDTO("2011-11-03");
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);


        //Run Zero Payment Again before Initiation Date
        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar thisMonth = previousMonth.copy();
        thisMonth.addMonths(1);
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(thisMonth));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();


    }

    /**
     * Test Zero Payment after Inactive Payment Template
     */
    @Test
    public void testZeroPaymentInactivePaymentTemplate() {
        String paymentTemplateCd = "OK-OW9A-PAYMENT";
        String lawId = "38";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"OK"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        PayrollServices.commitUnitOfWork();

        // Inactivate Payment Template

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company,lawId);
        companyLaw.setFilingStatus(PayrollItemStatus.Inactive);
        Application.save(companyLaw);
        PayrollServices.commitUnitOfWork();

        //Run Zero Payment Again before Initiation Date
        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());
        PayrollServices.commitUnitOfWork();


    }
    /**
     * Test HI's Zero Payment
     */
    @Test
    public void testHIZeroPayment() {
        String paymentTemplateCd = "HI-VP1-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"HI"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();


    }

    /**
     * Test IA MA NM  Zero Payment
     */
    @Ignore
    @Test

    public void testIA_MA_NM_ZeroPayment() {
        // TODO_MV - enable this test again after fixing TT for unsupported DFs
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"IA", "MA", "NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        String[] paymentTemplateList = new String[]{"IA-44105-PAYMENT", "MA-M941-PAYMENT", "NM-CRS1-PAYMENT"};
        for (String paymentTemplateCd : paymentTemplateList) {
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
            DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));
        }

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        for (String paymentTemplateCd : paymentTemplateList) {
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                    paymentTemplateCd,
                    DepositFrequencyCode.MONTHLY.toString(),
                    CalendarUtils.convertToRulesCalendar(processingDate));

            DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                    company,
                    paymentTemplate,
                    paymentPeriod);

            assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
            MoneyMovementTransaction mmt = mmts.get(0);
            assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
            assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        }
        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * Test HI KY  Zero Payment
     */
    @Ignore
    @Test
    public void testHI_KY_ZeroPayment() {
        // TODO_MV - enable this test again after fixing TT for unsupported DFs
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"HI", "KY"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        String[] paymentTemplateList = new String[]{"HI-VP1-PAYMENT", "KY-K1-PAYMENT"};
        for (String paymentTemplateCd : paymentTemplateList) {
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
            DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));
        }

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        //Force Check Payment
        PayrollServices.beginUnitOfWork();
        PaymentTemplate pt = PaymentTemplate.findPaymentTemplate("KY-K1-PAYMENT") ;
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companies.get(0), pt );
        capt.updateAgencyTaxpayerId(null);
        CompanyPaymentTemplatePaymentMethod caPM = capt.getCompanyPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit);
        caPM.setEnabled(false);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
         pt = PaymentTemplate.findPaymentTemplate("KY-K1-PAYMENT") ;
         capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companies.get(0), pt );
        capt.updateAgencyTaxpayerId("123456acd12");

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        for (String paymentTemplateCd : paymentTemplateList) {
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                    paymentTemplateCd,
                    DepositFrequencyCode.MONTHLY.toString(),
                    CalendarUtils.convertToRulesCalendar(processingDate));

            DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                    company,
                    paymentTemplate,
                    paymentPeriod);

            assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
            MoneyMovementTransaction mmt = mmts.get(0);
            assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
            assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        }
        PayrollServices.rollbackUnitOfWork();

    }


    /**
     * Test CO's Zero Payment
     * Payment Method - Check
     */
    @Test
    public void testCOZeroPayment() {
        String paymentTemplateCd = "CO-DR1094-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
         PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"CO"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));
        //Force Check Payment
        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companies.get(0), paymentTemplate );
        CompanyPaymentTemplatePaymentMethod caPM = capt.getCompanyPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit);
        caPM.setEnabled(false);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2011, 11, 9));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        assertEquals("Initiation Date not Changed", mmt.getOriginalInitiationDate(), mmt.getInitiationDate() );
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test NM  Zero Payment
     */

    @Test

    public void testNM_ZeroPayment() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        String[] paymentTemplateList = new String[]{"NM-CRS1-PAYMENT"};
        for (String paymentTemplateCd : paymentTemplateList) {
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
            DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 10, 1));
        }

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        for (String paymentTemplateCd : paymentTemplateList) {
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                    paymentTemplateCd,
                    DepositFrequencyCode.MONTHLY.toString(),
                    CalendarUtils.convertToRulesCalendar(processingDate));

            DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                    company,
                    paymentTemplate,
                    paymentPeriod);

            assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
            MoneyMovementTransaction mmt = mmts.get(0);
            assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
            assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
            assertEquals("Initiation Date not Changed", mmt.getOriginalInitiationDate(), mmt.getInitiationDate() );
        }
        PayrollServices.rollbackUnitOfWork();

        DateDTO payrollDate = new DateDTO("2011-11-20");

        for (Company company1 : companies) {
            DataLoadServices.runPayrollRun(company1, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        }

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        for (String paymentTemplateCd : paymentTemplateList) {
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                    paymentTemplateCd,
                    DepositFrequencyCode.MONTHLY.toString(),
                    CalendarUtils.convertToRulesCalendar(processingDate));

            DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                    company,
                    paymentTemplate,
                    paymentPeriod);

            assertEquals("Money Movement Transaction", 1, mmts.size());
            MoneyMovementTransaction mmt = mmts.get(0);
           // assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
           // assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
            assertEquals("Initiation Date not Changed", mmt.getOriginalInitiationDate(), mmt.getInitiationDate() );
        }
        PayrollServices.rollbackUnitOfWork();
    }


    /**
     * Test NM  Zero Payment
     */

    @Test
    public void testObsoleteDepositFrequency_ZeroPayment() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        String[] paymentTemplateList = new String[]{"NM-CRS1-PAYMENT"};
        for (String paymentTemplateCd : paymentTemplateList) {
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
            DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.ANNUAL, SpcfCalendar.createInstance(2011, 10, 1));
        }

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 11, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    //not a proper test since the exception occurs in in a thread which I can't seem to catch
    public void testCancelledMDCompanyDoesNotAttemptToDeleteExecutedPayments() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updatePaymentTemplateSupportedDate("MD-MW506-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 8, 1);

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2013, 8, 1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 8, 13);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("MD-MW506-PAYMENT"));

        DataLoadServices.setPSPDate(2013, 8, 14);
        DataLoadServices.cancelService(company, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2013, 8, 14));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMMTNotDeleted() {
        //PSP-3531
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updatePaymentTemplateSupportedDate("MI-MW106-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 8, 8);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-08-10"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "24");
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
        companyLawDTO.setFilingStatus(PayrollItemStatus.Inactive);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2013, 8, 20));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        FinancialTransaction miAtc = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.Law().LawId().equalTo("24")));
        assertNotNull(miAtc.getMoneyMovementTransaction());

        PayrollServices.rollbackUnitOfWork();

    }

    /*
    * Fix for PSP-4759
    * Test Scenario 1:
    * 15th Dec, 2013      2 companies created. (company1 and company2)
    * 3rd Jan, 2014     Run the ZeroPaymentsProcess  ==> Should generate 2 zero-MMTs (1 for each company)
    * 4th Jan, 2014     Run Payroll for only one company. (payroll run for company1)
    * 7th Feb, 2014     Run the ZeroPaymentsProcess with processingDate as 6th Jan, 2014.
    *                       ==>  zero-MMT for company1 becomes a non-Zero MMT as a result of payrollRun
    *                       ==>  1 zero-MMT of company2 remains the same.
    */
    @Ignore
    @Test
    public void testKSZeroPaymentForPreviousMonth(){

        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 12, 15));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"KS"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        String paymentTemplateCd = "KS-KW5-PAYMENT";
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2014, 1, 2));
        }

        Company company1 = companies.get(0);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2014, 1, 3);
        PSPDate.setPSPTime(processingDate);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        //======================================================================================================================================================
        //Assertions before Payroll Run

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company1,
                paymentTemplate,
                paymentPeriod);
        PayrollServices.commitUnitOfWork();

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("MMT Amount", "0.00", mmt.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt.getFinancialTransactionCollection().size());
        assertEquals("Initiation Date not Changed", mmt.getOriginalInitiationDate(), mmt.getInitiationDate() );

        PayrollServices.beginUnitOfWork();
        Company company2 = companies.get(1);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts2 = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company2,
                paymentTemplate,
                paymentPeriod);
        PayrollServices.commitUnitOfWork();

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts2.size());
        MoneyMovementTransaction mmt2 = mmts2.get(0);
        assertEquals("MMT Amount", "0.00", mmt2.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt2.getFinancialTransactionCollection().size());
        assertEquals("Initiation Date not Changed", mmt2.getOriginalInitiationDate(), mmt2.getInitiationDate() );
        //======================================================================================================================================================

        //Run payroll for company1
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 1, 4));
        PayrollServices.commitUnitOfWork();

        DateDTO payrollDate = new DateDTO("2014-1-4");
        DataLoadServices.runPayrollRun(company1, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        //======================================================================================================================================================
        //Run ZeroPaymentsProcess on 7/2/2014 for 6/1/2014

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 2, 7));
        processingDate = SpcfCalendar.createInstance(2014, 1, 6);
        PSPDate.setPSPTime(processingDate);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        //======================================================================================================================================================
        //Assertions
        //company1
        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts3 = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company1,
                paymentTemplate,
                paymentPeriod);
        PayrollServices.commitUnitOfWork();

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts3.size());
        MoneyMovementTransaction mmt3 = mmts3.get(0);
        assertEquals("MMT Amount", "36.00", mmt3.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 2, mmt3.getFinancialTransactionCollection().size());
        assertEquals("Initiation Date not Changed", mmt3.getOriginalInitiationDate(), mmt3.getInitiationDate() );


        //company2
        PayrollServices.beginUnitOfWork();
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts4 = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company2,
                paymentTemplate,
                paymentPeriod);
        PayrollServices.commitUnitOfWork();

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts4.size());
        MoneyMovementTransaction mmt4 = mmts4.get(0);
        assertEquals("MMT Amount", "0.00", mmt4.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt4.getFinancialTransactionCollection().size());
        assertEquals("Initiation Date not Changed", mmt4.getOriginalInitiationDate(), mmt4.getInitiationDate() );

    }


    /**
     * Fix for PSP-4759
     * Test Scenario 2
     * 5th Jan, 2014     Create 2 companies company1 and company2. Run payroll for company1.
     * 5th Jan, 2014     Run payroll for company1.
     * 7th Feb, 2014     Run the ZeroPaymentsProcess with processingDate as 6th Jan, 2014.
     *                   ==> Does not create a zero-MMT for company1 as there is already MMT created during payroll-Run
     *                   ==> Creates a zero-MMT for company2
     */
    @Ignore
    @Test
    public void testKSZeroPaymentForPreviousMonthScenario2(){
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 1, 5));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"KS"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());

        String paymentTemplateCd = "KS-KW5-PAYMENT";
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2014, 1, 5));
        }

        Company company1 = companies.get(0);
        Company company2 = companies.get(1);

        //Run payroll for company1
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 1, 5));
        PayrollServices.commitUnitOfWork();

        DateDTO payrollDate = new DateDTO("2014-1-5");
        DataLoadServices.runPayrollRun(company1, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        //Run ZeroPaymentsProcess on 7/2/2014 for 6/1/2014

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 2, 7));
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2014, 1, 6);
        PSPDate.setPSPTime(processingDate);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        //======================================================================================================================================================
        //Assertions
        //company1
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts3 = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company1,
                paymentTemplate,
                paymentPeriod);
        PayrollServices.commitUnitOfWork();

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts3.size());
        MoneyMovementTransaction mmt3 = mmts3.get(0);
        assertEquals("MMT Amount", "36.00", mmt3.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt3.getFinancialTransactionCollection().size());
        assertEquals("Initiation Date not Changed", mmt3.getOriginalInitiationDate(), mmt3.getInitiationDate() );


        //company2
        PayrollServices.beginUnitOfWork();
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts4 = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company2,
                paymentTemplate,
                paymentPeriod);
        PayrollServices.commitUnitOfWork();

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts4.size());
        MoneyMovementTransaction mmt4 = mmts4.get(0);
        assertEquals("MMT Amount", "0.00", mmt4.getMoneyMovementTransactionAmount().toString());
        assertEquals("Financial Transaction", 1, mmt4.getFinancialTransactionCollection().size());
        assertEquals("Initiation Date not Changed", mmt4.getOriginalInitiationDate(), mmt4.getInitiationDate());
    }

    @Test
    public void testMDZeroPayment_Executed() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 7));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 1, 1));

        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20110108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 1, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        SpcfCalendar previousMonth = processingDate.copy();
        previousMonth.addMonths(-1);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(previousMonth));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        // No zero payment should be created
        assertEquals("Zero Payment Money Movement Transaction", 0, mmts.size());

        paymentPeriod =  MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);


        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        Assert.assertEquals("Amount should be zero.", "0.00", mmts.get(0).getMoneyMovementTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processingDate = SpcfCalendar.createInstance(2011, 2, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        // No zero payment should be created
        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testMDZeroPayment_NullStartDate() {
        String paymentTemplateCd = "MD-MW506-PAYMENT";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 7));

        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        String sourceCompanyId = companies.get(0).getSourceCompanyId();
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCd, supportedDate);
        DataLoadServices.updateEffectiveDepositFreqEffDate(companies.get(0).getSourceCompanyId(), paymentTemplateCd, DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 1, 1));

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
        taxService.setServiceStartDate(null);
        Application.save(taxService);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        // Call Zero Payment Process
        PSPDate.setPSPTime("20110108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 1, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

        IPaymentPeriod paymentPeriod =  MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);


        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        Assert.assertEquals("Amount should be zero.", "0.00", mmts.get(0).getMoneyMovementTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processingDate = SpcfCalendar.createInstance(2011, 2, 8);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        // No zero payment should be created
        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        PayrollServices.rollbackUnitOfWork();

    }

}
