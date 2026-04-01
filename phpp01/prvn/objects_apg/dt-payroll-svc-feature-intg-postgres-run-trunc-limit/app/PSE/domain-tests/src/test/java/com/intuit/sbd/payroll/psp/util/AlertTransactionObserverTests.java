package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Ankit on 9/23/2015.
 */
public class AlertTransactionObserverTests {

    private static final SpcfLogger logger = Application.getLogger(AlertTransactionObserverTests.class);

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
        PayrollServices.commitUnitOfWork();
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testThresholdBreachERTaxDebit() throws InterruptedException {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "234567891", "198765432", false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        //
        // Add an employee
        //

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1");
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, true);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "250000");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        PayrollRunDTO payrollRunDTO2 = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(checkDate), employeeList, lawAmounts);
        ProcessResult processResult2 = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO2);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult2);

        PayrollServices.commitUnitOfWork();

        //Thread.sleep(10000);

    }

    @Test
    public void testThresholdBreachERTaxDirectDebit() throws Exception {

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
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"70000", "15000", "400", "27"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        PayrollRun payrollRun = processResult.getResult();
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        Thread.sleep(10000);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("81"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("256200"), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone()), 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("256200"), TransactionTypeCode.EmployerTaxDirectDebit, null, 1);
        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("81"), TransactionTypeCode.EmployerTaxDebit, null, 1);

    }
}
