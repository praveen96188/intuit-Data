package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.workflows.WorkflowPackager;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.hibernate.ScrollableResults;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.OptimisticLockException;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Contains the unit tests for the <CODE>CompanyBE</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 18, 2007
 */

public class CompanyBETests {

    private Company company;
    private DataLoader dataloader;

    private String EXPECTED_FRAUD_NOTES =
            "This company was not activated because one or more fields match the company,  Intuit(Source System=QBOE Source ID=123456) with status of Terminated.  The list of fields matched are as follows:\n" +
                    "FED TAX ID: 123456789\n";
    public static SpcfLogger logger = SpcfLogManager.getLogger(CompanyBETests.class);

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.commitUnitOfWork();
        company = new Company();
        dataloader = new DataLoader();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFindCompaniesBySubStatusOnHold() {
        ACHReturnsDataLoader.loadData2DayERNSFs();
        PayrollServices.beginUnitOfWork();

        ArrayList<ServiceSubStatusCode> subStatusStrings = new ArrayList<ServiceSubStatusCode>();
        subStatusStrings.add(ServiceSubStatusCode.AchRejectR1R9);
        subStatusStrings.add(ServiceSubStatusCode.AchRejectOther);

        DomainEntitySet<Company> companies = Company.findCompaniesByOnHoldSubStatus(subStatusStrings, null, false,0,50);
        assertEquals("One company matches", 1, companies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindCompaniesBySubStatus() {
        PayrollServices.beginUnitOfWork();
        CompanyQB1DataLoader dl = new CompanyQB1DataLoader();
        dl.persistQBCompany1NoVerify("3838383");
        ArrayList<ServiceSubStatusCode> subStatusStrings = new ArrayList<ServiceSubStatusCode>();
        subStatusStrings.add(ServiceSubStatusCode.AchRejectOther);
        subStatusStrings.add(ServiceSubStatusCode.PendingBankVerification);

        DomainEntitySet<Company> companies = Company.findCompaniesByPendingSubStatus(subStatusStrings, null, false, 0, 50);
        assertEquals("One company matches", 1, companies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void accountSignatoryTrue() {
        Application.beginUnitOfWork();
        assertTrue(getTestIntuitCompany().hasAccountSignatoryContact());
        Application.commitUnitOfWork();
    }

    @Test
    public void accountSignatoryFalse() {
        Application.beginUnitOfWork();
        assertFalse(company.hasAccountSignatoryContact());
        Application.commitUnitOfWork();
    }

    @Test
    public void findCompanyBySourceIdFindNone() {
        Application.beginUnitOfWork();
        Company testCompany = getTestIntuitCompany();

        company = Company.findCompany(testCompany.getSourceCompanyId() + "a", testCompany.getSourceSystemCd());

        assertNull(company);
        Application.commitUnitOfWork();
    }

    @Test
    public void findNoSimilarCompaniesValidCompany() {
        Application.beginUnitOfWork();
        StringBuilder similarNotes = new StringBuilder();
        assertFalse(getTestIntuitCompany().companyMeetsFraudCriteria(similarNotes));
        Application.commitUnitOfWork();
    }

    @Test
    public void findCompanyMatchesFraudCriteria() {
        //Load a company that matches getTestIntutiFraudCompany()
        Application.beginUnitOfWork();
        company = dataloader.persistTestIntuitCompany();
        CompanyService service = dataloader.persistTestCompanyService(company);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Application.commitUnitOfWork();

        //Start txn
        Application.beginUnitOfWork();


        StringBuilder fraudNotes = new StringBuilder();
        boolean bMeetsCriteria = getTestIntuitFraudCompany().companyMeetsFraudCriteria(fraudNotes);
        assertTrue(bMeetsCriteria);
        assertEquals(EXPECTED_FRAUD_NOTES, fraudNotes.toString());

        //Commit the txn and unload the data
        Application.commitUnitOfWork();
    }

    @Ignore
    @Test
    public void findCompanyMatchesFraudMixedCaseCriteria() {
        //Load a company that matches getTestIntutiFraudCompany()
        Application.rollbackUnitOfWork();
        Application.beginUnitOfWork();
        company = dataloader.persistTestIntuitCompany();
        CompanyService service = dataloader.persistTestCompanyService(company);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Application.commitUnitOfWork();

        //Start txn
        Application.beginUnitOfWork();

        StringBuilder fraudNotes = new StringBuilder();
        boolean bMeetsCriteria = getTestIntuitSimilarFraudCompany().companyMeetsFraudCriteria(fraudNotes);
        assertTrue(bMeetsCriteria);

        String expectedFraudNotes = "This company was not activated because one or more fields match the company,  Intuit(Source System=QBOE Source ID=123456) with status of Terminated.  The list of fields matched are as follows:\n" +
                "CONTACT EMAIL:paYroLLadMin2@EMAIL.com";
        assertEquals(expectedFraudNotes, fraudNotes.toString().trim());

        //Commit the txn and unload the data
        Application.commitUnitOfWork();
    }

    @Test
    public void findSimilarFraudCompaniesMatchesCriteria() {
        Application.beginUnitOfWork();
        company = dataloader.persistTestIntuitCompany();
        CompanyService service = dataloader.persistTestCompanyService(company);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SortedMap matchedCompanyMap = new TreeMap();
        SortedMap companyFieldMatches = new TreeMap();

        getTestIntuitFraudCompany().findSimilarFraudCompanies(matchedCompanyMap, companyFieldMatches);

        assertEquals(1, matchedCompanyMap.size());
        assertEquals(1, companyFieldMatches.size());

        Application.commitUnitOfWork();

    }

    @Test
    public void findSimilarFraudAddressesMatchesCriteria() {
        Application.beginUnitOfWork();
        company = dataloader.persistTestIntuitCompany();
        CompanyService service = dataloader.persistTestCompanyService(company);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        SortedMap matchedCompanyMap = new TreeMap();
        SortedMap companyFieldMatches = new TreeMap();

        getTestIntuitFraudCompany().findSimilarFraudAddresses(matchedCompanyMap, companyFieldMatches);

        assertEquals(1, matchedCompanyMap.size());
        assertEquals(1, companyFieldMatches.size());
        Application.commitUnitOfWork();
    }

    @Test
    public void findSimilarFraudContactsMatchesCriteriaPhone() {
        Application.beginUnitOfWork();
        company = dataloader.persistTestIntuitCompany();
        CompanyService service = dataloader.persistTestCompanyService(company);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        SortedMap matchedCompanyMap = new TreeMap();
        SortedMap companyFieldMatches = new TreeMap();

        Company testCompany = getTestIntuitFraudCompany();
        testCompany.getContactCollection().iterator().next().setPhone("(775) 333-3333");

        testCompany.findSimilarFraudContacts(matchedCompanyMap, companyFieldMatches);

        assertEquals(1, matchedCompanyMap.size());
        assertEquals(1, companyFieldMatches.size());

        Application.commitUnitOfWork();
    }

    @Test
    public void findNoSimilarCompaniesInvalidCompany() {
        Application.beginUnitOfWork();
        StringBuilder similarNotes = new StringBuilder();
        assertFalse(company.companyMeetsFraudCriteria(similarNotes));
        Application.commitUnitOfWork();
    }

    @Test
    public void testExpectedReversalDate() {
        Application.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();
        CompanyService service = dataloader.persistTestCompanyService(company);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SpcfCalendar reversalDate = company.getExpectedReversalDate();
        Application.commitUnitOfWork();
        assertEquals("Reversal Expected Date", reversalDate, SpcfCalendar.createInstance(2007, SpcfCalendar.September, 11, SpcfTimeZone.getLocalTimeZone()));


    }


    private Company getTestIntuitFraudCompany() {
        Company company = new Company();

        company.setDbaName("Intuit");
        company.setFedTaxId("123456789");
        company.setLegalAddress(getTestLegalAddress());
        company.setLegalName("Intuit");
        company.setMailingAddress(getTestMailingAddress());
        company.setNotificationEmail("notifications@intuit.com");
        company.setSourceCompanyId("123456");
        company.addContact(getTestFraudContact());
        company.setSourceSystemCd(SourceSystemCode.QBOE);

        return company;
    }

    private Company getTestIntuitSimilarFraudCompany() {
        Company company = new Company();

        company.setDbaName("Sean Test");
        company.setFedTaxId("123456700");
        company.setLegalAddress(getTestLegalAddress());
        company.getLegalAddress().setAddressLine1("test street 1");
        company.setLegalName("Sean Test");
        company.setMailingAddress(getTestMailingAddress());
        company.getMailingAddress().setAddressLine1("test street 1");
        company.setNotificationEmail("bah@intuit.com");
        company.setSourceCompanyId("654321");
        company.addContact(getTestFraudSimilarContact());
        company.setSourceSystemCd(SourceSystemCode.QBOE);

        return company;
    }

    private Company getTestIntuitCompany() {
        Company company = new Company();
        company.setDbaName("Intuit1");
        company.setFedTaxId("123456788");
        company.setLegalAddress(getTestLegalAddress());
        company.setLegalName("Intuit1");
        company.setMailingAddress(getTestMailingAddress());
        company.setNotificationEmail("notifications1@intuit.com");
        company.setSourceCompanyId("1234567");
        company.addContact(getTestContact());
        company.getContactCollection().get(0).setCompany(company);
        company.setSourceSystemCd(SourceSystemCode.QBOE);

        FundingModel fiveDayFunding = Application.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);
        company.setFundingModel(fiveDayFunding);

        return company;
    }

    private Address getTestLegalAddress() {
        Address legalAddress = new Address();
        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");
        return legalAddress;
    }

    private Address getTestMailingAddress() {
        Address mailingAddress = new Address();
        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 45");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89521");
        mailingAddress.setState("NV");
        return mailingAddress;
    }

    private Contact getTestContact() {
        Address contactAddr = new Address();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");

        Contact contact = new Contact();
        contact.setFirstName("Johnny");
        contact.setMiddleName("P");
        contact.setLastName("Doe");
        contact.setPhone("(775) 424-8439");
        contact.setGenderCd(Gender.Male);
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAuthSignerYnInd(Boolean.TRUE);
        contact.setMailingAddress(contactAddr);
        contact.setSourceContactId(ContactRole.PayrollAdmin.toString() + contact.getLastName() + contact.getFirstName() + contact.getMiddleName());

        return contact;
    }

    private Contact getTestFraudContact() {
        Address contactAddr = new Address();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");

        Contact contact = new Contact();
        contact.setFirstName("Steve");
        contact.setMiddleName("P");
        contact.setLastName("PayrollAdmin2");
        contact.setPhone("(775) 493-3333");
        contact.setGenderCd(Gender.Male);
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAuthSignerYnInd(Boolean.TRUE);
        contact.setMailingAddress(contactAddr);
        contact.setSourceContactId(ContactRole.PayrollAdmin.toString() + contact.getLastName() + contact.getFirstName() + contact.getMiddleName());

        return contact;
    }

    private Contact getTestFraudSimilarContact() {
        Address contactAddr = new Address();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");

        Contact contact = new Contact();
        contact.setFirstName("Max");
        contact.setMiddleName("P");
        contact.setLastName("Pain");
        contact.setEmail("paYroLLadMin2@EMAIL.com");
        contact.setPhone("(775) 333-4444");
        contact.setGenderCd(Gender.Male);
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAuthSignerYnInd(Boolean.TRUE);
        contact.setMailingAddress(contactAddr);
        contact.setSourceContactId(ContactRole.PayrollAdmin.toString() + contact.getLastName() + contact.getFirstName() + contact.getMiddleName());

        return contact;
    }

    @Test
    public void test22() {

        Expression ex2 = new Query<Employee>()
                .Where(Employee.Company().LegalName().like("ABC%")
                        .And(Employee.Email().isNull())
                        .And(
                                Employee.Company().NumberOfFailedLoginAttempts().greaterOrEqualThan(3)
                                        .Or(Employee.Phone().isNotNull())
                        )
                        .Or(Employee.MailingAddress().City().in("Tampa", "Reno"))
                )
                .OrderBy(Employee.FirstName(), Employee.LastName().Descending(), Employee.Company().LegalName());

        ex2 = new Query<Employee>()
                .Where(Employee.Company().LegalName().like("ABC%")
                        .Or(Employee.Email().isNull())
                        .Or(Employee.Company().NumberOfFailedLoginAttempts().greaterOrEqualThan(3)
                        .And(Employee.MailingAddress().City().in("Tampa", "Reno")))
                )
                .OrderBy(Employee.FirstName(), Employee.LastName().Descending(), Employee.Company().LegalName());

        DomainEntitySet<Employee> results = PayrollServices.entityFinder.find(Employee.class, ex2);

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);

        Criterion<PaycheckSplit> where = PaycheckSplit.Paycheck().PayrollRun().Company().equalTo(company);
        DomainEntitySet<PaycheckSplit> paycheckSplits = Application.find(PaycheckSplit.class, where);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCompanyAllowedCapability() {
        Application.beginUnitOfWork();
        company = dataloader.persistTestIntuitCompany();
        CompanyService service = dataloader.persistTestCompanyService(company);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    company.getSourceCompanyId(),
                                                                                    ServiceSubStatusCode.PendingTermination);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("Add Onhold Reason : PendingTermination ", result);

        //Update Company Info
        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = dataloader.getTestIntuitCompany2();
        company2.setSourceSystemCd(company.getSourceSystemCd());
        company2.setCompanyId(company.getSourceCompanyId());
        company2.setFein("384757575");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company
                .findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result2.getMessages().size());
        Message errorMessage = result2.getMessages().get(0);
        assertEquals("Error message code", "1101", errorMessage.getMessageCode());
        assertEquals("Error message", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456 in its current state.",
                errorMessage.getMessage());
    }

