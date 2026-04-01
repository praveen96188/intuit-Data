package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsBasePin;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsBasePinResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsUpdatePin;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.AuthenticatePinProcess;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.UpdatePinProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jeff Jones
 */
public class UpdatePinProcessTests {

    private EwsUpdatePin mRequest;
    private EwsBasePinResponse mResponse;
    private String psid = null;
    private CreatePinProcessTests mCreatePinProcessTests = new CreatePinProcessTests();
    private Boolean assistedTest;

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
    public void updatePinCloud() throws Exception {
        mCreatePinProcessTests.createPinCloudOnly();
        psid = mCreatePinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        mRequest = TestDataFactory.createEwsUpdatePin(company.getSourceCompanyId());
        Application.commitUnitOfWork();

        UpdatePinProcess process = new UpdatePinProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        assertNotNull(mResponse.getPrivateKey());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull( mResponse.getDateTimeStamp());

        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.ChangePIN),"v1_10/test_updatePinCloud.xml",
                Arrays.asList("DateTimeStamp", "PSID"));
    }

    @Test
    public void updatePinAssisted() throws Exception {
        assistedTest = true;
        mCreatePinProcessTests.createPinCloudAndAssisted();
        psid = mCreatePinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        mRequest = TestDataFactory.createEwsUpdatePin(company.getSourceCompanyId());
        Application.commitUnitOfWork();

        UpdatePinProcess process = new UpdatePinProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        assertNotNull(mResponse.getPrivateKey());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull( mResponse.getDateTimeStamp());

        EwsBasePin mRequest2 = TestDataFactory.createEwsAuthenticatePin(company.getSourceCompanyId());
        mRequest2.setPin("1234ABCDabcd");

        AuthenticatePinProcess authenticatePinProcess = new AuthenticatePinProcess(mRequest2);
        mResponse = authenticatePinProcess.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        assertNotNull(mResponse.getPrivateKey());

        assertNotNull(mResponse.getEwsResponseStatus());
        ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());
    }

    @Test
    public void updatePinDD() throws Exception {
        mCreatePinProcessTests.createPinCloudAndDD();
        psid = mCreatePinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        mRequest = TestDataFactory.createEwsUpdatePin(company.getSourceCompanyId());
        Application.commitUnitOfWork();

        UpdatePinProcess process = new UpdatePinProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        assertNotNull(mResponse.getPrivateKey());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull( mResponse.getDateTimeStamp());
    }
}
