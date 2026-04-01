package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResetPin;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResetPinResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.ResetPinProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.AssetItemCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class ResetPinProcessTests {

    private EwsResetPin mRequest;
    private EwsResetPinResponse mResponse;
    private String psid = null;
    CreatePinProcessTests createPinProcessTests = new CreatePinProcessTests();
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
    public void ResetPinCloudTest() throws Exception {
        createPinProcessTests.createPinCloudOnly();
        psid = createPinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit eu = company.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.DIY);

        String ein = company.getFedTaxId();
        psid = company.getSourceCompanyId();
        String subscriptionNumber = eu.getEntitlement().getSubscriptionNumber();
        String quid = java.util.UUID.randomUUID().toString();
        Application.commitUnitOfWork();        

        String pinSignature = ein + ":" + psid + ":" + subscriptionNumber + ":" + quid;

        PrivateKey pk = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
        String encryptedPinSignature = PINUtils.getEncryptedValue(pinSignature, pk);

        mRequest = TestDataFactory.createEwsResetPin(psid, encryptedPinSignature);

        ResetPinProcess process = new ResetPinProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPin());
        assertNotNull(mResponse.getPsid());
        assertNotNull(mResponse.getPrivateKey());

        assertNull(mResponse.getServicesResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        //Reset PIN Test
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.ResetPIN), "v1_10/test_ResetPinCloudTest_ResetPIN.xml",
                Arrays.asList("DateTimeStamp", "PSID"));

        //Create Account Test
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.CreateAccount), "v1_10/test_ResetPinCloudTest_CreateAccount.xml",
                Arrays.asList("DateTimeStamp", "PSID", "EIN", "ServiceKey", "SubscriptionNumber"));

        //Create PIN Test
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.CreatePIN), "v1_10/test_ResetPinCloudTest_CreatePIN.xml",
                Arrays.asList("DateTimeStamp", "PSID"));
    }

    @Test
    public void ResetPinDDTest() throws Exception {
        createPinProcessTests.createPinCloudAndDD();
        psid = createPinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit eu = company.findEnabledEntitlementUnitByAssetItemCd(AssetItemCode.DIY);

        String ein = company.getFedTaxId();
        psid = company.getSourceCompanyId();
        String subscriptionNumber = eu.getEntitlement().getSubscriptionNumber();
        String quid = java.util.UUID.randomUUID().toString();
        Application.commitUnitOfWork();

        String pinSignature = ein + ":" + psid + ":" + subscriptionNumber + ":" + quid;

        PrivateKey pk = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
        String encryptedPinSignature = PINUtils.getEncryptedValue(pinSignature, pk);

        mRequest = TestDataFactory.createEwsResetPin(psid, encryptedPinSignature);

        ResetPinProcess process = new ResetPinProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPin());
        assertNotNull(mResponse.getPsid());
        assertNotNull(mResponse.getPrivateKey());

        assertNull(mResponse.getServicesResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void ResetPinAssistedTest() throws Exception {
        assistedTest = true;
        createPinProcessTests.createPinCloudAndAssisted();
        psid = createPinProcessTests.getPSID();
        
        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);

        String ein = company.getFedTaxId();
        psid = company.getSourceCompanyId();
        String subscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getSubscriptionNumber();
        String quid = java.util.UUID.randomUUID().toString();
        Application.commitUnitOfWork();

        String pinSignature = ein + ":" + psid + ":" + subscriptionNumber + ":" + quid;

        PrivateKey pk = PINUtils.getPrivateKeyFromString(company.getPrivateKey());
        String encryptedPinSignature = PINUtils.getEncryptedValue(pinSignature, pk);

        mRequest = TestDataFactory.createEwsResetPin(psid, encryptedPinSignature);

        ResetPinProcess process = new ResetPinProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPin());
        assertNotNull(mResponse.getPsid());
        assertNotNull(mResponse.getPrivateKey());

        assertNull(mResponse.getServicesResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }
}
