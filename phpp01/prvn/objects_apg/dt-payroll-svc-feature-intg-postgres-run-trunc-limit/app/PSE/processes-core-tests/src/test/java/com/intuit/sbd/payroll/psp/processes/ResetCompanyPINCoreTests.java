package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Sep 23, 2008
 * Time: 9:35:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResetCompanyPINCoreTests {
    private DataLoader dataloader = new DataLoader();
    private int mLockAccountDuration;
    private int mMaxNumberOfFailedLoginAttempts;

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

        ProcessResult processResult = PayrollServices.subscriptionManager.resetCompanyPIN(null, "123272727", null, null);

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

        ProcessResult processResult = PayrollServices.subscriptionManager.resetCompanyPIN(SourceSystemCode.QBOE, null, null, null);

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

        ProcessResult processResult = PayrollServices.subscriptionManager.resetCompanyPIN(SourceSystemCode.QBOE, "112233", "111", null);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:112233 does not exist.",
                errorMessage.getMessage());
    }


    @Test
    public void resetCompanyPIN_Success() throws Exception {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());
        
        DataLoadServices.addEntitlementUnit(result.getResult(), "123456", "654321");

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
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        QuickbooksInfo agreementInfo = new QuickbooksInfo();
        company.setQuickbooksInfo(agreementInfo);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Create the Company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Get the Company Private Key
        String message = "";
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = getMessage(company);
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }

        String oldPublicKey = company.getPublicKey();
        String oldPrivateKey = company.getPrivateKey();

        ProcessResult processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, null);
        PayrollServicesTest.assertSuccess(processResult);

        PayrollServices.commitUnitOfWork();

        // Verify the PIN has changed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyPIN> companyPINCollection = company.getCompanyPINCollection();
        assertEquals("Number Of Company PINs", 1, companyPINCollection.size());
        CompanyPIN companyPIN = companyPINCollection.iterator().next();
        assertFalse("Company PIN", PINUtils.encrypt("PIN123456", companyPIN.getHashType()).equals(companyPIN.getPINValue()));

        // Verify PINReset Event is created
        DomainEntitySet<CompanyEvent> pinResetEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINReset, null, null, null);

        assertEquals("Number of PIN Reset Events", 1, pinResetEvents.size());
        // verify the details
        CompanyEvent pinResetEvent = pinResetEvents.get(0);
        DomainEntitySet<CompanyEventDetail> eventDetails = pinResetEvent.getCompanyEventDetailCollection();
        assertEquals("Number Of Event Details", 2, eventDetails.size());

        PublicKey publicKey = PINUtils.getPublicKeyFromString(oldPublicKey);
        String decValue = PINUtils.getDecryptedValue(message, publicKey);
        //Compare
        String[] messages = decValue.split(":");
        assertEquals("Unique Identifier Detail Value ", messages[3],
                pinResetEvent.getCompanyEventDetailValue(EventDetailTypeCode.UniqueIdentifier));
        assertEquals("User Id Detail Value ", null,
                pinResetEvent.getCompanyEventDetailValue(EventDetailTypeCode.UserId));

        // Verify PINUpdated Event is created
        DomainEntitySet<CompanyEvent> pinUpdatedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINUpdated, null, null, null);

        assertEquals("Number of PIN Updated Events", 1, pinUpdatedEvents.size());

        // Verify KeyPairGenerated Event is created
        DomainEntitySet<CompanyEvent> keyPairGeneratedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.KeyPairGenerated, null, null, null);

        assertEquals("Number of KeyPairGenerated Events", 1, keyPairGeneratedEvents.size());

        assertTrue("Public Key not Created For Company", company.getPublicKey().equals(oldPublicKey));
        assertTrue("Private Key not Created For Company", company.getPrivateKey().equals(oldPrivateKey));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void resetCompanyPIN_AuthenticationFailed() throws Exception {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        DataLoadServices.addEntitlementUnit(result.getResult(), "123456", "654321");

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
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        QuickbooksInfo agreementInfo = new QuickbooksInfo();
        company.setQuickbooksInfo(agreementInfo);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Create the Company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Get the Company Private Key
        String message = "";
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = "wrongEIN:" + company.getSourceCompanyId() + ":" + SpcfUniqueId.generateRandomUniqueIdString();
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }

        String oldPublicKey = company.getPublicKey();
        String oldPrivateKey = company.getPrivateKey();

        ProcessResult processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, "QBDT USER");

        PayrollServices.commitUnitOfWork();

        // Verify the PIN has not changed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyPIN> companyPINCollection = company.getCompanyPINCollection();
        assertEquals("Number Of Company PINs", 1, companyPINCollection.size());
        CompanyPIN companyPIN = companyPINCollection.iterator().next();
        assertTrue("Company PIN", PINUtils.encrypt("PIN123456", companyPIN.getHashType()).equals(companyPIN.getPINValue()));

        // Verify PINReset Event is not created
        DomainEntitySet<CompanyEvent> pinResetEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINReset, null, null, null);

        assertEquals("Number of PIN Reset Events", 0, pinResetEvents.size());

        // Verify Authentication Failed Event
        DomainEntitySet<CompanyEvent> authenticationFailedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.AuthenticationFailed, null, null, null);

        assertEquals("Number of Authentication Failed Events", 1, authenticationFailedEvents.size());

        // verify the details
        CompanyEvent authenticationFailedEvent = authenticationFailedEvents.get(0);
        DomainEntitySet<CompanyEventDetail> eventDetails = authenticationFailedEvent.getCompanyEventDetailCollection();
        assertEquals("Number Of Event Details", 1, eventDetails.size());

        // Verify PINUpdated Event is not created
        DomainEntitySet<CompanyEvent> pinUpdatedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINUpdated, null, null, null);

        assertEquals("Number of PIN Updated Events", 0, pinUpdatedEvents.size());

        // Verify that a new KeyPairGenerated Event is not created
        DomainEntitySet<CompanyEvent> keyPairGeneratedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.KeyPairGenerated, null, null, null);

        assertEquals("Number of KeyPairGenerated Events", 1, keyPairGeneratedEvents.size());

        assertTrue("Public Key not Created For Company", company.getPublicKey().equals(oldPublicKey));
        assertTrue("Private Key not Created For Company", company.getPrivateKey().equals(oldPrivateKey));

        PayrollServices.commitUnitOfWork();

        // Second Failed Attempt
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = "wrongEIN:wrongCompanyId:" + SpcfUniqueId.generateRandomUniqueIdString();
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }
        processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, "QBDT USER");

        PayrollServices.commitUnitOfWork();

        // Third Failed Attempt - Account gets locked
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = "123465";
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }
        processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, "QBDT USER");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> accountLockedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.AccountLocked, null, null, null);

        assertEquals("Number of Account Locked Events", 1, accountLockedEvents.size());
        PayrollServices.commitUnitOfWork();

        // Fourth Attempt - Successful after lock account duration period
        PayrollServices.beginUnitOfWork();
        getSourcePayrollParameters();
        SpcfCalendar pspTime = PSPDate.getPSPTime();
        pspTime.addMinutes(mLockAccountDuration + 1);
        PSPDate.setPSPTime(pspTime);

        // Get the Company Private Key
        message = "";
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = getMessage(company);
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }

        processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, null);
        PayrollServicesTest.assertSuccess(processResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertEquals("Number Of Failed Authentications", 0, company.getNumberOfFailedAuthentications());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void resetCompanyPIN_AuthenticationFailed2() throws Exception {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        DataLoadServices.addEntitlementUnit(result.getResult(), "123456", "654321");

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
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        QuickbooksInfo agreementInfo = new QuickbooksInfo();
        company.setQuickbooksInfo(agreementInfo);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Create the Company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Get the Company Private Key
        String message = "";
        String uniqueIdentifier = SpcfUniqueId.generateRandomUniqueIdString();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = getMessage(company, uniqueIdentifier);
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }


        ProcessResult<HashMap<String, String>> processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, "QBDT USER");
        assertSuccess(processResult);

        String resetPIN = processResult.getResult().get("PIN");
        PayrollServices.commitUnitOfWork();

        // Verify the PIN has changed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyPIN> companyPINCollection = company.getCompanyPINCollection();
        assertEquals("Number Of Company PINs", 1, companyPINCollection.size());
        CompanyPIN companyPIN = companyPINCollection.iterator().next();
        assertFalse("Company PIN", PINUtils.encrypt("PIN123456", companyPIN.getHashType()).equals(companyPIN.getPINValue()));

        // Verify PINReset Event is created
        DomainEntitySet<CompanyEvent> pinResetEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINReset, null, null, null);

        assertEquals("Number of PIN Reset Events", 1, pinResetEvents.size());
        PayrollServices.commitUnitOfWork();

        // Failed Attempt with the same unique identifier
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = getMessage(company, uniqueIdentifier);
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }
        processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, "QBDT USER");

        PayrollServices.commitUnitOfWork();

        // Verify the PIN has not changed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        companyPINCollection = company.getCompanyPINCollection();
        assertEquals("Number Of Company PINs", 1, companyPINCollection.size());
        companyPIN = companyPINCollection.iterator().next();
        assertTrue("Company PIN", PINUtils.encrypt(resetPIN, companyPIN.getHashType()).equals(companyPIN.getPINValue()));

        // Verify PINReset Event is not created
        pinResetEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINReset, null, null, null);

        assertEquals("Number of PIN Reset Events", 1, pinResetEvents.size());

        // Verify Authentication Failed Event
        DomainEntitySet<CompanyEvent> authenticationFailedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.AuthenticationFailed, null, null, null);

        assertEquals("Number of Authentication Failed Events", 1, authenticationFailedEvents.size());

        // verify the details
        CompanyEvent authenticationFailedEvent = authenticationFailedEvents.get(0);
        DomainEntitySet<CompanyEventDetail> eventDetails = authenticationFailedEvent.getCompanyEventDetailCollection();
        assertEquals("Number Of Event Details", 1, eventDetails.size());
        assertEquals("Failure Reason", "PIN Reset identifier is not unique;", eventDetails.iterator().next().getValue());
        PayrollServices.commitUnitOfWork();

        // Second Failed Attempt
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(null);
            message = "123465";
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }
        processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, "QBDT USER");
        PayrollServices.commitUnitOfWork();

        // Third Failed Attempt
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(null);
            message = "123465";
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }
        processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, "QBDT USER");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        authenticationFailedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.AuthenticationFailed, null, null, null);
        assertEquals("Number of Authentication Failed Events", 3, authenticationFailedEvents.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
     public void testBase64Encoding() throws Exception {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        DataLoadServices.addEntitlementUnit(result.getResult(), "123456", "654321");

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
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        QuickbooksInfo agreementInfo = new QuickbooksInfo();
        company.setQuickbooksInfo(agreementInfo);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Create the Company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMPQukFAIpkYRdjSOk94unNeVcKApct+bgVP3hMtEccIvT8kH2X29bKUep3HUuevfxjpKzBjOFGr8Cemxihhd22VfuaZAOf36u9JOtDEQypN8LPMHqiuZuleVPb6eZak+JCrpxxHYEosikVSW4KYXLJo/0o2PgDci5VAVtfJm5gQIDAQAB");
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Get the Company Private Key
        String message = "";
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString("MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIuEnXGjZHJgBONiUc2InMmvG/gn7eYITuuReAGKRjwFSOL00avc5KWlC3ufPqINmWS+xxtCJXW3/SHDDezRRtM9x7P7c9W4lGb+97rQha2UETkDioWcgiW6aHRRfWUP3ghW96KN0r+cZOQOnLIY3rInZPvNxKBVSAAGfO6AaCnTAgMBAAECgYBqfbvLLGMUJhQ1FyvHTPoXu3AG1ztvtHHAvtUxn8CCOapriWCs5LslcDe2kVM2UFkAG9pQBQogs2IkLgTn+MksfP+e2v42Nvn0byQXSo3khGRC5rsJw5p7iY4m89EswPjyGk/hz9HDnW0W0Sfta0k8bqQVj1RX5ReLpQhn+skm4QJBAMbWMEHVsUZx83zLA1pCN31diY1ECaKFaRZR/WGlTulwFRPTKLPsxdJyXUL00ftxY4HdYMwrVIuPtVWK1TUQjhECQQCzoL5VloArHwpuuEXLtj/tW4md99Vx7aJM3Ts6vswcxNelaV59F42i/P2qvYkWmg/97JX6hYfSItucF03UwWWjAkBPDxb89kSYZ/KSaf4ZQz7g//ITUzwpfLY+A7elvZ7UvpYC3fDPKZO2i3Z9iu569ajFGArG42uoWimVq6/+e2ihAkBYKwZBrGWsxMeyIPDhVMt8Dfo2d3dlPGb1o4F3DD/Tvyq6HkYq0GzNG7DHJdEsLuK6mG3lUbWhB0sUCrxBOcxRAkEAic3nmerRSfPuZXAPNbq/v+XO3+v1o1BPTB7QPRqFcDbE7BjHrcBjmLJwDA7mBhwm+2YCbCh1xUSf1Mp0sYNrEw==");


            message = getMessage(company);
            message = "80-5805108:100000726:0004617339:EC8DD896-B75F-4474-B785-9F5C58D4EC6E";
            message = PINUtils.getEncryptedValue(message, privateKey);
            // message = "OTgtMzI3NDA5MjoxMDAwMDEwMDU6MDAwNDYxODAzODo3NTdFQzk5RC1DNzhFLTRDMTEtQkY2Mi05MUM2QzI3RThFRDE%3D =";
            assertEquals("Encrypted Value:", "X/pCHI/nVMdhjfGLXlv1x/xO8SbQ43KBeJoChbpV5cYueeeuV9RXKAdHj7Klf5857uq4RX9ZmdPVTldhE8NvYrLahCePZukmRM9p6uU2RdWQ+kJHp0XB139ZnFgD1lbUx/AEW4omsmZpq8KDIk/d/Z0j5715JvcPK0q0cEhRgn0=", message);
        }
        catch (Exception e) {

        }


    }
    private void getSourcePayrollParameters() {

        mLockAccountDuration = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.QBOE,
                SourcePayrollParameterCode.LockAccountDuration).getParameterValue());

        mMaxNumberOfFailedLoginAttempts = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.QBOE,
                SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts).getParameterValue());

    }

    @Test
    public void resetCompanyPIN_MultiPIN() throws Exception {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        DataLoadServices.addEntitlementUnit(result.getResult(), "123456", "654321");

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
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        QuickbooksInfo agreementInfo = new QuickbooksInfo();
        company.setQuickbooksInfo(agreementInfo);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult2);

        // Create the Company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        PayrollServices.commitUnitOfWork();

        //Add PINs to the Collection
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        CompanyPIN companyPIN = CompanyPIN.createCompanyPIN(company, "PIN123457");
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);

        companyPIN = CompanyPIN.createCompanyPIN(company, "PIN123458");
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);

        // Get the Company Private Key
        String message = "";
        try {
            PrivateKey privateKey = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
            message = getMessage(company);
            message = PINUtils.getEncryptedValue(message, privateKey);
        }
        catch (Exception e) {

        }

        String oldPublicKey = company.getPublicKey();
        String oldPrivateKey = company.getPrivateKey();

        ProcessResult processResult = PayrollServices.subscriptionManager.resetCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), message, null);
        PayrollServicesTest.assertSuccess(processResult);

        PayrollServices.commitUnitOfWork();

        // Verify the PIN has changed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<CompanyPIN> companyPINCollection = company.getCompanyPINCollection();
        assertEquals("Number Of Company PINs", 1, companyPINCollection.size());
        companyPIN = companyPINCollection.iterator().next();
        assertFalse("Company PIN", PINUtils.encrypt("PIN123456", companyPIN.getHashType()).equals(companyPIN.getPINValue()));

        // Verify PINReset Event is created
        DomainEntitySet<CompanyEvent> pinResetEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINReset, null, null, null);

        assertEquals("Number of PIN Reset Events", 1, pinResetEvents.size());
        // verify the details
        CompanyEvent pinResetEvent = pinResetEvents.get(0);
        DomainEntitySet<CompanyEventDetail> eventDetails = pinResetEvent.getCompanyEventDetailCollection();
        assertEquals("Number Of Event Details", 2, eventDetails.size());

        PublicKey publicKey = PINUtils.getPublicKeyFromString(oldPublicKey);
        String decValue = PINUtils.getDecryptedValue(message, publicKey);
        //Compare
        String[] messages = decValue.split(":");
        assertEquals("Unique Identifier Detail Value ", messages[3],
                pinResetEvent.getCompanyEventDetailValue(EventDetailTypeCode.UniqueIdentifier));
        assertEquals("User Id Detail Value ", null,
                pinResetEvent.getCompanyEventDetailValue(EventDetailTypeCode.UserId));

        // Verify PINUpdated Event is created
        DomainEntitySet<CompanyEvent> pinUpdatedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.PINUpdated, null, null, null);

        assertEquals("Number of PIN Updated Events", 1, pinUpdatedEvents.size());

        // Verify KeyPairGenerated Event is created
        DomainEntitySet<CompanyEvent> keyPairGeneratedEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.KeyPairGenerated, null, null, null);

        assertEquals("Number of KeyPairGenerated Events", 1, keyPairGeneratedEvents.size());

        assertTrue("Public Key not Created For Company", company.getPublicKey().equals(oldPublicKey));
        assertTrue("Private Key not Created For Company", company.getPrivateKey().equals(oldPrivateKey));

        PayrollServices.commitUnitOfWork();

    }

    private String getMessage(Company company) {
        //return company.getFedTaxId() + ":" + company.getSourceCompanyId() + ":" + company.getAgreementInfo().getSubscriptionNumber() + ":" + SpcfUniqueId.generateRandomUniqueIdString();
        EntitlementUnit eu = company.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.DIY);
        assertNotNull(eu);
        return company.getFedTaxId() + ":" + company.getSourceCompanyId() + ":" + eu.getEntitlement().getSubscriptionNumber() + ":" + SpcfUniqueId.generateRandomUniqueIdString();
    }

    private String getMessage(Company company, String identifier) {
        EntitlementUnit eu = company.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.DIY);
        assertNotNull("Company must have EU", eu);
        return company.getFedTaxId() + ":" + company.getSourceCompanyId() + ":" + eu.getEntitlement().getSubscriptionNumber() + ":" + identifier;
    }



}
