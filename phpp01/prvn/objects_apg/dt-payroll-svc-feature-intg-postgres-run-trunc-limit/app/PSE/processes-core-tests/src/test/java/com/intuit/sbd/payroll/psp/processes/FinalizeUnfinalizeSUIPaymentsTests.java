package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Feb 1, 2012
 * Time: 4:10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class FinalizeUnfinalizeSUIPaymentsTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void happyPath_FinalizeAndUnfinalize() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
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

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();


        // Finalize MMT Collection

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .setTaxPaymentStatuses(taxPaymentStatuses)
                        .find();
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Unfinalize AR-209B-PAYMENT

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.unfinalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ReadyToSend, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Unfinalize MMT Collection

        PayrollServices.beginUnitOfWork();
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .setTaxPaymentStatuses(taxPaymentStatuses)
                        .find();
        mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.unfinalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ReadyToSend, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testFinalizeUnfinalizeChangeSettlementDate() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"CT"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit);
        Company company = companies.get(0);

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        // First Payroll Run
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("CT-2MAG-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("CT-2MAG-PAYMENT", 1, moneyMovementTransactions.size());
        MoneyMovementTransaction firstMMT = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.InitiationDate().equalTo(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()))));
        //Finalize

        assertSuccess(PayrollServices.paymentManager.finalizeSUIPayments(Arrays.asList(firstMMT),
                null,
                2011,
                1));

        PayrollServices.commitUnitOfWork();

        // Unfinalize and update initiation date
        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("CT-2MAG-PAYMENT");
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("CT-2MAG-PAYMENT", 1, moneyMovementTransactions.size());
        firstMMT = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.InitiationDate().equalTo(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()))));
        assertSuccess(PayrollServices.paymentManager.unfinalizeSUIPayments(Arrays.asList(firstMMT),
                null,
                2011,
                1));

        assertSuccess(PayrollServices.paymentManager.updateInitiationDate(firstMMT.getId().toString(), SpcfCalendar.createInstance(2011, 1, 16, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();

        // Finalize

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("CT-2MAG-PAYMENT");
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("CT-2MAG-PAYMENT", 1, moneyMovementTransactions.size());
         firstMMT = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.InitiationDate().equalTo(SpcfCalendar.createInstance(2011, 1, 16, SpcfTimeZone.getLocalTimeZone()))));
        //Finalize

        assertSuccess(PayrollServices.paymentManager.finalizeSUIPayments(Arrays.asList(firstMMT),
                null,
                2011,
                1));

        PayrollServices.commitUnitOfWork();

        //Second Payroll Run

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("CT-2MAG-PAYMENT");
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).setOnHold().find());
    }

    @Test
    public void testPayrollSubmit_AfterATFFinalizedPayments() throws Exception {

        //Setting up company with SUI payment templates and Finalizing SUI payments
        Company company = setupCompanyWithATFFinalizedPayments();

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        String[] statesList = new String[]{"AR"};
        DateDTO payrollDate = new DateDTO("2011-01-10");

        //Submit payroll after finalizing first payroll SUI payments
        DataLoadServices.setPSPDate(2011, 1, 6);
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                                                                                                      .setPeriodBeginDate(quarterBeginDate)
                                                                                                      .setPeriodEndDate(quarterEndDate)
                                                                                                      .setTaxPaymentStatuses(taxPaymentStatuses)
                                                                                                      .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized AR-209B-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AR-209B-PAYMENT")
                                                                                                                       .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("460")))).size());
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.OnHold};
        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHDebit};
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                                                            .setPeriodBeginDate(quarterBeginDate)
                                                            .setPeriodEndDate(quarterEndDate)
                                                            .setTaxPaymentStatuses(taxPaymentStatuses)
                                                            .setPaymentMethods(paymentMethods)
                                                            .find();
        assertEquals("RTS SUI payments", 1, moneyMovementTransactions.size());
        assertEquals("RTS AR-209B-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AR-209B-PAYMENT")
                                                                                                                       .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("460")))).size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmit2PayrollAfterATFFinalizedPayments() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        //Setting up company with SUI payment templates and Finalizing SUI payments, submit another payroll after finalizing pending SUI payments
        testPayrollSubmit_AfterATFFinalizedPayments();

        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);

        String[] statesList = new String[]{"AR"};
        DateDTO payrollDate = new DateDTO("2011-01-10");

        //Submitting another payroll to validate this payroll payment will be combined to OnHold payments
        DataLoadServices.setPSPDate(2011, 1, 6);
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                                                                                                      .setPeriodBeginDate(quarterBeginDate)
                                                                                                      .setPeriodEndDate(quarterEndDate)
                                                                                                      .setTaxPaymentStatuses(taxPaymentStatuses)
                                                                                                      .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized AR-209B-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AR-209B-PAYMENT")
                                                                                                                       .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("460")))).size());
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.OnHold};
        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHDebit};
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                                                            .setPeriodBeginDate(quarterBeginDate)
                                                            .setPeriodEndDate(quarterEndDate)
                                                            .setTaxPaymentStatuses(taxPaymentStatuses)
                                                            .setPaymentMethods(paymentMethods)
                                                            .find();
        assertEquals("RTS SUI payments", 1, moneyMovementTransactions.size());
        assertEquals("RTS AR-209B-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AR-209B-PAYMENT")
                                                                                                              .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("920")))).size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoid_PayrollSubmittedAfterATFFinalizedPayments() throws Exception {
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        //Setting up company with SUI payment templates and Finalizing SUI payments, submit another payroll after finalizing pending SUI payments
        testPayrollSubmit_AfterATFFinalizedPayments();

        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);

        //Offload impound
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()));

        //Void Second payroll to make sure we update the amount to zero as they are not finalized
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), null));
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                                                                                                      .setPeriodBeginDate(quarterBeginDate)
                                                                                                      .setPeriodEndDate(quarterEndDate)
                                                                                                      .setTaxPaymentStatuses(taxPaymentStatuses)
                                                                                                      .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized AR-209B-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AR-209B-PAYMENT")
                                                                                                                       .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("460")))).size());
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHDebit};
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                                                            .setPeriodBeginDate(quarterBeginDate)
                                                            .setPeriodEndDate(quarterEndDate)
                                                            .setTaxPaymentStatuses(taxPaymentStatuses)
                                                            .setPaymentMethods(paymentMethods)
                                                            .find();
        assertEquals("Total RTS SUI payments", 1, moneyMovementTransactions.size());
        assertEquals("RTS SUI payments with zero amount", 1, moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO)).size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoid_PayrollWithATFFinalizedPayments() throws Exception {

        //Setting up company with SUI payment templates and Finalizing SUI payments
        Company company = setupCompanyWithATFFinalizedPayments();

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        //Void First payroll to validate ATFFinalized payments are not updated to zero
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                                                                                                      .setPeriodBeginDate(quarterBeginDate)
                                                                                                      .setPeriodEndDate(quarterEndDate)
                                                                                                      .setTaxPaymentStatuses(taxPaymentStatuses)
                                                                                                      .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized AR-209B-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AR-209B-PAYMENT")
                                                                                                                       .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("460")))).size());
        PayrollServices.commitUnitOfWork();
    }

    public static Company setupCompanyWithATFFinalizedPayments() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        //Offload impound
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        // Finalize MMT Collection
        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .setTaxPaymentStatuses(taxPaymentStatuses)
                                        .find();
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            junit.framework.Assert.assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        PayrollServices.rollbackUnitOfWork();
        return company;
    }

    @Test
    public void testFinalizeWithMMTsOnHoldAndAchDebit(){
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        //Run a payroll
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        //Return Payroll
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.AchRejectR1R9);

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        // MMT Collection put OnHold due to return of payroll
        PayrollServices.beginUnitOfWork();


        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);

        assertNotNull(mmts);
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            junit.framework.Assert.assertEquals("Tax Payment Status: ", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Finalize MMT Collection
        //MMTs in ReadyToSend state and in OnHold state with AchDebit PaymentMethod
        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .setTaxPaymentStatuses(taxPaymentStatuses)
                                        .find();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions1 =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .setTaxPaymentStatuses(new TaxPaymentStatus[]{TaxPaymentStatus.OnHold})
                                        .setPaymentMethods(new PaymentMethod[]{PaymentMethod.ACHDebit})
                                        .find();

        moneyMovementTransactions.addAll(moneyMovementTransactions1);
        mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            junit.framework.Assert.assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_FinalizeAndUnfinalizeForACHCredit() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MO"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        // Finalize AR-209B-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("MO-MODES-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("MO-MODES-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();


        // Finalize MMT Collection

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .setTaxPaymentStatuses(taxPaymentStatuses)
                        .find();
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Unfinalize AR-209B-PAYMENT

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("MO-MODES-PAYMENT");
        PayrollServices.paymentManager.unfinalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("MO-MODES-PAYMENT");
        quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ReadyToSend, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Unfinalize MMT Collection

        PayrollServices.beginUnitOfWork();
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .setTaxPaymentStatuses(taxPaymentStatuses)
                        .find();
        mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.unfinalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ReadyToSend, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    public void testPayrollSubmit_AfterATFFinalizedPaymentsForACHCredit() throws Exception {

        //Setting up company with SUI payment templates and Finalizing SUI payments
        Company company = setupCompanyWithATFFinalizedPaymentsForACHCredit();

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        String[] statesList = new String[]{"MO"};
        DateDTO payrollDate = new DateDTO("2011-01-10");

        //Submit payroll after finalizing first payroll SUI payments
        DataLoadServices.setPSPDate(2011, 1, 6);
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                .setPeriodBeginDate(quarterBeginDate)
                .setPeriodEndDate(quarterEndDate)
                .setTaxPaymentStatuses(taxPaymentStatuses)
                .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized MO-MODES-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("MO-MODES-PAYMENT")
                .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("214")))).size());
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.OnHold};
        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHCredit};
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                .setPeriodBeginDate(quarterBeginDate)
                .setPeriodEndDate(quarterEndDate)
                .setTaxPaymentStatuses(taxPaymentStatuses)
                .setPaymentMethods(paymentMethods)
                .find();
        assertEquals("RTS SUI payments", 1, moneyMovementTransactions.size());
        assertEquals("RTS MO-MODES-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("MO-MODES-PAYMENT")
                .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("214")))).size());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testSubmit2PayrollAfterATFFinalizedPaymentsForACHCredit() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        //Setting up company with SUI payment templates and Finalizing SUI payments, submit another payroll after finalizing pending SUI payments
        testPayrollSubmit_AfterATFFinalizedPaymentsForACHCredit();

        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);

        String[] statesList = new String[]{"MO"};
        DateDTO payrollDate = new DateDTO("2011-01-10");

        //Submitting another payroll to validate this payroll payment will be combined to OnHold payments
        DataLoadServices.setPSPDate(2011, 1, 6);
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                .setPeriodBeginDate(quarterBeginDate)
                .setPeriodEndDate(quarterEndDate)
                .setTaxPaymentStatuses(taxPaymentStatuses)
                .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized MO-MODES-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("MO-MODES-PAYMENT")
                .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("214")))).size());
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.OnHold};
        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHCredit};
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                .setPeriodBeginDate(quarterBeginDate)
                .setPeriodEndDate(quarterEndDate)
                .setTaxPaymentStatuses(taxPaymentStatuses)
                .setPaymentMethods(paymentMethods)
                .find();
        assertEquals("RTS SUI payments", 1, moneyMovementTransactions.size());
        assertEquals("RTS MO-MODES-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("MO-MODES-PAYMENT")
                .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("428")))).size());
        PayrollServices.rollbackUnitOfWork();
    }

    public static Company setupCompanyWithATFFinalizedPaymentsForACHCredit() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MO"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        //Offload impound
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        // Finalize MMT Collection
        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .setTaxPaymentStatuses(taxPaymentStatuses)
                        .find();
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            junit.framework.Assert.assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        PayrollServices.rollbackUnitOfWork();
        return company;
    }

    @Test
    public void testFinalizeWithMMTsOnHoldAndAchCredit(){
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MO"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        //Run a payroll
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        //Return Payroll
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.AchRejectR1R9);

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        // MMT Collection put OnHold due to return of payroll
        PayrollServices.beginUnitOfWork();


        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);

        assertNotNull(mmts);
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            junit.framework.Assert.assertEquals("Tax Payment Status: ", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Finalize MMT Collection
        //MMTs in ReadyToSend state and in OnHold state with AchDebit PaymentMethod
        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend};
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .setTaxPaymentStatuses(taxPaymentStatuses)
                        .find();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions1 =
                MoneyMovementTransaction.findTaxPayments()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .setTaxPaymentStatuses(new TaxPaymentStatus[]{TaxPaymentStatus.OnHold})
                        .setPaymentMethods(new PaymentMethod[]{PaymentMethod.ACHDebit})
                        .find();

        moneyMovementTransactions.addAll(moneyMovementTransactions1);
        mmts = new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            junit.framework.Assert.assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoid_PayrollWithATFFinalizedPaymentsForACHCredit() throws Exception {

        //Setting up company with SUI payment templates and Finalizing SUI payments
        Company company = setupCompanyWithATFFinalizedPaymentsForACHCredit();

        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        //Void First payroll to validate ATFFinalized payments are not updated to zero
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                .setPeriodBeginDate(quarterBeginDate)
                .setPeriodEndDate(quarterEndDate)
                .setTaxPaymentStatuses(taxPaymentStatuses)
                .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized MO-MODES-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("MO-MODES-PAYMENT")
                .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("214")))).size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoid_PayrollSubmittedAfterATFFinalizedPaymentsForACHCredit() throws Exception {
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        //Setting up company with SUI payment templates and Finalizing SUI payments, submit another payroll after finalizing pending SUI payments
        testPayrollSubmit_AfterATFFinalizedPaymentsForACHCredit();

        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);

        //Offload impound
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()));

        //Void Second payroll to make sure we update the amount to zero as they are not finalized
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), null));
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                .setPeriodBeginDate(quarterBeginDate)
                .setPeriodEndDate(quarterEndDate)
                .setTaxPaymentStatuses(taxPaymentStatuses)
                .find();
        assertEquals("ATFFinalized payments", 1, moneyMovementTransactions.size());
        assertEquals("ATFFinalized MO-MODES-PAYMENT payment", 1, moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("MO-MODES-PAYMENT")
                .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("214")))).size());
        taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.Ignore};
        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHCredit};
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments()
                .setPeriodBeginDate(quarterBeginDate)
                .setPeriodEndDate(quarterEndDate)
                .setTaxPaymentStatuses(taxPaymentStatuses)
                .setPaymentMethods(paymentMethods)
                .find();
        assertEquals("Total RTS SUI payments", 1, moneyMovementTransactions.size());
        assertEquals("RTS SUI payments with zero amount", 1, moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO)).size());
        PayrollServices.commitUnitOfWork();
    }

}
