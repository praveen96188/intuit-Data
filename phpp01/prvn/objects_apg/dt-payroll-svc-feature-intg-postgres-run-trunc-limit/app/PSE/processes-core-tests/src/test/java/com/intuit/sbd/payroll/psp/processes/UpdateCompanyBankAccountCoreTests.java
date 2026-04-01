package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static java.lang.System.out;

/**
 *
 * User: mvillani
 * Date: Sep 24, 2007
 * Time: 12:27:08 PM

 */
public class UpdateCompanyBankAccountCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updateCompanyBankAccountSuccessful() {
        PayrollServices.beginUnitOfWork();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);

        // Change only the bank name on CompanyBankAccountDTO
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO);
        out.println(processResult);
        companyBankAccount = processResult.getResult();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        // Verify that companybankaccount has been saved
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        company = companies.get(0);

        //Ensure there is just one bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);
        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(), companyBankAccount.getSourceBankAccountId());
        assertEquals("Bank Name: ", companyBankAccountDTO.getBankAccountDTO().getBankName(), companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(), companyBankAccount.getSourceBankAccountName());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void updateQBDTCompanyBankAccountSuccessful() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company1 = c3dl.persistCompany3();
        CompanyBankAccountDTO updatedCompanyBankAccountDTO = c3dl.getCompany1BankAccount();
        // Change only the source bank name on CompanyBankAccountDTO
        updatedCompanyBankAccountDTO.setSourceBankAccountName("WellsNickname");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedCompanyBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();

        // Verify that companybankaccount has been saved
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        Company company = companies.get(0);

        //Ensure there is just one bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", updatedCompanyBankAccountDTO.getCompanyBankAccountID(), companyBankAccount.getSourceBankAccountId());
        assertEquals("Bank Name: ", updatedCompanyBankAccountDTO.getBankAccountDTO().getBankName(), companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", updatedCompanyBankAccountDTO.getSourceBankAccountName(), companyBankAccount.getSourceBankAccountName());
        PayrollServices.commitUnitOfWork();

    }


    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void updateCompanyBankAccountCompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        // Set invalid company
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company.getSourceSystemCd(), "InvalidCompanyId", companyBankAccountDTO);
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());
    }

    @Test
    /**
     *  Test error message 177 - Company is not active
     */

    public void updateCompanyBankAccountCompanyNotActive() {
        PayrollServices.beginUnitOfWork();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        //Set company status to "Inactive"
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO);
        out.println(processResult);
        PayrollServices.commitUnitOfWork();


        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());
    }

    @Test
    /**
     *  Test error message 170 - BankAccount for company does not exist
     */

    public void updateCompanyBankAccountBankAccountDoesNotExist() {

        // Load Company Data
        PayrollServices.beginUnitOfWork();
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        // Load DTO with a non existent company bank account
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "170", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 does not exist.", message.getMessage());


    }

    @Test
    /**
     *  Test error message 186 - Company Bank Account is not Active
     */

    public void updateCompanyBankAccountAccountNotActive() {
        PayrollServices.beginUnitOfWork();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
        PayrollServices.commitUnitOfWork();

        // Deactivate the CBA
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        ProcessResult<CompanyBankAccount> deactivateProcessResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO.getCompanyBankAccountID(),false, false);
        assertEquals("Deactivate result: ", true, deactivateProcessResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO);
        out.println(processResult);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "186", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 is not active in PSP.", message.getMessage());
    }

    @Test
    /**
     *  Test error message 202  - Update not only Bank Name
     */

    public void updateCompanyBankAccountChangeMoreThanBankName() {
        PayrollServices.beginUnitOfWork();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);

        companyBankAccountDTO.getBankAccountDTO().setAccountNumber("NewNumber");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO);
        out.println(processResult);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "202", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Update failed - Account Number, Routing Number, and Account Type cannot be updated for company bank accounts that are verified or in the verification process.", message.getMessage());
    }

    @Test
    public void testInvalidCompanyBankAccountDTO() {
        // Load Company Data
        PayrollServices.beginUnitOfWork();
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.setCompanyBankAccountID("ThereAreMoreThan50CharactersInSourceCompanyBankAccountId");
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "BankAccountId has invalid value";
        assertEquals("Error Message", message.getMessage(), messageText);

    }

    @Test
    public void testInvalidBankAccountDTO() {
        // Load Company Data
        PayrollServices.beginUnitOfWork();
        Company company = CompanyBankAccountDataLoader.loadCompany();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        PayrollServices.commitUnitOfWork();

        //  Test Null Bank Account DTO
        PayrollServices.beginUnitOfWork();
        companyBankAccountDTO.setBankAccountDTO(null);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "142", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    @Test
    public void testInvalidCompanyParameters() {

        // Load Company Data
        PayrollServices.beginUnitOfWork();
        Company company = CompanyBankAccountDataLoader.loadCompany();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.updateCompanyBankAccount(null, company.getSourceCompanyId(), companyBankAccountDTO);
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
        processResult = PayrollServices.companyManager.updateCompanyBankAccount(SourceSystemCode.QBOE, null, companyBankAccountDTO);
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

}
