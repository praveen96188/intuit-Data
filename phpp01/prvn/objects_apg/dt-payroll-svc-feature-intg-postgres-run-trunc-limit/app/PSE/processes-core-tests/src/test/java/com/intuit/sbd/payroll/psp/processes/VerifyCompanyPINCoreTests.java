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
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Mar 27, 2008
 * Time: 9:35:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class VerifyCompanyPINCoreTests {
    private DataLoader dataloader = new DataLoader();
    private int mLockAccountDuration;
    private int mMaxNumberOfFailedLoginAttempts;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        getSourcePayrollParameters();
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

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(null, "123272727", null);

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

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(SourceSystemCode.QBOE, null, null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 11 - Invalid Argument
     */
    @Test
    public void testNullPIN() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(SourceSystemCode.QBOE, "123272727", null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: PIN",
                errorMessage.getMessage());
    }

    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testCompanyDoesNotExist() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(SourceSystemCode.QBOE, "112233", "111");
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:112233 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test message 292 - PIN not Recognized
     */
    @Test
    public void testInvalidPIN() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load company", 0, result.getMessages().size());

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
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        PayrollServices.commitUnitOfWork();
        assertEquals("Create Company PIN", 0, processCreatePINResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111");

        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        int numberOfIncorrectPINEvents = CompanyEvent.getEventCountByType(company, EventTypeCode.IncorrectPIN);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message", "PIN not recognized. Please enter the correct PIN and try again. " +
                "Account will lock for " + mLockAccountDuration + " minutes after " + mMaxNumberOfFailedLoginAttempts
                + " failed attempts.", errorMessage.getMessage());
        assertEquals("Error message code", "292", errorMessage.getMessageCode());
        assertEquals("Number of IncorrectPINEvents", 1, numberOfIncorrectPINEvents);
        assertEquals("Number Of Failed Login Attempts", 1, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
    }

    /**
     * Test message 293 - PIN not recognized. Account is now locked.
     */
    @Test
    public void VerifyCompanyPIN_AccountLocked() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company Process", 0, result.getMessages().size());

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
        assertEquals("Create Company PIN Process", 0, processCreatePINResult.getMessages().size());


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), SourceSystemCode.QBOE);
        SourcePayrollParameter sourceParameter =
                SourcePayrollParameter.findSourcePayrollParameter(company.getSourceSystemCd(),
                        SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts);

        int maxNumberOfAttempts = Integer.parseInt(sourceParameter.getParameterValue());
        PayrollServices.commitUnitOfWork();

        ProcessResult processResult = new ProcessResult();
        PayrollServices.beginUnitOfWork();
        for (int i = 0; i < maxNumberOfAttempts; i++) {


            processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                    SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                    company1.getCompanyId(), "111");

            company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

            sourceParameter =
                    SourcePayrollParameter.findSourcePayrollParameter(company.getSourceSystemCd(),
                            SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts);


        }
        PayrollServices.commitUnitOfWork();
        int numberOfAccountLockedEvents = CompanyEvent.getEventCountByType(company, EventTypeCode.AccountLocked);

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "293", errorMessage.getMessageCode());
        assertEquals("Error message", "PIN not recognized. Account is now locked. Please try again in " +
                mLockAccountDuration + " minutes.", errorMessage.getMessage());
        assertEquals("Account Locked Event", 1, numberOfAccountLockedEvents);
        assertEquals("Number Of Failed Login Attempts", maxNumberOfAttempts, company.getNumberOfFailedLoginAttempts());
        assertNotNull(company.getAccountLockedUntil());
    }

    @Test
    public void VerifyCompanyPIN_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

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

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PIN123456");

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
    }

    @Test
    public void VerifyCompanyPIN_MigrateToLatestHashType() {

        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

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

        // Manually change the PIN to the old format (SHA).
        PayrollServices.beginUnitOfWork();
        CompanyPIN pin = Application.find(CompanyPIN.class, CompanyPIN.Company().equalTo(company)).getFirst();
        pin.setPINValue(PINUtils.encrypt("PIN123456", HashType.SHA));
        pin.setHashType(HashType.SHA);
        PayrollServices.commitUnitOfWork();

        // Verification should migrate to the latest hash type.
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PIN123456");
        company = (Company) processResult.getResult();
        pin = Application.find(CompanyPIN.class, CompanyPIN.Company().equalTo(company)).getFirst();
        Assert.assertEquals("Hash Type", PINUtils.CURRENT_HASH_TYPE, pin.getHashType());
        PayrollServices.commitUnitOfWork();

        // Verify again to make sure we can validate with the latest algorithm.
        PayrollServices.beginUnitOfWork();
        PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PIN123456");
        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
    }

    @Test
    public void VerifyCompanyPIN_CreateKeyPair_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

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
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setPrivateKey(null);
        company.setPublicKey(null);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PIN123456");

        company = (Company) processResult.getResult();
        PayrollServicesTest.assertSuccess(processResult);
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
        PayrollServices.commitUnitOfWork();

        // Verify that the key pair was generated for the company
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertTrue("Company Private Key Generated", company.getPrivateKey() != null);
        assertTrue("Company Public Key Generated", company.getPublicKey() != null);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void VerifyCompanyPIN_CaseInsensitive_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

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

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "Pin123456");

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
    }

    @Test
    public void VerifyCompanyRandomPIN_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

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
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), processCreatePINResult.getResult().get("PIN"));

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
    }

    @Test
    public void VerifyCompanyPIN_Success2() {
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

        // Create the Company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> processCreatePINResult = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), "PIN123456");
        PayrollServices.commitUnitOfWork();

        // Try to verify an incorrect PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111");
        Company resultCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());
        assertEquals("Number Of Failed Login Attempts", 1, resultCompany.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, resultCompany.getAccountLockedUntil());

        DataLoadServices.updateSourcePayrollParameter(SourceSystemCode.QBOE, SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts, "1");
        DataLoadServices.updateSourcePayrollParameter(SourceSystemCode.QBOE, SourcePayrollParameterCode.LockAccountDuration, "10");                

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), SourceSystemCode.QBOE);

        processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111");

        resultCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());
        assertEquals("Number Of Failed Login Attempts", 2, resultCompany.getNumberOfFailedLoginAttempts());
        assertNotNull(resultCompany.getAccountLockedUntil());

        PayrollServices.beginUnitOfWork();
        SpcfCalendar pspTime = PSPDate.getPSPTime();
        pspTime.addMinutes(mLockAccountDuration);
        PSPDate.setPSPTime(pspTime);

        processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PIN123456");
        resultCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateSourcePayrollParameter(SourceSystemCode.QBOE, SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts, "3");
        DataLoadServices.updateSourcePayrollParameter(SourceSystemCode.QBOE, SourcePayrollParameterCode.LockAccountDuration, "15");
        
        PayrollServicesTest.assertSuccess(processResult);
        assertEquals("Number Of Failed Login Attempts", 0, resultCompany.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, resultCompany.getAccountLockedUntil());
    }

    @Test
    public void verifyCompanyMultiplePINAS400_Success() {
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
        CompanyPIN companyPIN = new CompanyPIN();
        companyPIN.setPINValue(PINUtils.encrypt("SecondPIN", PINUtils.CURRENT_HASH_TYPE));
        companyPIN.setHashType(PINUtils.CURRENT_HASH_TYPE);
        companyPIN.setCompany(company);
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        assertEquals("Load Company", 0, result.getMessages().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertEquals("Number of Company PINS", 2, company.getCompanyPINCollection().size());
        // Add a third PIN to the company
        companyPIN = new CompanyPIN();
        companyPIN.setPINValue(PINUtils.encrypt("ThirdPIN", PINUtils.CURRENT_HASH_TYPE));
        companyPIN.setHashType(PINUtils.CURRENT_HASH_TYPE);
        companyPIN.setCompany(company);
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        // Verify first PIN
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertEquals("Number of Company PINS", 3, company.getCompanyPINCollection().size());

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PIN123456");

        PayrollServicesTest.assertSuccess(processResult);

        company = (Company) processResult.getResult();

        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
        PayrollServices.commitUnitOfWork();

        // Verify Second PIN
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "SecondPIN");

        PayrollServicesTest.assertSuccess(processResult);
        company = (Company) processResult.getResult();
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());

        PayrollServices.commitUnitOfWork();

        // Verify Third PIN
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "ThirdPIN");

        PayrollServicesTest.assertSuccess(processResult);
        company = (Company) processResult.getResult();
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void VerifyCompanyPINAS400_Success() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = DataLoader.addCompany(company1);
        assertSuccess(result);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

        // Create the Company PIN Using AS400 encryption  - since there is no process available for this
        // we just set the PIN directly for testing purposes

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        String pinAS400 = PINUtils.encrypt("+@TEST123", HashType.AS400);
        CompanyPIN companyPIN = new CompanyPIN();
        companyPIN.setPINValue(pinAS400);
        companyPIN.setHashType(HashType.AS400);
        companyPIN.setCompany(company);
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(company1.getSourceSystemCd(),
                company1.getCompanyId(), "+@TesT123");

        company = (Company) processResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        companyPIN = company.getCompanyPINCollection().iterator().next();
        assertEquals("New PIN", PINUtils.encrypt("+@TEST123", companyPIN.getHashType()), companyPIN.getPINValue());
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
    }


    @Test
    public void verifyCompanyMultipleAS400PIN_Success() {
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
        assertSuccess("verifyCompanyBankAccount", processResult2);
        PayrollServices.commitUnitOfWork();

        // Create the First AS400 Company PIN
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        String pinAS400 = PINUtils.encrypt("+@TEST123", HashType.AS400);
        CompanyPIN companyPIN = new CompanyPIN();
        companyPIN.setPINValue(pinAS400);
        companyPIN.setHashType(HashType.AS400);
        companyPIN.setCompany(company);
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        // Add one more PIN to the Company
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        companyPIN = new CompanyPIN();
        companyPIN.setPINValue(PINUtils.encrypt("SecondPIN", HashType.AS400));
        companyPIN.setHashType(HashType.AS400);
        companyPIN.setCompany(company);
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertEquals("Number of Company PINS", 2, company.getCompanyPINCollection().size());
        // Add a third PIN to the company
        companyPIN = new CompanyPIN();
        companyPIN.setPINValue(PINUtils.encrypt("ThirdPIN", HashType.AS400));
        companyPIN.setHashType(HashType.AS400);
        companyPIN.setCompany(company);
        companyPIN = Application.save(companyPIN);

        company.addCompanyPIN(companyPIN);
        company = Application.save(company);
        PayrollServices.commitUnitOfWork();

        // Verify PINs in random order
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        assertEquals("Number of Company PINS", 3, company.getCompanyPINCollection().size());

        ProcessResult processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "SecondPin");

        PayrollServicesTest.assertSuccess(processResult);

        company = (Company) processResult.getResult();

        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
        PayrollServices.commitUnitOfWork();

        // Verify Second PIN
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "+@TEST123");

        PayrollServicesTest.assertSuccess(processResult);
        company = (Company) processResult.getResult();
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());

        PayrollServices.commitUnitOfWork();

        // Verify Third PIN
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "ThirdPIN");

        PayrollServicesTest.assertSuccess(processResult);
        company = (Company) processResult.getResult();
        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());

        PayrollServices.commitUnitOfWork();


    }

    private void getSourcePayrollParameters() {

        mLockAccountDuration = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.QBOE,
                SourcePayrollParameterCode.LockAccountDuration).getParameterValue());

        mMaxNumberOfFailedLoginAttempts = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.QBOE,
                SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts).getParameterValue());

    }


}