    @Test
    public void updateCompanyTwoThreadsDifferentProperties() {
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        Company company1 = dataloader.persistTestIntuitCompany();
        final SpcfUniqueId companyId = company1.getId();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        company1 = Application.refresh(company1);
        company1.setLegalName(company1.getLegalName() + "a");

        // Force an update in another thread
        PayrollServices.executeTransactionThread(new TransactionThread() {
            public ProcessResult transaction() {
                Company localCompany = Application.findById(Company.class, companyId);
                localCompany.setDbaName(localCompany.getDbaName() + "ab");
                Application.setCurrentPrincipal(new PspPrincipal("SeparateThread", "SeparateThread"));
                Application.save(localCompany);
                return new ProcessResult();
            }
        });

        PayrollServices.commitUnitOfWork();
    }

    @Test(expected= OptimisticLockException.class)
    public void updateCompanyTwoThreadsSameProperties() {
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        Company company1 = dataloader.persistTestIntuitCompany();
        final SpcfUniqueId companyId = company1.getId();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        company1 = Application.refresh(company1);

        company1.setLegalName(company1.getLegalName() + "a");

        // Force an update in another thread
        PayrollServices.executeTransactionThread(new TransactionThread() {
            public ProcessResult transaction() {
                Company localCompany = Application.findById(Company.class, companyId);
                localCompany.setLegalName(localCompany.getLegalName() + "a");
                Application.save(localCompany);
                return new ProcessResult();
            }
        });

        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testGetCurrentStrikes() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 3, 25));
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        Company strikeCompany = dataloader.persistTestIntuitCompany();
        SpcfUniqueId companyId = strikeCompany.getId();
        dataloader.persistTestCompanyService(strikeCompany);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        strikeCompany = Application.findById(Company.class, companyId);
        strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", PSPDate.getPSPTime(), (FinancialTransaction)null);
        assertEquals("strike count", 1, strikeCompany.getCurrentStrikeEvents().size());

        // current means w/in last 12 months, make sure not included
        SpcfCalendar cal = PSPDate.getPSPTime();
        cal.setValues(cal.getYear() - 1, cal.getMonth() - 1, cal.getDay());
        strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", cal, (FinancialTransaction)null);
        assertEquals("strike count", 1, strikeCompany.getCurrentStrikeEvents().size());

        // verify that even though they have same type and event timestamp, the ID keeps them unique
        cal = PSPDate.getPSPTime();
        CompanyEvent strike1 = strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", cal, (FinancialTransaction)null);
        CompanyEvent strike2 = strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", cal, (FinancialTransaction)null);
        strike2.setEventTimeStamp(strike1.getEventTimeStamp());
        assertEquals("strike count", 3, strikeCompany.getCurrentStrikeEvents().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        strikeCompany = Application.findById(Company.class, companyId);
        assertEquals("strike count", 3, strikeCompany.getCurrentStrikeEvents().size());

        strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", PSPDate.getPSPTime(), (FinancialTransaction)null);
        assertEquals("strike count", 4, strikeCompany.getCurrentStrikeEvents().size());
        PayrollServices.rollbackUnitOfWork();

        //--------------------------------------
        // now repeat tests w/flush mode MANUAL
        //--------------------------------------
        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

        strikeCompany = dataloader.persistTestIntuitCompany();
        companyId = strikeCompany.getId();
        dataloader.persistTestCompanyService(strikeCompany);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        strikeCompany = Application.findById(Company.class, companyId);
        strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", PSPDate.getPSPTime(), (FinancialTransaction)null);
        assertEquals("strike count", 1, strikeCompany.getCurrentStrikeEvents().size());

        // current means w/in last 12 months, make sure not included
        cal = PSPDate.getPSPTime();
        cal.setValues(cal.getYear() - 1, cal.getMonth() - 1, cal.getDay());
        strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", cal, (FinancialTransaction)null);
        assertEquals("strike count", 1, strikeCompany.getCurrentStrikeEvents().size());

        // verify that even though they have same type and event timestamp, the ID keeps them unique
        cal = PSPDate.getPSPTime();
        strike1 = strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", cal, (FinancialTransaction)null);
        strike2 = strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", cal, (FinancialTransaction)null);
        strike2.setEventTimeStamp(strike1.getEventTimeStamp());
        assertEquals("strike count", 3, strikeCompany.getCurrentStrikeEvents().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        strikeCompany = Application.findById(Company.class, companyId);
        assertEquals("strike count", 3, strikeCompany.getCurrentStrikeEvents().size());

        strikeCompany.addStrikeEvent(StrikeReason.Manual, "Test1", PSPDate.getPSPTime(), (FinancialTransaction)null);
        assertEquals("strike count", 4, strikeCompany.getCurrentStrikeEvents().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testGetPayrollCount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2012, 1, 10);
        // NOTE - use DLP, instead of DLS
        Company tCompany = DataLoadPalette.setupTaxCompany();

        // verify the new company
        PayrollServices.beginUnitOfWork();
        Application.refresh(tCompany);
        assertEquals("PayrollCount", 0, tCompany.getPayrollCount());
        PayrollServices.rollbackUnitOfWork();

        // run payroll
        DataLoadPalette.runSimpleTaxPayroll(tCompany, new DateDTO("2012-01-13"));
        PayrollServices.beginUnitOfWork();
        Application.refresh(tCompany);
        assertEquals("PayrollCount", 1, tCompany.getPayrollCount());
        PayrollServices.rollbackUnitOfWork();

        // run payroll once more
        DataLoadPalette.runSimpleTaxPayroll(tCompany, new DateDTO("2012-01-20"));
        PayrollServices.beginUnitOfWork();
        Application.refresh(tCompany);
        assertEquals("PayrollCount", 2, tCompany.getPayrollCount());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testNegativePaycheckIdsNotIncludedInPayrollCount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2012, 1, 10);
        // NOTE - use DLP, instead of DLS
        Company tCompany = DataLoadPalette.setupTaxCompany();

        // verify the new company
        PayrollServices.beginUnitOfWork();
        Application.refresh(tCompany);
        assertEquals("PayrollCount", 0, tCompany.getPayrollCount());
        PayrollServices.rollbackUnitOfWork();

        // run payroll
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(tCompany, new DateDTO("2012-01-13"));
        Application.beginUnitOfWork();
        Application.refresh(tCompany);
        assertEquals("PayrollCount", 1, tCompany.getPayrollCount());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        int i = 1;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            Application.refresh(paycheck);
            paycheck.setSourcePaycheckId("-" + i);
            i++;
            Application.save(paycheck);
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(tCompany);
        assertEquals("PayrollCount", 0, tCompany.getPayrollCount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNameControl(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud );
        assertNotNull(company.getNameControl());
        assertEquals("TEST", company.getNameControl());
    }

    @Test
    public void setOIIFlagTest() throws Exception {
        String psid = "123456789";
        String workflowFlagValues = "00000";

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setOIIFlag(workflowFlagValues);
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        WorkflowPackager workFlowFlagValuesForComp = company.getWorkFlowPackager();

        assertEquals(WorkflowState.ENABLED, workFlowFlagValuesForComp.getWorkflowState(Workflows.OII));
        assertEquals(true, company.isOIIEnabled());

        Application.commitUnitOfWork();
    }

    @Test
    public void setTronFlagTest() throws Exception {
        String psid = "123456789";
        String workflowFlagValues = "00000";

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setOIIFlag(workflowFlagValues);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        WorkflowPackager workFlowFlagValuesForComp = company.getWorkFlowPackager();

        assertEquals(WorkflowState.ENABLED, workFlowFlagValuesForComp.getWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING));
        assertEquals(true, company.isMoneyMovementOnboardingEnabled());

        Application.commitUnitOfWork();
    }

    @Test
    public void enableAllWorkFlows() throws Exception {
        String psid = "123456789";
        String workflowFlagValues = "11211";

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();

        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setOIIFlag(workflowFlagValues);
        WorkflowPackager workFlowFlagValuesForComp = company.getWorkFlowPackager();

        assertEquals(WorkflowState.ENABLED, workFlowFlagValuesForComp.getWorkflowState(Workflows.OII));
        assertEquals(WorkflowState.ENABLED, workFlowFlagValuesForComp.getWorkflowState(Workflows.ADD_EIN));
        assertEquals(WorkflowState.ENABLE_AUTHORIZATION, workFlowFlagValuesForComp.getWorkflowState(Workflows.PURCHASE_PAYROLL));
        assertEquals(WorkflowState.ENABLED, workFlowFlagValuesForComp.getWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT));
        assertEquals(WorkflowState.ENABLED, workFlowFlagValuesForComp.getWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING));

        Application.commitUnitOfWork();
    }

    @Test
    public void setOIIFlagTestForextraPlace() throws Exception {
        String psid = "123456789";
        String workflowFlagValues = "00";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);
        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setOIIFlag(workflowFlagValues);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        WorkflowPackager workFlowFlagValuesForComp = company.getWorkFlowPackager();
        assertEquals(WorkflowState.DISABLED, workFlowFlagValuesForComp.getWorkflowState(Workflows.OII));
        assertEquals(false, company.isOIIEnabled());
        Application.commitUnitOfWork();
    }

    @Test
    public void validateCompanyLookupByNonAuthCriteria(){
        String firstName = "johnny";
        String lastName = "primaryprincipal";
        String ssn = "616306726";
        String email = "PrimaryPrincipal@aol.com";
        String phone = "(775) 111-1111";
        String sourceSystemCode = SourceSystemCode.QBDT.name();
        String contactRole = ContactRole.PrimaryPrincipal.name();

        String psid = "123456789";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company assistedCompany = psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Contact contactByRoleCode = assistedCompany.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        contactByRoleCode.setSocialSecurityNumber(ssn);
        Application.commitUnitOfWork();

        // Happypath
        List<Company> companyByNonAuthCriteria = Company.findCompanyByNonAuthCriteria(ssn, firstName, lastName,sourceSystemCode, contactRole, email, phone);

        assertNotNull("Obtained a null response", companyByNonAuthCriteria);
        assertEquals("Number of companies in the search do not match", 1, companyByNonAuthCriteria.size());

        companyByNonAuthCriteria.stream().forEach(company -> System.out.println(company.getSourceCompanyId()));

        //Phone number format change
        phone = "775 111-1111";
        companyByNonAuthCriteria = Company.findCompanyByNonAuthCriteria(ssn, firstName, lastName,sourceSystemCode, contactRole, email, phone);

        assertNotNull("Obtained a null response", companyByNonAuthCriteria);
        assertEquals("Number of companies in the search do not match", 1, companyByNonAuthCriteria.size());

        //formatted ssn
        ssn = "61-630-6726";
        companyByNonAuthCriteria = Company.findCompanyByNonAuthCriteria(ssn, firstName, lastName,sourceSystemCode, contactRole, email, phone);

        assertNotNull("Obtained a null response", companyByNonAuthCriteria);
        assertEquals("Number of companies in the search do not match", 1, companyByNonAuthCriteria.size());

        //One of the criteria no matching
        ssn = "7751111111";
        companyByNonAuthCriteria = Company.findCompanyByNonAuthCriteria(ssn, firstName, lastName,sourceSystemCode, contactRole, email, phone);

        assertNotNull("Obtained a null response", companyByNonAuthCriteria);
        assertEquals("Number of companies in the search do not match", 0, companyByNonAuthCriteria.size());

        //None of the criteria match
        firstName = "firstName";
        lastName = "lastName";
        ssn = "99099092";
        email = "email@mail.com";
        companyByNonAuthCriteria = Company.findCompanyByNonAuthCriteria(ssn, firstName, lastName,sourceSystemCode, contactRole, email, phone);

        assertNotNull("Obtained a null response", companyByNonAuthCriteria);
        assertEquals("Number of companies in the search do not match", 0, companyByNonAuthCriteria.size());


    }

    @Test
    public void testGetAllCompaniesByRealm() {
        logger.info("DG_DISCOVERABILITY_FEATURE testGetAllCompaniesByRealm started");
        String psid = "625001001";
        String realmId = "9130354161103686";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);

        company.setIAMRealmId(realmId);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.getAllCompaniesByRealm(realmId);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(realmId, companySetPreDelete.get(0).getIAMRealmId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.getAllCompaniesByRealm(realmId);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testGetAllCompaniesByRealm ended");
    }

    //DG_DISCOVERABILITY Post Delete - Testing HSQL
    @Test
    public void testFindCompanyNoEagerLoad() {
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyNoEagerLoad started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);

        assertNotNull(company);
        assertEquals(psid, company.getSourceCompanyId());

        company.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPostDelete = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertNull(companyPostDelete);
        } else {
            assertNotNull(companyPostDelete);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyNoEagerLoad ended");
    }

    @Test
    public void testFindCompany() {
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompany started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        assertNotNull(company);
        assertEquals(psid, company.getSourceCompanyId());

        company.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPostDelete = Company.findCompany(psid, SourceSystemCode.QBDT);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertNull(companyPostDelete);
        } else {
            assertNotNull(companyPostDelete);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompany ended");
    }

    @Test
    public void testIsEINInUse() {
        logger.info("DG_DISCOVERABILITY_FEATURE testIsEINInUse started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);

        DomainEntitySet<EntitlementUnit> entitlementUnit = Application.find(EntitlementUnit.class, EntitlementUnit.Company().equalTo(company));
        entitlementUnit.get(0).setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        boolean isEINInUsePreDelete = Company.isEINInUse(fein);

        assertTrue(isEINInUsePreDelete);

        Company companyPreDelete = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        companyPreDelete.setIsDgDisassociated(true);

        DomainEntitySet<EntitlementUnit> entitlementUnitPreDeactivate = Application.find(EntitlementUnit.class, EntitlementUnit.Company().equalTo(companyPreDelete));
        entitlementUnitPreDeactivate.get(0).setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        boolean isEINInUsePostDelete = Company.isEINInUse(fein);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertFalse(isEINInUsePostDelete);
        } else {
            assertTrue(isEINInUsePostDelete);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testIsEINInUse ended");
    }

    @Test
    public void testFindCompanies() {
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanies started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.findCompanies(SourceSystemCode.QBDT, fein);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(fein, companySetPreDelete.get(0).getFedTaxId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findCompanies(SourceSystemCode.QBDT, fein);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanies ended");
    }

    @Test
    public void testSearchCompaniesByEIN() {
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByEIN started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.searchCompaniesByEIN(fein);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(fein, companySetPreDelete.get(0).getFedTaxId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.searchCompaniesByEIN(fein);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByEIN ended");
    }

    @Test
    public void testSearchCompaniesBySourceCompanyId() {
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesBySourceCompanyId started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.searchCompaniesBySourceCompanyId(psid);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.searchCompaniesBySourceCompanyId(psid);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesBySourceCompanyId ended");
    }

    @Test
    public void testSearchCompaniesByLegalName() {
        logger.info("DG_DISCOVERABILITY_FEATURE testsearchCompaniesByLegalName started");
        String psid = "625001001";
        String legalName = "TEST_COMPANY_DG_INTUIT";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySet.get(0).setLegalName(legalName);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.searchCompaniesByLegalName(legalName);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(legalName, companySetPreDelete.get(0).getLegalName());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.searchCompaniesByLegalName(legalName);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testsearchCompaniesByLegalName ended");
    }

    @Test
    public void testSearchCompaniesByLicenseNumber() {
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByLicenseNumber started");
        String psid = "625001001";
        String licenseNumber = "lic_625001001Y";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.searchCompaniesByLicenseNumber(licenseNumber);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.searchCompaniesByLicenseNumber(licenseNumber);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByLicenseNumber ended");
    }

    @Test
    public void testSearchCompaniesByCAN() {
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByCAN started");
        String psid = "625001001";
        String can = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.searchCompaniesByCAN(can);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.searchCompaniesByCAN(can);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByCAN ended");
    }

    @Test
    public void testSearchCompaniesByServiceKey() {
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByServiceKey started");
        String psid = "625001001";
        String serviceKey = "4008-0128-0000-4001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<EntitlementUnit> entitlementUnit = Application.find(EntitlementUnit.class, EntitlementUnit.Company().SourceCompanyId().equalTo(psid));

        entitlementUnit.get(0).setServiceKey(serviceKey);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.searchCompaniesByServiceKey(serviceKey);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.searchCompaniesByServiceKey(serviceKey);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByServiceKey ended");
    }

    @Test
    public void testSearchCompaniesByRegistrationNumber() {
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByRegistrationNumber started");
        String psid = "625001001";
        String quickBookLicenseNumber = "924586016933296";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<QuickbooksInfo> quickbooksInfos = Application.find(QuickbooksInfo.class, QuickbooksInfo.Company().SourceCompanyId().equalTo(psid));

        quickbooksInfos.get(0).setLicenseNumber(quickBookLicenseNumber);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.searchCompaniesByRegistrationNumber(quickBookLicenseNumber);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.searchCompaniesByRegistrationNumber(quickBookLicenseNumber);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByRegistrationNumber ended");
    }

    @Test
    public void testSearchCompaniesByAnything() {
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByRegistrationNumber started");
        String psid = "625001001";
        String fein = "720010643";
        String legalName = "TEST_COMPANY_DG_INTUIT";
        String realmId = "9130354161103686";
        String serviceKey = "4008-0128-0000-4001";
        String licenseNumber = "lic_625001001Y";
        String can = "233575401";

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);
        company.setIAMRealmId(realmId);

        DomainEntitySet<EntitlementUnit> entitlementUnit = Application.find(EntitlementUnit.class, EntitlementUnit.Company().equalTo(company));
        entitlementUnit.get(0).setServiceKey(serviceKey);
        entitlementUnit.get(0).getEntitlement().setCustomerId(can);
        Application.commitUnitOfWork();

        //search PSID
        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete1 = Company.searchCompaniesByAnything(psid);

        assertEquals(1, companySetPreDelete1.size());
        assertEquals(psid, companySetPreDelete1.get(0).getSourceCompanyId());

        companySetPreDelete1.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete1 = Company.searchCompaniesByAnything(psid);
        assertEquals(0, companySetPostDelete1.size());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIsDgDisassociated(false);
        Application.save(company);
        Application.commitUnitOfWork();

        //search FEIN
        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete2 = Company.searchCompaniesByAnything(fein);

        assertEquals(1, companySetPreDelete2.size());
        assertEquals(fein, companySetPreDelete2.get(0).getFedTaxId());

        companySetPreDelete2.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete2 = Company.searchCompaniesByAnything(fein);

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete2.size());
        } else {
            assertEquals(1, companySetPostDelete2.size());
        }

        Application.refresh(company);

        company.setLegalName(legalName);
        company.setIsDgDisassociated(false);
        Application.save(company);
        Application.commitUnitOfWork();

        //search LegalName
        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete3 = Company.searchCompaniesByAnything(legalName);

        assertEquals(1, companySetPreDelete3.size());
        assertEquals(legalName, companySetPreDelete3.get(0).getLegalName());

        companySetPreDelete3.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete3 = Company.searchCompaniesByAnything(legalName);

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete3.size());
        } else {
            assertEquals(1, companySetPostDelete3.size());
        }

        Application.refresh(company);

        company.setLegalName(legalName);
        company.setIsDgDisassociated(false);
        Application.save(company);
        Application.commitUnitOfWork();

        //search RealmId
        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete4 = Company.searchCompaniesByAnything(realmId);

        assertEquals(1, companySetPreDelete4.size());
        assertEquals(realmId, companySetPreDelete4.get(0).getIAMRealmId());

        companySetPreDelete4.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete4 = Company.searchCompaniesByAnything(realmId);

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete4.size());
        } else {
            assertEquals(1, companySetPostDelete4.size());
        }

        Application.refresh(company);

        company.setLegalName(legalName);
        company.setIsDgDisassociated(false);
        Application.save(company);
        Application.commitUnitOfWork();

        //search ServiceKey
        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete5 = Company.searchCompaniesByAnything(serviceKey);

        assertEquals(1, companySetPreDelete5.size());
        assertEquals(psid, companySetPreDelete5.get(0).getSourceCompanyId());

        companySetPreDelete5.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete5 = Company.searchCompaniesByAnything(serviceKey);

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete5.size());
        } else {
            assertEquals(1, companySetPostDelete5.size());
        }
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();

        Application.refresh(company);

        company.setLegalName(legalName);
        company.setIsDgDisassociated(false);
        Application.save(company);
        Application.commitUnitOfWork();

        //search LicenseNumber
        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete6 = Company.searchCompaniesByAnything(licenseNumber);

        assertEquals(1, companySetPreDelete6.size());
        assertEquals(psid, companySetPreDelete6.get(0).getSourceCompanyId());

        companySetPreDelete6.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete6 = Company.searchCompaniesByAnything(licenseNumber);

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete6.size());
        } else {
            assertEquals(1, companySetPostDelete6.size());
        }
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();

        Application.refresh(company);

        company.setLegalName(legalName);
        company.setIsDgDisassociated(false);
        Application.save(company);
        Application.commitUnitOfWork();

        //search CAN
        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete7 = Company.searchCompaniesByAnything(can);

        assertEquals(1, companySetPreDelete7.size());
        assertEquals(psid, companySetPreDelete7.get(0).getSourceCompanyId());

        companySetPreDelete7.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete7 = Company.searchCompaniesByAnything(can);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete7.size());
        } else {
            assertEquals(1, companySetPostDelete7.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSearchCompaniesByRegistrationNumber ended");
    }

    @Test
    public void testFindCompaniesByPendingSubStatus() {
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesByPendingSubStatus started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        ArrayList<ServiceSubStatusCode> subStatusStrings = new ArrayList<ServiceSubStatusCode>();
        subStatusStrings.add(ServiceSubStatusCode.ActiveCurrent);
        DomainEntitySet<Company> companySetPreDelete = Company.findCompaniesByPendingSubStatus(subStatusStrings, null, false, 0, 50);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findCompaniesByPendingSubStatus(subStatusStrings, null, false, 0, 50);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesByPendingSubStatus ended");
    }

    @Test
    public void testGetCompaniesByPendingSubStatusCount() {
        logger.info("DG_DISCOVERABILITY_FEATURE testGetCompaniesByPendingSubStatusCount started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        ArrayList<ServiceSubStatusCode> subStatusStrings = new ArrayList<ServiceSubStatusCode>();
        subStatusStrings.add(ServiceSubStatusCode.ActiveCurrent);
        long sourceCompanyIdPreDeleteCount = Company.getCompaniesByPendingSubStatusCount(subStatusStrings);

        assertEquals(1, sourceCompanyIdPreDeleteCount);

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        long sourceCompanyIdPostDeleteCount = Company.getCompaniesByPendingSubStatusCount(subStatusStrings);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, sourceCompanyIdPostDeleteCount);
        } else {
            assertEquals(1, sourceCompanyIdPostDeleteCount);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testGetCompaniesByPendingSubStatusCount ended");
    }

    @Test
    public void testFindCompaniesByOnHoldSubStatus() {
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesByOnHoldSubStatus started");
        ACHReturnsDataLoader.loadData2DayERNSFs();

        Application.beginUnitOfWork();
        ArrayList<ServiceSubStatusCode> subStatusStrings = new ArrayList<ServiceSubStatusCode>();
        subStatusStrings.add(ServiceSubStatusCode.AchRejectR1R9);
        subStatusStrings.add(ServiceSubStatusCode.AchRejectOther);

        DomainEntitySet<Company> companySetPreDelete = Company.findCompaniesByOnHoldSubStatus(subStatusStrings, null, false,0,50);

        assertEquals(1, companySetPreDelete.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findCompaniesByOnHoldSubStatus(subStatusStrings, null, false,0,50);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesByOnHoldSubStatus ended");
    }

    @Test
    public void testGetCompaniesByOnHoldSubStatusCount() {
        logger.info("DG_DISCOVERABILITY_FEATURE testGetCompaniesByOnHoldSubStatusCount started");
        String psid = "1234567";
        ACHReturnsDataLoader.loadData2DayERNSFs();

        PayrollServices.beginUnitOfWork();
        ArrayList<ServiceSubStatusCode> subStatusStrings = new ArrayList<ServiceSubStatusCode>();
        subStatusStrings.add(ServiceSubStatusCode.AchRejectR1R9);
        subStatusStrings.add(ServiceSubStatusCode.AchRejectOther);

        long sourceCompanyIdPreDeleteCount = Company.getCompaniesByOnHoldSubStatusCount(subStatusStrings);

        assertEquals(1, sourceCompanyIdPreDeleteCount);

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        long sourceCompanyIdPostDeleteCount = Company.getCompaniesByOnHoldSubStatusCount(subStatusStrings);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, sourceCompanyIdPostDeleteCount);
        } else {
            assertEquals(1, sourceCompanyIdPostDeleteCount);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testGetCompaniesByOnHoldSubStatusCount ended");
    }

    /*
    @Test(expected = RuntimeException.class)
    public void testGetBookTransferCompany() {
        logger.info("DG_DISCOVERABILITY_FEATURE testGetBookTransferCompany started");
        String psid = "111111111";
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));
        companySet.get(0).setDDPublishFlag(false);
        companySet.get(0).setIsDgDisassociated(false);
        Application.save(companySet.get(0));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company companyPreDelete = Company.getBookTransferCompany();
        assertNotNull(companyPreDelete);
        assertEquals(psid, companyPreDelete.getSourceCompanyId());
        companyPreDelete.setIsDgDisassociated(true);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company.getBookTransferCompany();
        Application.rollbackUnitOfWork();
        logger.info("DG_DISCOVERABILITY_FEATURE testGetBookTransferCompany ended");
    }
    */

    @Test
    public void testFindActiveCompanyByRealmId() {
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompanyByRealmId started");
        String psid = "625001001";
        String realmId = "9130354161103686";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySet.get(0).setIAMRealmId(realmId);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPreDelete = Company.findActiveCompanyByRealmId(realmId);

        assertNotNull(companyPreDelete);
        assertEquals(realmId, companyPreDelete.getIAMRealmId());

        companyPreDelete.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPostDelete = Company.findActiveCompanyByRealmId(realmId);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertNull(companyPostDelete);
        } else {
            assertNotNull(companyPostDelete);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompanyByRealmId ended");
    }

    @Test
    public void testFindAllCompaniesByRealmId() {
        logger.info("DG_DISCOVERABILITY_FEATURE testfindAllCompaniesByRealmId started");
        String psid = "625001001";
        String realmId = "9130354161103686";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySet.get(0).setIAMRealmId(realmId);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.findAllCompaniesByRealmId(realmId);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(realmId, companySetPreDelete.get(0).getIAMRealmId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findAllCompaniesByRealmId(realmId);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testfindAllCompaniesByRealmId ended");
    }

    //DG_DISCOVERABILITY Post Delete - Testing Named Queries
    @Test
    public void testFindActiveCompaniesOnDDService() {
        //Query Name : findActiveCompaniesOnDirectDeposit
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompaniesOnDDService started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        List<String> sourceIdList = Company.findActiveCompaniesOnDDService();

        assertNotNull(sourceIdList);
        assertEquals(psid, sourceIdList.get(0));

        Company company = Company.findCompanyNoEagerLoad(sourceIdList.get(0), SourceSystemCode.QBDT);
        company.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<String> sourceIdListPostDelete = Company.findActiveCompaniesOnDDService();
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, sourceIdListPostDelete.size());
        } else {
            assertEquals(1, sourceIdListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompaniesOnDDService ended");
    }

    @Test
    public void testFindActiveCompany() {
        //Query Name : findActiveCompaniesForEINENC
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompany started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPreDelete = Company.findActiveCompany(SourceSystemCode.QBDT, fein);

        assertNotNull(companyPreDelete);
        assertEquals(fein, companyPreDelete.getFedTaxId());

        companyPreDelete.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPostDelete = Company.findActiveCompany(SourceSystemCode.QBDT, fein);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertNull(companyPostDelete);
        } else {
            assertNotNull(companyPostDelete);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompany ended");
    }

    @Test
    public void testFindActiveCompanyWithPSID() {
        //Query Name : findActiveCompaniesForEINENCAndPSID
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompanyWithPSID started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPreDelete = Company.findActiveCompanyWithPSID(SourceSystemCode.QBDT, fein, psid);

        assertNotNull(companyPreDelete);
        assertEquals(fein, companyPreDelete.getFedTaxId());
        assertEquals(psid, company.getSourceCompanyId());

        companyPreDelete.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyPostDelete = Company.findActiveCompanyWithPSID(SourceSystemCode.QBDT, fein, psid);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertNull(companyPostDelete);
        } else {
            assertNotNull(companyPostDelete);
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompanyWithPSID ended");
    }

    @Test
    public void testFindActiveCompanies() {
        //Query Name : findActiveCompaniesForEINENC
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompanies started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.findActiveCompanies(SourceSystemCode.QBDT, fein);
        assertEquals(1, companySetPreDelete.size());
        assertEquals(fein, companySetPreDelete.get(0).getFedTaxId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findActiveCompanies(SourceSystemCode.QBDT, fein);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindActiveCompanies ended");
    }

    @Test
    public void testFindTerminatedCompanies() {
        //Query Name : findCompaniesTermedForEINENC
        logger.info("DG_DISCOVERABILITY_FEATURE testFindTerminatedCompanies started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(fein);

        DomainEntitySet<CompanyService> companyServicePreTerminate = Application.find(CompanyService.class, CompanyService.Company().equalTo(company)
                .And(CompanyService.Service().ServiceCd().equalTo(ServiceCode.DirectDeposit)));
        companyServicePreTerminate.get(0).setStatusCd(ServiceSubStatusCode.Terminated);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.findTerminatedCompanies(fein);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(fein, companySetPreDelete.get(0).getFedTaxId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findTerminatedCompanies(fein);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindTerminatedCompanies ended");
    }

    @Test
    public void testFindCompaniesBySourceSystemAndService() {
        //Query Name : findCompaniesBySourceSystemAndService
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesBySourceSystemAndService started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.findCompaniesBySourceSystemAndService(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findCompaniesBySourceSystemAndService(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesBySourceSystemAndService ended");
    }

    @Test
    public void testFindCompaniesBySourceSystemAndPendingTaxService() {
        //Query Name : findCompaniesBySourceSystemAndPendingTaxService
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesBySourceSystemAndPendingTaxService started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<CompanyService> companyService = Application.find(CompanyService.class, CompanyService.Company().SourceCompanyId().equalTo(psid)
                .And(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)));

        companyService.get(0).setServiceStartDate(null);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.findCompaniesBySourceSystemAndPendingTaxService(SourceSystemCode.QBDT);

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findCompaniesBySourceSystemAndPendingTaxService(SourceSystemCode.QBDT);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesBySourceSystemAndPendingTaxService ended");
    }

    @Test
    public void testFindProcessingDisabledCompanies() {
        //Query Name : findProcessingDisabledCompanies
        logger.info("DG_DISCOVERABILITY_FEATURE testFindProcessingDisabledCompanies started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<QuickbooksInfo> quickbooksInfo = Application.find(QuickbooksInfo.class, QuickbooksInfo.Company().SourceCompanyId().equalTo(psid));

        quickbooksInfo.get(0).setProcessTransmissions(false);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvent = Application.find(CompanyEvent.class, CompanyEvent.Company().SourceCompanyId().equalTo(psid)
                .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.RequestProcessingFlagChanged)));

        SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
        spcfCalendar.addHours(-(SystemParameter.findIntValue(SystemParameter.Code.RESET_QBDT_FLAGS_BEGIN_TIME, 24) + 15));

        companyEvent.get(0).setCreatedDate(spcfCalendar);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Company.findProcessingDisabledCompanies();

        assertEquals(1, companySetPreDelete.size());
        assertEquals(psid, companySetPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPostDelete = Company.findProcessingDisabledCompanies();
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySetPostDelete.size());
        } else {
            assertEquals(1, companySetPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindProcessingDisabledCompanies ended");
    }

    @Test
    public void testFindCompanyTaxPayments() {
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyTaxPayments started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));
        Application.rollbackUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(companySet.get(0));

        Application.beginUnitOfWork();
        PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, PaymentTemplate.IRS_941);
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactionSet = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.Company().equalTo(companySet.get(0)));

        moneyMovementTransactionSet.stream().forEach(moneyMovementTransaction -> {
            moneyMovementTransaction.setPaymentTemplate(paymentTemplate);
            Application.save(moneyMovementTransaction);
        });

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Law law = Application.findById(Law.class, Law.FIT);
        DomainEntitySet<FinancialTransaction> financialTransactionSet = Application.find(FinancialTransaction.class, FinancialTransaction.Company().equalTo(companySet.get(0)));

        financialTransactionSet.stream().forEach(financialTransaction -> {
            financialTransaction.setLaw(law);
            Application.save(financialTransaction);
        });

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SpcfCalendar payPeriodBeginDate = PSPDate.getPSPTime();
        payPeriodBeginDate.addDays(-10);

        SpcfCalendar payPeriodEndDate = PSPDate.getPSPTime();
        payPeriodEndDate.addDays(10);

        ScrollableResults scrollableResultsPreDelete = Company.findCompanyTaxPayments(psid, payPeriodBeginDate, payPeriodEndDate);
        scrollableResultsPreDelete.next();

        assertEquals(psid, scrollableResultsPreDelete.get(0));

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ScrollableResults scrollableResultsPostDelete = Company.findCompanyTaxPayments(psid, payPeriodBeginDate, payPeriodEndDate);
        scrollableResultsPostDelete.next();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(-1, scrollableResultsPostDelete.getRowNumber());
        } else {
            assertEquals(1, scrollableResultsPostDelete.getRowNumber());
        }
        Application.rollbackUnitOfWork();
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyTaxPayments ended");
    }

    @Test
    public void testFindCompanyByNonAuthCriteria() {
        //Query Name : findCompaniesByNonAuthCriteria
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyByNonAuthCriteria started");
        String psid = "625001001";
        String ssn = "826392049";
        String firstName = "Johnny";
        String lastName = "PayrollAdmin";
        String email = "PayrollAdmin@aol.com";
        String phoneNumber = "(775) 333-3333";

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Contact> contactSet = Application.find(Contact.class, Contact.Company().SourceCompanyId().equalTo(psid)
                .And(Contact.ContactRoleCd().equalTo(ContactRole.PayrollAdmin)));

        contactSet.get(0).setSocialSecurityNumber(ssn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Company> companyListPreDelete = Company.findCompanyByNonAuthCriteria(ssn, firstName, lastName, SourceSystemCode.QBDT.name(),
                ContactRole.PayrollAdmin.name(), email, phoneNumber);
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        assertEquals(1, companyListPreDelete.size());
        assertEquals(psid, companyListPreDelete.get(0).getSourceCompanyId());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Company> companyListPostDelete = Company.findCompanyByNonAuthCriteria(ssn, firstName, lastName, SourceSystemCode.QBDT.name(),
                ContactRole.PayrollAdmin.name(), email, phoneNumber);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyListPostDelete.size());
        } else {
            assertEquals(1, companyListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyByNonAuthCriteria ended");
    }

    @Test
    public void testCompanyQueryByEinenc() {
        //Query Name = company.query.by.einenc
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryByEinenc started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySet.get(0).setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"companyFedTaxIdEncList", "sourceSystemCd"};
        Object[] paramValues = new Object[]{EncryptionUtils.deterministicEncrypt(Company.FedTaxIdKeyName, fein),
                SourceSystemCode.QBDT};

        List<Integer> number = Application.executeNamedQuery("company.query.by.einenc", paramNames, paramValues);

        assertEquals(1, number.size());
        assertEquals(1, number.get(0));

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Integer> numberPostDelete = Application.executeNamedQuery("company.query.by.einenc", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, numberPostDelete.size());
        } else {
            assertEquals(1, numberPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryByEinenc ended");
    }

    @Test
    public void testFindServiceStatusForCompanyService() {
        //Query Name : findServiceStatusForCompanyService
        logger.info("DG_DISCOVERABILITY_FEATURE testFindServiceStatusForCompanyService started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"companyId", "serviceCd","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{company.getId(), ServiceCode.Tax, !AuthUser.hasSAPAdminAccess()};
        List<ServiceSubStatusCode> serviceCode = Application.executeNamedQuery("findServiceStatusForCompanyService", paramNames, paramValues);
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        assertEquals(1, serviceCode.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<ServiceSubStatusCode> serviceCodePostDelete = Application.executeNamedQuery("findServiceStatusForCompanyService", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, serviceCodePostDelete.size());
        } else {
            assertEquals(1, serviceCodePostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindServiceStatusForCompanyService ended");
    }

    @Test
    public void testCompanyQueryCompanyserviceBySourcesystem() {
        //Query Name : company.query.companyservice.by.sourcesystem
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceBySourcesystem started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"sourceSystemCd", "serviceCd", "excludeDeletedCompany"};
        Object[] paramValues = new Object[]{SourceSystemCode.QBDT, ServiceCode.Tax, !AuthUser.hasSAPAdminAccess()};
        List<CompanyService> companyServiceSet = Application.executeNamedQuery("company.query.companyservice.by.sourcesystem", paramNames, paramValues);
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        assertEquals(1, companyServiceSet.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<CompanyService> companyServicePostDelete = Application.executeNamedQuery("company.query.companyservice.by.sourcesystem", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyServicePostDelete.size());
        } else {
            assertEquals(1, companyServicePostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceBySourcesystem ended");
    }

    @Test
    public void testCompanyQueryCompanyserviceByLegalName() {
        //Query Name : company.query.companyservice.by.legalname
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceByLegalName started");
        String psid = "625001001";
        String legalName = "TEST_COMPANY_DG_INTUIT";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySet.get(0).setLegalName(legalName);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"sourceSystemCd", "legalNameWithPercentSigns", "serviceCd", "excludeDeletedCompany"};
        Object[] paramValues = new Object[]{SourceSystemCode.QBDT, "%"+legalName+"%", ServiceCode.Tax,!AuthUser.hasSAPAdminAccess()};
        List<CompanyService> companyServiceSet = Application.executeNamedQuery("company.query.companyservice.by.legalname", paramNames, paramValues);
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        assertEquals(1, companyServiceSet.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<CompanyService> companyServicePostDelete = Application.executeNamedQuery("company.query.companyservice.by.legalname", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyServicePostDelete.size());
        } else {
            assertEquals(1, companyServicePostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceByLegalName ended");
    }

    @Test
    public void testCompanyQueryCompanyserviceByFedtaxidenc() {
        //Query Name : company.query.companyservice.by.fedtaxidenc
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceByFedtaxidenc started");
        String psid = "625001001";
        String fein = "720010643";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySet.get(0).setFedTaxId(fein);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"sourceSystemCd", "fedTaxIdEncList", "serviceCd","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{SourceSystemCode.QBDT, EncryptionUtils.deterministicEncrypt(Company.FedTaxIdKeyName, fein),
                ServiceCode.Tax,!AuthUser.hasSAPAdminAccess()};
        List<CompanyService> companyServiceSet = Application.executeNamedQuery("company.query.companyservice.by.fedtaxidenc", paramNames, paramValues);
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        assertEquals(1, companyServiceSet.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<CompanyService> companyServicePostDelete = Application.executeNamedQuery("company.query.companyservice.by.fedtaxidenc", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyServicePostDelete.size());
        } else {
            assertEquals(1, companyServicePostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceByFedtaxidenc ended");
    }

    @Test
    public void testCompanyQueryCompanyserviceBySourcecompanyid() {
        //Query Name : company.query.companyservice.by.sourcecompanyid
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceBySourcecompanyid started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"sourceSystemCd", "sourceCompanyId", "serviceCd","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{SourceSystemCode.QBDT, psid, ServiceCode.Tax,!AuthUser.hasSAPAdminAccess()};
        List<CompanyService> companyServiceSet = Application.executeNamedQuery("company.query.companyservice.by.sourcecompanyid", paramNames, paramValues);
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        assertEquals(1, companyServiceSet.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<CompanyService> companyServicePostDelete = Application.executeNamedQuery("company.query.companyservice.by.sourcecompanyid", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyServicePostDelete.size());
        } else {
            assertEquals(1, companyServicePostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testCompanyQueryCompanyserviceBySourcecompanyid ended");
    }

    @Test
    public void testFindUpdatedCompaniesForATFDataExtract() {
        //Query Name : findUpdatedCompaniesForATFDataExtract
        logger.info("DG_DISCOVERABILITY_FEATURE testFindUpdatedCompaniesForATFDataExtract started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addDays(-10);

        SpcfCalendar toDate = PSPDate.getPSPTime();
        toDate.addDays(10);

        String[] paramNames = new String[]{"sourceSystemCd", "serviceCd", "fromDate", "toDate"};
        Object[] paramValues = new Object[]{SourceSystemCode.QBDT, ServiceCode.Tax, fromDate, toDate};
        List<Company> companyList = Application.executeNamedQuery("findUpdatedCompaniesForATFDataExtract", paramNames, paramValues);

        assertEquals(1, companyList.size());
        assertEquals(psid, companyList.get(0).getSourceCompanyId());

        companyList.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Company> companyListPostDelete = Application.executeNamedQuery("findUpdatedCompaniesForATFDataExtract", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyListPostDelete.size());
        } else {
            assertEquals(1, companyListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindUpdatedCompaniesForATFDataExtract ended");
    }

    @Test
    public void testFindAllQBDTCompanyIdsToAddCloudService() {
        //Query Name : findAllQBDTCompanyIdsToAddCloudService
        logger.info("DG_DISCOVERABILITY_FEATURE testFindAllQBDTCompanyIdsToAddCloudService started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"excludeDeletedCompany"};
        Object[] paramValues = new Object[]{!AuthUser.hasSAPAdminAccess()};
        List<String> psidList = Application.executeNamedQuery("findAllQBDTCompanyIdsToAddCloudService", paramNames, paramValues);

        assertEquals(1, psidList.size());
        assertEquals(psid, psidList.get(0));

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<String> psidListPostDelete = Application.executeNamedQuery("findAllQBDTCompanyIdsToAddCloudService", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, psidListPostDelete.size());
        } else {
            assertEquals(1, psidListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindAllQBDTCompanyIdsToAddCloudService ended");
    }

    @Test
    public void testFindCompanyIdsBySourceSystemAndService() {
        //Query Name : findCompanyIdsBySourceSystemAndService
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyIdsBySourceSystemAndService started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"sourceSystemCd", "serviceCd","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{SourceSystemCode.QBDT, ServiceCode.Tax,!AuthUser.hasSAPAdminAccess()};
        List<String> psidList = Application.executeNamedQuery("findCompanyIdsBySourceSystemAndService", paramNames, paramValues);

        assertEquals(1, psidList.size());
        assertEquals(company.getId(), psidList.get(0));

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<String> psidListPostDelete = Application.executeNamedQuery("findCompanyIdsBySourceSystemAndService", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, psidListPostDelete.size());
        } else {
            assertEquals(1, psidListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompanyIdsBySourceSystemAndService ended");
    }

    @Test
    public void testFindCompaniesWithFileableLiabilities() {
        //Query Name : findCompaniesWithFileableLiabilities
        logger.info("DG_DISCOVERABILITY_FEATURE testfindCompaniesWithFileableLiabilities started");
        Application.beginUnitOfWork();
        SpcfCalendar existDate = PSPDate.getPSPTime();
        PSPDate.resetPSPTime();
        Application.commitUnitOfWork();

        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        DataLoadServices.addFederalTaxCompanyLaws(company);

        Application.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        Application.rollbackUnitOfWork();

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(checkDate));

        Application.beginUnitOfWork();
        DomainEntitySet<PayrollRun> payrollRunSet = Application.find(PayrollRun.class,
                PayrollRun.Company().SourceCompanyId().equalTo(psid).And(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));

        payrollRunSet.stream().forEach(payrollRun -> {
            payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
            Application.save(payrollRun);
        });
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));
        SpcfCalendar quarterStartDate = CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime());
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(PSPDate.getPSPTime());
        SpcfCalendar twoDaysAway = PSPDate.getPSPTime();

        CalendarUtils.addBusinessDays(twoDaysAway, (companySetPreDelete.get(0).getFundingModel().getNumberOfFundingDays() + 5));
        String[] paramNames = new String[]{"quarterStartDate", "quarterEndDate", "twoDaysAway","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{quarterStartDate, quarterEndDate, twoDaysAway,!AuthUser.hasSAPAdminAccess()};

        List<Company> companyList = Application.executeNamedQuery("findCompaniesWithFileableLiabilities", paramNames, paramValues);

        assertEquals(1, companyList.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Company> companyListPostDelete = Application.executeNamedQuery("findCompaniesWithFileableLiabilities", paramNames, paramValues);

        PSPDate.setPSPTime(existDate);
        Application.commitUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyListPostDelete.size());
        } else {
            assertEquals(1, companyListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testfindCompaniesWithFileableLiabilities ended");
    }

    @Test
    public void testFindCompaniesWithFileableAdjustments() {
        //Query Name : findCompaniesWithFileableAdjustments
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithFileableAdjustments started");
        Application.beginUnitOfWork();
        SpcfCalendar existDate = PSPDate.getPSPTime();
        PSPDate.resetPSPTime();
        Application.commitUnitOfWork();

        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        DataLoadServices.addFederalTaxCompanyLaws(company);

        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        Application.beginUnitOfWork();
        DomainEntitySet<PayrollRun> payrollRunSet = Application.find(PayrollRun.class, PayrollRun.Company().SourceCompanyId().equalTo(psid).And(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)));

        payrollRunSet.stream().forEach(payrollRun -> {
            payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
            Application.save(payrollRun);
        });
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));
        SpcfCalendar twoDaysAway = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(twoDaysAway, (companySet.get(0).getFundingModel().getNumberOfFundingDays() + 5));
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(twoDaysAway));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO("1", "5", null, new DateDTO(twoDaysAway),
                new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);

        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.payrollManager.addLiabilityAdjustments(SourceSystemCode.QBDT, psid, sourcePayrollId,
                companyAdjustmentSubmissionDTO, new DateDTO(twoDaysAway), liabilityAdjustmentOptionsDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));
        SpcfCalendar quarterStartDate = CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime());
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(PSPDate.getPSPTime());
        String[] paramNames = new String[]{"quarterStartDate", "quarterEndDate", "twoDaysAway","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{quarterStartDate, quarterEndDate, twoDaysAway, !AuthUser.hasSAPAdminAccess()};

        List<Company> companyList = Application.executeNamedQuery("findCompaniesWithFileableAdjustments", paramNames, paramValues);

        assertEquals(1, companyList.size());

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Company> companyListPostDelete = Application.executeNamedQuery("findCompaniesWithFileableAdjustments", paramNames, paramValues);

        PSPDate.setPSPTime(existDate);
        Application.commitUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyListPostDelete.size());
        } else {
            assertEquals(1, companyListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithFileableAdjustments ended");
    }

    @Test
    public void testFindCompaniesWithUpdatedFileableLiabilities() {
        //Query Name : findCompaniesWithUpdatedFileableLiabilities
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithUpdatedFileableLiabilities started");
        Application.beginUnitOfWork();
        SpcfCalendar existDate = PSPDate.getPSPTime();
        PSPDate.resetPSPTime();
        Application.commitUnitOfWork();

        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        DataLoadServices.addFederalTaxCompanyLaws(company);

        Application.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        Application.rollbackUnitOfWork();

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(checkDate));

        Application.beginUnitOfWork();
        DomainEntitySet<PayrollRun> payrollRunSet = Application.find(PayrollRun.class);

        payrollRunSet.stream().forEach(payrollRun -> {
            payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
            Application.save(payrollRun);
        });
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        //DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));
        SpcfCalendar todaysDate = PSPDate.getPSPTime();
        //CalendarUtils.addBusinessDays(twoDaysAway, (companySet.get(0).getFundingModel().getNumberOfFundingDays() + 1));
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(todaysDate));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO("1", "5", null, new DateDTO(todaysDate),
                new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);

        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        String sourcePayrollId =PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        PayrollServices.payrollManager.addLiabilityAdjustments(SourceSystemCode.QBDT, psid, sourcePayrollId,
                companyAdjustmentSubmissionDTO, new DateDTO(todaysDate), liabilityAdjustmentOptionsDTO);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SpcfCalendar beginningOfTimeTax = SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar lastRunDate = PSPDate.getPSPTime();
        lastRunDate.addDays(-2);
        SpcfCalendar runDateTime = PSPDate.getPSPTime();
        String[] paramNames = new String[]{"lastRunDate", "beginningOfTimeTax", "runDateTime","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{lastRunDate, beginningOfTimeTax, runDateTime, !AuthUser.hasSAPAdminAccess()};

        List<Object[]> companySeqList = Application.executeNamedQuery("findCompaniesWithUpdatedFileableLiabilities", paramNames, paramValues);

        assertEquals(1, companySeqList.size());
        assertEquals(company.getId(), companySeqList.get(0)[0]);

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<String> companySeqListPostDelete = Application.executeNamedQuery("findCompaniesWithUpdatedFileableLiabilities", paramNames, paramValues);

        PSPDate.setPSPTime(existDate);
        Application.commitUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySeqListPostDelete.size());
        } else {
            assertEquals(1, companySeqListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithUpdatedFileableLiabilities ended");
    }

    @Test
    public void testFindCompaniesWithUpdatedFileableAdjustments() {
        //Query Name : findCompaniesWithUpdatedFileableAdjustments
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithUpdatedFileableAdjustments started");
        Application.beginUnitOfWork();
        SpcfCalendar existDate = PSPDate.getPSPTime();
        PSPDate.resetPSPTime();
        Application.commitUnitOfWork();

        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        DataLoadServices.addFederalTaxCompanyLaws(company);

        Application.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        String sourcePayrollId = payrollRun.getSourcePayRunId();

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));
        SpcfCalendar twoDaysAway = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(twoDaysAway, (companySet.get(0).getFundingModel().getNumberOfFundingDays() + 1));
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(twoDaysAway));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO("1", "5", null, new DateDTO(twoDaysAway),
                new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);

        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.payrollManager.addLiabilityAdjustments(SourceSystemCode.QBDT, psid, sourcePayrollId,
                companyAdjustmentSubmissionDTO, new DateDTO(twoDaysAway), liabilityAdjustmentOptionsDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        SpcfCalendar lastRunDate = PSPDate.getPSPTime();
        lastRunDate.addDays(-2);
        SpcfCalendar beginningOfTimeTax = SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar runDateTime = PSPDate.getPSPTime();
        String[] paramNames = new String[]{"lastRunDate", "beginningOfTimeTax", "runDateTime", "excludeDeletedCompany"};
        Object[] paramValues = new Object[]{lastRunDate, beginningOfTimeTax, runDateTime, !AuthUser.hasSAPAdminAccess()};

        List<Company> companySeqList = Application.executeNamedQuery("findCompaniesWithUpdatedFileableAdjustments", paramNames, paramValues);

        assertEquals(1, companySeqList.size());
        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Company> companySeqListPostDelete = Application.executeNamedQuery("findCompaniesWithUpdatedFileableAdjustments", paramNames, paramValues);

        PSPDate.setPSPTime(existDate);
        Application.commitUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySeqListPostDelete.size());
        } else {
            assertEquals(1, companySeqListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithUpdatedFileableAdjustments ended");
    }

    @Test
    public void testFindCompaniesWithUpdatedPayments() {
        //Query Name : findCompaniesWithUpdatedPayments
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithUpdatedPayments started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        SpcfCalendar existingDate = PSPDate.getPSPTime();
        PSPDate.resetPSPTime();
        SpcfCalendar paymentPeriodBegin = PSPDate.getPSPTime();
        paymentPeriodBegin.addDays(-8);
        SpcfCalendar paymentPeriodEnd = PSPDate.getPSPTime();
        paymentPeriodEnd.addDays(-2);
        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        initiationDate.setValues(initiationDate.getYear(), initiationDate.getMonth(), initiationDate.getDay());
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactionSet = Application.find(MoneyMovementTransaction.class,
                MoneyMovementTransaction.Company().SourceCompanyId().equalTo(psid));

        moneyMovementTransactionSet.get(0).setMoneyMovementPaymentMethod(PaymentMethod.ACHDebit);
        moneyMovementTransactionSet.get(0).setPaymentPeriodBegin(paymentPeriodBegin);
        moneyMovementTransactionSet.get(0).setPaymentPeriodEnd(paymentPeriodEnd);
        moneyMovementTransactionSet.get(0).setInitiationDate(initiationDate);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SpcfCalendar endDate = PSPDate.getPSPTime();
        SpcfCalendar lastRunDate = PSPDate.getPSPTime();
        lastRunDate.addDays(-2);
        SpcfCalendar runDatePlus1 = PSPDate.getPSPTime();
        runDatePlus1.addDays(1);
        SpcfCalendar beginningOfTimeTax = SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone());

        String[] paramNames = new String[]{"endDate", "lastRunDate", "runDatePlus1", "beginningOfTimeTax","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{endDate, lastRunDate, runDatePlus1, beginningOfTimeTax, !AuthUser.hasSAPAdminAccess()};

        List<Object[]> companySeqList = Application.executeNamedQuery("findCompaniesWithUpdatedPayments", paramNames, paramValues);

        assertEquals(1, companySeqList.size());
        assertEquals(company.getId(), companySeqList.get(0)[0]);

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Object[]> companySeqListPostDelete = Application.executeNamedQuery("findCompaniesWithUpdatedPayments", paramNames, paramValues);

        PSPDate.setPSPTime(existingDate);
        Application.commitUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companySeqListPostDelete.size());
        } else {
            assertEquals(1, companySeqListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCompaniesWithUpdatedPayments ended");
    }

    @Test
    public void testFindIABENNumbersENC() {
        //Query Name : findIABENNumbersENC
        logger.info("DG_DISCOVERABILITY_FEATURE testFindIABENNumbersENC started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        String name = "BEN Number";
        CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId = new CompanyPaymentTemplateAgencyId();
        DomainEntitySet<CompanyAgency> companyAgencySet = Application.find(CompanyAgency.class, CompanyAgency.Company().SourceCompanyId().equalTo(psid));

        companyPaymentTemplateAgencyId.setName(name);
        companyPaymentTemplateAgencyId.setAgencyTaxpayerId(companyAgencySet.get(0).getCompanyAgencyPaymentTemplateSet().iterator().next().getAgencyTaxpayerId());
        companyPaymentTemplateAgencyId.setCompanyAgencyPaymentTemplate(companyAgencySet.get(0).getCompanyAgencyPaymentTemplateSet().iterator().next());
        Application.save(companyPaymentTemplateAgencyId);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{};
        Object[] paramValues = new Object[]{};
        List<Object[]> psidList = Application.executeNamedQuery("findIABENNumbersENC", paramNames, paramValues);

        assertEquals(1, psidList.size());
        assertEquals(psid, psidList.get(0)[0]);

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Object[]> psidListPostDelete = Application.executeNamedQuery("findIABENNumbersENC", paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, psidListPostDelete.size());
        } else {
            assertEquals(1, psidListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindIABENNumbersENC ended");
    }

    @Test
    public void testFindSuiCustomerRatesDataForStateEnc() {
        //Query Name : findSuiCustomerRatesDataForStateEnc
        logger.info("DG_DISCOVERABILITY_FEATURE testFindSuiCustomerRatesDataForStateEnc started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        DataLoadServices.addFederalAndNYStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawRates(company);

        Application.beginUnitOfWork();
        DomainEntitySet<CompanyService> companyServiceSet = Application.find(CompanyService.class,
                CompanyService.Company().SourceCompanyId().equalTo(psid).And(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)));

        companyServiceSet.get(0).setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SpcfCalendar today = PSPDate.getPSPTime();
        String[] paramNames = new String[]{"yearQuarter", "state", "excludeDeletedCompany"};
        Object[] paramValues = new Object[]{Integer.toString(CalendarUtils.getYearAndQuarterAsInt(today)), "NY-%", !AuthUser.hasSAPAdminAccess()};
        List<Object[]> companyList = Application.executeNamedQuery(
                Application.getQueryName("findSuiCustomerRatesDataForStateEnc"), paramNames, paramValues);

        assertEquals(1, companyList.size());
        assertEquals(psid, companyList.get(0)[0]);

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Object[]> companyListPostDelete = Application.executeNamedQuery(
                Application.getQueryName("findSuiCustomerRatesDataForStateEnc"), paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyListPostDelete.size());
        } else {
            assertEquals(1, companyListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindSuiCustomerRatesDataForStateEnc ended");
    }

    @Test
    public void testSqlFindCompanyByCompanyRealm() {
        //Query Name : sqlFindCompanyByCompanyRealm
        logger.info("DG_DISCOVERABILITY_FEATURE testSqlFindCompanyByCompanyRealm started");
        String psid = "625001001";
        String realmId = "9130354161103686";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.createAssistedCompany(psid);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companySet = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySet.get(0).setIAMRealmId(realmId);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        String[] paramNames = new String[]{"company_realm_id","excludeDeletedCompany"};
        Object[] paramValues = new Object[]{realmId, !AuthUser.hasSAPAdminAccess()};

        List<Company> companyList = Application.executeNamedQuery(Application.getQueryName("sqlFindCompanyByCompanyRealm"), paramNames, paramValues);

        assertEquals(1, companyList.size());
        assertEquals(realmId, companyList.get(0).getIAMRealmId());

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Company> companyListPostDelete = Application.executeNamedQuery(Application.getQueryName("sqlFindCompanyByCompanyRealm"), paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyListPostDelete.size());
        } else {
            assertEquals(1, companyListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testSqlFindCompanyByCompanyRealm ended");
    }

    @Test
    public void testFindCustomerDepositFrequencyDataForStateEnc() {
        //Query Name : findCustomerDepositFrequencyDataForStateEnc
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCustomerDepositFrequencyDataForStateEnc started");
        String psid = "625001001";
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Company company = psdl.createAssistedCompany(psid);

        DataLoadServices.addFederalAndNYStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawRates(company);

        Application.beginUnitOfWork();
        DomainEntitySet<CompanyService> companyServiceSet = Application.find(CompanyService.class,
                CompanyService.Company().SourceCompanyId().equalTo(psid).And(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)));

        companyServiceSet.get(0).setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SpcfCalendar today = PSPDate.getPSPTime();
        String[] paramNames = new String[]{"yearQuarter", "state", "excludeDeletedCompany"};
        Object[] paramValues = new Object[]{Integer.toString(CalendarUtils.getYearAndQuarterAsInt(today)), "NY-%", !AuthUser.hasSAPAdminAccess()};

        List<Object[]> companyList = Application.executeNamedQuery(
                Application.getQueryName("findCustomerDepositFrequencyDataForStateEnc"), paramNames, paramValues);

        assertEquals(1, companyList.size());
        assertEquals(psid, companyList.get(0)[0]);

        DomainEntitySet<Company> companySetPreDelete = Application.find(Company.class, Company.SourceCompanyId().equalTo(psid));

        companySetPreDelete.get(0).setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        List<Object[]> companyListPostDelete = Application.executeNamedQuery(
                Application.getQueryName("findCustomerDepositFrequencyDataForStateEnc"), paramNames, paramValues);
        Application.rollbackUnitOfWork();

        if(!AuthUser.hasSAPAdminAccess()) {
            assertEquals(0, companyListPostDelete.size());
        } else {
            assertEquals(1, companyListPostDelete.size());
        }
        logger.info("DG_DISCOVERABILITY_FEATURE testFindCustomerDepositFrequencyDataForStateEnc ended");
    }
}
