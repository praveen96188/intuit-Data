package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryServiceKey;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryServiceKeyCompany;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryServiceKeyItem;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryServiceKeyResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.QueryServiceKeyProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.domain.Offer;
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
public class QueryServiceKeyProcessTests {

    private EwsQueryServiceKey mRequest;
    private EwsQueryServiceKeyResponse mResponse;

    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();

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
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EWSAdapter));
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void queryServiceKeyEinNotFound() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            String psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryServiceKey(company);
            mRequest.setEin("000001000");
            Application.rollbackUnitOfWork();

            QueryServiceKeyProcess process = new QueryServiceKeyProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getDateTimeStamp());
            assertEquals(30102, mResponse.getEwsResponseStatus().getCode());
            assertEquals("EIN Does Not Exist", mResponse.getEwsResponseStatus().getMessage());

            assertEquals(0, mResponse.getCompanies().size());

            //Checking for null EIN
            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryServiceKey(company);
            mRequest.setEin(null);
            Application.rollbackUnitOfWork();

            process = new QueryServiceKeyProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getDateTimeStamp());
            assertEquals(30005, mResponse.getEwsResponseStatus().getCode());
            assertEquals("Field EIN in Request does not contain valid data", mResponse.getEwsResponseStatus().getMessage());

            assertEquals(0, mResponse.getCompanies().size());

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryServiceKeyActive() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            String psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryServiceKey(company);
            Application.rollbackUnitOfWork();

            QueryServiceKeyProcess process = new QueryServiceKeyProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getDateTimeStamp());
            assertEquals(0, mResponse.getEwsResponseStatus().getCode());
            assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());

            assertNotNull(mResponse.getCompanies());
            assertEquals(1, mResponse.getCompanies().size());
            EwsQueryServiceKeyCompany skCompany = mResponse.getCompanies().get(0);

            assertEquals("Acme Software", skCompany.getLegalName());
            assertEquals(1, skCompany.getServiceKeys().size());
            EwsQueryServiceKeyItem skItem = skCompany.getServiceKeys().get(0);

            assertNotNull(skItem.getServiceKey());
            assertEquals("Activated", skItem.getStatus());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryServiceKeyDeactivated() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            String psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            Application.save(entitlementUnit);

            mRequest = TestDataFactory.createEwsQueryServiceKey(company);
            Application.commitUnitOfWork();

            QueryServiceKeyProcess process = new QueryServiceKeyProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getDateTimeStamp());
            assertEquals(0, mResponse.getEwsResponseStatus().getCode());
            assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());

            assertNotNull(mResponse.getCompanies());
            assertEquals(1, mResponse.getCompanies().size());
            EwsQueryServiceKeyCompany skCompany = mResponse.getCompanies().get(0);

            assertEquals("Acme Software", skCompany.getLegalName());
            assertEquals(1, skCompany.getServiceKeys().size());
            EwsQueryServiceKeyItem skItem = skCompany.getServiceKeys().get(0);

            assertNotNull(skItem.getServiceKey());
            assertEquals("Deactivated", skItem.getStatus());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryServiceKeyCompanyDGDeleted() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            String psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryServiceKey(company);
            mRequest.setEin("000001000");
            Application.rollbackUnitOfWork();

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            company.setIsDgDisassociated(Boolean.TRUE);
            Application.save(company);
            Application.commitUnitOfWork();

            QueryServiceKeyProcess process = new QueryServiceKeyProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getDateTimeStamp());
            assertEquals(30102, mResponse.getEwsResponseStatus().getCode());
            assertEquals("EIN Does Not Exist", mResponse.getEwsResponseStatus().getMessage());

            assertEquals(0, mResponse.getCompanies().size());

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
}
