package com.intuit.sbd.payroll.psp.batchjobs.statereports;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import y.co;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Tests to exercise state coupons
 */
public class StateReportTests {
    private static SpcfLogger logger = Application.getLogger(StateReportTests.class);

    private static final String AL_REPORT = "AL-CR4WH-PAYMENT";
    private static final String IA_REPORT = "IA-44105-PAYMENT";
    private static final String MA_REPORT = "MA-M941-PAYMENT";
    private static final String NM_REPORT = "NM-CRS1-PAYMENT";
    private static final String KY_REPORT = "KY-K1-PAYMENT";
    private static final String OK_OW9A_PAYMENT_TEMPLATE = "OK-OW9A-PAYMENT";
    private static final String MI_MIW106_PAYMENT_TEMPLATE = "MI-MW106-PAYMENT";
    private static final String NY_1MN_PAYMENT = "NY-1MN-PAYMENT";
    private static String expected_Dir = "PSE/batch-jobs-tests/src/test/resources/statecoupons/";

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    private boolean allowNegativeMmt;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        DataLoadServices.reinitialize();
        allowNegativeMmt = SystemParameter.findBooleanValue(SystemParameter.Code.ALLOW_NEGATIVE_MMT);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate(OK_OW9A_PAYMENT_TEMPLATE, PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate(MI_MIW106_PAYMENT_TEMPLATE, PSPDate.getPSPTime());
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();

        DataLoadServices.updateSystemParameter(SystemParameter.Code.ALLOW_NEGATIVE_MMT, allowNegativeMmt ? "true" : "false");
    }

    /**
     * Checks if the stateReportOutput is null or not
     * if null returns empty string
     * if not null returns the reportOutput
     *
     * @param stateReportOutput The StateCouponOutput object to read
     * @return The coupon output into a String for processing
     */
    public static String readStateCouponOutput(StateReportOutput stateReportOutput) {
        String reportOutput = getReportOutput(stateReportOutput);
        return  Objects.isNull(reportOutput) ? StringUtils.EMPTY : reportOutput;
    }

    /**
     * Run various asserts on the StateCouponOutput
     *
     * @param expectedOutputs           The regexes to run on the StateCouponOutput
     * @param expectedTemplateFrequency The PaymentTemplateFrequency to test
     */
    private void runStateCouponAsserts(String[] expectedOutputs,
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
        assertNotNull("Could not find StateCouponOutput object", stateReportOutput);
        runStateCouponAsserts(expectedOutputs, stateReportOutput);
    }

    /**
     * Run various asserts on the StateCouponOutput
     *
     * @param expectedOutputs   The regexes to run on the StateCouponOutput
     * @param stateReportOutput The report to assert against
     */
    private void runStateCouponAsserts(String[] expectedOutputs,
                                       StateReportOutput stateReportOutput) {

   String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        assertEquals("StateCouponOutput length does not match", expectedOutputs.length, stateCouponArray.length);

        for (String expectedOutput : expectedOutputs) {

            boolean found = false;

            Pattern pattern = Pattern.compile(expectedOutput);

            for (String stateCoupon : stateCouponArray) {
                Matcher matcher = pattern.matcher(stateCoupon);

                if (matcher.matches()) {
                    found = true;
                }
            }

            assertTrue("Did not find expected output:\n" + expectedOutput + "\nIn output:\n" + couponOutput, found);
        }
    }

