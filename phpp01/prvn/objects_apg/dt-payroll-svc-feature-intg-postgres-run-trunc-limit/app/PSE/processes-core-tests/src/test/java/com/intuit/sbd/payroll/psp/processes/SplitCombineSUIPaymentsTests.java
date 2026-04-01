package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
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
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Feb 2, 2012
 * Time: 4:10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SplitCombineSUIPaymentsTests {



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
    public void happyPath_SplitPartialThenCombineEntirePayment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        payrollDate = new DateDTO("2011-01-14");

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
        SpcfUniqueId mmtId = null;
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            mmtId = mmt.getId();
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        assertNotNull("Money Movement Transaction is null", mmt);
        ArrayList<FinancialTransaction> financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        financialTransactions.remove(0);
        PayrollServices.paymentManager.splitSUIPayments(financialTransactions, "NOTE- UNIT TEST");

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setOnHold()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("Money Movement Transactions On Hold", moneyMovementTransactions.size() == 1);
        mmtId = moneyMovementTransactions.getFirst().getId();

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        PayrollServices.paymentManager.combineSUIPayments(financialTransactions, null);
        PayrollServices.commitUnitOfWork();

        // Assert that after combining there will be only one mmt with ATFFinalized Tax payment Status

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("Money Movement Transactions Size = 1", moneyMovementTransactions.size() == 1);

        mmt = moneyMovementTransactions.getFirst();

        assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void happyPath_SplitPartialThenFinalizePaymentChangeSettlementDate() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        payrollDate = new DateDTO("2011-01-14");

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
        SpcfUniqueId mmtId = null;
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            mmtId = mmt.getId();
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        assertNotNull("Money Movement Transaction is null", mmt);
        ArrayList<FinancialTransaction> financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        financialTransactions.remove(0);
        PayrollServices.paymentManager.splitSUIPayments(financialTransactions, "NOTE- UNIT TEST");

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setOnHold()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertEquals("Money Movement Transactions On Hold", 1, moneyMovementTransactions.size());
        mmt = moneyMovementTransactions.getFirst();
        mmt.expireTaxPaymentOnHoldReason(PaymentOnHoldReason.Agent);
        mmtId = mmt.getId();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        SpcfCalendar newInitiationDate = SpcfCalendar.createInstance(2011, 4, 27);
        newInitiationDate = newInitiationDate.toLocal();
        CalendarUtils.clearTime(newInitiationDate);
        mmt.updateTaxInitiationDate(newInitiationDate);
        ArrayList<MoneyMovementTransaction> mmtList = new ArrayList<MoneyMovementTransaction>();
        mmtList.add(mmt);
        PayrollServices.paymentManager.finalizeSUIPayments(mmtList, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        // Assert that after combining there will be only one mmt with ATFFinalized Tax payment Status

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertEquals("Money Movement Transactions", 1, moneyMovementTransactions.size());

        mmt = moneyMovementTransactions.getFirst();

        assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void happyPath_SplitThenCombineEntirePayment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        payrollDate = new DateDTO("2011-01-14");

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
        SpcfUniqueId mmtId = null;
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            mmtId = mmt.getId();
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        assertNotNull("Money Movement Transaction is null", mmt);
        ArrayList<FinancialTransaction> financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }

        PayrollServices.paymentManager.splitSUIPayments(financialTransactions, null);

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("Money Movement Transactions On Hold", moneyMovementTransactions.size() == 1);
        mmt = moneyMovementTransactions.getFirst();
        assertEquals("Tax Payment Status: ", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        mmtId = mmt.getId();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        PayrollServices.paymentManager.combineSUIPayments(financialTransactions, null);
        PayrollServices.commitUnitOfWork();

        // Assert that after combining there will be only one mmt with ATFFinalized Tax payment Status

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("Money Movement Transactions Size = 1", moneyMovementTransactions.size() == 1);

        mmt = moneyMovementTransactions.getFirst();

        assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void happyPath_SplitEntireThenCombinePartialPayment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        payrollDate = new DateDTO("2011-01-14");

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
        SpcfUniqueId mmtId = null;
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            mmtId = mmt.getId();
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        assertNotNull("Money Movement Transaction is null", mmt);
        ArrayList<FinancialTransaction> financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }

        PayrollServices.paymentManager.splitSUIPayments(financialTransactions, null);

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("Money Movement Transactions On Hold", moneyMovementTransactions.size() == 1);
        mmt = moneyMovementTransactions.getFirst();
        assertEquals("Tax Payment Status: ", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        mmtId = mmt.getId();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        financialTransactions.remove(0);
        PayrollServices.paymentManager.combineSUIPayments(financialTransactions, null);
        PayrollServices.commitUnitOfWork();

        // Assert that after combining there will be still 2 mmts, one onHold and one ATFFinalized

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("On Hold  Money Movement Transactions = 1", moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)).size() == 1);
        assertTrue("ATFFinalized Money Movement Transactions = 1", moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ATFFinalized)).size() == 1);


        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_SplitPartialThenCombinePartialPayment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        payrollDate = new DateDTO("2011-01-14");

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
        SpcfUniqueId mmtId = null;
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            mmtId = mmt.getId();
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        assertNotNull("Money Movement Transaction is null", mmt);
        ArrayList<FinancialTransaction> financialTransactions = new ArrayList<FinancialTransaction>();

        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        SpcfMoney onHoldAmount = new SpcfMoney(mmt.getMoneyMovementTransactionAmount().subtract(financialTransactions.get(0).getFinancialTransactionAmount()));
        SpcfMoney finalizedAmount = new SpcfMoney(mmt.getMoneyMovementTransactionAmount().subtract(onHoldAmount));
        financialTransactions.remove(0);
        PayrollServices.paymentManager.splitSUIPayments(financialTransactions, null);

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        DomainEntitySet<MoneyMovementTransaction> mmtsByStatus = moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold));
        assertTrue("On Hold  Money Movement Transactions = 1", mmtsByStatus.size() == 1);
        MoneyMovementTransaction onHoldMMT = mmtsByStatus.getFirst();
        assertEquals("OnHold MMT Amount", onHoldAmount, onHoldMMT.getMoneyMovementTransactionAmount());
        mmtsByStatus = moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ATFFinalized));
        assertTrue("ATFFinalized Money Movement Transactions = 1", mmtsByStatus.size() == 1);
        assertEquals("Finalized MMT Amount", finalizedAmount, mmtsByStatus.getFirst().getMoneyMovementTransactionAmount());
        mmtId = onHoldMMT.getId();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        financialTransactions.remove(0);
        PayrollServices.paymentManager.combineSUIPayments(financialTransactions, null);
        PayrollServices.commitUnitOfWork();

        // Assert that after combining there will be still 2 mmts, one onHold and one ATFFinalized

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("On Hold  Money Movement Transactions = 1", moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)).size() == 1);
        assertTrue("ATFFinalized Money Movement Transactions = 1", moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ATFFinalized)).size() == 1);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_SplitPartialThenRemoveHold() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        payrollDate = new DateDTO("2011-01-14");

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
        SpcfUniqueId mmtId = null;
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            mmtId = mmt.getId();
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        assertNotNull("Money Movement Transaction is null", mmt);
        ArrayList<FinancialTransaction> financialTransactions = new ArrayList<FinancialTransaction>();

        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }
        SpcfMoney onHoldAmount = new SpcfMoney(mmt.getMoneyMovementTransactionAmount().subtract(financialTransactions.get(0).getFinancialTransactionAmount()));
        SpcfMoney finalizedAmount = new SpcfMoney(mmt.getMoneyMovementTransactionAmount().subtract(onHoldAmount));
        financialTransactions.remove(0);
        PayrollServices.paymentManager.splitSUIPayments(financialTransactions, null);

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPaymentTemplate(paymentTemplate)
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        DomainEntitySet<MoneyMovementTransaction> mmtsByStatus = moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold));
        assertTrue("On Hold  Money Movement Transactions = 1", mmtsByStatus.size() == 1);
        MoneyMovementTransaction onHoldMMT = mmtsByStatus.getFirst();
        assertEquals("OnHold MMT Amount", onHoldAmount, onHoldMMT.getMoneyMovementTransactionAmount());
        mmtsByStatus = moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ATFFinalized));
        assertTrue("ATFFinalized Money Movement Transactions = 1", mmtsByStatus.size() == 1);
        assertEquals("Finalized MMT Amount", finalizedAmount, mmtsByStatus.getFirst().getMoneyMovementTransactionAmount());
        mmtId = onHoldMMT.getId();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        SpcfCalendar newInitiationDate = mmt.getInitiationDate().toLocal();
        newInitiationDate.addDays(20);
        mmt.updateInitiationDate(newInitiationDate);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(mmt);
        PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmt,PaymentOnHoldReason.Agent);
        PayrollServices.commitUnitOfWork();

        // Assert the hold is removed

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPaymentTemplate(paymentTemplate)
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        assertEquals("On Hold  Money Movement Transactions", 0, moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)).size());
        assertEquals("ATFFinalized Money Movement Transactions", 1, moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("ReadyToSend Money Movement Transactions", 1, moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)).size());
        
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPaymentTemplate(paymentTemplate)
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find().find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend));
        ArrayList<MoneyMovementTransaction>  mmts =   new ArrayList<MoneyMovementTransaction>(moneyMovementTransactions);
        PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, 2011, 1);
        PayrollServices.commitUnitOfWork();

        // Assert the mmts are combined into one Finalized Transaction

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setPaymentTemplate(paymentTemplate)
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        assertEquals("On Hold  Money Movement Transactions", 0, moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)).size());
        assertEquals("ATFFinalized Money Movement Transactions", 1, moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ATFFinalized)).size());
        assertEquals("ReadyToSend Money Movement Transactions", 0, moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)).size());

        PayrollServices.rollbackUnitOfWork();
        
    }


    @Test
    public void testFinalizeTwoPayments() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"CT"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit);
        Company company = companies.get(0);

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("CT-2MAG-PAYMENT");
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("CT-2MAG-PAYMENT Payment Init date", SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertSuccess(PayrollServices.paymentManager.updateInitiationDate(moneyMovementTransaction.getId().toString(), SpcfCalendar.createInstance(2011, 1, 16, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("CT-2MAG-PAYMENT Payment Init date", SpcfCalendar.createInstance(2011, 1, 16, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("CT-2MAG-PAYMENT", 2, moneyMovementTransactions.size());
        MoneyMovementTransaction firstMMT = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.InitiationDate().equalTo(SpcfCalendar.createInstance(2011, 1, 16, SpcfTimeZone.getLocalTimeZone()))));
        MoneyMovementTransaction secondMMT = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.InitiationDate().equalTo(SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()))));
        assertSuccess(PayrollServices.paymentManager.finalizeSUIPayments(Arrays.asList(firstMMT),
                null,
                2011,
                1));

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(secondMMT);
        assertSuccess(PayrollServices.paymentManager.finalizeSUIPayments(Arrays.asList(secondMMT),
                null,
                2011,
                1));

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("CT-2MAG-PAYMENT Payment Init date", SpcfCalendar.createInstance(2011, 1, 16, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("CT-2MAG-PAYMENT Payment Due date", SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        assertEquals("Financial transaction by Settlement date", 2, moneyMovementTransaction.getFinancialTransactionCollection().find(FinancialTransaction.SettlementDate().equalTo(SpcfCalendar.createInstance(2011, 1, 18, SpcfTimeZone.getLocalTimeZone()))).size());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void happyPath_SplitEntirePayment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"CT"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        // Finalize AR-209B-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("CT-2MAG-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("CT-2MAG-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        SpcfUniqueId mmtId = null;
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            mmtId = mmt.getId();
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        assertNotNull("Money Movement Transaction is null", mmt);
        ArrayList<FinancialTransaction> financialTransactions = new ArrayList<FinancialTransaction>();
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            financialTransactions.add(ft);
        }

        PayrollServices.paymentManager.splitSUIPayments(financialTransactions, "UNIT TEST NOTE");

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        assertTrue("Money Movement Transactions On Hold", moneyMovementTransactions.size() == 1);
        mmt = moneyMovementTransactions.getFirst();
        assertEquals("Tax Payment Status: ", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        mmtId = mmt.getId();
        PayrollServices.rollbackUnitOfWork();


    }

}
