package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;


/**
 * Test suite for company bank accounts
 */

public class CompanyBankAccountBETests {
    private CompanyBankAccount companyBankAccount;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void validateFindCompanyBankAccountBySourceBankAccountId() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        loadData();
        companyBankAccount = new CompanyBankAccount();
        PayrollServices.commitUnitOfWork();

        companyBankAccount = getTestCompanyBankAccount();
        CompanyBankAccount findCompanyBankAccount;

        Company company = Company.findCompany("123456", SourceSystemCode.QBOE);

        findCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());
        assertNotNull(findCompanyBankAccount);

        companyBankAccount.setSourceBankAccountId("NOTFOUND");
        findCompanyBankAccount = CompanyBankAccount
                .findCompanyBankAccount(company, companyBankAccount.getSourceBankAccountId());
        assertNull(findCompanyBankAccount);
    }

    private CompanyBankAccount getTestCompanyBankAccount() {
        CompanyBankAccount companyBankAccount = new CompanyBankAccount();
        Company company = getTestCompany();
        companyBankAccount.setCompany(company);
        companyBankAccount.setBankAccount(getTestBankAccount());
        companyBankAccount.setSourceBankAccountId("123123");
        companyBankAccount.setSourceBankAccountName("ChartofAccountsName");
        return companyBankAccount;
    }

    private Company getTestCompany() {
        Company company = new Company();
        company.setDbaName("Dreams Come True, Inc.");
        company.setFedTaxId("123456788");
        company.setLegalName("Dreams Come True, Inc.");
        company.setSourceCompanyId("000027");
        company.setSourceSystemCd(SourceSystemCode.QBOE);
        return company;
    }

    private BankAccount getTestBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber("12345876");
        bankAccount.setAccountTypeCd(BankAccountType.Checking);
        bankAccount.setBankName("Bank of America");
        bankAccount.setRoutingNumber("263182914");
        return bankAccount;
    }

    private void evaluateMessage(Message messageToEvaluate, String pMessageCode, EntityName pEntityName) {
        assertEquals(pEntityName, messageToEvaluate.getEntityName());
        assertEquals(MessageInfo.MessageLevel.ERROR, messageToEvaluate.getLevel());
        assertEquals(pMessageCode, messageToEvaluate.getMessageCode());
    }

    private void evaluateMessage(Message messageToEvaluate, String pMessage, String pMessageCode,
                                 EntityName pEntityName) {
        evaluateMessage(messageToEvaluate, pMessageCode, pEntityName);
        assertEquals(pMessage, messageToEvaluate.getMessage());
    }


    @Test
    public void testUpdateBankAccountStatus() {
        //Setup
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        Company company1 = loadCompanyData();

        //Test
        Application.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyBankAccount foundCBA = CompanyBankAccount
                .findCompanyBankAccount(foundCompany, "123123");
        SpcfCalendar statusChangeDate = CalendarUtils.getPSPDateFromDB();
        SpcfCalendar rightBeforeStatusChange = PSPDate.getPSPTime();
        rightBeforeStatusChange.addSeconds(-1);
        foundCBA.updateBankAccountStatus(BankAccountStatus.Inactive);
        Application.commitUnitOfWork();

        //Persistence verification
        Application.beginUnitOfWork();
        EventTypeCode expectedEventTypeCd = EventTypeCode.CompanyBankAccountStatusChange;

        foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        foundCBA = CompanyBankAccount.findCompanyBankAccount(foundCompany, "123123");

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(foundCompany, expectedEventTypeCd, null, null, null);
        assertEquals("Company Events size", 2, companyEvents.size());


        CompanyEvent cbaStatusChange2 = companyEvents.get(0);
        CompanyEvent cbaStatusChange1 = companyEvents.get(1);
        if (cbaStatusChange2.getCompanyEventDetailValue(EventDetailTypeCode.OldBAStatus).equals(EnumUtils.getReadableName(BankAccountStatus.PendingVerification))) {
            cbaStatusChange1 = companyEvents.get(0);
            cbaStatusChange2 = companyEvents.get(1);
        }

        assertEquals("Event type", expectedEventTypeCd, cbaStatusChange1.getEventTypeCd());
        SpcfCalendar eventTimeStamp = cbaStatusChange1.getEventTimeStamp();
        if (eventTimeStamp.isUTC()) {
            eventTimeStamp = eventTimeStamp.toLocal();
        }
        assertEquals("Event timestamp", statusChangeDate.getDayOfYear(), eventTimeStamp.getDayOfYear());
        CompanyBankAccount eventCBA = PayrollServices.entityFinder.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaStatusChange1.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId)));
        assertEquals("Company bank account", foundCBA, eventCBA);
        assertEquals("Old status", EnumUtils.getReadableName(BankAccountStatus.PendingVerification), cbaStatusChange1.getCompanyEventDetailValue(EventDetailTypeCode.OldBAStatus));
        assertEquals("New status", BankAccountStatus.Active.toString(), cbaStatusChange1.getCompanyEventDetailValue(EventDetailTypeCode.NewBAStatus));
        assertEquals("Company", foundCompany, cbaStatusChange1.getCompany());

        assertEquals("Company Bank account status", BankAccountStatus.Inactive, foundCBA.getStatusCd());

        assertEquals("Event type", expectedEventTypeCd, cbaStatusChange2.getEventTypeCd());
        eventTimeStamp = cbaStatusChange2.getEventTimeStamp();
        if (eventTimeStamp.isUTC()) {
            eventTimeStamp = eventTimeStamp.toLocal();
        }
        assertEquals("Event timestamp", statusChangeDate.getDayOfYear(), eventTimeStamp.getDayOfYear());
        eventCBA = PayrollServices.entityFinder.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(cbaStatusChange2.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId)));

        assertEquals("Company bank account", foundCBA, eventCBA);
        assertEquals("Old status", BankAccountStatus.Active.toString(), cbaStatusChange2.getCompanyEventDetailValue(EventDetailTypeCode.OldBAStatus));
        assertEquals("New status", BankAccountStatus.Inactive.toString(), cbaStatusChange2.getCompanyEventDetailValue(EventDetailTypeCode.NewBAStatus));
        assertEquals("Company", foundCompany, cbaStatusChange2.getCompany());

        assertEquals("Company Bank account status", BankAccountStatus.Inactive, foundCBA.getStatusCd());
        Application.commitUnitOfWork();
    }

    @Test
    public void findVerificationTransactions() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2008, 2, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        CompanyBankAccount foundCBA = CompanyBankAccount
                .findCompanyBankAccount(company, "123123");

        DomainEntitySet<FinancialTransaction> finTxnList = foundCBA.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Financial Transactions:", 2, finTxnList.size());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2008, 3, 20, SpcfTimeZone.getLocalTimeZone()));
        finTxnList = foundCBA.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Financial Transactions:", 2, finTxnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2008, 3, 3, SpcfTimeZone.getLocalTimeZone()));
        finTxnList = foundCBA.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Financial Transactions:", 2, finTxnList.size());
    }


    private static void loadData() {
        DataLoader dataLoader = new DataLoader();
        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestIntuitCompany();
        Application.save(company);

        dataLoader.persistTestCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        // Create Company Service - Direct Deposit
//		dataLoader.persistTestCompanyService(company);
    }

    private static Company loadCompanyData() {
        Application.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        Company company = dataloader.persistCompany(c1dl.getCompany1());

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c1dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        assertSuccess("verifyCompanyBankAccount", processResult);
        Employee employee1 = c1dl.persistEmployee(c1dl.getEmployee1(company));
        Employee employee2 = c1dl.persistEmployee(c1dl.getEmployee2(company));
        company = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        employee2 = Employee.findEmployee(company, "EE2");

        EmployeeBankAccount eeba2 = c1dl.persistEEBA(company, employee2, c1dl.getEmployee2BankAccount(employee2));

        company = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        employee1 = Employee.findEmployee(company, "EE1");

        EmployeeBankAccount eeba1 = c1dl.persistEEBA(company, employee1, c1dl.getEmployee1BankAccount());

        company = Company
                .findCompany("1234567", SourceSystemCode.QBOE);


        Application.commitUnitOfWork();
        return company;
    }

    @Test
    public void testCompanyEventsForCBA() {

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        Company company = dataloader.persistCompany(c1dl.getCompany1());
        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c1dl.getCompany1Service());

        // TestCase No 1
        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true);

        // Verify that no  validation errors have been returned
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount cba1 = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Verify that Event is created in Company_Event table
        DomainEntitySet<CompanyEvent> compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyBankAccountChange);
        Assert.assertTrue("Number of First CBA events", 1 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        DomainEntitySet<CompanyEventDetail> compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.CompanyBankAccountChange, EventDetailTypeCode.CompanyBankAccountId, cba1.getId().getStandardFormatString());
        assertTrue("Number of new CBA change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.CompanyBankAccountChange, EventDetailTypeCode.OldCompanyBankAccountId);
        assertTrue("Number of Old CBA change events", 1 == compEventDetails.size());
        PayrollServices.rollbackUnitOfWork();

        // TestCase No 2
        /* Same Source_ID with diff bank acc.
        * Exp o/p : Deactivate other CBA & Activate this CBA */
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult prChangeCBA = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                            company.getSourceCompanyId(), companyBankAccountDTO, false, true, true);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess(prChangeCBA);

        String oldID = cba1.getId().getStandardFormatString();
        CompanyBankAccount cbaNew = CompanyBankAccount.findCompanyBankAccount(company,
                                                companyBankAccountDTO.getCompanyBankAccountID());

        // Verify that Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyBankAccountChange);
        Assert.assertTrue("Total Number of total CBA change events", 2 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.CompanyBankAccountChange, EventDetailTypeCode.CompanyBankAccountId, cbaNew.getId().getStandardFormatString());
        assertTrue("Number of New change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.CompanyBankAccountChange, EventDetailTypeCode.OldCompanyBankAccountId, oldID);
        assertTrue("Number of Old CBA change events", 1 == compEventDetails.size());

        // TestCase No 3
        /* Diff Source_ID with diff bank acc via changeCompanyBankAccount()
         * Exp o/p : Should throw CBA record doesn't exists error */
        PayrollServices.beginUnitOfWork();
        companyBankAccountDTO.setCompanyBankAccountID("12345");
        prChangeCBA = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                      company.getSourceCompanyId(), companyBankAccountDTO, false, true, true);
        assertFalse(prChangeCBA.isSuccess());

        // Verify that no New Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyBankAccountChange);
        Assert.assertTrue("Total Number of CBA Change events", 2 == compEvents.size());

        PayrollServices.rollbackUnitOfWork();


        // TestCase No 4
        /* Diff Source_ID with diff bank acc via addCompanyBankAccount()
         * Exp o/p : Deactivate other CBA & Activate this CBA */
        PayrollServices.beginUnitOfWork();
        oldID = cbaNew.getId().getStandardFormatString();
        companyBankAccountDTO.setCompanyBankAccountID("12345");
        prChangeCBA = PayrollServices.companyManager.addCompanyBankAccount(SourceSystemCode.QBOE,
                                      company.getSourceCompanyId(), companyBankAccountDTO, false, false);
        PayrollServices.commitUnitOfWork();

        // Verify that the new CBA created is the only one with Active status
        cbaNew = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccountDTO.getCompanyBankAccountID());
        assertEquals(cbaNew, CompanyBankAccount.findActiveCompanyBankAccount(company));

        // verify the two bank accounts are not equal
        assertFalse("BankAccounts", cba1.getBankAccount().equals(cbaNew.getBankAccount()));

        // Verify that Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyBankAccountChange);
        Assert.assertTrue("Total Number of total CBA change events", 3 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.CompanyBankAccountChange, EventDetailTypeCode.
                        CompanyBankAccountId, cbaNew.getId().getStandardFormatString());
        assertTrue("Number of New change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.CompanyBankAccountChange, EventDetailTypeCode.OldCompanyBankAccountId, oldID);
        assertTrue("Number of Old CBA change events", 1 == compEventDetails.size());


    }

}
