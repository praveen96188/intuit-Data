package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 2/2/12
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RecalculateCompanyLedgerBalancesTests {

    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2010, 1, 1));
        }
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testValidation(){
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, "12345678");
        assertEquals("Error messages", 1, processResult.getErrorMessages().size());
        assertEquals("Error message Code", "169", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message ", "Company QBDT:12345678 does not exist.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();

    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testWithSingleCompany() throws Exception {

        Long psid = 12345678L;
        String[] statesList = new String[] {"GA", "NY"};
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        Company company = assertOne(DataLoadServices.setupCompany(psid, 1, statesList, PaymentTemplateCategory.Withholding));

        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("12", "50001");
        lawAmounts.put("36", "100");
        lawAmounts.put("54", "100");
        lawAmounts.put("56", "100");
        lawAmounts.put("57", "100");

        DateDTO payrollRunDate = new DateDTO("2011-08-12");
        DataLoadServices.setPSPDate(2011, 8, 10);

        DataLoadServices.runPayrollRun(company, statesList, beginDate, payrollRunDate, true, lawAmounts, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        //Recalculate ledger balances - on same day to make we do not calculate
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company.getSourceCompanyId()));
        DomainEntitySet<LedgerBalance> ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company));
        assertEquals("LedgerBalances", 0, ledgerBalances.size());
        PayrollServices.commitUnitOfWork();

        //Offload Impound
        DataLoadServices.runOffload();

        //Offload state and federal payments
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2011, 10, 29);

        PayrollServices.beginUnitOfWork();
        //Recalculate ledger balances
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company.getSourceCompanyId()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company));
        assertEquals("LedgerBalances", 8, ledgerBalances.size());

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 913)
        );

        PayrollServices.commitUnitOfWork();

        SpcfCalendar today = PSPDate.getPSPTime().copy();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                        LedgerAccountCode.ERReturnReceivable, LedgerAccountCode.ERReturnCash, new SpcfMoney("65.00"), null, null, "NoteText - NULL"));
        PayrollServices.commitUnitOfWork();

        CalendarUtils.addBusinessDays(today, 1);
        DataLoadServices.setPSPDate(today);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company));

        assertEquals("LedgerBalances", 10, ledgerBalances.size());
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 65),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -65)
        );
        PayrollServices.commitUnitOfWork();

    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testWithTwoCompanies() throws Exception {

        Long psid = 12345678L;
        String[] statesList = new String[] {"GA", "NY"};
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        List<Company> companies = DataLoadServices.setupCompany(psid, 2, statesList, PaymentTemplateCategory.Withholding);

        assertEquals("Number of companies", 2, companies.size());
        Company company1 = companies.get(0);
        Company company2 = companies.get(1);

        DataLoadServices.claimOffer(company1, DataLoadServices.WAIVE_ALL_FEES);
        DataLoadServices.claimOffer(company2, DataLoadServices.WAIVE_ALL_FEES);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("12", "50001");
        lawAmounts.put("36", "100");
        lawAmounts.put("54", "100");
        lawAmounts.put("56", "100");
        lawAmounts.put("57", "100");

        DateDTO payrollRunDate = new DateDTO("2011-08-12");
        DataLoadServices.setPSPDate(2011, 8, 10);

        DataLoadServices.runPayrollRun(company1, statesList, beginDate, payrollRunDate, true, lawAmounts, PaymentTemplateCategory.Withholding);
        DataLoadServices.runPayrollRun(company2, statesList, beginDate, payrollRunDate, true, lawAmounts, PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        //Recalculate ledger balances - on same day to make we do not calculate
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company1.getSourceCompanyId()));
        DomainEntitySet<LedgerBalance> ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company1));
        assertEquals("company1 LedgerBalances", 0, ledgerBalances.size());
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company2.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company2));
        assertEquals("company2 LedgerBalances", 0, ledgerBalances.size());
        PayrollServices.commitUnitOfWork();

        //Offload Impound
        DataLoadServices.runOffload();

        //Offload state and federal payments
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()));

        SpcfCalendar today = PSPDate.getPSPTime().copy();

        //Submit FLA only for both companies and recalculate, make sure that we did not insert new rows as recalculate till max date in ledger date.
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company1.getSourceCompanyId(),
                                        LedgerAccountCode.ERReturnReceivable, LedgerAccountCode.ERReturnCash, new SpcfMoney("65.00"), null, null, "NoteText"));

        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company2.getSourceCompanyId(),
                                        LedgerAccountCode.TaxCurrentLiability, LedgerAccountCode.ERLiabilityOffset, new SpcfMoney("45.00"), null, null, "NoteText"));
        PayrollServices.commitUnitOfWork();

        CalendarUtils.addBusinessDays(today, 1);
        DataLoadServices.setPSPDate(today);

        PayrollServices.beginUnitOfWork();
        //Recalculate ledger balances - only for Company 1
        System.out.println("Company FK:"+company1.getId().toString());
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company1.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company1));
        assertEquals("Company 1 - LedgerBalances", 10, ledgerBalances.size());

        //Check for Company 2- make sure we did not generate for both.
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company2));
        assertEquals("Company 2 LedgerBalances", 0, ledgerBalances.size());

        //Recalculate for second company also
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company2.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company2));
        assertEquals("Company 2 - LedgerBalances", 10, ledgerBalances.size());

        //Assert Ledger Balances
        DataLoadServices.assertLedgerBalances(company1,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 913),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 65),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -65)
        );

        DataLoadServices.assertLedgerBalances(company2,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 868), // $45 deducted from this account
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 45)
        );
        PayrollServices.commitUnitOfWork();

        CalendarUtils.addBusinessDays(today, -2); // To mess with ledger balances, submitting FLA before the latest date in ledger date
        DataLoadServices.setPSPDate(today);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company1.getSourceCompanyId(),
                                        LedgerAccountCode.ERReturnReceivable, LedgerAccountCode.ERReturnCash, new SpcfMoney("65.00"), null, null, "NoteText"));

        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company2.getSourceCompanyId(),
                                        LedgerAccountCode.TaxCurrentLiability, LedgerAccountCode.ERLiabilityOffset, new SpcfMoney("45.00"), null, null, "NoteText"));
        PayrollServices.commitUnitOfWork();

        CalendarUtils.addBusinessDays(today, 2); // Move forward to the same date
        DataLoadServices.setPSPDate(today);

        PayrollServices.beginUnitOfWork();
        //Recalculate ledger balances - only for Company 1
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company1.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company1));
        assertEquals("Company 1 - LedgerBalances", 12, ledgerBalances.size());

        //Assert Ledger Balances - Company 1
        DataLoadServices.assertLedgerBalances(company1,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 913),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 130), // Updated
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -130)       // Updated
        );

        //Assert Ledger Balances - Company 2 - No changes as we recalculated only for company1
        DataLoadServices.assertLedgerBalances(company2,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 868), // No change
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 45)     // No change
        );
        //Check for Ledger records - make sure we did not generate for both.
        ledgerBalances = Application.find(LedgerBalance.class);
        assertEquals("Total LedgerBalances", 22, ledgerBalances.size());

        //Recalculate for second company also
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company2.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company2));
        assertEquals("Company 2 - LedgerBalances", 12, ledgerBalances.size());
        ledgerBalances = Application.find(LedgerBalance.class);
        assertEquals("Total LedgerBalances", 24, ledgerBalances.size());

        DataLoadServices.assertLedgerBalances(company2,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 823), // $45 more deducted from this account
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 90)     // Updated
        );
        PayrollServices.commitUnitOfWork();

        CalendarUtils.addBusinessDays(today, -1); // To mess with ledger balances, submitting FLA on the latest date in ledger date
        DataLoadServices.setPSPDate(today);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company1.getSourceCompanyId(),
                                        LedgerAccountCode.ERReturnReceivable, LedgerAccountCode.ERReturnCash, new SpcfMoney("65.00"), null, null, "NoteText"));

        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company2.getSourceCompanyId(),
                                        LedgerAccountCode.TaxCurrentLiability, LedgerAccountCode.ERLiabilityOffset, new SpcfMoney("45.00"), null, null, "NoteText"));
        PayrollServices.commitUnitOfWork();

        CalendarUtils.addBusinessDays(today, 1); // Move forward to the same date
        DataLoadServices.setPSPDate(today);

        PayrollServices.beginUnitOfWork();
        //Recalculate ledger balances - only for Company 1
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company1.getSourceCompanyId()));
        //Assert Ledger Balances - Company 1
        DataLoadServices.assertLedgerBalances(company1,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 913),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 195), // Updated
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -195)       // Updated
        );

        //Assert Ledger Balances - Company 2 - No changes as we recalculated only for company1
        DataLoadServices.assertLedgerBalances(company2,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 823), // No change
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 90)     // No change
        );

        //Recalculate for second company also
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company2.getSourceCompanyId()));
        DataLoadServices.assertLedgerBalances(company2,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 778), // Updated
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 135)     // Updated
        );
        PayrollServices.commitUnitOfWork();

        //Submit FLA for both companies and recalculate, make sure that we did not insert new rows as recalculate till max date in ledger date.
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company1.getSourceCompanyId(),
                                        LedgerAccountCode.DDCurrentLiability, LedgerAccountCode.DDCurrentCash, new SpcfMoney("65.00"), null, null, "NoteText"));

        assertSuccess(PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.QBDT, company2.getSourceCompanyId(),
                                        LedgerAccountCode.DDCurrentCash, LedgerAccountCode.DDCurrentLiability, new SpcfMoney("45.00"), null, null, "NoteText"));
        PayrollServices.commitUnitOfWork();

        CalendarUtils.addBusinessDays(today, 2); // Move forward to next date, as regular ledger balance will update till today-1, Recalc should not insert new records
        DataLoadServices.setPSPDate(today);

        PayrollServices.beginUnitOfWork();
        //Recalculate ledger balances - only for Company 1
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company1.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company1));
        assertEquals("Company 1 - LedgerBalances", 12, ledgerBalances.size()); // No new rows

        //Recalculate for second company also
        assertSuccess(PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company2.getSourceCompanyId()));
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company2));
        assertEquals("Company 2 - LedgerBalances", 12, ledgerBalances.size()); // No new rows
        PayrollServices.commitUnitOfWork();

        //Batch job has update for both companies
        BatchJobManager.runJob(BatchJobType.LedgerBalance);

        PayrollServices.beginUnitOfWork();
        //Company 1
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company1));

        assertEquals("Company 1 - LedgerBalances", 14, ledgerBalances.size()); // 2 New rows
        DataLoadServices.assertLedgerBalances(company1,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 195),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -195),
                new DataLoadServices.LB(LedgerAccountCode.DDCurrentLiability, -65),
                new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, -65)
        );
        //Company 2
        ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(company2));
        assertEquals("Company 2 - LedgerBalances", 14, ledgerBalances.size());  // 2 New rows

        DataLoadServices.assertLedgerBalances(company2,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 913),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 778),
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 135),
                new DataLoadServices.LB(LedgerAccountCode.DDCurrentLiability, 45),
                new DataLoadServices.LB(LedgerAccountCode.DDCurrentCash, 45)
        );
        PayrollServices.rollbackUnitOfWork();

    }

}
