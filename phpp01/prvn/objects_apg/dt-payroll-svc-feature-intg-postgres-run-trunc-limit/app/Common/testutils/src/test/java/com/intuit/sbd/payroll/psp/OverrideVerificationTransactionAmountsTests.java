package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.processes.OverrideVerificationTransactionAmounts;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jun 4, 2008
 * Time: 8:51:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class OverrideVerificationTransactionAmountsTests {

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

    @Test
    public void testOverrideVerificationAmounts_Success() {
        SpcfMoney amount1 = new SpcfMoney("0.99");
        SpcfMoney amount2 = new SpcfMoney("0.99");
        PayrollServices.beginUnitOfWork();
        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company newCompany = CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        SpcfCalendar settlementDate = CalendarUtils.getPSPDateFromDB();
        CalendarUtils.clearTime(settlementDate);
        CalendarUtils.addBusinessDays(settlementDate, 2);

        OverrideVerificationTransactionAmounts overrideProcess = new  OverrideVerificationTransactionAmounts(
                newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(), amount1, amount2, settlementDate);
        ProcessResult result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of errors", 0, result.getMessages().size());
        assertEquals("Result", true, result.isSuccess());

        // verify the amounts
        PayrollServices.beginUnitOfWork();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions =
                                companyBankAccount.getVerificationTransactions();
        assertEquals("Amount1", "0.99", verificationTransactions.get(0).getFinancialTransactionAmount().toString());
        assertEquals("TransactionStateCd1", "Executed", verificationTransactions.get(0).getCurrentTransactionState().getTransactionStateCd().toString());

        assertEquals("Amount2", "0.99", verificationTransactions.get(1).getFinancialTransactionAmount().toString());
        assertEquals("TransactionStateCd2", "Executed", verificationTransactions.get(1).getCurrentTransactionState().getTransactionStateCd().toString());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testOverrideVerificationAmounts_InvalidCompanyParams() {
        SpcfMoney amount1 = new SpcfMoney("0.99");
        SpcfMoney amount2 = new SpcfMoney("0.99");
        PayrollServices.beginUnitOfWork();
        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company newCompany = CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        SpcfCalendar settlementDate = CalendarUtils.getPSPDateFromDB();
        CalendarUtils.addBusinessDays(settlementDate, -2);

        OverrideVerificationTransactionAmounts overrideProcess = new  OverrideVerificationTransactionAmounts(
                null, newCompany.getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(), amount1, amount2, settlementDate);
        ProcessResult result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of errors", 1, result.getMessages().size());
        assertEquals("Result", false, result.isSuccess());
        // Verify the error message
        Message message = result.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());
        // verify the amounts
        PayrollServices.beginUnitOfWork();
        overrideProcess = new  OverrideVerificationTransactionAmounts(
                newCompany.getSourceSystemCd(), null,
                companyBankAccountDTO.getCompanyBankAccountID(), amount1, amount2, settlementDate);
        result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of errors", 1, result.getMessages().size());
        assertEquals("Result", false, result.isSuccess());
        // Verify the error message
        message = result.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void testOverrideVerificationAmounts_InvalidCompany() {
        SpcfMoney amount1 = new SpcfMoney("0.99");
        SpcfMoney amount2 = new SpcfMoney("0.99");
        PayrollServices.beginUnitOfWork();
        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company newCompany = CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        SpcfCalendar settlementDate = CalendarUtils.getPSPDateFromDB();
        CalendarUtils.addBusinessDays(settlementDate, -2);

        OverrideVerificationTransactionAmounts overrideProcess = new  OverrideVerificationTransactionAmounts(
                newCompany.getSourceSystemCd(), "Invalid",
                companyBankAccountDTO.getCompanyBankAccountID(), amount1, amount2, settlementDate);
        ProcessResult result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of errors", 1, result.getMessages().size());
        assertEquals("Result", false, result.isSuccess());

        Message message = result.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());
        String messageText = "Company QBOE:Invalid does not exist.";
        assertEquals("Error Message", messageText, message.getMessage());
    }


    public void testOverrideVerificationAmounts_InvalidCompanyBankAccount() {
        SpcfMoney amount1 = new SpcfMoney("0.99");
        SpcfMoney amount2 = new SpcfMoney("0.99");
        PayrollServices.beginUnitOfWork();
        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company newCompany = CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        SpcfCalendar settlementDate = CalendarUtils.getPSPDateFromDB();
        CalendarUtils.addBusinessDays(settlementDate, -2);

        OverrideVerificationTransactionAmounts overrideProcess = new  OverrideVerificationTransactionAmounts(
                newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(),
                "Invalid", amount1, amount2, settlementDate);
        ProcessResult result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of errors", 1, result.getMessages().size());
        assertEquals("Result", false, result.isSuccess());

        Message message = result.getMessages().get(0);
        assertEquals("Error Code:", "170", message.getMessageCode());
        String messageText = "Bank Account Invalid for company QBOE:123272727 does not exist.";
        assertEquals("Error Message", messageText, message.getMessage());
    }
    
    @Test
    public void testOverrideVerificationAmounts_CBAAlreadyVerified() {
        SpcfMoney amount1 = new SpcfMoney("0.99");
        SpcfMoney amount2 = new SpcfMoney("0.99");
        PayrollServices.beginUnitOfWork();
        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company newCompany = CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        PSPDate.setPSPTime("20070831000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        processResult = PayrollServices.companyManager.verifyCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(),
                verificationTransactions.get(0).getFinancialTransactionAmount(),
                verificationTransactions.get(1).getFinancialTransactionAmount(), false);
        out.println(processResult);
        assertEquals(0, processResult.getMessages().size());
        companyBankAccount = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        //todo:v2 review this
        SpcfCalendar settlementDate = CalendarUtils.getPSPDateFromDB();
        CalendarUtils.addBusinessDays(settlementDate, -2);

        OverrideVerificationTransactionAmounts overrideProcess = new  OverrideVerificationTransactionAmounts(
                newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(), amount1, amount2, settlementDate);
        ProcessResult result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of errors", 1, result.getMessages().size());
        assertEquals("Result", false, result.isSuccess());

        Message message = result.getMessages().get(0);
        assertEquals("Error Code:", "188", message.getMessageCode());
        String messageText = "Bank Account 123123 for company QBOE:123272727 currently has a status of Active and cannot be verified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    @Test
    public void testOverrideVerificationAmounts_InvalidAmounts() {
        SpcfMoney amount1 = new SpcfMoney("0.99");
        SpcfMoney amount2 = new SpcfMoney("0.99");
        SpcfMoney invalidAmount1 = new SpcfMoney("0.00");
        SpcfMoney invalidAmount2 = new SpcfMoney("1.00");
        PayrollServices.beginUnitOfWork();
        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company newCompany = CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        CompanyBankAccount companyBankAccount = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        SpcfCalendar settlementDate = CalendarUtils.getPSPDateFromDB();
        CalendarUtils.addBusinessDays(settlementDate, -2);

        OverrideVerificationTransactionAmounts overrideProcess = new  OverrideVerificationTransactionAmounts(
                newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(), invalidAmount1, amount2, settlementDate);
        ProcessResult result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of errors", 1, result.getMessages().size());
        assertEquals("Result", false, result.isSuccess());
        // Verify the error message
        Message message = result.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());
        String messageText = "VerificationAmount has invalid value";
        assertEquals("Error Message", messageText, message.getMessage());
        // verify the amounts
        PayrollServices.beginUnitOfWork();
        overrideProcess = new  OverrideVerificationTransactionAmounts(
                newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(), amount1, invalidAmount2, settlementDate);
        result = overrideProcess.execute();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of errors", 1, result.getMessages().size());
        assertEquals("Result", false, result.isSuccess());
        // Verify the error message
        message = result.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());
        messageText = "VerificationAmount has invalid value";
        assertEquals("Error Message", messageText, message.getMessage());
    }


}
