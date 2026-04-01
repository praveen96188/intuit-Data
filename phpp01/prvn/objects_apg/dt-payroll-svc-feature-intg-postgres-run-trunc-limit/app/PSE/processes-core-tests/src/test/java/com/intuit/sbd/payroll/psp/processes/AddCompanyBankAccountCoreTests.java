package com.intuit.sbd.payroll.psp.processes;

import com.intuit.platform.integration.ius.common.types.IntuitContext;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: mvillani
 * Date: Aug 18, 2007
 * Time: 5:11:19 PM
 */
public class AddCompanyBankAccountCoreTests {
    private String cbaMatchesFraudNotes = "This company was not activated because the company bank account matches the company bank account of company Intuit (Source System=QBOE Source ID=1234567) with status of Terminated.";
    private String eebaMatchesFraudNotes = "This company was not activated because the company bank account matches the employee bank account TestLastName, FirstNameOfEE1 TMI of company Intuit (Source System=QBOE Source ID=1234567) with status of Terminated.";
    private String multBAMatchFraudNotes = "This company was not activated because the company bank account matches the company bank account of company Intuit (Source System=QBOE Source ID=1234567) with status of Terminated.";

    private static final String PAYROLL_PLUGIN_ASSET_ALIAS = "Intuit.payroll.dirctdeposit.qbdtpayrolltronexp";

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
    /**
     * Successfully add a company bank account
     */
    public void addCompanyBankAccountSuccessful() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        Company company = companies.get(0);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());

        //Verifty that CBA Verification amounts are between $0.01 and $0.99
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            assertEquals("FinancialTransaction Amount ",
                    financialTransaction.getFinancialTransactionAmount().compareTo(new SpcfMoney("1")), -1);
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Successfully add a company bank account, without adding random debit amounts
     */
    public void addCompanyBankAccountSuccessful_NoRandomDebits() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, false, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        Company company = companies.get(0);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());
        CompanyService ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.Active,
                companyBankAccount.getStatusCd());
        assertEquals("Company Status:", ServiceSubStatusCode.PendingFirstPayroll,
                ddService.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 0, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * test to verify company status does not change if it is already in active state
     */
    @Test
    public void addCBA_CompanyAlreadyActive() {
        // Load CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollDTO = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        // Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyService ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        assertEquals("Company Status", ServiceSubStatusCode.ActiveCurrent, ddService.getStatusCd());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company,
                "123123");
        ProcessResult<CompanyBankAccount> deactivateResult =
                PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE,
                        "123272727", companyBankAccount.getSourceBankAccountId(), true, false);
        companyBankAccount = deactivateResult.getResult();
        // validate error count
        assertEquals("Number of Errors:", 0, deactivateResult.getMessages().size());
        assertTrue(deactivateResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Add another bank account
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        ProcessResult<CompanyBankAccount> addCBAResult = PayrollServices.companyManager.addCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727",
                dataloader.getTestCompanyBankAccount2(), true, true);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Errors:", 0, addCBAResult.getMessages().size());
        assertTrue(addCBAResult.isSuccess());

        // Verify that the company status is not in pending verification
        PayrollServices.beginUnitOfWork();
        ddService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        assertEquals("Company Status", ServiceSubStatusCode.ActiveCurrent, ddService.getStatusCd());
        PayrollServices.commitUnitOfWork();

    }

    /**
     * test to add another cba, one already exists in pending verification
     */
    @Test
    public void addCBADuplicate_AlreadyHasCBAInPendingVerification() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        
        // Verify that no  validation errors have been returned
        assertSuccess(processResult);
        // Commit
        PayrollServices.commitUnitOfWork();

        // Add another bank account
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        ProcessResult<CompanyBankAccount> addCBAResult = PayrollServices.companyManager.addCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727",
                dataloader.getTestCompanyBankAccount2(), true, true);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertSuccess(addCBAResult);

    }



    @Test
    /**
     * try to add another active company bank account
     */
    public void addCBA_AlreadyHasActiveBankAccount() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccounte
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        PSPDate.setPSPTime("20070823000000");
        CompanyBankAccount oldCBA = dataloader.persistCompanyBankAccount(company, dataloader.getTestCompanyBankAccount());
        oldCBA = Application.refresh(oldCBA);
        String oldCBASourceID = oldCBA.getSourceBankAccountId();
        assertEquals("Old CBA Status", BankAccountStatus.Active, oldCBA.getStatusCd());
        PayrollServices.commitUnitOfWork();

        // try to add another bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        dataloader.getTestCompanyBankAccount2(), true, true);
        CompanyBankAccount newCBA = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("New CBA Status", BankAccountStatus.PendingVerification, newCBA.getStatusCd());

        oldCBA = CompanyBankAccount.findCompanyBankAccountsIncludingExpired(
                company, oldCBASourceID).get(0);
        assertEquals("Old CBA Status", BankAccountStatus.Active, oldCBA.getStatusCd());
        // Commit
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Add company 1 with a bank account
     * Terminate company 1
     * Add company 2
     * Add company 2 bank account with same acct num/routing num as company 1 bank account
     */
    public void addCompanyBankAccountFailsFraudControls() {
        Company1Dataloader c1dl = new Company1Dataloader();
        Company2Dataloader c2dl = new Company2Dataloader();
        DataLoader dataloader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = c1dl.persistCompany1();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = c2dl.getCompany1();
        for (ContactDTO currContact : company2.getContacts()) {
            currContact.setEmail("someEmail1@aol.com");
        }        
        Company domainCompany2 = dataloader.persistCompany(company2);
        dataloader.persistCompanyService(domainCompany2, c2dl.getCompany1Service());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companybankAccount2 = c2dl.getCompany1BankAccount();
        //same BA used by c1dl persistCompany
        BankAccountDTO c1BA = dataloader.getTestCompanyBankAccount().getBankAccountDTO();
        companybankAccount2.setBankAccountDTO(c1BA);
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(SourceSystemCode.QBOE, company2.getCompanyId(), companybankAccount2, true, true);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Company bank account added", processResult);
        assertEquals("Number of messages", 1, processResult.getMessages().size());

        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBOE:2222222 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        Assert.assertEquals("Company Events", 1, companyEventsList.size());
        Assert.assertEquals("Event Details", cbaMatchesFraudNotes,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));
        PayrollServices.commitUnitOfWork();
        

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companybankAccount2.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companybankAccount2.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companybankAccount2.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companybankAccount2.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companybankAccount2.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Add company 1 with a bank account
     * Terminate company 1
     * Add company 2
     * Add company 2 bank account with same acct num/routing num as company 1 bank account
     */
    public void addCompanyBankAccountCompanyOnHold() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF return");
        Company3Dataloader co3DL = new Company3Dataloader();
        CompanyBankAccountDTO cbaDTO = co3DL.getCompany1BankAccount();
        cbaDTO.setCompanyBankAccountID("CBA2");

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.addCompanyBankAccount(SourceSystemCode.QBDT, "8574536", cbaDTO, true, true);
        Application.commitUnitOfWork();

        assertSuccess("Company bank account added", processResult);
        assertEquals("Number of messages", 0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertTrue("Company is on hold", isCompanyOnHold);

        assertEquals("Number of notes", 0, numNotes);

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount companyBankAccount =CompanyBankAccount.findCompanyBankAccount(company, "CBA2");
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there are two company bank accounts associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 2);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", cbaDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", cbaDTO.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", cbaDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", cbaDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", cbaDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Add company 1 with a bank account
     * Terminate company 1
     * Add company 2
     * Add company 2 bank account with same acct num/routing num as company 1 ee1 bank account
     */
    public void addCompanyBankAccountFailsFraudControlsMultBA() {
        Company1Dataloader c1dl = new Company1Dataloader();
        Company2Dataloader c2dl = new Company2Dataloader();
        DataLoader dataloader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = c1dl.persistCompany1();
        c1dl.deactivateEE1BA1();
        c1dl.addEE1BAMatchesCoBA();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = c2dl.getCompany1();
        for (ContactDTO currContact : company2.getContacts()) {
            currContact.setEmail("someEmail1@aol.com");
        }        
        Company domainCompany2 = dataloader.persistCompany(company2);
        dataloader.persistCompanyService(domainCompany2, c2dl.getCompany1Service());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companybankAccount2 = c2dl.getCompany1BankAccount();
        //same BA used by c1dl persistCompany
        BankAccountDTO c1BA = dataloader.getTestCompanyBankAccount().getBankAccountDTO();
        companybankAccount2.setBankAccountDTO(c1BA);
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(SourceSystemCode.QBOE, company2.getCompanyId(), companybankAccount2, true, true);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Company bank account added", processResult);

        assertEquals("Number of messages", 1, processResult.getMessages().size());

        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBOE:2222222 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        //Verify that the company is on hold and the note was correctly saved
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());

        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany.getOnHoldReasonCollection();
        int numOnHolds = onHoldReasons.size();
        OnHoldReason onHoldReason = null;
        if (numOnHolds == 1) {
            onHoldReason = onHoldReasons.iterator().next();
        }
        PayrollServices.commitUnitOfWork();

        //Verify company is on hold for Fraud review
        assertEquals("Number of on hold reasons", 1, numOnHolds);
        assertEquals("On hold reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());
        assertTrue("Company is on hold", isCompanyOnHold);

        //Verify notes
        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        Assert.assertEquals("Company Events", 1, companyEventsList.size());
        Assert.assertEquals("Event Details", multBAMatchFraudNotes,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));
        PayrollServices.commitUnitOfWork();
        

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companybankAccount2.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companybankAccount2.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companybankAccount2.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companybankAccount2.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companybankAccount2.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Add company 1 with a bank account
     * Terminate company 1
     * Add company 2
     * Add company 2 bank account with same acct num/routing num as company 1 ee1 bank account
     */
    public void addCompanyBankAccountFailsFraudControlsEEBA() {
        Company1Dataloader c1dl = new Company1Dataloader();
        Company2Dataloader c2dl = new Company2Dataloader();
        DataLoader dataloader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = c2dl.getCompany1();
        for (ContactDTO currContact : company2.getContacts()) {
            currContact.setEmail("someEmail1@aol.com");
        }
        Company domainCompany2 = dataloader.persistCompany(company2);
        dataloader.persistCompanyService(domainCompany2, c2dl.getCompany1Service());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companybankAccount2 = c2dl.getCompany1BankAccount();
        //same BA used by c1dl persistCompany for ee1
        BankAccountDTO ee1BA = c1dl.getEmployee1BankAccount().getBankAccount();
        companybankAccount2.setBankAccountDTO(ee1BA);
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(SourceSystemCode.QBOE, company2.getCompanyId(), companybankAccount2, true, true);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Company bank account added", processResult);
        assertEquals("Number of messages", 1, processResult.getMessages().size());

        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Message code", "1040", errorMessage.getMessageCode());
        Assert.assertEquals("Message text",
                "Company QBOE:2222222 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        Assert.assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        //Verify that the company is on hold and the note was correctly saved
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        PayrollServices.commitUnitOfWork();
        assertTrue("Company is on hold", isCompanyOnHold);

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                foundCompany,
                EventTypeCode.CompanyMatchesFraudulentCompany, CompanyEventStatus.Active, null, null);

        //Assertion for CompanyMatchesFraudulentCompany Event
        Assert.assertEquals("Company Events", 1, companyEventsList.size());
        Assert.assertEquals("Event Details", eebaMatchesFraudNotes,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));
        PayrollServices.commitUnitOfWork();
        

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companybankAccount2.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companybankAccount2.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companybankAccount2.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companybankAccount2.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companybankAccount2.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCompanyBankAccountLikeEEBACompanyOnHold() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF return");
        Company3Dataloader co3DL = new Company3Dataloader();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO cbaDTO = co3DL.getCompany1BankAccount();
        BankAccountDTO ee1BA = c1dl.getEmployee1BankAccount().getBankAccount();
        cbaDTO.setCompanyBankAccountID("CBA2");
        cbaDTO.setBankAccountDTO(ee1BA);

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.addCompanyBankAccount(SourceSystemCode.QBDT, "8574536", cbaDTO, true, true);
        Application.commitUnitOfWork();

        assertSuccess("Company bank account added", processResult);
        assertEquals("Number of messages", 0, processResult.getMessages().size());

        //Verify that the company is on hold and the note was correctly saved
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        PayrollServices.commitUnitOfWork();
        assertTrue("Company is on hold", isCompanyOnHold);
        assertEquals("Number of notes", 0, numNotes);

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, "CBA2");
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 2);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", cbaDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", cbaDTO.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", cbaDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", cbaDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", cbaDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Add company 1 with a bank account
     * Terminate company 1
     * Add company 2
     * UPDATE THESE COMMENTS!!!!!!!!!!!
     * Add company 2 bank account with same acct num/routing num as company 1 ee1 bank account
     */
    public void addCompanyBankAccountDoesNotFailFraudControls() {
        Company1Dataloader c1dl = new Company1Dataloader();
        Company2Dataloader c2dl = new Company2Dataloader();
        DataLoader dataloader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = c1dl.persistCompany1();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = c2dl.getCompany1();
        company2.getContacts().iterator().next().setEmail("differentemail@aaa.net");
        Company domainCompany2 = dataloader.persistCompany(company2);
        dataloader.persistCompanyService(domainCompany2, c2dl.getCompany1Service());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companybankAccount2 = c2dl.getCompany1BankAccount();
        //same BA used by c1dl persistCompany for ee1
        BankAccountDTO ee1BA = c1dl.getEmployee1BankAccount().getBankAccount();
        ee1BA.setAccountNumber("888888888888");
        companybankAccount2.setBankAccountDTO(ee1BA);
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(SourceSystemCode.QBOE, company2.getCompanyId(), companybankAccount2, true, true);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Company bank account added", processResult);

        //Verify that the company is on hold and the note was correctly saved
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        boolean isCompanyOnHold = foundCompany.isCompanyOnHold();
        DomainEntitySet<CompanyNote> notes = foundCompany.getCompanyNoteCollection();
        int numNotes = notes.size();
        PayrollServices.commitUnitOfWork();
        assertFalse("Company is not on hold", isCompanyOnHold);
        assertEquals("Number of notes", 0, numNotes);

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company2.getCompanyId(), company2.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companybankAccount2.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companybankAccount2.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companybankAccount2.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companybankAccount2.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companybankAccount2.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void addCompanyBankAccountCompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();

        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();

        // Set invalid company

        ProcessResult processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), "InvalidCompanyId", companyBankAccountDTO, true, true);
        out.println(processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());


    }


    @Test
    /**
     *  Test error message 162 - BankAccount for company already exists
     */

    public void addCompanyBankAccountBankAccountAlreadyExists() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();

        //  Add valid company bank account
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);

        // validate that add was successful
        assertEquals("Number of Errors:", 0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Try to add the same company bank account

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "162", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for Company QBOE:123272727 already exists.",
                message.getMessage());


    }

    /**
     * An active CBA already exists with the specified source company bank account id, but with different bank account number
     * Test error message 162 - BankAccount for company already exists
     */
    @Test
    public void addCBASrcCBAAlreadyExistsWithDifferentAccountNumber() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.getBankAccountDTO().setAccountNumber("123000");

        //  Add valid company bank account
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);

        // validate that add was successful
        assertEquals("Number of Errors:", 0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Try to add the a company bank account with same source CBA id but with different a/c number
        BankAccountDTO bankAccountDTO = companyBankAccountDTO.getBankAccountDTO();
        bankAccountDTO.setAccountNumber("123400");
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "162", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for Company QBOE:123272727 already exists.",
                message.getMessage());


    }

    /**
     * Test error message 177 - Company is not active
     */
    @Test
    public void addCompanyBankAccountCompanyNotActive() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        out.println(processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        Assert.assertEquals(
                "The operation AddEmployerBankAccount is not allowed for company QBOE:123272727 in its current state.",
                message.getMessage());

    }

    @Test
    /**
     *  Test error message 255 - Invalid Routing Number specified
     */

    public void addCompanyBankAccounInvalidRoutingNumber() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();

        // Set invalid routing number
        BankAccountDTO invalidBankAccountDTO = companyBankAccountDTO.getBankAccountDTO();
        invalidBankAccountDTO.setRoutingNumber("invalidRN");
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        out.println(processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "255", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Invalid Routing Number invalidRN specified.", message.getMessage());


    }

    @Test
    /**
     *   Validate the creation of a new company bank account if an inactive company bank account already exists
     *   but with different account number.
     */

    public void addCBAInactiveBAAlreadyExistsWithDifferentBANumber() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.getBankAccountDTO().setAccountNumber("123000");

        //  Add valid company bank account
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        CompanyBankAccount companyBankAccount = processResult.getResult();

        // validate that add was successful
        assertEquals("Number of Errors First Account:", 0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Inactivate Company Bank Account
        // Get companybankaccount and set status to "Inactive"
        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(dataLoader.getSourceCompanyId(), dataLoader.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(dataLoader.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Add another company bank account with same source bank a/c id but with different bank a/c number
        BankAccountDTO bankAccountDTO = companyBankAccountDTO.getBankAccountDTO();
        String oldAccountNumber = bankAccountDTO.getAccountNumber();
        bankAccountDTO.setAccountNumber("123400");
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Result:", true, processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(dataLoader.getSourceCompanyId(), dataLoader.getSourceSystemCd());

        // verify company has two bank accounts one is active and the other is deactive
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = company.getCompanyBankAccountCollection();

        assertTrue("No of company bank accounts:", 2 == companyBankAccounts.size());

        com.intuit.sbd.payroll.psp.DomainEntitySet<CompanyBankAccount> cbas = company.getCompanyBankAccountCollection();
        cbas = cbas.sort(CompanyBankAccount.StatusCd());

        //Verify that Company Bank Account has the correct values
        companyBankAccount = cbas.get(0);
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.Inactive,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", oldAccountNumber,
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        companyBankAccount = cbas.get(1);
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    /**
     *   Validate the creation of a new company bank account if the transmitted one already
     *   exists but is inactive.
     */

    public void addCompanyBankAccountInactiveBankAccountAlreadyExists() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.getBankAccountDTO().setAccountNumber("123000");
        //  Add valid company bank account
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        CompanyBankAccount companyBankAccount = processResult.getResult();

        // validate that add was successful
        assertEquals("Number of Errors First Account:", 0, processResult.getMessages().size());
        // Commit
        PayrollServices.commitUnitOfWork();

        // Inactivate Company Bank Account
        // Get companybankaccount and set status to "Inactive"
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(dataLoader.getSourceCompanyId(), dataLoader.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(dataLoader.getSourceSystemCd(),
                company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);
        // Commit
        PayrollServices.commitUnitOfWork();

        // Add the same company bank account
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors Second Account:", 0, processResult.getMessages().size());

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        company = companies.get(0);
        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();

        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();

        //Make sure we have just 1 bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());

        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());
        assertEquals("Company Bank Account Status (Pending Verification):", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        //Ensure that the verification retry count was reset to zero
        assertEquals("Retry count:", 0L, companyBankAccount.getVerifyRetryCount());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     *   Validate the creation of a new company bank account if the transmitted one already
     *   exists but is inactive and has already had a failed verification attempt.
     */

    public void addCBAInactiveBAExistsWithRetries() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.getBankAccountDTO().setAccountNumber("123000");
        //  Add valid company bank account
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        CompanyBankAccount companyBankAccount = processResult.getResult();

        // validate that add was successful
        assertEquals("Number of Errors First Account:", 0, processResult.getMessages().size());
        // Commit
        PayrollServices.commitUnitOfWork();

        //PayrollServices.beginUnitOfWork();
        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());
        //PayrollServices.commitUnitOfWork();
        //Fail one retry attempt
        PayrollServices.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.addDaysToPSPTime(10);        
        Company company = Company.findCompany(dataLoader.getSourceCompanyId(), dataLoader.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();

        processResult = PayrollServices.companyManager.verifyCompanyBankAccount(dataLoader.getSourceSystemCd(),
                company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), new SpcfMoney("0"), new SpcfMoney("0"), false);
        CompanyBankAccount resultingCBA = processResult.getResult();
        // Commit
        PayrollServices.commitUnitOfWork();
        assertEquals("Verify retry count", 1L, resultingCBA.getVerifyRetryCount());

        //Deactivate the company bank account
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(dataLoader.getSourceCompanyId(), dataLoader.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();

        processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(dataLoader.getSourceSystemCd(),
                company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), false, false);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Add the same company bank account
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors Second Account:", 0, processResult.getMessages().size());

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        company = companies.get(0);
        companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();

        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();
        //Make sure we have just 1 bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        assertEquals("Company Bank Account Status (Pending Verification):",
                BankAccountStatus.PendingVerification, companyBankAccount.getStatusCd());
        //Ensure that the verification retry count was reset to zero
        assertEquals("Retry count:", 0L, companyBankAccount.getVerifyRetryCount());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
    }

    @Test
    public void testInvalidCompanyBankAccountDTO() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.setCompanyBankAccountID("ThereAreMoreThan50CharactersInSourceCompanyBankAccountId");
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
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
        // Load Data
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //  Test Null Bank Account DTO
        companyBankAccountDTO.setBankAccountDTO(null);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "142", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        // Test Invalid Routing Number
        PayrollServices.beginUnitOfWork();
        companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.getBankAccountDTO().setRoutingNumber("1234");
        processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "255", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Invalid Routing Number 1234 specified.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void testInvalidCompanyParameters() {

        // Load Data
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(null, dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
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
        processResult = PayrollServices.companyManager.addCompanyBankAccount(SourceSystemCode.QBOE, null, companyBankAccountDTO, true, true);
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
     *  Test error message 162 - BankAccount for company already exists
     */

    public void addMultipleCompanyBankAccounts() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();

        //  Add valid company bank account
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);

        // validate that add was successful
        assertEquals("Number of Errors:", 0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Try to add the same company bank account

        PayrollServices.beginUnitOfWork();
        companyBankAccountDTO.setCompanyBankAccountID("222222");
        processResult = PayrollServices.companyManager.addCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);

        // Commit
        PayrollServices.commitUnitOfWork();
        // validate that add was successful
        assertEquals("Number of Errors:", 0, processResult.getMessages().size());

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        company = companies.get(0);
        CompanyBankAccount companyBankAccount =
                CompanyBankAccount.findCompanyBankAccount(company, "222222");
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 2);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());
        assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
        assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
        assertEquals("Bank Account Number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                companyBankAccount.getBankAccount().getAccountNumber());
        assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                companyBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                companyBankAccount.getBankAccount().getBankName());
        assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                companyBankAccount.getSourceBankAccountName());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());

        //Verifty that CBA Verification amounts are between $0.01 and $0.99
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            assertEquals("FinancialTransaction Amount ",
                    financialTransaction.getFinancialTransactionAmount().compareTo(new SpcfMoney("1")), -1);
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addCompanyBankAccount_WhenCompanyOnHold() {
        //Add Company1
        PayrollServices.beginUnitOfWork();
        Company company1 = AddCompanyDataLoader.dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = AddCompanyDataLoader.dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //Add OnHold Reason to company1
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        //Add Company2
        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        company2.setCompanyId("48484848488");
        company2.setFein("847656466");
        DDServiceInfoDTO service2 = AddCompanyDataLoader.dataloader.getTestCompanyService();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company2);
        PayrollServices.commitUnitOfWork();

        assertTrue("Result " , result2.isSuccess());
        assertEquals(0, result2.getMessages().size());

        PayrollServices.beginUnitOfWork();
        //Ensure we can still add the service
        Company domainCompany2 = result2.getResult();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), service2);

        PayrollServices.commitUnitOfWork();
        assertSuccess(ddServiceAddProcessResult2);
        assertEquals(0, ddServiceAddProcessResult2.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany2 = Company.findCompany("48484848488", company1.getSourceSystemCd());
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany2, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyNote> notes = foundCompany2.getCompanyNoteCollection();
        int numNotes = notes.size();
        String noteContents = "";
        if (numNotes == 1) {
            Iterator<CompanyNote> itrNotes = notes.iterator();
            noteContents = itrNotes.next().getNotes();
        }
        boolean isCompanyOnHold = foundCompany2.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = foundCompany2.getOnHoldReasonCollection();
        junit.framework.Assert.assertEquals(0, onHoldReasons.size());
        OnHoldReason onHoldReason = null;
        PayrollServices.commitUnitOfWork();

        //Add CompanyBankAccount to Company2
        PayrollServices.beginUnitOfWork();
        Company2Dataloader c2dl = new Company2Dataloader();
        DataLoader dataloader = new DataLoader();        
        CompanyBankAccountDTO companybankAccount2 = c2dl.getCompany1BankAccount();
        //same BA used by c1dl persistCompany
        BankAccountDTO c1BA = dataloader.getTestCompanyBankAccount().getBankAccountDTO();
        companybankAccount2.setBankAccountDTO(c1BA);
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(SourceSystemCode.QBOE, domainCompany2.getSourceCompanyId(), companybankAccount2, true, true);
        PayrollServices.commitUnitOfWork();        
        assertSuccess("Company bank account added", processResult);
    }

    @Test
    public void testAddBankAccount_CancelAssistedAddDD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        CompanyService companyService = company.getService(ServiceCode.Cloud);
        assertEquals("cloud service active", ServiceSubStatusCode.ActiveCurrent, companyService.getStatusCd());

        companyService = company.getService(ServiceCode.Tax);
        assertEquals("tax service active", ServiceSubStatusCode.ActiveCurrent, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        // cancel tax
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax);
        PSP_PRAssert.assertSuccess("deactivate assisted service", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        companyService = company.getService(ServiceCode.Cloud);
        assertEquals("cloud service active", ServiceSubStatusCode.ActiveCurrent, companyService.getStatusCd());

        companyService = company.getService(ServiceCode.Tax);
        assertEquals("tax service cancelled", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addDDService(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        companyService = company.getService(ServiceCode.DirectDeposit);
        assertEquals("dd service pending bank verification", ServiceSubStatusCode.PendingFirstPayroll, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        companyService = company.getService(ServiceCode.Cloud);
        assertEquals("cloud service active", ServiceSubStatusCode.ActiveCurrent, companyService.getStatusCd());

        companyService = company.getService(ServiceCode.Tax);
        assertEquals("tax service cancelled", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());
        
        companyService = company.getService(ServiceCode.DirectDeposit);
        assertEquals("dd service pending bank verification", ServiceSubStatusCode.PendingFirstPayroll, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    /**
     * Successfully add a company bank account for TRON company.
     *
     * Random debits should not take place
     */
    public void addCompanyBankAccountSuccessfullyFromTRON() {
        try{
            setPayrollPluginContext();
            PayrollServices.beginUnitOfWork();
            // Load Data
            CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
            // Load Company Data
            CompanyBankAccountDataLoader.loadCompanyWithRealmId();
            PayrollServices.commitUnitOfWork();

            // Load CompanyBankAccount
            CompanyBankAccountDTO companyBankAccountDTO =
                    CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
            // Call process to add company bank account
            PayrollServices.beginUnitOfWork();
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                    .addCompanyBankAccount(SourceSystemCode.QBDT, dataLoader.getSourceCompanyId(),
                            companyBankAccountDTO, true, true);
            out.println(processResult);

            // Verify that no  validation errors have been returned
            assertEquals(0, processResult.getMessages().size());

            // Commit
            PayrollServices.commitUnitOfWork();

            // Verify that companybankaccount has been saved
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
            assertEquals(companies.size(), 1);
            Company company = companies.get(0);
            CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
            DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = PayrollServices.entityFinder.findById(Company.class, company.getId());
            companyBankAccount = PayrollServices.entityFinder
                    .findById(CompanyBankAccount.class, companyBankAccount.getId());

            // Ensure there is just one company bank account associated with the company
            assertEquals(company.getCompanyBankAccountCollection().size(), 1);

            //Verify that Company Bank Account has the correct values
            assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                    companyBankAccount.getSourceBankAccountId());
            assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                    companyBankAccount.getStatusCd());
            assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
            assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
            assertEquals("Bank Account Number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                    companyBankAccount.getBankAccount().getAccountNumber());
            assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                    companyBankAccount.getBankAccount().getRoutingNumber());
            assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                    companyBankAccount.getBankAccount().getBankName());
            assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                    companyBankAccount.getSourceBankAccountName());

            // Verify that 0 verification transactions have been created because bank activation is done through TRON
            assertEquals("Number of Verification Transactions: ", 0, verificationTransactions.size());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            out.println(e.getMessage());
        } finally {
            removePayrollPluginContext();
        }
    }

    @Test
    /**
     * Successfully add a company bank account for TRON company
     * which is reactivated.
     *
     * Random debits should not take place.
     */
    public void addReactivatedCompanyBankAccountSuccessfullyFromTRON() {
        try{
            setPayrollPluginContext();
            PayrollServices.beginUnitOfWork();
            // Load Data
            CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
            // Load Company Data
            CompanyBankAccountDataLoader.loadCompanyWithRealmId();
            PayrollServices.commitUnitOfWork();

            // Load CompanyBankAccount
            CompanyBankAccountDTO companyBankAccountDTO =
                    CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
            // Call process to add company bank account
            PayrollServices.beginUnitOfWork();
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                    .addCompanyBankAccount(SourceSystemCode.QBDT, dataLoader.getSourceCompanyId(),
                            companyBankAccountDTO, true, true);
            out.println(processResult);

            // Verify that no  validation errors have been returned
            assertEquals(0, processResult.getMessages().size());

            // Commit
            PayrollServices.commitUnitOfWork();

            // Verify that companybankaccount has been saved
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
            assertEquals(companies.size(), 1);
            Company company = companies.get(0);
            CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
            DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = PayrollServices.entityFinder.findById(Company.class, company.getId());

            // Ensure there is just one company bank account associated with the company
            assertEquals(company.getCompanyBankAccountCollection().size(), 1);

            // Verify that 0 verification transactions have been created because bank activation is done through TRON
            assertEquals("Number of Verification Transactions: ", 0, verificationTransactions.size());
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            // Cancel the company
            ProcessResult<CompanyService> processResult1 = PayrollServices.companyManager.updateServiceStatus
                    (SourceSystemCode.QBDT, dataLoader.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
            Assert.assertTrue(processResult1.isSuccess());
            Application.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            // Create a company with the same realmId
            company = CompanyBankAccountDataLoader.loadCompany1WithRealmId();
            PayrollServices.commitUnitOfWork();

            // Load CompanyBankAccount
            companyBankAccountDTO =
                    CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
            // Call process to add company bank account
            PayrollServices.beginUnitOfWork();
            processResult = PayrollServices.companyManager
                    .addCompanyBankAccount(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                            companyBankAccountDTO, true, true);
            out.println(processResult);

            // Verify that no  validation errors have been returned
            assertEquals(0, processResult.getMessages().size());

            // Commit
            PayrollServices.commitUnitOfWork();

            // Verify that companybankaccount has been saved
            PayrollServices.beginUnitOfWork();
            companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
            assertEquals(companies.size(), 2);
            // company = companies.get(0);
            company = PayrollServices.entityFinder.findById(Company.class, company.getId());
            companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
            verificationTransactions = companyBankAccount.getVerificationTransactions();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = PayrollServices.entityFinder.findById(Company.class, company.getId());
            companyBankAccount = PayrollServices.entityFinder
                    .findById(CompanyBankAccount.class, companyBankAccount.getId());

            // Ensure there is just one company bank account associated with the company
            assertEquals(company.getCompanyBankAccountCollection().size(), 1);

            // Verify that Company Bank Account has the correct values
            assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                    companyBankAccount.getSourceBankAccountId());
            assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                    companyBankAccount.getStatusCd());
            assertEquals("Total Retry Count:", 0L, companyBankAccount.getTotalRetryCount());
            assertEquals("Verify Retry Count:", 0L, companyBankAccount.getVerifyRetryCount());
            assertEquals("Bank Account Number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                    companyBankAccount.getBankAccount().getAccountNumber());
            assertEquals("Bank Routing Number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                    companyBankAccount.getBankAccount().getRoutingNumber());
            assertEquals("Bank Name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                    companyBankAccount.getBankAccount().getBankName());
            assertEquals("Source Bank Name", companyBankAccountDTO.getSourceBankAccountName(),
                    companyBankAccount.getSourceBankAccountName());
            assertEquals("Number of Verification Transactions: ", 0, verificationTransactions.size());
            PayrollServices.commitUnitOfWork();

        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            out.println(e.getMessage());
        } finally {
            removePayrollPluginContext();
        }
    }


    private void setPayrollPluginContext() {
        IntuitContext intuitContext = new IntuitContext();
        intuitContext.setAssetAlias(PAYROLL_PLUGIN_ASSET_ALIAS);
        RequestAttributesUtils.setAttribute(ContextConstants.INTUIT_CONTEXT, intuitContext);
    }

    private void removePayrollPluginContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.INTUIT_CONTEXT);
    }

}
