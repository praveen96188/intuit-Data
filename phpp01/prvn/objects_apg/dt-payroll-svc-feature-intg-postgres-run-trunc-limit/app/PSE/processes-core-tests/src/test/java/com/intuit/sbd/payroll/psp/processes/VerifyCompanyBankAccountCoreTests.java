package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: mvillani
 * Date: Oct 11, 2007
 * Time: 3:26:12 PM
 */
public class VerifyCompanyBankAccountCoreTests {

    private Company company;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Successfully verify a company bank account
     */
    @Test
    public void verifyCompanyBankAccountSuccessful() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Set Verification Transactions State to "EXECUTED"  and get the correct amounts to be verified

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            // Add the FinancialTransactionState object for the current State
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
            PayrollServicesTest.save(financialTransaction);
        }
        PayrollServices.commitUnitOfWork();
        System.out.println("No of amounts to verify:" + amountsToVerify.size());
        assertEquals("Number of verify txns", 2, amountsToVerify.size());
        PayrollServices.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        out.println(processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        CompanyService ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        ServiceSubStatusCode serviceSubStatusCd = ddService.getStatusCd();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.BankAccountVerified, CompanyEventStatus.Active, null, null);
        CompanyEvent baVerifiedEvent = null;

        if (companyEvents.size() == 1) {
            baVerifiedEvent = companyEvents.get(0);
        }
        PayrollServices.commitUnitOfWork();

        assertEquals("Service status", ServiceSubStatusCode.PendingFirstPayroll, serviceSubStatusCd);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        //Retrieve the company bank account
        companyBankAccount = processResult.getResult();

        // Verify that status = Active
        assertEquals("Company Bank Account Status:", BankAccountStatus.Active, companyBankAccount.getStatusCd());

        // Verify that retry count = 0
        assertEquals("Company Bank Account Verify Retry Count:", 0, companyBankAccount.getVerifyRetryCount());

        // Verify that Last Retry Date
        assertNotNull("Company Bank Account Last Retry Date:", companyBankAccount.getLastRetryDate());
        assertEquals("Num company events", 1, companyEvents.size());
        //assertEquals("Company bank account on event", companyBankAccount, baVerifiedEvent.getCompanyBankAccount());
        assertEquals("Company on event", company, baVerifiedEvent.getCompany());
    }

    @Test
    public void verifyCompanyBankAccountDeactivateReactivate() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Set Verification Transactions State to "EXECUTED"  and get the correct amounts to be verified

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            // Add the FinancialTransactionState object for the current State
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
            PayrollServicesTest.save(financialTransaction);
        }
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of verify txns", 2, amountsToVerify.size());
        PayrollServices.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(5);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult<CompanyBankAccount> deactivateProcessResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        assertEquals("Deactivate process success", true, deactivateProcessResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> reactivateProcessResult = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount), true, true);
        assertEquals("Reactivate process success", true, reactivateProcessResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        ArrayList<SpcfMoney> amountsToVerify2 = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions2 = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction finTxn : verificationTransactions2) {
            // Add the FinancialTransactionState object for the current State
            TransactionState currentTransactionState = PayrollServices.entityFinder.findById(TransactionState.class, TransactionStateCode.Executed);
            finTxn.addTransactionState(currentTransactionState);
            amountsToVerify2.add(finTxn.getFinancialTransactionAmount());
            PayrollServicesTest.save(finTxn);
        }
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of verify txns", 2, amountsToVerify2.size());
        PayrollServices.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(5);
        ProcessResult<CompanyBankAccount> verifyProcessResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify2.get(0), amountsToVerify2.get(1), false);
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", verifyProcessResult.getMessages().size() == 0);

        //Retrieve the company bank account
        companyBankAccount = processResult.getResult();

        // Verify that status = Active
        assertEquals("Company Bank Account Status:", BankAccountStatus.Active, companyBankAccount.getStatusCd());

        // Verify that retry count = 0
        assertEquals("Company Bank Account Verify Retry Count:", 0, companyBankAccount.getVerifyRetryCount());

        // Verify that Last Retry Date
        assertNotNull("Company Bank Account Last Retry Date:", companyBankAccount.getLastRetryDate());
    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void verifyCompanyBankAccountCompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);

        // Set invalid company
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), "InvalidCompanyId", companyBankAccount.getSourceBankAccountId(), null, null, false);
        out.println(processResult);
        // Commit
        PayrollServices.commitUnitOfWork();

        // Verify error count
        assertEquals("Number of Errors: ", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());


    }


    @Test
    /**
     *  Test error message 170 - BankAccount for company does not exist
     */

    public void verifyCompanyBankAccountBankAccountDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        // Call Process with a non existent company bank account
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), "InvalidSourceBankAccountId", null, null, false);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "170", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account InvalidSourceBankAccountId for company QBOE:123272727 does not exist.", message.getMessage());


    }

    @Test
    /**
     *  Test error message 250 - Company Bank Account already verified
     */

    public void verifyCompanyBankAccountAlreadyVerified() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Set Company Bank Account status to Active
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        companyBankAccount.setStatusCd(BankAccountStatus.Active);
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), null, null, false);
        out.println(processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "250", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 has already been verified.", message.getMessage());
    }

    @Test
    /**
     *  Reverify should work for QBDT until PIN is not created.
     */
    public void verifyCompanyBankAccountAlreadyVerifiedQBDT() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        DataLoader dataloader = new DataLoader();
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = c3dl.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");
        company = dataloader.persistCompany(company1);

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c3dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), c3dl.getCompany1BankAccount(), true, true);
        Assert.assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult.getResult().getSourceBankAccountId());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not be on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
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
        ProcessResult<CompanyBankAccount> reverifyProcessResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("reverify company bank account", reverifyProcessResult);
    }

    @Test
    /**
     *  Reverify should work for QBDT, but not if they pass in the wrong amounts
     */
    public void verifyCompanyBankAccountAlreadyVerifiedQBDTWrongAmounts() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        DataLoader dataloader = new DataLoader();
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = c3dl.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");
        company = dataloader.persistCompany(company1);

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c3dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), c3dl.getCompany1BankAccount(), true, true);
        Assert.assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult.getResult().getSourceBankAccountId());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not be on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
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
        ProcessResult<CompanyBankAccount> reverifyProcessResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), new SpcfMoney(".00"), new SpcfMoney(".00"), false);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(
                company, EventTypeCode.BankAccountVerified, CompanyEventStatus.Active, null, null);
        int numCompanyEvents = companyEvents.size();
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", reverifyProcessResult.getMessages().size() > 0);

        // validate error code
        Message message = reverifyProcessResult.getMessages().get(0);
        assertEquals("Error Code:", "190", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account C1BA1 for company QBDT:8574536 failed verification - verification transaction amounts do not match.", message.getMessage());

        // Verify that verifyRetryCount is 1
        assertEquals("Verify Retry Count: ", 1, companyBankAccount.getVerifyRetryCount());

        //Verify that there is only one verified event (the one from the first verification attempt)
        assertEquals("Only 1 verified event exists", 1, numCompanyEvents);
    }

    @Test
    /**
     *  Reverify should not work for QBDT since PIN is created.
     */
    public void verifyCompanyBankAccountAlreadyPINCreatedQBDT() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        DataLoader dataloader = new DataLoader();
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = c3dl.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");
        company = dataloader.persistCompany(company1);

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c3dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), c3dl.getCompany1BankAccount(), true, true);
        Assert.assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                processResult.getResult().getSourceBankAccountId());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not be on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
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

        //Create company PIN
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.subscriptionManager.createCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111aaa12");
        PayrollServices.commitUnitOfWork();
        assertSuccess("PIN Creation", result);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> reverifyProcessResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", reverifyProcessResult.getMessages().size() > 0);

        // validate error code
        Message message = reverifyProcessResult.getMessages().get(0);
        assertEquals("Error Code:", "250", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account C1BA1 for company QBDT:8574536 has already been verified.", message.getMessage());

    }

    @Test
    /**
     *  Test error message 188 - Company Bank Account not Pending Verification - Current Status = Inactive
     */

    public void verifyCompanyBankAccountNotPendingVerification_Inactive() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Set Company Bank Account status to Inactive
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        ProcessResult<CompanyBankAccount> deactivateProcess =
                PayrollServices.companyManager.deactivateCompanyBankAccount(sourceSystemCd, sourceCompanyId,
                        companyBankAccountDTO.getCompanyBankAccountID(), false, false);
        assertSuccess(deactivateProcess);
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), null, null, false);
        out.println(processResult);
        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "188", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 currently has a status of Inactive and cannot be verified.", message.getMessage());
    }

    @Test
    /**
     *  Test error message 189 - Verify Retry Count Over Limit
     */

    public void verifyCompanyBankAccountVerifyRetryCountOverLimit() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Set Company Bank Account VerifyRetryCount over limit
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        int verificationAttemptLimit = Integer.parseInt(LimitRule.findLimitRule(company, ServiceCode.DirectDeposit)
                                                                 .findLimitValueByName(LimitValueType.CompanyBankAccountVerificationAttemptLimit).getValue());
        companyBankAccount.setVerifyRetryCount(verificationAttemptLimit);
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), null, null, false);
        out.println(processResult);
        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "189", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 has previously failed verification " + Integer.toString(verificationAttemptLimit) + " times and cannot be verified.", message.getMessage());
    }

    /**
     * Test error message 197 - No Recent Verification Transactions
     */
    @Test
    public void verifyCompanyBankAccountNoRecentVerificationTransactions() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();

        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of transactions to verify ", 2, amountsToVerify.size());
        PayrollServices.beginUnitOfWork();

        company = Application.refresh(company);
        // Set PSPDate in the future so there will be no recent verification transactions
        int verificationLimitDays = Integer.parseInt(LimitRule.findLimitRule(company, ServiceCode.DirectDeposit)
                                                                 .findLimitValueByName(LimitValueType.CompanyBankAccountDurationLimitForVerification).getValue());
        PSPDate.addDaysToPSPTime(verificationLimitDays + 10);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        out.println(processResult);

        // Reset PSPDate
        PSPDate.resetPSPTime();

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() > 0);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "197", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 has no recent verification transactions and cannot be verified.", message.getMessage());
    }

    /**
     * Test error message 205 -  Verification Transactions not Issued yet
     */
    @Test
    public void verifyCompanyBankAccountVerificationTransactionsNotIssuedYet() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();

        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of verify txns", 2, amountsToVerify.size());
        PayrollServices.beginUnitOfWork();

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        out.println(processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() > 0);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "205", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 cannot be verified - verification transactions have not been issued to the bank account yet.", message.getMessage());
    }

    /**
     * Test error message 208 -  Verification Transactions have been Canceled
     */
    @Test
    public void verifyCompanyBankAccountVerificationTransactionsCanceled() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Set Verification Transactions State to "CANCELED"  and get the correct amounts to be verified
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            // Add the FinancialTransactionState object for the current State
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Cancelled);
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of verify txns", 2, amountsToVerify.size());
        PayrollServices.beginUnitOfWork();

        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        out.println(processResult);

        // Reset PSPDate
        PSPDate.resetPSPTime();

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() > 0);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "208", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "BankAccount 123123 for company QBOE:123272727 cannot be verified - verification transactions were cancelled.", message.getMessage());
    }

    /**
     * Test error message 204 -  Verification Transactions have been Returned
     */
    @Test
    public void verifyCompanyBankAccountVerificationTransactionsReturned() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Set Verification Transactions State to "CANCELED"  and get the correct amounts to be verified
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            // Add the FinancialTransactionState object for the current State
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Returned);
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of verify txns", 2, amountsToVerify.size());
        PayrollServices.beginUnitOfWork();

        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        out.println(processResult);

        // Reset PSPDate
        PSPDate.resetPSPTime();

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() > 0);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "204", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 cannot be verified - verification transactions were returned.", message.getMessage());
    }

    /**
     * Test error message 190 -  Verification Transaction Amounts do not match
     */
    @Test
    public void verifyCompanyBankAccountAmountsFailedVerification() {
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        // Store current verify retry count
        Long verifyRetryCount = companyBankAccount.getVerifyRetryCount();

        // Set Verification Transactions State to "EXECUTED"  and set incorrect amounts to be verified
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            // Add the FinancialTransactionState object for the current State
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            // Attempt to verify a wrong amount - correct amounts are between 0.01 and 0.99
            amountsToVerify.add(new SpcfMoney(SpcfDecimal.createInstance("1")));
        }
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of verify txns", 2, amountsToVerify.size());
        PayrollServices.beginUnitOfWork();

        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        out.println(processResult);
        companyBankAccount = processResult.getResult();

        // Reset PSPDate
        PSPDate.resetPSPTime();

        // Commit
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        CompanyService ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(
                company, EventTypeCode.BankAccountVerified, CompanyEventStatus.Active, null, null);
        int numCompanyEvents = companyEvents.size();
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() > 0);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "190", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 failed verification - verification transaction amounts do not match.", message.getMessage());

        // Verify that verifyRetryCount has been increased by 1
        assertEquals("Verify Retry Count: ", verifyRetryCount + 1, companyBankAccount.getVerifyRetryCount());

        assertEquals("No verified events exist", 0, numCompanyEvents);
        // Verify that Last Retry Date
        assertNotNull("Company Bank Account Last Retry Date:", processResult.getResult().getLastRetryDate());

    }

    @Test
    public void testInvalidCompanyParameters() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(null, company.getSourceCompanyId(), "123123", null, null, false);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.verifyCompanyBankAccount(SourceSystemCode.QBOE, null, "123123", null, null, false);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    @Test
        /**
         *  Test error message 1101 - Company Bank Account Verification not allowed for current company status
         */

        public void verifyCompanyBankAccountNotAllowedCapability() {
            String sourceCompanyId = company.getSourceCompanyId();
            SourceSystemCode sourceSystemCd = company.getSourceSystemCd();

            PayrollServices.beginUnitOfWork();
            // Load CompanyBankAccount
            CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
            CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
            PayrollServices.commitUnitOfWork();

             //Set company status to "Inactive"
            PayrollServices.beginUnitOfWork();
            PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), null, null, false);
            out.println(processResult);

            // Commit
            PayrollServices.commitUnitOfWork();

            // validate error count
            assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

            // validate error code
            Message message = processResult.getMessages().get(0);
            assertEquals("Error Code:", "1101", message.getMessageCode());

            // Verify that the correct message string has returned
            assertEquals("Error Message", "The operation VerifyCompanyBankAccount is not allowed for company QBOE:123272727 in its current state.", message.getMessage());

        }


    @Test
    public void verifyCompanyBankAccountAllowedCapability_RiskAssessment() {
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload("STD", null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        // todo v2: use processes instead of direct data manipulation
        PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.FraudReview);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        ProcessResult<CompanyBankAccount> processResult =
                PayrollServices.companyManager.verifyCompanyBankAccount(
                        company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        companyBankAccount.getSourceBankAccountId(),
                        verificationTransactions.get(0).getFinancialTransactionAmount(),
                        verificationTransactions.get(1).getFinancialTransactionAmount(), false);
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);
    }

    @Test
    public void verifyCompanyBankAccountAllowedCapability_AchRejectOther() {
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload("STD", null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        // todo v2: use processes instead of direct data manipulation
        PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        ProcessResult<CompanyBankAccount> processResult =
                PayrollServices.companyManager.verifyCompanyBankAccount(
                        company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        companyBankAccount.getSourceBankAccountId(),
                        verificationTransactions.get(0).getFinancialTransactionAmount(),
                        verificationTransactions.get(1).getFinancialTransactionAmount(), false);
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);
    }

    /**
     * test to verify company status does not change if it is already in active state
     */
    @Test
    public void verifyCBA_CompanyAlreadyActive() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyBankAccount companyBankAccount = dataloader.persistCompanyBankAccount(company, dataloader.getTestCompanyBankAccount());
        PayrollServices.commitUnitOfWork();

        // Submit payroll
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
        company = PayrollServicesTest.save(company);
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        PayrollRunDTO payrollRunDTO = psdl.createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());
        // Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyService ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);

        assertEquals("Company Status", ServiceSubStatusCode.ActiveCurrent, ddService.getStatusCd());

        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                "123123");
        ProcessResult<CompanyBankAccount> deactivateResult =
                PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE,
                        "123272727", companyBankAccount.getSourceBankAccountId(), true, false);
        companyBankAccount = deactivateResult.getResult();
        // validate error count
        assertEquals("Number of Errors:", 0, deactivateResult.getMessages().size());
        Assert.assertTrue(deactivateResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Add another bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> addCBAResult = PayrollServices.companyManager.addCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727",
                dataloader.getTestCompanyBankAccount2(), true, true);
        companyBankAccount = addCBAResult.getResult();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Errors:", 0, addCBAResult.getMessages().size());
        Assert.assertTrue(addCBAResult.isSuccess());

        // verify the bank account
        PayrollServices.beginUnitOfWork();
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        ProcessResult<CompanyBankAccount> verifyCBAResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727",
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Errors:", 0, addCBAResult.getMessages().size());
        Assert.assertTrue(addCBAResult.isSuccess());

        // Verify that the company status is not in pending verification
        PayrollServices.beginUnitOfWork();
        ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        assertEquals("Company Status", ServiceSubStatusCode.ActiveCurrent, ddService.getStatusCd());
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test to verify that the current active CBA is deactivated when another CBA is verified
     */
    @Test
    public void verifyCBADeactivatesCurrentActiveCBA() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        String oldCBASourceID = dataloader.persistCompanyBankAccount(
                company, dataloader.getTestCompanyBankAccount()).getSourceBankAccountId();
        PayrollServices.commitUnitOfWork();

        // Add another bank account
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070831000000");
        ProcessResult<CompanyBankAccount> addCBAResult = PayrollServices.companyManager.addCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727",
                dataloader.getTestCompanyBankAccount2(), true, true);
        CompanyBankAccount newCBA = addCBAResult.getResult();
        assertSuccess(addCBAResult);
        assertEquals("New CBA status", BankAccountStatus.PendingVerification, newCBA.getStatusCd());
        CompanyBankAccount oldCBA = CompanyBankAccount.findCompanyBankAccountsIncludingExpired(
                company, oldCBASourceID).get(0);
        assertEquals("Old CBA Status", BankAccountStatus.Active, oldCBA.getStatusCd());
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        // verify the bank account
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        newCBA = Application.findById(CompanyBankAccount.class, newCBA.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = newCBA.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        ProcessResult<CompanyBankAccount> verifyCBAResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727",
                newCBA.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess(verifyCBAResult);

        // Verify that the oldCBA was deactivated during verification
        PayrollServices.beginUnitOfWork();
        newCBA = CompanyBankAccount.findCompanyBankAccountsIncludingExpired(
                company, newCBA.getSourceBankAccountId()).get(0);
        assertEquals("New CBA status", BankAccountStatus.Active, newCBA.getStatusCd());

        oldCBA = CompanyBankAccount.findCompanyBankAccountsIncludingExpired(
                company, oldCBASourceID).get(0);
        assertEquals("Old CBA Status", BankAccountStatus.Inactive, oldCBA.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to verify that the current active CBA is deactivated when another CBA is added without verification
     */
    @Test
    public void addCBADeactivatesCurrentActiveCBA() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        String oldCBASourceID = dataloader.persistCompanyBankAccount(
                company, dataloader.getTestCompanyBankAccount()).getSourceBankAccountId();
        PayrollServices.commitUnitOfWork();

        // Add another bank account
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070831000000");
        ProcessResult<CompanyBankAccount> addCBAResult = PayrollServices.companyManager.addCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727",
                dataloader.getTestCompanyBankAccount2(), false, true);
        CompanyBankAccount newCBA = addCBAResult.getResult();
        assertSuccess(addCBAResult);
        assertEquals("New CBA status", BankAccountStatus.Active, newCBA.getStatusCd());
        CompanyBankAccount oldCBA = CompanyBankAccount.findCompanyBankAccountsIncludingExpired(
                company, oldCBASourceID).get(0);
        assertEquals("Old CBA Status", BankAccountStatus.Inactive, oldCBA.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAgentVerifyCancelsPendingEVDs() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addCompanyBankAccount(company, true);

        PayrollServices.beginUnitOfWork();
        assertEquals(2, FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created).size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        CompanyBankAccount companyBankAccount = assertOne(company.getCompanyBankAccountCollection());

        assertSuccess(PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(),
                null,
                null,
                true));

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals(0, FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created).size());
        assertEquals(2, FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Cancelled).size());
        PayrollServices.rollbackUnitOfWork();
    }
}
