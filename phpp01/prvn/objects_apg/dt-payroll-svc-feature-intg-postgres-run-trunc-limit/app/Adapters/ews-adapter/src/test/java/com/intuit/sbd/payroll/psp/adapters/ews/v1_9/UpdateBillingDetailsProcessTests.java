package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsUpdateBillingDetails;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.UpdateBillingDetailsProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Jeff Jones
 */
public class UpdateBillingDetailsProcessTests {

    private EwsUpdateBillingDetails mRequest;
    private String psid = null;

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
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();
    }    

    @After
    public void after() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updateBillingDetails() {
        try {
            CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
            createAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = createAccountProcessTests.getPSID();
            
            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBillingDetails(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateBillingDetailsProcess process = new UpdateBillingDetailsProcess(mRequest);
            EwsResponse response = process.execute();
            
            assertEquals(0 , response.getEwsResponseStatus().getCode());
            assertEquals("Success",response.getEwsResponseStatus().getMessage());

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
}
