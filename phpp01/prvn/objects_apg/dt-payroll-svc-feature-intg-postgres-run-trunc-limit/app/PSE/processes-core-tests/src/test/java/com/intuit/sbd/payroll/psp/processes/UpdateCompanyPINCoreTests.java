package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Apr 10, 2008
 * Time: 2:30:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateCompanyPINCoreTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
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

        ProcessResult processResult = PayrollServices.subscriptionManager.updateCompanyPIN(null, "123272727", null);

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

        ProcessResult processResult = PayrollServices.subscriptionManager.updateCompanyPIN(SourceSystemCode.QBOE, null, null);

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

        ProcessResult processResult = PayrollServices.subscriptionManager.updateCompanyPIN(SourceSystemCode.QBOE, "112233", "111");
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
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
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

        // First scenario - PIN too short
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.updateCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111");

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());
        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());

        // Second scenario - PIN too long
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.subscriptionManager.updateCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "123456789012A");

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());
        assertEquals("Messages size", 1, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());

        // Third scenario - PIN does not contain at least one number
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.subscriptionManager.updateCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "aaaaaaaaa");

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());
        assertEquals("Messages size", 1, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());

        // Fourth scenario - PIN does not contain at least one letter
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.subscriptionManager.updateCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "12345678");

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());
        assertEquals("Messages size", 1, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "294", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid PIN format.", errorMessage.getMessage());
    }

    /**
     * Update PIN is successful, PIN provided as a parameter
     */
    @Test
    public void updateCompanyPIN_Successful() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();

        ProcessResult<Company> result = DataLoader.addCompany(company1);
        Company company = result.getResult();
        PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

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

        // Create a PIN for the company
        PayrollServices.beginUnitOfWork();
        PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PINCREATED01");
        PayrollServices.commitUnitOfWork();
        assertEquals("Create Company PIN", 0, result.getMessages().size());

        // Manually set failed authentications to 4 and reset timer to 15 minutes
        PayrollServices.beginUnitOfWork();
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.addMinutes(15);
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setNumberOfFailedAuthentications(4);
        company.setAccountLockedUntil(calendar);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult<HashMap<String, String>> processResult = PayrollServices.subscriptionManager.updateCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "updated01");
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(),
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()));

        // Verify PIN was updated correctlyCompanyPIN companyPIN = company.getCompanyPINCollection().iterator().next();
        CompanyPIN companyPIN = company.getCompanyPINCollection().iterator().next();
        assertEquals("PIN", PINUtils.encrypt("updated01", companyPIN.getHashType()), companyPIN.getPINValue());
        assertEquals("PIN Failed Count", 0, company.getNumberOfFailedAuthentications());
        assertEquals("PIN Reset Timer", null, company.getAccountLockedUntil());

        // Verify the creation of one "PIN Updated" event
        DomainEntitySet<CompanyEvent> pinUpdatedEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PINUpdated, CompanyEventStatus.Active, null, null);
        assertTrue("PIN Updated", pinUpdatedEvents.size() == 1);
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Update PIN is successful, PIN is randomly generated
     */
    @Test
    public void updateRandomCompanyPIN_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        Company company = result.getResult();
        PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

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

        // Create a PIN for the company
        PayrollServices.beginUnitOfWork();
        PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PINCREATED01");
        PayrollServices.commitUnitOfWork();
        assertEquals("PIN Created", 0, result.getMessages().size());

        // Manually set failed authentications to 4 and reset timer to 15 minutes
        PayrollServices.beginUnitOfWork();
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.addMinutes(15);
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setNumberOfFailedAuthentications(4);
        company.setAccountLockedUntil(calendar);
        PayrollServices.commitUnitOfWork();
        

        PayrollServices.beginUnitOfWork();

        ProcessResult<HashMap<String, String>> processResult = PayrollServices.subscriptionManager.updateCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), null);
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(),
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()));
        String randomPIN = (String) processResult.getResult().get("PIN");

        // Verify PIN was updated
        CompanyPIN companyPIN = company.getCompanyPINCollection().iterator().next();
        assertNotNull("PIN Not Null", companyPIN);
        assertTrue("PIN", companyPIN.getPINValue().equals(PINUtils.encrypt(randomPIN, companyPIN.getHashType())));
        assertEquals("PIN Failed Count", 0, company.getNumberOfFailedAuthentications());
        assertEquals("PIN Reset Timer", null, company.getAccountLockedUntil());
        
        // Verify the creation of one "PIN Updated" event
        DomainEntitySet<CompanyEvent> pinUpdatedEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PINUpdated, CompanyEventStatus.Active, null, null);
        assertTrue("PIN Updated", pinUpdatedEvents.size() == 1);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void updateCompanyMultiplePIN_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());

        assertEquals("Load Company", 0, result.getMessages().size());
        PayrollServices.commitUnitOfWork();

        // Create and verify CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

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

        // Create the Company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        PayrollServices.commitUnitOfWork();

        // Add one more PIN to the Company
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        CompanyPIN companyPIN = CompanyPIN.createCompanyPIN(company, "SecondPIN");
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        assertEquals("Load Company", 0, result.getMessages().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertEquals("Number of Company PINS", 2, company.getCompanyPINCollection().size());

        // Add a third PIN to the company
        companyPIN = CompanyPIN.createCompanyPIN(company, "ThirdPIN");
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        // Manually set failed authentications to 4 and reset timer to 15 minutes
        PayrollServices.beginUnitOfWork();
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.addMinutes(15);
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setNumberOfFailedAuthentications(4);
        company.setAccountLockedUntil(calendar);
        PayrollServices.commitUnitOfWork();


        // Update PIN
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertEquals("Number of Company PINS", 3, company.getCompanyPINCollection().size());

        ProcessResult<HashMap<String, String>> processResulUpdate = PayrollServices.subscriptionManager.updateCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "updated01");
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResulUpdate);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(),
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()));
        // Verify that there is only one PIN associated with the company
        assertEquals("Number of Company PINS", 1, company.getCompanyPINCollection().size());
        // Verify PIN was updated correctly
        companyPIN = company.getCompanyPINCollection().iterator().next();
        assertEquals("PIN", PINUtils.encrypt("updated01", companyPIN.getHashType()), companyPIN.getPINValue());
        assertEquals("PIN Failed Count", 0, company.getNumberOfFailedAuthentications());
        assertEquals("PIN Reset Timer", null, company.getAccountLockedUntil());
        
        // Verify the creation of one "PIN Updated" event
        DomainEntitySet<CompanyEvent> pinUpdatedEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PINUpdated, CompanyEventStatus.Active, null, null);
        assertTrue("PIN Updated", pinUpdatedEvents.size() == 1);
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
        ProcessResult<HashMap<String, String>> result2 = PayrollServices.subscriptionManager.updateCompanyPIN(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), null);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(1, result2.getMessages().size());
        Assert.assertEquals("1101", result2.getMessages().get(0).getMessageCode());
        Assert.assertEquals("The operation UpdatePIN is not allowed for company QBOE:123456 in its current state.",
                            result2.getMessages().get(0).getMessage());
    }

    @Test
    public void updatePIN_CompanyAMLHOld() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);

       company1.addOnHoldReason(ServiceSubStatusCode.AMLHold);
        PayrollServicesTest.save(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> updatePinPR = PayrollServices.subscriptionManager.updateCompanyPIN(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), "efghEFGH5678");
        PayrollServices.commitUnitOfWork();

        assertSuccess(updatePinPR);
    }

}
