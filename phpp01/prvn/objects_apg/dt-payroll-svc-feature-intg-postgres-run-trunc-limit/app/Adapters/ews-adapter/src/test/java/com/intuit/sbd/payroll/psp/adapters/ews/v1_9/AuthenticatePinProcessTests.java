package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBasePin;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBasePinResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.AuthenticatePinProcess;
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
public class AuthenticatePinProcessTests {

    private EwsBasePin mRequest;
    private EwsBasePinResponse mResponse;

    private CreatePinProcessTests mCreatePinProcessTests = new CreatePinProcessTests();

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
    public void AuthPinCloudOnly() {
        try {
            mCreatePinProcessTests.createPinCloudOnly();
            psid = mCreatePinProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsAuthenticatePin(company.getSourceCompanyId());

            AuthenticatePinProcess authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();

            assertNotNull(mResponse);

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
    public void AuthPinCloudAndDD() {
        String psid = null;
        try {
            mCreatePinProcessTests.createPinCloudAndDD();
            psid = mCreatePinProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsAuthenticatePin(company.getSourceCompanyId());

            AuthenticatePinProcess authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();

            assertNotNull(mResponse);

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
    public void AuthPinCloudAndAssisted() {
        assistedTest = true;
        try {
            mCreatePinProcessTests.createPinCloudAndAssisted();
            psid = mCreatePinProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsAuthenticatePin(company.getSourceCompanyId());

            AuthenticatePinProcess authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();

            assertNotNull(mResponse);

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
    public void AuthPinCloudOnlyLockOut() {
        try {
            mCreatePinProcessTests.createPinCloudOnly();
            psid = mCreatePinProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsAuthenticatePin(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            mRequest.setPin("BadPin");

            //1st attempt
            AuthenticatePinProcess authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30103, ewsResponseStatus.getCode());
            assertEquals("PIN Authentication Failure", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

            //2nd attempt
            authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30103, ewsResponseStatus.getCode());
            assertEquals("PIN Authentication Failure", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

            //3rd attempt
             authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30108, ewsResponseStatus.getCode());
            assertEquals("PIN retry attempts exceeded. Retry after 15 min", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

            //Lockout wait 15 mins
                Application.beginUnitOfWork();
                SpcfCalendar pspDate = PSPDate.getPSPTime();
                pspDate.addMinutes(16);
                PSPDate.setPSPTime(pspDate);
                Application.commitUnitOfWork();

            mRequest.setPin("ABCDefgh1234");

            //Successful attempt
            authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

/*    @Test
    public void AuthPinAssistedLockOut() {
        try {
            mCreatePinProcessTests.createPinCloudAndAssisted();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompanyByEin("876543210", "0");
            mRequest = TestDataFactory.createEwsAuthenticatePin(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            mRequest.setPin("BadPin1234");

            //1st attempt
            AuthenticatePinProcess authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30103, ewsResponseStatus.getCode());
            assertEquals("PIN Authentication Failure", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

            //2nd attempt
            authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30103, ewsResponseStatus.getCode());
            assertEquals("PIN Authentication Failure", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

            //3rd attempt
             authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30108, ewsResponseStatus.getCode());
            assertEquals("PIN retry attempts exceeded. Retry after 15 min", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

            //Lockout wait 15 mins
            Thread.sleep(960000);

            mRequest.setPin("ABCDefgh1234");

            //Successful attempt
            authenticatePinProcess = new AuthenticatePinProcess(mRequest);
            mResponse = authenticatePinProcess.execute();
            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());
            assertNotNull( mResponse.getDateTimeStamp());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }*/
}
