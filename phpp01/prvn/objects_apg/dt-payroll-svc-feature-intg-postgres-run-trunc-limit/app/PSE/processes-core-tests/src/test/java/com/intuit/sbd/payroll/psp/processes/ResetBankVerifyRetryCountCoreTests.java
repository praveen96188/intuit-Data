package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 11, 2008
 * Time: 8:55:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResetBankVerifyRetryCountCoreTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
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

        ProcessResult processResult = PayrollServices.companyManager.resetBankAccountVerifyRetryCount(null, "123272727", null);

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
        ProcessResult processResult = PayrollServices.companyManager.resetBankAccountVerifyRetryCount(SourceSystemCode.QBOE, null, null);

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
    public void testInvalidCompany() {
        PayrollServices.beginUnitOfWork();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        ProcessResult processResult = PayrollServices.companyManager.resetBankAccountVerifyRetryCount(SourceSystemCode.QBOE,
                "1232727", companyBankAccount.getSourceBankAccountId());

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1232727 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test message 170 - BankAccount for company does not exist
     */
    @Test
    public void testInvalidCompanyBankAccount() {
        PayrollServices.beginUnitOfWork();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult processResult = PayrollServices.companyManager.resetBankAccountVerifyRetryCount(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), companyBankAccountDTO.getCompanyBankAccountID());

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "170", errorMessage.getMessageCode());
        assertEquals("Error message", "Bank Account 123123 for company QBOE:123272727 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test error message 186 - Company Bank Account is not Active
     */
    @Test
    public void testCompanyBankAccountNotActive() {
        PayrollServices.beginUnitOfWork();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
        Application.commitUnitOfWork();

        // Deactivate the CBA
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        ProcessResult<CompanyBankAccount> deactivateProcessResult = PayrollServices.companyManager.
                deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        companyBankAccountDTO.getCompanyBankAccountID(), false, false);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Deactivate result: ", true, deactivateProcessResult.isSuccess());


        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.resetBankAccountVerifyRetryCount(SourceSystemCode.QBOE,
                "123272727", companyBankAccount.getSourceBankAccountId());

        PayrollServices.commitUnitOfWork();


        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "186", errorMessage.getMessageCode());
        assertEquals("Error message", "Bank Account 123123 for company QBOE:123272727 is not active in PSP.",
                errorMessage.getMessage());
    }    

    /**
     * Test error message 186 - Company Bank Account is not Active
     */
    @Test
    public void testHappyPath() {
        PayrollServices.beginUnitOfWork();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        companyBankAccount = PayrollServices.entityFinder.findById(CompanyBankAccount.class, companyBankAccount.getId());

        //Verify Company Bank Account
        PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), new SpcfMoney("0"),new SpcfMoney("0"), false);

        //Verify Company Bank Account
        PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), new SpcfMoney("0"),new SpcfMoney("0"), false);

        assertEquals("Verify Retry Count", 2L, companyBankAccount.getVerifyRetryCount());        
        PayrollServices.commitUnitOfWork();



        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.resetBankAccountVerifyRetryCount(SourceSystemCode.QBOE,
                "123272727", companyBankAccount.getSourceBankAccountId());

        companyBankAccount = processResult.getResult();

        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess(processResult);
        assertEquals("Verify Retry Count", 0L, companyBankAccount.getVerifyRetryCount());
    }
}
