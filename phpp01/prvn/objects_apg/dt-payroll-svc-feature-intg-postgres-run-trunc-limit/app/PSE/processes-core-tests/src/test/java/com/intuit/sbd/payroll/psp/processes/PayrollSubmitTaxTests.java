package com.intuit.sbd.payroll.psp.processes;

import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.util.AchReturnAccountingFile;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.tools.ComplianceToolkit;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.*;
import com.intuit.sbd.payroll.psp.batchjobs.zeropayments.ProcessZeroPayments;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 10, 2009
 * Time: 1:31:32 PM
 */
public class PayrollSubmitTaxTests {
    private static final SpcfLogger logger = Application.getLogger(PayrollSubmitTaxTests.class);
    private int achTaxOffloadOffset;
    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");
    }

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
    }

    @After
    public void runAfterEachTest() {
       PayrollServicesTest.afterEachTest();
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testEFTPS941WithMEStateTaxesPayroll() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 1));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalAndMEStateTaxCompanyLaws(company);

        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        String[] statesList = new String[]{"PA", "OH", "AZ", "KS", "ME"};
        DataLoadServices.setupCompany(1L, 10, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());

        //ME-900ME-PAYMENT and ME-941C1ME-PAYMENT

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "PA-501-PAYMENT");
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", supportedDate);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "ME-900ME-PAYMENT");
        DataLoadServices.updatePaymentTemplateSupportedDate("ME-900ME-PAYMENT", supportedDate);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "ME-941C1ME-PAYMENT");
        DataLoadServices.updatePaymentTemplateSupportedDate("ME-941C1ME-PAYMENT", supportedDate);

        for (Company company1 : Application.find(Company.class)) {
            DataLoadServices.updateRequiredIDs(company1, null, false);
            DataLoadServices.updateRequiredIDs(company1, "ME-900ME-PAYMENT", true);
        }



        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndMEStateTaxes(payrollRunDTO, company, new DateDTO("2010-11-02"), emps);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        DataLoadServices.setPSPDate(2011, 01, 27);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        String[] expectedOutput = {
                "101 02100002197226160001101271325G094101JPMORGAN CHASE         INTUIT                         ",
                "5220COMPUTING RESOURCES        1234567899118556001CCDMAINEWH   110131110131   1021000020000001",
                "62202105205381302364         0000005400123456789      TEST_COMPANY_\\d*\\s*1\\d{15}+",
                ".*",
                ".*",
                ".*",
                ".*",
                ".*",
                ".*",
                ".*"
        };

        assertEquals("Offload output sizes do not match up.", lines.length, expectedOutput.length);

        for (int i = 0; i < expectedOutput.length; i++) {
            Pattern pattern = Pattern.compile(expectedOutput[i]);
            Matcher matcher = pattern.matcher(lines[i]);

            assertTrue("Did not find expected output:\n" + expectedOutput[i] + "\nIn output:\n" + lines[i], matcher.matches());
        }
    }


    @Test
    public void testThreshold_NY_UnorderedPayrolls() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        // run 1st payroll over the threshold
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 3, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2012-03-05"),
                                                                    emps,
                                                                    new String[]{"36", "197"},
                                                                    new String[]{"4250", "136"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("4250.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2012, 3, 8, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);
        assertEquals("Payment Initiation date", statePaymentInitDate, moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Payment Due date", SpcfCalendar.createInstance(2012, 3, 8, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        // run 2nd payroll over the threshold dated before 1st payroll
        PayrollServices.beginUnitOfWork();
        payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2012-03-02"),
                                                                    emps,
                                                                    new String[]{"54", "57"},
                                                                    new String[]{"1496.75", "192.5"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                     .setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                     .setDueDate(SpcfCalendar.createInstance(2012, 3, 7, SpcfTimeZone.getLocalTimeZone()))
                                                                     .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("1689.25"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2012, 3, 7, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        assertEquals("Payment Initiation date", statePaymentInitiationDate, moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Payment Due date", SpcfCalendar.createInstance(2012, 3, 7, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                     .setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                     .setDueDate(SpcfCalendar.createInstance(2012, 3, 8, SpcfTimeZone.getLocalTimeZone()))
                                                                     .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("4250.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        statePaymentInitiationDate = SpcfCalendar.createInstance(2012, 3, 8, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        assertEquals("Payment Initiation date", statePaymentInitiationDate, moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Payment Due date", SpcfCalendar.createInstance(2012, 3, 8, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNYPaymentMethodWithPayroll() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        // run 1st payroll over the threshold
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 3, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-03-05"),
                                                                    emps,
                                                                    new String[]{"36", "197"},
                                                                    new String[]{"4250", "136"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();



        // run 2nd payroll over the threshold dated before 1st payroll
        PayrollServices.beginUnitOfWork();
        payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        payrollDTO1 =  DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-03-02"),
                                                                    emps,
                                                                    new String[]{"54", "57"},
                                                                    new String[]{"1496.75", "192.5"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                                              .setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                                              .setDueDate(SpcfCalendar.createInstance(2015, 3, 10, SpcfTimeZone.getLocalTimeZone()))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("4250.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("Payment Initiation date", SpcfCalendar.createInstance(2015, 03, 6, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Payment Due date", SpcfCalendar.createInstance(2015, 3, 10, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());

        PayrollServices.rollbackUnitOfWork();

    }

   ///Happy Path tests

   /*
    *Test for 24/6 Payroll| 25/6 Paycheck 450$ and 29/6 Payroll|30/6 Paycheck 450$
    * After the ACHDebit change this is 2 day offset difference.
    * Result: With the change
    * 	PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    * 	06/25/2015	    06/24/2015	            900	                	07/06/2015	    07/02/2015	        07/06/2015
    *   06/30/2015  	06/29/2015          	900         	       	07/06/2015  	07/02/2015          07/06/2015

    *Result: Without the change
    *    PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    *     06/25/2015	06/24/2015	            900              	07/06/2015	    07/02/2015	        07/06/2015
    *     06/30/2015  	06/29/2015          	900         		07/06/2015  	07/02/2015          07/06/2015
    */
   @Test
   public void testNYPaymentMethodWithPayrollScenario1() {
       DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

       String psid = "123456789";
       String[] statesList = {"NY"};
       DataLoadServices.setPSPDate(2015, 1, 1);
       Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
       String stateAccessCode = "";
       AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
       PayrollServices.beginUnitOfWork();
       assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
       PayrollServices.commitUnitOfWork();
       List<Employee> emps = DataLoadServices.addEEs(company, 1);


       //24/6 Run Payroll paycheck date of 25/6

       DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
       PayrollServices.beginUnitOfWork();
       PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
       DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
       PayrollRunDTO payrollDTO1 =
               DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                   company,
                                                                   new DateDTO("2015-06-25"),
                                                                   emps,
                                                                   new String[]{"36"},
                                                                   new String[]{"450"});
       assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
       PayrollServices.commitUnitOfWork();

       offloadpayment(5,company);

       //30/6 Run Payroll paycheck date of 1/7 (This passes over the threshold $700)

       DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 29, SpcfTimeZone.getLocalTimeZone()));
       PayrollServices.beginUnitOfWork();
       PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
       DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
       PayrollRunDTO payrollDTO2 =
               DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                   company,
                                                                   new DateDTO("2015-06-30"),
                                                                   emps,
                                                                   new String[]{"36"},
                                                                   new String[]{"450"});
       assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
       PayrollServices.commitUnitOfWork();

       MoneyMovementTransaction moneyMovementTransaction;

       PayrollServices.beginUnitOfWork();
       moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                    .setCompany(company)
                                                                    .setReadyToSend().find());
       assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
       assertEquals("Payment Amount", SpcfMoney.createInstance("900.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
       assertEquals("Payment Initiation date", SpcfCalendar.createInstance(2015, 7, 2, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
       assertEquals("Payment Due date", SpcfCalendar.createInstance(2015, 7, 6, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
       PayrollServices.rollbackUnitOfWork();
}


    /*
    *Test for 24/6 Payroll| 25/6 Paycheck 450$ and 25/6 Payroll|25/6 Paycheck 450$
    *After ACHDebit changes there is 2 day offset between Inititation Date for NY_1MN changes
    *
    * Result: With the change
    * 	PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT		    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
            06/25/2015	    06/24/2015	            900	                    06/30/2015      	06/28/2015	    06/30/2015
            06/25/2015	    06/25/2015	            900	                	06/30/2015	        06/28/2015	    06/30/2015
    *
    *
    *Result: Without the change
    *    PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    *       06/25/2015	    06/24/2015	            900	           	    06/30/2015      	06/29/2015	    06/30/2015
            06/25/2015	    06/25/2015	            900	            	06/30/2015	        06/29/2015	    06/30/2015
    */

 @Test
 public void testNYPaymentMethodWithPayrollScenario2() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        offloadpayment(1,company);

        //25/6 Run Payroll paycheck date of 25/6 (This passes over the threshold $700)

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();

         MoneyMovementTransaction moneyMovementTransaction;

         PayrollServices.beginUnitOfWork();
         moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                      .setCompany(company)
                                                                      .setReadyToSend().find());
         assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
         assertEquals("Payment Amount", SpcfMoney.createInstance("900.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
         assertEquals("Initiation date", SpcfCalendar.createInstance(2015, 6, 26, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
         assertEquals("Due date", SpcfCalendar.createInstance(2015, 6, 30, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
         PayrollServices.rollbackUnitOfWork();


    }


    /*
    *Test for 24/6 Payroll| 25/6 Paycheck 450$ and 24/6 Payroll| 29/6 Paycheck 450$
    *After ACHDebit changes there is 2 day offset between Inititation Date for NY_1MN changes
    *
    * Result:  With the change
    *PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	       DUE_DATE	   INITIATION_DATE	    SETTLEMENT_DATE
    * 06/29/2015	06/24/2015	        900	                          07/02/2015	06/30/2015	           07/02/2015
      06/25/2015	06/24/2015	        900	                          07/02/2015	07/01/2015	           7/02/2015
    *
    * Result:  Without the change
    *PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	      DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    * 06/29/2015	06/24/2015	        900	                          07/02/2015	07/01/2015	        07/02/2015
      06/25/2015	06/24/2015	        900	                          07/02/2015	07/01/2015	        7/02/2015
    *
    *
    */

    @Test
    public void testNYPaymentMethodWithPayrollScenario3() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-29"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();


        MoneyMovementTransaction moneyMovementTransaction;

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                     .setCompany(company)
                                                                     .setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("900.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Initiation date", SpcfCalendar.createInstance(2015, 06, 30, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Due date", SpcfCalendar.createInstance(2015, 07, 02, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

    }

   /*
    * Test for 24/6 Payroll| 25/6 Paycheck 450$ and 24/6 Payroll| 27/6(Non working day) Paycheck 450$
    * After ACHDebit: 07/29/2015 as initiation date is 2day prior.
    * Result: With the change
    *PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    * 06/29/2015	06/24/2015	        900	                          07/01/2015	07/29/2015	         07/01/2015
      06/25/2015	06/24/2015	        900	                          07/01/2015	07/01/2015	         7/01/2015
    *
    *Result:  Without the change
    *PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    * 06/29/2015	06/24/2015	        900	                          07/01/2015	07/29/2015	         07/01/2015
      06/25/2015	06/24/2015	        900	                          07/01/2015	07/30/2015	         7/01/2015
    *
    *
    */

    @Test
    public void testNYPaymentMethodWithPayrollScenario4() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-27"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();


        MoneyMovementTransaction moneyMovementTransaction;

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                     .setCompany(company)
                                                                     .setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("900.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Initiation date", SpcfCalendar.createInstance(2015, 06, 29, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Due date", SpcfCalendar.createInstance(2015, 07, 01, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

    }

    /*
     *Test for 24/6 Payroll| 25/6 Paycheck 450$ and 30/6 Payroll|1/6 Paycheck 450$
     *Different Quarter:- because of initiation date fixture.
     *
     * Result:
     * PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
     * 06/25/2015	      06/24/2015	        450                 	07/29/2015	    07/30/2015	        07/31/2015
       07/01/2015	      06/30/2015	        450	                    11/02/2015	    10/30/2015  	    11/02/2015
     *
     */
    @Test
    public void testNYPaymentMethodWithPayrollScenario5() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        offloadpayment(6,company);

        //30/6 Run Payroll paycheck date of 1/7 (This passes over the threshold $700)

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-07-01"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();

        MoneyMovementTransaction moneyMovementTransaction;

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                     .setCompany(company)
                                                                     .setReadyToSend()
                                                                     .setDueDate(SpcfCalendar.createInstance(2015, 07, 31, SpcfTimeZone.getLocalTimeZone()))
                                                                     .find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("450.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Initiation date", SpcfCalendar.createInstance(2015, 07, 29, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Due date", SpcfCalendar.createInstance(2015, 07, 31, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

    }

    //Check If seven day hold good.
    /*
    *Test for 24/6 Payroll| 18/6 Backdated Paycheck 450$ and 24/6 Payroll| 24/6 Paycheck 450$
    *
    * Result:  With the change
    *PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    * 06/24/2015	06/24/2015	        450	                        06/29/2015	        06/26/2015	        06/29/2015
      06/18/2015	06/24/2015	        450                     	06/23/2015	        07/01/2015	        07/02/2015
    *
    *Result:  Without the change
    *PAYCHECK_DATE	PAYROLL_RUN_DATE	MM_TRANSACTION_AMOUNT	    DUE_DATE	    INITIATION_DATE	    SETTLEMENT_DATE
    * 06/24/2015	06/24/2015	        450	                        06/29/2015	        06/26/2015	        06/29/2015
      06/18/2015	06/24/2015	        450                     	06/23/2015	        07/01/2015	        07/02/2015
    *
    *
    *
    */

    @Test
    public void testNYPaymentMethodWithPayrollScenario6() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-18"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void testNYPaymentMethodWithPayrollScenario7() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-18"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();
        offloadpayment(4,company);
        //29/6 Run Payroll paycheck date of 29/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-06-29"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));



        PayrollServices.commitUnitOfWork();


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-06-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));



        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNYPaymentMethodWithPayrollScenario8() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));


        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();



        offloadpayment(6,company);
        //29/6 Run Payroll paycheck date of 29/6



        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-06-29"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));

        PayrollServices.commitUnitOfWork();

        offloadpayment(1,company);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-06-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNYPaymentMethodWithPayrollScenario9() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));


        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();



        offloadpayment(6,company);
        //29/6 Run Payroll paycheck date of 29/6



        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-06-29"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNYPaymentMethodWithPayrollScenario10() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-18"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();
        offloadpayment(4,company);
        //29/6 Run Payroll paycheck date of 29/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-06-29"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));



        PayrollServices.commitUnitOfWork();


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-06-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));



        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO5 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO5);
        PayrollRunDTO payrollDTO5 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-06-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO5));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO6 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO6);
        PayrollRunDTO payrollDTO6 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-06-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO6));
        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void testNYPaymentMethodWithPayrollScenario11() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "1234731";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-05-18"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        //24/6 Run Payroll paycheck date of 27/6

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-05-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();
        offloadpayment(4,company);
        //29/6 Run Payroll paycheck date of 29/6


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-05-29"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));

        PayrollServices.commitUnitOfWork();


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-05-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));

        PayrollServices.commitUnitOfWork();


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO5 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO5);
        PayrollRunDTO payrollDTO5 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-05-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO5));
        PayrollServices.commitUnitOfWork();

        stateAccessCode = "";
        agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.AddCompanyPaymentMethods.name(), "NY-1MN-PAYMENT"});
        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.RecalculateCompanyPaymentMethodsEnabled.name(), "NY-1MN-PAYMENT"});

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO6 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO6);
        PayrollRunDTO payrollDTO6 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-05-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"550"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO6));
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNYPaymentMethodWithPayrollScenario12() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //20/6 Run Payroll paycheck date of 21/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 21, SpcfTimeZone.getLocalTimeZone()));


        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-06-21"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"750"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));
        PayrollServices.commitUnitOfWork();

        offloadpayment(5,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6,26 , SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-26"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();


        //This results in employer tax debit to be executed (etd)
        offloadpayment(3,company);
        //29/6 Run Payroll paycheck date of 30/6

        //Common Scenario. If Agent changes the State Access code to null.

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 29, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"10"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testNYPaymentMethodWithPayrollScenario13() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //07/01 - P1 Sent - $600 - Q - 3BD

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07, 01, SpcfTimeZone.getLocalTimeZone()));


        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-07-01"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"600"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));
        PayrollServices.commitUnitOfWork();

        //07/02 - P2 Sent - $200 - 3BD

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07, 02, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-07-02"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"200"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();

        //07/03 - offload all
        offloadpayment(6,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07,8, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-07-08"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"700"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));
        PayrollServices.commitUnitOfWork();



        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-07-07"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"100"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        offloadpayment(2,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07,10, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO5 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO5);
        PayrollRunDTO payrollDTO5 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-07-10"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"605"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO5));
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testNYPaymentMethodWithPayrollScenario14() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);


        //24/6 Run Payroll paycheck date of 25/6

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 24, SpcfTimeZone.getLocalTimeZone()));


        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-06-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"800"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-06-25"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-06-26"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));
        PayrollServices.commitUnitOfWork();

        //This results in employer tax debit to be executed (etd)
        offloadpayment(5,company);
        //29/6 Run Payroll paycheck date of 30/6

        //Common Scenario. If Agent changes the State Access code to null.

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 6, 29, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-06-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"50"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO5 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO5);
        PayrollRunDTO payrollDTO5 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-06-30"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"450"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO5));
        PayrollServices.commitUnitOfWork();
    }

