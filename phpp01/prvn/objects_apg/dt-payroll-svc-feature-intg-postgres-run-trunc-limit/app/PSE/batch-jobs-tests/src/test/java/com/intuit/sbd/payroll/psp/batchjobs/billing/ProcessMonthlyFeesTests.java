package com.intuit.sbd.payroll.psp.batchjobs.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.*;
import org.junit.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: kpaul
 * Date: 11/2/12
 * Time: 4:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessMonthlyFeesTests {
    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void beforeEachTest() {
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testBillMonthlyFeeHappyPath() throws Exception {
        //
        // Set the PSPDate to 6/1/2007 in preparation for company creation
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        //
        // Add an employee
        //
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();

        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        //
        // Set the PSPDate to 11/1/2012 in preparation for billing
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 1));
        PayrollServices.commitUnitOfWork();

        //
        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        //
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));

        //
        // Call the ProcessMonthlyFeesTask
        //
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod);

        task.call();

        //
        // Check to ensure Billing Detail was created
        //
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        BillingDetail billingDetail = assertOne(bdSet);
        assertEquals("Monthly processing fee for Oct 2012", billingDetail.getMemo());

        PayrollRun payrollRun = billingDetail.getPayrollRun();
        LiabilityCheck liabilityCheck = assertOne(payrollRun.getLiabilityCheckCollection());
        assertOne(liabilityCheck.getLiabilityCheckLineCollection().find(LiabilityCheckLine.QbdtTransactionInfo().Memo().equalTo(billingDetail.getMemo())));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCompanyNotOnTaxService_DoNotBill() throws Exception {
        //
        // Set the PSPDate to 6/1/2007 in preparation for company creation
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        //
        // Add an employee
        //
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();

        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        //
        // Set the PSPDate to 11/1/2012 in preparation for billing
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 1));
        PayrollServices.commitUnitOfWork();

        //
        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        //
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));

        //
        // Call the ProcessMonthlyFeesTask
        //
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod);

        task.call();

        //
        // Check to ensure Billing Detail was created
        //
        DomainEntitySet<BillingDetail> bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertTrue("Monthly billing non-process fee found", bdSet.isEmpty());
    }

    @Test
    public void testCompanyNotBALFed_DoNotBill() throws Exception {
        //
        // Set the PSPDate to 6/1/2007 in preparation for company creation
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "987654321", false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.activateCloudService(company);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        //
        // Add an employee
        //
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();

        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        //
        // Set the PSPDate to 11/1/2012 in preparation for billing
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 1));
        PayrollServices.commitUnitOfWork();

        //
        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        //
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));

        //
        // Call the ProcessMonthlyFeesTask
        //
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod);

        task.call();

        //
        // Check to ensure Billing Detail was created
        //
        DomainEntitySet<BillingDetail> bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertTrue("Monthly billing non-process fee found", bdSet.isEmpty());
    }

    @Test
    public void testCompanyDisqualifiedByTaxQuarter_DoNotBill() throws Exception {
        //
        // Set PSPDate to same tax quarter that we will attempt to bill
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        //
        // Add an employee
        //
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();

        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        //
        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        //
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));

        //
        // Call the ProcessMonthlyFeesTask
        //
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod);

        task.call();

        //
        // Check to ensure Billing Detail was created
        //
        DomainEntitySet<BillingDetail> bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertTrue("Monthly billing non-process fee found", bdSet.isEmpty());
    }

    @Test
    public void testBillingPeriodBeforePSPIsResponsibleForMonthlyBilling_DoNotBill() throws Exception {
        //
        // Set the PSPDate to 6/1/2007 in preparation for company creation
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        //
        // Add an employee
        //
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();

        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        //
        // Set the billing period to the previous month from the QBDT_MONTHLY_BILLING_START_DATE system parameter
        // (this should cause the company to not be billed on PSP)
        //
        SpcfCalendar billingPeriod = SystemParameter.findCalendarValue(SystemParameter.Code.QBDT_MONTHLY_BILLING_START_DATE);

        billingPeriod = CalendarUtils.getFirstDayOfMonth(billingPeriod);

        CalendarUtils.addBusinessDays(billingPeriod, -1);

        //
        // Call the ProcessMonthlyFeesTask
        //
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod);

        task.call();

        //
        // Check to ensure Billing Detail was created
        //
        DomainEntitySet<BillingDetail> bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertTrue("Monthly billing non-process fee found", bdSet.isEmpty());
    }

    @Test
    public void testMonthlyFeeAlreadyAssessedForBillingPeriod_DoNotRebill() throws Exception {
        //
        // Set the PSPDate to 6/1/2007 in preparation for company creation
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        //
        // Add an employee
        //
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();

        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        //
        // Set the PSPDate to 10/1/2012 to allow payroll submit to create MonthlyFee
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 10, 1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        checkDate = PSPDate.getPSPTime();

        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);

        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        //
        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        //
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));

        //
        // Check to ensure Billing Detail was created
        //
        DomainEntitySet<BillingDetail> bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertEquals("Monthly billing non-process fee not found", 1, bdSet.size());
        assertEquals("Monthly processing fee for Oct 2012", bdSet.get(0).getMemo());

        //
        // Set the PSPDate to 11/1/2012 in preparation for billing
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Call the ProcessMonthlyFeesTask
        //
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod);

        task.call();

        //
        // Check to ensure Billing Detail was created
        //
        bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertEquals("Monthly billing non-process fee not found", 1, bdSet.size());
        assertEquals("Monthly processing fee for Oct 2012", bdSet.get(0).getMemo());
    }


    @Test
    public void testMonthlyFeeAlreadyAssessedForBillingPeriodReturned_DoNotRebill() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO("2011-10-03"), employeeList);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess("Submit Payroll", processResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012, 10, 1);

        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-10-05"));

        DataLoadServices.setPSPDate(2012, 10, 3);
        DataLoadServices.runOffload();

        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));
        PayrollServices.beginUnitOfWork();
        BillingDetail monthlyFee = assertOne(BillingDetail.findBillingDetailsInBillingPeriod(company,
                                                                                                billingPeriod,
                                                                                                OfferingServiceChargeType.MonthlyFee));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 10, 6);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerFeeDebit);

        DataLoadServices.setPSPDate(2012, 10, 9);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 10, 15);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 10, 25);
        DataLoadServices.runACHTransactionProcessor(0);

        DataLoadServices.removeCompanyOnHoldReasons(company);

        DataLoadServices.setPSPDate(2012, 11, 1);
        new ProcessMonthlyFees().process(billingPeriod);


        PayrollServices.beginUnitOfWork();
        //no shell PR
        assertEquals(0, Application.find(PayrollRun.class, PayrollRun.PayrollRunType().equalTo(PayrollType.FeeOnly)).size());

        //only one is same as before
        assertEquals(monthlyFee, assertOne(BillingDetail.findBillingDetailsInBillingPeriod(company,
                                                                                           billingPeriod,
                                                                                           OfferingServiceChargeType.MonthlyFee)));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testFeesCreatedForOnHoldCompanyButLeftOnHoldUntilCompanyHoldRemoved() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.setPSPDate(2012, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        CompanyEvent.createBalanceFileReceivedEvent(company, company.getService(ServiceCode.Tax));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 8));
        assertSuccessResult(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012, 10, 4);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 10, 13);
        DataLoadServices.runACHTransactionProcessor(0);

        DataLoadServices.setPSPDate(2012, 11, 1);
        new ProcessMonthlyFees().process(SpcfCalendar.createInstance(2012, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2012, 12, 1);
        new ProcessMonthlyFees().process(SpcfCalendar.createInstance(2012, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        new ProcessMonthlyFees().process(SpcfCalendar.createInstance(2012, 11, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2012, 12, 3);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 12, 15);
        DataLoadServices.runACHTransactionProcessor(0);

        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.AchRejectR1R9);

        DataLoadServices.setPSPDate(2013, 1, 1);
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 12, 1));
        new ProcessMonthlyFees().process(billingPeriod);

        PayrollServices.beginUnitOfWork();
        BillingDetail billingDetail = assertOne(BillingDetail.findBillingDetailsInBillingPeriod(company,
                                                                                                billingPeriod,
                                                                                                OfferingServiceChargeType.MonthlyFee));
        FinancialTransaction feeTransaction = billingDetail.getFeeTransaction();
        assertEquals(PayrollStatus.Pending, feeTransaction.getPayrollRun().getPayrollRunStatus());
        assertEquals(TransactionStateCode.Created, feeTransaction.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 2);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        Application.refresh(feeTransaction);
        assertEquals(TransactionStateCode.Created, feeTransaction.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessMissedPayrolls().process("20130102");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        new ProcessMissedACHTransactions().process("20130102");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(feeTransaction);
        assertEquals(TransactionStateCode.Created, feeTransaction.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 3);
        DataLoadServices.runOffload();
        PayrollServices.beginUnitOfWork();
        new ProcessMissedPayrolls().process("20130103");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        new ProcessMissedACHTransactions().process("20130103");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(feeTransaction);
        assertEquals(PayrollStatus.Pending, feeTransaction.getPayrollRun().getPayrollRunStatus());
        assertEquals(TransactionStateCode.Created, feeTransaction.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 7);
        DataLoadServices.removeCompanyOnHoldReasons(company);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        Application.refresh(feeTransaction);
        assertEquals(PayrollStatus.OffloadedAll, feeTransaction.getPayrollRun().getPayrollRunStatus());
        assertEquals(TransactionStateCode.Executed, feeTransaction.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 16);
        DataLoadServices.runACHTransactionProcessor(0);

        PayrollServices.beginUnitOfWork();
        Application.refresh(feeTransaction);
        assertEquals(PayrollStatus.Complete, feeTransaction.getPayrollRun().getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    //Test for - PSRV003989: Monthly Fee Job will not pick up Assisted Companies that have not sent a DD paycheck
    @Test
    public void testMonthlyFeeCreatingWithPendingDD() throws Exception {
        String psid = "123456789";

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();               
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess(processResult);

        // Set the PSPDate to 1/1/2012 to allow payroll submit to create MonthlyFee
        DataLoadServices.setPSPDate(2011, 1, 1);

        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2011, 1, 1));

        // Check to ensure Billing Detail was created
        DomainEntitySet<BillingDetail> bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertEquals("Monthly billing non-process fee not found", 1, bdSet.size());
        assertEquals("Monthly processing fee for Jan 2011", bdSet.get(0).getMemo());

        // Set the PSPDate to 11/1/2012 in preparation for billing
        DataLoadServices.setPSPDate(2012, 11, 1);

        billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 11, 1));

        // Call the ProcessMonthlyFeesTask
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod);

        task.call();

        // Check to ensure Billing Detail was created
        bdSet = BillingDetail.findBillingDetailsInBillingPeriod(company, billingPeriod, OfferingServiceChargeType.MonthlyFee);

        assertEquals("Monthly billing non-process fee not found", 1, bdSet.size());
        assertEquals("Monthly processing fee for Nov 2012", bdSet.get(0).getMemo());
    }
    @Test
    public void testFiftyPercentDiscountOffer() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 12, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        String fein = "987654321";
        // Create the company (internally uses a date of 20070601)
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670404", fein, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        Application.beginUnitOfWork();
        EntitlementUnit entitlementUnit = EntitlementUnit.findActiveEntitlementUnits(fein, null, null).getFirst();
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlementUnit.getEntitlement());
        entitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
        PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                                                                      .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                                                  entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        entitlementUnitDTO = DataLoadServices.createAssistedSymponyCompanyEntitlementDTO(fein);
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        DataLoadServices.updateOffering(company, OfferingCode.SYMFY14, "SYM-FY14");

        Application.beginUnitOfWork();
        Offering offering = Offering.findByOfferingCode(OfferingCode.SYMFY14);
        SpcfMoney actualMonthlyFee = offering.getCharge(OfferingServiceChargeType.MonthlyFee, 1).getCurrentPrice().getUnitPrice();
        junit.framework.Assert.assertEquals("Monthly Fee", actualMonthlyFee.getIntegerPart(), 99);
        Application.commitUnitOfWork();
        SpcfDecimal multiplyFactor = SpcfDecimal.createInstance(2);

        DataLoadServices.activateTaxService(company);
        DataLoadServices.addDDService(company);
        DataLoadServices.activateDDService(company, true);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 12, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company.claimOfferForCompany(Offer.findOfferByOfferCode("50% off Monthly Fees for SYMFY14"));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 12, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        // Add an employee
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2015, 12, 1, SpcfTimeZone.getLocalTimeZone()));
        // Call the ProcessMonthlyFeesTask
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod).call();

        // Set the PSPDate to 11/1/2012 in preparation for billing

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 12, 7, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        int yearcount = 0;
        int monthCount = 0;
        for (int i = 1; i < 15; i++) {
            monthCount = (12 + i) % 12;
            if (monthCount == 1) {
                yearcount++;
            }
            if (monthCount == 0) {
                monthCount = 12;
            }
            Application.beginUnitOfWork();
            SpcfCalendar date = SpcfCalendar.createInstance(2015 + yearcount, monthCount, 1, SpcfTimeZone.getLocalTimeZone());
            PSPDate.setPSPTime(date);
            PayrollServices.commitUnitOfWork();
            billingPeriod = CalendarUtils.getLastDayOfMonth(date);

            // Call the ProcessMonthlyFeesTask
            new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod).call();
        }
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);
        SpcfCalendar offerEndDate = company.getCompanyOffers().getFirst().getEndDate();
        SpcfCalendar offerBeginDate = company.getCompanyOffers().getFirst().getCreatedDate();
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.MonthlyFee == osc) {
                    if (currTxn.getSettlementDate().between(offerBeginDate,offerEndDate)) {
                        junit.framework.Assert.assertEquals("MOnthly fee amount wihting 365 days", actualMonthlyFee, currTxn.getFinancialTransactionAmount().multiply(multiplyFactor));
                    } else {
                        junit.framework.Assert.assertEquals("MOnthly fee amount after 365 days", actualMonthlyFee, currTxn.getFinancialTransactionAmount());
                    }

                }
            }
        }
        PayrollServices.commitUnitOfWork();

    }
    
    @Test
    public void testTwentyPercentDiscountOffer() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 12, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        String fein = "987654321";
        // Create the company (internally uses a date of 20070601)
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670404", fein, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        Application.beginUnitOfWork();
        EntitlementUnit entitlementUnit = EntitlementUnit.findActiveEntitlementUnits(fein, null, null).getFirst();
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlementUnit.getEntitlement());
        entitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
        PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                                                                      .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                                                  entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        entitlementUnitDTO = DataLoadServices.createAssistedSymponyCompanyEntitlementDTO(fein);
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        DataLoadServices.updateOffering(company, OfferingCode.SYMFY14, "SYM-FY14");

        Application.beginUnitOfWork();
        Offering offering = Offering.findByOfferingCode(OfferingCode.SYMFY14);
        SpcfMoney actualMonthlyFee = offering.getCharge(OfferingServiceChargeType.MonthlyFee, 1).getCurrentPrice().getUnitPrice();
        junit.framework.Assert.assertEquals("Monthly Fee", actualMonthlyFee.getIntegerPart(), 99);
        Application.commitUnitOfWork();
        SpcfDecimal multiplyFactor = SpcfDecimal.createInstance(5);
        SpcfDecimal divideFactor = SpcfDecimal.createInstance(4);

        DataLoadServices.activateTaxService(company);
        DataLoadServices.addDDService(company);
        DataLoadServices.activateDDService(company, true);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 12, 16, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company.claimOfferForCompany(Offer.findOfferByOfferCode("Twenty percent off Monthly Fees"));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 12, 19, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        // Add an employee
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2016, 12, 1, SpcfTimeZone.getLocalTimeZone()));
        // Call the ProcessMonthlyFeesTask
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod).call();

        // Set the PSPDate to 11/1/2012 in preparation for billing

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 12, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        int yearcount = 0;
        int monthCount = 0;
        for (int i = 1; i < 15; i++) {
            monthCount = (12 + i) % 12;
            if (monthCount == 1) {
                yearcount++;
            }
            if (monthCount == 0) {
                monthCount = 12;
            }
            Application.beginUnitOfWork();
            SpcfCalendar date = SpcfCalendar.createInstance(2016 + yearcount, monthCount, 1, SpcfTimeZone.getLocalTimeZone());
            PSPDate.setPSPTime(date);
            PayrollServices.commitUnitOfWork();
            billingPeriod = CalendarUtils.getLastDayOfMonth(date);

            // Call the ProcessMonthlyFeesTask
            new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod).call();
        }
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);
        SpcfCalendar offerEndDate = company.getCompanyOffers().getFirst().getEndDate();
        SpcfCalendar offerBeginDate = company.getCompanyOffers().getFirst().getCreatedDate();
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.MonthlyFee == osc) {
                    if (currTxn.getSettlementDate().between(offerBeginDate,offerEndDate)) {
                        junit.framework.Assert.assertEquals("Monthly fee inside offer period", actualMonthlyFee, currTxn.getFinancialTransactionAmount().multiply(multiplyFactor).divide(divideFactor));
                    } else {
                        junit.framework.Assert.assertEquals("Monthly fee after offer period", actualMonthlyFee, currTxn.getFinancialTransactionAmount());
                    }

                }
            }
        }
        PayrollServices.commitUnitOfWork();

    }

}
