package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsEinServiceEligibility;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsEinServiceEligibilityResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.QueryServiceEligibilityProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.PayrollSubmitTaxTests;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class QueryServiceEligibilityProcessTests {

    private EwsEinServiceEligibility mRequest;
    private EwsEinServiceEligibilityResponse mResponse;

    private String psid = null;
    private Boolean assistedTest;

    private static SpcfCalendar oldOfferEndDate;

    public String getPSID() {
        return psid;
    }

    @BeforeClass
    public static void beforeClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar newOfferEndDate = SpcfCalendar.createInstance();
        newOfferEndDate.addDays(30);
        offer.setEndDate(newOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @AfterClass
    public static void afterClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar oldOfferEndDate = SpcfCalendar.createInstance(2013, 7, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        offer.setEndDate(oldOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @Before
    public void startUp() {
        assistedTest = false;
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void afterEachTest() {

        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void einDoesNotExists() {
        mRequest = TestDataFactory.createEwsEinServiceEligibility("123456789");

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());

        //Checking for null EIN
        mRequest = TestDataFactory.createEwsEinServiceEligibility(null);

        process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertEquals("Error message code does not match", 30005, mResponse.getEwsResponseStatus().getCode());
        assertEquals("Error message text does not match", "Field EIN in Company does not contain valid data", mResponse.getEwsResponseStatus().getMessage());
        assertNull(mResponse.getEligibleForDIY());
        assertNull(mResponse.getEligibleForDD());
        assertNull(mResponse.getEligibleForAssisted());
    }

    @Test
    public void companyIsDGDeleted() {

        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudOnly();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility("123456789");

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());

    }

    @Test
    public void cloudActive() {
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudOnly();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());
    }

    @Test
    public void cloudCancelled() {
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudOnly();

        Application.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        company.getService(ServiceCode.Cloud).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());
    }

    @Test
    public void ddActive() {
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudAndDirectDeposit();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertFalse(mResponse.getEligibleForDD());
        assertFalse(mResponse.getEligibleForAssisted());
    }

    @Test
    public void ddCancelled() {
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudAndDirectDeposit();

        Application.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        company.getService(ServiceCode.DirectDeposit).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());
    }

    @Test
    public void ddTerminated() {
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudAndDirectDeposit();

        Application.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        company.getService(ServiceCode.DirectDeposit).updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());
    }

    @Test
    public void taxActive() {
        assistedTest = true;

        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudAndAssisted();
        psid = createAccountProcessTests.getPSID();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertFalse(mResponse.getEligibleForDD());
        assertFalse(mResponse.getEligibleForAssisted());
    }

    @Test
    public void taxCancelled() {
        assistedTest = true;
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudAndAssisted();
        psid = createAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        company.getService(ServiceCode.Tax).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        company.getService(ServiceCode.DirectDeposit).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());
    }

    @Test
    public void taxTerminated() {
        assistedTest = true;
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudAndAssisted();
        psid = createAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        company.getService(ServiceCode.Tax).updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility(company.getFedTaxId());

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertFalse(mResponse.getEligibleForDD());
        assertFalse(mResponse.getEligibleForAssisted());
    }

    @Test
    public void taxCancelledWithCurrentYearPayroll() {
        try {
            DataLoadServices.reinitialize();
            PayrollSubmitTaxTests tests=new PayrollSubmitTaxTests();
            tests.runBeforeEachTest();
            tests.testHappyPath();
        } finally {
            Application.rollbackUnitOfWork();
        }

        Application.beginUnitOfWork();
        Company company = Company.findActiveCompany(SourceSystemCode.QBDT, "000000001");
        company.getService(ServiceCode.Tax).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        company.getService(ServiceCode.DirectDeposit).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility("000000001");

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertFalse(mResponse.getEligibleForAssisted());
    }

    @Test
    public void taxCancelledWithoutCurrentYearPayroll() {
        try {
            DataLoadServices.reinitialize();
            PayrollSubmitTaxTests tests=new PayrollSubmitTaxTests();
            tests.runBeforeEachTest();
            tests.testHappyPath();
        } finally {
            Application.rollbackUnitOfWork();
        }

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20110301000000");
        Company company = Company.findActiveCompany(SourceSystemCode.QBDT, "000000001");
        company.getService(ServiceCode.Tax).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        company.getService(ServiceCode.DirectDeposit).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility("000000001");

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());
    }

    @Test
    public void taxCancelledWithCurrentYearPayrollDisabled() {
        try {
            DataLoadServices.reinitialize();
            PayrollSubmitTaxTests tests=new PayrollSubmitTaxTests();
            tests.runBeforeEachTest();
            tests.testHappyPath();
        } finally {
            Application.rollbackUnitOfWork();
        }

        Application.beginUnitOfWork();
        Company company = Company.findActiveCompany(SourceSystemCode.QBDT, "000000001");
        company.getService(ServiceCode.Tax).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        company.getService(ServiceCode.DirectDeposit).updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsEinServiceEligibility("000000001");
        mRequest.setEnableCurrentTaxYearValidation(false);

        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertTrue(mResponse.getEligibleForDIY());
        assertTrue(mResponse.getEligibleForDD());
        assertTrue(mResponse.getEligibleForAssisted());
    }

}