// After the initiation date change for  NY-1MN the initiation date is 2days.

    @Test
    public void testNYPaymentMethodWithPayrollScenario15() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);




        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07, 01, SpcfTimeZone.getLocalTimeZone()));


        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-07-01"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"800"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));
        PayrollServices.commitUnitOfWork();


        offloadpayment(7,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-07-08"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"100"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();

        offloadpayment(1,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07, 10, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-07-10"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"620"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));
        PayrollServices.commitUnitOfWork();


        offloadpayment(1,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 07, 11, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO5 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO5);
        PayrollRunDTO payrollDTO5 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-07-11"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"600"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO5));
        PayrollServices.commitUnitOfWork();

        MoneyMovementTransaction moneyMovementTransaction;

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                     .setCompany(company)
                                                                     .setReadyToSend()
                                                                     .setDueDate(SpcfCalendar.createInstance(2015, 07, 15, SpcfTimeZone.getLocalTimeZone()))
                                                                     .find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY
                , moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("620.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Initiation date", SpcfCalendar.createInstance(2015, 07, 13, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Due date", SpcfCalendar.createInstance(2015, 07, 15, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();
}

    /*
      Before change: 966.22 (Quaterly) Threshold is hit
      277.92 => (ETD is executed in this case the amount is calculated)
      After change: 966.22 should split 277.3 to ETD three banking day because
      If ETD created it goes to Quaterly.
     */

    @Test
    public void testNYPaymentMethodPayrollScenarioProductionCompany(){
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "336016075";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(336016075L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "34567891";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 05, 11, SpcfTimeZone.getLocalTimeZone()));

        //Apr 10
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                                                                    company,
                                                                    new DateDTO("2015-04-10"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"155.54"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();


         //Apr 17
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2,
                                                                    company,
                                                                    new DateDTO("2015-04-17"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"39.02"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2));
        PayrollServices.commitUnitOfWork();



        //Apr 24
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3,
                                                                    company,
                                                                    new DateDTO("2015-04-24"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"155.54"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));
        PayrollServices.commitUnitOfWork();



        //May 01
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4,
                                                                    company,
                                                                    new DateDTO("2015-05-01"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"39.02"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));
        PayrollServices.commitUnitOfWork();



        //May 08
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO5 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO5);
        PayrollRunDTO payrollDTO5 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO5,
                                                                    company,
                                                                    new DateDTO("2015-05-08"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"155.54"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO5));
        PayrollServices.commitUnitOfWork();

        offloadpayment(2,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 05, 13, SpcfTimeZone.getLocalTimeZone()));

        //May15
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO6 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO6);
        PayrollRunDTO payrollDTO6 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO6,
                                                                    company,
                                                                    new DateDTO("2015-05-15"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"39.02"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO6));
        PayrollServices.commitUnitOfWork();

        offloadpayment(6,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 05, 19, SpcfTimeZone.getLocalTimeZone()));
        //May 19
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO7 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO7);
        PayrollRunDTO payrollDTO7 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO7,
                                                                    company,
                                                                    new DateDTO("2015-05-19"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"116.52"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO7));
        PayrollServices.commitUnitOfWork();


        //May 22
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO8 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO8);
        PayrollRunDTO payrollDTO8 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO8,
                                                                    company,
                                                                    new DateDTO("2015-05-22"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"161.4"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO8));
        PayrollServices.commitUnitOfWork();



        offloadpayment(7,company);


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 05, 26, SpcfTimeZone.getLocalTimeZone()));
        //May 29
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO9 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO9);
        PayrollRunDTO payrollDTO9 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO9,
                                                                    company,
                                                                    new DateDTO("2015-05-29"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"277.92"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO9));
        PayrollServices.commitUnitOfWork();

        offloadpayment(2,company);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 05, 28, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        agencyIdDTO = new AgencyIdDTO("NY-1MN-PAYMENT", "State Access Code", "");
        PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO);
        PayrollServices.commitUnitOfWork();
        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.AddCompanyPaymentMethods.name(), "NY-1MN-PAYMENT"});
        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.RecalculateCompanyPaymentMethodsEnabled.name(), "NY-1MN-PAYMENT"});

        offloadpayment(6,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 06, 03, SpcfTimeZone.getLocalTimeZone()));
        //Jun 5
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO10 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO10);
        PayrollRunDTO payrollDTO10 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO10,
                                                                    company,
                                                                    new DateDTO("2015-06-05"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"84.4"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO10));
        PayrollServices.commitUnitOfWork();

        offloadpayment(7,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 06, 10, SpcfTimeZone.getLocalTimeZone()));
        //Jun12

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO11 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO11);
        PayrollRunDTO payrollDTO11 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO11,
                                                                    company,
                                                                    new DateDTO("2015-06-12"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"201.30"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO11));
        PayrollServices.commitUnitOfWork();

        offloadpayment(6,company);


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 06, 16, SpcfTimeZone.getLocalTimeZone()));
        //Jun19

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO12 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO12);
        PayrollRunDTO payrollDTO12 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO12,
                                                                    company,
                                                                    new DateDTO("2015-06-19"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"201.30"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO12));
        PayrollServices.commitUnitOfWork();

        offloadpayment(8,company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 06, 24, SpcfTimeZone.getLocalTimeZone()));
        //Jun26
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO13 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO13);
        PayrollRunDTO payrollDTO13 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO13,
                                                                    company,
                                                                    new DateDTO("2015-06-26"),
                                                                    emps,
                                                                    new String[]{"36"},
                                                                    new String[]{"201.30"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO13));
        PayrollServices.commitUnitOfWork();

 }
    public static void offloadpayment(int days, Company company){

        int i = 0;
        while(i++< days ){
            DataLoadServices.runOffload(company,PSPDate.getPSPTime());
            DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
            BatchJobManager.runJob(BatchJobType.AchDebitOffload);
            Application.beginUnitOfWork();
            PSPDate.addDaysToPSPTime(1);
            Application.commitUnitOfWork();
        }
    }

    @Test
    public void testInvalidLawId() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Collection<PaycheckDTO> paycheckDTOs = payrollDTO.getPaychecks();
        Collection<LiabilityTransactionDTO> liabilityTransactions = paycheckDTOs.iterator().next().getLiabilityTransactions();
        liabilityTransactions.iterator().next().setLawId("Invalid");

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);

        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1500", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Law does not exist.", message.getMessage());



    }

    @Test
    public void testHappyPath() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertNotNull(company);
        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
        assertNotNull(taxService);
        assertEquals("Company Service Status", ServiceSubStatusCode.ActiveCurrent, taxService.getStatusCd());

        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Net Amount", new SpcfMoney("0.00"), payroll.getPayrollDirectDepositAmount());
        // Verify 2 paychecks were created
        assertEquals("Number of paychecks", 2, payroll.getPaycheckCollection().size());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxCredits = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of Agency Credit Transactions", 6, agencyTaxCredits.size());

        assertEquals("FinancialTransaction Amount1", "4.00", agencyTaxCredits.get(0).getFinancialTransactionAmount().toString());
        assertEquals("FinancialTransaction Amount2", "10.00", agencyTaxCredits.get(1).getFinancialTransactionAmount().toString());
        assertEquals("FinancialTransaction Amount3", "11.00", agencyTaxCredits.get(2).getFinancialTransactionAmount().toString());
        assertEquals("FinancialTransaction Amount4", "24.00", agencyTaxCredits.get(3).getFinancialTransactionAmount().toString());
        assertEquals("FinancialTransaction Amount5", "50.00", agencyTaxCredits.get(4).getFinancialTransactionAmount().toString());
        assertEquals("FinancialTransaction Amount6", "90.00", agencyTaxCredits.get(5).getFinancialTransactionAmount().toString());

        // law ids
        assertEquals("Payement Template1", "143", agencyTaxCredits.get(0).getLaw().getLawId());
        assertEquals("Payement Template2", "61", agencyTaxCredits.get(1).getLaw().getLawId());
        assertEquals("Payement Template3", "63", agencyTaxCredits.get(2).getLaw().getLawId());
        assertEquals("Payement Template4", "62", agencyTaxCredits.get(3).getLaw().getLawId());
        assertEquals("Payement Template5", "1", agencyTaxCredits.get(4).getLaw().getLawId());
        assertEquals("Payement Template6", "64", agencyTaxCredits.get(5).getLaw().getLawId());

        // Payment Templates
        assertEquals("Payement Template1", "IRS-941-PAYMENT", agencyTaxCredits.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Payement Template2", "IRS-941-PAYMENT", agencyTaxCredits.get(1).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Payement Template3", "IRS-941-PAYMENT", agencyTaxCredits.get(2).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Payement Template4", "IRS-941-PAYMENT", agencyTaxCredits.get(3).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Payement Template5", "IRS-941-PAYMENT", agencyTaxCredits.get(4).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Payement Template6", "IRS-941-PAYMENT", agencyTaxCredits.get(5).getLaw().getPaymentTemplate().getPaymentTemplateCd());

        // Settlement Types
        assertEquals("Settlement Type for FinancialTransaction 1", SettlementType.EFTPS, agencyTaxCredits.get(0).getSettlementTypeCd());
        assertEquals("Settlement Type for FinancialTransaction 2", SettlementType.EFTPS, agencyTaxCredits.get(1).getSettlementTypeCd());
        assertEquals("Settlement Type for FinancialTransaction 3", SettlementType.EFTPS, agencyTaxCredits.get(2).getSettlementTypeCd());
        assertEquals("Settlement Type for FinancialTransaction 4", SettlementType.EFTPS, agencyTaxCredits.get(3).getSettlementTypeCd());
        assertEquals("Settlement Type for FinancialTransaction 5", SettlementType.EFTPS, agencyTaxCredits.get(4).getSettlementTypeCd());
        assertEquals("Settlement Type for FinancialTransaction 6", SettlementType.EFTPS, agencyTaxCredits.get(5).getSettlementTypeCd());
        PayrollServices.commitUnitOfWork();


        //Assertion for UncollectedTaxAmount
        PayrollServices.beginUnitOfWork();
        payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        HashMap<FinancialTransaction, SpcfMoney> taxAmounts = payroll.getUncollectedTaxAmount();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Tax txns", 1, taxAmounts.size());
        for (FinancialTransaction taxTransaction : taxAmounts.keySet()) {
            assertEquals("Uncollected Tax Amount ", new SpcfMoney("0.00"), taxAmounts.get(taxTransaction));
        }
    }

    @Test
    public void testHappyPath_WithNoSupportedAgencies() {
        String psid = "123456789";
        List<Employee> emps = DataLoadServices.setupCompany(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"40"}, new String[]{"27500"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertNotNull(company);
        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
        assertNotNull(taxService);
        assertEquals("Company Service Status", ServiceSubStatusCode.ActiveCurrent, taxService.getStatusCd());

        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Complete, payroll.getPayrollRunStatus());
        assertEquals("Payroll Net Amount", new SpcfMoney("0.00"), payroll.getPayrollDirectDepositAmount());
        // Verify 2 paychecks were created
        assertEquals("Number of paychecks", 2, payroll.getPaycheckCollection().size());

        // Verify that no Agency Credits were created
        DomainEntitySet<FinancialTransaction> agencyTaxCredits = FinancialTransaction.getFinancialTransactions(payroll,
                                                                                                               TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of Agency Credit Transactions", 0, agencyTaxCredits.size());

        // Verify Employer Tax Debit Transaction wasn't created
        DomainEntitySet<FinancialTransaction> erTaxDebits = FinancialTransaction.getFinancialTransactions(payroll,
                                                                                                          TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Tax Debit Transactions", 0, erTaxDebits.size());

        PayrollServices.commitUnitOfWork();

        //Assertion for UncollectedTaxAmount
        PayrollServices.beginUnitOfWork();
        payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        HashMap<FinancialTransaction, SpcfMoney> taxAmounts = payroll.getUncollectedTaxAmount();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Tax txns", 0, taxAmounts.size());
    }

    @Test
    public void testPayrollForPriorQuarter() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-03-25"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 5, 2));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEFTPS941OnlyPayroll() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEFTPS941OnlyPayroll_With_COBRA() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "196"}, new String[]{"5", "12", "5.5", "45", "25", "-27"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));

        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"900.00", "-900.00", "0.00", "0.00", "0.00", "0.00"});
        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        printLedgerBalances(ledgerBalances);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEFTPS941100K_With_COBRA() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "196"}, new String[]{"15000", "14000", "12000", "10000", "8000", "-10000"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));

        DomainEntitySet<MoneyMovementTransaction> eftpsDirectMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPSDirectDebit));
        assertEquals("EFTPSDirectDebit MMTs", 1, eftpsDirectMMTs.size());
        assertFalse("EFTPSDirectDebit MMT is not on Hold", eftpsDirectMMTs.get(0).hasActiveOnHoldReasons());
        PayrollServices.rollbackUnitOfWork();


    }


    @Test
    public void testEFTPS941WithPAStateTaxesPayroll() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 1));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        String[] statesList = new String[]{"PA", "OH", "AZ", "KS"};
        DataLoadServices.setupCompany(1L, 10, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "PA-501-PAYMENT");
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", supportedDate);
        DataLoadServices.updateRequiredIDs(company, null, true);
        DataLoadServices.updateAgencyTaxpayerId(company,"PA-501-PAYMENT","12245678");


        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(payrollRunDTO, company, new DateDTO("2010-11-02"), emps);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        int dueDay = 18;
        int offloadDay = dueDay-achTaxOffloadOffset;
        DataLoadServices.setPSPDate(2010, 11, dueDay-achTaxOffloadOffset);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        String[] expectedOutput = {
                "101 02100002197226160001011"+String.valueOf(offloadDay)+"1325\\w094101JPMORGAN CHASE         INTUIT                         ",
                "5220TEST_COMPANY_\\d*\\s*123456789\\d{10}+CCDEFT TAX PY101118101118   1021000020000001",
                "6220430000961001342875       0000005000123456789      TEST_COMPANY_\\d*\\s*1\\d{15}+",
                "705TXP\\*12245678\\*EM340\\*101115\\*T\\*5000\\\\                                               \\d{11}+",
                "82200000020004300009000000000000000000005000\\d{10}+                         021000020000001",
                "5225INTUIT                     7700346619118556001CCDEFT TAX PY101118101118   1021000020000002",
                "627021000021911855633        0000005000911855633      INTUIT TAX              0\\d{15}+",
                "822500000100021000020000000050000000000000009118556001                         021000020000002",
                "9000002000001000000030006400011000000005000000000005000                                       ",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"
        };

        assertEquals("Offload output sizes do not match up.", lines.length, expectedOutput.length);

        for (int i = 0; i < expectedOutput.length; i++) {
            Pattern pattern = Pattern.compile(expectedOutput[i]);
            Matcher matcher = pattern.matcher(lines[i]);

            assertTrue("Did not find expected output:\n" + expectedOutput[i] + "\nIn output:\n" + lines[i], matcher.matches());
        }
    }



    /**
     * Test DE's 5 record
     */
    @Test
    public void testEFTPS941WithDEStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"DE"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111214000000" : "20111213000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);
        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        // Company name must be set to "CRI"
        boolean found = false;

        Pattern pattern = Pattern.compile("5220CRI                        1        \\d{10}+CCDEFT TAX PY111215111215   1021000020000001");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("DE 5 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test UT's 5 and 6 record
     */
    @Test
    public void testEFTPS941WithUTStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"UT"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111230000000" : "20111229000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        // Company name must not have punctuation
        boolean found5 = false;
        boolean found6 = false;

        Pattern pattern5 = Pattern.compile("5220TESTCOMPANY\\d*\\s*1        \\d{10}CCDEFT TAX PY120103120103   1021000020000001");
        Pattern pattern6 = Pattern.compile("6221210002480510805161       00000094001              TESTCOMPANY\\d*\\s*1\\d{15}");


        for (String line : lines) {
            Matcher matcher5 = pattern5.matcher(line);

            if (matcher5.matches()) {
                found5 = true;
            }

            Matcher matcher6 = pattern6.matcher(line);

            if (matcher6.matches()) {
                found6 = true;
            }
        }

        assertTrue("UT 5 record not found.  Output was:\n" + output, found5);
        assertTrue("UT 6 record not found.  Output was:\n" + output, found6);
    }

    /**
     * Test OR's 6 record
     */
    @Test
    public void testEFTPS941WithORStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"OR"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.updateAgencyTaxpayerId(company,"OR-OTCWH-PAYMENT","63259094-1");
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20120130000000" : "20120127000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // OR  Zero fill from the left  + state tax ID to populate 15 digits
        Pattern pattern = Pattern.compile("62202105205372561852         000000780000000\\d{10}+TEST_COMPANY_\\d*\\s*1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("OR 6 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test MO's 6 record
     */
    @Test
    public void testEFTPS941WithMOStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MO"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111214000000" : "20111213000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // Name should be ?MO DEPT OF REVENUE?
        // MO  ?0115000? + state tax ID
        Pattern pattern = Pattern.compile("6220865071748600500          000000520001150001224567\\d.{24}1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("MO 6 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test LA's 6 record
     */
    @Test
    public void testEFTPS941WithLAStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"LA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111129000000" : "20111128000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // LA  state tax ID
        Pattern pattern = Pattern.compile("6220654001377900406139       0000004000\\d{10}+     TEST_COMPANY_\\d*\\s*1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("LA 6 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test AL's 6 record
     */
    @Test
    public void testEFTPS941WithALStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"AL"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111214000000":"20111213000000"; 
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // AL
        //    If state tax ID is 6 bytes long ? ?000IW0000? + state tax ID
        //    If state tax ID is 10 bytes long ? ?000IW? + state tax ID
        Pattern pattern = Pattern.compile("6220830001083160062351       0000000600000IW[a-zA-Z0-9]\\d{9}+TEST_COMPANY_\\d*\\s*1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("AL 6 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test KY's 6 record
     */
    @Test
    public void testEFTPS941WithKYStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"KY"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111125000000" : "20111123000000";// 24/11/2011 is holiday
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // KY  ?011? + 1st 6 digits of state tax ID
        Pattern pattern = Pattern.compile("622083000137937190478        0000003800011123456      TEST_COMPANY_\\d*\\s*1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("KY 6 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test MT's 6 record
     */
    @Test
    public void testEFTPS941WithMTStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MT"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111214000000" : "20111213000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // MT  1st 10 digits of state tax ID + ?WTH?
        Pattern pattern = Pattern.compile("622092900383DOR156041200221  0000005600\\d{10}+WTH  TEST_COMPANY_\\d*\\s*1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("MT 6 record not found.  Output was:\n" + output, found);
    }

    @Test
    public void testMA_UI_5_Record() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> suiPayments = MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI));
        PayrollServices.rollbackUnitOfWork();
        for (MoneyMovementTransaction suiPayment : suiPayments) {
            DataLoadServices.finalizePayment(suiPayment);
        }
        PayrollServices.rollbackUnitOfWork();

        if(achTaxOffloadOffset == 1) {
        		DataLoadServices.setPSPDate(2012, 4, 27);
        } else {
        		DataLoadServices.setPSPDate(2012, 4, 26);
        }
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // MA Uses a 5 record that includes 16 digit company name, TPA Account, and FEIN.
        Pattern pattern = Pattern.compile("5220TEST_COMPANY_.{3}" + EntryDetailRecord.TPA_ACCOUNT_NUMBER + "\\s{13}9118556001CCDMA DUA\\s{4}\\d{12}\\s{3}\\d{16}");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
                break;
            }
        }

        assertTrue("MA UI 5 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test MA's 6 record
     */
    @Test
    public void testEFTPS941WithMAStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MA"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111214000000" : "20111213000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // MA  Use FEIN
        Pattern pattern = Pattern.compile("622011000206501321576        000000420\\d{10}      TEST_COMPANY_\\d*\\s*1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("MA 6 record not found.  Output was:\n" + output, found);
    }

    /**
     * Test MD's 6 record
     */
    @Test
    public void testEFTPS941WithMDStateTaxesPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String taxOffloadTime = achTaxOffloadOffset == 1? "20111109000000" : "20111108000000";
        PSPDate.setPSPTime(taxOffloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        // Name should be ?240104 WITHHOLDING?
        Pattern pattern = Pattern.compile("6221210002484104095807       00000044001              240104 WITHHOLDING      1\\d{15}+");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        assertTrue("MD 6 record not found.  Output was:\n" + output, found);
    }


    @Test
    public void testACHTaxFileSettlementDateOneDayOffSet() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};

        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);

        testACHTaxFileSettlementDateOffSet(1, companies, statesList);


    }


    @Test
    public void testACHTaxFileSettlementDateTwoDayOffSet() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 1));
        PayrollServices.commitUnitOfWork();

        String[] statesList = new String[]{"MD"};

        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding);

        testACHTaxFileSettlementDateOffSet(2, companies, statesList);

    }

    private void testACHTaxFileSettlementDateOffSet(int pDays, List<Company> pCompanies, String[] pStatesList) {
        // update the parameter value
        PayrollServices.beginUnitOfWork();
        SystemParameter sysParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        sysParam = Application.refresh(sysParam);
        String originalValue = sysParam.getSystemParameterValue();
        sysParam.setSystemParameterValue(Integer.toString(pDays));
        Application.save(sysParam);
        PayrollServices.commitUnitOfWork();


        for (Company company : pCompanies) {
            DataLoadServices.runPayrollRun(company, pStatesList,
                                           SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-11-07"), false);
        }

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        String offloadTime = (pDays == 1 ? "20111109000000" : "20111108000000");
        PSPDate.setPSPTime(offloadTime);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);
        SpcfCalendar batchDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(batchDate, pDays);
        String stringDate = StringFormatter.formatDate(batchDate, "yyMMdd");
        String[] lines = output.split(System.getProperty("line.separator"));

        boolean found = false;

        Pattern pattern = Pattern.compile("5220TEST_COMPANY_\\d*\\s*1        9118556001CCDEFT TAX PY" + stringDate.trim() + stringDate.trim() + "   10210000200\\d\\d\\d\\d\\d");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                found = true;
            }
        }

        //update the parameter value  back to original value
        PayrollServices.beginUnitOfWork();
        sysParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        sysParam = Application.refresh(sysParam);
        sysParam.setSystemParameterValue(originalValue);
        Application.save(sysParam);
        PayrollServices.commitUnitOfWork();

        assertTrue("Settlement date on Batch Record is incorrect. Output was:\n" + output, found);
    }

    @Test
    public void testNYStateTaxesPayroll() throws Exception {
        String psid = "123456789";


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 1));
        PayrollServices.commitUnitOfWork();

        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company1, null);
        DataLoadServices.addCompanyBankAccount(company1);


        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company1);
        DataLoadServices.updateCompanyService(company1, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company1, 2);
        String[] statesList = new String[]{"NY"};
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("57", "351");
        List<Company> companies = DataLoadServices.setupCompany(1L, 10, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        DateDTO payrollDate = new DateDTO("2011-01-07");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);
        }


        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "PA-501-PAYMENT");
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", supportedDate);
        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company1, "PADOR");

        // companyAgency.setAgencyTaxpayerId("123456");
        Application.save(companyAgency);
        PayrollServices.commitUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company1, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 20));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(payrollRunDTO, company1, new DateDTO("2011-01-22"), emps);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Offload Employee Credit transaction
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone()));

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company1, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 5, 2));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAZStateTaxesPayroll() throws Exception {
        String psid = "123456789";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company1, null);
        DataLoadServices.addCompanyBankAccount(company1);


        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company1);
        DataLoadServices.updateCompanyService(company1, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company1, 2);
        String[] statesList = new String[]{"AZ"};
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("5", "351");
        DateDTO payrollDate = new DateDTO("2011-01-07");
        List<Company> companies = DataLoadServices.setupCompany(1L, 10, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);
        }

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "PA-501-PAYMENT");
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", supportedDate);
        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company1, "PADOR");

        // companyAgency.setAgencyTaxpayerId("123456");
        Application.save(companyAgency);
        PayrollServices.commitUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company1, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 25));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(payrollRunDTO, company1, new DateDTO("2011-11-02"), emps);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        PSPDate.setPSPTime("20111108000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company1, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2012, 1, 31));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEFTPS941OnlyWithMultiplePayroll() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();


        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-20"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    @Ignore("Just for performance test setup")
    public void testALotOfPayrolls() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        List<PayrollRunDTO> dtoList = new ArrayList<PayrollRunDTO>();
        for (Integer psid = 1; psid < 100; psid++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid.toString(), false, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, null);
            DataLoadServices.addCompanyBankAccount(company);

            DataLoadServices.addFederalTaxCompanyLaws(company);
            DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
            List<Employee> emps = DataLoadServices.addEEs(company, 3);

            DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid.toString(), "IRS-941-PAYMENT");

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64"}, new String[]{"5000", "5000"});
            dtoList.add(payrollDTO);
            PayrollServices.rollbackUnitOfWork();


        }

        StopWatch sw = new StopWatch();
        sw.start();
        PayrollServices.beginUnitOfWork();
        for (Integer psid = 1; psid < 100; psid++) {
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid.toString(), dtoList.get(psid-1));
            assertSuccess(processResult);
        }
        PayrollServices.commitUnitOfWork();
        System.out.println(sw.toString());


    }

    @Test
    public void test100KWithOrderedPayrolls() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone());

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64"}, new String[]{"5000", "5000"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-24"), emps, new String[]{"61"}, new String[]{"10000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("60000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-02-04"), emps, new String[]{"61"}, new String[]{"10000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO2, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.commitUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110120000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110202000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2011-02-18"), emps, new String[]{"61", "62"}, new String[]{"5000", "5000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("15000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 2);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("15000"), TransactionTypeCode.AgencyDirectCredit, null, 2);
        DomainEntitySet<EftpsEnrollment> eftpsEnrollments = EftpsEnrollment.getPendingEftpsEnrollments(10);
        assertEquals("EFTPS Payments pending for EFTPSEnrollments", 1, eftpsEnrollments.size());
        assertEquals("Pending EFTPS enrollment Company", company, eftpsEnrollments.get(0).getCompanyAgency().getCompany());
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find();
        assertEquals("EFTPS holds", 1, moneyMovementTransactions.size());
        assertEquals("EFTPS payment hold reasons", 1, moneyMovementTransactions.get(0).getActiveOnHoldReasons().size());
        assertEquals("EFTPS hold reason", PaymentOnHoldReason.Enrollment, moneyMovementTransactions.get(0).getActiveOnHoldReasons().get(0).getOnHoldReasonCd());
        DomainEntitySet<MoneyMovementTransaction> eftpsDirectMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPSDirectDebit));
        assertEquals("EFTPSDirectDebit MMTs", 1, eftpsDirectMMTs.size());
        assertFalse("EFTPSDirectDebit MMT is not on Hold", eftpsDirectMMTs.get(0).hasActiveOnHoldReasons());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        eftpsEnrollments = EftpsEnrollment.getPendingEftpsEnrollments(10);
        assertEquals("EFTPS Enrollments", 0, eftpsEnrollments.size());
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find();
        assertEquals("EFTPS holds", 0, moneyMovementTransactions.size());
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setNonDirect().find();
        assertEquals("EFTPS Payments", 1, moneyMovementTransactions.size());
        assertEquals("EFTPS Payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransactions.get(0).getTaxPaymentStatus());
        eftpsDirectMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPSDirectDebit));
        assertEquals("EFTPSDirectDebit MMTs", 1, eftpsDirectMMTs.size());
        assertTrue("EFTPSDirectDebit MMT is on Hold", !eftpsDirectMMTs.get(0).hasActiveOnHoldReasons());
        assertEquals("EFTPSDirectDebit Payment status", TaxPaymentStatus.ReadyToSend, eftpsDirectMMTs.get(0).getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void test100KWithOrderedPayrollsNoOffload() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64"}, new String[]{"5000", "5000"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-24"), emps, new String[]{"61"}, new String[]{"10000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("60000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-02-04"), emps, new String[]{"61"}, new String[]{"10000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO2, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2011-02-18"), emps, new String[]{"61", "62"}, new String[]{"5000", "5000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun);
        assertEquals("no EFTPS txns", 0, Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)).size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), 1);

        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("30000"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Cancelled, 4);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("15000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 4);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("30000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 2);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("15000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 4);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("30000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 2);

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void test100KWithUnOrderedPayrolls() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61"}, new String[]{"12500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("25000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-24"), emps, new String[]{"61"}, new String[]{"15000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("55000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-02-18"), emps, new String[]{"61"}, new String[]{"17500"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO2, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.commitUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110120000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110216000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110217080000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2011-02-17"), emps, new String[]{"61"}, new String[]{"15000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun);

        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("30000"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("30000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("30000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 1);

        DataLoadServices.assertEffectiveDepositFreq(SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void test100KWithUnorderedPayrollsWithOffload() throws Exception {

        DataLoadServices.setPSPDate(2012, 1, 1);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        // payroll 1
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 3, 26, 10, 7, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2012-03-30"), emps, new String[]{"1"}, new String[]{"22817.81"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun1);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun1, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2012, 4, 4));
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("22817.81"), SpcfCalendar.createInstance(2012, 4, 3, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 4, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        // payroll 2
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 3, 26, 10, 53, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2012-03-28"), emps, new String[]{"1"}, new String[]{"4422.40"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun2);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO2, payrollRun2, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2012, 4, 4));
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("27240.21"), SpcfCalendar.createInstance(2012, 4, 3, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 4, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        // offload 3/26
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2012, 3, 26, SpcfTimeZone.getLocalTimeZone()));

        // payroll 3
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 3, 26, 5, 40, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2012-03-29"), emps, new String[]{"1"}, new String[]{"373.21"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun3 = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun3);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO3, payrollRun3, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2012, 4, 4));
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("27613.42"), SpcfCalendar.createInstance(2012, 4, 3, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 4, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        // payroll 4 100k
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 3, 27, 1, 6, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO4 = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO4);
        PayrollRunDTO payrollDTO4 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO4, company, new DateDTO("2012-03-29"), emps, new String[]{"1"}, new String[]{"101202.04"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO4));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun4 = PayrollRun.findPayrollRun(company, payrollRunDTO4.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO4, payrollRun4);

        // payroll 1 should still be sent on the normal due date since the paycheck date is after the 100k accumulation
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("22817.81"), SpcfCalendar.createInstance(2012, 4, 3, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 4, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), 1);

        // since the debit for payroll 2 already offloaded there should be a next day eftps payment to satisfy the 100k rules. payroll 2 is included in the 100k accumulation
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("4422.40"), SpcfCalendar.createInstance(2012, 3, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), 1);

        // there should be 2 direct debits for payrolls 3 and 4
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("373.21"), SpcfCalendar.createInstance(2012, 3, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 29, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("101202.04"), SpcfCalendar.createInstance(2012, 3, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 30, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2012, 3, 29, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test100KWithUnOrderedPayrollsNoOffload() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61"}, new String[]{"12500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("25000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-24"), emps, new String[]{"61"}, new String[]{"15000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("55000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-02-18"), emps, new String[]{"61"}, new String[]{"17500"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO2, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2011-02-04"), emps, new String[]{"61"}, new String[]{"15000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun);

        assertEquals("no EFTPS txns", 0, Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)).size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("25000"), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("35000"), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 22, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), 1);

        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("30000"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Cancelled, 2);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("35000"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("25000"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("25000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("35000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("30000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 2);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("25000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("35000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("30000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 2);

        DataLoadServices.assertEffectiveDepositFreq(SpcfCalendar.createInstance(2011, 2, 19, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void test100KWithUnOrderedPayrollsAndSubsequent() throws Exception {

        String psid = "123456789";
        test100KWithUnOrderedPayrolls();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 2, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        List<Employee> emps = new ArrayList<Employee>();
        for (Employee emp : employees) {
            emps.add(emp);
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2011-03-04"), emps, new String[]{"61"}, new String[]{"15000"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun);

        //test the other txns were not changed
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 17, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("30000"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("30000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("30000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 1);

        //test the new ones were created
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 3, 8, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 9, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 4, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("30000"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Created, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("30000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created, 2); //1 from before, 1 on this one
        DataLoadServices.assertEffectiveDepositFreq(SpcfCalendar.createInstance(2011, 2, 18));

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void test100KSemiWeekly1() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 1, 12);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-05"), emps, new String[]{"61"}, new String[]{"27500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110103000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.OffloadedAll, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("55000"), SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 12, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-06"), emps, new String[]{"61"}, new String[]{"25000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("55000"), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("50000"), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("50000"), TransactionTypeCode.EmployerTaxDirectDebit, null, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("50000"), TransactionTypeCode.AgencyDirectCredit, null, 1);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test100KSemiWeekly2() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-05"), emps, new String[]{"61"}, new String[]{"52500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("105000"), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-06"), emps, new String[]{"61"}, new String[]{"25000"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("105000"), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("50000"), SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 12, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("105000"), TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("50000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created, 1);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCurrentQuarterBackdatedPayroll() {
        //PSP calculates payment initiation date as the later of the following
        //o	The payment due date
        //o	The next possible payment initiation date after the impound is offloaded
        //?	(If the paycheck date were in the prior quarter, this would be the next possible payment initiation date after the impound will have completed)

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 1, 21);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyService companyService = company.getService(ServiceCode.Tax);
        companyService.setServiceStartDate(SpcfCalendar.createInstance(2011, 1, 1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-15"), emps, new String[]{"61"}, new String[]{"12500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(processResult);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("25000"), SpcfCalendar.createInstance(2011, 1, 27, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 21, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 15, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 18, SpcfTimeZone.getLocalTimeZone()), 1);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = company.findPendingTaxPayments();
        assertEquals("EFTPS MMTs", 1, moneyMovementTransactions.size());
        SpcfCalendar initDate = moneyMovementTransactions.get(0).getInitiationDate().copy();
        CalendarUtils.addBusinessDays(initDate, 1);
        for (FinancialTransaction financialTransaction : moneyMovementTransactions.get(0).getFinancialTransactionCollection()) {
            assertEquals("FT Settlement Date", initDate, financialTransaction.getSettlementDate());
        }
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    @Ignore("Previous supported quarters are not yet coded")
    //todo R3
    public void testPreviousSupportedQuarterBackDatedPayroll() {
        //TODO -- Needs to be revisited to correct init date
        //PSP calculates payment initiation date as the later of the following
        //o	The payment due date
        //o	The next possible payment initiation date after the impound is offloaded
        //?	(If the paycheck date were in the prior quarter, this would be the next possible payment initiation date after the impound will have completed)

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        //DataLoadServices.updateEFTPS941EffectiveDepositeFreqEffDate(psid, "IRS-941-PAYMENT");
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-03-31"), emps, new String[]{"61"}, new String[]{"12500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(processResult);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("25000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPreviousQuarterBackDatedPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 04, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-02-24"), emps, new String[]{"61"}, new String[]{"12500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(processResult);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        //Check liabilities are stored
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            DataLoadServices.assertTax(paycheck, "61", new SpcfMoney("12500"));
        }
        assertEquals("Number of Money movement transactions:", 0, Application.find(MoneyMovementTransaction.class).size());
        assertEquals("Number of Financial transactions:", 0, Application.find(FinancialTransaction.class).size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testThreshold_With_RecalculatePaymentMethods() {
        //Todo - [Not fixed. we need to fix for Initiation date]
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        lawAmounts.put("1", "10");
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-18");

        DataLoadServices.setPSPDate(2011, 12, 27);

        //Disable ACH Credit payment
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "NY-1MN-PAYMENT", PaymentMethod.ACHCredit, false));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 28);

        payrollDate = new DateDTO("2012-01-15");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("Payment Initiation date", SpcfCalendar.createInstance(2012, 1, 19, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Payment Due date", SpcfCalendar.createInstance(2012, 1, 23, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        //Enable ACH Credit payment
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "NY-1MN-PAYMENT", PaymentMethod.ACHCredit, true));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "NYDTF");
        companyAgency.recalculatePaymentMethods();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("Payment Due date", SpcfCalendar.createInstance(2012, 1, 23, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2012, 1, 23, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        assertEquals("Payment Initiation date", statePaymentInitiationDate, moneyMovementTransaction.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPaymentOffloadBeforeDebitOffload() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        DataLoadServices.claimNoFeesOffer(company);

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");

        //Disable ACH Credit payment
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "NY-1MN-PAYMENT", PaymentMethod.ACHCredit, false));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 28);

        DateDTO payrollDate = new DateDTO("2012-01-18");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        DataLoadServices.setPSPDate(2012, 1, 5);
        SpcfCalendar newInitiationDate = SpcfCalendar.createInstance(2012, 1, 5, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).find());
        assertSuccess(PayrollServices.paymentManager.updateInitiationDate(moneyMovementTransaction.getId().toString(), newInitiationDate));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).find());
        assertEquals("payment date did not change", newInitiationDate, moneyMovementTransaction.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).find());
        assertEquals("Status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        FinancialTransaction paymentTransaction = moneyMovementTransaction.getFinancialTransactionCollection().get(0);
        FinancialTransaction employerDebit = paymentTransaction.getPayrollRun().getFinancialTransactionCollection()
                                                               .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)
                                                                                               .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        assertNotNull("employer debit does not exist", employerDebit);
        assertEquals("$400 debit", new SpcfMoney("400.00"), employerDebit.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(paymentTransaction.getPayrollRun().getPaycheckCollection().get(0).getSourcePaycheckId());
        transactionCancelDTO.setSourcePayrollRunId(paymentTransaction.getPayrollRun().getSourcePayRunId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), transactionCancelDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012, 4, 26);
        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        SftpFactory.setInstanceClass(Transporter.class);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).find());
        assertEquals("Status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        paymentTransaction = moneyMovementTransaction.getFinancialTransactionCollection().get(0);
        employerDebit = paymentTransaction.getPayrollRun().getFinancialTransactionCollection()
                                          .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)
                                                                          .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        assertNotNull("employer debit does not exist", employerDebit);
        assertEquals("$200 debit", new SpcfMoney("200.00"), employerDebit.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testThreshold_reversal_withRecall() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        lawAmounts.put("1", "10");
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-18");

        DataLoadServices.setPSPDate(2011, 12, 27);

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        DataLoadServices.setPSPDate(2011, 12, 28);

        payrollDate = new DateDTO("2012-01-15");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        PayrollServices.beginUnitOfWork();
        PayrollRun secPayrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 12, 28, SpcfTimeZone.getLocalTimeZone()), null));
        transactionCancelDTO.setSourcePayrollRunId(secPayrollRun.getSourcePayRunId());
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        payrollDate = new DateDTO("2012-01-05");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

        PayrollServices.beginUnitOfWork();
        recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testThreshold_NY_MTA305() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate capt =CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company,PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));
        capt.updateAgencyTaxpayerId("");
        PayrollServices.commitUnitOfWork();

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        lawAmounts.put("197", "10");
        lawAmounts.put("1", "10");
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-18");

        DataLoadServices.setPSPDate(2011, 12, 27);

        DataLoadServices.runPayrollRunNYMetro(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("472.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.CheckPayment, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("Payment Initiation", SpcfCalendar.createInstance(2012, 4, 26, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 28);

        payrollDate = new DateDTO("2012-01-15");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("492.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.CheckPayment, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("Payment Initiation", SpcfCalendar.createInstance(2012, 4, 26, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        // enable ach for ny metro
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        capt =CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company,PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));
        capt.setAgencyTaxpayerId(company.getFedTaxId()+" 0");
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), new AgencyIdDTO("NY-MTA305-PAYMENT", "State Access Code", company.getFedTaxId()+" 0")));
        for (CompanyAgency companyAgency : company.getCompanyAgencyCollection()) {
            DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Agency().equalTo(companyAgency.getAgency()));
            for (PaymentTemplate paymentTemplate : paymentTemplates) {
                PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true);
            }
        }
        PayrollServices.commitUnitOfWork();

        // metro payment should follow NY WH payment threshold once it is ACH Credit
        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("492.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2012, 1, 23, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        assertEquals("Payment Initiation", statePaymentInitiationDate, moneyMovementTransaction.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_NY_MTA305_CheckPayment_Frequency() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate capt =CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company,PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));
        capt.updateAgencyTaxpayerId("");
        PayrollServices.commitUnitOfWork();

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        lawAmounts.put("197", "10");
        lawAmounts.put("1", "10");
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-18");

        DataLoadServices.setPSPDate(2011, 12, 27);

        // metro uses freq of NY-1MN-PAYMENT
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-1MN-PAYMENT", DepositFrequencyCode.FIVEBANKINGDAY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRunNYMetro(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT"))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("472.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment method", PaymentMethod.CheckPayment, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        // update the deposit frequency to 3D
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-1MN-PAYMENT", DepositFrequencyCode.THREEBANKINGDAY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        // the payment should not have changed
        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT"))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("472.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment method", PaymentMethod.CheckPayment, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        // enable ach for ny metro
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        capt =CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company,PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));
        capt.setAgencyTaxpayerId(company.getFedTaxId()+" 0");
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), new AgencyIdDTO("NY-MTA305-PAYMENT", "State Access Code", company.getFedTaxId()+" 0")));
        for (CompanyAgency companyAgency : company.getCompanyAgencyCollection()) {
            DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Agency().equalTo(companyAgency.getAgency()));
            for (PaymentTemplate paymentTemplate : paymentTemplates) {
                PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true);
            }
        }
        PayrollServices.commitUnitOfWork();

        // payment should revert to effective deposit freq
        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT"))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("472.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testThreshold_NY_SameDayPayroll() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        lawAmounts.put("197", "10");
        lawAmounts.put("1", "10");
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-15");

        DataLoadServices.setPSPDate(2011, 12, 27);

        DataLoadServices.runPayrollRunNYMetro(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 28);

        payrollDate = new DateDTO("2012-01-15");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);




    }

    @Test
    public void testThreshold_NY_MTA305_PSRV003105() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "40");
        lawAmounts.put("54", "40");
        lawAmounts.put("56", "35");
        lawAmounts.put("57", "35");
        lawAmounts.put("197", "10");
        lawAmounts.put("1", "3500");
        lawAmounts.put("61", "3500");
        lawAmounts.put("62", "3500");
        lawAmounts.put("63", "3500");
        lawAmounts.put("64", "3500");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-06");

        DataLoadServices.setPSPDate(2011, 12, 27);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-MTA305-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-1MN-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRunNYMetro(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("300.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 28);

        payrollDate = new DateDTO("2012-01-12");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        payrollDate = new DateDTO("2012-01-20");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("900.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        assertForThresholdDepositFrequency(company);

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2012, 1, 10, SpcfTimeZone.getLocalTimeZone()));

        SpcfCalendar secondPayrollRunDate = SpcfCalendar.createInstance(2012, 1, 12, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.beginUnitOfWork();
        PayrollRun secondPayrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(secondPayrollRunDate)));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 29);
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(secondPayrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

        //Validate for Deposit Frequencies
        PayrollServices.beginUnitOfWork();
        PaymentTemplate nyMetroPaymentTemplate = PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT");
        PaymentTemplate nyWithholdingPaymentTemplate = PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT");
        PaymentTemplate irsPaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        DomainEntitySet<EffectiveDepositFrequency> activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyMetroPaymentTemplate, null, true);
        DomainEntitySet<EffectiveDepositFrequency> inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyMetroPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyWithholdingPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyWithholdingPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, irsPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, irsPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 2, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies = inValidEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.SEMIWEEKLY)
                                                                                                                                                  .And(EffectiveDepositFrequency.EffectiveDate().equalTo(SpcfCalendar.createInstance(2012, 1, 21, SpcfTimeZone.getLocalTimeZone()))));
        Assert.assertEquals("SEMIWEEKLY Effective deposit frequency added after Threshold (IRS-941)", 1, effectiveDepositFrequencies.size());

        PayrollServices.rollbackUnitOfWork();

        lawAmounts.put("36", "1");
        lawAmounts.put("54", "1");
        lawAmounts.put("56", "1");
        lawAmounts.put("57", "1");

        payrollDate = new DateDTO("2012-01-21");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        lawAmounts.put("36", "1");
        lawAmounts.put("54", "1");
        lawAmounts.put("56", "1");
        lawAmounts.put("57", "1");

        payrollDate = new DateDTO("2012-01-21");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        lawAmounts.put("36", "1");
        lawAmounts.put("54", "1");
        lawAmounts.put("56", "1");
        lawAmounts.put("57", "1");

        payrollDate = new DateDTO("2012-01-21");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyMetroPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyMetroPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyWithholdingPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyWithholdingPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, irsPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, irsPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 2, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 2, activeEffectiveDepositFrequencies.size());

        effectiveDepositFrequencies = inValidEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.SEMIWEEKLY)
                                                                                                       .And(EffectiveDepositFrequency.EffectiveDate().equalTo(SpcfCalendar.createInstance(2012, 1, 21, SpcfTimeZone.getLocalTimeZone()))));
        Assert.assertEquals("SEMIWEEKLY Effective deposit frequency added after Threshold (IRS-941)", 1, effectiveDepositFrequencies.size());

        effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.SEMIWEEKLY)
                                                                                                      .And(EffectiveDepositFrequency.EffectiveDate().equalTo(SpcfCalendar.createInstance(2012, 1, 22, SpcfTimeZone.getLocalTimeZone()))));
        Assert.assertEquals("SEMIWEEKLY Effective deposit frequency added after Threshold (IRS-941)", 1, effectiveDepositFrequencies.size());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testThreshold_CA_VoidResubmit_ACHCredit() {
        String[] statesList = {"CA"};
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        // submit payroll for Mon
        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("6", "2000");
        lawAmounts.put("1", "55001");
        lawAmounts.put("61", "10000");
        lawAmounts.put("62", "15000");
        lawAmounts.put("63", "10000");
        lawAmounts.put("64", "10000");
        lawAmounts.put("66", "800");
        lawAmounts.put("67", "0");

        DateDTO payrollDate = new DateDTO("2012-02-06");

        DataLoadServices.setPSPDate(2012, 2, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Amount", SpcfMoney.createInstance("4000.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // submit payroll for Wed
        lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("6", "150");
        lawAmounts.put("1", "160");
        lawAmounts.put("61", "150");
        lawAmounts.put("62", "200");
        lawAmounts.put("63", "250");
        lawAmounts.put("64", "125");
        lawAmounts.put("66", "100");
        lawAmounts.put("67", "0");

        payrollDate = new DateDTO("2012-02-08");

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        // offload debit and EFTPS direct payment
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2012, 2, 2, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar achOffloadDate = SpcfCalendar.createInstance(2012, 2, 7, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(achOffloadDate, -achTaxOffloadOffset);

        DataLoadServices.runOffloadTaxPayments(achOffloadDate);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2012, 2, 6, SpcfTimeZone.getLocalTimeZone()));

        // void 1 paycheck from the monday payroll, to create ATOAs on the Wed payroll payment
        PayrollServices.beginUnitOfWork();
        PayrollRun mondayPayroll = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 2, 6, SpcfTimeZone.getLocalTimeZone()))));
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(mondayPayroll.getSourcePayRunId());
        voidPayrollDTO.setPaycheckIdList(new ArrayList<String>());
        voidPayrollDTO.getPaycheckIdList().add(mondayPayroll.getPaycheckCollection().get(0).getSourcePaycheckId());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class,
                                                              MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit)
                                                                                      .And(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.Ignore))));
        assertEquals(SpcfMoney.ZERO, moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertOne(moneyMovementTransaction.getFinancialTransactionCollection()
                                          .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit)));
        PayrollServices.rollbackUnitOfWork();

        // recall a paycheck from the wed payroll
        PayrollServices.beginUnitOfWork();
        PayrollRun wednesdayPayroll = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 2, 8, SpcfTimeZone.getLocalTimeZone()))));
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePayrollRunId(wednesdayPayroll.getSourcePayRunId());
        transactionCancelEEDTO.setSourcePaycheckIdList(new ArrayList<String>());
        transactionCancelEEDTO.getSourcePaycheckIdList().add(wednesdayPayroll.getPaycheckCollection().get(0).getSourcePaycheckId());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), transactionCancelEEDTO);
        assertSuccess(recallProcessResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testThreshold_NY_PSRV003109() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "40");
        lawAmounts.put("54", "40");
        lawAmounts.put("56", "35");
        lawAmounts.put("57", "35");
        lawAmounts.put("197", "10");
        lawAmounts.put("1", "3500");
        lawAmounts.put("61", "3500");
        lawAmounts.put("62", "3500");
        lawAmounts.put("63", "3500");
        lawAmounts.put("64", "3500");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-06");

        DataLoadServices.setPSPDate(2011, 12, 27);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-MTA305-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-1MN-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRunNYMetro(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("300.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        DataLoadServices.setPSPDate(2011, 12, 28);

        payrollDate = new DateDTO("2012-01-12");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        payrollDate = new DateDTO("2012-01-20");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        DataLoadServices.setPSPDate(2011, 12, 29);
        payrollDate = new DateDTO("2012-01-20");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find()
                                                                     .find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("1200.00"))));
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();

        assertForThresholdDepositFrequency(company);

        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();

        PayrollServices.beginUnitOfWork();
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find()
                                                                     .find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("900.00"))));
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();

        assertForThresholdDepositFrequency(company);

        PayrollServices.beginUnitOfWork();
        PayrollRun secPayrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 12, 29, SpcfTimeZone.getLocalTimeZone()), null));
        transactionCancelDTO.setSourcePayrollRunId(secPayrollRun.getSourcePayRunId());
        recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find()
                                                                     .find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("600.00"))));
        assertEquals("Payment Frequency ", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());  //Payment template frequency is reverted here
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate nyMetroPaymentTemplate = PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT");
        PaymentTemplate nyWithholdingPaymentTemplate = PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT");
        PaymentTemplate irsPaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        DomainEntitySet<EffectiveDepositFrequency> activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyMetroPaymentTemplate, null, true);
        DomainEntitySet<EffectiveDepositFrequency> inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyMetroPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyWithholdingPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyWithholdingPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, irsPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, irsPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 2, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies = inValidEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.SEMIWEEKLY)
                                                                                                                                                  .And(EffectiveDepositFrequency.EffectiveDate().equalTo(SpcfCalendar.createInstance(2012, 1, 21, SpcfTimeZone.getLocalTimeZone()))));
        Assert.assertEquals("SEMIWEEKLY Effective deposit frequency added after Threshold (IRS-941)", 1, effectiveDepositFrequencies.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testThreshold_CombiningPayments_PSRV003147() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "40");
        lawAmounts.put("54", "40");
        lawAmounts.put("56", "35");
        lawAmounts.put("57", "35");
        lawAmounts.put("197", "10");
        lawAmounts.put("1", "3500");
        lawAmounts.put("61", "3500");
        lawAmounts.put("62", "3500");
        lawAmounts.put("63", "3500");
        lawAmounts.put("64", "3500");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2012-01-06");

        DataLoadServices.setPSPDate(2011, 12, 27);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-MTA305-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-1MN-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRunNYMetro(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT"))
                                                                                              .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("300.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        DataLoadServices.setPSPDate(2011, 12, 28);

        payrollDate = new DateDTO("2012-01-12");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        PayrollRun secondPayrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 12, 28, SpcfTimeZone.getLocalTimeZone()), null));
        PayrollServices.rollbackUnitOfWork();

        payrollDate = new DateDTO("2012-01-20");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        DataLoadServices.setPSPDate(2011, 12, 29);
        payrollDate = new DateDTO("2012-01-20");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find()
                                                                     .find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("1200.00"))));
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();

        assertForThresholdDepositFrequency(company);

        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();

        PayrollServices.beginUnitOfWork();
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find()
                                                                     .find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("900.00"))));
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();

        assertForThresholdDepositFrequency(company);

        PayrollServices.beginUnitOfWork();
        transactionCancelDTO.setSourcePayrollRunId(secondPayrollRun.getSourcePayRunId());
        recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT")).setCompany(company).setReadyToSend().find()
                                                                     .find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("600.00"))));  // Payments are combined
        assertEquals("Payment Frequency ", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());  //Payment template frequency is reverted here
        PayrollServices.rollbackUnitOfWork();
    }

    void assertForThresholdDepositFrequency(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        PaymentTemplate nyMetroPaymentTemplate = PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT");
        PaymentTemplate nyWithholdingPaymentTemplate = PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT");
        PaymentTemplate irsPaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        DomainEntitySet<EffectiveDepositFrequency> activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(pCompany, nyMetroPaymentTemplate, null, true);
        DomainEntitySet<EffectiveDepositFrequency> inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(pCompany, nyMetroPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId()
                                                                                                                                                 .equalTo(DepositFrequencyCode.QUARTERLY)
                                                                                                                                                 .And(EffectiveDepositFrequency.EffectiveDate().equalTo(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()))));
        Assert.assertEquals("QUARTERLY Effective deposit frequency added after Threshold (NY-MTA305)", 1, effectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(pCompany, nyWithholdingPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(pCompany, nyWithholdingPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());

        effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.QUARTERLY)
                                                                                                      .And(EffectiveDepositFrequency.EffectiveDate().equalTo(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()))));
        Assert.assertEquals("QUARTERLY Effective deposit frequency added after Threshold (NY-1MN)", 1, effectiveDepositFrequencies.size());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(pCompany, irsPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(pCompany, irsPaymentTemplate, null, false);
        assertEquals("Invalid deposit Deposit frequencies", 1, inValidEffectiveDepositFrequencies.size());
        assertEquals("Active deposit Deposit frequencies", 2, activeEffectiveDepositFrequencies.size());

        effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.SEMIWEEKLY)
                                                                                                      .And(EffectiveDepositFrequency.EffectiveDate().equalTo(SpcfCalendar.createInstance(2012, 1, 21, SpcfTimeZone.getLocalTimeZone()))));
        Assert.assertEquals("SEMIWEEKLY Effective deposit frequency added after Threshold (IRS-941)", 1, effectiveDepositFrequencies.size());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, EventTypeCode.ThresholdExceeded);
        assertEquals("Threshold Exceeded events", 3, companyEvents.size());

        DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEvent.findCompanyEventDetails(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.PaymentTemplate, irsPaymentTemplate.getPaymentTemplateCd());
        assertEquals("IRS Threshold exceeded companyEvent detail", 1, companyEventDetails.size());
        companyEventDetails = CompanyEvent.findCompanyEventDetails(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.PaymentTemplate, nyMetroPaymentTemplate.getPaymentTemplateCd());
        assertEquals("NY Metro Threshold exceeded companyEvent detail", 1, companyEventDetails.size());
        companyEventDetails = CompanyEvent.findCompanyEventDetails(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.PaymentTemplate, nyWithholdingPaymentTemplate.getPaymentTemplateCd());
        assertEquals("NY WH exceeded companyEvent detail", 1, companyEventDetails.size());
        companyEventDetails = CompanyEvent.findCompanyEventDetails(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.ThresholdPeriodStartDate, "1/1/2012");
        assertEquals("Threshold exceeded companyEvent detail with Start dates", 3, companyEventDetails.size());
        companyEventDetails = CompanyEvent.findCompanyEventDetails(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.ThresholdPeriodEndDate, "1/20/2012");
        assertEquals("Threshold exceeded companyEvent detail with End dates", 3, companyEventDetails.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testThreshold_reversal_withRecall_MultipleDFs() {
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        lawAmounts.put("1", "10");
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("65", "6.5");
        lawAmounts.put("66", "6.6");

        DateDTO payrollDate = new DateDTO("2011-12-18");

        DataLoadServices.setPSPDate(2011, 12, 12);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-1MN-PAYMENT", DepositFrequencyCode.FIVEBANKINGDAY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        PaymentTemplate nyPaymentTemplate = PaymentTemplate.findPaymentTemplate("NY-1MN-PAYMENT");
        SpcfCalendar taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);

        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                                              .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        payrollDate = new DateDTO("2012-01-04");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 11, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.FIVEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());

        DomainEntitySet<EffectiveDepositFrequency> activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyPaymentTemplate, null, true);
        DomainEntitySet<EffectiveDepositFrequency> inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyPaymentTemplate, null, false);
        Assert.assertEquals("Invalid deposit Deposit frequencies", 0, inValidEffectiveDepositFrequencies.size());
        Assert.assertEquals("Active deposit Deposit frequencies", 2, activeEffectiveDepositFrequencies.size());
        DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.QUARTERLY));
        Assert.assertEquals("QUARTERLY Effective deposit frequency", 1, effectiveDepositFrequencies.size());
        Assert.assertEquals("QUARTERLY Effective deposit frequency effective date", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), effectiveDepositFrequencies.get(0).getEffectiveDate().toLocal());
        effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.FIVEBANKINGDAY));
        Assert.assertEquals("FIVEBANKINGDAY Effective deposit frequency", 1, effectiveDepositFrequencies.size());
        Assert.assertEquals("FIVEBANKINGDAY Effective deposit frequency effective date", SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), effectiveDepositFrequencies.get(0).getEffectiveDate().toLocal());

        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 14);
        payrollDate = new DateDTO("2011-12-24");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        taxInitiationDate = SpcfCalendar.createInstance(2011, 12, 29, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);

        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate).setCompany(company).setReadyToSend()
                                                                     .setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 11, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.FIVEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyPaymentTemplate, null, false);
        Assert.assertEquals("Invalid deposit Deposit frequencies", 1, inValidEffectiveDepositFrequencies.size());
        Assert.assertEquals("Active deposit Deposit frequencies", 1, activeEffectiveDepositFrequencies.size());
        effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.QUARTERLY));
        assertEquals("QUARTERLY Effective deposit frequency", 1, effectiveDepositFrequencies.size());
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, nyPaymentTemplate, SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Effective deposit frequency effective on 1/1/2011", DepositFrequencyCode.QUARTERLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, nyPaymentTemplate, SpcfCalendar.createInstance(2011, 12, 25, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Effective deposit frequency added after threshold", DepositFrequencyCode.QUARTERLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 12, 20);
        lawAmounts.put("36", "5");
        lawAmounts.put("54", "5");
        lawAmounts.put("56", "5");
        lawAmounts.put("57", "5");
        payrollDate = new DateDTO("2012-01-08");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        taxInitiationDate = SpcfCalendar.createInstance(2011, 12, 29, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);

        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate).setCompany(company).setReadyToSend()
                                                                     .setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.THREEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("800.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 11, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.FIVEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        
        taxInitiationDate = SpcfCalendar.createInstance(2012, 4, 30, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("40.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        PayrollServices.beginUnitOfWork();
        PayrollRun secPayrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 12, 14, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 12, 15, SpcfTimeZone.getLocalTimeZone())));
        transactionCancelDTO.setSourcePayrollRunId(secPayrollRun.getSourcePayRunId());
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 11, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.FIVEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 13, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.FIVEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("40.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());

        activeEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyPaymentTemplate, null, true);
        inValidEffectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, nyPaymentTemplate, null, false);
        Assert.assertEquals("Invalid deposit Deposit frequencies", 1, inValidEffectiveDepositFrequencies.size());
        Assert.assertEquals("Active deposit Deposit frequencies", 2, activeEffectiveDepositFrequencies.size());
        effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.QUARTERLY));
        assertEquals("QUARTERLY Effective deposit frequency", 1, effectiveDepositFrequencies.size());
        effectiveDepositFrequencies = activeEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.FIVEBANKINGDAY));
        assertEquals("FIVEBANKINGDAY Effective deposit frequency", 1, effectiveDepositFrequencies.size());

        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, nyPaymentTemplate, SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Effective deposit frequency added after threshold", DepositFrequencyCode.QUARTERLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, nyPaymentTemplate, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        assertEquals("Effective deposit frequency added after threshold", DepositFrequencyCode.FIVEBANKINGDAY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        assertEquals("Invalid Effective deposit frequency", 1, inValidEffectiveDepositFrequencies.find(EffectiveDepositFrequency.PaymentTemplateFrequency().PaymentFrequencyId().equalTo(DepositFrequencyCode.FIVEBANKINGDAY)).size());
        PayrollServices.rollbackUnitOfWork();

        payrollDate = new DateDTO("2012-01-08");
        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());

        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 11, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.FIVEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("400.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        
        taxInitiationDate = SpcfCalendar.createInstance(2012, 1, 13, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(nyPaymentTemplate)
                                                                     .setCompany(company).setReadyToSend().setInitiationDate(taxInitiationDate).find());
        assertEquals("Payment Frequency", DepositFrequencyCode.FIVEBANKINGDAY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("80.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

    }

    private void displayPayroll(PayrollRun payroll) {
        logger.warn("payrollrun = " + payroll.getId());
        logger.warn("payrollRun.settlementDate = " + payroll.getPaycheckSettlementDate());
        displayLiablities(PayrollSubmitHelper.getInstance().getLiabilityBalances(payroll));
        displayFinancialTransactions(payroll.getFinancialTransactionCollection());
    }

    private void displayLiablities(Map<Law, SpcfDecimal> liabilities) {
        for (Law law : liabilities.keySet()) {
            logger.warn("law = " + law.getLawId() + " amount = " + liabilities.get(law));
        }
    }

    private HashMap<LedgerAccountCode, String> createCompareMap(String[] pAmounts) {
        HashMap<LedgerAccountCode, String> amountMap = new HashMap<LedgerAccountCode, String>();
        amountMap.put(LedgerAccountCode.TaxCurrentLiability, pAmounts[0]);
        amountMap.put(LedgerAccountCode.TaxCurrentCash, pAmounts[1]);
        amountMap.put(LedgerAccountCode.ERPayable, pAmounts[2]);
        amountMap.put(LedgerAccountCode.AgencyTaxRefund, pAmounts[3]);
        amountMap.put(LedgerAccountCode.TaxFutureLiability, pAmounts[4]);
        amountMap.put(LedgerAccountCode.TaxFutureReceivable, pAmounts[5]);
        return amountMap;
    }

    private HashMap<LedgerAccountCode, SpcfMoney> assertLedgerBalances(PayrollRun pPayrollRun, HashMap<LedgerAccountCode, String> pAmountsToCompare) {
        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = LedgerAccount.getLedgerAccountBalances(pPayrollRun.getCompany(), pPayrollRun, taxLedgerAccounts);

        for (LedgerAccountCode ledgerAccount : ledgerBalances.keySet()) {
            //  assertEquals(ledgerAccount.toString(), pAmountsToCompare.get(ledgerAccount), ledgerBalances.get(ledgerAccount).toString());
        }
        return ledgerBalances;
    }


    private HashMap<LedgerAccountCode, SpcfMoney> assertLedgerBalances(ArrayList<PayrollRun> pPayrollRuns, HashMap<LedgerAccountCode, String> pAmountsToCompare) {

        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = LedgerAccount.getLedgerAccountBalances(pPayrollRuns.get(0).getCompany(), pPayrollRuns, taxLedgerAccounts);

        for (LedgerAccountCode ledgerAccount : ledgerBalances.keySet()) {
            Assert.assertEquals(ledgerAccount.toString(), pAmountsToCompare.get(ledgerAccount), ledgerBalances.get(ledgerAccount).toString());
        }
        return ledgerBalances;
    }

    private void printLedgerBalances(HashMap<LedgerAccountCode, SpcfMoney> pLedgerBalances) {
        for (LedgerAccountCode ledgerAccount : pLedgerBalances.keySet()) {
            System.out.println(ledgerAccount.toString() + ": " + pLedgerBalances.get(ledgerAccount).toString());
        }
    }

    private static final ArrayList<LedgerAccountCode> taxLedgerAccounts = new ArrayList<LedgerAccountCode>();

    static {
        taxLedgerAccounts.add(LedgerAccountCode.TaxCurrentLiability);
        taxLedgerAccounts.add(LedgerAccountCode.TaxCurrentCash);
        taxLedgerAccounts.add(LedgerAccountCode.ERPayable);
        taxLedgerAccounts.add(LedgerAccountCode.AgencyTaxRefund);
        taxLedgerAccounts.add(LedgerAccountCode.TaxFutureLiability);
        taxLedgerAccounts.add(LedgerAccountCode.TaxFutureReceivable);
    }

    private void displayFinancialTransactions(Set<FinancialTransaction> transactions) {
        for (FinancialTransaction transaction : transactions) {
            logger.warn("type = " + transaction.getTransactionType().getTransactionTypeCd() +
                                " state = " + transaction.getCurrentTransactionState().getTransactionStateCd() +
                                " law = " + ((transaction.getLaw() != null) ? transaction.getLaw().getLawId() : "") +
                                " amount = " + transaction.getFinancialTransactionAmount() +
                                " date = " + transaction.getSettlementDate());
            if (transaction.getMoneyMovementTransaction() != null) {
                MoneyMovementTransaction mmt = transaction.getMoneyMovementTransaction();
                logger.warn("\tmethod = " + mmt.getMoneyMovementPaymentMethod() +
                                    " amount = " + mmt.getMoneyMovementTransactionAmount());
                for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
                    logger.warn("\tft = " + ft.getTransactionType().getTransactionTypeCd() + " amount = " + ft.getFinancialTransactionAmount());
                }
            }
        }
    }

    @Test
    public void testTaxPaymentImpoundReturn() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize(); // reset so fedTaxId will start at 1 (so generated file can match static file)

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101029000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // return the EmployerTaxDebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110210050000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName(null);
        transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();

            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }

        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        SpcfUniqueId batchId = transactionReturnBatch.getId();

        Application.commitUnitOfWork();

        // Process TransactionReturns associated with the TransactionReturnBatch
        ReturnFileParser returnsProcessor = new ReturnFileParser();
        returnsProcessor.processTransactionReturns(batchId);

        // Verify EmployerTaxDebit is returned and EmployerTaxRedebit/EmployerFeeDebit is created
        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Returned);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Returned EmployerTaxDebit count", 1, finTxs.size());

        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxRedebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Created EmployerTaxRedebit count", 1, finTxs.size());

        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerFeeDebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Created EmployerFeeDebit count", 1, finTxs.size());

        Application.commitUnitOfWork();

        // create the reject returns file
        File expected = new File(System.getProperty("user.dir"), "PSE/batch-jobs-tests/src/test/resources/offload/expected/test-psp-ach-returns.csv");
        Application.beginUnitOfWork();
        List<File> rejectFile = AchReturnAccountingFile.createFile(batchId);
        PayrollServices.commitUnitOfWork();

        // compare the new reject returns file with the expected result
        assertTrue("ACH returns accounting files do not match", compareFiles(rejectFile.get(0), expected));

        // create the reject returns file again to ensure file name is sequenced
        PayrollServices.beginUnitOfWork();
        rejectFile = AchReturnAccountingFile.createFile(batchId);
        PayrollServices.commitUnitOfWork();

        // compare the new reject returns file with the expected result
        assertTrue("ACH returns accounting files do not match", compareFiles(rejectFile.get(0), expected));
    }

    @Test
    public void testEntryDetailsForPayroll_Over100K() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"20000", "15000", "400", "27"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        PayrollRun payrollRun = processResult.getResult();
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);

        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("81"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("106200"), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("106200"), TransactionTypeCode.EmployerTaxDirectDebit, null, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("81"), TransactionTypeCode.EmployerTaxDebit, null, 1);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit));
        assertEquals("ACH Direct Deposit MMTs", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("ACH Direct Deposit MMT Amount", new SpcfMoney("81"), mmt.getMoneyMovementTransactionAmount());
        assertEquals("Entry detail records count for MMT", 2, mmt.getEntryDetailRecordCollection().size());
        for (EntryDetailRecord entryDetailRecord : mmt.getEntryDetailRecordCollection()) {
            assertEquals("Entry Detail Record amount", mmt.getMoneyMovementTransactionAmount(), entryDetailRecord.getAmount());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEntryDetailsForCompanyOnHoldAndPayroll_Over100K() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"20000", "15000", "400", "27"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        PayrollRun payrollRun = processResult.getResult();
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);

        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("81"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("106200"), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("106200"), TransactionTypeCode.EmployerTaxDirectDebit, null, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("81"), TransactionTypeCode.EmployerTaxDebit, null, 1);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit));
        assertEquals("ACH Direct Deposit MMTs", 1, mmts.size());
        MoneyMovementTransaction mmt = mmts.get(0);
        assertEquals("ACH Direct Deposit MMT Amount", new SpcfMoney("81"), mmt.getMoneyMovementTransactionAmount());
        assertEquals("Entry detail records count for MMT", 2, mmt.getEntryDetailRecordCollection().size());
        for (EntryDetailRecord entryDetailRecord : mmt.getEntryDetailRecordCollection()) {
            assertEquals("Entry Detail Record amount", mmt.getMoneyMovementTransactionAmount(), entryDetailRecord.getAmount());
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertTrue(company.isCompanyOnHold());
        mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPSDirectDebit));
        assertEquals("ACH Direct Deposit MMTs", 1, mmts.size());
        mmt = mmts.get(0);
        assertEquals(TaxPaymentStatus.ReadyToSend, mmt.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEntryDetailsWith2Payrolls_Over100K() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"5000", "10000", "4000", "12"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"5100", "8000", "4200", "27"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> movementTransactions = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.EFTPSDirectDebit);
        Assert.assertEquals("EFTPSDirectDebit EFTPS MMT ", 1, movementTransactions.size());
        Assert.assertEquals("EFTPSDirectDebit MMT Amount ", new SpcfMoney("51900.00"), movementTransactions.get(0).getMoneyMovementTransactionAmount());

        DomainEntitySet<MoneyMovementTransaction> eftps941Mmts = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirect().find();
        DomainEntitySet<MoneyMovementTransaction> eftps940Mmts = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("EFTPS MMTs for 941 Payments", 1, eftps941Mmts.size());
        assertEquals("EFTPS MMTs for 940 Payments", 1, eftps940Mmts.size());
        assertEquals("EFTPS MMT Amount for 941 Payments", new SpcfMoney("57000"), eftps941Mmts.get(0).getMoneyMovementTransactionAmount());
        assertEquals("EFTPS MMT Amount for 940 Payments", new SpcfMoney("117"), eftps940Mmts.get(0).getMoneyMovementTransactionAmount());

        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("81"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Created, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("57036"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed, 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit));
        assertEquals("ACH Direct Deposit MMTs", 2, mmts.size());
        for (MoneyMovementTransaction mmt : mmts) {
            assertEquals("Entry detail records count for MMT", 2, mmt.getEntryDetailRecordCollection().size());
            for (EntryDetailRecord entryDetailRecord : mmt.getEntryDetailRecordCollection()) {
                assertEquals("Entry Detail Record amount", mmt.getMoneyMovementTransactionAmount(), entryDetailRecord.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void test100KPayrollStatusComplete() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3,true, false);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"5000", "10000", "4000", "12"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"5100", "8000", "4200", "27"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        for (PaycheckDTO paycheckDTO:payrollDTO.getPaychecks()) {
            // Create Paycheck splits
            List<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            Employee employee = Employee.findEmployee(company,paycheckDTO.getEmployeeId());
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                if (employeeBankAccounts.get(i).getStatusCd().equals(BankAccountStatus.Active)) {
                    ddTransactions.add(DataLoadServices.createDDTransactionDTO(DataLoadServices.createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i + 1)));
                }
            }
            paycheckDTO.setDdTransactions(ddTransactions);

        }
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> movementTransactions = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.EFTPSDirectDebit);
        Assert.assertEquals("EFTPSDirectDebit EFTPS MMT ", 1, movementTransactions.size());
        Assert.assertEquals("EFTPSDirectDebit MMT Amount ", new SpcfMoney("51900.00"), movementTransactions.get(0).getMoneyMovementTransactionAmount());

        DomainEntitySet<MoneyMovementTransaction> eftps941Mmts = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirect().find();
        DomainEntitySet<MoneyMovementTransaction> eftps940Mmts = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("EFTPS MMTs for 941 Payments", 1, eftps941Mmts.size());
        assertEquals("EFTPS MMTs for 940 Payments", 1, eftps940Mmts.size());
        assertEquals("EFTPS MMT Amount for 941 Payments", new SpcfMoney("57000"), eftps941Mmts.get(0).getMoneyMovementTransactionAmount());
        assertEquals("EFTPS MMT Amount for 940 Payments", new SpcfMoney("117"), eftps940Mmts.get(0).getMoneyMovementTransactionAmount());

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessACHTransactions.main(new String[] {"20110116"});
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "Payroll_2") ;
        assertEquals("Payroll Run Status: ", "OffloadedAll", payrollRun.getPayrollRunStatus().toString());
        FinancialTransaction directDebit = payrollRun.getEmployerTaxDirectDebitTransaction();
        directDebit.updateFinancialTransactionState(TransactionStateCode.Executed);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions.main(new String[] {"20110120"});
        payrollRun = PayrollRun.findPayrollRun(company, "Payroll_2") ;
        assertEquals("Payroll Run Status: ", "Complete", payrollRun.getPayrollRunStatus().toString());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void test_PSRV002384_CancelledTaxCreditsAndDebits() {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.enrollEFTPS(company);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "62", "63", "64", "65", "66", "1"}, new String[]{"610", "620", "630", "640", "650", "660", "10"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.assertPayrollsEqual(payrollRunDTO, processResult.getResult());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        Application.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110112000000");
        Application.commitUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-15"), emps, new String[]{"61", "62", "63", "64", "65", "66", "1"}, new String[]{"610", "620", "630", "640", "650", "660", "10"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();

        //Recall one paycheck from second payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110113000000");
        Application.commitUnitOfWork();
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110114000000");
        Application.commitUnitOfWork();

        //Return the debit
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "NSF return");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> hold940Payments = DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> hold941Payments = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> ready940Payments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find();
        DomainEntitySet<MoneyMovementTransaction> ready941Payments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("Hold 940 MMTs", 1, hold940Payments.size());
        assertEquals("Hold 941 MMTs", 1, hold941Payments.size());
        assertEquals("ReadyToSend 940 MMTs", 1, ready940Payments.size());
        assertEquals("ReadyToSend 941 MMTs", 1, ready941Payments.size());
        assertEquals("Hold 940 MMT Amount", new SpcfMoney("2620"), hold940Payments.get(0).getMoneyMovementTransactionAmount());
        assertEquals("Hold 941 MMT Amount", new SpcfMoney("5020"), hold941Payments.get(0).getMoneyMovementTransactionAmount());
        assertEquals("ReadyToSend 940 MMT Amount", new SpcfMoney("3930"), ready940Payments.get(0).getMoneyMovementTransactionAmount());
        assertEquals("ReadyToSend 941 MMT Amount", new SpcfMoney("7530"), ready941Payments.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testZeroDollarAEICDoesNotCreateFT() {
        Integer psid = 1;
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid.toString(), false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid.toString(), "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "143", "1"}, new String[]{"5000", "5000", "0", "1"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid.toString(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        assertEquals(0, Application.find(FinancialTransaction.class, FinancialTransaction.Law().LawId().equalTo("143")).size());
        assertEquals(1, Application.find(FinancialTransaction.class, FinancialTransaction.Law().LawId().equalTo("1")).size());
    }

    @Test
    public void testRedebitNSFFileCreation() throws Throwable {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize(); // reset so fedTaxId will start at 1 (so generated file can match static file)

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});
        /*  Execute and verify payroll */

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        /* offload impounds */
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101029000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /*  Now return the EmployerTaxDebit */
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110210050000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        /*  Get the financial transaction that need to be returned  */
        DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

        /*  Create a return batch and add returns for each financial transaction to it  */
        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName(null);
        transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();

            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }

        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReturnBatch = Application.save(transactionReturnBatch);
        /*  The batchId will be needed later for assertions and creating NSF Report */
        SpcfUniqueId batchId = transactionReturnBatch.getId();

        Application.commitUnitOfWork();
        // Process TransactionReturns associated with the TransactionReturnBatch
        ReturnFileParser returnsProcessor = new ReturnFileParser();
        returnsProcessor.processTransactionReturns(batchId);

        // Verify EmployerTaxDebit is returned and EmployerTaxReDebit/ EmployerFeeDebit is created
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Returned);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Returned EmployerTaxDebit count", 1, finTxs.size());

        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxRedebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Created EmployerTaxRedebit count", 1, finTxs.size());


        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerFeeDebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Created EmployerFeeDebit count", 1, finTxs.size());
        Application.commitUnitOfWork();

        /*  Test the debit returns file (we should have debit returns only, at this time)  */
        PayrollServices.beginUnitOfWork();
        List<File> rejectFiles = AchReturnAccountingFile.createFile(batchId);
        PayrollServices.commitUnitOfWork();
        assertEquals("Incorrect number of return files", 1, rejectFiles.size());
        // compare the new reject returns file with the expected result
        File expected = new File(System.getProperty("user.dir"), "PSE/batch-jobs-tests/src/test/resources/offload/expected/test-psp-ach-returns.csv");
        assertTrue("ACH returns accounting files do not match", compareFiles(rejectFiles.get(0), expected));

        /*  Offload and return the redebit */
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110210050000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        TransactionReturnBatch transactionReDebitReturnBatch = new TransactionReturnBatch();
        transactionReDebitReturnBatch.setACHReturnFileName(null);
        transactionReDebitReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReDebitReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReDebitReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReDebitReturnBatch = Application.save(transactionReDebitReturnBatch);

        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxRedebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));

        finTxs = Application.find(FinancialTransaction.class, where);

        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();

            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReDebitReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }
        transactionReDebitReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReDebitReturnBatch = Application.save(transactionReDebitReturnBatch);

        SpcfUniqueId reDebitBatchId = transactionReDebitReturnBatch.getId();
        Application.commitUnitOfWork();

        // Process TransactionReturns associated with the TransactionReturnBatch
        returnsProcessor = new ReturnFileParser();
        returnsProcessor.processTransactionReturns(reDebitBatchId);

        /*  Now test the redebit returns file   */
        PayrollServices.beginUnitOfWork();
        rejectFiles = AchReturnAccountingFile.createFile(reDebitBatchId);
        PayrollServices.commitUnitOfWork();
        assertEquals("Incorrect number of redebit return files", 1, rejectFiles.size());
        expected = new File(System.getProperty("user.dir"), "PSE/batch-jobs-tests/src/test/resources/offload/expected/test-psp-ach-redebit-returns.csv");
        assertTrue("ACH redebit returns accounting files do not match", compareFiles(rejectFiles.get(0), expected));
    }

    @Test
    public void testMixedNSFReturnsFileCreation() {
        String psid = "123456789";
        String psid2 = "987654321";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize(); // reset so fedTaxId will start at 1 (so generated file can match static file)

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        /*  now for second company  */
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid2, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company2, null);
        DataLoadServices.addCompanyBankAccount(company2);

        DataLoadServices.addFederalTaxCompanyLaws(company2);
        DataLoadServices.updateCompanyService(company2, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps2 = DataLoadServices.addEEs(company2, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid2, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101029000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // return the EmployerTaxDebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110210050000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName(null);
        transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();

            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }

        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        SpcfUniqueId batchId = transactionReturnBatch.getId();
        Application.commitUnitOfWork();

        // Process TransactionReturns associated with the TransactionReturnBatch
        ReturnFileParser returnsProcessor = new ReturnFileParser();
        returnsProcessor.processTransactionReturns(batchId);

        // Verify EmployerTaxDebit is returned and EmployerTaxRedebit/EmployerFeeDebit is created
        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Returned);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Returned EmployerTaxDebit count", 1, finTxs.size());

        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxRedebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Created EmployerTaxRedebit count", 1, finTxs.size());

        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerFeeDebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        where = FinancialTransaction.Company().equalTo(company)
                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        finTxs = Application.find(FinancialTransaction.class, where);

        assertEquals("Created EmployerFeeDebit count", 1, finTxs.size());
        Application.commitUnitOfWork();

        /*  First test the debit returns file   */
        PayrollServices.beginUnitOfWork();
        List<File> rejectFiles = AchReturnAccountingFile.createFile(batchId);
        PayrollServices.commitUnitOfWork();
        assertEquals("Incorrect number of return files", 1, rejectFiles.size());
        // compare the new reject returns file with the expected result
        File expected = new File(System.getProperty("user.dir"), "PSE/batch-jobs-tests/src/test/resources/offload/expected/test-psp-ach-returns.csv");
        assertTrue("ACH returns accounting files do not match", compareFiles(rejectFiles.get(0), expected));

        /*  Offload and return the redebit */

        /*  Create a  payroll for the second company and offload it   */
        /*  Then return its TaxDebit in the same batch  */
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company2, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company2, new DateDTO("2011-02-09"), emps2, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"50", "120", "5.5", "45", "20", "25"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid2, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110210050000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110211050000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TransactionReturnBatch transactionReDebitReturnBatch = new TransactionReturnBatch();
        transactionReDebitReturnBatch.setACHReturnFileName(null);
        transactionReDebitReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReDebitReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReDebitReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReDebitReturnBatch = Application.save(transactionReDebitReturnBatch);

        txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxRedebit);
        txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
        TransactionType newPayrollTxnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);

        where = FinancialTransaction.TransactionType().equalTo(txnType)
                                    .Or(FinancialTransaction.TransactionType().equalTo(newPayrollTxnType))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));

        finTxs = Application.find(FinancialTransaction.class, where);

        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();

            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReDebitReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }
        transactionReDebitReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReDebitReturnBatch = Application.save(transactionReDebitReturnBatch);

        SpcfUniqueId reDebitBatchId = transactionReDebitReturnBatch.getId();
        Application.commitUnitOfWork();

        // Process TransactionReturns associated with the TransactionReturnBatch
        returnsProcessor = new ReturnFileParser();
        returnsProcessor.processTransactionReturns(reDebitBatchId);

        /*  Now test the redebit returns file   */
        PayrollServices.beginUnitOfWork();
        rejectFiles = AchReturnAccountingFile.createFile(reDebitBatchId);
        PayrollServices.commitUnitOfWork();
        assertEquals("Incorrect number of debit and redebit return files", 1, rejectFiles.size());

        // compare the new reject returns file with the expected result
        expected = new File(System.getProperty("user.dir"), "PSE/batch-jobs-tests/src/test/resources/offload/expected/test-psp-ach-return-with-redebits.csv");
        assertTrue("ACH returns accounting files do not match", compareFiles(rejectFiles.get(0), expected));

    }

    @Test
    public void testMultipleTemplateYears() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-UIETT-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "CAEDD");
        //companyAgency.setAgencyTaxpayerId("123456");
        Application.save(companyAgency);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 9, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2010-09-20"), "1");

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-02-20"), "3");
        DataLoadServices.runOffload();
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-03-20"), "7");
        DataLoadServices.runOffload();
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-04-20"), "13");
        DataLoadServices.runOffload();
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-05-20"), "17");
        DataLoadServices.runOffload();
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-07-20"), "23");
        DataLoadServices.runOffload();
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 19, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-09-20"), "29");
        DataLoadServices.runOffload();
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
//        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 15, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2012-01-20"), "31");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction pitPayment = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("CA-PITSDI-PAYMENT").setPending().find().getFirst();
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEftpsThresholdCalculationBeforeERTaxDbtOffloadTime_PSRV003082() throws Exception {
        testEftpsThresholdCalculationWithERTaxDbtOffloadTime_PSRV003082(false);
    }

    @Test
    public void testEftpsThresholdCalculationAfterERTaxDbtOffloadTime_PSRV003082() throws Exception {
        testEftpsThresholdCalculationWithERTaxDbtOffloadTime_PSRV003082(true);
    }

    public void testEftpsThresholdCalculationWithERTaxDbtOffloadTime_PSRV003082(boolean pAfterCutOff) throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        String[] states = {"AZ"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5000", "10000", "4000", "12","10"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        // offload impounds -- Offload cut off time is - 17:10:00
        PayrollServices.beginUnitOfWork();
        if(pAfterCutOff) {
            PSPDate.setPSPTime("20110105171200");
        } else {
            PSPDate.setPSPTime("20110105170800");
        }
        Application.commitUnitOfWork();

        //Second payroll to make over 100K
        payrollDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5100", "8000", "4200", "27", "12"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult2);
        PayrollRun payrollRun2 = processResult2.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun1);
        Application.refresh(payrollRun2);

        FinancialTransaction erTaxDebit = payrollRun1.getEmployerTaxDebitTransaction();
        assertEquals("ER Tax Debit transaction state", TransactionState.findTransactionState(TransactionStateCode.Created), erTaxDebit.getCurrentTransactionState());

        DomainEntitySet<MoneyMovementTransaction> eftpsTaxPayments;
        if(pAfterCutOff) {
            assertEquals("ER Tax Debit transaction Amount", new SpcfMoney("57066.00"), erTaxDebit.getFinancialTransactionAmount());
            assertNull("ER Tax Direct Debit ", payrollRun1.getEmployerTaxDirectDebitTransaction());

            eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set941().setNonDirectEFTPS().find();
            assertEquals("941 EFTPS Tax payments", 1, eftpsTaxPayments.size());
            assertEquals("941 EFTPS Tax payment amount", new SpcfMoney("57000.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());
            PaymentMethod[] paymentMethods = {PaymentMethod.EFTPSDirectDebit};
            eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set941().setPaymentMethods(paymentMethods).find();
            assertEquals("941 EFTPS Direct Tax payments", 1, eftpsTaxPayments.size());
            assertEquals("941 EFTPS Direct Tax payment amount", new SpcfMoney("51900.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());

        } else {
            assertEquals("ER Tax Debit transaction Amount", new SpcfMoney("66.00"), erTaxDebit.getFinancialTransactionAmount());
            assertNotNull("ER Tax Direct Direct Debit ", payrollRun1.getEmployerTaxDirectDebitTransaction());
            assertEquals("ER Tax Direct Debit transaction Amount", new SpcfMoney("57000.00"), payrollRun1.getEmployerTaxDirectDebitTransaction().getFinancialTransactionAmount());
            assertEquals("Cancelled ER Tax Debit", 1, payrollRun1.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerTaxDebit))
                                                                                                                               .And(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled)))
                                                                                                                               .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("57066.00")))).size());

            eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set941().setNonDirectEFTPS().find();
            assertEquals("941 EFTPS Tax payments", 0, eftpsTaxPayments.size());
            PaymentMethod[] paymentMethods = {PaymentMethod.EFTPSDirectDebit};
            eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set941().setPaymentMethods(paymentMethods).find();
            assertEquals("941 EFTPS Direct Tax payments", 2, eftpsTaxPayments.size());
            assertEquals("941 EFTPS Direct Tax payment amount for first payroll", 1, eftpsTaxPayments.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("57000.00"))).size());
            assertEquals("941 EFTPS Direct Tax payment amount for second payroll", 1, eftpsTaxPayments.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("51900.00"))).size());
        }

        FinancialTransaction erTaxDirectDbt = payrollRun2.getEmployerTaxDirectDebitTransaction();
        assertEquals("ER Tax Direct Debit transaction state", TransactionState.findTransactionState(TransactionStateCode.Created), erTaxDirectDbt.getCurrentTransactionState());
        assertEquals("ER Tax Direct Debit transaction Amount", new SpcfMoney("51900.00"), erTaxDirectDbt.getFinancialTransactionAmount());
        FinancialTransaction erTaxDebit2 = payrollRun2.getEmployerTaxDebitTransaction();
        assertEquals("ER Tax Debit", new SpcfMoney("117.00"), erTaxDebit2.getFinancialTransactionAmount());

        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set940().find();
        assertEquals("940 EFTPS Tax payments", 1, eftpsTaxPayments.size());
        assertEquals("940 EFTPS Tax payment amount", new SpcfMoney("117.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());

        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AZ-A1-PAYMENT").find();
        assertEquals("AZ-A1-PAYMENT Tax payments", 1, eftpsTaxPayments.size());
        assertEquals("AZ-A1-PAYMENT Tax payment amount", new SpcfMoney("66.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());

        PayrollServices.rollbackUnitOfWork();

    }

    // PSRV004061: 2nd Offload - PSP does not create Direct Deposits for 100K payrolls during 2nd offload window
    @Test
    public void testEftpsThresholdCalculation_BeforeSecondOffload() throws Throwable {

        DataLoadServices.setPSPDate(2013, 1, 5);

        String psid = "123456789";

        String[] states = {"AZ"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-06"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5000", "10000", "4000", "12","10"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        //Creating second offload on 01/07/2013
        DataLoadServices.setPSPDate(2013, 1, 7);
        DataLoadServices.createSecondStandardOffload();

        //Run first offload
        DataLoadServices.runOffload();

        //Set PSP time as after first offload
        // offload impounds -- Offload cut off time is - 17:10:00
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20130107171200");
        PayrollServices.commitUnitOfWork();

        //Second payroll to make over 100K
        payrollDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2013-01-08"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5100", "8000", "4200", "27", "12"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult2);
        PayrollRun payrollRun2 = processResult2.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun1);
        Application.refresh(payrollRun2);

        FinancialTransaction erTaxDebit = payrollRun1.getEmployerTaxDebitTransaction();
        assertEquals("ER Tax Debit transaction state", TransactionState.findTransactionState(TransactionStateCode.Executed), erTaxDebit.getCurrentTransactionState());

        DomainEntitySet<MoneyMovementTransaction> eftpsTaxPayments;
        assertEquals("ER Tax Debit transaction Amount", new SpcfMoney("57066.00"), erTaxDebit.getFinancialTransactionAmount());
        assertNull("ER Tax Direct Debit ", payrollRun1.getEmployerTaxDirectDebitTransaction());

        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set941().setNonDirectEFTPS().find();
        assertEquals("941 EFTPS Tax payments", 1, eftpsTaxPayments.size());
        assertEquals("941 EFTPS Tax payment amount", new SpcfMoney("57000.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());
        PaymentMethod[] paymentMethods = {PaymentMethod.EFTPSDirectDebit};
        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set941().setPaymentMethods(paymentMethods).find();
        assertEquals("941 EFTPS Direct Tax payments", 1, eftpsTaxPayments.size());
        assertEquals("941 EFTPS Direct Tax payment amount", new SpcfMoney("51900.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());

        FinancialTransaction erTaxDirectDbt = payrollRun2.getEmployerTaxDirectDebitTransaction();
        assertEquals("ER Tax Direct Debit transaction state", TransactionState.findTransactionState(TransactionStateCode.Created), erTaxDirectDbt.getCurrentTransactionState());
        assertEquals("ER Tax Direct Debit transaction Amount", new SpcfMoney("51900.00"), erTaxDirectDbt.getFinancialTransactionAmount());
        FinancialTransaction erTaxDebit2 = payrollRun2.getEmployerTaxDebitTransaction();
        assertEquals("ER Tax Debit", new SpcfMoney("117.00"), erTaxDebit2.getFinancialTransactionAmount());

        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set940().find();
        assertEquals("940 EFTPS Tax payments", 1, eftpsTaxPayments.size());
        assertEquals("940 EFTPS Tax payment amount", new SpcfMoney("117.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());

        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AZ-A1-PAYMENT").find();
        assertEquals("AZ-A1-PAYMENT Tax payments", 1, eftpsTaxPayments.size());
        assertEquals("AZ-A1-PAYMENT Tax payment amount", new SpcfMoney("66.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());

        PayrollServices.rollbackUnitOfWork();

    }

    // PSRV004061: 2nd Offload - PSP does not create Direct Deposits for 100K payrolls during 2nd offload window
    @Test
    public void testEftpsThresholdCalculation_BeforeSecondOffload_PayrollsSubmittedAfterFirstOffload() throws Throwable {

        DataLoadServices.setPSPDate(2013, 1, 5);

        String psid = "123456789";

        String[] states = {"AZ"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        //Creating second offload on 01/07/2013
        DataLoadServices.setPSPDate(2013, 1, 7);
        DataLoadServices.createSecondStandardOffload();

        //Run first offload
        DataLoadServices.runOffload();

        //Set PSP time as after first offload
        // offload impounds -- Offload cut off time is - 17:10:00
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20130107171200");
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-06"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5000", "10000", "4000", "12","10"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        //Second payroll to make over 100K
        payrollDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2013-01-08"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5100", "8000", "4200", "27", "12"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult2);
        PayrollRun payrollRun2 = processResult2.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun1);
        Application.refresh(payrollRun2);

        DomainEntitySet<MoneyMovementTransaction> eftpsTaxPayments;

        FinancialTransaction erTaxDirectDbt = payrollRun1.getEmployerTaxDirectDebitTransaction();
        assertEquals("ER Tax Direct Debit transaction state", TransactionState.findTransactionState(TransactionStateCode.Created), erTaxDirectDbt.getCurrentTransactionState());
        assertEquals("ER Tax Direct Debit transaction Amount", new SpcfMoney("57000.00"), erTaxDirectDbt.getFinancialTransactionAmount());
        FinancialTransaction erTaxDebit = payrollRun1.getEmployerTaxDebitTransaction();
        assertEquals("ER Tax Debit", new SpcfMoney("66.00"), erTaxDebit.getFinancialTransactionAmount());

        PaymentMethod[] paymentMethods = {PaymentMethod.EFTPSDirectDebit};
        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set941().setPaymentMethods(paymentMethods).find();
        assertEquals("941 EFTPS Direct Tax payments", 2, eftpsTaxPayments.size());

        assertEquals("First payroll 941 EFTPS Direct Tax payment amount", 1, eftpsTaxPayments.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("57000.00"))).size());
        assertEquals("Second payroll 941 EFTPS Direct Tax payment amount", 1, eftpsTaxPayments.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("51900.00"))).size());


        erTaxDirectDbt = payrollRun2.getEmployerTaxDirectDebitTransaction();
        assertEquals("ER Tax Direct Debit transaction state", TransactionState.findTransactionState(TransactionStateCode.Created), erTaxDirectDbt.getCurrentTransactionState());
        assertEquals("ER Tax Direct Debit transaction Amount", new SpcfMoney("51900.00"), erTaxDirectDbt.getFinancialTransactionAmount());
        erTaxDebit = payrollRun2.getEmployerTaxDebitTransaction();
        assertEquals("ER Tax Debit", new SpcfMoney("117.00"), erTaxDebit.getFinancialTransactionAmount());

        eftpsTaxPayments = MoneyMovementTransaction.findTaxPayments().set940().find();
        assertEquals("940 EFTPS Tax payments", 1, eftpsTaxPayments.size());
        assertEquals("940 EFTPS Tax payment amount", new SpcfMoney("117.00"), eftpsTaxPayments.getFirst().getMoneyMovementTransactionAmount());

        DomainEntitySet<MoneyMovementTransaction> statePayments = MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AZ-A1-PAYMENT").find();
        assertEquals("AZ-A1-PAYMENT Tax payments", 2, statePayments.size());
        MoneyMovementTransaction statePayment = statePayments.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("30"))).getFirst();
        assertNotNull("First Payroll state payment", statePayment);
        assertEquals("First Payroll due date", SpcfCalendar.createInstance(2013, 1, 7, SpcfTimeZone.getLocalTimeZone()), statePayment.getDueDate().toLocal());
        assertEquals("First Payroll Init date", SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()), statePayment.getInitiationDate().toLocal());

        statePayment = statePayments.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("36"))).getFirst();
        assertNotNull("Second Payroll state payment", statePayment);
        assertEquals("Second Payroll due date", SpcfCalendar.createInstance(2013, 1, 9, SpcfTimeZone.getLocalTimeZone()), statePayment.getDueDate().toLocal());
        assertEquals("Second Payroll Init date", SpcfCalendar.createInstance(2013, 1, 8, SpcfTimeZone.getLocalTimeZone()), statePayment.getInitiationDate().toLocal());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_NY() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        testStateThresholdPayrolls("NY", DepositFrequencyCode.THREEBANKINGDAY, DepositFrequencyCode.QUARTERLY, lawAmounts, true);
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_GA() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("12", "25001");
        testStateThresholdPayrolls("GA", DepositFrequencyCode.NEXTBANKINGDAY, DepositFrequencyCode.SEMIWEEKLY, lawAmounts, true);
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_AZ() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("5", "25001");
        // testFollowsFederalThresholdPayrolls("AZ", DepositFrequencyCode.SEMIWEEKLY, DepositFrequencyCode.SEMIWEEKLY, lawAmounts, true);
    }

    @Test
    public void testStateThresholdWithUnOrderedPayrolls_NY() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("36", "50");
        lawAmounts.put("54", "50");
        lawAmounts.put("56", "50");
        lawAmounts.put("57", "50");
        testStateThresholdPayrolls("NY", DepositFrequencyCode.THREEBANKINGDAY, DepositFrequencyCode.QUARTERLY, lawAmounts, false);
    }

    @Test
    public void testStateThresholdWithUnOrderedPayrolls_GA() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("12", "25001");
        testStateThresholdPayrolls("GA", DepositFrequencyCode.NEXTBANKINGDAY, DepositFrequencyCode.SEMIWEEKLY, lawAmounts, false);
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_NY_VoidPayroll() throws Exception {
        testStateThresholdWithOrderedPayrolls_NY();

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 8, 10, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        Company company = companies.get(0);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate("NY", PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, new SpcfMoney("400"), initDate, dueDate, startDate, endDate, 1);
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not updated back to Quarterly", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY));
        PayrollServices.rollbackUnitOfWork();


        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2011, 8, 11));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 10, 28, 17, 30, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        // this should not cancel the ach credit transaction
        BatchJobManager.executeCommand(new String[]{"runstep",
                BatchJobType.PrimaryDailyBatchJobs.toString(),
                "MissedTransactionProcessor"});

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, new SpcfMoney("400"), initDate, dueDate, startDate, endDate, 1);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_GA_VoidPayroll() throws Exception {
        testStateThresholdWithOrderedPayrolls_GA_VoidPayroll(false, false);
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_GA_VoidPayroll_partial() throws Exception {
        testStateThresholdWithOrderedPayrolls_GA_VoidPayroll(true, false);
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_GA_VoidPayroll_offload() throws Exception {
        testStateThresholdWithOrderedPayrolls_GA_VoidPayroll(false, true);
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_GA_VoidPayroll_partial_offload() throws Exception {
        testStateThresholdWithOrderedPayrolls_GA_VoidPayroll(true, true);
    }

    public void testStateThresholdWithOrderedPayrolls_GA_VoidPayroll(boolean voidPartial, boolean offloadStatePayments) throws Exception {
        testStateThresholdWithOrderedPayrolls_GA();

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 8, 10, SpcfTimeZone.getLocalTimeZone()));
        if (offloadStatePayments) {
            SpcfCalendar taxInitiationDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
            CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
            DataLoadServices.runOffloadTaxPayments(taxInitiationDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        Company company = companies.get(0);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        SpcfMoney statePayment = new SpcfMoney("50002");
        if (voidPartial) {
            Application.refresh(payrollRun);
            List<String> paycheckList = new ArrayList<String>();
            paycheckList.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
            voidPayrollDTO.setPaycheckIdList(paycheckList);
            statePayment = new SpcfMoney("75003");
        }
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 8, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 9, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 9, 15, SpcfTimeZone.getLocalTimeZone());

        if (offloadStatePayments) {
            // Due and init date will not change.
            dueDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
            initDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
            statePayment = new SpcfMoney("100004");
        }

        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);
        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate("GA", PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePayment, initDate, dueDate, startDate, endDate, 1);
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));

        assertTrue("State template deposit frequency is not updated back to MONTHLY", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY));

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_NY_RecallPayroll() throws Exception {
        testStateThresholdWithOrderedPayrolls_NY();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        Company company = companies.get(0);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        PayrollServices.commitUnitOfWork();

        //Recall one paycheck from second payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, new SpcfMoney("400"), initDate, dueDate, startDate, endDate, 1);
        PayrollServices.rollbackUnitOfWork();
    }

    public void testStateThreshold941Payments_Over100K_State_Liabilities(String pState, HashMap<String, String> lawAmounts) {
        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(pState, PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        SpcfMoney statePaymentAmount = SpcfMoney.ZERO;

        for (String law : lawAmounts.keySet()) {
            statePaymentAmount = (SpcfMoney) statePaymentAmount.add(new SpcfMoney(lawAmounts.get(law)));
        }

        statePaymentAmount = (SpcfMoney) statePaymentAmount.multiply(new SpcfMoney("2"));

        lawAmounts.put("61", "12200");
        lawAmounts.put("62", "12400");
        lawAmounts.put("63", "12600");
        lawAmounts.put("64", "12800");
        lawAmounts.put("1", "2500");
        lawAmounts.put("65", "6500");

        Company company = setupCompanyAndRunPayrollForThreshold(lawAmounts, pState);

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar statePaymentStartDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentEndDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);

        SpcfMoney irsPaymentAmount = new SpcfMoney("105000");

        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, statePaymentInitDate, dueDate, statePaymentStartDate, statePaymentEndDate, 1);

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, irsPaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PaymentTemplate statePaymentTemplate = DataLoadServices.getStatePaymentTemplate(pState, PaymentTemplateCategory.Withholding);
        PaymentTemplate irs941PaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, statePaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("IRS 941 template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAZStateThreshold941Payments_Over100K_State_Liabilities() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("5", "50");
        testStateThreshold941Payments_Over100K_State_Liabilities("AZ", lawAmounts);
    }

    @Test
    public void testMNStateThreshold941Payments_Over100K_State_Liabilities() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("25", "25");
        testStateThreshold941Payments_Over100K_State_Liabilities("MN", lawAmounts);
    }

    @Test
    public void testCAStateThreshold941Payments_Over100K_State_Liabilities() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("6", "12");
        lawAmounts.put("67", "14");
        testStateThreshold941Payments_Over100K_State_Liabilities("CA", lawAmounts);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT")).find();
        assertEquals("Number of CA state payments ", 1, moneyMovementTransactions.size());
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.get(0);
        assertTrue("Expected NEXTBANKINGDAY Dep fre on MMT", moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.NEXTBANKINGDAY));
        assertEquals("Txp record for state payment", "TXP*12245678*01102*110930*T*0000002800*T*0000002400*T*0000005200*000000*\\", moneyMovementTransaction.getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).get(0).getTxpRecordData());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAZStateThreshold941Payments_Over100K_State_Liabilities_Void_Partial() throws Exception {
        testAZStateThreshold_Void_Recall(true, true);
    }

    @Test
    public void testAZStateThreshold941Payments_Over100K_State_Liabilities_Void() throws Exception {
        testAZStateThreshold_Void_Recall(false, true);
    }

    @Test
    @Ignore
    public void testAZStateThreshold941Payments_Over100K_State_Liabilities_Recall_Partial() throws Exception {
        testAZStateThreshold_Void_Recall(true, false);
    }

    @Test
    public void testAZStateThreshold941Payments_Over100K_State_Liabilities_Recall() throws Exception {
        testAZStateThreshold_Void_Recall(false, false);
    }

    @Test
    public void testAZStateThreshold941Payments_Over100K_Adjustment_Void() throws Exception {
        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate("AZ", PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("5", "50");
        lawAmounts.put("61", "12200");
        lawAmounts.put("62", "12400");
        lawAmounts.put("63", "12600");
        lawAmounts.put("64", "12800");
        lawAmounts.put("1", "2500");
        lawAmounts.put("65", "6500");

        Company company = setupCompanyAndRunPayrollForThreshold(lawAmounts, "AZ");
        String psid = company.getSourceCompanyId();
        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();


        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar statePaymentStartDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentEndDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);

        SpcfMoney statePaymentAmount = new SpcfMoney("100");
        SpcfMoney irsPaymentAmount = new SpcfMoney("105000");

        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, statePaymentInitDate, dueDate, statePaymentStartDate, statePaymentEndDate, 1);

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, irsPaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PaymentTemplate statePaymentTemplate = DataLoadServices.getStatePaymentTemplate("AZ", PaymentTemplateCategory.Withholding);
        PaymentTemplate irs941PaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, statePaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("IRS 941 template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO("2011-08-14"));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO("5", "5", null, new DateDTO("2011-8-14"), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, sourcePayrollId, companyAdjustmentSubmissionDTO, new DateDTO("2011-08-14"), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        String companyAdjustmentSubmissionId = processResult.getResult().getSourceId();
        PayrollServices.commitUnitOfWork();

        // Now void the adjustment
        PayrollServices.beginUnitOfWork();
        Collection<String> casIds = new ArrayList<String>();
        casIds.add(companyAdjustmentSubmissionId);
        assertSuccess(PayrollServices.payrollManager
                                     .voidLiabilityAdjustments(SourceSystemCode.QBDT, psid, casIds, true));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        DomainEntitySet<FinancialTransaction> employerTransactions =
                payrollRun.getFinancialTransactionCollection()
                          .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDirectDebit)
                                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        assertTrue("Employer Direct Debit is not Cancelled", employerTransactions.size() == 1);
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void test_CA_AZ_StateThreshold941Payments_Over100K() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        PaymentTemplate paymentTemplateAZ = DataLoadServices.getStatePaymentTemplate("AZ", PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateAZ.getPaymentTemplateCd(), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        PaymentTemplate paymentTemplateCA = DataLoadServices.getStatePaymentTemplate("CA", PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateCA.getPaymentTemplateCd(), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        HashMap<String, String> lawAmountsAz = new HashMap<String, String>();
        lawAmountsAz.put("5", "50");
        lawAmountsAz.put("61", "12200");
        lawAmountsAz.put("65", "100");

        HashMap<String, String> lawAmountsCa = new HashMap<String, String>();
        lawAmountsCa.put("6", "60");
        lawAmountsCa.put("62", "12400");
        lawAmountsCa.put("65", "100");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "12200");
        lawAmounts.put("62", "12400");
        lawAmounts.put("63", "25200");
        lawAmounts.put("64", "13600");
        lawAmounts.put("1", "12500");
        lawAmounts.put("65", "12600");

        String[] statesList = new String[]{"AZ", "CA"};
        List<Company> companies = DataLoadServices.setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);
        Company company = companies.get(0);

        DataLoadServices.setPSPDate(2011, 11, 9);
        List<Employee> employeesCa = new ArrayList<Employee>();
        List<Employee> employeesAz = new ArrayList<Employee>();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals("Number of employees", 2, employees.size());

        employeesCa.add(employees.get(0));
        employeesAz.add(employees.get(1));


        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-11-14"), employeesCa, lawAmountsCa);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar quarterStartDate = SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar quarterEndDate = SpcfCalendar.createInstance(2011, 12, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);
        SpcfCalendar quarterDueDate = SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar irsInitDate = SpcfCalendar.createInstance(2012, 1, 30, SpcfTimeZone.getLocalTimeZone());

        SpcfMoney statePaymentAmountAz = new SpcfMoney("50");
        SpcfMoney statePaymentAmountCa = new SpcfMoney("60");
        SpcfMoney irsPaymentAmount = new SpcfMoney("12400");

        //Assert state ACH payment- CA
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmountCa, statePaymentInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);
        //Assert EFTPS payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, irsPaymentAmount, irsInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-11-15"), employeesAz, lawAmountsAz);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        irsPaymentAmount = new SpcfMoney("24600");

        //Assert state ACH payment- AZ
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmountAz, statePaymentInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);
        //Assert state ACH payment- CA        
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmountCa, statePaymentInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, irsPaymentAmount, irsInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-11-15"), employeesCa, lawAmounts);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollRun payrollRun = processResult.getResult();
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar firstPaycheckDate = SpcfCalendar.createInstance(2011, 11, 14, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar secondPaycheckDate = SpcfCalendar.createInstance(2011, 11, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar nextDate = SpcfCalendar.createInstance(2011, 11, 16, SpcfTimeZone.getLocalTimeZone());
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 11, 16, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);

        //Assert state ACH payment- AZ
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmountAz, statePaymentInitiationDate, nextDate, quarterStartDate, quarterEndDate, 1);
        //Assert state ACH payment- CA
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmountCa, statePaymentInitiationDate, nextDate, quarterStartDate, quarterEndDate, 1);

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("12400"), firstPaycheckDate, secondPaycheckDate, firstPaycheckDate, firstPaycheckDate, 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("12200"), secondPaycheckDate, nextDate, secondPaycheckDate, secondPaycheckDate, 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("75900"), secondPaycheckDate, nextDate, secondPaycheckDate, secondPaycheckDate, 1);

        PayrollServices.beginUnitOfWork();
        //Recall one paycheck from second payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        Application.refresh(payrollRun);
        List<String> paycheckList = new ArrayList<String>();
        paycheckList.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), transactionCancelDTO);
        assertSuccess(recallProcessResult);
        PayrollServices.commitUnitOfWork();

        //Assert state ACH payment- AZ
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmountAz, statePaymentInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);
        //Assert state ACH payment- CA
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmountCa, statePaymentInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("24600"), irsInitDate, quarterDueDate, quarterStartDate, quarterEndDate, 1);

    }

    @Test
    public void testAZStateThreshold941Payments_Over100K_State() throws Exception {
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2010, 1, 1));
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "12200");
        lawAmounts.put("62", "12400");
        lawAmounts.put("63", "12600");
        lawAmounts.put("64", "12800");
        lawAmounts.put("1", "2500");
        lawAmounts.put("65", "6500");

        Company company = setupCompanyAndRunPayrollForThreshold(lawAmounts, "AZ");

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());

        SpcfMoney irsPaymentAmount = new SpcfMoney("105000");

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, irsPaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PaymentTemplate statePaymentTemplate = DataLoadServices.getStatePaymentTemplate("AZ", PaymentTemplateCategory.Withholding);
        PaymentTemplate irs941PaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, statePaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("IRS 941 template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        PayrollServices.rollbackUnitOfWork();

        DateDTO payrollRunDate = new DateDTO("2011-08-12");

        List<Employee> employees = new ArrayList<Employee>(Employee.findEmployees(company));

        lawAmounts = new HashMap<String, String>();
        lawAmounts.put("5", "50");
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, payrollRunDate, employees, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        achTaxOffloadOffset = SystemParameter.findIntValue(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar statePaymentStartDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentEndDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);
        SpcfMoney statePaymentAmount = new SpcfMoney("100");
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, statePaymentInitDate, dueDate, statePaymentStartDate, statePaymentEndDate, 1);
        
        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, irsPaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, statePaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("IRS 941 template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testAZStateThreshold941Payments_Over100K_State2() throws Exception {
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2010, 1, 1));

        String[] laws = {"61", "62", "63", "64", "1", "65", "6"};
        String[] lawAmounts = {"100", "100", "100", "100", "100", "100", "100"};

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        String[] statesList = {"CA", "AZ"};

        List<Company> companies = DataLoadServices.setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 10));
        Company company = companies.get(0);

        DataLoadServices.claimNoFeesOffer(company);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertTrue("State template deposit frequency is not default deposit freq", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString().equals(paymentTemplate.getDefaultDepositFrequency()));
        PayrollServices.rollbackUnitOfWork();

        paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[1], PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertTrue("State template deposit frequency is not default deposit freq", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString().equals(paymentTemplate.getDefaultDepositFrequency()));
        PayrollServices.rollbackUnitOfWork();

        DateDTO payrollRunDate = new DateDTO("2011-08-12");

        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, payrollRunDate, employees, laws, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone());

        SpcfMoney irsPaymentAmount = new SpcfMoney("1000");

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, irsPaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PaymentTemplate statePaymentTemplate = DataLoadServices.getStatePaymentTemplate("AZ", PaymentTemplateCategory.Withholding);
        PaymentTemplate irs941PaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, statePaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("IRS 941 template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY));
        PayrollServices.rollbackUnitOfWork();

        payrollRunDate = new DateDTO("2011-08-12");

        employees = new ArrayList<Employee>(Employee.findEmployees(company));

        laws = new String[]{"61", "62", "63", "64", "1", "65", "5", "1"};
        lawAmounts = new String[]{"12200", "12400", "12600", "12800", "2500", "6500", "12200", "12400"};

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, payrollRunDate, employees, laws, lawAmounts);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        SpcfCalendar statePaymentStartDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentEndDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar statePaymentDueDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);

        SpcfMoney statePaymentAmount = new SpcfMoney("24400");
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, statePaymentInitDate, statePaymentDueDate, statePaymentStartDate, statePaymentEndDate, 1);

        startDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        endDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        dueDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        initDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());

        irsPaymentAmount = new SpcfMoney("129800");

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, irsPaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, statePaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("IRS 941 template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAZStateThresholdOver100K_State_Liabilities() throws Exception {
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2010, 1, 1));
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("5", "50001");
        lawAmounts.put("61", "122");
        lawAmounts.put("62", "124");
        lawAmounts.put("63", "126");
        lawAmounts.put("64", "128");
        lawAmounts.put("1", "250");
        lawAmounts.put("65", "65");

        Company company = setupCompanyAndRunPayrollForThreshold(lawAmounts, "AZ");

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);

        SpcfMoney statePaymentAmount = new SpcfMoney("100002");
        SpcfMoney irsPaymentAmount = new SpcfMoney("1500");

        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, statePaymentInitDate, dueDate, startDate, endDate, 1);

        //Assert EFTPSDirectDbt payment
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, irsPaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PaymentTemplate statePaymentTemplate = DataLoadServices.getStatePaymentTemplate("AZ", PaymentTemplateCategory.Withholding);
        PaymentTemplate irs941PaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, statePaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not default", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIWEEKLY));
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("IRS 941 template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testStateThresholdWithOrderedPayrolls_GA_RecallPayroll() throws Exception {
        testStateThresholdWithOrderedPayrolls_GA_RecallPayroll(false);
    }

    @Test
    @Ignore
    public void testStateThresholdWithOrderedPayrolls_GA_Recall_Partial_Payroll() throws Exception {
        testStateThresholdWithOrderedPayrolls_GA_RecallPayroll(true);
    }

    public void testAZStateThreshold_Void_Recall(boolean isPartial, boolean isVoid) throws Exception {
        testAZStateThreshold941Payments_Over100K_State_Liabilities();

        if (isVoid) {
            DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 8, 10, SpcfTimeZone.getLocalTimeZone()));
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        }

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("12345678", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        SpcfMoney statePaymentAmount = new SpcfMoney("0");
        SpcfMoney irsPaymentAmount = new SpcfMoney("0");
        if (isVoid) {
            VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
            voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            if (isPartial) {
                List<String> paycheckList = new ArrayList<String>();
                paycheckList.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
                voidPayrollDTO.setPaycheckIdList(paycheckList);
                statePaymentAmount = new SpcfMoney("50");
                irsPaymentAmount = new SpcfMoney("52500");
            }
            ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
            assertSuccess(voidProcessResult);
        } else {
            //Recall one paycheck from second payroll run
            TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
            transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            if (isPartial) {
                Application.refresh(payrollRun);
                List<String> paycheckList = new ArrayList<String>();
                paycheckList.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
                transactionCancelDTO.setSourcePaycheckIdList(paycheckList);
                statePaymentAmount = new SpcfMoney("50");
                irsPaymentAmount = new SpcfMoney("52500");
            }
            ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), transactionCancelDTO);
            assertSuccess(recallProcessResult);
        }
        PayrollServices.commitUnitOfWork();


        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar statePaymentInitDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitDate, -achTaxOffloadOffset);
        PaymentTemplate irs941PaymentTemplate = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        if (isVoid || isPartial) {
            //Assert state ACH payment
            DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, statePaymentInitDate, dueDate, startDate, endDate, 1);

            //Assert EFTPSDirectDbt payment
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().set941().setInitiationDate(initDate).setPeriodBeginDate(startDate).setPeriodEndDate(endDate).find();
            assertEquals("IRS 941 Payments", 1, moneyMovementTransactions.size());
            assertEquals("IRS 941 payment due date", dueDate, moneyMovementTransactions.get(0).getDueDate().toLocal());
            assertEquals("IRS 941 payment Amount", irsPaymentAmount, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        } else {
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class);
            for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
                assertEquals("mmt not zero", SpcfMoney.ZERO, moneyMovementTransaction.getMoneyMovementTransactionAmount());
            }
        }

        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, irs941PaymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
