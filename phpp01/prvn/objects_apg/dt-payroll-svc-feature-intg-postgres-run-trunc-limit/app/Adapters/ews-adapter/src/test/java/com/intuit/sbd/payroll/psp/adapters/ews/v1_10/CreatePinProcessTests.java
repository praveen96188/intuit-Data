package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsServiceStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.CreatePinProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class CreatePinProcessTests {

    private EwsBasePin mRequest;
    private EwsBasePinResponse mResponse;

    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();
    private ValidateBankProcessTests mValidateBankProcessTests = new ValidateBankProcessTests();

    private String psid = null;
    private Boolean assistedTest;

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
    public void createPinCloudOnly() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsCreatePin(company.getSourceCompanyId());

            CreatePinProcess createPinProcess = new CreatePinProcess(mRequest);
            mResponse = createPinProcess.execute();

            assertNotNull(mResponse);

            psid = mResponse.getPsid();

            assertNotNull(mResponse.getPsid());
            assertNotNull(mResponse.getPrivateKey());

            assertNotNull(mResponse.getServicesResponse());
            assertNotNull(mResponse.getServicesResponse().getCloudResponse());

            assertNull(mResponse.getServicesResponse().getDirectDepositResponse());
            assertNull(mResponse.getServicesResponse().getAssistedResponse());
            assertNull(mResponse.getServicesResponse().getThirdParty401kResponse());
            assertNull(mResponse.getServicesResponse().getBillPaymentResponse());
            assertNull(mResponse.getServicesResponse().getCheckDistributionResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void createPinCloudAndDD() {
        try {
            mValidateBankProcessTests.ValidateBankDD();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsCreatePin(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            CreatePinProcess createPinProcess = new CreatePinProcess(mRequest);
            mResponse = createPinProcess.execute();

            assertNotNull(mResponse);

            psid = mResponse.getPsid();

            assertNotNull(mResponse.getPsid());
            assertNotNull(mResponse.getPrivateKey());

            assertNotNull(mResponse.getServicesResponse());
            assertNotNull(mResponse.getServicesResponse().getCloudResponse());
            assertNotNull(mResponse.getServicesResponse().getDirectDepositResponse());

            assertNull(mResponse.getServicesResponse().getAssistedResponse());
            assertNull(mResponse.getServicesResponse().getThirdParty401kResponse());
            assertNull(mResponse.getServicesResponse().getBillPaymentResponse());
            assertNull(mResponse.getServicesResponse().getCheckDistributionResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void createPinCloudAndAssisted() {
        assistedTest = true;
        try {
            mValidateBankProcessTests.ValidateBankAssisted();
            psid = mValidateBankProcessTests.getPSID();
            
            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsCreatePin(company.getSourceCompanyId());

            CreatePinProcess createPinProcess = new CreatePinProcess(mRequest);
            mResponse = createPinProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());
            assertNotNull(mResponse.getPrivateKey());

            assertNotNull(mResponse.getServicesResponse());
            assertNotNull(mResponse.getServicesResponse().getCloudResponse());
            assertNotNull(mResponse.getServicesResponse().getAssistedResponse());

            assertNull(mResponse.getServicesResponse().getDirectDepositResponse());
            assertNull(mResponse.getServicesResponse().getThirdParty401kResponse());
            assertNull(mResponse.getServicesResponse().getBillPaymentResponse());
            assertNull(mResponse.getServicesResponse().getCheckDistributionResponse());

            assertNotNull(mResponse.getServicesResponse());
            EwsPinServicesResponse ewsPinServicesResponse = mResponse.getServicesResponse();
            assertNotNull(ewsPinServicesResponse.getCloudResponse());
            EwsBaseServiceResponse ewsBaseServiceResponse = ewsPinServicesResponse.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsBaseServiceResponse.getStatus());
            assertNotNull(ewsPinServicesResponse.getAssistedResponse());
            ewsBaseServiceResponse = ewsPinServicesResponse.getAssistedResponse();
            assertEquals(EwsServiceStatus.PendingActivation, ewsBaseServiceResponse.getStatus());
            assertNull(ewsPinServicesResponse.getDirectDepositResponse());
            assertNull(ewsPinServicesResponse.getCheckDistributionResponse());
            assertNull(ewsPinServicesResponse.getBillPaymentResponse());
            assertNull(ewsPinServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
