package com.intuit.sbd.payroll.psp.batchjobs.statereports;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.util.StringUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.MA_WH;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.StateReportBase;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import static org.junit.Assert.*;

/**
 * Tests for state coupons
 *
 * @author janderson
 */
public class StateCouponsTests {

    private static final String ALL_COUPONS = "ALL-COUPONS";
    private int achTaxOffloadOffset;

    private static String expected_Dir = "PSE/batch-jobs-tests/src/test/resources/statecoupons/";

    /**
     * An array containing all supported states<br>
     * The list of states that don't have withholding<br>
     * "AK","FL","NV","NH","SD","TN","TX","WA","WY"<br>
     * Not supported yet<br>
     * "MS","IN"
     */
    String[] ALL_STATES_LIST = {"AL", "AZ", "AR", "CA", "CO", "CT", "DE", "IA", "IL", "KY", "LA", "ME", "MD", "MA", "MN", "MO", "MT", "NJ",
        "NM", "NY", "NC", "ND", "OK", "OR", "RI", "SC", "UT", "VA", "WI", "GA", "HI", "ID", "KS", "MI", "NE", "OH", "VT", "PA", "WV"};

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = (SystemParameter)Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue()).intValue();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    static String QTD_MTD_COUPON_REPORT_HEADER = "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Qtr to Date Liabilities,Qtr to Date Wages,Payroll Admin Name,Payroll Admin Phone Number";
    static String MTD_COUPON_REPORT_HEADER = "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Payroll Admin Name,Payroll Admin Phone Number";
    static String COUPON_REPORT_HEADER = "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number";
    static String RECON_REPORT_HEADER = "MA941X000004880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*";

    private void runStateCouponAsserts(String[] expectedPaymentOutputs, String[] expectedZeroPaymentOutputs, String pState) {
        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, pState, null);
    }

    /**
     * Run various asserts on the StateCouponOutput
     *
     * @param expectedPaymentOutputs The regexes to run on the
     * StateCouponOutput's payment
     * @param expectedZeroPaymentOutputs The regexes to run on the
     * StateCouponOutput's zero payment
     */
    private void runStateCouponAsserts(String[] expectedPaymentOutputs, String[] expectedZeroPaymentOutputs, String pState, DepositFrequencyCode pDepositFrequencyCode) {

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(pState, PaymentTemplateCategory.Withholding);
        Application.beginUnitOfWork();

        if (pDepositFrequencyCode == null) {
            pDepositFrequencyCode = DepositFrequencyCode.valueOf(paymentTemplate.getDefaultDepositFrequency());
        }

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(paymentTemplate.getPaymentTemplateCd(), pDepositFrequencyCode);

        StateReportOutput zeroPaymentReportOutput;

        DomainEntitySet<StateReportOutput> stateReportOutputs = Application.find(StateReportOutput.class, StateReportOutput.ReportType().equalTo(StateReportType.Coupon));

        StateReportAssoc zeroStateReportAssoc = Application.find(StateReportAssoc.class, StateReportAssoc.PaymentTemplateFrequency().equalTo(paymentTemplateFrequency)
                .And(StateReportAssoc.StateReportOutput().ReportType().equalTo(StateReportType.ZeroCoupon))).getFirst();

        if (expectedPaymentOutputs != null) {
            boolean found = false;

            // Go through each StateReportOutput object to see if it matches expectedPaymentOutputs
            for (StateReportOutput stateReportOutput : stateReportOutputs) {
                found = runChecks(expectedPaymentOutputs, stateReportOutput);

                if (found == true) {
                    break;
                }
            }

            if (!found) {
                // No StateReportOutput found with expectedPaymentOutputs, throw assert
                StringBuilder expectedPaymentBuilder = new StringBuilder();

                for (String expectedPaymentOutput : expectedPaymentOutputs) {
                    expectedPaymentBuilder.append(expectedPaymentOutput).append("\n");
                }

                StringBuilder stateReportOutputBuilder = new StringBuilder();

                for (StateReportOutput stateReportOutput : stateReportOutputs) {
                    stateReportOutputBuilder.append(StateReportTests.readStateCouponOutput(stateReportOutput)).append("\n\n");
                }

                assertTrue("Did not find expected output in " + stateReportOutputs.size() + " StateReportOutput reports Expected Output:\n"
                        + expectedPaymentBuilder + "\nStateReportOutputs:\n" + stateReportOutputBuilder, found);
            }
        }

        if (zeroStateReportAssoc == null) {
            zeroPaymentReportOutput = Application.find(StateReportOutput.class, StateReportOutput.ReportType().equalTo(StateReportType.ZeroCoupon)).getFirst();
        } else {
            zeroPaymentReportOutput = zeroStateReportAssoc.getStateReportOutput();
        }

        Application.commitUnitOfWork();

        assertNotNull("Could not find StateCouponOutput zero payment object", zeroPaymentReportOutput);

        if (expectedZeroPaymentOutputs != null && expectedZeroPaymentOutputs.length > 0) {
            runChecks(expectedZeroPaymentOutputs, zeroPaymentReportOutput);
        }
    }

    /**
     * Run various asserts on the StateCouponOutput
     *
     * @param expectedPaymentOutputs The regexes to run on the
     * StateCouponOutput's payment
     */
    private void runStateReconAsserts(String[] expectedPaymentOutputs, String pState, DepositFrequencyCode pDepositFrequencyCode) {

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(pState, PaymentTemplateCategory.Withholding);
        Application.beginUnitOfWork();

        if (pDepositFrequencyCode == null) {
            pDepositFrequencyCode = DepositFrequencyCode.valueOf(paymentTemplate.getDefaultDepositFrequency());
        }

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(paymentTemplate.getPaymentTemplateCd(), pDepositFrequencyCode);

        StateReportOutput paymentReportOutput;
        DomainEntitySet<StateReportOutput> stateReportOutputs;

        StateReportAssoc stateReportAssoc = Application.find(StateReportAssoc.class, StateReportAssoc.PaymentTemplateFrequency().equalTo(paymentTemplateFrequency)
                .And(StateReportAssoc.StateReportOutput().ReportType().equalTo(StateReportType.Recon))).getFirst();

        if (stateReportAssoc == null) {
            if (expectedPaymentOutputs != null) {
                assertEquals("Output file is empty whereas Expected file is not empty", 1, expectedPaymentOutputs.length);
                stateReportOutputs = Application.find(StateReportOutput.class, StateReportOutput.ReportType().equalTo(StateReportType.Recon));
                for (StateReportOutput stateReportOutput : stateReportOutputs) {
                    runChecks(new String[]{RECON_REPORT_HEADER}, stateReportOutput);
                }
            }
        } else {
            paymentReportOutput = stateReportAssoc.getStateReportOutput();
            assertNotNull("Could not find StateCouponOutput payment object", paymentReportOutput);
            assertTrue(runChecks(expectedPaymentOutputs, paymentReportOutput));
        }

        Application.commitUnitOfWork();

    }

    private boolean runChecks(String[] expectedOutputs, StateReportOutput reportOutput) {

        String couponOutput = StateReportTests.readStateCouponOutput(reportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");

        if (expectedOutputs.length != stateCouponArray.length) {
            return false;
        }

        for (String expectedOutput : expectedOutputs) {

            boolean found = false;

            Pattern pattern = Pattern.compile(expectedOutput);

            for (String stateCoupon : stateCouponArray) {
                Matcher matcher = pattern.matcher(stateCoupon);

                if (matcher.matches()) {
                    found = true;
                    break;
                }
            }

            if (found == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets all MoneyMovementTransactions to PaymentMethod.CheckPayment that are
     * currently in SentToAgency state
     */
    public static void setPaymentMethodToCheck() {
    	Application.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.SentToAgency);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                new Query<MoneyMovementTransaction>().Where(paymentWhereClause));

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setMoneyMovementPaymentMethod(PaymentMethod.CheckPayment);
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void test212121() {
        Pattern pattern = Pattern.compile("aa\\s*");
        Matcher matcher = pattern.matcher("aa           ");
        assertTrue(matcher.matches());
    }

    @Test
    public void testPaddedMoney() {
        StateReportBase reportUtil = new MA_WH();
        assertEquals("000000021009", reportUtil.getPaddedMoney(SpcfDecimal.createInstance("210.09"), 10, 2));
        assertEquals("000000021009", reportUtil.getPaddedMoney(SpcfDecimal.createInstance("-210.09"), 10, 2));
        assertEquals("0000000210.09", reportUtil.getPaddedMoney(SpcfDecimal.createInstance("210.09"), 10, 2, true));
        assertEquals("0000000210.09", reportUtil.getPaddedMoney(SpcfDecimal.createInstance("-210.09"), 10, 2, true));
        assertEquals(" 000000021009", reportUtil.getPaddedMoneyWithSign(SpcfDecimal.createInstance("210.09"), 10, 2));
        assertEquals("-000000021009", reportUtil.getPaddedMoneyWithSign(SpcfDecimal.createInstance("-210.09"), 10, 2));
    }

    /**
     * Creates 2 companies in MA and tests them
     */
    @Test
    public void stateReportOutputMA_Quauterly() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 18);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("MA-M941-PAYMENT"));

        DataLoadServices.setPSPDate(2011, 3, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
            "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
            "Q00000103312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",
            "Q00000203312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);
    }

    /**
     * Creates 2 companies in MA and NM, run few payrolls, void a paycheck and
     * run a fresh payroll, ETOA type Financial Transaction is created
     */
    @Test
    public void stateReportOutputMA_VoidedPaycheck() {

        DataLoadServices.updatePaymentTemplateSupportedDate("NM-CRS1-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        String[] statesList = {"MA", "NM"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NM-CRS1-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }
        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);

        DataLoadServices.setPSPDate(2011, 2, 1);

        /**
         * select a payroll
         */
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_1");
        PayrollServices.rollbackUnitOfWork();
        /**
         * void a paycheck
         */
        List<PayrollRun> payrollRuns = new ArrayList();
        payrollRuns.add(payrollRun);
        voidPayroll(companies, payrollRuns);

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NM-CRS1-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NM-CRS1-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 20);
        BatchJobManager.runJob(BatchJobType.StateReport);
        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
            "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
            "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",
            "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency("NM-CRS1-PAYMENT", DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);

        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected_StateCoupon_1");

        String[] expectedOutput = linesList.toArray(new String[0]);

        assertTrue(runCheckForNM(expectedOutput, stateReportOutput));

    }

    private List<String> getFileDataAsList(String filename) {
        File expectedFile = new File(expected_Dir, filename);
        BufferedReader expectedReader = null;
        try {
            expectedReader = new BufferedReader(new FileReader(expectedFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(expectedFile.getName() + " not found", e);
        }
        String line;
        ArrayList<String> linesList = new ArrayList<String>();
        try {
            while ((line = expectedReader.readLine()) != null) {
                if (!line.equals("/") && line.length() > 0) {
                    linesList.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading " + expectedFile.getName());
        }
        return linesList;
    }

    /**
     * Compare expected output and processed report for NM state
     */
    private boolean runCheckForNM(String expectedOutputs[], StateReportOutput reportOutput) {

        String couponOutput = StateReportTests.readStateCouponOutput(reportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");

        if (expectedOutputs.length != stateCouponArray.length) {
            return false;
        }

        for (String expectedOutput : expectedOutputs) {

            boolean found = false;
            if(expectedOutput.indexOf("<TransmissionId>")> -1 || expectedOutput.indexOf("<Timestamp>")> -1
                    || expectedOutput.indexOf("<SubmissionId>")> -1 || expectedOutput.indexOf("<?xml version=")> -1)
                continue;

            Pattern pattern = Pattern.compile(expectedOutput);

            for (String stateCoupon : stateCouponArray) {
                Matcher matcher = pattern.matcher(stateCoupon);

                if (matcher.matches()) {
                    found = true;
                    break;
                }
            }

            if (found == false) {
                return false;
            }
        }

        return true;
    }

    private StateReportOutput getStateCoupon(
            PaymentTemplateFrequency expectedTemplateFrequency) {
        Application.beginUnitOfWork();

        DomainEntitySet<StateReportOutput> stateCouponOutputs = Application.find(StateReportOutput.class);
        StateReportOutput stateReportOutput = null;

        for (StateReportOutput couponOutput : stateCouponOutputs) {
            for (StateReportAssoc stateReportAssoc : couponOutput.getStateReportAssocCollection()) {
                if (stateReportAssoc.getPaymentTemplateFrequency().equals(expectedTemplateFrequency)) {
                    stateReportOutput = couponOutput;
                    break;
                }
            }
        }
        Application.commitUnitOfWork();
        return stateReportOutput;
    }

    /**
     * Creates 2 companies in MA, process few payrolls and void a check
     * afterwards, Financial Transaction of type ATOA is created
     */
    @Test
    public void stateReportOutputMA_VoidedPaycheck_afterAllPayrollRun() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);

        DataLoadServices.setPSPDate(2011, 3, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        DataLoadServices.setPSPDate(2011, 3, 10);
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_1");
        PayrollServices.rollbackUnitOfWork();

        List<PayrollRun> payrollRuns = new ArrayList();
        payrollRuns.add(payrollRun);
        voidPayroll(companies, payrollRuns);

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
            "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
            "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",
            "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

    }

    /**
     * Create 2 companies in MA and NM, run few payrolls, void a paycheck which
     * is not yet offloaded
     */
    @Test
    public void stateReportOutputMA_VoidedUnOffloadedPaycheck() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NM-CRS1-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        String[] statesList = {"MA", "NM"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NM-CRS1-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);
        
        DataLoadServices.setPSPDate(2011, 3, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NM-CRS1-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);
        }

        DataLoadServices.setPSPDate(2011, 3, 10);
        List<PayrollRun> payrollRuns = new ArrayList();
        List<Company> voidCompanies = new ArrayList();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_8");
        if (payrollRun == null) {
            payrollRun = PayrollRun.findPayrollRun(companies.get(1), "Batch_8");
            voidCompanies.add(companies.get(1));
        } else {
            voidCompanies.add(companies.get(0));
        }
        payrollRuns.add(payrollRun);
        PayrollServices.rollbackUnitOfWork();

        voidPayroll(voidCompanies, payrollRuns);

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        DataLoadServices.setPSPDate(2011, 4, 20);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
            "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
            "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",
            "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency("NM-CRS1-PAYMENT", DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);

        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected_StateCoupon_2");

        String[] expectedOutput = linesList.toArray(new String[0]);

        assertTrue(runCheckForNM(expectedOutput, stateReportOutput));
    }

    /**
     * Creates 2 companies in MA, process few payrolls , void 2 paychecks , run
     * fresh payrolls
     */
    @Test
    public void stateReportOutputMA_2VoidedPaycheck() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }
        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);

        DataLoadServices.setPSPDate(2011, 2, 1);
        List<PayrollRun> payrollRuns = new ArrayList();
        List<Company> voidCompanies = new ArrayList();
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_1");
        payrollRuns.add(payrollRun);
        voidCompanies.add(companies.get(0));
        boolean bothPayrollSameCompapny = true;
        payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_2");
        if (payrollRun == null) {
            payrollRun = PayrollRun.findPayrollRun(companies.get(1), "Batch_2");
            voidCompanies.add(companies.get(1));
            bothPayrollSameCompapny = false;
        } else {
            voidCompanies.add(companies.get(0));
        }
        payrollRuns.add(payrollRun);
        PayrollServices.rollbackUnitOfWork();

        voidPayroll(voidCompanies, payrollRuns);

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);
        String[] expectedPaymentOutputs;
        if (bothPayrollSameCompapny) {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000016800000000016800 000000000000000000016800 000000000000000000000000000000000000000000000000  NN\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};
            expectedPaymentOutputs = temp;
        } else {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",};
            expectedPaymentOutputs = temp;
        }

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

    }

    /**
     * Create 2 companies in MA, run few payrolls , void 2 paychecks
     */
    @Test
    public void stateReportOutputMA_2VoidedPaycheck_afterAllPayrollRun() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);
        
        DataLoadServices.setPSPDate(2011, 3, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        DataLoadServices.setPSPDate(2011, 3, 10);
        List<PayrollRun> payrollRuns = new ArrayList();
        List<Company> voidCompanies = new ArrayList();
        PayrollServices.beginUnitOfWork();
        /**
         * Add first payroll for voiding
         */
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_1");
        payrollRuns.add(payrollRun);
        voidCompanies.add(companies.get(0));
        boolean bothPayrollSameCompapny = true;
        /**
         * Adding second payroll for voiding
         */
        payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_4");
        if (payrollRun == null) {
            payrollRun = PayrollRun.findPayrollRun(companies.get(1), "Batch_4");
            voidCompanies.add(companies.get(1));
            bothPayrollSameCompapny = false;
        } else {
            voidCompanies.add(companies.get(0));
        }
        payrollRuns.add(payrollRun);
        PayrollServices.rollbackUnitOfWork();

        voidPayroll(voidCompanies, payrollRuns);

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);
        String[] expectedPaymentOutputs;
        if (bothPayrollSameCompapny) {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000016800000000016800 000000000000000000016800 000000000000000000000000000000000000000000000000  NN\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};
            expectedPaymentOutputs = temp;
        } else {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",};
            expectedPaymentOutputs = temp;
        }

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

    }

    @Test
    public void stateReportOutputMA_2VoidedPaycheck_differentTimes() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);
        
        DataLoadServices.setPSPDate(2011, 3, 1);
        List<PayrollRun> payrollRuns = new ArrayList();
        List<Company> voidCompanies = new ArrayList();
        PayrollServices.beginUnitOfWork();
        /**
         * Add first payroll for voiding
         */
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_1");
        payrollRuns.add(payrollRun);
        voidCompanies.add(companies.get(0));
        PayrollServices.rollbackUnitOfWork();

        voidPayroll(voidCompanies, payrollRuns);

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        DataLoadServices.setPSPDate(2011, 3, 10);
        payrollRuns = new ArrayList();
        voidCompanies = new ArrayList();
        PayrollServices.beginUnitOfWork();
        boolean bothPayrollSameCompapny = true;
        /**
         * Adding second payroll for voiding
         */
        payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_4");
        if (payrollRun == null) {
            payrollRun = PayrollRun.findPayrollRun(companies.get(1), "Batch_4");
            voidCompanies.add(companies.get(1));
            bothPayrollSameCompapny = false;
        } else {
            voidCompanies.add(companies.get(0));
        }
        payrollRuns.add(payrollRun);
        PayrollServices.rollbackUnitOfWork();

        voidPayroll(voidCompanies, payrollRuns);

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);
        String[] expectedPaymentOutputs;
        if (bothPayrollSameCompapny) {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000016800000000016800 000000000000000000016800 000000000000000000000000000000000000000000000000  NN\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};
            expectedPaymentOutputs = temp;
        } else {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",
                "Q00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000018900000000018900 000000000000000000018900 000000000000000000000000000000000000000000000000  NN\\s*",};
            expectedPaymentOutputs = temp;
        }

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

    }

    @Test
    public void stateReportOutputMA_VoidedPaycheck_afterAllPayrollRunWithATO() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);
        
        DataLoadServices.setPSPDate(2011, 3, 20);
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_1");
        PayrollServices.rollbackUnitOfWork();

        List<PayrollRun> payrollRuns = new ArrayList();
        payrollRuns.add(payrollRun);
        voidPayroll(companies, payrollRuns);

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
            "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
            "D00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000010500000000012600 000000000000000000010500-000000002100000000000000000000000000000000002100R NN\\s*",
            "D00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000012600000000012600 000000000000000000012600 000000000000000000000000000000000000000000000000  NN\\s*",};

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

    }

    @Test
    public void stateReportOutputMA_2VoidedPaycheck_ATO() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }
        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 13);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2011, 1, 18);
        SpcfCalendar taxInitDate1 = SpcfCalendar.createInstance(2011, 1, 20, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate1, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate2 = SpcfCalendar.createInstance(2011, 1, 26, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate2, -achTaxOffloadOffset);
        SpcfCalendar taxInitDate3 = SpcfCalendar.createInstance(2011, 2, 15, 1, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitDate3, -achTaxOffloadOffset);
        DataLoadServices.runOffloadTaxPayments(taxInitDate1);
        DataLoadServices.runOffloadTaxPayments(taxInitDate2);
        DataLoadServices.runOffloadTaxPayments(taxInitDate3);
        
        DataLoadServices.setPSPDate(2011, 2, 1);
        List<PayrollRun> payrollRuns = new ArrayList();
        List<Company> voidCompanies = new ArrayList();
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_1");
        payrollRuns.add(payrollRun);
        voidCompanies.add(companies.get(0));
        boolean bothPayrollSameCompapny = true;
        payrollRun = PayrollRun.findPayrollRun(companies.get(0), "Batch_2");
        if (payrollRun == null) {
            payrollRun = PayrollRun.findPayrollRun(companies.get(1), "Batch_2");
            voidCompanies.add(companies.get(1));
            bothPayrollSameCompapny = false;
        } else {
            voidCompanies.add(companies.get(0));
        }
        payrollRuns.add(payrollRun);
        PayrollServices.rollbackUnitOfWork();

        voidPayroll(voidCompanies, payrollRuns);

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);
        String[] expectedPaymentOutputs;
        if (bothPayrollSameCompapny) {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "D00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000008400000000012600 000000000000000000008400-000000004200000000000000000000000000000000004200R NN\\s*",
                "D00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000012600000000012600 000000000000000000012600 000000000000000000000000000000000000000000000000  NN\\s*",};
            expectedPaymentOutputs = temp;
        } else {
            String[] temp = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "D00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000010500000000012600 000000000000000000010500-000000002100000000000000000000000000000000002100R NN\\s*",
                "D00000\\d03312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000010500000000012600 000000000000000000010500-000000002100000000000000000000000000000000002100R NN\\s*",};
            expectedPaymentOutputs = temp;
        }

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);

    }

    
    @Test
    public void stateReportOutputMA_QuauterMonthlyWithout_Offloading() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 3, 1);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);


            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }


        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
                "D00000103312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000000000 000000000000000000021000 000000021000000000000000000000021000000000000000C NN\\s*",
                "D00000203312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000000000 000000000000000000021000 000000021000000000000000000000021000000000000000C NN\\s*",
        };

        String[] monthlyExpectedPaymentOutputs = {
                "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
        };

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);
    }
    
    private void voidPayroll(List<Company> pCompanies, List<PayrollRun> pPayrollRuns) {
        for (int i = 0; i < pPayrollRuns.size(); i++) {
            PayrollServices.beginUnitOfWork();
            Application.refresh(pPayrollRuns.get(i));
            VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
            voidPayrollDTO.setSourcePayrollRunId(pPayrollRuns.get(i).getSourcePayRunId());
            List<String> voidPaychecks = new ArrayList<String>();
            voidPaychecks.add(pPayrollRuns.get(i).getPaycheckCollection().get(0).getSourcePaycheckId());
            voidPayrollDTO.setPaycheckIdList(voidPaychecks);
            assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, pCompanies.get(i).getSourceCompanyId(), voidPayrollDTO));
            PayrollServices.commitUnitOfWork();
        }
    }

    /**
     * Creates 2 companies in MA and tests them
     */
    @Test
    public void stateReportOutputMA_QuauterMonthly() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 18);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("MA-M941-PAYMENT"));

        DataLoadServices.setPSPDate(2011, 3, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 3, 10, SpcfTimeZone.getLocalTimeZone()));
        }

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
            "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
            "D00000103312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",
            "D00000203312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000021000000000021000 000000000000000000021000 000000000000000000000000000000000000000000000000  NN\\s*",};

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);
    }

    /**
     * Creates 2 companies in MA and tests them
     */
    @Test
    public void stateReportOutputMA_Monthly() {
        String[] statesList = {"MA"};
        DataLoadServices.reinitialize();
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-15"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-18"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 18);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("MA-M941-PAYMENT"));

        DataLoadServices.setPSPDate(2011, 3, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-1"), false);

            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 3, 5, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 3, 10, SpcfTimeZone.getLocalTimeZone()));
        }

        DataLoadServices.setPSPDate(2011, 4, 27);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedPaymentOutputs = {
            "MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",};

        String[] monthlyExpectedPaymentOutputs = {
            "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*",
            "M00000103312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000008400000000000000 000000000000000000008400 000000008400000000000000000000008400000000000000  NN\\s*",
            "M00000203312011\\d*  TEST_COMPANY_\\d*                Y0000000002000000008400000000000000 000000000000000000008400 000000008400000000000000000000008400000000000000  NN\\s*",};

        runStateReconAsserts(expectedPaymentOutputs, statesList[0], DepositFrequencyCode.QUARTERLY);
        runStateReconAsserts(monthlyExpectedPaymentOutputs, statesList[0], DepositFrequencyCode.MONTHLY);
    }

    /* Creates 2 companies in MI and tests them */
    /**
     * Creates 2 companies in VA and tests them
     */
    @Test
    public void stateReportOutputVA() {
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in VA, runs 2 payrolls, runs 2 batch jobs and tests
     * them
     */
    @Test
    public void stateReportOutputVAMultipleReports() {
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 2, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-02-06"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();
        
        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);

        BatchJobManager.runJob(BatchJobType.StateReport, "20110209", ALL_COUPONS);

        Application.beginUnitOfWork();
        DomainEntitySet<StateReportOutput> stateReportOutputs = Application.find(StateReportOutput.class);
        assertEquals("Not all state reports were found.", 8, stateReportOutputs.size());
        Application.rollbackUnitOfWork();

    }

    /**
     * Creates 2 companies in VA with commas in their legal name and tests them
     */
    @Test
    public void stateReportOutputVAWithComma() {
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        Application.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            company.setLegalName(company.getLegalName() + ", LLC");
        }
        Application.commitUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);

        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d* LLC\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d* LLC\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in VA, adds payments in previous quarter and tests
     * them
     */
    @Test
    public void stateReportOutputVAQuarterly() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2010, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2010-12-30"), false);
        }

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);
        
        String aid = DataLoadServices.getAIDRequirement("VA-VA15-PAYMENT").getExample();
        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in VA, adds liability adjustments and tests them
     */
    @Test
    public void stateReportOutputVAAdjustments() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2010, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2010-12-30"), false);
        }

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            for (PayrollRun payrollRun : PayrollRun.findPayrollRuns(company)) {
                LiabilityAdjustment liabilityAdjustment = new LiabilityAdjustment();
                liabilityAdjustment.setAmount(new SpcfMoney("1000"));
                liabilityAdjustment.setCompany(company);
                liabilityAdjustment.setTaxableWages(new SpcfMoney("2000"));
                liabilityAdjustment.setTotalWages(new SpcfMoney("3000"));
                liabilityAdjustment.setLaw(assertOne(Law.findWithholdingLawForTemplate("VA-VA15-PAYMENT")));
                liabilityAdjustment.setPayrollRun(payrollRun);
                Application.save(liabilityAdjustment);
            }
        }
        PayrollServices.commitUnitOfWork();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in VA, runs a payroll, marks as sent, runs another
     * payroll to get multiple MMTs
     */
    @Test
    public void stateReportOutputVAMultipleMMTs() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"192.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"192.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"192.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"192.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in VA with zero payments and tests that they do not
     * output
     */
    @Test
    public void stateReportOutputVAZero() {
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        zeroAllMMTs();

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110110", ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in CO with zero payments and tests them
     */
    @Test
    public void stateReportOutputCOZero() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = {"CO"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2010, 12, 19, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2010-12-23"), false);
        }

        zeroAllMMTs();

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        BatchJobManager.runJob(BatchJobType.StateReport, "20101220", ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"CO-DR1094-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"4\",=\"2010\",=\"12/01/2010\",=\"12/31/2010\",=\"01/18/2011\",=\"CO Personal Income Tax Withholding\",=\"14.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"CO-DR1094-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"4\",=\"2010\",=\"12/01/2010\",=\"12/31/2010\",=\"01/18/2011\",=\"CO Personal Income Tax Withholding\",=\"14.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in IA and checks their BEN number
     */
    @Test
    public void stateReportOutputIA() {
        String[] statesList = {"IA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                "IA-44105-PAYMENT", DepositFrequencyCode.MONTHLY);
        StateReportTests.addBIN(paymentTemplateFrequency.getPaymentTemplate());

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110214" : "20110211";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);


        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"IA-44105-PAYMENT\",=\"\\d{2}-\\d{7}\\d{3}+\",=\"12345678\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"28.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"IA Income Tax Withholding\",=\"28.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"IA-44105-PAYMENT\",=\"\\d{2}-\\d{7}\\d{3}+\",=\"12345678\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"28.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"IA Income Tax Withholding\",=\"28.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    private void zeroAllMMTs() {
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class);

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setMoneyMovementTransactionAmount(SpcfMoney.ZERO);
        }
        Application.commitUnitOfWork();
    }

    /**
     * Creates 2 companies in VA and tests before, during, and after the
     * initiation date
     */
    @Test
    public void stateReportOutputVABeforeAndAfter() {
        String[] statesList = {"VA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();
        
        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110110" : "20110107";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);

        Application.beginUnitOfWork();
        DomainEntitySet<StateReportOutput> stateReportOutputs = Application.find(StateReportOutput.class);

        for (StateReportOutput stateReportOutput : stateReportOutputs) {
            Application.delete(stateReportOutput);
        }
        Application.commitUnitOfWork();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        expectedPaymentOutputs = new String[]{
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        expectedZeroPaymentOutputs = new String[]{
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);

        deleteAllStateReports();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        reportTime = achTaxOffloadOffset == 1? "20110112" : "20110111";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        expectedPaymentOutputs = new String[]{
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};
        expectedZeroPaymentOutputs = new String[]{
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in KY and HI with zero payments and tests that they
     * are included in the zero report
     */
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void stateReportOutputKYAndHIZero() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = {"KY", "HI"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2010, 12, 19, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2010-12-23"), false);
        }

        zeroAllMMTs();

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110113", ALL_COUPONS);

        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Qtr to Date Liabilities,Qtr to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"HI-VP1-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"4\",=\"2010\",=\"12/01/2010\",=\"12/31/2010\",=\"01/18/2011\",=\"HI Income Tax Withholding\",=\"26.00\",,,,,,,,,=\"26.00\",=\"260.00\",=\"26.00\",=\"260.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"HI-VP1-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"4\",=\"2010\",=\"12/01/2010\",=\"12/31/2010\",=\"01/18/2011\",=\"HI Income Tax Withholding\",=\"26.00\",,,,,,,,,=\"26.00\",=\"260.00\",=\"26.00\",=\"260.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[1]);
        deleteAllStateReports();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110127", ALL_COUPONS);

        expectedPaymentOutputs = new String[]{
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};
        expectedZeroPaymentOutputs = new String[]{
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"KY-K1-PAYMENT\",=\"123456acd12\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"TWICEMONTHLY\",=\"CheckPayment\",=\"0.00\",=\"4\",=\"2010\",=\"12/16/2010\",=\"12/31/2010\",=\"01/31/2011\",=\"KY Income Tax Withholding\",=\"38.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"KY-K1-PAYMENT\",=\"123456acd12\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"TWICEMONTHLY\",=\"CheckPayment\",=\"0.00\",=\"4\",=\"2010\",=\"12/16/2010\",=\"12/31/2010\",=\"01/31/2011\",=\"KY Income Tax Withholding\",=\"38.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, statesList[0]);
    }

    /**
     * Creates 2 companies in all states
     */
    @Test
    public void allStateReportOutput() {
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, ALL_STATES_LIST, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.updateAgencyTaxpayerId(company,"CT-CTWH-PAYMENT","13579845-843");
            DataLoadServices.updateAgencyTaxpayerId(company,"SC-WH1601-PAYMENT","123456783");
            DataLoadServices.runPayrollRun(company, ALL_STATES_LIST,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();


        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String[] expectedPaymentOutputs = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
                "=\"NJ-NJ927PWH-PAYMENT\",=\"\\d{9}/\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"WEEKLY\",=\"CheckPayment\",=\"66.00\",=\"1\",=\"2011\",=\"01/02/2011\",=\"01/08/2011\",=\"01/12/2011\",=\"NJ Income Tax Withholding\",=\"66.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"CT-CTWH-PAYMENT\",=\"\\d{8}-\\d{3}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"WEEKLY\",=\"CheckPayment\",=\"16.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"CT Income Tax Withholding\",=\"16.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"NJ-NJ927PWH-PAYMENT\",=\"\\d{9}/\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"WEEKLY\",=\"CheckPayment\",=\"66.00\",=\"1\",=\"2011\",=\"01/02/2011\",=\"01/08/2011\",=\"01/12/2011\",=\"NJ Income Tax Withholding\",=\"66.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"CT-CTWH-PAYMENT\",=\"\\d{8}-\\d{3}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"WEEKLY\",=\"CheckPayment\",=\"16.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"CT Income Tax Withholding\",=\"16.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"VA-VA15-PAYMENT\",=\"\\d{2}-?[a-zA-Z0-9]\\d{8}F-?\\d{3}\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"96.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"VA Income Tax Withholding\",=\"96.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};

        String[] expectedPaymentOutputs_md = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",
                "=\"MD-MW506-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"01/07/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"MD-MW506-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"01/07/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};

        String[] expectedZeroPaymentOutputs = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, "NJ");
        runStateCouponAsserts(expectedPaymentOutputs_md, expectedZeroPaymentOutputs, "MD");
    }

    public static void main(String[] args) {
        StateCouponsTests stateCouponsTests = new StateCouponsTests();
        PayrollServicesTest.beforeEachTest();

        String mode = args[0];

        if (mode.compareToIgnoreCase("create") == 0) {
            // Example: create 30
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());
            DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());

            int numberOfCompanies = Integer.parseInt(args[1]);

            //DataLoadServices.fedTaxId = 35000;
            List<Company> companies = DataLoadServices.setupCompany(5000L, numberOfCompanies, stateCouponsTests.ALL_STATES_LIST, PaymentTemplateCategory.Withholding);

            System.out.println("Companies created");

            for (Company company : companies) {
                DataLoadServices.runPayrollRun(company, stateCouponsTests.ALL_STATES_LIST,
                        SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
            }

            StateReportTests.setStatePaymentsToSent();

            System.out.println("Created " + numberOfCompanies + " companies");
        } else if (mode.compareToIgnoreCase("report") == 0) {
            // Example: report "20110110 ALL-COUPONS"
            String reportsList = args[1];

            StateReportBatchProcess batchProcess = new StateReportBatchProcess(reportsList);
            batchProcess.createFiles();
        } else if (mode.compareToIgnoreCase("truncate") == 0) {
            // Example: truncate
            PayrollServicesTest.truncateTables();
        } else {
            System.out.println("Mode not found");
        }
    }

    /**
     * Creates 2 companies in all states with zero payments
     */
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void allStateReportOutputZero() {
        // TODO: This test is failing because the agency XML is not updated with the correct zero payment requirements
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, ALL_STATES_LIST, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, ALL_STATES_LIST,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        zeroAllMMTs();

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110211", ALL_COUPONS);

        String[] expectedPaymentOutputs_hi = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Qtr to Date Liabilities,Qtr to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"HI-VP1-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"HI Income Tax Withholding\",=\"26.00\",,,,,,,,,=\"26.00\",=\"260.00\",=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"HI-VP1-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"HI Income Tax Withholding\",=\"26.00\",,,,,,,,,=\"26.00\",=\"260.00\",=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};

        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"MO-941-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"MO Income Tax Withholding\",=\"52.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"WV-IT101-PAYMENT\",=\"\\d{4}+-\\d{4}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"WV Income Tax Withholding\",=\"104.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"NC-NC5P-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"NC Income Tax Withholding\",=\"58.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"AR-941M-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"AR Income Tax Withholding\",=\"8.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"CO-DR1094-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"CO Personal Income Tax Withholding\",=\"14.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"OK-OW9A-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"MT-MW1-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"MT Income Tax Withholding\",=\"56.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"MO-941-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"MO Income Tax Withholding\",=\"52.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"WV-IT101-PAYMENT\",=\"\\d{4}+-\\d{4}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"WV Income Tax Withholding\",=\"104.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"NC-NC5P-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"NC Income Tax Withholding\",=\"58.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"AR-941M-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"AR Income Tax Withholding\",=\"8.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"CO-DR1094-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"CO Personal Income Tax Withholding\",=\"14.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"OK-OW9A-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"MT-MW1-PAYMENT\",=\"\\d{10}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"MT Income Tax Withholding\",=\"56.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};

        runStateCouponAsserts(null, expectedZeroPaymentOutputs, ALL_STATES_LIST[0]);
        runStateCouponAsserts(expectedPaymentOutputs_hi, expectedZeroPaymentOutputs, "HI");

        BatchJobManager.runJob(BatchJobType.StateReport, "20110110", ALL_COUPONS);
        String[] expectedPaymentOutputs_md = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"MD-MW506-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/07/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"MD-MW506-PAYMENT\",=\"\\d{8}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"0.00\",=\"1\",=\"2011\",=\"01/07/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        runStateCouponAsserts(expectedPaymentOutputs_md, null, "MD");
    }

    /**
     * Creates 2 companies in OK, MD, Mo and tests them
     */
    @Test
    public void stateReportOutputOKMDMO_regular_MTD() {
        String[] statesList = {"OK", "MD", "MO"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110110" : "20110107";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String mdAid = DataLoadServices.getAIDRequirement("MD-MW506-PAYMENT").getExample();
        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"MD-MW506-PAYMENT\",=\"" + mdAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"01/06/2011\",=\"01/06/2011\",=\"01/11/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"MD-MW506-PAYMENT\",=\"" + mdAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"01/06/2011\",=\"01/06/2011\",=\"01/11/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, "MD");

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        reportTime = achTaxOffloadOffset == 1? "20110214" : "20110211";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String moAid = DataLoadServices.getAIDRequirement("MO-941-PAYMENT").getExample();
        String okAid = "\\d+";
        String[] expectedPaymentOutputs_mo = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"MO-941-PAYMENT\",=\"" + moAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"52.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"MO Income Tax Withholding\",=\"52.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"MO-941-PAYMENT\",=\"" + moAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"52.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/15/2011\",=\"MO Income Tax Withholding\",=\"52.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        runStateCouponAsserts(expectedPaymentOutputs_mo, expectedZeroPaymentOutputs, "OK");

        BatchJobManager.runJob(BatchJobType.StateReport, "20110217", ALL_COUPONS);

        String[] expectedPaymentOutputs_ok = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"OK-OW9A-PAYMENT\",=\"" + okAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/22/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"OK-OW9A-PAYMENT\",=\"" + okAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/22/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        runStateCouponAsserts(expectedPaymentOutputs_ok, expectedZeroPaymentOutputs, "OK");

    }

    /* create 2 companies in MI and Test for semiweekly only */
    @Test
    public void stateReportOutputMI_Semiweekly() {
        String[] statesList = {"MI"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);

            EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
            dto.setAgencyId("MIDOT");
            SpcfCalendar newEffectiveDate = SpcfCalendar.createInstance(2011, 1, 2);
            dto.setEffectiveDate(newEffectiveDate);
            dto.setPaymentTemplateCd("MI-MW106-PAYMENT");
            dto.setPaymentFrequencyId(DepositFrequencyCode.SEMIWEEKLY);
            PayrollServices.beginUnitOfWork();
            PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, company.getSourceCompanyId(), dto);
            PayrollServices.commitUnitOfWork();
        }

        StateReportTests.setStatePaymentsToSent();

        //This is not valid test anymore. CheckPayment & ACH have 2 different offsets.
        String reportTime = achTaxOffloadOffset == 1? "20110111" : "20110110";
        BatchJobManager.runJob(BatchJobType.StateReport, reportTime, ALL_COUPONS);

        String mdAid = DataLoadServices.getAIDRequirement("MI-MW106-PAYMENT").getExample();
        String[] expectedPaymentOutputs = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",
                "=\"MI-MW106-PAYMENT\",=\"00-\\d{7}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"ACHCredit\",=\"48.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"MI-MW106-PAYMENT\",=\"00-\\d{7}+\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"ACHCredit\",=\"48.00\",=\"1\",=\"2011\",=\"01/05/2011\",=\"01/07/2011\",=\"01/12/2011\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
        String[] expectedZeroPaymentOutputs = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, "MI");
    }

    /*Company with monthly should not get effected*/
    @Test
    public void stateReportOutputMI_Monthly() {
        String[] statesList = {"MI"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        for (Company company : companies) {

            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);

        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110110", ALL_COUPONS);

        String mdAid = DataLoadServices.getAIDRequirement("MI-MW106-PAYMENT").getExample();
        String[] expectedPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",};
        String[] expectedZeroPaymentOutputs = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, "MI");
    }

    /**
     * Creates 2 companies in OK, MD, Mo and tests them
     */
    @Test
    public void stateReportOutputOKMDMO_MTD() {
        String[] statesList = {"OK", "MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-11"), false);
        }
        DataLoadServices.setPSPDate(2011, 2, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "OK-OW9A-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, PSPDate.getPSPTime());
        }
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-02-06"), false);
        }

        StateReportTests.setStatePaymentsToSent();
        setPaymentMethodToCheck();

        //Update all MMTs initiation date to same date, so that all different scenario payments are processed in one batch
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                in("OK-OW9A-PAYMENT", "MD-MW506-PAYMENT"));
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.updateInitiationDate(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110107", ALL_COUPONS);

        String okAid = "\\d+";
        String mdAid = DataLoadServices.getAIDRequirement("MD-MW506-PAYMENT").getExample();
        String[] expectedPaymentOutputs_ok_mo = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Month to Date Liabilities,Month to Date Wages,Payroll Admin Name,Payroll Admin Phone Number",
                "=\"MD-MW506-PAYMENT\",=\"" + mdAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"01/11/2011\",=\"01/11/2011\",=\"01/14/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"MD-MW506-PAYMENT\",=\"" + mdAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"02/06/2011\",=\"02/06/2011\",=\"02/09/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"OK-OW9A-PAYMENT\",=\"" + okAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2011\",=\"02/05/2011\",=\"02/08/2011\",=\"02/11/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"MD-MW506-PAYMENT\",=\"" + mdAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"01/11/2011\",=\"01/11/2011\",=\"01/14/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"MD-MW506-PAYMENT\",=\"" + mdAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"ACCELERATED\",=\"CheckPayment\",=\"44.00\",=\"1\",=\"2011\",=\"02/06/2011\",=\"02/06/2011\",=\"02/09/2011\",=\"MD Income Tax Withholding\",=\"44.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"OK-OW9A-PAYMENT\",=\"" + okAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2011\",=\"02/05/2011\",=\"02/08/2011\",=\"02/11/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"0.00\",=\"0.00\",=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\","
                };
        String[] expectedZeroPaymentOutputs = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",};

        String[] expectedPaymentOutputs = {
                "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
                "=\"OK-OW9A-PAYMENT\",=\"" + okAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/22/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
                "=\"OK-OW9A-PAYMENT\",=\"" + okAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2011\",=\"01/01/2011\",=\"01/31/2011\",=\"02/22/2011\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",};
       // runStateCouponAsserts(expectedPaymentOutputs, expectedZeroPaymentOutputs, "MO", DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedPaymentOutputs_ok_mo, expectedZeroPaymentOutputs, "OK", DepositFrequencyCode.SEMIWEEKLY);
    }

    @Test
    public void stateReportOutputMO_QM() {
        String[] statesList = {"MO"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(2011, 1, 1);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MO-941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, PSPDate.getPSPTime());
            DataLoadServices.updateAgencyTaxpayerId(company,"MO-941-PAYMENT","13572468");
        }
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-06"), false);
        }

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-13"), false);
        }


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                in("MO-941-PAYMENT"));
        System.out.println("moneyMovementTransactions: " + mmts.size());

        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 02, 01);

        BatchJobManager.runJob(BatchJobType.StateReport);
        SpcfCalendar transactionDate = PSPDate.getPSPTime().copy();
        SpcfCalendar fileMonth = PSPDate.getPSPTime().copy();
        fileMonth.addMonths(-1);

        //CompanyAgencyPaymentTemplate stateTaxId = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companies.get(0), PaymentTemplate.findPaymentTemplate(MOQM_PMT_TEMPLATE));

        String expectedPaymentOutput = "B" + transactionDate.format("yyyyMMdd") + "Computing Resource I" + "tax_eservice@intuit.com                            " + StringUtil.newLine()
                + "D" + transactionDate.format("yyyyMMdd") + mmts.get(0).getAgencyTaxpayerId() + transactionDate.format("yyyy") + fileMonth.format("MM") + "00000010400"+"00000000000"+"00000000000"+"00000010400             \n";

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate("MO", PaymentTemplateCategory.Withholding);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(paymentTemplate.getPaymentTemplateCd(), DepositFrequencyCode.QUARTERMONTHLY);

        StateReportOutput moQmPaymentReportOutput;

        StateReportAssoc stateReportAssoc = Application.find(StateReportAssoc.class, StateReportAssoc.PaymentTemplateFrequency().equalTo(paymentTemplateFrequency)
                .And(StateReportAssoc.StateReportOutput().ReportType().equalTo(StateReportType.Coupon))).getFirst();

        moQmPaymentReportOutput = stateReportAssoc.getStateReportOutput();
        String couponOutput = StateReportTests.readStateCouponOutput(moQmPaymentReportOutput);

        assertEquals(expectedPaymentOutput,couponOutput);
    }


    @Test
    @Ignore
    public void allStateReportOutputWithMOQM() {
        List<Company> companies = DataLoadServices.setupCompany(1L, 3, ALL_STATES_LIST, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MO-941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, PSPDate.getPSPTime());
            DataLoadServices.updateAgencyTaxpayerId(company,"MO-941-PAYMENT","13572468");
            DataLoadServices.updateAgencyTaxpayerId(company,"CT-CTWH-PAYMENT","13579845-843");
            DataLoadServices.updateAgencyTaxpayerId(company,"SC-WH1601-PAYMENT","123456783");
            DataLoadServices.runPayrollRun(company, ALL_STATES_LIST,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-03"), false);
            DataLoadServices.runPayrollRun(company, ALL_STATES_LIST,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-11"), false);
            DataLoadServices.runPayrollRun(company, ALL_STATES_LIST,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-19"), false);
            DataLoadServices.runPayrollRun(company, ALL_STATES_LIST,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-25"), false);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                notIn("MO-941-PAYMENT"));
        System.out.println("moneyMovementTransactions: " + mmts.size());

        DomainEntitySet<MoneyMovementTransaction> zerommtsforMO = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                in("MO-941-PAYMENT"));

        int count = 0;
        for (MoneyMovementTransaction moneyMovementTransaction : zerommtsforMO) {

            if(count%2==0){
                moneyMovementTransaction.setMoneyMovementTransactionAmount(SpcfMoney.ZERO);
            }
            count++;

        }

        int counts = 0;
        for (MoneyMovementTransaction moneyMovementTransaction : mmts) {

            if(counts % 2==0){
                moneyMovementTransaction.setMoneyMovementTransactionAmount(SpcfMoney.ZERO);
            }
            counts++;
        }

        for (MoneyMovementTransaction moneyMovementTransaction : mmts) {
            moneyMovementTransaction.updateInitiationDate(SpcfCalendar.createInstance(2011, 02, 01, SpcfTimeZone.getLocalTimeZone()));
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 02, 01);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println("Starting batch job for date: " + PSPDate.getPSPTime().toString());
        BatchJobManager.runJob(BatchJobType.StateReport);

    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void stateReportWithMultiplePaymentsWithInitDate() {
        String[] statesList = {"CA"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        DataLoadServices.setPSPDate(2011, 12, 13);
        for (Company company : companies) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-12-09"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-12-10"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-12-11"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-12-12"), false);
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-12-13"), false);
        }

        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction mmt : MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("CA-PITSDI-PAYMENT").find()) {
            PayrollServices.paymentManager.changePaymentMethod(mmt.getCompany().getSourceSystemCd(), mmt.getCompany().getSourceCompanyId(), mmt.getId(), PaymentMethod.CheckPayment);
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 20);
        BatchJobManager.runJob(BatchJobType.StateReport);

        String caAid = DataLoadServices.getAIDRequirement("CA-PITSDI-PAYMENT").getExample();
        String[] expectedPaymentOutputs_ca = {
            "State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number",
            "=\"CA-PIT/SDI\",=\"" + caAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"146.00\",=\"4\",=\"2011\",=\"12/07/2011\",=\"12/09/2011\",=\"12/14/2011\",=\"CA State Disability Insurance\",=\"134.00\",=\"CA Personal Income Tax\",=\"12.00\",,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"CA-PIT/SDI\",=\"" + caAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"438.00\",=\"4\",=\"2011\",=\"12/10/2011\",=\"12/13/2011\",=\"12/16/2011\",=\"CA State Disability Insurance\",=\"402.00\",=\"CA Personal Income Tax\",=\"36.00\",,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"CA-PIT/SDI\",=\"" + caAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"146.00\",=\"4\",=\"2011\",=\"12/07/2011\",=\"12/09/2011\",=\"12/14/2011\",=\"CA State Disability Insurance\",=\"134.00\",=\"CA Personal Income Tax\",=\"12.00\",,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\",",
            "=\"CA-PIT/SDI\",=\"" + caAid + "\",=\"\",=\"00-\\d{7}+\",=\"\\d*\",=\"TEST_COMPANY_\\d*\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"SEMIWEEKLY\",=\"CheckPayment\",=\"438.00\",=\"4\",=\"2011\",=\"12/10/2011\",=\"12/13/2011\",=\"12/16/2011\",=\"CA State Disability Insurance\",=\"402.00\",=\"CA Personal Income Tax\",=\"36.00\",,,,,,,=\"Johnny PayrollAdmin\",=\"\\(775\\) 333-3333\","
        };
        runStateCouponAsserts(expectedPaymentOutputs_ca, null, "CA", DepositFrequencyCode.SEMIWEEKLY);
    }

    //ACHCredit for MO-MODES-PAYMENT  different initiation date 1day or 2days offset
    @Test
    public void stateReportWithMOACHCredit() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = {"MO"};

        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2014, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2014, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MO-MODES-PAYMENT", SpcfCalendar.createInstance(2014, 1, 1));

        DataLoadServices.reinitialize();
        String psid = "166666666";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654331", false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("MO");
        DataLoadServices.addCompanyLawsWithAgencyId("22-55555-1-22", company, "MO", lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("107", "10");

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MO-MODES-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 11, 1));
        DataLoadServices.runPayrollRun(company, statesList,
                SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2015-01-06"), false, lawAmounts, PaymentTemplateCategory.Withholding);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                in("MO-MODES-PAYMENT").And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit)));

        SpcfCalendar taxSettlementDate  = moneyMovementTransactions.get(0).getSettlementDate();
        CalendarUtils.addBusinessDays(taxSettlementDate, -achTaxOffloadOffset);

        assertEquals("Initiation Day:", moneyMovementTransactions.get(0).getInitiationDate().getDay(), taxSettlementDate.getDay());
        assertEquals("Initiation Date:", moneyMovementTransactions.get(0).getInitiationDate(), taxSettlementDate);
    }

    /**
     * Deletes all StateReportOutputs
     */
    public static void deleteAllStateReports() {
        DomainEntitySet<StateReportOutput> stateReportOutputs;
        Application.beginUnitOfWork();
        DomainEntitySet<StateReportAssoc> stateReportAssocs = Application.find(StateReportAssoc.class);

        for (StateReportAssoc stateReportAssoc : stateReportAssocs) {
            Application.delete(stateReportAssoc);
        }

        stateReportOutputs = Application.find(StateReportOutput.class);

        for (StateReportOutput stateReportOutput : stateReportOutputs) {
            Application.delete(stateReportOutput);
        }
        Application.commitUnitOfWork();
    }

    public Integer getNamesStartswtB(List<String> lNames) {
        int count = 0;
        if (lNames != null) {
            for (int i = 0; i < lNames.size(); i++) {
                lNames.get(i);
            }

            for (String name : lNames) {
                if (name != null && (name.startsWith("B") || name.startsWith("b"))) {
                    count++;
                }
            }
        }
        return count;
    }

}