//        assertTrue("IRS 941 template deposit frequency is not switched back to Quarterly", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY));
        PayrollServices.rollbackUnitOfWork();
    }

    public Company setupCompanyAndRunPayrollForThreshold(HashMap<String, String> lawAmounts, String state) {
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        String[] statesList = new String[]{state};

        List<Company> companies = DataLoadServices.setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 10));
        Company company = companies.get(0);

        DataLoadServices.claimNoFeesOffer(company);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertTrue("State template deposit frequency is not default deposit freq", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString().equals(paymentTemplate.getDefaultDepositFrequency()));
        PayrollServices.rollbackUnitOfWork();

        DateDTO payrollRunDate = new DateDTO("2011-08-12");

        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, payrollRunDate, employees, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        return company;

    }

    public Company setupCompanyAndRunPayrollForThreshold(HashMap<String, String> lawAmounts, String... states) {
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        String[] statesList = states;

        List<Company> companies = DataLoadServices.setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 10));
        Company company = companies.get(0);

        DataLoadServices.claimNoFeesOffer(company);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertTrue("State template deposit frequency is not default deposit freq", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString().equals(paymentTemplate.getDefaultDepositFrequency()));
        PayrollServices.rollbackUnitOfWork();

        DateDTO payrollRunDate = new DateDTO("2011-08-12");

        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, payrollRunDate, employees, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        return company;

    }

    public void testStateThresholdWithOrderedPayrolls_GA_RecallPayroll(boolean recallPartial) throws Exception {
        testStateThresholdWithOrderedPayrolls_GA();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        Company company = companies.get(0);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        //Recall one paycheck from second payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

        SpcfMoney statePayment = new SpcfMoney("50002");
        if (recallPartial) {
            Application.refresh(payrollRun);
            List<String> paycheckList = new ArrayList<String>();
            paycheckList.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
            transactionCancelDTO.setSourcePaycheckIdList(paycheckList);
            statePayment = new SpcfMoney("75003");
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        SpcfCalendar startDate = SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar endDate = SpcfCalendar.createInstance(2011, 8, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar dueDate = SpcfCalendar.createInstance(2011, 9, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 9, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate("GA", PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePayment, initDate, dueDate, startDate, endDate, 1);
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not updated back to MONTHLY", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY));
        PayrollServices.rollbackUnitOfWork();
    }

    public void testStateThresholdPayrolls(String state, DepositFrequencyCode pRollOverDepFreqCd, DepositFrequencyCode pNewDepFreqCd, HashMap<String, String> lawAmounts, boolean pInAscending) throws Exception {

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        SpcfMoney statePaymentAmount = SpcfMoney.ZERO;
        for (String lawId : lawAmounts.keySet()) {
            statePaymentAmount = (SpcfMoney) statePaymentAmount.add(new SpcfMoney(lawAmounts.get(lawId)));
        }
        statePaymentAmount = (SpcfMoney) statePaymentAmount.multiply(new SpcfMoney("2"));

        String[] statesList = new String[]{state};
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");

        List<Company> companies = DataLoadServices.setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 10));
        Company company = companies.get(0);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertTrue("State template deposit frequency is not default deposit freq", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString().equals(paymentTemplate.getDefaultDepositFrequency()));
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar startDate = null;
        SpcfCalendar endDate = null;
        SpcfCalendar dueDate = null;
        SpcfCalendar initDate = null;
        if (effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
            startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
            endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
            dueDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
            initDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
            
        } else if (effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
            startDate = SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone());
            endDate = SpcfCalendar.createInstance(2011, 8, 31, SpcfTimeZone.getLocalTimeZone());
            dueDate = SpcfCalendar.createInstance(2011, 9, 15, SpcfTimeZone.getLocalTimeZone());
            initDate = SpcfCalendar.createInstance(2011, 9, 15, SpcfTimeZone.getLocalTimeZone());
        }
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);

        DateDTO payrollRunDate1;
        DateDTO payrollRunDate2;
        if (pInAscending) {
            payrollRunDate1 = new DateDTO("2011-08-12");
            payrollRunDate2 = new DateDTO("2011-08-14");
        } else {
            payrollRunDate1 = new DateDTO("2011-08-14");
            payrollRunDate2 = new DateDTO("2011-08-12");
        }
        DataLoadServices.runPayrollRun(company, statesList, beginDate, payrollRunDate1, true, lawAmounts, PaymentTemplateCategory.Withholding);
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, initDate, dueDate, startDate, endDate, 1);

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollRunDate2);

        statePaymentAmount = (SpcfMoney) statePaymentAmount.multiply(new SpcfMoney("2"));

        dueDate = SpcfCalendar.createInstance(2011, 8, 14, SpcfTimeZone.getLocalTimeZone());
        dueDate = updateDueDate(dueDate, pRollOverDepFreqCd);
        initDate = dueDate.copy();
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        Assert.assertEquals(String.format("State template deposit frequency for %s", paymentTemplate.getPaymentTemplateAbbrev()), pNewDepFreqCd, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPayrollsOverStateThreshold_NY() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("36", "100");
        lawAmounts.put("54", "100");
        lawAmounts.put("56", "100");
        lawAmounts.put("57", "100");
        testPayrollsOverStateThreshold("NY", DepositFrequencyCode.THREEBANKINGDAY, DepositFrequencyCode.QUARTERLY, lawAmounts);
    }

    @Test
    public void testPayrollsOverStateThreshold_GA() throws Exception {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("12", "50001");
        testPayrollsOverStateThreshold("GA", DepositFrequencyCode.NEXTBANKINGDAY, DepositFrequencyCode.SEMIWEEKLY, lawAmounts);
    }

    public void testPayrollsOverStateThreshold(String state, DepositFrequencyCode pRollOverDepFreqCd, DepositFrequencyCode pNewDepFreqCd, HashMap<String, String> lawAmounts) throws Exception {

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        SpcfMoney statePaymentAmount = SpcfMoney.ZERO;
        for (String lawId : lawAmounts.keySet()) {
            statePaymentAmount = (SpcfMoney) statePaymentAmount.add(new SpcfMoney(lawAmounts.get(lawId)));
        }
        statePaymentAmount = (SpcfMoney) statePaymentAmount.multiply(new SpcfMoney("2"));

        String[] statesList = new String[]{state};
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");

        List<Company> companies = DataLoadServices.setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 10));
        Company company = companies.get(0);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertTrue("State template deposit frequency is not default deposit freq", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString().equals(paymentTemplate.getDefaultDepositFrequency()));
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar startDate = null;
        SpcfCalendar endDate = null;
        SpcfCalendar dueDate;
        SpcfCalendar initDate;
        if (effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
            startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
            endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
        } else if (effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
            startDate = SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone());
            endDate = SpcfCalendar.createInstance(2011, 8, 31, SpcfTimeZone.getLocalTimeZone());
        }

        dueDate = SpcfCalendar.createInstance(2011, 8, 12, SpcfTimeZone.getLocalTimeZone());
        dueDate = updateDueDate(dueDate, pRollOverDepFreqCd);
        initDate = dueDate.copy();
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);

        DataLoadServices.runPayrollRun(company, statesList, beginDate, new DateDTO("2011-08-12"), true, lawAmounts, PaymentTemplateCategory.Withholding);
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, initDate, dueDate, startDate, endDate, 1);

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, new DateDTO("2011-08-15"));
        if (pNewDepFreqCd.equals(DepositFrequencyCode.SEMIWEEKLY)) {
            startDate = SpcfCalendar.createInstance(2011, 8, 13, SpcfTimeZone.getLocalTimeZone());
            endDate = SpcfCalendar.createInstance(2011, 8, 16, SpcfTimeZone.getLocalTimeZone());
        }
        dueDate = SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone());
        dueDate = updateDueDate(dueDate, pRollOverDepFreqCd);
        initDate = dueDate.copy();
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(pNewDepFreqCd));
        PayrollServices.rollbackUnitOfWork();

    }

    public void testFollowsFederalThresholdPayrolls(String state, DepositFrequencyCode pRollOverDepFreqCd, DepositFrequencyCode pNewDepFreqCd, HashMap<String, String> lawAmounts, boolean pInAscending) throws Exception {

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        SpcfMoney statePaymentAmount = SpcfMoney.ZERO;
        for (String lawId : lawAmounts.keySet()) {
            statePaymentAmount = (SpcfMoney) statePaymentAmount.add(new SpcfMoney(lawAmounts.get(lawId)));
        }


        String[] statesList = new String[]{state};
        lawAmounts.put("61", "100000");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");

        List<Company> companies = DataLoadServices.setupCompany(12345678L, 1, statesList, PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 10));
        Company company = companies.get(0);

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(statesList[0], PaymentTemplateCategory.Withholding);
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, PSPDate.getPSPTime());
        assertTrue("State template deposit frequency is not default deposit freq", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString().equals(paymentTemplate.getDefaultDepositFrequency()));
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar startDate = null;
        SpcfCalendar endDate = null;
        SpcfCalendar dueDate = null;
        SpcfCalendar initDate = null;
        if (effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
            startDate = SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone());
            endDate = SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone());
            dueDate = SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone());
            initDate = SpcfCalendar.createInstance(2011, 10, 27, SpcfTimeZone.getLocalTimeZone());
        } else if (effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
            startDate = SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone());
            endDate = SpcfCalendar.createInstance(2011, 8, 31, SpcfTimeZone.getLocalTimeZone());
            dueDate = SpcfCalendar.createInstance(2011, 9, 15, SpcfTimeZone.getLocalTimeZone());
            initDate = SpcfCalendar.createInstance(2011, 9, 13, SpcfTimeZone.getLocalTimeZone());
        }

        DateDTO payrollRunDate1;
        DateDTO payrollRunDate2;
        if (pInAscending) {
            payrollRunDate1 = new DateDTO("2011-08-12");
            payrollRunDate2 = new DateDTO("2011-08-14");
        } else {
            payrollRunDate1 = new DateDTO("2011-08-14");
            payrollRunDate2 = new DateDTO("2011-08-12");
        }
        DataLoadServices.runPayrollRun(company, statesList, beginDate, payrollRunDate1, true, lawAmounts, PaymentTemplateCategory.Withholding);
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, initDate, dueDate, startDate, endDate, 1);

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollRunDate2);

        statePaymentAmount = (SpcfMoney) statePaymentAmount.multiply(new SpcfMoney("2"));

        dueDate = SpcfCalendar.createInstance(2011, 8, 14, SpcfTimeZone.getLocalTimeZone());
        dueDate = updateDueDate(dueDate, pRollOverDepFreqCd);
        initDate = dueDate.copy();
        CalendarUtils.addBusinessDays(initDate, -2);
        //Assert state ACH payment
        DataLoadServices.assertMmt(PaymentMethod.ACHCredit, statePaymentAmount, initDate, dueDate, startDate, endDate, 1);

        PayrollServices.beginUnitOfWork();
        effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate, SpcfCalendar.createInstance(2011, 8, 15, SpcfTimeZone.getLocalTimeZone()));
        assertTrue("State template deposit frequency is not switched", effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().equals(pNewDepFreqCd));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testTwoBackdatedOnSameDayAreCombined() {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", SpcfCalendar.createInstance(2010, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId("PADOR", company, "PA");
        DataLoadServices.addCOBRACompanyLaw(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "PA-501-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2010, 1, 1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 9, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backdated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-07-14"), emps, new String[]{"40", "61", "62", "63", "64", "1", "65", "66"}, new String[]{"400", "500", "1100", "550", "450", "250", "6.5", "5.6"});

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Submit another backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-07-15"), emps, new String[]{"40", "61", "62", "63", "64", "1", "65", "66"}, new String[]{"200", "500", "1100", "550", "450", "250", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //TODO: assert success (visually inspected for now)

    }

    @Test
    public void testAZStateThreshold941Payments_Over100K_State_Void_Partial_Submit() throws Exception {

        testAZStateThreshold_Void_Recall(true, true);


        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        String psid = "12345678";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        List<Employee> emps = new ArrayList<Employee>(employees);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-08-13"), emps, new String[]{"1", "61", "62", "65", "64", "63", "66", "5"}, new String[]{"3", "4", "5", "6", "7", "8", "9", "10"});


        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AZ-A1-PAYMENT"));
        assertEquals("Number of state payments", 1, moneyMovementTransactions.size());
        assertEquals("ACH Credit MMT Payment Frequency payment template", "AZ-A1-PAYMENT", moneyMovementTransactions.get(0).getPaymentFrequency().getPaymentTemplate().getPaymentTemplateCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test100KPayrollsWithSamePaycheckDate() throws Exception {

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2011, 1, 1);
        DataLoadServices.setPSPDate(beginDate);

        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 10, 31);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-08-19"), emps, new String[]{"1", "61", "62", "63", "64", "65", "66"}, new String[]{"1000", "6100", "6200", "6300", "6400", "650", "660"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("78000"), SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 10, 31, SpcfTimeZone.getLocalTimeZone()),
                                   SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        payrollRunDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-08-19"), emps, new String[]{"1", "61", "62", "63", "64", "65", "66"}, new String[]{"1000", "6100", "6200", "6300", "6400", "650", "660"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("78000"), SpcfCalendar.createInstance(2011, 8, 19, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 8, 22, SpcfTimeZone.getLocalTimeZone()),
                                   SpcfCalendar.createInstance(2011, 8, 19, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 8, 19, SpcfTimeZone.getLocalTimeZone()), 2);
        PayrollServices.rollbackUnitOfWork();

        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        payrollRunDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-08-19"), emps, new String[]{"1", "61", "62", "63", "64", "65", "66"}, new String[]{"1000", "6100", "6200", "6300", "6400", "650", "660"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("78000"), SpcfCalendar.createInstance(2011, 8, 19, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 8, 22, SpcfTimeZone.getLocalTimeZone()),
                                   SpcfCalendar.createInstance(2011, 8, 19, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 8, 19, SpcfTimeZone.getLocalTimeZone()), 3);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test100K_Void_paycheck_PSRV002901() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String[] states = {"AZ"};
        List<Company> companies = DataLoadServices.setupCompany(123456789l, 1, states, PaymentTemplateCategory.Withholding);
        Company company = companies.get(0);
        List<Employee> emps = new ArrayList<Employee>(Employee.findEmployees(company));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-23"), emps, new String[]{"61", "62", "63", "64", "1", "5"}, new String[]{"500", "400", "200", "100", "800", "50"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));

        DomainEntitySet<MoneyMovementTransaction> eftpsMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS));
        assertEquals("EFTPS MMTs", 1, eftpsMMTs.size());
        assertTrue("EFTPS MMT is not on Hold", eftpsMMTs.get(0).hasActiveOnHoldReasons());
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-23"), emps, new String[]{"61", "62", "63", "64", "1", "5"}, new String[]{"15000", "14000", "12000", "10000", "8000", "500"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Recall one paycheck from second payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> eftpsDirectMMTs = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPSDirectDebit));
        assertEquals("EFTPSDirectDebit MMTs", 1, eftpsDirectMMTs.size());
        assertFalse("EFTPSDirectDebit MMT is on Hold", eftpsDirectMMTs.get(0).hasActiveOnHoldReasons());
        PayrollServices.rollbackUnitOfWork();
    }

    //PSRV003119: NJ-NJ927PWH-PAYMENT Incorrect Settlement Dates
    @Test
    public void testNJ_WHPaymentSettlementDayForBackDatedPayroll() {

        DataLoadServices.setPSPDate(2012, 1, 1);
        SpcfCalendar supportDate = PSPDate.getPSPTime();
        String[] statesList = new String[]{"NJ"};
        Company company = assertOne(DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding));

        DataLoadServices.setPSPDate(2012, 4, 19);

        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2012-04-17"), true);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2012, 5, 2, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);

        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setDueDate(SpcfCalendar.createInstance(2012, 4, 25, SpcfTimeZone.getLocalTimeZone())).find());
        assertEquals("Payment Method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("Payment Amount", new SpcfMoney("66"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Initiation Date", statePaymentInitiationDate, moneyMovementTransaction.getInitiationDate().toLocal());
        FinancialTransaction financialTransaction = assertOne(moneyMovementTransaction.getFinancialTransactionCollection());
        assertEquals("Settlement Date", SpcfCalendar.createInstance(2012, 5, 2, SpcfTimeZone.getLocalTimeZone()), financialTransaction.getSettlementDate().toLocal());
        assertEquals("Settlement Day", Calendar.WEDNESDAY, financialTransaction.getSettlementDate().getDayOfWeek());
        assertEquals("Due Day", Calendar.WEDNESDAY, moneyMovementTransaction.getDueDate().getDayOfWeek());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testSUIBackDateHold() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction suiPayment = assertOne(MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI)));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.finalizePayment(suiPayment);

        // submit a second payroll in the same quarter
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> suiPayments = MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI));
        MoneyMovementTransaction finalizedPayment = assertOne(suiPayments.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ATFFinalized)));
        MoneyMovementTransaction onHoldPayment = assertOne(suiPayments.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)));
        assertOne(onHoldPayment.getTaxPaymentOnHoldReasonCollection().find(TaxPaymentOnHoldReason.OnHoldReasonCd().equalTo(PaymentOnHoldReason.BackDate).And(TaxPaymentOnHoldReason.ExpirationDate().isNull())));
        assertNotNull(onHoldPayment.getActiveOnHoldReasons().getFirst().getCompany());
        assertEquals(onHoldPayment.getCompany(),onHoldPayment.getActiveOnHoldReasons().getFirst().getCompany());
        PayrollServices.rollbackUnitOfWork();

        // remove holds to combine
        DataLoadServices.unfinalizePayment(finalizedPayment);
        PayrollServices.beginUnitOfWork();
        onHoldPayment = Application.refresh(onHoldPayment);
        MoneyMovementTransaction.removeTaxPaymentOnHoldReason(onHoldPayment, PaymentOnHoldReason.BackDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        suiPayment = assertOne(MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI)));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.finalizePayment(suiPayment);
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2012, 4, 4, SpcfTimeZone.getLocalTimeZone()));

        // submit a payroll after one payment for the quarter has been submitted
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-03-29"));

        PayrollServices.beginUnitOfWork();
        suiPayments = MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI));
        onHoldPayment = assertOne(suiPayments.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)));
        assertOne(onHoldPayment.getTaxPaymentOnHoldReasonCollection().find(TaxPaymentOnHoldReason.OnHoldReasonCd().equalTo(PaymentOnHoldReason.BackDate).And(TaxPaymentOnHoldReason.ExpirationDate().isNull())));
        MoneyMovementTransaction.removeTaxPaymentOnHoldReason(onHoldPayment, PaymentOnHoldReason.BackDate);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.finalizePayment(onHoldPayment);
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2012, 4, 26, SpcfTimeZone.getLocalTimeZone()));

        // submit a payroll more than one month after the due date
        //This is invalid scenario after fix PSP-6508
        /*
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-05-06"));

        PayrollServices.beginUnitOfWork();
        suiPayments = MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI));
        onHoldPayment = assertOne(suiPayments.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)));
        assertOne(onHoldPayment.getTaxPaymentOnHoldReasonCollection().find(TaxPaymentOnHoldReason.OnHoldReasonCd().equalTo(PaymentOnHoldReason.BackDate).And(TaxPaymentOnHoldReason.ExpirationDate().isNull())));
        PayrollServices.rollbackUnitOfWork();
        */


    }

    @Test
    public void testUpperLimitCombinesFollowsFed() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        submitPayroll(company, new DateDTO("2011-01-10"), "11000");
        submitPayroll(company, new DateDTO("2011-01-10"), "10");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction caPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                               .setCompany(company)
                                                                               .setPaymentTemplateCd("CA-PITSDI-PAYMENT")
                                                                               .find());
        assertEquals(new SpcfMoney("44040"), caPayment.getMoneyMovementTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 1, 8, SpcfTimeZone.getLocalTimeZone()), caPayment.getPaymentPeriodBegin().toLocal());
        assertEquals(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()), caPayment.getPaymentPeriodEnd().toLocal());
        assertEquals(DepositFrequencyCode.NEXTBANKINGDAY, caPayment.getPaymentFrequency().getPaymentFrequencyId());


        //but EFTPS direct debits never combine (already sent LC)
        DomainEntitySet<MoneyMovementTransaction> directDebits = MoneyMovementTransaction.findTaxPayments()
                                                                                         .setCompany(company)
                                                                                         .set941()
                                                                                         .setPaymentMethods(new PaymentMethod[]{PaymentMethod.EFTPSDirectDebit})
                                                                                         .find();
        assertEquals(2, directDebits.size());
        for (MoneyMovementTransaction directDebit : directDebits) {
            assertEquals(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()), directDebit.getPaymentPeriodBegin().toLocal());
            assertEquals(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()), directDebit.getPaymentPeriodEnd().toLocal());
            assertEquals(DepositFrequencyCode.SEMIWEEKLY, directDebit.getPaymentFrequency().getPaymentFrequencyId());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testUpperLimitCombinesDoesNotFollowFed() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("HI-VP1-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        submitPayroll(company, new DateDTO("2011-01-10"), "21000");
        submitPayroll(company, new DateDTO("2011-01-10"), "3000");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction hiPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                               .setCompany(company)
                                                                               .setPaymentTemplateCd("HI-VP1-PAYMENT")
                                                                               .find());
        assertEquals(new SpcfMoney("48000"), hiPayment.getMoneyMovementTransactionAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), hiPayment.getPaymentPeriodBegin().toLocal());
        assertEquals(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()), hiPayment.getPaymentPeriodEnd().toLocal());
        assertEquals(DepositFrequencyCode.SEMIWEEKLY, hiPayment.getPaymentFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBackdatePriorToProcessingStartEvent() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 6, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("HI-VP1-PAYMENT", SpcfCalendar.createInstance(2010, 6, 1));

        DataLoadServices.setPSPDate(2010, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2010, 10, 1);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2010-09-24"));

        PayrollServices.beginUnitOfWork();
        CompanyEvent event = assertOne(Application.find(CompanyEvent.class, CompanyEvent.EventTypeCd().equalTo(EventTypeCode.BackdatePriorToProcessingStart)));
        assertEquals(payrollRun.getId().toString(), assertOne(event.getCompanyEventDetails(EventDetailTypeCode.PayrollRunId)).getValue());
        PayrollServices.rollbackUnitOfWork();
    }



    @Test
    public void testBackdatedPayrollsThatExceedThresholdUse5DayLogic() {
        //PSP-3178
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("HI-VP1-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 7);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        List<String> lawIds = DataLoadServices.getCompanyLawsIds(company);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                //don't add dead laws
                if (!companyLaw.getLaw().shouldExcludeFromUI()) {
                    lawIds.add(companyLaw.getLaw().getLawId());
                }
            }

        }
        String[] amounts = new String[lawIds.size()];
        Arrays.fill(amounts, "1000.00"); //threshold is $700
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getCloudEmployees()), lawIds.toArray(new String[lawIds.size()]), amounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction nyPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NY-1MN-PAYMENT").find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 4, SpcfTimeZone.getLocalTimeZone()), nyPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()), nyPayment.getInitiationDate().toLocal());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBackdatedFederal5Day() {
        //fed exceeds and still next day because direct, follows fed follows but gets 5 day logic
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 7);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        String[] laws = {"1", "6"};
        String[] amounts = {"100000.00", "12.22"};

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getCloudEmployees()),laws, amounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction fedPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), fedPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 7, SpcfTimeZone.getLocalTimeZone()), fedPayment.getInitiationDate().toLocal());

        MoneyMovementTransaction caPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("CA-PITSDI-PAYMENT").find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), caPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()), caPayment.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBackdatedFederal5Day_Multiple() {
        //two backdated payrolls at different times:
        //fed exceeds and split because of 100K logic itself (EFTPSDD+Normal), and direct on time and regular delayed (to the original date); follows fed follows, is split, and both delayed differently
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 5);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        String[] laws = {"1", "6"};
        String[] amounts = {"100.00", "1.22"};

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getCloudEmployees()),laws, amounts);

        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction fedPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 4, SpcfTimeZone.getLocalTimeZone()), fedPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()), fedPayment.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 7);

        PayrollServices.beginUnitOfWork();
        amounts = new String[]{"100000.00", "12.22"};
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-07"), new ArrayList<Employee>(company.getCloudEmployees()),laws, amounts);

        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        fedPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 4, SpcfTimeZone.getLocalTimeZone()), fedPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()), fedPayment.getInitiationDate().toLocal());

        MoneyMovementTransaction fed100KPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setDirect().find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 8, SpcfTimeZone.getLocalTimeZone()), fed100KPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 7, SpcfTimeZone.getLocalTimeZone()), fed100KPayment.getInitiationDate().toLocal());

        DomainEntitySet<MoneyMovementTransaction> caPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("CA-PITSDI-PAYMENT")
                                                                                       .find()
                                                                                       .sort(MoneyMovementTransaction.InitiationDate());
        assertEquals(2, caPayments.size());
        MoneyMovementTransaction firstLatePayment = caPayments.get(0);
        assertEquals(SpcfCalendar.createInstance(2013, 1, 8, SpcfTimeZone.getLocalTimeZone()), firstLatePayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 7, SpcfTimeZone.getLocalTimeZone()), firstLatePayment.getInitiationDate().toLocal());

        MoneyMovementTransaction secondLatePayment = caPayments.get(1);
        assertEquals(SpcfCalendar.createInstance(2013, 1, 4, SpcfTimeZone.getLocalTimeZone()), secondLatePayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()), secondLatePayment.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBackdatedFederal5Day_Multiple_OneTimely_OneBackdate() {
        //fed exceeds and split because of 100K logic itself (EFTPSDD+Normal), both are on time; follows fed follows and is split; timely on time, backdated delayed
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2012, 12, 15);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 12, 28);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        String[] laws = {"1", "6"};
        String[] amounts = {"100.00", "1.22"};

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getCloudEmployees()),laws, amounts);

        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(2013, 1, 2);

        PayrollServices.beginUnitOfWork();
        amounts = new String[]{"100000.00", "12.22"};
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getCloudEmployees()),laws, amounts);

        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction fedPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), fedPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), fedPayment.getInitiationDate().toLocal());

        MoneyMovementTransaction fed100KPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setDirect().find());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), fed100KPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), fed100KPayment.getInitiationDate().toLocal());

        DomainEntitySet<MoneyMovementTransaction> caPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("CA-PITSDI-PAYMENT")
                                                                                       .find()
                                                                                       .sort(MoneyMovementTransaction.InitiationDate());
        assertEquals(2, caPayments.size());
        MoneyMovementTransaction timelyPayment = caPayments.get(0);
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), timelyPayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), timelyPayment.getInitiationDate().toLocal());

        MoneyMovementTransaction latePayment = caPayments.get(1);
        assertEquals(SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), latePayment.getDueDate().toLocal());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 9, SpcfTimeZone.getLocalTimeZone()), latePayment.getInitiationDate().toLocal());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBackDated3Threshold3PayrollsOneCompleted(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010,1,1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2010,1,1));

        DataLoadServices.setPSPDate(2013,1,1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.invalidateDepositFrequencies(company, "NY-1MN-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(),"NY-1MN-PAYMENT",DepositFrequencyCode.QUARTERLY,SpcfCalendar.createInstance(2013,1,1));


        DataLoadServices.setPSPDate(2013,1,6);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        List<String> lawIds = DataLoadServices.getCompanyLawsIds(company);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()){
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()){
                //don't add dead laws
                if (!companyLaw.getLaw().shouldExcludeFromUI()){
                    lawIds.add(companyLaw.getLaw().getLawId());
                }
            }
        }
        String[] amounts = new String[lawIds.size()];
        Arrays.fill(amounts, "10.00");
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getEmployees()), lawIds.toArray(new String[lawIds.size()]), amounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(),payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013,1,7);
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getEmployees()), lawIds.toArray(new String[lawIds.size()]), amounts);

        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013,1,18);
        DataLoadServices.runOffload();
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
        DataLoadServices.runACHTransactionProcessor();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate(PaymentTemplate.IRS_941));

        DataLoadServices.setPSPDate(2013,1,8);

        // Create a 2nd Payroll that is Back-dated that contains NY taxes
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        String[] amounts2 = new String[lawIds.size()];
        Arrays.fill(amounts2, "150.00");
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-01"), new ArrayList<Employee>(company.getEmployees()), lawIds.toArray(new String[lawIds.size()]), amounts2);

        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //two completed payrolls should have initiation date as soon as possible; new one should have 5 day wait period.
        List<SpcfCalendar> expectedNYPaymentInitiationDates = Arrays.asList(
                SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2013, 1, 14, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2013, 1, 15, SpcfTimeZone.getLocalTimeZone()));
        int i = 0;
        for (PayrollRun payrollRun : PayrollRun.findPayrollRuns(company).sort(PayrollRun.PayrollRunDate())) {
            FinancialTransaction atc = payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.Law().PaymentTemplate().PaymentTemplateCd().equalTo("NY-1MN-PAYMENT")).getFirst();
            assertEquals(atc.getMoneyMovementTransaction().getInitiationDate().toLocal(), expectedNYPaymentInitiationDates.get(i++));
        }
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testNegativeMMT() {
        MoneyMovementTransaction mmt = new MoneyMovementTransaction();
        //added company reference to mmt for logging failure
        DataLoadServices.setPSPDate(2012, 12, 15);
        Company company = DataLoadPalette.setupTaxCompany();
        mmt.setCompany(company);
        try {
            mmt.setMoneyMovementTransactionAmount(new SpcfMoney("-42.00"));
            fail("expected exception");
        } catch (RuntimeException re) {
            assertEquals("Attempting to assign a negative amount to a MMT", re.getMessage());
        }
    }

    private SpcfUniqueId  createFailedTransaction(String psid,Company company, List<Employee> emps,String payrollRunDate,String offloadImpoundsDate,String empTaxReturnDate){

        //Run payroll for employees

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO(payrollRunDate), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"50", "120", "5.5", "45", "20", "25"});

        ProcessResult<PayrollRun>  processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(offloadImpoundsDate);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(empTaxReturnDate);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TransactionReturnBatch transactionReDebitReturnBatch = new TransactionReturnBatch();
        transactionReDebitReturnBatch.setACHReturnFileName(null);
        transactionReDebitReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReDebitReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReDebitReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReDebitReturnBatch = Application.save(transactionReDebitReturnBatch);

        TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxRedebit);
        TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
        TransactionType newPayrollTxnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);

        Criterion<FinancialTransaction> where = FinancialTransaction.TransactionType().equalTo(txnType)
                                                                    .Or(FinancialTransaction.TransactionType().equalTo(newPayrollTxnType))
                                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));

        DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReDebitReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }
        transactionReDebitReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReDebitReturnBatch = Application.save(transactionReDebitReturnBatch);

        SpcfUniqueId reDebitBatchId = transactionReDebitReturnBatch.getId();
        Application.commitUnitOfWork();
        ReturnFileParser returnsProcessor = new ReturnFileParser();
        returnsProcessor.processTransactionReturns(reDebitBatchId);

        logger.info("Finished creating failed transaction");

        return reDebitBatchId ;

    }

    private void createStrikeEvent(Company company,String dateOfStrikeEvent){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(dateOfStrikeEvent);
        Application.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyEvent> processResult = PayrollServices.companyManager.addStrikeEvent(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                "Strike Reason",
                PSPDate.getPSPTime());
        assertEquals("Add Strike", processResult.isSuccess(), true);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testStrikeCountPerYear() throws Throwable {

        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();

        //Create Company
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        //Add employees
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        SpcfUniqueId reDebitBatchId;
        reDebitBatchId = this.createFailedTransaction(psid, company, emps, "2010-11-02", "20101029000000", "20110210050000");
        createStrikeEvent(company,"20111010101010");

        //Date not in last one 12 months span
        //Modify the date for different test cases
        createStrikeEvent( company,"20100912181010");


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110913001010");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<File> rejectFiles = AchReturnAccountingFile.createFile(reDebitBatchId);
        PayrollServices.commitUnitOfWork();
        assertEquals("File not generated:", 1, rejectFiles.size());
        File expected = new File(System.getProperty("user.dir"), "PSE/batch-jobs-tests/src/test/resources/offload/expected/test-psp-ach-strike-returns.csv");
        assertTrue("ACH returns accounting files do not match", compareFiles(rejectFiles.get(0), expected));




    }

    private void submitPayroll(Company company, DateDTO checkDate, String amountPerLaw) {
        List<String> lawIds = DataLoadServices.getCompanyLawsIds(company);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        String[] amounts = new String[lawIds.size()];
        Arrays.fill(amounts, amountPerLaw);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, checkDate, new ArrayList<Employee>(company.getEmployees()), lawIds.toArray(new String[lawIds.size()]), amounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
    }

    private SpcfCalendar updateDueDate(SpcfCalendar dueDate, DepositFrequencyCode pRollOverDepFreqCd) {
        if (pRollOverDepFreqCd.equals(DepositFrequencyCode.THREEBANKINGDAY)) {
            CalendarUtils.addBusinessDays(dueDate, 3);
        } else if (pRollOverDepFreqCd.equals(DepositFrequencyCode.NEXTBANKINGDAY)) {
            CalendarUtils.addBusinessDays(dueDate, 1);
        }
        return dueDate;
    }

    private PayrollRun runPayroll(Company company, DateDTO date, String amount) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"6", "67", "87", "142", "61", "62", "63", "64", "66", "1"}, new String[]{amount, amount, amount, amount, amount, amount, amount, amount, amount, amount});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }

    private boolean compareFiles(File pLhsFile, File pRhsFile) {
        try {
            BufferedReader reader;

            StringWriter lhsContent = new StringWriter();
            reader = new BufferedReader(new FileReader(pLhsFile));
            try {
                while (reader.ready()) {
                    lhsContent.write(reader.readLine());
                }
            } finally {
                reader.close();
            }

            StringWriter rhsContent = new StringWriter();
            reader = new BufferedReader(new FileReader(pRhsFile));
            try {
                while (reader.ready()) {
                    rhsContent.write(reader.readLine());
                }
            } finally {
                reader.close();
            }

            boolean filesMatch = lhsContent.toString().substring(0, lhsContent.toString().length() - 60).equals(rhsContent.toString().substring(0, rhsContent.toString().length() - 60));

            if (!filesMatch) {
                System.out.println("LHS file: " + pLhsFile.getPath());
                System.out.println("RHS file: " + pRhsFile.getPath());
                System.out.println("LHS content: " + lhsContent.toString());
                System.out.println("RHS content: " + rhsContent.toString());
            }

            return filesMatch;
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error comparing files (lhs: %s, rhs: %s)", pLhsFile.getPath(), pRhsFile.getPath()), t);
        }
    }

    
    /**
     * Earlier there was Upper Limit for Jurisdiction - MA, Agency - MADOR, PaymentTemplate - MA-M941-PAYMENT, Payment Frequency - QUARTERLY
     * So whenever Tax Payment amount exceeds the Upper Limit (1200), Deposit Frequency will be rolled over automatically from QUARTERLY to MONTHLY
     * 
     * But from January-2017, Tax Agency has removed the Upper Limit, so there won't be any more deposit frequency auto roll over.
     * 
     * Below test has been modified to test the new behavior
     */
    @Test
    public void testUpperLimitForMADepFreqQuarterly2015() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-M941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));


        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "MA-M941-PAYMENT");

        //Submit a payroll with amount less than the threshold for Upperlimit (1200) for PaymentFrequency QUARTERLY
        submitPayroll(company, new DateDTO("2011-01-10"), "200");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction maPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                               .setCompany(company)
                                                                               .setPaymentTemplateCd("MA-M941-PAYMENT")
                                                                               .find());

        assertEquals(DepositFrequencyCode.QUARTERLY, maPayment.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals(SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), maPayment.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        //Submit a payroll with amount more than the threshold for Upperlimit (1200) for PaymentFrequency QUARTERLY, which will
        //change the depositfrequency to the rolloverfrequency MONTHLY
        submitPayroll(company, new DateDTO("2011-01-10"), "3000");
        PayrollServices.beginUnitOfWork();
        maPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                      .setCompany(company)
                                                      .setPaymentTemplateCd("MA-M941-PAYMENT")
                                                      .find());
        assertEquals(SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), maPayment.getDueDate().toLocal());
        assertEquals(DepositFrequencyCode.QUARTERLY, maPayment.getPaymentFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Earlier there was Upper Limit for Jurisdiction - MA, Agency - MADOR, PaymentTemplate - MA-M941-PAYMENT, Payment Frequency - ANNUAL
     * So whenever Tax Payment amount exceeds the Upper Limit (100), Deposit Frequency will be rolled over automatically from ANNUAL to QUARTERLY
     * 
     * But from January-2017, Tax Agency has removed the Upper Limit, so there won't be any more deposit frequency auto roll over.
     * 
     * Below test has been modified to test the new behavior
     */
    @Test
    public void testUpperLimitForMADepFreqAnnual2015() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-M941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));


        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-M941-PAYMENT", DepositFrequencyCode.ANNUAL);

        //Submit a payroll with amount less than the threshold for Upperlimit (100) for PaymentFrequency ANNUAL
        submitPayroll(company, new DateDTO("2011-01-10"), "20");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction maPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                                               .setCompany(company)
                                                                               .setPaymentTemplateCd("MA-M941-PAYMENT")
                                                                               .find());

        assertEquals(DepositFrequencyCode.ANNUAL, maPayment.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()), maPayment.getDueDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        //Submit a payroll with amount more than the threshold for Upperlimit (100) for PaymentFrequency ANNUAL, which will
        //change the depositfrequency to the rolloverfrequency QUARTERLY
        submitPayroll(company, new DateDTO("2011-01-10"), "300");
        PayrollServices.beginUnitOfWork();
        maPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                                                      .setCompany(company)
                                                      .setPaymentTemplateCd("MA-M941-PAYMENT")
                                                      .find());
        assertEquals(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()), maPayment.getDueDate().toLocal());
        assertEquals(DepositFrequencyCode.ANNUAL, maPayment.getPaymentFrequency().getPaymentFrequencyId());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testMNStateTaxesPayroll() throws Exception {
        String psid = "123456789";
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2001, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.updatePaymentTemplateSupportedDate("MN-DEED1-PAYMENT", supportedDate);
        DataLoadServices.updatePaymentTemplateSupportedDate("MN-MW1-PAYMENT", supportedDate);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company1, null);
        DataLoadServices.addCompanyBankAccount(company1);
        DataLoadServices.enrollEFTPS(company1);
        DataLoadServices.addFederalAndMNStateTaxCompanyLaws(company1);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company1);
        DataLoadServices.activateTaxService(company1);
        DataLoadServices.activateDDService(company1);
        List<Employee> emps = DataLoadServices.addEEs(company1, 2);
        String[] statesList = new String[]{"AZ","MN"};
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("106", "100");
        lawAmounts.put("25", "75");
        lawAmounts.put("106", "50");
        DateDTO payrollDate = new DateDTO("2015-01-07");
        List<Company> companies = DataLoadServices.setupCompany(123456790L, 5, statesList, PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate("MN-DEED1-PAYMENT", supportedDate);
        int wh_id = 1;
        int ui_id = 1;
        for (Company company : companies) {
            DataLoadServices.addFederalAndMNStateTaxCompanyLaws(company);
            DataLoadServices.updateAgencyTaxpayerId(company,"MN-MW1-PAYMENT","113456"+wh_id++);
            DataLoadServices.updateAgencyTaxpayerId(company,"MN-DEED1-PAYMENT","1223456"+ui_id++);
            DataLoadServices.updateACHAgentEnabledFlags(company,"MN-DEED1-PAYMENT",true);
            DataLoadServices.updateACHAgentEnabledFlags(company,"MN-MW1-PAYMENT",true);
            DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);
        }
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateAgencyTaxpayerId(company1,"MN-MW1-PAYMENT","1134567");
        DataLoadServices.updateAgencyTaxpayerId(company1,"MN-DEED1-PAYMENT","12234567");
        DataLoadServices.updateACHAgentEnabledFlags(company1,"MN-DEED1-PAYMENT",true);
        DataLoadServices.updateACHAgentEnabledFlags(company1,"MN-MW1-PAYMENT",true);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company1, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 11, 5));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndMNStateTaxes(payrollRunDTO, company1, new DateDTO("2015-11-5"), emps);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        SpcfCalendar ofloadDate = SpcfCalendar.createInstance(2015,1,5,17,15,0,0,SpcfTimeZone.getLocalTimeZone());
        //DataLoadServices.runOffload(ofloadDate);
        ofloadDate = SpcfCalendar.createInstance(2015,4,30,13,30,0,0,SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(ofloadDate, -achTaxOffloadOffset);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, ofloadDate, ACHFileType.Tax);

        //DataLoadServices.runOffloadTaxPayments(ofloadDate);
        String fileName = offloader.getOutputFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        for (int i = 0; i < lines.length; i++) {
            //Company header batches
            if(lines[i].startsWith("522") && lines[i].contains(EntryDetailRecord.getStateSpecificCompanyDiscretionaryData(EntryDetailRecord.MN_UI_PAYMENT_TEMPLATE)) )  {
                String companyHeaderLine = "5220COMPUTING RESOURACOMRE\\s{14}9118556001CCDMN UI PAY 150430150430   102100002\\d{7}";
                assertTrue("CompanyHeaderLine didn't matches output for MN UI:\n" + companyHeaderLine + "\nIn output:\n" + lines[i], isLinesMatches(companyHeaderLine,lines[i]));
            }else if(lines[i].startsWith("5220"))  {
                String companyHeaderLine = "5220TEST_COMPANY_.*\\s{12}\\d{19}CCDEFT TAX PY150430150430   102100002\\d{7}";
                assertTrue("CompanyHeaderLine didn't matches output:\n" + companyHeaderLine + "\nIn output:\n" + lines[i], isLinesMatches(companyHeaderLine,lines[i]));
            } else if(lines[i].startsWith("5225"))  {
                String companyHeaderLine = "5225INTUIT\\s{21}\\d{19}CCDEFT TAX PY150430150430   102100002\\d{7}";
                assertTrue("CompanyHeaderLine didn't matches output:\n" + companyHeaderLine + "\nIn output:\n" + lines[i], isLinesMatches(companyHeaderLine,lines[i]));
            }

            //Record data
            if(lines[i].startsWith("622") || lines[i].startsWith("705")){
                long traceNumber =  Long.parseLong(lines[i].substring(lines[i].length() - 7, lines[i].length()));
                PayrollServices.beginUnitOfWork();
                EntryDetailRecord edr=EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(traceNumber);
                if(lines[i].startsWith("622")) {  //Record data  validation
                    boolean isRecordDataMatches = lines[i].contains(edr.getRecordData());
                    assertTrue("CompanyRecordDataLine didn't matches output:\n",isRecordDataMatches);
                    String agentId = lines[i].substring(39,54).trim();
                    String recievingCompany = lines[i].substring(54,76).trim();
                    String descretionaryData = lines[i].substring(76,78).trim();
                    if(edr.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd().equals(EntryDetailRecord.MN_UI_PAYMENT_TEMPLATE)){
                        assertEquals("MN AGENT ID is wrong",EntryDetailRecord.MN_UI_AGENT_ID,agentId);
                        assertEquals("MN RECEIVING COMPANY ID is wrong",EntryDetailRecord.MN_UI_RECIEVING_COMPANY_NAME,recievingCompany);
                        assertEquals("MN DESCRETIONARY DATA is wrong","00",descretionaryData);
                    }else{
                        assertEquals("MN INDIVIDUAL ID is wrong",edr.getCompany().getSourceCompanyId(),agentId);
                        assertEquals("MN RECEIVING COMPANY ID is wrong",edr.getCompany().getLegalName(),recievingCompany);
                        assertEquals("MN DESCRETIONARY DATA is wrong","",descretionaryData);

                    }
                } else if(lines[i].startsWith("705")) {  //Addenda record validation
                    boolean isTxpRecordDataMatches = lines[i].contains(edr.getTxpRecordData());
                    assertTrue("CompanyTxpRecordDataLine didn't matches output:\n",isTxpRecordDataMatches);
                    String txpIdentifier = lines[i].substring(3,6).trim();
                    if(edr.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd().equals(EntryDetailRecord.MN_UI_PAYMENT_TEMPLATE)){
                        assertEquals("MN DESCRETIONARY DATA is wrong","000",txpIdentifier);
                    }else{
                        assertEquals("MN DESCRETIONARY DATA is wrong","TXP",txpIdentifier);
                    }
                }

                PayrollServices.rollbackUnitOfWork();
            }
            //footer data
            if(lines[i].startsWith("822")  )  {
                String recordDataLine = "822\\d{41}+9118556001                         02100002\\d{7}";
                assertTrue("CompanyHeaderLine didn't matches output for MN UI:\n" + recordDataLine + "\nIn output:\n" + lines[i], isLinesMatches(recordDataLine,lines[i]));
            }

        }

    }

    public static boolean isLinesMatches(String expected,String outLine){
        Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher(outLine);
        return  matcher.matches();
    }

    @Test
    public void testZeroPaymentTax() {
        String paymentTemplateCd = "HI-VP1-PAYMENT";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2017, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("HI-VP1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2017, 12, 8);
        Company company = DataLoadPalette.setupTaxCompany();

        submitPayroll(company, new DateDTO("2017-12-12"), "2100");
        submitPayroll(company, new DateDTO("2017-12-28"), "3000");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction hiPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                .setCompany(company)
                .setPaymentTemplateCd(paymentTemplateCd)
                .find());


        assertEquals(SpcfCalendar.createInstance(2017, 12, 1, SpcfTimeZone.getLocalTimeZone()), hiPayment.getPaymentPeriodBegin().toLocal());
        assertEquals(SpcfCalendar.createInstance(2017, 12, 31, SpcfTimeZone.getLocalTimeZone()), hiPayment.getPaymentPeriodEnd().toLocal());
        assertEquals(DepositFrequencyCode.MONTHLY, hiPayment.getPaymentFrequency().getPaymentFrequencyId());
        SpcfCalendar spcfCalendar = hiPayment.getDueDate();
        spcfCalendar.addDays(-1);
        hiPayment.setDueDate(spcfCalendar);
        assertEquals(SpcfCalendar.createInstance(2018, 1, 15, SpcfTimeZone.getLocalTimeZone()), hiPayment.getDueDate().toLocal());

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2017, 12, 13);
        new ProcessZeroPayments().process(processingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateCd,
                DepositFrequencyCode.MONTHLY.toString(),
                CalendarUtils.convertToRulesCalendar(processingDate));

        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                company,
                paymentTemplate,
                paymentPeriod);

        assertEquals("Zero Payment Money Movement Transaction", 1, mmts.size());
        PayrollServices.commitUnitOfWork();


        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCovidAdjustmentCreation() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt22 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-EE", "TTT22", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt23 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Family", "TTT23", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt24 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-FMLA", "TTT24", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt25 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Health", "TTT25", PayrollItemCode.OtherNonTaxableEmployerContribution);
        CompanyPayrollItem ttt28 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Medicare", "TTT28", PayrollItemCode.OtherNonTaxableEmployerContribution);
        CompanyPayrollItem ttt30 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-EE", "TTT30", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt31 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-Family", "TTT31", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt32 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-FMLA", "TTT32", PayrollItemCode.Compensation);

        DataLoadServices.addCompanyLaws(company, "214");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.setPayPeriodEndDate(new DateDTO(2021,03,31));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt22.getSourcePayrollItemId(), null, new SpcfMoney("5.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt23.getSourcePayrollItemId(), null, new SpcfMoney("10.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt24.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));
        firstPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt28.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        firstPaycheck.setPayPeriodEndDate(new DateDTO(2021,03,31));
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt25.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt30.getSourcePayrollItemId(), null, new SpcfMoney("12.00")));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt31.getSourcePayrollItemId(), null, new SpcfMoney("14.00")));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt32.getSourcePayrollItemId(), null, new SpcfMoney("16.00")));

        DataLoadServices.setPSPDate(2021, 3, 24);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2021, 3, 26));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("104.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "85.00", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "214", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCovidAdjustmentCreationAfterEndDate() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt22 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-EE", "TTT22", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt23 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Family", "TTT23", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt24 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-FMLA", "TTT24", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt25 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Health", "TTT25", PayrollItemCode.OtherNonTaxableEmployerContribution);
        CompanyPayrollItem ttt28 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Medicare", "TTT28", PayrollItemCode.OtherNonTaxableEmployerContribution);
        CompanyPayrollItem ttt30 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-EE", "TTT30", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt31 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-Family", "TTT31", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt32 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-FMLA", "TTT32", PayrollItemCode.Compensation);
        DataLoadServices.addCompanyLaws(company, "214");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.setPayPeriodEndDate(new DateDTO(2021,10,04));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt22.getSourcePayrollItemId(), null, new SpcfMoney("5.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt23.getSourcePayrollItemId(), null, new SpcfMoney("10.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt24.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));
        firstPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt28.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        firstPaycheck.setPayPeriodEndDate(new DateDTO(2021,10,04));
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt25.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt30.getSourcePayrollItemId(), null, new SpcfMoney("12.00")));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt31.getSourcePayrollItemId(), null, new SpcfMoney("14.00")));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt32.getSourcePayrollItemId(), null, new SpcfMoney("16.00")));

        DataLoadServices.setPSPDate(2021, 9, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2021, 10, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals("Process Result Message Code:" ,"12010",processResult.getMessages().get(0).getMessageCode());
    }
    @Test
    public void testCovidAdjustmentBackDated() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt22 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-EE", "TTT22", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt23 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Family", "TTT23", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt24 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-FMLA", "TTT24", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt25 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Health", "TTT25", PayrollItemCode.OtherNonTaxableEmployerContribution);
        CompanyPayrollItem ttt28 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Medicare", "TTT28", PayrollItemCode.OtherNonTaxableEmployerContribution);
        CompanyPayrollItem ttt30 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-EE", "TTT30", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt31 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-Family", "TTT31", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt32 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-ARPA-FMLA", "TTT32", PayrollItemCode.Compensation);
        DataLoadServices.addCompanyLaws(company, "214");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.setPayPeriodEndDate(new DateDTO(2021,9,27));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt22.getSourcePayrollItemId(), null, new SpcfMoney("5.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt23.getSourcePayrollItemId(), null, new SpcfMoney("10.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt24.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));
        firstPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt28.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        firstPaycheck.setPayPeriodEndDate(new DateDTO(2021,9,27));
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt25.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt30.getSourcePayrollItemId(), null, new SpcfMoney("12.00")));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt31.getSourcePayrollItemId(), null, new SpcfMoney("14.00")));
        secondPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt32.getSourcePayrollItemId(), null, new SpcfMoney("16.00")));

        DataLoadServices.setPSPDate(2021, 10, 4);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2021, 10, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
    }

    @Test
    public void testEmployeeRetentionAdjustmentCreation2020() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt26 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - EE", "TTT26", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt27 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - Health", "TTT27", PayrollItemCode.OtherNonTaxableEmployerContribution);
        DataLoadServices.addCompanyLaws(company, "215");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt26.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt27.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        DataLoadServices.setPSPDate(2020, 3, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 4, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("176.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "13.00", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "215", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeeRetentionAdjustmentCreation2021() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt26 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - EE", "TTT26", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt27 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - Health", "TTT27", PayrollItemCode.OtherNonTaxableEmployerContribution);
        DataLoadServices.addCompanyLaws(company, "215");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt26.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt27.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        DataLoadServices.setPSPDate(2021, 3, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2021, 4, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("170.80").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "18.20", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "215", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeeRetentionAdjustmentCreationAfterEndDate() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt26 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - EE", "TTT26", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt27 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - Health", "TTT27", PayrollItemCode.OtherNonTaxableEmployerContribution);
        DataLoadServices.addCompanyLaws(company, "215");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt26.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt27.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        DataLoadServices.setPSPDate(2022, 1, 1);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2022, 1, 11));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 0, agencyTaxDebit.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testFicaDeferralAdjustmentCreation() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.addCompanyLaws(company, "216");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
        companyAgency.setErFicaDeferralEnabled(true);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2020, 3, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 4, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("165.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "24.00", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "216", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testFicaDeferralAdjustmentCreation_OutsideDateRange() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.addCompanyLaws(company, "216");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
        companyAgency.setErFicaDeferralEnabled(true);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2020, 3, 12);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 3, 15));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("189.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 0, agencyTaxDebit.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testFicaDeferralAdjustmentCreation_Disabled() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // create and deactivate deferal law
        List<CompanyLaw> companyLaws = DataLoadServices.addCompanyLaws(company, Law.FICA_ER_DEFERRAL_CREDIT);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
        companyAgency.setErFicaDeferralEnabled(false);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2020, 3, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 4, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("189.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 0, agencyTaxDebit.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeeDeferral() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt29 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "EE FICA Deferral", "TTT29", PayrollItemCode.Compensation);
        DataLoadServices.addCompanyLaws(company, "217");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        DeductionTransactionDTO deductionTransaction = DataLoadServices.createDeductionTransaction(ttt29.getSourcePayrollItemId());
        deductionTransaction.setDeductionAmount(new BigDecimal("-5.00"));
        deductionTransaction.setDeductionYTDAmount(new BigDecimal("-5.00"));
        firstPaycheck.getDeductionTransactions().add(deductionTransaction);

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        DeductionTransactionDTO deductionTransaction2 = DataLoadServices.createDeductionTransaction(ttt29.getSourcePayrollItemId());
        deductionTransaction2.setDeductionAmount(new BigDecimal("-2.00"));
        deductionTransaction2.setDeductionYTDAmount(new BigDecimal("-2.00"));
        secondPaycheck.getDeductionTransactions().add(deductionTransaction2);

        DataLoadServices.setPSPDate(2020, 3, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 4, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("182.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "7.00", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "217", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeeDeferralAfterEndDate() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt29 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "EE FICA Deferral", "TTT29", PayrollItemCode.Compensation);
        DataLoadServices.addCompanyLaws(company, "217");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        DeductionTransactionDTO deductionTransaction = DataLoadServices.createDeductionTransaction(ttt29.getSourcePayrollItemId());
        deductionTransaction.setDeductionAmount(new BigDecimal("-5.00"));
        deductionTransaction.setDeductionYTDAmount(new BigDecimal("-5.00"));
        firstPaycheck.getDeductionTransactions().add(deductionTransaction);

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        DeductionTransactionDTO deductionTransaction2 = DataLoadServices.createDeductionTransaction(ttt29.getSourcePayrollItemId());
        deductionTransaction2.setDeductionAmount(new BigDecimal("-2.00"));
        deductionTransaction2.setDeductionYTDAmount(new BigDecimal("-2.00"));
        secondPaycheck.getDeductionTransactions().add(deductionTransaction2);

        DataLoadServices.setPSPDate(2020, 12, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2021, 1, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 0, agencyTaxDebit.size());
        PayrollServices.rollbackUnitOfWork();
    }
}
