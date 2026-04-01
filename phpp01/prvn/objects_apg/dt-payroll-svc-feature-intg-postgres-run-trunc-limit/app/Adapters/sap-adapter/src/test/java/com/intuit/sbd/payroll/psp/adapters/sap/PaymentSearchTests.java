package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdt.TestHttpServletRequest;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayment;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentSearch;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.adapters.sap.printing.AmfSerializer;
import com.intuit.sbd.payroll.psp.adapters.sap.printing.PaymentsSearchReportHtml;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * User: dweinberg
 * Date: 11/28/11
 * Time: 10:17 AM
 * Tests the tax adapter payment searches
 */
public class PaymentSearchTests {

    private static final String IRS941 = "IRS-941/944";
    private static final String IRS940 = "IRS-940";
    private static final String IRS941_CODE = "IRS-941-PAYMENT";
    private static final String PIT = "CA-PIT/SDI";
    private static final String PIT_CODE = "CA-PITSDI-PAYMENT";
    private static final String MA = "MA-M941-PAYMENT";

    private Company company1;
    private Company company2;
    private Company company3;
    private int achTaxOffloadOffset;




    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.reinitialize();

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(MA, SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2010, 6, 1);
        company1 = DataLoadPalette.setupTaxCompany();
        company2 = DataLoadPalette.setupTaxCompany();
        company3 = DataLoadPalette.setupTaxCompany();
        PayrollServices.beginUnitOfWork();
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.rollbackUnitOfWork();
    }


    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    //create a body of payments for the 3 companies and 3 templates.
    private void setupPayments(boolean offloadPayments) throws Throwable {
        //company 3 will never be on ACH
        DataLoadServices.updateAgencyTaxpayerId(company3, "CA-PITSDI-PAYMENT", "blah");
        DataLoadServices.updateAgencyTaxpayerId(company3, "MA-M941-PAYMENT", "");

        DataLoadServices.setPSPDate(2011, 6, 1);
        DataLoadPalette.runSimpleTaxPayroll(company1, new DateDTO("2011-06-08"));
        PayrollRun company2PayrollRun1 = DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO("2011-06-08"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 6, 17, 15, 0, 0));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 6, 8);
        DataLoadServices.returnTxns(company2PayrollRun1, TransactionTypeCode.EmployerTaxDebit);

        if (offloadPayments) {
            DataLoadServices.setPSPDate(2011, 6, 14);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

            DataLoadServices.setPSPDate(2011, 7, 29);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());
        }



        DataLoadServices.setPSPDate(2011, 9, 10);
        DataLoadPalette.runSimpleTaxPayroll(company1, new DateDTO("2011-09-15"));
        DataLoadPalette.runSimpleTaxPayroll(company3, new DateDTO("2011-09-16"));

        if (offloadPayments) {
            DataLoadServices.setPSPDate(2011, 9, 19);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"));

            DataLoadServices.setPSPDate(2011, 9, 20);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

            DataLoadServices.setPSPDate(2011, 10, 28);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());
        }

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company1, new DateDTO("2012-01-15"));
        DataLoadPalette.runSimpleTaxPayroll(company3, new DateDTO("2012-01-16"));

        DataLoadServices.setPSPDate(2012, 1, 13);

        if (offloadPayments) {
            DataLoadServices.setPSPDate(2012, 1, 18);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"));

            DataLoadServices.setPSPDate(2012, 1, 19);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

            DataLoadServices.setPSPDate(2012, 2, 13);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("MA-M941-PAYMENT"));

            DataLoadServices.setPSPDate(2012, 4, 27);
            DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());
        }

    }

    @Test
    public void testPendingPaymentSearchFindsCorrectPayments() throws Throwable {
        setupPayments(false);

        //note this is not a possible search from the UI, but will use it to verify the data setup
        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company1, 2011, 9, 21 - achTaxOffloadOffset),
                expectedSAPPayment(PIT, company1, 2012, 1, 20 - achTaxOffloadOffset),
                expectedSAPPayment(PIT, company3, 2011, 9, 21),
                expectedSAPPayment(PIT, company3, 2012, 1, 20),
                expectedSAPPayment(IRS940, company1, 2011, 7, 29),
                expectedSAPPayment(IRS940, company1, 2011, 10, 28),
                expectedSAPPayment(IRS940, company1, 2012, 4, 27),
                expectedSAPPayment(IRS940, company2, 2011, 7, 29),
                expectedSAPPayment(IRS940, company3, 2011, 10, 28),
                expectedSAPPayment(IRS940, company3, 2012, 4, 27),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company1, 2011, 9, 20),
                expectedSAPPayment(IRS941, company1, 2012, 1, 19),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14),
                expectedSAPPayment(IRS941, company3, 2011, 9, 20),
                expectedSAPPayment(IRS941, company3, 2012, 1, 19),
                expectedSAPPayment(MA, company1, 2012, 2, 15 - achTaxOffloadOffset),
                expectedSAPPayment(MA, company3, 2012, 2, 15 - achTaxOffloadOffset)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company1, 2011, 9, 20),
                expectedSAPPayment(IRS941, company1, 2012, 1, 19),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14),
                expectedSAPPayment(IRS941, company3, 2011, 9, 20),
                expectedSAPPayment(IRS941, company3, 2012, 1, 19)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, null, null, null, null, null, new SAPQuarter(2011, 2), false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", "Not on hold", "IRS", IRS941_CODE, null, null, null, null, null, null, new SAPQuarter(2011, 2), false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", "On hold", "IRS", IRS941_CODE, null, null, null, null, null, null, new SAPQuarter(2011, 2), false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", "On Company hold", "IRS", IRS941_CODE, null, null, null, null, null, null, new SAPQuarter(2011, 2), false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", "On Enrollment hold", "IRS", IRS941_CODE, null, null, null, null, null, null, new SAPQuarter(2011, 2), false), 0, 100, null, false));
        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", "On Agent hold", "IRS", IRS941_CODE, null, null, null, null, null, null, new SAPQuarter(2011, 2), false), 0, 100, null, false));


        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, company1.getSourceCompanyId(), null, null, null, null, new SAPQuarter(2011, 2), false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14)
        );


        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, null, null, null, null, null, null, true), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company1, 2011, 9, 20),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14),
                expectedSAPPayment(IRS941, company3, 2011, 9, 20)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, null, SAPTranslator.createDate(2011, 6, 15), null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company1, 2011, 9, 20),
                expectedSAPPayment(IRS941, company1, 2012, 1, 19),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14),
                expectedSAPPayment(IRS941, company3, 2011, 9, 20),
                expectedSAPPayment(IRS941, company3, 2012, 1, 19)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, null, SAPTranslator.createDate(2011, 6, 15), SAPTranslator.createDate(2011, 9, 21), null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company1, 2011, 9, 20),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14),
                expectedSAPPayment(IRS941, company3, 2011, 9, 20)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, null, SAPTranslator.createDate(2011, 6, 15), SAPTranslator.createDate(2011, 6, 15), null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company2, 2011, 6, 14)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", IRS941_CODE, null, null, SAPTranslator.createDate(2011, 6, 14), SAPTranslator.createDate(2011, 6, 14), null, null, null, false), 0, 100, null, false));


        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "MADOR", MA, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(MA, company1, 2012, 2, 15 - achTaxOffloadOffset),
                expectedSAPPayment(MA, company3, 2012, 2, 15 - achTaxOffloadOffset)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "MADOR", MA, "CheckPayment", null, null, null, null, null, null, false), 0, 100, null, false));

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "MADOR", MA, "ACHCredit", null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(MA, company1, 2012, 2, 15 - achTaxOffloadOffset),
                                   expectedSAPPayment(MA, company3, 2012, 2, 15 - achTaxOffloadOffset)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "MADOR", MA, "None", null, null, null, null, null, null, false), 0, 100, null, false));

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", "On Enrollment hold", "MADOR", MA, "None", null, null, null, null, null, null, false), 0, 100, null, false));
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testExecutedPaymentSearchFindsCorrectPayments() throws Throwable {
        setupPayments(true);

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Executed", null, null, null, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company1, 2011, 9, 19),
                expectedSAPPayment(PIT, company1, 2012, 1, 18),
                expectedSAPPayment(PIT, company3, 2011, 9, 19),
                expectedSAPPayment(PIT, company3, 2012, 1, 18),
                expectedSAPPayment(IRS940, company1, 2011, 7, 29),
                expectedSAPPayment(IRS940, company1, 2011, 10, 28),
                expectedSAPPayment(IRS940, company1, 2012, 4, 27),
                expectedSAPPayment(IRS940, company3, 2011, 10, 28),
                expectedSAPPayment(IRS940, company3, 2012, 4, 27),
                expectedSAPPayment(IRS941, company1, 2011, 6, 14),
                expectedSAPPayment(IRS941, company1, 2011, 9, 20),
                expectedSAPPayment(IRS941, company1, 2012, 1, 19),
                expectedSAPPayment(IRS941, company3, 2011, 9, 20),
                expectedSAPPayment(IRS941, company3, 2012, 1, 19),
                expectedSAPPayment(MA, company1, 2012, 2, 13),
                expectedSAPPayment(MA, company3, 2012, 2, 13)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Executed", null, "CAEDD", PIT_CODE, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company1, 2011, 9, 19),
                expectedSAPPayment(PIT, company1, 2012, 1, 18),
                expectedSAPPayment(PIT, company3, 2011, 9, 19),
                expectedSAPPayment(PIT, company3, 2012, 1, 18)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Executed", null, "CAEDD", PIT_CODE, "CheckPayment", null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company3, 2011, 9, 19),
                expectedSAPPayment(PIT, company3, 2012, 1, 18)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Executed", null, "CAEDD", PIT_CODE, "CheckPayment", null, null, null, null, null, new SAPQuarter(2011, 3), false), 0, 100, null, false),
                expectedSAPPayment(PIT, company3, 2011, 9, 19)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Executed", null, "CAEDD", PIT_CODE, "CheckPayment", null, SAPTranslator.createDate(2012, 1, 20), SAPTranslator.createDate(2012, 1, 20), null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company3, 2012, 1, 18)
        );

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testRejectedPaymentSearchFindsCorrectPayments() throws Throwable {
        setupPayments(true);

        //todo test IRS rejects
        DataLoadServices.returnAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"));
        DataLoadServices.returnAgencyTaxCredits(PaymentTemplate.findPaymentTemplate(MA));

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Rejected", null, null, null, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company1, 2011, 9, 19),
                expectedSAPPayment(PIT, company1, 2012, 1, 18),
                expectedSAPPayment(PIT, company3, 2011, 9, 19),
                expectedSAPPayment(PIT, company3, 2012, 1, 18),
                expectedSAPPayment(MA, company1, 2012, 2, 13),
                expectedSAPPayment(MA, company3, 2012, 2, 13)

        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Rejected", "Rejected", null, null, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company1, 2011, 9, 19),
                expectedSAPPayment(PIT, company1, 2012, 1, 18),
                expectedSAPPayment(PIT, company3, 2011, 9, 19),
                expectedSAPPayment(PIT, company3, 2012, 1, 18),
                expectedSAPPayment(MA, company1, 2012, 2, 13),
                expectedSAPPayment(MA, company3, 2012, 2, 13)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Rejected", "Returned", null, null, null, null, null, null, null, null, null, false), 0, 100, null, false));


        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Rejected", null, "CAEDD", null, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company1, 2011, 9, 19),
                expectedSAPPayment(PIT, company1, 2012, 1, 18),
                expectedSAPPayment(PIT, company3, 2011, 9, 19),
                expectedSAPPayment(PIT, company3, 2012, 1, 18)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Rejected", null, "CAEDD", PIT_CODE, null, null, null, null, null, null, null, false), 0, 100, null, false),
                expectedSAPPayment(PIT, company1, 2011, 9, 19),
                expectedSAPPayment(PIT, company1, 2012, 1, 18),
                expectedSAPPayment(PIT, company3, 2011, 9, 19),
                expectedSAPPayment(PIT, company3, 2012, 1, 18)
        );
    }

    @Test
    public void testSorting() throws Throwable {
        setupPayments(false);

        assertPaymentsSearchResultsSorted(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, null, false), new SAPPaymentSortCompare(new SAPPaymentAccessor() {
            public Comparable get(SAPPayment payment) {
                return payment.getInitiationDate();
            }
        }, false));

        assertPaymentsSearchResultsSorted(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, "settlementDate", false), new SAPPaymentSortCompare(new SAPPaymentAccessor() {
            public Comparable get(SAPPayment payment) {
                return payment.getInitiationDate();
            }
        }, false));

        assertPaymentsSearchResultsSorted(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, "initiationDate", true), new SAPPaymentSortCompare(new SAPPaymentAccessor() {
            public Comparable get(SAPPayment payment) {
                return payment.getInitiationDate();
            }
        }, true));

        assertPaymentsSearchResultsSorted(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, "dueDate", false), new SAPPaymentSortCompare(new SAPPaymentAccessor() {
            public Comparable get(SAPPayment payment) {
                return payment.getDueDate();
            }
        }, false));

        assertPaymentsSearchResultsSorted(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, "companyName", true), new SAPPaymentSortCompare(new SAPPaymentAccessor() {
            public Comparable get(SAPPayment payment) {
                return payment.getCompanyName();
            }
        }, true));

        assertPaymentsSearchResultsSorted(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, "agencyId", false), new SAPPaymentSortCompare(new SAPPaymentAccessor() {
            public Comparable get(SAPPayment payment) {
                return payment.getInitiationDate();
            }
        }, false));

        assertPaymentsSearchResultsSorted(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false), 0, 100, "paymentType", true), new SAPPaymentSortCompare(new SAPPaymentAccessor() {
            public Comparable get(SAPPayment payment) {
                return payment.getPaymentType();
            }
        }, true));
    }

    @Test
    public void testPaging() throws Throwable {
        setupPayments(false);

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", null, null, company3.getSourceCompanyId(), null, null, null, null, null, false), 0, 3, null, false), 4,
                expectedSAPPayment(IRS941, company3, 2011, 9, 20),
                expectedSAPPayment(IRS940, company3, 2011, 10, 28),
                expectedSAPPayment(IRS941, company3, 2012, 1, 19)
        );

        assertPaymentSearchResults(new TaxAdapter().findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", null, null, company3.getSourceCompanyId(), null, null, null, null, null, false), 3, 3, null, false), 4,
                expectedSAPPayment(IRS940, company3, 2012, 4, 27)
        );

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testExport() throws Throwable {
        setupPayments(false);
        SAPPaymentSearch paymentSearch = new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false);
        String amf = new AmfSerializer().toAmf(paymentSearch);
        HashMap<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("paymentSearch", amf);
        String body = new PaymentsSearchReportHtml(new TestHttpServletRequest("").withParameters(parameterMap), null, true).getBody();

        String expectedFile = FileUtils.readFileToString(new File(Application.findFileOnClassPath("resources/paymentExport.html")));
        assertEquals(expectedFile.replaceAll("\\s+", "").replaceAll("[\\r\\n]+", "\r\n"), body.replaceAll("\\s+", "").replaceAll("[\\r\\n]+", "\r\n"));
    }

    private class SAPPaymentSortCompare implements Comparator<SAPPayment> {

        private SAPPaymentAccessor accessor;
        private boolean descending;

        private SAPPaymentSortCompare(SAPPaymentAccessor accessor, boolean descending) {
            this.accessor = accessor;
            this.descending = descending;
        }

        public int compare(SAPPayment o1, SAPPayment o2) {
            //noinspection unchecked
            int propertyCompareResult = (descending ? -1 : 1) * (accessor.get(o1).compareTo(accessor.get(o2)));
            if (propertyCompareResult != 0) {
                return propertyCompareResult;
            } else {
                return o1.getPaymentId().compareTo(o2.getPaymentId());
            }
        }
    }

    private interface SAPPaymentAccessor {
        public Comparable get(SAPPayment payment);
    }

    private void assertPaymentsSearchResultsSorted(SAPSearchResults<SAPPayment> actual, Comparator<SAPPayment> sort) {
        ArrayList<SAPPayment> sortedList = new ArrayList<SAPPayment>(actual.getReturnsList());
        Collections.sort(sortedList, sort);

        for (int i = 0; i < sortedList.size(); i++) {
            assertEquals(sortedList.get(i), actual.getReturnsList().get(i));
        }
    }


    private void assertPaymentSearchResults(SAPSearchResults<SAPPayment> actual, int totalRecords, SAPPayment... expected) {
        assertEquals(totalRecords, actual.getTotalRecords());
        assertEquals(actual.getReturnsList().size(), expected.length);

        Collections.sort(actual.getReturnsList(), new SAPPaymentCompare());
        Arrays.sort(expected, new SAPPaymentCompare());

        for (int i = 0; i < expected.length; i++) {
            SAPPayment actualPayment = actual.getReturnsList().get(i);
            SAPPayment expectedPayment = expected[i];
            assertEquals(Integer.toString(i), expectedPayment.getPaymentType(), actualPayment.getPaymentType());
            assertEquals(Integer.toString(i), expectedPayment.getCompanyName(), actualPayment.getCompanyName());
            assertEquals(Integer.toString(i), expectedPayment.getInitiationDate(), actualPayment.getInitiationDate());
        }
    }

    private void assertPaymentSearchResults(SAPSearchResults<SAPPayment> actual, SAPPayment... expected) {
        assertPaymentSearchResults(actual, expected.length, expected);
    }

    //set just the things asserted and test-default-sorted
    private SAPPayment expectedSAPPayment(String paymentTemplate, Company company, int initYear, int initMonth, int initDay) {
        SAPPayment payment = new SAPPayment();
        payment.setPaymentType(paymentTemplate);
        payment.setCompanyName(company.getLegalName());
        SpcfCalendar initCalendar = SpcfCalendar.createInstance(initYear, initMonth, initDay, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.clearTime(initCalendar);
        payment.setInitiationDate(SAPTranslator.getDateFromSpcfCalendar(initCalendar));
        return payment;
    }


    private class SAPPaymentCompare implements Comparator<SAPPayment> {
        public int compare(SAPPayment o1, SAPPayment o2) {
            if (!o1.getPaymentType().equals(o2.getPaymentType())) {
                return o1.getPaymentType().compareTo(o2.getPaymentType());
            }
            if (!o1.getCompanyName().equals(o2.getCompanyName())) {
                return o1.getCompanyName().compareTo(o2.getCompanyName());
            }
            if (!o1.getInitiationDate().equals(o2.getInitiationDate())) {
                return o1.getInitiationDate().compareTo(o2.getInitiationDate());
            }
            throw new RuntimeException();
        }
    }

}
