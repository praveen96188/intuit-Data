package com.intuit.sbd.payroll.psp.batchjobs.suicredits;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SUICreditsJobDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: dweinberg
 * Date: 9/25/13
 * Time: 10:52 AM
 */
public class ProcessSUICreditsTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testPartialSUIAmount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 55, "NV SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction nvPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NV-NUCS4072-PAYMENT").find());
        assertEquals(new SpcfMoney("1100.00"), nvPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("NV-NUCS4072-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(nvPayment);
        assertEquals(new SpcfMoney("1045.00"), nvPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(nvPayment, "NV SUI-ER", "55.00");
        assertEmployerTaxCreditAmount(company, "55.00");
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testExceedingSUI() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 555, "NV SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction nvPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NV-NUCS4072-PAYMENT").find());
        assertEquals(new SpcfMoney("1100.00"), nvPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("NV-NUCS4072-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(nvPayment);
        assertEquals(new SpcfMoney("545.00"), nvPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(nvPayment, "NV SUI-ER", "464.00");
        assertPaymentATD(nvPayment, "SUP-NV CEP", "91.00");
        assertEmployerTaxCreditAmount(company, "555.00");

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testFullAmount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 1100, "NV SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction nvPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NV-NUCS4072-PAYMENT").find());
        assertEquals(new SpcfMoney("1100.00"), nvPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("NV-NUCS4072-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(nvPayment);
        assertEquals(new SpcfMoney("0.00"), nvPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(nvPayment, "NV SUI-ER", "464.00");
        assertPaymentATD(nvPayment, "SUP-NV CEP", "636.00");
        assertEmployerTaxCreditAmount(company, "1100.00");

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testExceedingFullAmount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 1200, "NV SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction nvPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NV-NUCS4072-PAYMENT").find());
        assertEquals(new SpcfMoney("1100.00"), nvPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("NV-NUCS4072-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(nvPayment);
        assertEquals(new SpcfMoney("0.00"), nvPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(nvPayment, "NV SUI-ER", "464.00");
        assertPaymentATD(nvPayment, "SUP-NV CEP", "636.00");
        assertATO(company, "NV SUI-ER", "100.00");
        assertEmployerTaxCreditAmount(company, "1100.00");

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.SUICreditsApplied));
        assertEquals("1200.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("1100.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundAmount));
        assertEquals("NV-NUCS4072-PAYMENT", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        assertEquals("2013 Q1", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewDate));

        CompanyEventEmail email = assertOne(companyEvent.getCompanyEventEmailCollection());
        assertEquals(EventEmailTemplateTypeCode.SUICreditNotification, email.getEmailTemplateTypeCd());
        assertEquals("1200.00", email.getEmailParamValue(EventEmailParamTypeCode.SUICreditAmount));
        assertEquals("1100.00", email.getEmailParamValue(EventEmailParamTypeCode.Amount));

        PayrollServices.rollbackUnitOfWork();
    }
    

    @Test
    public void testWAExceedingFullAmount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WA-F5208-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 1200, "WA SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction nvPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WA-F5208-PAYMENT").find());
        assertEquals(new SpcfMoney("1180.00"), nvPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("WA-F5208-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(nvPayment);
        assertEquals(new SpcfMoney("0.00"), nvPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(nvPayment, "WA SUI-ER", "524.00");
        assertPaymentATD(nvPayment, "SUP-WA WTF", "656.00");
        assertATO(company, "WA SUI-ER", "20.00");
        assertEmployerTaxCreditAmount(company, "1180.0000");

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.SUICreditsApplied));
        assertEquals("1200.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("1180.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundAmount));
        assertEquals("WA-F5208-PAYMENT", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        assertEquals("2013 Q1", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewDate));

        CompanyEventEmail email = assertOne(companyEvent.getCompanyEventEmailCollection());
        assertEquals(EventEmailTemplateTypeCode.SUICreditNotification, email.getEmailTemplateTypeCd());
        assertEquals("1200.00", email.getEmailParamValue(EventEmailParamTypeCode.SUICreditAmount));
        assertEquals("1180.00", email.getEmailParamValue(EventEmailParamTypeCode.Amount));

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testAZExceedingFullAmount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-UC018-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 1200, "AZ SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction nvPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AZ-UC018-PAYMENT").find());
        assertEquals(new SpcfMoney("1060.00"), nvPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("AZ-UC018-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(nvPayment);
        assertEquals(new SpcfMoney("0.00"), nvPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(nvPayment, "AZ SUI-ER", "344.00");
        assertPaymentATD(nvPayment, "SUP-AZ TT", "716.00");
        assertATO(company, "AZ SUI-ER", "140.00");
        assertEmployerTaxCreditAmount(company, "1060.00");

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.SUICreditsApplied));
        assertEquals("1200.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("1060.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundAmount));
        assertEquals("AZ-UC018-PAYMENT", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        assertEquals("2013 Q1", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewDate));

        CompanyEventEmail email = assertOne(companyEvent.getCompanyEventEmailCollection());
        assertEquals(EventEmailTemplateTypeCode.SUICreditNotification, email.getEmailTemplateTypeCd());
        assertEquals("1200.00", email.getEmailParamValue(EventEmailParamTypeCode.SUICreditAmount));
        assertEquals("1060.00", email.getEmailParamValue(EventEmailParamTypeCode.Amount));

        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testSingleLawPartial() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WI-UCT101-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 55, "WI SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction wiPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WI-UCT101-PAYMENT").find());
        assertEquals(new SpcfMoney("528.00"), wiPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("WI-UCT101-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(wiPayment);
        assertEquals(new SpcfMoney("473.00"), wiPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(wiPayment, "WI SUI-ER", "55.00");
        assertEmployerTaxCreditAmount(company, "55.00");
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSingleLawExceeding() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WI-UCT101-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 628, "WI SUI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction wiPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WI-UCT101-PAYMENT").find());
        assertEquals(new SpcfMoney("528.00"), wiPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("WI-UCT101-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(wiPayment);
        assertEquals(new SpcfMoney("0.00"), wiPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(wiPayment, "WI SUI-ER", "528.00");
        assertATO(company, "WI SUI-ER", "100.00");
        assertEmployerTaxCreditAmount(company, "528.00");
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testMultipleCreditsPartial() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 55, "MA SUI Credit", 2013, 1);
        DataLoadServices.updateAdditionalFilingAmount(company, 88, "MA UHI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction maPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MA-1700HI-PAYMENT").find());
        assertEquals(new SpcfMoney("1888.00"), maPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("MA-1700HI-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(maPayment);
        assertEquals(new SpcfMoney("1745"), maPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(maPayment, "MA SUI-ER", "55.00");
        assertPaymentATD(maPayment, "MA SUI-ER", "88.00");
        assertEmployerTaxCreditAmount(company, "55.00");
        assertEmployerTaxCreditAmount(company, "88.00");
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testMultipleCreditsOnePartialOneExceeding() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 1688, "MA SUI Credit", 2013, 1);
        DataLoadServices.updateAdditionalFilingAmount(company, 500, "MA UHI Credit", 2013, 1);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction maPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MA-1700HI-PAYMENT").find());
        assertEquals(new SpcfMoney("1888.00"), maPayment.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("MA-1700HI-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        Application.refresh(maPayment);
        assertEquals(new SpcfMoney("0.00"), maPayment.getMoneyMovementTransactionAmount());
        assertPaymentATD(maPayment, "MA SUI-ER", "408.00");
        assertPaymentATD(maPayment, "SUP-MA WTF", "400.00");
        assertPaymentATD(maPayment, "SUP-MA WTF", "200.00");
        assertATO(company, "MA SUI-ER", "300.00");
        assertEmployerTaxCreditAmount(company, "1688.00");
        assertEmployerTaxCreditAmount(company, "200.00");
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNoPendingPayment()  {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WI-UCT101-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateAdditionalFilingAmount(company, 808, "WI SUI Credit", 2013, 1);

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("WI-UCT101-PAYMENT");
        new ProcessSUICredits().process();

        Application.beginUnitOfWork();
        assertATO(company, "WI SUI-ER", "808.00");

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.SUICreditsApplied));
        assertEquals("808.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("0.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundAmount));
        assertEquals("WI-UCT101-PAYMENT", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaymentTemplate));
        assertEquals("2013 Q1", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewDate));

        assertTrue(companyEvent.getCompanyEventEmailCollection().isEmpty());

        Application.rollbackUnitOfWork();
    }
    
    @Test
    public void testMultipleCreditsMultipleCompanies() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company1 = DataLoadPalette.setupTaxCompany("556638641", "8641", 2);
        Company company2 = DataLoadPalette.setupTaxCompany("556638642", "8642", 2);

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company1, new DateDTO(2013, 1, 10));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company1, 1688, "MA SUI Credit", 2013, 1);
        DataLoadServices.updateAdditionalFilingAmount(company1, 500, "MA UHI Credit", 2013, 1);
        
        DataLoadServices.updateAdditionalFilingAmount(company2, 1880, "MA SUI Credit", 2013, 1);
        DataLoadServices.updateAdditionalFilingAmount(company2, 200, "MA UHI Credit", 2013, 1);

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job("MA-1700HI-PAYMENT");
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        assertATO(company1, "MA SUI-ER", "300.00");
        assertEmployerTaxCreditAmount(company1, "1688.00");
        assertEmployerTaxCreditAmount(company1, "200.00");
        assertATO(company2, "MA SUI-ER", "192.00");
        assertEmployerTaxCreditAmount(company2, "1880.00");
        assertEmployerTaxCreditAmount(company2, "8.00");
        PayrollServices.rollbackUnitOfWork();
    }
    
    @Test
    public void testMultipleCreditsMultipleCompaniesNoPaymentTemplate() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company1 = DataLoadPalette.setupTaxCompany("556638641", "8641", 2);
        Company company2 = DataLoadPalette.setupTaxCompany("556638642", "8642", 2);

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company1, new DateDTO(2013, 1, 10));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company1, 1200, "NV SUI Credit", 2013, 1);
        
        DataLoadServices.updateAdditionalFilingAmount(company1, 1688, "MA SUI Credit", 2013, 1);
        DataLoadServices.updateAdditionalFilingAmount(company1, 500, "MA UHI Credit", 2013, 1);
        
        DataLoadServices.updateAdditionalFilingAmount(company2, 1880, "MA SUI Credit", 2013, 1);
        DataLoadServices.updateAdditionalFilingAmount(company2, 200, "MA UHI Credit", 2013, 1);

        DataLoadServices.setPSPDate(2013, 3, 20);
        createQ1Job(null);
        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();
        assertATO(company1, "MA SUI-ER", "300.00");
        assertEmployerTaxCreditAmount(company1, "1688.00");
        assertEmployerTaxCreditAmount(company1, "200.00");
        assertATO(company2, "MA SUI-ER", "192.00");
        assertEmployerTaxCreditAmount(company2, "1880.00");
        assertEmployerTaxCreditAmount(company2, "8.00");
        assertATO(company1, "NV SUI-ER", "100.00");
        assertEmployerTaxCreditAmount(company1, "1100.00");
        PayrollServices.rollbackUnitOfWork();
    }

    private void createQ1Job(String paymentTemplate) {
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, paymentTemplate)));
        PayrollServices.commitUnitOfWork();
    }

    private void assertPaymentATD(MoneyMovementTransaction payment, String lawTypeCd, String amount) {
        DomainEntitySet<FinancialTransaction> financialTransactions = payment.getFinancialTransactionCollection().find(
                FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit)
                                    .And(FinancialTransaction.Law().LawTypeCd().equalTo(lawTypeCd))
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed)));
        FinancialTransaction ft;
        if (financialTransactions.size() == 1) {
            ft = financialTransactions.getFirst();
        } else {
            ft = financialTransactions.findEntity(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney(amount)));
        }

        assertEquals(new SpcfMoney(amount), ft.getFinancialTransactionAmount());
    }

    private void assertATO(Company company, String lawTypeCd, String amount) {
        FinancialTransaction ft = company.getFinancialTransactions().findEntity(
                FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxOverpayment)
                                    .And(FinancialTransaction.Law().LawTypeCd().equalTo(lawTypeCd))
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed)));
        assertEquals(new SpcfMoney(amount), ft.getFinancialTransactionAmount());
    }

    private void assertEmployerTaxCreditAmount(Company company, String amount) {
        FinancialTransaction erTaxCredit = assertOne(FinancialTransaction.findFinancialTransaction(company, TransactionTypeCode.EmployerTaxCredit)
                                                                         .find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney(amount))));
        assertEquals(new SpcfMoney(amount), erTaxCredit.getFinancialTransactionAmount());
    }

}
