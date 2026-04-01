package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertContains;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertCount;
import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * User: mvillani
 * Date: Oct 11, 2007
 * Time: 3:25:49 PM

 */
public class DeactivateCompanyBankAccountCoreTests {

    private Company company;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    /**
     * Successfully deactivate a company bank account
     */
    @Test
    public void deactivateCompanyBankAccountSuccessful() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        // Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        companyBankAccount = processResult.getResult();
        out.println(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        // Verify that companybankaccount has been deactivated
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        company = companies.get(0);

        //Make sure we have just one bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);
        companyBankAccount = findMostRecentlyDeactivatedCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());

        // Verify that status = Inactive
        assertEquals("Company Bank Account Status:", BankAccountStatus.Inactive, companyBankAccount.getStatusCd());
        assertNotNull("Expiration Date:", companyBankAccount.getExpirationDate());
        
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Try after transactions cutoff time
     */
    @Test
    public void deactivateCompanyBankAccount_AfterCutOffTime() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070912180000");
        PayrollServices.commitUnitOfWork();

        // Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        companyBankAccount = processResult.getResult();
        out.println(processResult);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        // verify transactions are not cancelled
        PayrollServices.beginUnitOfWork();
        companyBankAccount = findMostRecentlyDeactivatedCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());

        // Verify that status = Inactive
        assertEquals("Company Bank Account Status:", BankAccountStatus.Inactive, companyBankAccount.getStatusCd());
        assertNotNull("Expiration Date:", companyBankAccount.getExpirationDate());
        DomainEntitySet<FinancialTransaction> verificationTransactions =
                    FinancialTransaction.findFinancialTransactions(
                            company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getBankAccount(),
                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        assertEquals("Pending verification transactions", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test error message 1101 - is allowed capability false
     */
    @Test
    public void deactivateCompanyBankAccountNotAllowedCapability() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPrincipalToQBDT();

        // Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        companyBankAccount = processResult.getResult();
        out.println(processResult);
        PayrollServices.commitUnitOfWork();

       // validate error count
        assertCount(1, processResult);
        assertContains(1101, MessageInfo.MessageLevel.ERROR, processResult);
    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void deactivateCompanyBankAccountCompanyDoesNotExist() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        // Set invalid company
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), "InvalidCompanyId", companyBankAccount.getSourceBankAccountId(), false, false);
        out.println(processResult);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());

        // Commit
        PayrollServices.commitUnitOfWork();

    }


    @Test
    /**
     *  Test error message 170 - BankAccount for company does not exist
     */

    public void deactivateCompanyBankAccountBankAccountDoesNotExist() {
        // Call Process with a non existent company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), "InvalidSourceBankAccountId", false, false);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "170", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account InvalidSourceBankAccountId for company QBOE:123272727 does not exist.", message.getMessage());


    }

    @Test
    /**
     *  Test error message 217 - Company Bank Account already Inactive
     */

    public void deactivateCompanyBankAccountAccountNotActive() {
        // Load Company Data
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
        PayrollServices.commitUnitOfWork();

        // Create company bank account and set status to inactive
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        ProcessResult<CompanyBankAccount> firstDeactivateProcessResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        PayrollServices.commitUnitOfWork();
        assertEquals("First deactivate result: ", true, firstDeactivateProcessResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        
        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "217", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 was already inactive in the PSE.", message.getMessage());

        // Commit
        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void testInvalidCompanyParameters() {

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(null, company.getSourceCompanyId(), "123123", false, false);
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
        processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, null, "123123", false, false);
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

    /**
     * Try to deactivate a company bank account which has pending transactions with shouldAllowPendingTransactions
     * false
     */
    @Test
    public void deactivateCBAFailure_HasPendingTransactions() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        PSPDate.setPSPTime("20070823000000");
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
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        companyBankAccount = processResult.getResult();
        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "218", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account 123123 for company QBOE:123272727 has pending transactions and cannot be deactivated.";
        assertEquals("Error Message", messageText, message.getMessage());
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Try to deactivate a company bank account which has pending transactions with shouldAllowPendingTransactions
     * true
     */
    @Test
    public void deactivateCBASuccess_HasPendingTransactions() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        PSPDate.setPSPTime("20070823000000");
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
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), true, false);
        companyBankAccount = processResult.getResult();        
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Verify that companybankaccount has been deactivated
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        company = companies.get(0);

        //Make sure we have just one bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);
        companyBankAccount = findMostRecentlyDeactivatedCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());

        // Verify that status = Inactive
        assertEquals("Company Bank Account Status:", BankAccountStatus.Inactive, companyBankAccount.getStatusCd());
        assertNotNull("Expiration Date:", companyBankAccount.getExpirationDate());
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Try to deactivate a company bank account which has unresolved transaction returns
     * false
     */
    @Test
    public void deactivateCBAFailure_HasUnResolvedTransactionReturns() {
        company = null;
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(company);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());
        assertTrue("TransactionReturn status is not 'Resolved'", TransactionReturnStatusCode.Resolved != returnList.get(0).getReturnStatusCd());

        // Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> prDeactivate = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), cba.getSourceBankAccountId(), false, false);
        cba = prDeactivate.getResult();
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, prDeactivate.getMessages().size());

        // validate error code
        Message message = prDeactivate.getMessages().get(0);
        assertEquals("Error Code:", "226", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account C1BA1 for company QBDT:8574536 has unresolved ACH returns and cannot be deactivated.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Try to deactivate a company bank account which has unresolved transaction returns
     * false
     */
    @Test
    public void deactivateCBASuccess_HasResolvedTransactionReturns() {
        DataLoader dataLoader = new DataLoader();

        DataLoadServices.setPSPDate(2007, 8, 22);

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //Persist Company Bank Account
        CompanyBankAccount companyBankAccount =dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        // Create Employees and Employee Bank Accounts
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
        company = PayrollServicesTest.save(company);

        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRunDTO payrollRunDTO = psdl.createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        //Submit Payroll
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO));

        PayrollServices.commitUnitOfWork();

        // create and offload random debit refunds
        DataLoadServices.runACHTransactionProcessor(5);
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runACHTransactionProcessor(5);

        //Offload debit transaction
        DataLoadServices.runOffload(company, 2007, 9, 25);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an Employer Debit Return");


        // Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        companyBankAccount = assertSuccessResult(PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Verify that companybankaccount has been deactivated
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        company = companies.get(0);

        //Make sure we have just one bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);
        companyBankAccount = findMostRecentlyDeactivatedCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());
        PayrollServices.commitUnitOfWork();
        
        // Verify that status = Inactive
        assertEquals("Company Bank Account Status:", BankAccountStatus.Inactive, companyBankAccount.getStatusCd());
        assertNotNull("Expiration Date:", companyBankAccount.getExpirationDate());        
    }

    /**
     * Try to deactivate a company bank account which has unresolved transaction returns
     * true
     */
    @Test
    public void deactivateCBASucess_HasUnResolvedTransactionReturns() {
        company = null;
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(company);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());
        assertTrue("TransactionReturn status is not 'Resolved'", TransactionReturnStatusCode.Resolved != returnList.get(0).getReturnStatusCd());

        // Deactivate Company Bank Account without resolving the Transaction Returns
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult1 = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), cba.getSourceBankAccountId(), true, false);
        cba = processResult1.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Verify that companybankaccount has been deactivated
        company = Application.refresh(company);

        //Make sure we have just one bank account associated with the company
        assertEquals("number of CBAs for company", 1, company.getCompanyBankAccountCollection().size());
        cba = findMostRecentlyDeactivatedCompanyBankAccount(company, cba.getSourceBankAccountId());
        PayrollServices.commitUnitOfWork();

        // Verify that status = Inactive
        assertEquals("Company Bank Account Status:", BankAccountStatus.Inactive, cba.getStatusCd());
        assertNotNull("Expiration Date:", cba.getExpirationDate());

    }

    @Test
    public void deactivateCBASucess_HasResolvedTransactionReturns() {
        DataLoader dataLoader = new DataLoader();

        DataLoadServices.setPSPDate(2007, 8, 31);

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //Persist Company Bank Account
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());
        // Create Employees and Employee Bank Accounts
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
        company = PayrollServicesTest.save(company);

        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRunDTO payrollRunDTO = psdl.createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        PayrollServices.commitUnitOfWork();
        
        //Add Transactions for Company Bank Account2
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRunDTO = psdl.createPayrollRunDTO(company, companyBankAccount, "BatchId02");

        //Submit Payroll
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        // create and offload random debit refunds
        DataLoadServices.runACHTransactionProcessor(5);
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2007, 9, 17, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runACHTransactionProcessor(5);

        //Offload Employer debit transaction
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an Employer Debit Return");

        // Deactivate Company Bank Account after resolving the Transaction Returns
        PayrollServices.beginUnitOfWork();
        companyBankAccount = assertSuccessResult(PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false));
        PayrollServices.commitUnitOfWork();
                PayrollServices.beginUnitOfWork();
        // Verify that companybankaccount has been deactivated
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        company = companies.get(0);

        //Make sure we have two bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);
        CompanyBankAccount deactivatedCBA = findMostRecentlyDeactivatedCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());
        PayrollServices.commitUnitOfWork();

        // Verify that status = Inactive
        assertEquals("Company Bank Account Status:", BankAccountStatus.Inactive, deactivatedCBA.getStatusCd());
        assertNotNull("Expiration Date:", deactivatedCBA.getExpirationDate());
    }

    private CompanyBankAccount findMostRecentlyDeactivatedCompanyBankAccount(Company pCompany, String pSourceBankAccountId) {
        CompanyBankAccount cbaToReturn = null;

        DomainEntitySet<CompanyBankAccount> companyBankAccounts = CompanyBankAccount.findDeactivatedCompanyBankAccounts(pCompany, pSourceBankAccountId);

        for (CompanyBankAccount cbaToTest : companyBankAccounts) {
            if (cbaToReturn == null || cbaToReturn.getExpirationDate().compareTo(cbaToTest.getExpirationDate()) < 0) {
                cbaToReturn = cbaToTest;
            }
        }

        return cbaToReturn;
    }
}
