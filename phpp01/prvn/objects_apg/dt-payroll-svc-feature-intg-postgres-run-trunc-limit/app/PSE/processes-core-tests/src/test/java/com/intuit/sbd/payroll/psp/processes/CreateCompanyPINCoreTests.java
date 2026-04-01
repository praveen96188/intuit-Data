package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Apr 10, 2008
 * Time: 1:19:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateCompanyPINCoreTests {
    private DataLoader dataloader;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        dataloader = new DataLoader();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.subscriptionManager.createCompanyPIN(null, "123272727", null);

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBOE, null, null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }


    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testCompanyDoesNotExist() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBOE,
                "112233", "111");
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:112233 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test message 294 - Invalid PIN Format
     */
    @Test
    public void testInvalidPINFormat() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        Company company = assertSuccessResult(DataLoader.addCompany(company1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDDService(company);

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = assertSuccessResult(PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                                                                                         company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                                                                       companyBankAccount.getSourceBankAccountId());

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        Assert.assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        ProcessResult<CompanyBankAccount> processResult2 = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // First scenario - PIN too short
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult1 = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111");
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult1.isSuccess());
        assertEquals("Messages size", 1, processResult1.getMessages().size());
        Message errorMessage = processResult1.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());

        // Second scenario - PIN too long
        PayrollServices.beginUnitOfWork();
        processResult1 = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "123456789012A");
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult1.isSuccess());
        assertEquals("Messages size", 1, processResult1.getMessages().size());
        errorMessage = processResult1.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());

        // Third scenario - PIN does not contain at least one number
        PayrollServices.beginUnitOfWork();
        processResult1 = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "aaaaaaaaa");
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult1.isSuccess());
        assertEquals("Messages size", 1, processResult1.getMessages().size());
        errorMessage = processResult1.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());

        // Fourth scenario - PIN does not contain at least one letter
        PayrollServices.beginUnitOfWork();
        processResult1 = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "12345678");
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult1.isSuccess());
        assertEquals("Messages size", 1, processResult1.getMessages().size());
        errorMessage = processResult1.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());
    }

    /**
     * Create PIN is successful, PIN provided as a parameter
     */
    @Test
    public void createCompanyPIN_Successful() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        Company company = assertSuccessResult(DataLoader.addCompany(company1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDDService(company);

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        ProcessResult<CompanyBankAccount> processResult1 = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true);
        Assert.assertEquals(0, processResult1.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult1.getResult().getSourceBankAccountId());

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        Assert.assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        ProcessResult<CompanyBankAccount> processResult2 = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Manually set failed authentications to 4 and reset timer to 15 minutes
        PayrollServices.beginUnitOfWork();
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.addMinutes(15);
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setNumberOfFailedAuthentications(4);
        company.setAccountLockedUntil(calendar);
        PayrollServices.commitUnitOfWork();        

        PayrollServices.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111aaa12");
        company = Company.findCompany(company1.getCompanyId(),
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);

        // Verify PIN was created correctly
        CompanyPIN companyPIN = company.getCompanyPINCollection().iterator().next();
        assertEquals("PIN", PINUtils.encrypt("111aaa12", companyPIN.getHashType()), companyPIN.getPINValue());
        assertEquals("PIN Failed Count", 0, company.getNumberOfFailedAuthentications());
        assertEquals("PIN Reset Timer", null, company.getAccountLockedUntil());
        // Verify the creation of "PIN Created" event
        assertTrue("PIN Created", company.isPINCreated());

        // Verify that service status has been updated to "Pending First Payroll"
        CompanyService ddService = CompanyService.findCompanyService(company,
                ServiceCode.DirectDeposit);
        assertEquals("Service Status", ServiceSubStatusCode.PendingFirstPayroll, ddService.getStatusCd());

    }

    /**
     * Create PIN is successful, PIN is randomly generated
     */
    @Test
    public void createRandomCompanyPIN_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        Company company = assertSuccessResult(DataLoader.addCompany(company1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDDService(company);

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        ProcessResult<CompanyBankAccount> processResult1 = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true);
        Assert.assertEquals(0, processResult1.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult1.getResult().getSourceBankAccountId());

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        Assert.assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        ProcessResult<CompanyBankAccount> processResult2 = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Manually set failed authentications to 4 and reset timer to 15 minutes
        PayrollServices.beginUnitOfWork();
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.addMinutes(15);
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setNumberOfFailedAuthentications(4);
        company.setAccountLockedUntil(calendar);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), null);
        company = Company.findCompany(company1.getCompanyId(),
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        PayrollServices.beginUnitOfWork();
        String randomPIN = processResult.getResult().get("PIN");
        PayrollServices.commitUnitOfWork();

        // Verify PIN was created
        assertTrue("PIN", randomPIN != null);
        assertEquals("PIN Failed Count", 0, company.getNumberOfFailedAuthentications());
        assertEquals("PIN Reset Timer", null, company.getAccountLockedUntil());

        // Verify PIN Format
        assertTrue("PIN Format", PINUtils.validatePINFormat(randomPIN));

        // Verify the creation of "PIN Created" event
        assertTrue("PIN Created", company.isPINCreated());

        PayrollServices.beginUnitOfWork();
        // Verify that service status has been updated to "Pending First Payroll"
        CompanyService ddService = CompanyService.findCompanyService(company,
                ServiceCode.DirectDeposit);
        assertEquals("Service Status", ServiceSubStatusCode.PendingFirstPayroll, ddService.getStatusCd());
        PayrollServices.commitUnitOfWork();


    }

    @Test
    /**
     * Test message 1101
     */
    public void createPIN_CompanyTerminated() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);

        //Terminate Company
        CompanyService companyService = CompanyService.findCompanyService(company1, ServiceCode.DirectDeposit);
        companyService.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        company1 = PayrollServicesTest.save(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = dataloader.getTestIntuitCompany2();
        ProcessResult<HashMap<String,String>> result2 = PayrollServices.subscriptionManager.createCompanyPIN(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(),  null);
        PayrollServices.commitUnitOfWork();
        System.out.println(result2);

        Assert.assertEquals(1, result2.getMessages().size());
        Assert.assertEquals("1101", result2.getMessages().get(0).getMessageCode());
        Assert.assertEquals("The operation UpdatePIN is not allowed for company QBDT:123456 in its current state.",
                result2.getMessages().get(0).getMessage());
    }

    /**
     * Create PIN is successful, PIN provided as a parameter
     */
    @Test
    public void createQBDTCompanyPIN_Successful() {
        // Create Company and CompanyService
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        PSPDate.setPSPTime("20070822000000");
        CompanyDTO company1 = c3dl.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");
        Company company = dataloader.persistCompany(company1);
        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c3dl.getCompany1Service());
        PayrollServices.commitUnitOfWork();

        assertEquals("CompanyStatus", ServiceSubStatusCode.PendingBankVerification, ddCompanyService.getStatusCd());

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                company.getSourceCompanyId(), c3dl.getCompany1BankAccount(), true, true);
        Assert.assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult.getResult().getSourceBankAccountId());
        company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        Assert.assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        ProcessResult<CompanyBankAccount> processResult2 = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        PayrollServices.beginUnitOfWork();
        CompanyService ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        ddService = Application.refresh(ddService);
        PayrollServices.commitUnitOfWork();
        assertEquals("CompanyStatus", ServiceSubStatusCode.PendingPinCreation, ddService.getStatusCd());

        // Manually set failed authentications to 4 and reset timer to 15 minutes
        PayrollServices.beginUnitOfWork();
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.addMinutes(15);
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setNumberOfFailedAuthentications(4);
        company.setAccountLockedUntil(calendar);
        PayrollServices.commitUnitOfWork();

        //Create company PIN
        PayrollServices.beginUnitOfWork();

        ProcessResult result = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111aaa12");
        company = Company.findCompany(company1.getCompanyId(),
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        // Verify PIN was created correctly
        CompanyPIN companyPIN = company.getCompanyPINCollection().iterator().next();
        assertEquals("PIN", PINUtils.encrypt("111aaa12", companyPIN.getHashType()), companyPIN.getPINValue());
        // Verify the creation of "PIN Created" event
        assertTrue("PIN Created", company.isPINCreated());

        // Verify that service status has been updated to "Pending First Payroll"
        ddService = CompanyService.findCompanyService(company,
                ServiceCode.DirectDeposit);
        assertEquals("Service Status", ServiceSubStatusCode.PendingFirstPayroll, ddService.getStatusCd());
    }

    /**
     * Test message 1101 - Test case to create a Company PIN before verifying the company bank account 
     */
    @Test
    public void createQBDTCompanyPIN_OperationNotAllowed() {
        // Create Company and CompanyService
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        PSPDate.setPSPTime("20070822000000");
        CompanyDTO company1 = c3dl.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");
        Company company = dataloader.persistCompany(company1);
        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c3dl.getCompany1Service());
        PayrollServices.commitUnitOfWork();

        assertEquals("CompanyStatus", ServiceSubStatusCode.PendingBankVerification, ddCompanyService.getStatusCd());

        // Create CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                company.getSourceCompanyId(), c3dl.getCompany1BankAccount(), true, true);
       PayrollServices.commitUnitOfWork();
        assertEquals("Add CompanyBank Account",0, processResult.getMessages().size());

        //Create company PIN
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company.getSourceSystemCd().toString()),company.getSourceCompanyId(), "111aaa12");
        PayrollServices.commitUnitOfWork();

        assertTrue(result.isSuccess());
        assertEquals(0, result.getMessages().size());
    }

    /**
     * Regenerate PIN is successful when the company is in Active State
     */
    @Test
    //@Ignore
    public void regenerateQBDTCompanyPIN_Successful() {
        // Create Company and CompanyService
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        PSPDate.setPSPTime("20071031000000");
        c3dl.persistCompany3();
        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-11-15"));
        c3dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Regenerate company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(c3dl.getCompany().getSourceSystemCd().toString()),
                c3dl.getCompany().getSourceCompanyId(), "111aaa12");
        Company company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                SourceSystemCode.valueOf(c3dl.getCompany().getSourceSystemCd().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(result);
        // Verify PIN was created correctly
        CompanyPIN companyPIN = company.getCompanyPINCollection().iterator().next();
        assertEquals("PIN", PINUtils.encrypt("111aaa12", companyPIN.getHashType()), companyPIN.getPINValue());
        // Verify the creation of "PIN Created" event
        assertTrue("PIN Created", company.isPINCreated());

        // Verify that service status has been updated to "Pending First Payroll"
        CompanyService ddService = CompanyService.findCompanyService(company,
                ServiceCode.DirectDeposit);
        assertEquals("Service Status", ServiceSubStatusCode.ActiveCurrent, ddService.getStatusCd());
    }

    /**
     * Create PIN is successful, PIN provided as a parameter
     */
    @Test
    public void createCompanyPIN_WithExistingMultiPin() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        Company company = assertSuccessResult(DataLoader.addCompany(company1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDDService(company);

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        ProcessResult<CompanyBankAccount> processResult1 = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true);
        Assert.assertEquals(0, processResult1.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult1.getResult().getSourceBankAccountId());

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        Assert.assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        ProcessResult<CompanyBankAccount> processResult2 = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Manually set failed authentications to 4 and reset timer to 15 minutes
        PayrollServices.beginUnitOfWork();
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.addMinutes(15);
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setNumberOfFailedAuthentications(4);
        company.setAccountLockedUntil(calendar);
        PayrollServices.commitUnitOfWork();

        //Create existing PINS
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        CompanyPIN companyPIN = CompanyPIN.createCompanyPIN(company, "PIN123456");
        companyPIN = Application.save(companyPIN);
        company.addCompanyPIN(companyPIN);

        companyPIN = CompanyPIN.createCompanyPIN(company, "PIN123457");
        companyPIN = Application.save(companyPIN);
        company.addCompanyPIN(companyPIN);

        companyPIN = CompanyPIN.createCompanyPIN(company, "PIN123458");
        companyPIN = Application.save(companyPIN);
        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111aaa12");
        company = Company.findCompany(company1.getCompanyId(),
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);

        // Verify PIN was created correctly
        companyPIN = company.getCompanyPINCollection().iterator().next();
        assertEquals("PIN", PINUtils.encrypt("111aaa12", companyPIN.getHashType()), companyPIN.getPINValue());
        assertEquals("PIN Failed Count", 0, company.getNumberOfFailedAuthentications());
        assertEquals("PIN Reset Timer", null, company.getAccountLockedUntil());
        // Verify the creation of "PIN Created" event
        assertTrue("PIN Created", company.isPINCreated());

        // Verify that service status has been updated to "Pending First Payroll"
        CompanyService ddService = CompanyService.findCompanyService(company,
                ServiceCode.DirectDeposit);
        assertEquals("Service Status", ServiceSubStatusCode.PendingFirstPayroll, ddService.getStatusCd());

    }

}
