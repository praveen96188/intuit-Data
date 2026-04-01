package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadATFFinalizedPayments;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Feb 21, 2012
 * Time: 4:10:43 PM
 */
public class AdjustSUITaxPaymentsTests {
    @BeforeClass
    public static void beforeClass() {
    }

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
    public void happyPath_AdjustImmediateDebit() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
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

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        Company company = Company.findCompany(moneyMovementTransactions.get(0).getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Immediate Debit
        company = DataLoadServices.refreshCompany(company);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, true, null);
        PayrollServices.commitUnitOfWork();


        // Check Financial Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 0, financialTransactions.size());

        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of employer tax debits created: ", 2, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        LiabilityCheck liabilityCheck = assertOne(Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                       .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + ""))));
        assertEquals("Token did not increment", beginningToken + 1, company.getCurrentToken());
        assertEquals("Next transaction id did not increment", beginningTransactionId + 1, Long.parseLong(company.getNextPayrollTransactionId()));

        assertEquals(beginningTransactionId, Long.parseLong(liabilityCheck.getSourceId()));
        assertEquals(beginningToken + 1, liabilityCheck.getQbdtTransactionInfo().getToken());
        assertEquals(CalendarUtils.getLastDayOfQuarter(2011, 1), liabilityCheck.getPeriodEndDate().toLocal());
        FinancialTransaction financialTransaction = assertOne(liabilityCheck.getPayrollRun().getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit)));
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, financialTransaction.getLaw().getLawId());
        assertEquals(1, liabilityCheck.getLiabilityCheckLineCollection().size());
        SpcfDecimal total = SpcfMoney.ZERO;
        for (LiabilityCheckLine liabilityCheckLine : liabilityCheck.getLiabilityCheckLineCollection()) {
            assertEquals(companyLaw.getQbdtPayrollItemInfo().getExpenseAccount(), liabilityCheckLine.getQbdtTransactionInfo().getAccountName());
            assertEquals("Q1 2011 Expense Account for " + companyLaw.getSourceDescription(), liabilityCheckLine.getQbdtTransactionInfo().getMemo());
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void happyPath_AdjustImmediateCredit() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
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

        // Adjust Finalized Payment With Immediate Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());

        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, true, null);
        PayrollServices.commitUnitOfWork();

        // Check Financial Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 0, financialTransactions.size());

        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxCredit)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of employer tax credits created: ", 1, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();        
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                               .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + "")));
        assertEquals("Liability check created for credit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void happyPath_AdjustImmediateCreditWithPositiveAndNegativeAdjustment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"IA"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
        // Finalize IA-600103-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("IA-600103-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("IA-600103-PAYMENT");
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

        // Adjust Finalized Payment With Immediate Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());

        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "95");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        law = Application.findById(Law.class, "156");
        lawAmounts.put(law, new SpcfMoney("100.00"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, true, null);
        PayrollServices.commitUnitOfWork();

        // Check Financial Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 0, financialTransactions.size());

        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxCredit)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of employer tax credits created: ", 1, financialTransactions.size());
        assertEquals("Employer Tax Credit Amount:", "27.27", financialTransactions.get(0).getFinancialTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();

        // Check ERPayable Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                               .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + "")));
        assertEquals("Liability check created for credit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_AdjustImmediateDebitWithPositiveAndNegativeAdjustment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"IA"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
        // Finalize IA-600103-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("IA-600103-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("IA-600103-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.get(0);
        Company company = Company.findCompany(moneyMovementTransaction.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "95");
        String suiERAccountName = "IA SUI Expenses";
        companyLaw.getQbdtPayrollItemInfo().setExpenseAccount(suiERAccountName);
        Application.save(companyLaw);
        companyLaw = CompanyLaw.findCompanyLaw(company, "156");
        companyLaw.getQbdtPayrollItemInfo().setExpenseAccount(null);
        Application.save(companyLaw);
        PayrollServices.commitUnitOfWork();

        // Adjust Finalized Payment With Immediate Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "95");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        law = Application.findById(Law.class, "156");
        lawAmounts.put(law, new SpcfMoney("-100.00"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, true, null);
        PayrollServices.commitUnitOfWork();

        // Check Financial Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 0, financialTransactions.size());

        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of employer tax debits created: ", 1, financialTransactions.size());
        assertEquals("Employer Tax Debit Amount:", "27.27", financialTransactions.get(0).getFinancialTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();

        // Check ERPayable Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        LiabilityCheck liabilityCheck = assertOne(Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                       .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + ""))));

        assertEquals(SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), liabilityCheck.getPeriodEndDate().toLocal());
        assertEquals(2, liabilityCheck.getLiabilityCheckLineCollection().size());
        SpcfDecimal total = SpcfMoney.ZERO;
        for (LiabilityCheckLine liabilityCheckLine : liabilityCheck.getLiabilityCheckLineCollection()) {
            assertEquals(suiERAccountName, liabilityCheckLine.getQbdtTransactionInfo().getAccountName());
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_AdjustDebitWithVarianceAccount() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
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

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Variance Debit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxReceivable variance transaction created: ", TransactionTypeCode.EmployerSUITaxReceivable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxReceivable amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                               .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + "")));
        assertEquals("Liability check without debit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void happyPath_AdjustCreditWithVarianceAccount() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));

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

        // Adjust Finalized Payment With Variance - Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxPayable variance transaction created: ", TransactionTypeCode.EmployerSUITaxPayable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxPayable amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                               .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + "")));
        assertEquals("Liability check created for credit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_AdjustDebitWithVarianceAccountOffloadDebit() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));

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

        // Adjust Finalized Payment With Variance - Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxReceivable variance transaction created: ", TransactionTypeCode.EmployerSUITaxReceivable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxReceivable amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance

        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-127.27", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.TaxCurrentLiability));
        assertEquals("TaxCurrentLiability amount: ", "573.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERLiabilityOffset));
        assertEquals("ERLiabilityOffset amount: ", "127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                               .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + "")));
        assertEquals("Liability check created for credit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();
        
        //Offload ACHDebit payment

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 4, 29);
        new OffloadATFFinalizedPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        // Check Ledger Balances
        PayrollServices.beginUnitOfWork();
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-127.27", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.TaxCurrentLiability));
        assertEquals("TaxCurrentLiability amount: ", "113.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERLiabilityOffset));
        assertEquals("ERLiabilityOffset amount: ", "0.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_testOverpaymentsNotApplied() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));

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

        // Adjust Finalized Payment With Variance - Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxReceivable variance transaction created: ", TransactionTypeCode.EmployerSUITaxReceivable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxReceivable amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance

        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                               .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + "")));
        assertEquals("Liability check created for credit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();

        //Offload ACHDebit payment

        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 4, 29);
        new OffloadATFFinalizedPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        // Check Ledger Balances
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        String companyId = mmt.getCompany().getSourceCompanyId();
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        //Void Paycheck
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(companyId, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        voidPayrollDTO.setPaycheckIdList(Arrays.asList(payrollRun.getPaycheckCollection().getFirst().getSourcePaycheckId()));
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, companyId, voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void happyPath_AdjustDebitWithVarianceAccountAR_PSRV003294() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AL"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        // Finalize AL-CR4UI-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AL-CR4UI-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AL-CR4UI-PAYMENT");
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

        // Adjust Finalized Payment With Variance Debit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        company = mmt.getCompany();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "84");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();


        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                                                                                       TransactionTypeCode.EmployerSUITaxPayable)
                                                             .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxReceivable variance transaction created: ", TransactionTypeCode.EmployerSUITaxReceivable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxReceivable amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)
                                                                                                               .And(LiabilityCheck.SourceId().equalTo(beginningTransactionId + "")));
        assertEquals("Liability check without debit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Variance Debit

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        lawAmounts = new HashMap<Law, SpcfMoney>();
        law = Application.findById(Law.class, "144");
        lawAmounts.put(law, new SpcfMoney("144.00"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        where = FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                                                                                       TransactionTypeCode.EmployerSUITaxPayable)
                                                             .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 2, financialTransactions.size());
        assertEquals("EmployerSUITaxReceivable variance transaction created (Law-84): ", 1, financialTransactions.find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerSUITaxReceivable)
                                                                      .And(FinancialTransaction.Law().LawId().equalTo("84")).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("127.27")))).size());
        assertEquals("EmployerSUITaxReceivable variance transaction created (Law-144): ", 1, financialTransactions.find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerSUITaxReceivable)
                                                                                                                                  .And(FinancialTransaction.Law().LawId().equalTo("144")).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("144")))).size());

        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-271.27", balance.toString());   // 127.27+144
        PayrollServices.rollbackUnitOfWork();

    }
}
