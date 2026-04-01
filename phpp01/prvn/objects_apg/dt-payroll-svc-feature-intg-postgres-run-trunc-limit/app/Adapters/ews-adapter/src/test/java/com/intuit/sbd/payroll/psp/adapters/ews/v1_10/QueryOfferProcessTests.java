package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryOffer;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryOfferResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.QueryOfferProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
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
 * @author Marcela Villani
 */
public class QueryOfferProcessTests {

    private EwsQueryOffer mRequest;
    private EwsQueryOfferResponse mResponse;

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
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void queryOffer() {
        try {
            mRequest = new EwsQueryOffer();
            mRequest.setIpAddress("127.0.0.1");
            mRequest.setOfferCode("P57213");

            QueryOfferProcess queryOfferProcess = new QueryOfferProcess(mRequest);
            mResponse = queryOfferProcess.execute();

            assertNotNull(mResponse);
            assertEquals("Offer Description", "20% discount on all fees", mResponse.getDescription());
            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void queryOfferAS400() {
        try {
            mRequest = new EwsQueryOffer();
            mRequest.setIpAddress("127.0.0.1");
            mRequest.setOfferCode("1099426");

            QueryOfferProcess queryOfferProcess = new QueryOfferProcess(mRequest);
            mResponse = queryOfferProcess.execute();

            assertNotNull(mResponse);
            assertEquals("Offer Code", "1099426", mResponse.getOfferCode());
            assertEquals("Offer Description", "Two months free", mResponse.getDescription().trim());
            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void queryOfferDoesNotExist() {
        try {
            mRequest = new EwsQueryOffer();
            mRequest.setIpAddress("127.0.0.1");
            mRequest.setOfferCode("NotAnOffer");

            QueryOfferProcess queryOfferProcess = new QueryOfferProcess(mRequest);
            mResponse = queryOfferProcess.execute();

            assertNotNull(mResponse);
            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals("Code: ", 30112, ewsResponseStatus.getCode());
            assertEquals("Message: ", "This Offer Code does not exist", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}