    /**
     * Sets all MoneyMovementTransactions to SentToAgency that are currently in ReadyToSend state
     */
    public static void setStatePaymentsToSent() {
        Application.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause));

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.SentToAgency);
            moneyMovementTransaction.setStatus(PaymentStatus.Executed);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Sets all MoneyMovementTransactions to ReadyToSend that are currently in SentToAgency state
     */
    public static void setStatePaymentsToReadyToSend() {
        Application.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.SentToAgency);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause));

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Sets all MoneyMovementTransactions that are SentToAgency to a negative amount
     */
    public static void setStatePaymentsToNegative() {
        DataLoadServices.updateSystemParameter(SystemParameter.Code.ALLOW_NEGATIVE_MMT, "true");

        Application.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.SentToAgency);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause));

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setMoneyMovementTransactionAmount(new SpcfMoney("-20.00"));
        }

        Application.commitUnitOfWork();
    }

    /**
     * Runs a happy path on AL's StateCouponOutput
     */
    @Test
    public void happyPathAL() {
        String[] statesList = new String[]{"AL"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();

        //Updating all mmts for AL-CR4W-PAYMENT to more than 3 business date to current date
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                in(AL_REPORT));
        for (MoneyMovementTransaction moneyMovementTransaction : mmts) {
            moneyMovementTransaction.updateInitiationDate(SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()));
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", AL_REPORT + ",MONTHLY");

        String[] expectedOutputs = {
                "\\w{10},20110131,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  ",
                "\\w{10},20110131,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  "
        };

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                AL_REPORT, DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    @Test
    public void testAllCoupons() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"OK"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.CheckPayment);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2012-01-03"), false);
        }

        setStatePaymentsToSent();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 2, 16, SpcfTimeZone.getLocalTimeZone()));

        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedOutputs = {
                Pattern.quote("State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number"),
                Pattern.quote("=\"OK-OW9A-PAYMENT\",=\"1000000001\",=\"\",=\"00-0000001\",=\"1\",=\"TEST_COMPANY_1\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\","),
                Pattern.quote("=\"OK-OW9A-PAYMENT\",=\"2000000002\",=\"\",=\"00-0000002\",=\"2\",=\"TEST_COMPANY_2\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"76.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"OK Income Tax Withholding\",=\"76.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\",")
        };

        Application.beginUnitOfWork();

        Criterion<StateReportAssoc> stateReportAssocCriterion = StateReportAssoc.PaymentTemplateFrequency().PaymentTemplate().PaymentTemplateCd().equalTo("OK-OW9A-PAYMENT");
        StateReportAssoc stateReportAssoc = Application.find(StateReportAssoc.class, new Query<StateReportAssoc>().Where(stateReportAssocCriterion)).getFirst();
        runStateCouponAsserts(expectedOutputs, stateReportAssoc.getStateReportOutput());
        Application.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testAllCouponsMICheck() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"MI"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.CheckPayment);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2012-01-03"), false);
        }

        setStatePaymentsToSent();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 2, 16, SpcfTimeZone.getLocalTimeZone()));

        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedOutputs = {
                Pattern.quote("State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number"),
                Pattern.quote("=\"MI-MW106-PAYMENT\",=\"1000000001\",=\"\",=\"00-0000001\",=\"1\",=\"TEST_COMPANY_1\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"48.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\","),
                Pattern.quote("=\"MI-MW106-PAYMENT\",=\"2000000002\",=\"\",=\"00-0000002\",=\"2\",=\"TEST_COMPANY_2\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"48.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\",")
        };

        Application.beginUnitOfWork();

        Criterion<StateReportAssoc> stateReportAssocCriterion = StateReportAssoc.PaymentTemplateFrequency().PaymentTemplate().PaymentTemplateCd().equalTo("MI-MW106-PAYMENT");
        StateReportAssoc stateReportAssoc = Application.find(StateReportAssoc.class, new Query<StateReportAssoc>().Where(stateReportAssocCriterion)).getFirst();

        runStateCouponAsserts(expectedOutputs, stateReportAssoc.getStateReportOutput());
        Application.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testAllCouponsMIACHCreditAndCheck() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"MI"};
        List<Company> companiesWithACHCredit = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        for (Company company : companiesWithACHCredit) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2012-01-03"), false);
        }
        setStatePaymentsToSent();
        List<Company> companiesWithCheck = DataLoadServices.setupCompany(3L, 2, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.CheckPayment);
        for (Company company : companiesWithCheck) {
            if (!companiesWithACHCredit.contains(company)) {
                DataLoadServices.runPayrollRun(company, statesList,
                                               SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2012-01-03"), false);
            }
        }
        setStatePaymentsToSent();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 2, 16, SpcfTimeZone.getLocalTimeZone()));

        BatchJobManager.runJob(BatchJobType.StateReport);

        String[] expectedOutputs = {
                Pattern.quote("State Abrv,State Tax ID,IA BEN,FEIN,PSID,Company Legal Name,Company Legal Address,City,State,Zip,State Payment Frequency,Payment Method,Payment Amt with Decimal,Quarter,Period Year,Period Begin Date,Period End Date,Payment Due Date,Tax Name 1,Amount 1,Tax Name 2,Amount 2,Tax Name 3,Amount 3,Tax Name 4,Amount 4,Tax Name 5,Amount 5,Payroll Admin Name,Payroll Admin Phone Number"),
                Pattern.quote("=\"MI-MW106-PAYMENT\",=\"00-0000002\",=\"\",=\"00-0000002\",=\"2\",=\"TEST_COMPANY_2\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"ACHCredit\",=\"48.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\","),
                Pattern.quote("=\"MI-MW106-PAYMENT\",=\"00-0000001\",=\"\",=\"00-0000001\",=\"1\",=\"TEST_COMPANY_1\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"ACHCredit\",=\"48.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\","),
                Pattern.quote("=\"MI-MW106-PAYMENT\",=\"3000000003\",=\"\",=\"00-0000003\",=\"3\",=\"TEST_COMPANY_3\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"48.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\","),
                Pattern.quote("=\"MI-MW106-PAYMENT\",=\"4000000004\",=\"\",=\"00-0000004\",=\"4\",=\"TEST_COMPANY_4\",=\"COLEGAL_AddressLine1 COLEGAL_AddressLine2\",=\"Ridgewood\",=\"NJ\",=\"07450\",=\"MONTHLY\",=\"CheckPayment\",=\"48.00\",=\"1\",=\"2012\",=\"01/01/2012\",=\"01/31/2012\",=\"02/21/2012\",=\"MI Income Tax Withholding\",=\"48.00\",,,,,,,,,=\"Johnny PayrollAdmin\",=\"(775) 333-3333\",")
        };

        Application.beginUnitOfWork();

        Criterion<StateReportAssoc> stateReportAssocCriterion = StateReportAssoc.PaymentTemplateFrequency().PaymentTemplate().PaymentTemplateCd().equalTo("MI-MW106-PAYMENT");
        StateReportAssoc stateReportAssoc = Application.find(StateReportAssoc.class, new Query<StateReportAssoc>().Where(stateReportAssocCriterion)).getFirst();
        assertNotNull(stateReportAssoc);
        runStateCouponAsserts(expectedOutputs, stateReportAssoc.getStateReportOutput());
        Application.rollbackUnitOfWork();
    }

    /**
     * Runs a negative MMT on AL's StateCouponOutput
     */
    @Test
    public void negativeMMTAL() {
        String[] statesList = new String[]{"AL"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();
        setStatePaymentsToNegative();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", AL_REPORT + ",MONTHLY");

        String[] expectedOutputs = {""};

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                AL_REPORT, DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a happy path on AL's StateCouponOutput
     */
    @Test
    public void withOtherStatesAL() {
        String[] statesList = new String[]{"AL", "IA", "NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();

        //Updating all mmts for AL-CR4W-PAYMENT to more than 3 business date to current date
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                in(AL_REPORT));
        for (MoneyMovementTransaction moneyMovementTransaction : mmts) {
            moneyMovementTransaction.updateInitiationDate(SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()));
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", AL_REPORT + ",MONTHLY");

        String[] expectedOutputs = {
                "\\w{10},20110131,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  ",
                "\\w{10},20110131,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  "
        };

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                AL_REPORT, DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Adds MMTs around the time the MMT is run.  Verifies that it doesn't get the others.
     */
    @Test
    public void testSurroundingMMTAL() {
        String[] statesList = new String[]{"AL"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 1));
        Application.commitUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-02"), false);
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 1));
        Application.commitUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-04-02"), false);
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 5, 1));
        Application.commitUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-05-02"), false);
        }

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110510", AL_REPORT + ",MONTHLY");

        String[] expectedOutputs = {
                "\\w{10},20110430,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  ",
                "\\w{10},20110430,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  "
        };

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                AL_REPORT, DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a happy path on AL's StateCouponOutput
     */
    @Test
    public void backDatedRerunAL() {
        String[] statesList = new String[]{"AL"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();

        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 2, 1);
        endDate.addMilliseconds(-1);

        BatchJobManager.runJob(BatchJobType.StateReport, "20110209", AL_REPORT + ",MONTHLY");

        String[] expectedOutputs = {
                "\\w{10},20110131,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  ",
                "\\w{10},20110131,M,A6,00002,0000000006.00,             ,0000000000.00,0000000006.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  "
        };

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                AL_REPORT, DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);

        StateCouponsTests.deleteAllStateReports();

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-17"), false);
        }

        BatchJobManager.runJob(BatchJobType.StateReport, "20110209", AL_REPORT + ",MONTHLY");

        String[] newExpectedOutputs = {
                "\\w{10},20110131,M,A6,00002,0000000012.00,             ,0000000000.00,0000000012.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  ",
                "\\w{10},20110131,M,A6,00002,0000000012.00,             ,0000000000.00,0000000012.00,N,        ,    ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,      ,0000000000.00,0000000000.00,V,N,                                   ,                                 ,  ,     ,    ,        , ,         ,                  "
        };

        paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                "AL-CR4WH-PAYMENT", DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(newExpectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a zero MMT on AL's StateCouponOutput
     */
    @Test
    public void zeroMMTAL() {
        String[] statesList = new String[]{"AL"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();

        Application.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.SentToAgency);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause));

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            DomainEntitySet<FinancialTransaction> financialTransactions = moneyMovementTransaction.getFinancialTransactionCollection();

            for (FinancialTransaction financialTransaction : financialTransactions) {
                financialTransaction.setFinancialTransactionAmount(new SpcfMoney("0.00"));
            }
            moneyMovementTransaction.setMoneyMovementTransactionAmount(SpcfMoney.ZERO);
        }
        Application.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", AL_REPORT + ",MONTHLY");

        String[] expectedOutputs = {""};

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                AL_REPORT, DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Adds the IA BIN Number to all IA companies
     *
     * @param paymentTemplate The payment template
     */
    public static void addBIN(PaymentTemplate paymentTemplate) {
        Application.beginUnitOfWork();

        DomainEntitySet<Agency> agencies = Application.find(Agency.class, Agency.AgencyId().equalTo("IADOR"));
        assertEquals("Too many agencies found", 1, agencies.size());

        Agency iaAgency = agencies.get(0);

        DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, CompanyAgency.Agency().equalTo(iaAgency));

        for (CompanyAgency companyAgency : companyAgencies) {
            DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = Application.find(CompanyAgencyPaymentTemplate.class,
                                                                                                           CompanyAgencyPaymentTemplate.CompanyAgency().equalTo(companyAgency)
                                                                                                                                       .And(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate)));

            assertEquals("Too many CompanyAgencyPaymentTemplates found", 1, companyAgencyPaymentTemplates.size());

            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgencyPaymentTemplates.get(0);

            CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId = new CompanyPaymentTemplateAgencyId();
            companyPaymentTemplateAgencyId.setName("BEN Number");
            companyPaymentTemplateAgencyId.setAgencyTaxpayerId("12345678");
            companyPaymentTemplateAgencyId.setCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);

            Application.save(companyPaymentTemplateAgencyId);
        }

        Application.commitUnitOfWork();
    }

    /**
     * Runs a happy path on IA's monthly StateCouponOutput
     */
    @Test
    public void happyPathIAMonthly() {
        String[] statesList = new String[]{"IA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.MONTHLY);
        addBIN(paymentTemplateFrequency.getPaymentTemplate());

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", IA_REPORT + ",MONTHLY");

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "12345678\\d{12}+01/31/2011000000028         00000000028                    000000002800A",
                "12345678\\d{12}+01/31/2011000000028         00000000028                    000000002800A"
        };

        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a negative MMT on IA's monthly StateCouponOutput
     */
    @Test
    public void negativeMMTIAMonthly() {
        String[] statesList = new String[]{"IA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.MONTHLY);
        addBIN(paymentTemplateFrequency.getPaymentTemplate());

        setStatePaymentsToSent();
        setStatePaymentsToNegative();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", IA_REPORT + ",MONTHLY");

        String[] expectedOutputs = {""};

        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a happy path on IA's semimonthly StateCouponOutput
     */
    @Test
    public void happyPathIASemimonthly() {
        String[] statesList = new String[]{"IA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        Application.beginUnitOfWork();
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.SEMIMONTHLY);
        changePaymentFrequency(paymentTemplateFrequency);
        Application.commitUnitOfWork();

        addBIN(paymentTemplateFrequency.getPaymentTemplate());

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", IA_REPORT + ",MONTHLY");

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "12345678\\d{12}+01/31/2011000000028         00000000028                    000000002800A",
                "12345678\\d{12}+01/31/2011000000028         00000000028                    000000002800A",
        };

        runStateCouponAsserts(expectedOutputs, PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.MONTHLY));

        BatchJobManager.runJob(BatchJobType.StateReport, "20110118", IA_REPORT + ",SEMIMONTHLY");

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs2 = {
                ""
        };

        runStateCouponAsserts(expectedOutputs2, paymentTemplateFrequency);
    }

    /**
     * Runs a happy path on IA's semimonthly second part of month StateCouponOutput
     */
    @Test
    public void happyPathIASemimonthlySecond() {
        String[] statesList = new String[]{"IA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 20, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-26"), false);
        }

        Application.beginUnitOfWork();
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.SEMIMONTHLY);
        changePaymentFrequency(paymentTemplateFrequency);

        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause));

        // Change the payment periods to the second part of month
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setPaymentPeriodBegin(SpcfCalendar.createInstance(2011, 1, 16));
            moneyMovementTransaction.setPaymentPeriodEnd(SpcfCalendar.createInstance(2011, 1, 31));
        }
        Application.commitUnitOfWork();

        addBIN(paymentTemplateFrequency.getPaymentTemplate());

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", IA_REPORT + ",MONTHLY");

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "12345678\\d{12}+01/31/2011000000028         00000000028                    000000002800A",
                "12345678\\d{12}+01/31/2011000000028         00000000028                    000000002800A",
        };

        runStateCouponAsserts(expectedOutputs, PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.MONTHLY));
    }

    /**
     * Runs a negative MMT on IA's semimonthly StateCouponOutput
     */
    @Test
    public void negativeMMTIASemimonthly() {
        String[] statesList = new String[]{"IA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        Application.beginUnitOfWork();
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.SEMIMONTHLY);
        changePaymentFrequency(paymentTemplateFrequency);
        Application.commitUnitOfWork();

        addBIN(paymentTemplateFrequency.getPaymentTemplate());

        setStatePaymentsToSent();
        setStatePaymentsToNegative();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110118", IA_REPORT + ",SEMIMONTHLY");

        String[] expectedOutputs = {""};

        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Changes the payment frequency of all MoneyMovementTransactions to the specified frequency
     *
     * @param paymentTemplateFrequency The specified frequency
     */
    public static void changePaymentFrequency(PaymentTemplateFrequency paymentTemplateFrequency) {
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause));

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setPaymentFrequency(paymentTemplateFrequency);

            if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIMONTHLY)) {
                SpcfCalendar newPeriodEnd = moneyMovementTransaction.getPaymentPeriodEnd();
                newPeriodEnd.addDays(-16);
                moneyMovementTransaction.setPaymentPeriodEnd(newPeriodEnd);
            }
        }
    }

    /**
     * Runs a happy path on MA's Quarterly (MA-M941-PAYMENT) StateCouponOutput
     */
    @Test
    public void happyPathMAQuarterly() {
        String[] statesList = new String[]{"MA"};
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(PayrollServicesTest.BASE_YEAR, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(PayrollServicesTest.BASE_YEAR, 12, 19, SpcfTimeZone.getLocalTimeZone()), new DateDTO(PayrollServicesTest.BASE_YEAR + "-12-23"), false);
        }

        Application.beginUnitOfWork();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.QUARTERLY);
        changePaymentFrequency(paymentTemplateFrequency);

        Application.commitUnitOfWork();

        setStatePaymentsToSent();

        //DataLoadServices.setPSPDate(2011, 1, 26); // Runs 3 business days before Jan-31
        BatchJobManager.runJob(BatchJobType.StateReport, (PayrollServicesTest.BASE_YEAR + 1) + "0118", MA_REPORT + ",MONTHLY");


        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511                                                                                                                                                                  ",
                "M\\d{6}+1231" + PayrollServicesTest.BASE_YEAR + "\\d{9}+  TEST_COMPANY_\\d*\\s*Y0000000002000000004200000000000000 000000000000000000004200 000000004200000000000000000000004200000000000000  NN                                                                                                               ",
                "M\\d{6}+1231" + PayrollServicesTest.BASE_YEAR + "\\d{9}+  TEST_COMPANY_\\d*\\s*Y0000000002000000004200000000000000 000000000000000000004200 000000004200000000000000000000004200000000000000  NN                                                                                                               "
        };

        runStateCouponAsserts(expectedOutputs, PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.MONTHLY));
    }

    /**
     * Runs a negative MMT on MA's Quarterly (MA-M941-PAYMENT) StateCouponOutput
     */
    @Test
    public void negativeMMTMAQuarterly() {
        String[] statesList = new String[]{"MA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();
        setStatePaymentsToNegative();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.QUARTERLY);
        changePaymentFrequency(paymentTemplateFrequency);

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", MA_REPORT + ",QUARTERLY");

        String[] expectedOutputs = {"MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*"};

        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a happy path on MA's Weekly (MA-M941-PAYMENT) StateCouponOutput
     */
    @Test
    public void happyPathMAWeekly() {
        String[] statesList = new String[]{"MA"};
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(PayrollServicesTest.BASE_YEAR, 12, 18, SpcfTimeZone.getLocalTimeZone()));
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(PayrollServicesTest.BASE_YEAR, 12, 19, SpcfTimeZone.getLocalTimeZone()), new DateDTO(PayrollServicesTest.BASE_YEAR + "-12-23"), false);
        }

        Application.beginUnitOfWork();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.QUARTERMONTHLY);
        changePaymentFrequency(paymentTemplateFrequency);

        Application.commitUnitOfWork();

        setStatePaymentsToSent();

        // Weekly MA reports run along with quarterly
        BatchJobManager.runJob(BatchJobType.StateReport, (PayrollServicesTest.BASE_YEAR + 1) + "0118", MA_REPORT + ",MONTHLY");

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511                                                                                                                                                                  ",
                "M\\d{6}+1231+" + PayrollServicesTest.BASE_YEAR + "\\d{9}+  TEST_COMPANY_\\d*\\s*Y0000000002000000004200000000000000 000000000000000000004200 000000004200000000000000000000004200000000000000  NN                                                                                                               ",
                "M\\d{6}+1231+" + PayrollServicesTest.BASE_YEAR + "\\d{9}+  TEST_COMPANY_\\d*\\s*Y0000000002000000004200000000000000 000000000000000000004200 000000004200000000000000000000004200000000000000  NN                                                                                                               "
        };

        runStateCouponAsserts(expectedOutputs, PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.MONTHLY));
    }

    /**
     * Runs a negative MMT on MA's Weekly (MA-M941-PAYMENT) StateCouponOutput
     */
    @Test
    public void negativeMMTMAWeekly() {
        String[] statesList = new String[]{"MA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();
        setStatePaymentsToNegative();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.QUARTERMONTHLY);
        changePaymentFrequency(paymentTemplateFrequency);

        // Weekly MA reports run along with quarterly
        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", MA_REPORT + ",QUARTERLY");

        String[] expectedOutputs = {"MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*"};

        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a happy path on MA's monthly (MA-M941-PAYMENT) StateCouponOutput
     */
    @Test
    public void happyPathMAMonthly() {
        String[] statesList = new String[]{"MA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", MA_REPORT + ",MONTHLY");

        // These are virtually the same now because the regex masks any differences.  Leaving in, in case we change amounts
        String[] expectedOutputs = {
                "MA941X000002880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511                                                                                                                                                                  ",
                "M\\d{6}+01312011\\d{9}+  TEST_COMPANY_\\d*\\s*Y0000000002000000004200000000000000 000000000000000000004200 000000004200000000000000000000004200000000000000  NN                                                                                                               ",
                "M\\d{6}+01312011\\d{9}+  TEST_COMPANY_\\d*\\s*Y0000000002000000004200000000000000 000000000000000000004200 000000004200000000000000000000004200000000000000  NN                                                                                                               "
        };

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.MONTHLY);
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a negative MMT on MA's monthly (MA-M941-PAYMENT) StateCouponOutput
     */
    @Test
    public void negativeMMTMAMonthly() {
        String[] statesList = new String[]{"MA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();
        setStatePaymentsToNegative();

        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 2, 1);
        endDate.addMilliseconds(-1);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.MONTHLY);

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", MA_REPORT + ",MONTHLY");

        String[] expectedOutputs = {"MA941X000000880146711Computing Resources Inc       6884 Sierra Center Parkway    Reno                          NV89511\\s*"};
        runStateCouponAsserts(expectedOutputs, paymentTemplateFrequency);
    }

    /**
     * Runs a happy path on KY's StateCouponOutput - Monthly
     */
    @Test
    public void happyPathKY_Monthly(){
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 2, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"KY"};
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("KY", psid, "987654321", "089064", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid,KY_REPORT, DepositFrequencyCode.MONTHLY);
        DataLoadServices.updateAgencyTaxpayerId(company,"KY-K1-PAYMENT","089064");

        Application.beginUnitOfWork();
        Application.refresh(company);
        Address address = company.getLegalAddress();
        address.setState("KY");
        address.setZipCode("31839-2045");
        Application.commitUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList,
                SpcfCalendar.createInstance(2021, 2, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2021-02-07"), false);

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20210324", KY_REPORT + ",MONTHLY");

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                KY_REPORT, DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
   String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        List<String> linesList = getFileDataAsList("test_KY_K1_MONTHLY_Expected");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        assertTrue("ProcessType contains 'T' for Test Env : ",stateCouponArray[9].contains("<ProcessType>T</ProcessType>"));
        assertTrue("Tax Period MONTHLY start date  should be 2021-02-01 for February ",stateCouponArray[18].contains("<TaxPeriodBeginDate>2021-02-01</TaxPeriodBeginDate>"));
        assertTrue("Tax Period MONTHLY end date  should be 2021-02-28 for February ",stateCouponArray[19].contains("TaxPeriodEndDate>2021-02-28</TaxPeriodEndDate>"));

    }

    /**
     * Runs a happy path on KY's StateCouponOutput - TWICEMONTHLY
     */
    @Test
    public void happyPathKY_TwiceMonthly(){
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 2, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"KY"};
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("KY", psid, "987654321", "089064", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid,KY_REPORT, DepositFrequencyCode.TWICEMONTHLY);
        DataLoadServices.updateAgencyTaxpayerId(company,"KY-K1-PAYMENT","089064");

        DataLoadServices.runPayrollRun(company, statesList,
                SpcfCalendar.createInstance(2021, 2, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2021-02-07"), false);


        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20210224", KY_REPORT + ",TWICEMONTHLY");

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                KY_REPORT, DepositFrequencyCode.TWICEMONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);

        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        List<String> linesList = getFileDataAsList("test_KY_K1_TWICEMONTHLY_Expected");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        assertTrue("ProcessType contains 'T' for Test Env : ",stateCouponArray[9].contains("<ProcessType>T</ProcessType>"));
        assertTrue("Tax Period TWICEMONTHLY start date  should be 2021-02-01 for February ",stateCouponArray[18].contains("<TaxPeriodBeginDate>2021-02-01</TaxPeriodBeginDate>"));
        assertTrue("Tax Period TWICEMONTHLY end date  should be 2021-02-28 for February ",stateCouponArray[19].contains("TaxPeriodEndDate>2021-02-15</TaxPeriodEndDate>"));


    }

    /**
     * Runs a happy path on NM's StateCouponOutput - Monthly
     */
    @Test
    public void happyPathNM_Monthly() {

        String[] statesList = new String[]{"NM"};
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);


        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", NM_REPORT + ",MONTHLY");

        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed for NM CRS1 state coupons", e);
        }
   String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

    }

    @Test
    public void happyPathNM_MonthlyWithAmpersands() {

        String[] statesList = new String[]{"NM"};
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        // the '&' should be removed and the expected output shoukd remain the same
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setLegalName(company.getLegalName().concat("&"));
        Address address = company.getLegalAddress();
        address.setAddressLine1(address.getAddressLine1().concat("&"));
        Application.commitUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);


        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", NM_REPORT + ",MONTHLY");

        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed for NM CRS1 state coupons", e);
        }
        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected");
        List stateCouponList = Arrays.asList(stateCouponArray);


        assertEquals("Company Name contains &", "TEST_COMPANY_1&", company.getLegalName());
        assertTrue(company.getLegalAddress().getAddressLine1().contains("&"));
        assertEquals("Lines don't match.", stateCouponList.size(), linesList.size());

    }

    /**
     * Runs a happy path on NM's StateCouponOutput - Monthly
     */
    @Test
    public void happyPathNM_MonthlyQuarterlyMonthly() {

        String[] statesList = new String[]{"NM"};
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), NM_REPORT, DepositFrequencyCode.QUARTERLY);

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 2, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-02-07"), false);
        DataLoadServices.setPSPDate(2011, 3, 1);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), NM_REPORT, DepositFrequencyCode.MONTHLY, PSPDate.getPSPTime());

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110420", NM_REPORT + ",MONTHLY");

        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed for NM CRS1 state coupons", e);
        }

        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected_3");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        String[] expectedOutput = linesList.toArray(new String[0]);
        assertTrue("Lines in coupons are not matching", runCheckForNM(expectedOutput,stateReportOutput));
    }

    /**
     * Runs a happy path on NM's StateCouponOutput - Quarterly
     */
    @Test
    public void happyPathNM_Quarterly() {
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(NM_REPORT, SpcfCalendar.createInstance(2005, 1, 1));

        String[] statesList = new String[]{"NM"};
        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), DepositFrequencyCode.QUARTERLY);

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        DataLoadServices.setPSPDate(2011, 3, 1);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), DepositFrequencyCode.MONTHLY, PSPDate.getPSPTime());

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-03-08"), false);

        DataLoadServices.setPSPDate(2011, 4, 20);

        BatchJobManager.runJob(BatchJobType.StateReport);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(NM_REPORT, DepositFrequencyCode.MONTHLY);
        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed for NM CRS1 state coupons", e);
        }
        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected_2");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        String[] expectedOutput = linesList.toArray(new String[0]);
        assertTrue("Lines in coupons are not matching", runCheckForNM(expectedOutput,stateReportOutput));
    }

    /**
     * Runs a happy path on NM's StateCouponOutput - First SemiAnnual
     */
    @Test
    public void happyPathNM_FirstSemiannual() {
        String[] statesList = new String[]{"NM"};
        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), DepositFrequencyCode.SEMIANNUAL);

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);

        DataLoadServices.setPSPDate(2011, 7, 20);

        BatchJobManager.runJob(BatchJobType.StateReport);


        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(NM_REPORT, DepositFrequencyCode.MONTHLY);
        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed for NM CRS1 state coupons", e);
        }

        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");

        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected_Semiannual");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        String[] expectedOutput = linesList.toArray(new String[0]);
        assertTrue("Lines in coupons are not matching", runCheckForNM(expectedOutput,stateReportOutput));
    }

    /**
     * Runs a happy path on NM's StateCouponOutput - Second SemiAnnual
     */
    @Test
    public void happyPathNM_SecondSemiannual() {
        String[] statesList = new String[]{"NM"};
        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

        DataLoadServices.setPSPDate(2011, 7, 1);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), DepositFrequencyCode.SEMIANNUAL);

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-07-08"), false);

        DataLoadServices.setPSPDate(2012, 1, 20);

        BatchJobManager.runJob(BatchJobType.StateReport);
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(NM_REPORT, DepositFrequencyCode.MONTHLY);
        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed for NM CRS1 state coupons", e);
        }
        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");
        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected_SecondSemiannual");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        String[] expectedOutput = linesList.toArray(new String[0]);
        assertTrue("Lines in coupons are not matching", runCheckForNM(expectedOutput,stateReportOutput));
    }

    /**
     * Runs a negative MMT on NM's StateCouponOutput
     */
    @Test
    public void negativeMMTNM() {
        String[] statesList = new String[]{"NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();
        setStatePaymentsToNegative();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", NM_REPORT + ",MONTHLY");

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.MONTHLY);

        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        boolean validCoupon = false;
        /*
        Expected warning, because we are not generating any data
        Warning is due to missing atleast one <ReturnState> ata the time of validation with .xsd file
         */
        try {
            validCoupon = validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            validCoupon = false;
            e.printStackTrace();
        }
        assertFalse("change in xsd schema file.", validCoupon);

        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");

        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected_OnlyHeader");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        String[] expectedOutput = linesList.toArray(new String[0]);

        assertTrue("Lines in coupons are not matching", runCheckForNM(expectedOutput,stateReportOutput));
    }

    private boolean runCheckForNM(String expectedOutputs[], StateReportOutput reportOutput) {

        String couponOutput = readStateCouponOutput(reportOutput);

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

    /**
     * Check AL's scheduling
     */
    @Test
    public void testALScheduling() {
        AL_WH al_wh = new AL_WH();

        String[] statesList = new String[]{"AL"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-02-04"), false);
        }

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                AL_REPORT, DepositFrequencyCode.MONTHLY);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().
                in(AL_REPORT));
        System.out.println("moneyMovementTransactions: " + mmts.size());


        for (MoneyMovementTransaction moneyMovementTransaction : mmts) {
            moneyMovementTransaction.updateInitiationDate(SpcfCalendar.createInstance(2011, 2, 7, SpcfTimeZone.getLocalTimeZone()));
        }

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 9));
        Application.commitUnitOfWork();
        assertFalse("AL scheduling incorrect", al_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime().toUtc()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 2));
        Application.commitUnitOfWork();
        assertTrue("AL scheduling incorrect", al_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime().toUtc()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 1));
        Application.commitUnitOfWork();
        assertFalse("AL scheduling incorrect", al_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime().toUtc()));
    }

    /**
     * Check IA's Semi monthly scheduling
     */
    @Test
    public void testIASemimonthlyScheduling() {
        IA_WH ia_wh = new IA_WH();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.SEMIMONTHLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 6));
        Application.commitUnitOfWork();

        assertFalse("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 7));
        Application.commitUnitOfWork();

        assertTrue("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 8));
        Application.commitUnitOfWork();

        assertFalse("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));

        // Second part of month
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 21));
        Application.commitUnitOfWork();

        assertFalse("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22));
        Application.commitUnitOfWork();

        assertTrue("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 23));
        Application.commitUnitOfWork();

        assertFalse("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));
    }

    /**
     * Check IA monthly scheduling
     */
    @Test
    public void testIAMonthlyScheduling() {
        IA_WH ia_wh = new IA_WH();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                IA_REPORT, DepositFrequencyCode.MONTHLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 9));
        Application.commitUnitOfWork();

        assertFalse("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 10));
        Application.commitUnitOfWork();

        assertTrue("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 11));
        Application.commitUnitOfWork();

        assertFalse("IA semimonthly scheduling incorrect", ia_wh.isScheduled(paymentTemplateFrequency, null));
    }

    /**
     * Check MA monthly scheduling
     */
    @Test
    public void testMAMonthlyScheduling() {
        MA_WH ma_wh = new MA_WH();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.MONTHLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 26));
        Application.commitUnitOfWork();

        assertTrue("MA Quarterly scheduling incorrect", ma_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 9));
        Application.commitUnitOfWork();

        assertFalse("MA Monthly scheduling incorrect", ma_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 10));
        Application.commitUnitOfWork();

        assertTrue("MA Monthly scheduling incorrect", ma_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 11));
        Application.commitUnitOfWork();

        assertFalse("MA Monthly scheduling incorrect", ma_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));
    }

    /**
     * Check MA quarterly scheduling
     */
    @Test
    public void testMAQuarterlyScheduling() {
        MA_WH ma_wh = new MA_WH();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.QUARTERLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 26));
        Application.commitUnitOfWork();

        assertTrue("MA Quarterly scheduling incorrect", ma_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 10));
        Application.commitUnitOfWork();

        assertFalse("MA Quarterly scheduling incorrect", ma_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 11));
        Application.commitUnitOfWork();

        assertFalse("MA Quarterly scheduling incorrect", ma_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        // Weekly should always be false
        PaymentTemplateFrequency weeklyPaymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                MA_REPORT, DepositFrequencyCode.WEEKLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 10));
        Application.commitUnitOfWork();

        assertFalse("MA Weekly scheduling incorrect", ma_wh.isScheduled(weeklyPaymentTemplateFrequency, PSPDate.getPSPTime()));
    }

    /**
     * Check MA monthly scheduling
     */
    @Test
    public void testNMMonthlyScheduling() {
        NM_WH nm_wh = new NM_WH();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.MONTHLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 21));
        Application.commitUnitOfWork();

        assertFalse("NM Monthly scheduling incorrect", nm_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22));
        Application.commitUnitOfWork();

        assertTrue("NM Monthly scheduling incorrect", nm_wh.isScheduled(paymentTemplateFrequency, null));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 23));
        Application.commitUnitOfWork();

        assertFalse("NM Monthly scheduling incorrect", nm_wh.isScheduled(paymentTemplateFrequency, null));

        // Quarterly should always be false
        PaymentTemplateFrequency quarterlyPaymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.QUARTERLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22));
        Application.commitUnitOfWork();

        assertFalse("NM Quarterly scheduling incorrect", nm_wh.isScheduled(quarterlyPaymentTemplateFrequency, null));

        // Semiannual should always be false
        PaymentTemplateFrequency semiannualPaymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.SEMIANNUAL);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 22));
        Application.commitUnitOfWork();

        assertFalse("NM Semiannual scheduling incorrect", nm_wh.isScheduled(semiannualPaymentTemplateFrequency, null));
    }

    /**
     * Check KY MONTHLY scheduling
     */
    @Test
    public void testKYMONTHLYScheduling() {
        KY_WH ky_wh = new KY_WH();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                KY_REPORT, DepositFrequencyCode.MONTHLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2021, 1, 27));
        Application.commitUnitOfWork();

        assertTrue("KY MONTHLY scheduling correct", ky_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2021, 2, 9));
        Application.commitUnitOfWork();

        assertTrue("KY MONTHLY scheduling correct", ky_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2021, 2, 11));
        Application.commitUnitOfWork();

        assertFalse("KY MONTHLY scheduling incorrect", ky_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

    }

    /**
     * Check KY TWICEMONTHLY scheduling
     */
    @Test
    public void testKYTWICEMONTHLYScheduling() {
        KY_WH ky_wh = new KY_WH();

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                KY_REPORT, DepositFrequencyCode.TWICEMONTHLY);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2021, 1, 27));
        Application.commitUnitOfWork();

        assertTrue("KY TWICEMONTHLY scheduling correct", ky_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2021, 2, 5));
        Application.commitUnitOfWork();

        assertTrue("KY TWICEMONTHLY scheduling correct", ky_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2021, 2, 22));
        Application.commitUnitOfWork();

        assertTrue("KY TWICEMONTHLY scheduling correct", ky_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2021, 2, 11));
        Application.commitUnitOfWork();

        assertFalse("KY TWICEMONTHLY scheduling incorrect", ky_wh.isScheduled(paymentTemplateFrequency, PSPDate.getPSPTime()));

    }

    public boolean validateXmlWithXsd(File xsd, Reader xml) {
        return DataLoadServices.xmlSchemaValidator(xsd, xml);
    }

    @Test
    public void happyPathNM_Monthly_ValidateWithXsd() {
        String[] statesList = new String[]{"NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 3, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);
        }

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", NM_REPORT + ",MONTHLY");

        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed foir NM CRS1 state coupons", e);
        }


    }

    /**
     * Runs a happy path on NM's StateCouponOutput - Monthly
     */
    @Test
    public void happyPathNM_Monthly_New() {

        String[] statesList = new String[]{"NM"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        String psid = "199210091";
        Company company = createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1", statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

        DataLoadServices.runPayrollRun(company, statesList,
                                       SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-01-07"), false);

        setStatePaymentsToSent();

        BatchJobManager.runJob(BatchJobType.StateReport, "20110201", NM_REPORT + ",MONTHLY");

        File crsXmlXsd = new File(expected_Dir, "WWTReturn_XML_1.0.xsd");
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                NM_REPORT, DepositFrequencyCode.MONTHLY);
        StateReportOutput stateReportOutput = getStateCoupon(paymentTemplateFrequency);
        try {
            validateXmlWithXsd(crsXmlXsd, new StringReader(readStateCouponOutput(stateReportOutput)));
        } catch (Exception e) {
            throw new RuntimeException("Xml validation failed for NM CRS1 state coupons", e);
        }
        String couponOutput = readStateCouponOutput(stateReportOutput);

        String[] stateCouponArray = couponOutput.split("\\n");

        List<String> linesList = getFileDataAsList("test_NM_CRS1_Expected");
        List stateCouponList = Arrays.asList(stateCouponArray);
        assertEquals("Lines doesn't match.", stateCouponList.size(), linesList.size());

        String[] expectedOutput = linesList.toArray(new String[0]);
        assertTrue("Lines in coupons are not matching", runCheckForNM(expectedOutput,stateReportOutput));

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
     * @param expectedTemplateFrequency
     * @return
     */
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

    public static Company createAssistedCompanyWithRates(String state, String psid, String ein, String aid, String[] pStatesList, PaymentTemplateCategory pCategory, PaymentMethod pPaymentMethod) {

        // Company with laws, rates, etc.
        DataLoadServices.reinitialize();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, false, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DomainEntitySet<Company> companies = DataLoadServices.setupCompanyAgency(pStatesList, pCategory, pPaymentMethod);
        return company;
    }

    /**
     * ReportOutput is a lazy attribute and to fetch that we need session
     * @param stateReportOutput
     * @return
     */
    private static String getReportOutput(StateReportOutput stateReportOutput) {

        String reportOutput = null;
        boolean manageSession = !Application.hasActiveTransaction();

        if(manageSession) {
            Application.beginUnitOfWork();
        }

        // associate stateReportOutput to Session
        Application.refresh(stateReportOutput);
        // load the lazy attribute ReportOutput
        reportOutput = stateReportOutput.getReportOutput();

        if(manageSession) {
            Application.rollbackUnitOfWork();
        }

        return reportOutput;
    }
}
