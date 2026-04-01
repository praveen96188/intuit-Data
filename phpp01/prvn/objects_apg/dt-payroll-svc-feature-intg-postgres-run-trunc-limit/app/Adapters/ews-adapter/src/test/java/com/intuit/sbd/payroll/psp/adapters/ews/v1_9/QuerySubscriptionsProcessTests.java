package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsQuerySubscriptions;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsQuerySubscriptionsResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsSubscriptionResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEditionType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsTierType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.QuerySubscriptionsProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class QuerySubscriptionsProcessTests {

    private EwsQuerySubscriptions mRequest;
    private EwsQuerySubscriptionsResponse mResponse;
    private String psid = null;
    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();
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
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void querySubscriptionsCloudOnly() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQuerySubscriptions(company);
            Application.commitUnitOfWork();

            QuerySubscriptionsProcess process = new QuerySubscriptionsProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getEwsSubscriptionResponses());
            assertEquals(1, mResponse.getEwsSubscriptionResponses().size());

            EwsSubscriptionResponse ewsSubscriptionResponse = mResponse.getEwsSubscriptionResponses().get(0);
            assertNotNull(ewsSubscriptionResponse.getSubscriptionNumber());
            assertEquals("16", ewsSubscriptionResponse.getSubType());
            assertEquals("0", ewsSubscriptionResponse.getBillingAccountId());
            assertEquals("1", ewsSubscriptionResponse.getLicenseNumber());
            assertEquals("1234567890", ewsSubscriptionResponse.getEntitlementOfferingCode());
            assertEquals(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString(), ewsSubscriptionResponse.getAssetItemNumber());
            assertEquals(EwsEditionType.Basic, ewsSubscriptionResponse.getEdition());
            assertEquals(EwsTierType.One, ewsSubscriptionResponse.getTier());
            assertNull(ewsSubscriptionResponse.getBillingZip());
            assertEquals("test3@intuit.com", ewsSubscriptionResponse.getBuyerEmailAddress());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void querySubscriptionsDD() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQuerySubscriptions(company);
            Application.commitUnitOfWork();

            QuerySubscriptionsProcess process = new QuerySubscriptionsProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getEwsSubscriptionResponses());
            assertEquals(1, mResponse.getEwsSubscriptionResponses().size());

            EwsSubscriptionResponse ewsSubscriptionResponse = mResponse.getEwsSubscriptionResponses().get(0);
            assertNotNull(ewsSubscriptionResponse.getSubscriptionNumber());
            assertEquals("16", ewsSubscriptionResponse.getSubType());
            assertEquals("0", ewsSubscriptionResponse.getBillingAccountId());
            assertEquals("1", ewsSubscriptionResponse.getLicenseNumber());
            assertEquals("1234567890", ewsSubscriptionResponse.getEntitlementOfferingCode());
            assertEquals(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString(), ewsSubscriptionResponse.getAssetItemNumber());
            assertEquals(EwsEditionType.Basic, ewsSubscriptionResponse.getEdition());
            assertEquals(EwsTierType.One, ewsSubscriptionResponse.getTier());
            assertNull(ewsSubscriptionResponse.getBillingZip());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void querySubscriptionsAssisted() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQuerySubscriptions(company);
            Application.commitUnitOfWork();

            QuerySubscriptionsProcess process = new QuerySubscriptionsProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getEwsSubscriptionResponses());
            assertEquals(1, mResponse.getEwsSubscriptionResponses().size());

            EwsSubscriptionResponse ewsSubscriptionResponse = mResponse.getEwsSubscriptionResponses().get(0);
            assertNotNull(ewsSubscriptionResponse.getSubscriptionNumber());
            assertEquals("4", ewsSubscriptionResponse.getSubType());
            assertEquals("0", ewsSubscriptionResponse.getBillingAccountId());
            assertEquals("1", ewsSubscriptionResponse.getLicenseNumber());
            assertEquals("1234567890", ewsSubscriptionResponse.getEntitlementOfferingCode());
            assertEquals("1099750", ewsSubscriptionResponse.getAssetItemNumber());
            assertNull(ewsSubscriptionResponse.getEdition());
            assertNull(ewsSubscriptionResponse.getTier());
            assertNull(ewsSubscriptionResponse.getBillingZip());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void querySubscriptionsDoesNotExists() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQuerySubscriptions(company);
            mRequest.setSubscriptionNumber("999999999");
            Application.commitUnitOfWork();

            QuerySubscriptionsProcess process = new QuerySubscriptionsProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getEwsSubscriptionResponses());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30142, ewsResponseStatus.getCode());
            assertEquals("Subscription Number Does Not Exist.", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
}
