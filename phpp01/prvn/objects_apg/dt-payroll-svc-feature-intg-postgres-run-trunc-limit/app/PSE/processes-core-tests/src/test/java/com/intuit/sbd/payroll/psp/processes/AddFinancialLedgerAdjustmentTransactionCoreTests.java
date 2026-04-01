package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 1/12/12
 * Time: 1:58 PM
 */
public class AddFinancialLedgerAdjustmentTransactionCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2012, 1, 12);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        //To delete posting rules and update Temp Transaction types to reusable if used
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PostingRule> postingRules = Application.findObjects(PostingRule.class);
        for (PostingRule postingRule : postingRules) {
            if(TransactionType.TEMP_FLA_TRANSACTION_TYPES.contains(postingRule.getTransactionType().getTransactionTypeCd())) {
                Application.deleteObject(postingRule);
            }
        }
        DomainEntitySet<TransactionType> transactionTypes = Application.findObjects(TransactionType.class);
        for (TransactionType transactionType : transactionTypes) {
            if(TransactionType.TEMP_FLA_TRANSACTION_TYPES.contains(transactionType.getTransactionTypeCd()) && !transactionType.getTransactionTypeCd().toString().equals(transactionType.getName())) {
                transactionType.setName(transactionType.getTransactionTypeCd().toString());
                Application.getHibernateSession().save(transactionType);
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testValidations() {

        //Test for validations - for all required fields, payroll run and Law id are required for ledger accounts that has REQUIRES_QUARTER_LAW = true Ex - AgencyTaxRefund

        Company company = setupCompanyWithPayrollRun();

        PayrollServices.beginUnitOfWork();
        PayrollRun existingPayrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), null, LedgerAccountCode.DDFutureLiability, new SpcfMoney("80.00"), null, "61", "NoteText - 61");
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Debit account is null has invalid value", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.ERReturnCash, null, new SpcfMoney("80.00"), null, "61", "NoteText - 61");
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Credit account is null has invalid value", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.TaxFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("80.00"), null, "61", null);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Note Text is null has invalid value", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.ERReturnCash, LedgerAccountCode.AgencyTaxRefund, new SpcfMoney("80.00"), null, "61", "Note text");
        assertOne(processResult.getErrorMessages());
        assertEquals("418", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("An FLA transaction cannot be created for accounts ERReturnCash and AgencyTaxRefund without a payroll run and law.", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.ERReturnCash, LedgerAccountCode.AgencyTaxRefund, new SpcfMoney("80.00"), existingPayrollRun.getId().toString(), null, "Note text");
        assertOne(processResult.getErrorMessages());
        assertEquals("418", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("An FLA transaction cannot be created for accounts ERReturnCash and AgencyTaxRefund without a payroll run and law.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testForNewlyAdded_PositingRules_void() {

        /*
        1. Test for using temp transaction type with existing payroll
        2. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        3. Assert for Company Event and company event details - (AccountingFinancialLedgerAdjustmentCreated)
        4. Void transaction, assert Ledger balances to verify that posting rules are created for Voided transaction state
        */
        DataLoadServices.reinitialize();
        Company company = setupCompanyWithPayrollRun();

        PayrollServices.beginUnitOfWork();
        PayrollRun existingPayrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );
        PayrollServices.commitUnitOfWork();

        //Offloading ER Tax Debit
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2012, 1, 12);
        SpcfCalendar todaysDate = PSPDate.getPSPTime();
        CalendarUtils.clearTime(todaysDate);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("80.00"), existingPayrollRun.getId().toString(), "61", "NoteText - 61");
        assertFinancialTransactions(company, processResult, existingPayrollRun.getPaycheckDate(), todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp1), new SpcfMoney("80.00"), "61", existingPayrollRun);

        DataLoadServices.assertLedgerBalances(company, existingPayrollRun,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, -80),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80)
        );

        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 80),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80)
        );
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.AccountingFinancialLedgerAdjustmentCreated));
        assertEquals("80.00" , companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        assertEquals("NoteText - 61" , companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        assertEquals("Batch_1" , companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId));
        assertEquals("FLATemp1" , companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.TransactionType));
        processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), processResult.getResult().getId().toString());
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Check balance after voiding the FLA transaction
        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189)
        );
        DataLoadServices.assertLedgerBalances(company, existingPayrollRun,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -189)
        );
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testHappyPath_WithExistingPayroll() {

        /*
        1. Test for using temp transaction type with existing payroll, before offload check for error and after offload
        2. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        Company company = setupCompanyWithPayrollRun();

        PayrollServices.beginUnitOfWork();
        PayrollRun existingPayrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("80.00"), existingPayrollRun.getId().toString(), "61", "NoteText - 61");
        assertEquals("Number of error messages", 1, processResult.getErrorMessages().size());
        assertTrue("Error message - code", processResult.getErrorMessages().containsMessage("420"));
        PayrollServices.commitUnitOfWork();

        //Offloading ER Tax Debit
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2012, 1, 12);
        SpcfCalendar todaysDate = PSPDate.getPSPTime();
        CalendarUtils.clearTime(todaysDate);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("80.00"), existingPayrollRun.getId().toString(), "61", "NoteText - 61");
        assertFinancialTransactions(company, processResult, existingPayrollRun.getPaycheckDate(), todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp1), new SpcfMoney("80.00"), "61", existingPayrollRun);

        DataLoadServices.assertLedgerBalances(company, existingPayrollRun,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, -80),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80)
        );

        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 80),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80)
        );

        /*
        3. Test with ledger account combination for using already create transaction type on existing payroll run, Law Id = NULL
        4. Assert ledger balances.
        */
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.ERReturnReceivable , LedgerAccountCode.ERReturnCash, new SpcfMoney("65.00"), existingPayrollRun.getId().toString(), null, "NoteText - NULL");
        assertFinancialTransactions(company, processResult, existingPayrollRun.getPaycheckDate(), todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLAdERRRcERRC), new SpcfMoney("65.00"), null, existingPayrollRun);

        DataLoadServices.assertLedgerBalances(company, existingPayrollRun,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, -80),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80),
                                              new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -65),
                                              new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 65)
        );

        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 80),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80),
                                              new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 65),
                                              new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -65)
        );

        /*
        5. Test for using next available temp transaction type for new ledger account combinations, for given Law Id
        6. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDCurrentCash, LedgerAccountCode.AgencyTaxRefund, new SpcfMoney("85.00"), existingPayrollRun.getId().toString(), "64", "Note text-64");
        assertFinancialTransactions(company, processResult, existingPayrollRun.getPaycheckDate(), todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp2), new SpcfMoney("85.00"), "64", existingPayrollRun);

        DataLoadServices.assertLedgerBalances(company, existingPayrollRun,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -189), //First payroll
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, -80),
                new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -65),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 65),
                new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, -85),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 85)
        );

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 189), //First payroll
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 189),
                new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 80),
                new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 80),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 65),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -65),
                new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, 85),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 85)
        );
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testHappyPath_NewPayroll() {
        /*
        1. Test for using first available temp transaction type for new ledger accounts combination with creating new payroll run
        2. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        Company company = setupCompanyWithPayrollRun();

        DataLoadServices.setPSPDate(2012, 1, 12);
        SpcfCalendar todaysDate = PSPDate.getPSPTime();
        CalendarUtils.clearTime(todaysDate);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("200.00"), null, "61", "NoteText - 61");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp1), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("200.00"), "61");
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 200),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 200)
        );
        PayrollServices.commitUnitOfWork();

        /*
        3. Test for using next available temp transaction type for new ledger accounts combination with creating new payroll run
        4. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureLiability, LedgerAccountCode.DDFutureReceivable, new SpcfMoney("25.00"), null, "61", "NoteText - 61");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp2), LedgerAccountCode.DDFutureLiability, LedgerAccountCode.DDFutureReceivable, new SpcfMoney("25.00"), "61");
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 175),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 175)
        );
        PayrollServices.commitUnitOfWork();

        /*
        5. Test for using next available temp transaction type for new ledger accounts combination with creating new payroll run
        6. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDCurrentCash, LedgerAccountCode.DDFutureLiability, new SpcfMoney("30.00"), null, "62", "NoteText - 62");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp3), LedgerAccountCode.DDCurrentCash, LedgerAccountCode.DDFutureLiability, new SpcfMoney("30.00"), "62");
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 175),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 205),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, 30)
        );
        PayrollServices.commitUnitOfWork();

        /*
        7. Test for using next available temp transaction type for new ledger accounts combination with creating new payroll run
        8. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureLiability, LedgerAccountCode.DDCurrentCash, new SpcfMoney("85.00"), null, "63", "NoteText - 63");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp4), LedgerAccountCode.DDFutureLiability, LedgerAccountCode.DDCurrentCash, new SpcfMoney("85.00"), "63");
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 175),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 120),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, -55)
        );
        PayrollServices.commitUnitOfWork();

        /*
        9. Test for using assigned temp transaction type for the existing ledger accounts combination with creating new payroll run
        10. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("40.00"), null, "64", "NoteText - 64");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp1), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDFutureLiability, new SpcfMoney("40.00"), "64");
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 215),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 160),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, -55)
        );
        PayrollServices.commitUnitOfWork();

        /*
        11. Test for using next available temp transaction type for new ledger accounts combination with creating new payroll run
        12. Assert ledger balances to verify that posting rules are created for assigned temp transaction type.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDCurrentLiability, new SpcfMoney("95.00"), null, "65", "NoteText - 65");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLATemp5), LedgerAccountCode.DDFutureReceivable, LedgerAccountCode.DDCurrentLiability, new SpcfMoney("95.00"), "65");
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 310),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 160),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, -55),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentLiability, 95)
        );
        PayrollServices.commitUnitOfWork();

        /*
        13. Test for using the existing temp transaction type for existing ledger accounts combination with creating new payroll run, for given Law Id
        14. Assert ledger balances to verify posting rules are present for existing transaction type.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.TaxCurrentLiability, LedgerAccountCode.TaxCurrentCash, new SpcfMoney("50.00"), null, "61", "NoteText - 61");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLAdTXCLcTXCC), LedgerAccountCode.TaxCurrentLiability, LedgerAccountCode.TaxCurrentCash, new SpcfMoney("50.00"), "61");
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -50),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 310),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 160),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, -55),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentLiability, 95)
        );
        PayrollServices.commitUnitOfWork();

        /*
        15. Test for using the existing temp transaction type for existing ledger accounts combination with creating new payroll run, for Law Id = NULL
        16. Assert ledger balances to verify posting rules are present for existing transaction type.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.TaxCurrentCash, LedgerAccountCode.TaxCurrentLiability, new SpcfMoney("60.00"), null, null, "NoteText - NULL");
        assertFinancialTransactions(company, processResult, todaysDate, TransactionType.findTransactionType(TransactionTypeCode.FLAdTXCCcTXCL), LedgerAccountCode.TaxCurrentCash, LedgerAccountCode.TaxCurrentLiability, new SpcfMoney("60.00"), null);
        DataLoadServices.assertLedgerBalances(company,
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189), //First payroll
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 10),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 10),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureReceivable, 310),
                                              new DataLoadServices.LB(LedgerAccountCode.DDFutureLiability, 160),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, -55),
                                              new DataLoadServices.LB(LedgerAccountCode.DDCurrentLiability, 95)
        );
        PayrollServices.commitUnitOfWork();

        /*
        17. Test for using temp transaction type for new ledger accounts combination with when all temp transaction types are being used.
        18. Assert for error message - All temp transaction types have been used.
        */
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), LedgerAccountCode.DDCurrentLiability, LedgerAccountCode.DDFutureReceivable, new SpcfMoney("50.00"), null, "61", "NoteText - 61");
        assertOne(processResult.getErrorMessages());
        assertEquals("419", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("An FLA transaction cannot be created for accounts DDCurrentLiability and DDFutureReceivable as all Temporary Transaction types have been used.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();
    }

    private Company setupCompanyWithPayrollRun(){
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.claimNoFeesOffer(company);

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertNotNull(company);
        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
        assertNotNull(taxService);
        Assert.assertEquals("Company Service Status", ServiceSubStatusCode.ActiveCurrent, taxService.getStatusCd());

        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        DataLoadServices.assertPayrollsEqual(payrollDTO, payroll);
        PayrollServices.commitUnitOfWork();

        return company;
    }

    private void assertFinancialTransactions(Company pCompany, ProcessResult<FinancialTransaction> pProcessResult, SpcfCalendar pCheckDate, SpcfCalendar pSettlementDate, TransactionType pTransactionType, SpcfMoney pAmount, String pLawId, PayrollRun pPayrollRun) {
        assertFinancialTransactions(pCompany, pProcessResult, pCheckDate, pSettlementDate, pTransactionType, null, null, pAmount, pLawId, PayrollStatus.OffloadedAll);
        assertEquals("Financial Transaction Payroll Run", pPayrollRun, pProcessResult.getResult().getPayrollRun());
    }

    private void assertFinancialTransactions(Company pCompany, ProcessResult<FinancialTransaction> pProcessResult, SpcfCalendar pTodaysDate, TransactionType pTransactionType, LedgerAccountCode pDebitLedgerAccountCode, LedgerAccountCode pCreditLedgerAccountCode, SpcfMoney pAmount, String pLawId) {
        assertFinancialTransactions(pCompany, pProcessResult, pTodaysDate, pTodaysDate, pTransactionType, pDebitLedgerAccountCode, pCreditLedgerAccountCode, pAmount, pLawId, PayrollStatus.Complete);
    }

    private void assertFinancialTransactions(Company pCompany, ProcessResult<FinancialTransaction> pProcessResult, SpcfCalendar pCheckDate, SpcfCalendar pSettlementDate, TransactionType pTransactionType, LedgerAccountCode pDebitLedgerAccountCode, LedgerAccountCode pCreditLedgerAccountCode, SpcfMoney pAmount, String pLawId, PayrollStatus pPayrollStatus) {
        assertSuccess(pProcessResult);
        FinancialTransaction financialTransaction = pProcessResult.getResult();

        assertEquals("Financial Transaction amount", pAmount, financialTransaction.getFinancialTransactionAmount());

        if(pLawId == null) {
            assertNull("Financial transaction Law Id", financialTransaction.getLaw());
        } else {
            assertEquals("Financial transaction Law Id", pLawId, financialTransaction.getLaw().getLawId());
        }
        assertEquals("Financial Transaction state", TransactionState.findTransactionState(TransactionStateCode.Executed), financialTransaction.getCurrentTransactionState());
        assertEquals("Financial Transaction state Settlement Date", pSettlementDate, financialTransaction.getSettlementDate());
        assertEquals("Financial Transaction state Settlement Type", SettlementType.Other, financialTransaction.getSettlementTypeCd());
        assertEquals("Financial Transaction state Transaction Type", pTransactionType, financialTransaction.getTransactionType());
        assertEquals("Payroll Run paycheck date", pCheckDate, financialTransaction.getPayrollRun().getPaycheckDate());
        assertEquals("Payroll Run Status", pPayrollStatus, financialTransaction.getPayrollRun().getPayrollRunStatus());

        SpcfMoney negativeAmount = (SpcfMoney) pAmount.negate();

        if(pDebitLedgerAccountCode != null && pCreditLedgerAccountCode != null) {
            DataLoadServices.assertLedgerBalances(pCompany, financialTransaction.getPayrollRun(),
                                                  new DataLoadServices.LB(pDebitLedgerAccountCode, SpcfUtils.convertToBigDecimal(negativeAmount).doubleValue()),
                                                  new DataLoadServices.LB(pCreditLedgerAccountCode, SpcfUtils.convertToBigDecimal(pAmount).doubleValue())
            );
        }

    }

}
