package com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ModifyWireExpectedDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadATFFinalizedPayments;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EoqSUIAdjustmentsProcessor;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTRNRQ;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.request.ITXLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddWireExpectedDataLoader;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Feb 28, 2012
 * Time: 1:33:31 PM
 */
public class EoqSUIAdjustmentsTests {

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
    public void happyPath_EOQCreateDebit() throws Exception {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();
        // Adjust Finalized Payment With Variance Debit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
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
        assertEquals("EmployerSUITaxCollection amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
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
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_AgentImmediateDebit() throws Exception {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();
        // Adjust Finalized Payment With Variance Debit
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("Admin");
        ProcessResult<AuthUser> processResult =
                PayrollServices.userManager.addUser("UnitTestAgent1", Arrays.asList(foundRole.getRoleId()), "pFirstName", "Last1");
        assertSuccess("Add User ProcessResult ", processResult);
        AuthUser user = processResult.getResult();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        user = AuthUser.findUser(user.getCorpId());
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = mmt.getCompany();
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, true, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit))
                                    .And(FinancialTransaction.Company().equalTo(mmt.getCompany()))
                                    .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("127.27")));
        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of debit transactions created: ", 1, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();
        // Check EOQ Transactions
        PayrollServices.beginUnitOfWork();
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
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_EOQCreateDebit_BackDated() throws Exception {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();
        // Adjust Finalized Payment With Variance Debit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
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
        DataLoadServices.setPSPDate(2011, 5, 16);

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone());
        String processingNote = "Double credit";
        new EoqSUITaxAdjustments().process(processingDate, processingNote, true);

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
        assertEquals("EmployerSUITaxCollection  transaction created: ", TransactionTypeCode.EmployerSUITaxCollection, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxCollection amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollRun payrollRun = ft.getPayrollRun();
        assertEquals("period end date", SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), payrollRun.getPaycheckDate().toLocal());
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.TransactionDate().equalTo(ft.getSettlementDate()));
        assertEquals("Liability check not created for debit", 1, liabilityChecks.size());
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertEquals("Token did not increment", beginningToken + 1, company.getCurrentToken());
        assertEquals("Next transaction id did not increment", beginningTransactionId + 1, Long.parseLong(company.getNextPayrollTransactionId()));
        LiabilityCheck liabilityCheck = liabilityChecks.get(0);
        assertEquals(beginningTransactionId, Long.parseLong(liabilityCheck.getSourceId()));
        assertEquals(beginningToken + 1, liabilityCheck.getQbdtTransactionInfo().getToken());
        assertEquals(SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), liabilityCheck.getPeriodEndDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2011, 5, 18, SpcfTimeZone.getLocalTimeZone()), liabilityCheck.getTransactionDate().toLocal());
        assertEquals(1, liabilityCheck.getLiabilityCheckLineCollection().size());
        SpcfDecimal total = SpcfMoney.ZERO;
        for (LiabilityCheckLine liabilityCheckLine : liabilityCheck.getLiabilityCheckLineCollection()) {
            assertNotNull(liabilityCheckLine.getQbdtTransactionInfo().getAccountName());
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        // check manual note
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ManualNoteEvent);
        assertEquals(1, companyEvents.size());
        assertEquals(processingNote, companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NoteText).get(0).getValue());
        companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.SUIEoqCreditCreated);
        assertEquals(0, companyEvents.size());
        companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.SUIEoqDebitCreated);
        assertEquals(0, companyEvents.size());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * PSP-11857
     * Verify EOQSUITaxAdjustments Step
     * picks manually recreated transactions for processing
     * @throws Exception
     */
    @Test
    public void happyPath_EOQCreateCreditReturned() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 3, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
        // Finalize Payments
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setATFFinalized()
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        SpcfUniqueId mmtId_AR = moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT")).And(MoneyMovementTransaction.Company().equalTo(companies.get(1)))).get(0).getId();

        PayrollServices.rollbackUnitOfWork();
        // Adjust Finalized AR Payment With Variance - Credit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions for AR
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
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
        // Adjust Finalized CT Payment With Variance - Debit
        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate1 = SpcfCalendar.createInstance(2011, 4, 29);
        new OffloadATFFinalizedPayments().process(processingDate1);
        PayrollServices.commitUnitOfWork();

        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "127.27", balance.toString());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 1));
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);

        // Check EOQ Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxRefund)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));
        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of eoq adjustment transactions created: ", 1, financialTransactions.size());
        ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxRefund transaction created: ", TransactionTypeCode.EmployerSUITaxRefund, ft.getTransactionType().getTransactionTypeCd());
        DomainEntitySet<FinancialTransaction> associatedTransactions = ft.getAssociatedTransactionsCollection();
        Assert.assertEquals("Number of Associated Transactions: ", 1, associatedTransactions.size());
        assertEquals("EmployerSUITaxRefund amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void happyPath_EOQCreateCredit() throws Exception {
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
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
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

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);

        // Check EOQ Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxRefund)
                                                             .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));
        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of eoq adjustment transactions created: ", 1, financialTransactions.size());
        ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxRefund transaction created: ", TransactionTypeCode.EmployerSUITaxRefund, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxRefund amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        assertEquals("Payroll run status", PayrollStatus.Pending, ft.getPayrollRun().getPayrollRunStatus());
        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.SourceId().equalTo(beginningTransactionId + ""));
        assertEquals("Liability check created for credit", 0, liabilityChecks.size());
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.runOffload(company, 2011, 5, 2);
        PayrollServices.beginUnitOfWork();
        ft = Application.refresh(ft);
        assertEquals("Payroll run status", PayrollStatus.OffloadedAll, ft.getPayrollRun().getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.runACHTransactionProcessor();
        PayrollServices.beginUnitOfWork();
        ft = Application.refresh(ft);
        assertEquals("Payroll run status", PayrollStatus.Complete, ft.getPayrollRun().getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_EOQCreateERCreditMultipleAdjustments() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = new ArrayList<>();
        companies.add(assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit)));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
        // Finalize Payments
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setATFFinalized()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        SpcfUniqueId mmtId_AR = moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT"))).get(0).getId();
        PayrollServices.rollbackUnitOfWork();
        // Adjust Finalized AR Payment With Variance - Credit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions for AR
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
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
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);

        // Check EOQ Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxRefund)
                                                             .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));
        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of eoq adjustment transactions created: ", 1, financialTransactions.size());
        ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxRefund transaction created: ", TransactionTypeCode.EmployerSUITaxRefund, ft.getTransactionType().getTransactionTypeCd());
        DomainEntitySet<FinancialTransaction> associatedTransactions = ft.getAssociatedTransactionsCollection();
        Assert.assertEquals("Number of Associated Transactions: ", 1, associatedTransactions.size());
        assertEquals("EmployerSUITaxRefund amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_EOQCreateERDebitMultipleAdjustments() throws Exception {
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
        // Finalize Payments
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setATFFinalized()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        SpcfUniqueId mmtId_AR = moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT"))).get(0).getId();
        PayrollServices.rollbackUnitOfWork();
        // Adjust Finalized AR Payment With Variance - Credit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions for AR
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
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
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone());
        new EoqSUITaxAdjustments().process(processingDate, null, true);

        // Check EOQ Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId_AR);
        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxRefund, TransactionTypeCode.EmployerSUITaxCollection)
                                                             .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));
        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of eoq adjustment transactions created: ", 1, financialTransactions.size());
        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERPayable));
        assertEquals("ERPayable amount: ", "0.00", balance.toString());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_EOQCreateMultipleAdjustmentsMultipleCompanies() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 3, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
        // Finalize Payments
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setATFFinalized()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        PayrollServices.rollbackUnitOfWork();
        // Adjust Finalized AR Payment With Variance - Credit
        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            MoneyMovementTransaction mmt = getMMT(company, PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT"));
            HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
            Law law = Application.findById(Law.class, "85");
            lawAmounts.put(law, new SpcfMoney("-127.27"));
            PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        }
        PayrollServices.commitUnitOfWork();
        // Adjust Finalized AR Payment With Variance - Debits
        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            MoneyMovementTransaction mmt = getMMT(company, PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT"));
            HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
            Law law = Application.findById(Law.class, "85");
            lawAmounts.put(law, new SpcfMoney("27.27"));
            PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        }
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 4, 29);
        new OffloadATFFinalizedPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);

    }

    @Test
    public void happyPath_RunJobTwice() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 3, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
        // Finalize Payments
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                                        .setATFFinalized()
                                        .setPeriodBeginDate(quarterBeginDate)
                                        .setPeriodEndDate(quarterEndDate)
                                        .find();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }
        PayrollServices.rollbackUnitOfWork();
        // Adjust Finalized AR Payment With Variance - Credit
        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            MoneyMovementTransaction mmt = getMMT(company, PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT"));
            HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
            Law law = Application.findById(Law.class, "85");
            lawAmounts.put(law, new SpcfMoney("-127.27"));
            PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        }
        PayrollServices.commitUnitOfWork();
        // Adjust Finalized AR Payment With Variance - Debits
        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            MoneyMovementTransaction mmt = getMMT(company, PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT"));
            HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
            Law law = Application.findById(Law.class, "85");
            lawAmounts.put(law, new SpcfMoney("27.27"));
            PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        }
        PayrollServices.commitUnitOfWork();

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);


        processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);

    }

    @Test
    public void testEOQCreateDebit_WithCompanyOnHold() throws Exception {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();
        // Adjust Finalized Payment With Variance Debit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
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
        // place company on hold
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.AchRejectR1R9);

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
        assertEquals("EmployerSUITaxCollection  transaction created: ", TransactionTypeCode.EmployerSUITaxCollection, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxCollection amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
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
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.runOffload(company, 2011, 5, 2);
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor2 = new ProcessMissedACHTransactions();
        missedTxProcessor2.process(null);
        PayrollServices.commitUnitOfWork();
        // move past initiation date
        DataLoadServices.setPSPDate(2011, 5, 4);
        DataLoadServices.removeCompanyOnHoldReasons(company);
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxCollection)
                                                             .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));
        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of eoq adjustment transactions created: ", 1, financialTransactions.size());
        ft = assertOne(financialTransactions.get(0).getPayrollRun().getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)));
        // make sure the initiation date was updated so the debit will offload with the new initiation date
        assertEquals("Initiation date not updated", SpcfCalendar.createInstance(2011, 5, 4, SpcfTimeZone.getLocalTimeZone()), ft.getMoneyMovementTransaction().getInitiationDate().toLocal());
        for (FinancialTransaction financialTransaction : ft.getMoneyMovementTransaction().getFinancialTransactionCollection()) {
            assertEquals("Settlement date not updated", SpcfCalendar.createInstance(2011, 5, 5, SpcfTimeZone.getLocalTimeZone()), financialTransaction.getSettlementDate().toLocal());
        }
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void happyPath_EOQWriteOffReturnedDebit() throws Exception {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();
        // Adjust Finalized Payment With Variance Debit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
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
        assertEquals("EmployerSUITaxCollection  transaction created: ", TransactionTypeCode.EmployerSUITaxCollection, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxCollection amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
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
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.setPSPDate(2011, 5, 2);
        DataLoadServices.runOffload();
        PayrollServices.beginUnitOfWork();
        financialTransactions = FinancialTransaction.findFinancialTransactions(company, TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed);
        assertEquals(1, financialTransactions.size());
        FinancialTransaction financialTransaction = financialTransactions.getFirst();
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.setPSPDate(2011, 5, 3);
        DataLoadServices.returnTxns(financialTransactions);
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        financialTransaction = Application.refresh(financialTransaction);
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), financialTransaction.getPayrollRun().getSourcePayRunId());
        assertTrue(processResult.isSuccess());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void return_EOQCreateDebit() throws Exception {
        SpcfUniqueId mmtId = setupCompanyAndTaxPayment();
        // Adjust Finalized Payment With Variance Debit
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
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
        assertEquals("EmployerSUITaxCollection  transaction created: ", TransactionTypeCode.EmployerSUITaxCollection, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxCollection amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
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
        String companyId = company.getSourceCompanyId();
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
            assertNull(liabilityCheckLine.getCompanyLaw());
            total = total.add(liabilityCheckLine.getAmount());
        }
        assertEquals("total", total, liabilityCheck.getAmount().negate());
        PayrollServices.rollbackUnitOfWork();
        // Offload and return debit
        DataLoadServices.setPSPDate(2011, 5, 2);
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null, ACHFileType.DD);
        DataLoadServices.setPSPDate(2011, 5, 8);
        PayrollServices.beginUnitOfWork();
        where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerSUITaxCollection)
                                                             .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));
        financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        FinancialTransaction financialTransaction = financialTransactions.get(0);
        String payrollId = financialTransaction.getPayrollRun().getSourcePayRunId();
        String finTxnId = financialTransaction.getId().toString();
        PayrollServices.commitUnitOfWork();
        DataLoadServices.returnTxns(financialTransactions, "R01", "NSF");
        PayrollServices.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(payrollId);
        wireExpectedDTO.setWireExpectedDate(new DateDTO("2011-05-11"));
        wireExpectedDTO.setActionEventCode(ActionEventCode.DDRedebitEdit);
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBDT, companyId, wireExpectedDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(processResult.isSuccess());
        // Post the Wire
        PayrollServices.beginUnitOfWork();
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(finTxnId, financialTransaction.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);
        processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        assertTrue(processResult.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    private SpcfUniqueId setupCompanyAndTaxPayment() {
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
        return mmtId;
    }

    private MoneyMovementTransaction getMMT(Company pCompany, PaymentTemplate pPaymentTemplate) {
        Criterion<MoneyMovementTransaction> where =
                MoneyMovementTransaction.Company().equalTo(pCompany)
                                        .And(MoneyMovementTransaction.PaymentTemplate().equalTo(pPaymentTemplate));
        DomainEntitySet<MoneyMovementTransaction> mmts = PayrollServices.entityFinder.find(MoneyMovementTransaction.class, where);
        if (mmts.size() > 0) {
            return mmts.get(0);
        }
        return null;
    }

    /*
     * This test is inspired from happyPath_EOQCreateCredit() test.
     *
     * This test should ideally be in TestProcessMissedPayrolls. All efforts to simulate this data setup, there failed.
     * So here we are ....
     *
     * Why are there no asserts after the test ? The condition we are trying to see is that there are no "ERROR" statements in the log.
     * So we run the test and verify this in the console output manually. :(
     */
    @Test
    public void happyPath_EOQCreateCredit_ProcessMissedPayrolls() throws Exception {
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
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();
        // Check Variance Transactions
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

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, false);

        // This is were we deviate from happyPath_EOQCreateCredit() test
        // put FTs on hold - so that we can test PMP batch job
        PayrollServices.beginUnitOfWork();
        Criterion<FinancialTransaction> where2 = FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxCredit,
                                                                                                               TransactionTypeCode.EmployerSUITaxRefund)
                                                                     .And(FinancialTransaction.Company().equalTo(mmt.getCompany()));
        DomainEntitySet<FinancialTransaction> financialTransactions2 = PayrollServices.entityFinder.find(FinancialTransaction.class, where2);
        for (FinancialTransaction ft2 : financialTransactions2) {
            ft2.updateOnHold(true);
            Application.save(ft2);
        }
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 10));
        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20110502");
        PayrollServices.commitUnitOfWork();

    }

    @Ignore
    @Test
    public void testHappyPathIndividual() throws Exception {
        Company company = setupDataForLiabilityAdjustments();
        new SUIRatePaymentsCleanUp().process(null, true, SpcfCalendar.createInstance(2012,4, 1, SpcfTimeZone.getLocalTimeZone()),
                                       SpcfCalendar.createInstance(2012,6, 29, SpcfTimeZone.getLocalTimeZone()),
                                       SpcfCalendar.createInstance(2012,4, 1, SpcfTimeZone.getLocalTimeZone()),
                                       SpcfCalendar.createInstance(2012,6, 29, SpcfTimeZone.getLocalTimeZone()),
                                       null
        );

        new EoqSUITaxAdjustments().process(SpcfCalendar.createInstance(2012, 7, 1), null, true);

        new LiabilityAdjustmentsCleanUp().process(true, 20122);

        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());


    }

    @Ignore
    @Test
    public void testHappyPathCombined() throws Exception {
        Company company = setupDataForLiabilityAdjustments();

        DataLoadServices.setPSPDate(2012, 7, 1);
        ArrayList<String> parameterList = new ArrayList<String>();
        parameterList.add("-commit=false");
        parameterList.add("-quarter=20122");
        parameterList.add("-startDate=20120401");
        parameterList.add("-startPaycheckDate=20120401");
        parameterList.add("-endPaycheckDate=20120629");
        parameterList.add("-processingDate=20120701");
        String[] parameters = new String[parameterList.size()];
        parameters = parameterList.toArray(parameters);

        // Run with comit as false. Should not cleanup the liability adjustment
        EoqSUIAdjustmentsProcessor.main(parameters);

        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        assertEquals("ERSUITaxDue amount: ", "15.50", balance.toString());

        parameterList.clear();
        parameterList.add("-commit=true");
        parameterList.add("-quarter=20122");
        parameterList.add("-startDate=20120401");
        parameterList.add("-startPaycheckDate=20120401");
        parameterList.add("-endPaycheckDate=20120629");
        parameterList.add("-processingDate=20120701");
        parameters = new String[parameterList.size()];
        parameters = parameterList.toArray(parameters);

        // run with commit as true
        EoqSUIAdjustmentsProcessor.main(parameters);

        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        assertEquals("ERSUITaxDue amount: ", "0.00", balance.toString());


    }



    private Company setupDataForLiabilityAdjustments() {
        DataLoadServices.reinitialize();
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.setPSPDate(2012, 5, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateCompanyLawRate(company.getSourceSystemCd(),
                                                                          company.getSourceCompanyId(),
                                                                          Law.getSuiLaw("CA", LawCategoryCode.UnemploymentEmployer),
                                                                          SpcfCalendar.createInstance(2012, 1, 1,
                                                                                                      SpcfTimeZone.getLocalTimeZone()), 0.012, true));
        PayrollServices.commitUnitOfWork();
        // Run payroll with ui Rate as 1.2%
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-05-20"));
        DataLoadServices.setPSPDate(2012, 5, 17);
        DataLoadServices.runOffload();
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("CA-UIETT-PAYMENT");
        // Finalize the payments for CA-UI
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2012, 2);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("CA-UIETT-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2012, 2);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2012, 2);
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

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "87");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        // Update the amount for the finalized transaction
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        // Update the sui rate to 2.8%
        assertSuccess(PayrollServices.companyManager.updateCompanyLawRate(company.getSourceSystemCd(),
                                                                          company.getSourceCompanyId(),
                                                                          Law.getSuiLaw("CA", LawCategoryCode.UnemploymentEmployer),
                                                                          SpcfCalendar.createInstance(2012, 4, 1,
                                                                                                      SpcfTimeZone.getLocalTimeZone()), 0.028, true));
        PayrollServices.commitUnitOfWork();

        setupLiabilityAdjustments(company, company.getSourceCompanyId());
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

        //Add or update ledger balance
        DataLoadServices.setPSPDate(2012, 06, 29);
        com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager.runJob(BatchJobType.LedgerBalance, new String[0]);

        // Check Ledger Balance
        balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.AgencyTaxRefund));
        assertEquals("ERSUITaxDue amount: ", "15.50", balance.toString());

        return company;
    }

    private void setupLiabilityAdjustments(Company company, String psid) {
        Employee emp = Application.find(Employee.class).get(0);
        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class, CompanyPayrollItem.Company().equalTo(company));
        itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                                                                 new SpcfMoney("-15.5"),
                                                                 "Class",
                                                                 false,
                                                                 "Memo",
                                                                 "13",
                                                                 new SpcfMoney("0.0"),
                                                                 new SpcfMoney("0.0")));

        liabilityAdjustments.add(OFXRequestGenerator.generatePayrollTransaction(null,
                                                                                null,
                                                                                null,
                                                                                new Date("6/20/2012"),
                                                                                new Date("6/20/2012"),
                                                                                emp.getSourceEmployeeId(),
                                                                                null,
                                                                                null,
                                                                                true,
                                                                                QBOFX.OFXPayrollTransactionTransactionType.LIABADJ,
                                                                                null,
                                                                                false,
                                                                                itxlines));
        OFX adjustmentOfx = new OFX();
        adjustmentOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 liabilityAdjustments,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequestStringResponse(adjustmentOfx);
    }

}
